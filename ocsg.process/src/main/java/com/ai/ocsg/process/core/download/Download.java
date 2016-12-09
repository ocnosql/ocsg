package com.ai.ocsg.process.core.download;

import com.ai.ocsg.client.FileInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by wangkai8 on 16/8/24.
 */
public interface Download {

    public FileInfo getFileInfo() throws IOException;

    public void write(OutputStream out) throws IOException;

    public void close();
}
