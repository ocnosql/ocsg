package com.ai.ocsg.process.core.upload;

import com.ai.ocsg.process.Constants;
import com.ai.ocsg.process.conf.TableConfigruation;
import com.ai.ocsg.process.utils.DateUtil;
import com.ai.ocsg.process.utils.PropertiesUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by wangkai8 on 16/8/24.
 */
public class HBaseUpload implements Upload {

    public static final Log LOG = LogFactory.getLog(HBaseUpload.class);

    public int HBASE_SPLIT_SIZE = Integer.parseInt(PropertiesUtil.getProperty(RESOURCE_NAME, Constants.HBASE_SPLIT_SIZE, 1024 * 1024 + ""));

    public static final String TABLE_PREFIX = PropertiesUtil.getProperty(RESOURCE_NAME, Constants.HBASE_UPLOAD_TABLE_PREFIX);

    public HBaseUpload() {
        if(HBASE_SPLIT_SIZE <= 0) {
            HBASE_SPLIT_SIZE = 1024 * 1024;
        }
    }


    @Override
    public String upload(Configuration conf, InputStream in, String fileName, long fileSize) throws IOException {

        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(HBASE_SPLIT_SIZE);

        int count;
        int readCount = 0;
        int fieldIndex = 0;

        byte[] buffer = new byte[1];

        String uuid = UUID.randomUUID().toString();
        String tableName = TABLE_PREFIX + "_" + DateUtil.getYearMonth();
        Table table = TableConfigruation.getTable(tableName);

        long start = System.currentTimeMillis();
        while((count = bis.read(buffer)) != -1) {
            bos.write(buffer);
            readCount += count;
            if(readCount != 0 && readCount % HBASE_SPLIT_SIZE == 0) {
                Put put = new Put(uuid.getBytes());
                put.addColumn(Constants.DATA_FAMILY, getColumnName(fileSize, fieldIndex).getBytes(), bos.toByteArray());
                table.put(put);
                bos.reset();
                fieldIndex ++;
            }
        }
        if(bos.size() > 0) {
            Put put = new Put(uuid.getBytes());
            put.addColumn(Constants.DATA_FAMILY, getColumnName(fileSize, fieldIndex).getBytes(), bos.toByteArray());
            table.put(put);
            bos.reset();
        }

        //store file info, such as file name, file length
        Put fileInfo = new Put(uuid.getBytes());
        fileInfo.addColumn(Constants.INFO_FAMILY, Constants.FILE_NAME, fileName.getBytes());
        fileInfo.addColumn(Constants.INFO_FAMILY, Constants.FILE_LENGTH, (readCount + "").getBytes());
        table.put(fileInfo);

        LOG.info("upload file: " + fileName + " success, size: " + readCount + ", token: " + (System.currentTimeMillis() - start)
                + "ms, table: " + table.toString() + ", rowkey: " + uuid);
        return "hbase_" + tableName + "_" + uuid;
    }


    public String getColumnName(long fileSize, int index) {
        long splitNum = fileSize / HBASE_SPLIT_SIZE == 0 ? fileSize / HBASE_SPLIT_SIZE : fileSize / HBASE_SPLIT_SIZE + 1;
        String indexStr = Integer.toString(index);
        String splitNumStr = Long.toString(splitNum);
        for(int i = indexStr.length(); i < splitNumStr.length(); i ++) {
            indexStr = "0" + indexStr;
        }
        return indexStr;
    }

}
