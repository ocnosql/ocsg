package com.ai.ocsg.process.cache;

import com.ai.ocsg.client.FileInfo;

import java.io.Serializable;

/**
 * Created by wangkai8 on 16/12/7.
 */
public class CacheData implements Serializable {
    private com.ai.ocsg.client.FileInfo FileInfo;
    private byte[] data;

    public CacheData(FileInfo fileInfo, byte[] data) {
        FileInfo = fileInfo;
        this.data = data;
    }

    public FileInfo getFileInfo() {
        return FileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        FileInfo = fileInfo;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
