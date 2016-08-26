package com.ai.ocsg.process.core;

import org.apache.hadoop.conf.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by wangkai8 on 16/8/24.
 */
public interface Download {

    public void download(Configuration conf, String fileStorePath, HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
