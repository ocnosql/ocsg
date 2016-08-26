package com.ai.ocsg.client;

import java.io.InputStream;

/**
 * Created by wangkai8 on 16/8/23.
 */
public class Response {

    private String fileName;

    private InputStream in;

    public Response(String fileName, InputStream in) {
        this.fileName = fileName;
        this.in = in;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getInputStream() {
        return in;
    }

    public void setInputStream(InputStream in) {
        this.in = in;
    }
}
