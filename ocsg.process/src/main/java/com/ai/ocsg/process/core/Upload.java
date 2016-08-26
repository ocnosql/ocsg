package com.ai.ocsg.process.core;

import org.apache.hadoop.conf.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wangkai8 on 16/8/24.
 */
public interface Upload {

    public static final String RESOURCE_NAME = "runtime.properties";

    public String upload(Configuration conf, InputStream in, String fileName, long fileSize,
                         HttpServletRequest req, HttpServletResponse resp) throws IOException;

}
