package com.ai.ocsg.client;

import java.io.Serializable;

/**
 * Created by wangkai8 on 16/11/2.
 */
public class FileInfo  implements Serializable {

    private String fileName;

    private long fileSize;

    private String path;

    public FileInfo() {

    }

    public FileInfo(String fileName, long fileSize, String path) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
