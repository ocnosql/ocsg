package com.ai.ocsg.process.core.download;

import com.ai.ocsg.client.FileInfo;
import com.ai.ocsg.process.Constants;
import com.ai.ocsg.process.cache.CacheData;
import com.ai.ocsg.process.cache.CacheException;
import com.ai.ocsg.process.cache.CacheFactory;
import com.ai.ocsg.process.cache.ICache;
import com.ai.ocsg.process.conf.TableConfigruation;
import com.ai.ocsg.process.core.ResultData;
import com.ai.ocsg.process.utils.DateUtil;
import com.ai.ocsg.process.utils.PropertiesUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.client.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wangkai8 on 16/11/2.
 */
public class HBaseDownload implements Download {

    public static final Log LOG = LogFactory.getLog(HBaseDownload.class);
    public static final long CACHE_FILE_SIZE = PropertiesUtil.getLong(Constants.RESOURCE_NAME, "cache.file.size", 100 * 1024);
    // unit is secs
    public static final long EXPIRE_TIME = PropertiesUtil.getLong(Constants.RESOURCE_NAME, "cache.file.expire.time", 5 * 60);

    public static final boolean cacheEnable = PropertiesUtil.getBoolean(Constants.RESOURCE_NAME, "cache.enable", false);

    public static final String cacheType = PropertiesUtil.getProperty(Constants.RESOURCE_NAME, "cache.type", "redis");

    public static final int resultQueueSize = PropertiesUtil.getInt(Constants.RESOURCE_NAME, Constants.HBASE_SPLIT_RESULT_QUEUE, 1);


    private ICache cache;

    private Configuration conf;
    private String path;
    private String tableName;
    private String rowkey;
    private Table table;
    private boolean isClosed;
    private BlockingQueue<ResultData> resultQueue;
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private FileInfo fileInfo;
    private boolean cacheFile;
    private CacheData cacheData;

    public HBaseDownload(Configuration conf, String path) throws IOException {
        this.conf = conf;
        this.path = path;
        tableName = path.substring(path.indexOf("_") + 1, path.lastIndexOf("_"));
        rowkey = path.substring(path.lastIndexOf("_") + 1);
        table = TableConfigruation.getTable(tableName);
        isClosed = false;
        cache = CacheFactory.getCache(cacheType);
        queryFileInfo();
        cacheFile = cacheEnable && needCacheFile(fileInfo.getFileSize(), CACHE_FILE_SIZE);

        resultQueue = new ArrayBlockingQueue<ResultData>(resultQueueSize);
    }

    public void queryFileInfo() throws IOException {
        if(cacheEnable) {
            try {
                cacheData = (CacheData) cache.getValue(path);
            } catch (CacheException e) {
                throw new IOException("get data from cache exception", e);
            }
            if(cacheData != null) {
                fileInfo = cacheData.getFileInfo();
            }
        }
        if(fileInfo == null) {
            checkClosed();
            //get file info from hbase
            Get get = new Get(rowkey.getBytes());
            get.addColumn(Constants.INFO_FAMILY, Constants.FILE_NAME);
            get.addColumn(Constants.INFO_FAMILY, Constants.FILE_LENGTH);
            Result info = table.get(get);
            String fileName = new String(info.getValue(Constants.INFO_FAMILY, Constants.FILE_NAME));
            String fileSize = new String(info.getValue(Constants.INFO_FAMILY, Constants.FILE_LENGTH));
            fileInfo = new FileInfo(fileName, Long.parseLong(fileSize), rowkey);
        }
    }


    @Override
    public FileInfo getFileInfo() throws IOException {
        return fileInfo;
    }


