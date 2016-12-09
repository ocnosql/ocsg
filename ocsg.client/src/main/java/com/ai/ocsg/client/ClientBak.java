package com.ai.ocsg.client;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangkai8 on 16/8/23.
 */
public class ClientBak {

    public static final Log LOG = LogFactory.getLog(ClientBak.class);

    private String uploadUrl;
    private String downloadUrl;

    public ClientBak() {

    }

    public ClientBak(String uploadUrl, String downloadUrl) {
        this.uploadUrl = uploadUrl;
        this.downloadUrl = downloadUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String upload(InputStream in, long fileSize, String fileName) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            // 要上传的文件的路径
//            String filePath = new String("/Users/wangkai8/Downloads/末日孤舰.The.Last.Ship.S03E03.中英字幕.HDTVrip.1024X576.mp4");
            // 把一个普通参数和文件上传给下面这个地址 是一个servlet
            HttpPost httpPost = new HttpPost(uploadUrl);
            // 把文件转换成流对象FileBody
//            File file = new File(filePath);
//            FileBody bin = new FileBody(file);
            InputStreamBody inputStreamBody = new InputStreamBody(in, fileName);
            StringBody myfileLength = new StringBody(
                    fileSize + "", ContentType.create(
                    "text/plain", Consts.UTF_8));
            //以浏览器兼容模式运行，防止文件名乱码。
            HttpEntity reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("myfile", inputStreamBody)
                    .addPart("myfile_length", myfileLength)
                    .setCharset(CharsetUtils.get("UTF-8")).build();

            httpPost.setEntity(reqEntity);

//            System.out.println("发起请求的页面地址 " + httpPost.getRequestLine());
            // 发起请求 并返回请求的响应
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
//                System.out.println("----------------------------------------");
                // 打印响应状态
//                System.out.println(response.getStatusLine());
                // 获取响应对象
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
//                    // 打印响应长度
//                    System.out.println("Response content length: "
//                            + resEntity.getContentLength());
//                    // 打印响应内容
//                    System.out.println(EntityUtils.toString(resEntity,
//                            Charset.forName("UTF-8")));
                    return EntityUtils.toString(resEntity, Charset.forName("UTF-8"));
                }
                // 销毁
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
        return null;
    }



    public Response download(String filePath) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(downloadUrl + "?filePath=" + filePath);

        HttpResponse response = client.execute(httpget);

        String fileName = getFileName(response);

        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();

        return new Response(fileName, is);
    }


    public static String getFileName(HttpResponse response) {
        Header contentHeader = response.getFirstHeader("Content-Disposition");
        String filename = null;
        if (contentHeader != null) {
            HeaderElement[] values = contentHeader.getElements();
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename");
                if (param != null) {
                    try {
                        filename = new String(param.getValue().toString().getBytes(), "utf-8");
                        filename = URLDecoder.decode(param.getValue(), "utf-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return filename;
    }

    // upload http://localhost:8080/upload /Users/wangkai8/maintain/phone.txt
    // download http://localhost:8080/download hbase_upload_201608_e8490730-d6c6-4ee8-97b9-fc60528ec051 /Users/wangkai8
    // upload http://localhost:8080/upload <localFilePath>
    // download http://localhost:8080/download <filePath> <localStoreFilePath>
    public static void main(String[] args) throws IOException {

        if(args.length < 3) {
            printUsage();
            System.exit(1);
        }

        ClientBak client = new ClientBak();

        if(args[0].equals("upload")) {
            client.setUploadUrl(args[1]);
            String filePath = args[2];
            File localFile = new File(filePath);

            if(localFile.isDirectory()) {
                System.err.println("file path: " + filePath + " is a dir, but need file");
                System.exit(1);
            }

            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

            FileInputStream in = new FileInputStream(localFile);

            String storePath = client.upload(in, localFile.length(), fileName);

            LOG.info("upload file: " + filePath + " success! store path: " + storePath);

        } else if(args[0].equals("download")) {
            if(args.length < 4) {
                printUsage();
                System.exit(1);
            }
            client.setDownloadUrl(args[1]);
            Response response = client.download(args[2]);
            String fileName = response.getFileName();
            File localFile = new File(args[3] + "/" + fileName);
            while(localFile.exists()) {
                if(fileName.lastIndexOf(".") != -1) {
                    String filePrefix = fileName.substring(0, fileName.lastIndexOf("."));
                    String suffix = fileName.substring(fileName.lastIndexOf("."));
                    localFile = new File(args[3] + "/" + recreateFileName(filePrefix) + suffix);
                } else {
                    localFile = new File(args[3] + "/" +  recreateFileName(fileName));
                }
                fileName = localFile.getName();
            }
            FileOutputStream out = new FileOutputStream(localFile);
            IOUtils.copy(response.getInputStream(), out);
            LOG.info("download file success! file path: " + localFile.getAbsolutePath());
        } else {
            printUsage();
            throw new IllegalArgumentException("args illegal");
        }
    }

    public static void printUsage() {
        System.err.println("usage: [upload <upload_url> <local_file_path>] or [download <download_url> <file_pah> <local_store_dir>] ");
    }

    public static String recreateFileName(String filePrefix) {
        Pattern pattern = Pattern.compile(".+(\\((\\d+)\\)$)");
        Matcher matcher = pattern.matcher(filePrefix);
        String newFilePrefix = filePrefix;
        if(matcher.find()) {
            String group1 = matcher.group(1);
            String group2 = matcher.group(2);
            newFilePrefix = filePrefix.substring(0, filePrefix.lastIndexOf(group1)) + "(" + (Integer.parseInt(group2) + 1) + ")";
        } else {
            newFilePrefix = newFilePrefix + "(1)";
        }
        return newFilePrefix;
    }

}
