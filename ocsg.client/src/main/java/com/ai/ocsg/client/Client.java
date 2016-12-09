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
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangkai8 on 16/8/23.
 */
public class Client {

    public static final Log LOG = LogFactory.getLog(Client.class);

    public static final String SUFFIX = "_length";

    public static final String DEFAULT_CHARSET = "UTF-8";

    private String uploadUrl;
    private String downloadUrl;

    public Client() {

    }

    public Client(String uploadUrl, String downloadUrl) {
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


    /**
     *
     * @param in
     * @param fileSize
     * @param fileName
     * @return
     * @throws IOException
     */
    public UploadResponse upload(InputStream in, long fileSize, String fileName) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            HttpPost httpPost = new HttpPost(uploadUrl);
            InputStreamBody inputStreamBody = new InputStreamBody(in, fileName);

            StringBody fileLength = new StringBody(Long.toString(fileSize), ContentType.create("text/plain", Consts.UTF_8));

            HttpEntity reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart(fileName + SUFFIX, fileLength)
                    .addPart(fileName, inputStreamBody)
                    .setCharset(CharsetUtils.get(DEFAULT_CHARSET)).build();

            httpPost.setEntity(reqEntity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode != 200) {
                    throw new IOException(response.getStatusLine().toString() + ", http code: " + statusCode);
                }
                HttpEntity resEntity = response.getEntity();
                ObjectMapper om = new ObjectMapper();
                UploadResponse uploadResponse =  om.readValue(EntityUtils.toByteArray(resEntity), UploadResponse.class);
                // 销毁
                EntityUtils.consume(resEntity);
                return uploadResponse;
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }



    public DownloadResponse download(String filePath) throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();

        HttpGet get = new HttpGet(downloadUrl + "?filePath=" + filePath);

        HttpResponse response = client.execute(get);

        String fileName = getFileName(response);

        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();

        return new DownloadResponse(fileName, is);
    }


    public static String getFileName(HttpResponse response) throws IOException {
        Header contentHeader = response.getFirstHeader("Content-Disposition");
        String filename = null;
        if (contentHeader != null) {
            HeaderElement[] values = contentHeader.getElements();
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename");
                if (param != null) {
                    try {
                        filename = new String(param.getValue().toString().getBytes("ISO-8859-1"), DEFAULT_CHARSET);
                    } catch (Exception e) {
                        throw new IOException("can't get file name");
                    }
                }
            }
        }
        return filename;
    }



    /**
     * usage:
     * upload http://localhost:8080/upload <localFilePath>
     * download http://localhost:8080/download <filePath> <localStoreFilePath>
     *
     * for example:
     * upload http://localhost:8082/ocsg/upload /Users/wangkai8/hbase简介.pdf
     * download http://localhost:8082/ocsg/download hbase_upload_201612_8983b4e9-cabd-4e49-a856-aca918b7acd3 /Users/wangkai8
     **/
    public static void main(String[] args) throws IOException {

        if(args.length < 3) {
            printUsage();
            System.exit(1);
        }

        Client client = new Client();

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

            UploadResponse response = client.upload(in, localFile.length(), fileName);
            if(response.getRetCode() == UploadResponse.OK) {
                LOG.info("upload file: " + filePath + " success! store path: " + response.getData().get(0).getPath());
            } else {
                LOG.error("upload exception", new IOException(response.getErrorMsg()));
            }


        } else if(args[0].equals("download")) {
            if(args.length < 4) {
                printUsage();
                System.exit(1);
            }
            client.setDownloadUrl(args[1]);
            DownloadResponse response = client.download(args[2]);
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
