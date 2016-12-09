package com.ai.ocsg.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangkai8 on 16/12/1.
 */
public class UploadResponse {

    public static final int OK = 0;
    public static final int FAILED = 1;

    private int retCode;

    private List<FileInfo> data = new ArrayList<FileInfo>();

    private String errorMsg;

    public UploadResponse() {

    }

    public UploadResponse(int retCode, List data) {
        this.retCode = retCode;
        this.data = data;
    }

    public UploadResponse(int retCode, List data, String errorMsg) {
        this.retCode = retCode;
        this.data = data;
        this.errorMsg = errorMsg;
    }


    public UploadResponse(int retCode, String errorMsg) {
        this.retCode = retCode;
        this.errorMsg = errorMsg;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public List<FileInfo> getData() {
        return data;
    }

    public void setData(List<FileInfo> data) {
        this.data = data;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
