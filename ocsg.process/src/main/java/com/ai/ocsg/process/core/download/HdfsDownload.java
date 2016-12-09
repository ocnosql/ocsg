package com.ai.ocsg.process.core.download;

import com.ai.ocsg.client.FileInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by wangkai8 on 16/11/2.
 */
public class HdfsDownload implements Download {

    public static final Log LOG = LogFactory.getLog(HdfsDownload.class);

    private Configuration conf;
    private String path;
    private FileSystem fs;
    private String realPath;
    private String fileName;

    public HdfsDownload(Configuration conf, String path) throws IOException {
        this.conf = conf;
        this.path = path;
        realPath = path.substring(path.indexOf("_") + 1);
        fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("_"));
        fs = FileSystem.get(conf);
    }


    @Override
    public FileInfo getFileInfo() throws IOException {
        FileStatus fileStatus = fs.getFileStatus(new Path(realPath));
        return new FileInfo(fileName, fileStatus.getLen() + "", realPath);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        String realPath = path.substring(path.indexOf("_") + 1);
        FSDataInputStream fis = fs.open(new Path(realPath));
        IOUtils.copyBytes(fis, out, conf);
    }

    @Override
    public void close() {

    }


}
