package com.ai.ocsg.process.core;

import com.ai.ocsg.process.core.upload.HBaseUpload;
import com.ai.ocsg.process.core.upload.HdfsUpload;
import com.ai.ocsg.process.core.upload.Upload;

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
