package com.ai.ocsg.process.core;

import com.ai.ocsg.process.core.download.Download;
import com.ai.ocsg.process.core.download.HBaseDownload;
import com.ai.ocsg.process.core.download.HdfsDownload;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

/**
 * Created by wangkai8 on 16/11/2.
 */
public class DownloadFactory {

    public static Download getDownload(Configuration conf, String path) throws IOException {
        if(path.startsWith("hdfs")) {
            return new HdfsDownload(conf, path);
        } else if(path.startsWith("hbase")) {
            return new HBaseDownload(conf, path);
        } else {
            throw new IllegalArgumentException("Illegal file path: " + path + ", the path should start with hdfs or hbase");
        }
    }
}
