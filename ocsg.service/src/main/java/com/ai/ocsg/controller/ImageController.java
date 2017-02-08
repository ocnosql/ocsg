package com.ai.ocsg.controller;

import com.ai.ocsg.process.conf.TableConfigruation;
import com.ai.ocsg.process.core.download.Download;
import com.ai.ocsg.process.core.DownloadFactory;
import com.ai.ocsg.process.core.download.HBaseDownload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by wangkai8 on 16/11/2.
 */
@Controller
@RequestMapping("/image")
public class ImageController {

    public static final Log LOG = LogFactory.getLog(ImageController.class);

    @RequestMapping(method = RequestMethod.GET)
    public void uploadWithGet(HttpServletRequest req, HttpServletResponse resp) {
        upload(req, resp);
    }


    @RequestMapping(method = RequestMethod.POST)
    public void upload(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("image/jpeg");
        String path = req.getParameter("filePath");
        String opt = req.getParameter("opt");
        ServletOutputStream out;
        try {
            out = resp.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Configuration conf = TableConfigruation.getConf();
        Download download = null;
        try {
            download = DownloadFactory.getDownload(conf, path);

            //begin download, write data to response out socket ...
            if("dict".equals(opt) && (download instanceof HBaseDownload)) {
                ((HBaseDownload) download).writeDirect(out);
            } else {
                download.write(out);
            }



        } catch (Throwable e) {
            LOG.error("download exception", e);
            throw new RuntimeException(e);
        } finally {
            //file download complete, close the resources
            try {
                if (download != null)
                    download.close();
            } catch (Throwable e) {
            }

            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }
}