    public void writeDirect(OutputStream out) throws IOException {

        Scan scan = new Scan();
        scan.setBatch(1);
        byte[] rowkeyByte = rowkey.getBytes();
        scan.setStartRow(rowkey.getBytes());
        rowkeyByte[rowkeyByte.length - 1]++;
        scan.setStopRow(rowkeyByte);
        scan.addFamily(Constants.DATA_FAMILY);

        long startTime = DateUtil.now();
        long fileSize = 0;
        ResultScanner scanner = table.getScanner(scan);

        for(Result result : scanner) {
            CellScanner cellScanner = result.cellScanner();
            while(cellScanner.advance()) {
                Cell cell = cellScanner.current();
                byte[] value = cell.getValue();
                fileSize += value.length;
                out.write(value);
            }
        }
        scanner.close();
        LOG.info("file: " + path + " download complete, fileSize: " + fileSize + ", token: " + DateUtil.diff(startTime) + "ms");
    }


    @Override
    public void write(OutputStream out) throws IOException {

        if(cacheEnable && cacheData != null) {
            writeFromCache(out);
            return;
        }

        if(resultQueueSize == 0) {
            writeDirect(out);
            return;
        }

        checkClosed();

        FileScanner fileScanner = new FileScanner();
        fileScanner.start();

        boolean complete = false;

        try {
            while(!complete) {
                ResultData resultData = resultQueue.take();
                switch (resultData.getCode()) {
                    case DATA:
                        if(LOG.isDebugEnabled()) {
                            LOG.debug("fetch hbase split token: " + resultData.getScanCostTime() + "ms, " +
                                    "split in queue time: " + DateUtil.diff(resultData.getStartTimeInQueue()) + "ms");
                        }
                        out.write(resultData.getData());
                        if(cacheFile)
                            bos.write(resultData.getData());
                        break;
                    case FINISHI_FLAG:
                        complete = true;
                        break;
                    case ERROR:
                        Throwable throwable = resultData.getThrowable();
                        if(throwable instanceof IOException) {
                            throw (IOException) throwable;
                        } else {
                            throw new RuntimeException("fileScanner thread exception", throwable);
                        }
                    default:
                        break;
                }
            }

            if(cacheFile) {
                try {
                    cache.cacheValue(path, new CacheData(fileInfo, bos.toByteArray()), EXPIRE_TIME);
                } catch (CacheException e) {
                    throw new IOException(e);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("download thread Interrupted", e);
        } finally {
            if(!fileScanner.isComplete) {
                fileScanner.interrupt();
            }
        }
    }


    public void writeFromCache(OutputStream out) throws IOException {
        out.write(cacheData.getData());
    }


    public void checkClosed() throws IOException {
        if(isClosed) {
            throw new IOException("hbaseDownload is closed");
        }
    }


    @Override
    public void close() {
        isClosed = true;
        if(table != null) {
            try {
                table.close();
            } catch (IOException e) {
                LOG.error("close table: " + table + " exception", e);
            }
        }
    }


    public boolean needCacheFile(long fileSize, long cacheFileSize) {
        return fileSize <= cacheFileSize ? true : false;
    }


    class FileScanner extends Thread {

        boolean isComplete = false;

        @Override
        public void run() {
            try {
                Scan scan = new Scan();
                scan.setBatch(1);
                byte[] rowkeyByte = rowkey.getBytes();
                scan.setStartRow(rowkey.getBytes());
                rowkeyByte[rowkeyByte.length - 1]++;
                scan.setStopRow(rowkeyByte);
                scan.addFamily(Constants.DATA_FAMILY);

                long startTime = DateUtil.now();
                ResultScanner scanner = table.getScanner(scan);

                for(Result result : scanner) {
                    CellScanner cellScanner = result.cellScanner();
                    while(cellScanner.advance()) {
                        Cell cell = cellScanner.current();
                        byte[] value = cell.getValue();
                        resultQueue.put(new ResultData(ResultData.Code.DATA, value, DateUtil.diff(startTime)));
                    }
                    startTime = DateUtil.now();
                }
                scanner.close();

                //add finish flag
                resultQueue.put(new ResultData(ResultData.Code.FINISHI_FLAG));
                isComplete = true;
            } catch (Throwable e) {
                // add exception flag
                try {
                    resultQueue.put(new ResultData(ResultData.Code.ERROR, e));
                } catch (InterruptedException ex) {
                }
                isComplete = true;
            }
        }
    }

}
