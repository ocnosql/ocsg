package com.ai.ocsg.process.core;

/**
 * Created by wangkai8 on 16/8/25.
 */
public class UploadFactory {

    public static Upload getInstance(long fileSize, long thresthold) {
        if(fileSize >= thresthold) {
            return new HdfsUpload();
        } else {
            return new HBaseUpload();
        }
    }
}
