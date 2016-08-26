package com.ai.ocsg.controller;

import com.ai.ocsg.process.conf.TableConfigruation;
import com.ai.ocsg.process.core.Upload;
import com.ai.ocsg.process.core.UploadFactory;
import com.ai.ocsg.process.utils.PropertiesUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.io.IOUtils;
import org.apache.http.impl.auth.UnsupportedDigestAlgorithmException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Created by wangkai8 on 16/8/22.
 */

@Controller
@RequestMapping("/upload")
public class UploadController extends MultiActionController {

    public static final Log LOG = LogFactory.getLog(UploadController.class);

    public static final long THRESTHOLD = Long.parseLong(PropertiesUtil.getProperty("runtime.properties", "hdfs.upload.threshold", "67108864"));

    @RequestMapping(method = RequestMethod.GET)
    public void uploadWithGet(HttpServletRequest req, HttpServletResponse resp) {
        upload(req, resp);
    }


    @RequestMapping(method = RequestMethod.POST)
    public void upload(HttpServletRequest req, HttpServletResponse resp) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
        resp.setCharacterEncoding("utf-8");

        ServletOutputStream out = null;

        try {
            out = resp.getOutputStream();

            List<FileItem> list = (List<FileItem>) servletFileUpload.parseRequest(req);
            for(FileItem item : list) {
                if(item.isFormField()) {
                    req.setAttribute(item.getFieldName(), item.getString());
                }
            }

            for(FileItem item : list) {
                if(item.isFormField()) {
                    continue;
                }

                long fileSize = getFileSize(item.getFieldName(), req);

                if(fileSize == 0) {
                    continue;
                }

                Upload upload = UploadFactory.getInstance(fileSize, THRESTHOLD);

                String returnPath = upload.upload(TableConfigruation.getConf(), item.getInputStream(), item.getName(), fileSize, req, resp);

                out.write(returnPath.getBytes());
            }
        } catch (Exception e) {
            LOG.error("", e);
            throw new RuntimeException(e);
        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }


    public long getFileSize(String fieldName, HttpServletRequest req) {
        String fileSize = (String) req.getAttribute(fieldName + "_length");
        if(fileSize == null) {
            return 0;
        } else {
            return Long.parseLong(fileSize);
        }
    }

}
