package com.ai.ocsg.process.core.download;

import com.ai.ocsg.client.FileInfo;
import com.ai.ocsg.process.Constants;
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by wangkai8 on 16/11/2.
 */
public class HBaseDownload implements Download {

    public static final Log LOG = LogFactory.getLog(HBaseDownload.class);
    public static final long CACHE_FILE_SIZE = Long.parseLong(PropertiesUtil.getProperty(Constants.RESOURCE_NAME, "cache.file.size", Long.toString(100 * 1024)));

    private Configuration conf;
    private String path;
    private String tableName;
    private String rowkey;
    private Table table;
    private boolean isClosed;
    private FileInfo fileInfo;

    private BlockingQueue<ResultData> resultQueue = new ArrayBlockingQueue<ResultData>(60);

    public HBaseDownload(Configuration conf, String path) throws IOException {
        this.conf = conf;
        this.path = path;
        tableName = path.substring(path.indexOf("_") + 1, path.lastIndexOf("_"));
        rowkey = path.substring(path.lastIndexOf("_") + 1);
        table = TableConfigruation.getTable(tableName);
        isClosed = false;
    }


    @Override
    public FileInfo getFileInfo() throws IOException {
        checkClosed();
        //get file info from hbase
        Get get = new Get(rowkey.getBytes());
        get.addColumn(Constants.INFO_FAMILY, Constants.FILE_NAME);
        get.addColumn(Constants.INFO_FAMILY, Constants.FILE_LENGTH);
        Result info = table.get(get);
        String fileName = new String(info.getValue(Constants.INFO_FAMILY, Constants.FILE_NAME));
        String fileSize = new String(info.getValue(Constants.INFO_FAMILY, Constants.FILE_LENGTH));
        fileInfo = new FileInfo(fileName, fileSize, rowkey);
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
        checkClosed();

        FileScanner fileScanner = new FileScanner();
        fileScanner.start();

        long start = DateUtil.now();
        boolean complete = false;
        long fileSize = 0;

        try {
            while(!complete) {
                ResultData resultData = resultQueue.take();
                switch (resultData.getCode()) {
                    case DATA:
                        if(LOG.isDebugEnabled()) {
                            LOG.debug("fetch hbase split token: " + resultData.getScanCostTime() + "ms, " +
                                    "split in queue time: " + DateUtil.diff(resultData.getStartTimeInQueue()) + "ms");
                        }
                        fileSize += resultData.getSize();
                        out.write(resultData.getData());
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
            if(needCacheFile(Long.parseLong(fileInfo.getFileSize()), CACHE_FILE_SIZE)) {
                //cache.cache(fileInfo, data, timeout);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("download thread Interrupted", e);
        } finally {
            if(!fileScanner.isComplete) {
                fileScanner.interrupt();
            }
        }

//        LOG.info("file: " + path + " download complete, fileSize: " + fileSize + ", token: " + DateUtil.diff(start) + "ms");
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
