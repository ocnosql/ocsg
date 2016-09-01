package com.ai.ocsg.controller;

import com.ai.ocsg.process.conf.TableConfigruation;
import com.ai.ocsg.process.core.Upload;
import com.ai.ocsg.process.core.UploadFactory;
import com.ai.ocsg.process.utils.PropertiesUtil;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
        ServletFileUpload servletFileUpload = new ServletFileUpload();

        resp.setCharacterEncoding("utf-8");

        ServletOutputStream out = null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            FileItemIterator it = servletFileUpload.getItemIterator(req);
            out = resp.getOutputStream();

            while(it.hasNext()) {
                FileItemStream itemStream = it.next();
                InputStream in = itemStream.openStream();
                try {
                    if (itemStream.isFormField()) {
                        bos.reset();
                        IOUtils.copy(in, bos);
                        req.setAttribute(itemStream.getFieldName(), new String(bos.toByteArray()));
                    } else {
                        long fileSize = getFileSize(itemStream.getFieldName(), req);
                        if (fileSize == 0) {
                            continue;
                        }

                        Upload upload = UploadFactory.getInstance(fileSize, THRESTHOLD);

                        String returnPath = upload.upload(TableConfigruation.getConf(), in, itemStream.getName(), fileSize, req, resp);

                        out.write(returnPath.getBytes());

                    }
                } finally {
                    if(in != null) {
                        in.close();
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("", e);
            throw new RuntimeException(e);
        } finally {
            try {
                bos.close();
            } catch (IOException e) {

            }
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
