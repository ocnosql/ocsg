package com.ai.ocsg.controller;

import com.ai.ocsg.process.conf.TableConfigruation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by wangkai8 on 16/8/22.
 */
@Controller
@RequestMapping("/download")
public class DownloadController {

    public static final Log LOG = LogFactory.getLog(DownloadController.class);

    @RequestMapping(method = RequestMethod.GET)
    public void downloadWithGet(HttpServletRequest req, HttpServletResponse resp) {
        download(req, resp);
    }


    @RequestMapping(method = RequestMethod.POST)
    public void download(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/x-msdownload");
        String path = req.getParameter("filePath");
        ServletOutputStream out = null;
        try {
            out = resp.getOutputStream();
        } catch (IOException e) {
            LOG.error("", e);
        }

        String fileName;
        if(path.startsWith("hdfs")) {
            String realPath = path.substring(path.indexOf("_") + 1);
            fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("_"));
            try {
                fileName = URLEncoder.encode(fileName, "utf-8");
                resp.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                FileSystem fs = FileSystem.get(TableConfigruation.getConf());
                FSDataInputStream fis = fs.open(new Path(realPath));
                IOUtils.copyBytes(fis, out, TableConfigruation.getConf());
            } catch (IOException e) {
                LOG.error("", e);
            }

        } else if(path.startsWith("hbase")) {
            String tableName = path.substring(path.indexOf("_") + 1, path.lastIndexOf("_"));
            String rowkey = path.substring(path.lastIndexOf("_") + 1);

            try {
                Table table = TableConfigruation.getTable(tableName);

                //get file name from hbase
                Get get = new Get(rowkey.getBytes());
                get.addColumn("INFO".getBytes(), "FN".getBytes());
                Result fileInfo = table.get(get);
                fileName = new String(fileInfo.getValue("INFO".getBytes(), "FN".getBytes()));
                fileName = URLEncoder.encode(fileName, "utf-8");
                resp.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                Scan scan = new Scan();
                scan.setBatch(1);
                byte[] rowkeyByte = rowkey.getBytes();
                scan.setStartRow(rowkey.getBytes());
                rowkeyByte[rowkeyByte.length -1] ++;
                scan.setStopRow(rowkeyByte);
                scan.addFamily("F".getBytes());

                ResultScanner scanner = table.getScanner(scan);
                ByteArrayInputStream bis = null;

                for(Result result : scanner) {
                    CellScanner cellScanner = result.cellScanner();
                    while(cellScanner.advance()) {
                        Cell cell = cellScanner.current();
                        byte[] value = cell.getValue();
                        bis = new ByteArrayInputStream(value);
                        IOUtils.copyBytes(bis, out, TableConfigruation.getConf(), false);
                        bis.reset();
                    }
                }
                out.close();
                scanner.close();
                table.close();

            } catch (IOException e) {
                LOG.error("", e);
            }
        } else {
            throw new IllegalArgumentException("Illegal file path: " + path + ", the path should start with hdfs or hbase");
        }
    }
}
