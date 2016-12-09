package com.ai.ocsg.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangkai8 on 16/12/1.
 */
public class UploadResponse {

    private boolean isSuccess = true;

    private List<FileInfo> data = new ArrayList<FileInfo>();

    private String errorMsg;

    public UploadResponse(boolean isSuccess, List data) {
        this.isSuccess = isSuccess();
        this.data = data;
    }

    public UploadResponse(String isSuccess, List data, String errorMsg) {
        this.isSuccess = isSuccess();
        this.data = data;
        this.errorMsg = errorMsg;
    }


    public UploadResponse(boolean isSuccess, String errorMsg) {
        this.isSuccess = isSuccess;
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
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
