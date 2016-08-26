package com.ai.ocsg.controller.test;

//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.NameValuePair;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.methods.multipart.FilePart;
//import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
//import org.apache.commons.httpclient.methods.multipart.Part;
//import org.apache.commons.httpclient.methods.multipart.StringPart;
//import org.apache.commons.httpclient.params.HttpMethodParams;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.junit.Test;
//
//import java.io.File;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by wangkai8 on 16/8/22.
 */
public class TestUpload {


//    @Test
//    public void testUpload() throws IOException {
//        HttpClient client = new HttpClient();
//        String url = "http://localhost:8080/upload";
//        PostMethod post = new PostMethod(url);
//
//        File file = new File("/Users/wangkai8/Downloads/末日孤舰.The.Last.Ship.S03E03.中英字幕.HDTVrip.1024X576.mp4");
//        //file = new File("/Users/wangkai8/Downloads/a.log");
//
//        Part[] parts = { new FilePart("myfile", file), new StringPart("myfile_length", file.length() + "")};
//
//        NameValuePair m = new NameValuePair("m", "xiaomiemie");
//        HttpMethodParams params = post.getParams();
//
////        post.setRequestBody(new NameValuePair[]{m});
//        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
//        post.setRequestEntity(new MultipartRequestEntity(parts, params));
//        client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
//
//        client.executeMethod(post);
//        byte[] responeText = post.getResponseBody();
//        System.out.println(new String(responeText));
//    }



    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            // 要上传的文件的路径
            String filePath = new String("/Users/wangkai8/Downloads/末日孤舰.The.Last.Ship.S03E03.中英字幕.HDTVrip.1024X576.mp4");
            // 把一个普通参数和文件上传给下面这个地址 是一个servlet
            HttpPost httpPost = new HttpPost(
                    "http://localhost:8080/upload");
            // 把文件转换成流对象FileBody
            File file = new File(filePath);
            FileBody bin = new FileBody(file);
            StringBody myfileLength = new StringBody(
                    file.length() + "", ContentType.create(
                    "text/plain", Consts.UTF_8));
            //以浏览器兼容模式运行，防止文件名乱码。
            HttpEntity reqEntity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("myfile", bin)
                    .addPart("myfile_length", myfileLength)
                    .setCharset(CharsetUtils.get("UTF-8")).build();

            httpPost.setEntity(reqEntity);

            System.out.println("发起请求的页面地址 " + httpPost.getRequestLine());
            // 发起请求 并返回请求的响应
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                System.out.println("----------------------------------------");
                // 打印响应状态
                System.out.println(response.getStatusLine());
                // 获取响应对象
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    // 打印响应长度
                    System.out.println("Response content length: "
                            + resEntity.getContentLength());
                    // 打印响应内容
                    System.out.println(EntityUtils.toString(resEntity,
                            Charset.forName("UTF-8")));
                }
                // 销毁
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

}
