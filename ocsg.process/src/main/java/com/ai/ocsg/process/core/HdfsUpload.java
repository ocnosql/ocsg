package com.ai.ocsg.process.core;

import com.ai.ocsg.process.Constants;
import com.ai.ocsg.process.utils.DateUtil;
import com.ai.ocsg.process.utils.PropertiesUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

/**
 * Created by wangkai8 on 16/8/24.
 */
public class HdfsUpload implements Upload {

    public static final Log LOG = LogFactory.getLog(HdfsUpload.class);

    public static final String HDFS_UPLOAD_ROOT = PropertiesUtil.getProperty(RESOURCE_NAME, Constants.HDFS_UPLOAD_ROOT);


    @Override
    public String upload(Configuration conf, InputStream in, String fileName, long fileSize, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        FileSystem fs = FileSystem.get(conf);

        Path path = new Path(getStoreDir() + "/" + getStoreFileName(fileName));

        FSDataOutputStream fsout = fs.create(path);

        long start = System.currentTimeMillis();
        IOUtils.copyBytes(in, fsout, conf);
        LOG.info("upload file: " + fileName + " success, token: " + (System.currentTimeMillis() - start) + "ms, path: " + path.toString());

        return "hdfs_" + path.toString();
    }


    public String getStoreDir() {

        return HDFS_UPLOAD_ROOT + "/" + getSubStoreDir(256);
    }

    public String getStoreFileName(String fileName) {
        String uuid = UUID.randomUUID().toString();
        return fileName + "_" + uuid;
    }


    public String getSubStoreDir(int dirSubNum) {
        Random random = new Random();
        int level1 = random.nextInt(dirSubNum);
        int level2 = random.nextInt(dirSubNum);
        return DateUtil.getYearMonth() + "/" + formatNumber(level1, 3) + "/" + formatNumber(level2, 3);
    }


    public String formatNumber(int num, int length) {
        String num_ = num + "";
        int numLength = num_.length();
        if(numLength < length) {
            for(int i = 0; i < length - numLength; i++) {
                num_ = "0" + num_;
            }
        }
        return num_;
    }
}
