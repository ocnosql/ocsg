package com.ai.ocsg.client;

/**
 * Created by wangkai8 on 16/11/2.
 */
public class FileInfo {

    private String fileName;

    private String fileSize;

    private String path;

    public FileInfo(String fileName, String fileSize, String path) {
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

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
