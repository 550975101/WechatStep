package com.wechat.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class WechatStep {


    private String salt = "8061FD";

    public String coreApp(String account, String step) {
        String message = "";
        try {
            long l = System.currentTimeMillis();
            String md5Sign = getMD5Sign(l, account);
            List<NameValuePair> parameters = new ArrayList<>(3);
            parameters.add(new BasicNameValuePair("accountId", account));
            parameters.add(new BasicNameValuePair("timeStamp", l + ""));
            parameters.add(new BasicNameValuePair("sign", md5Sign));
            String s = sendPost("http://weixin.droi.com/health/phone/index.php/SendWechat/getWxOpenid", parameters);
            JSONObject jsonObject = JSONObject.parseObject(s);
            Integer code = jsonObject.getInteger("code");
            if (0 == code) {
                String openid = jsonObject.getString("openid");
                long l2 = System.currentTimeMillis();
                String md5Sign2 = getMD5Sign(l2, openid, account, step);
                List<NameValuePair> parameters2 = new ArrayList<>(4);
                parameters2.add(new BasicNameValuePair("accountId", account));
                parameters2.add(new BasicNameValuePair("jibuNuber", step));
                parameters2.add(new BasicNameValuePair("timeStamp", l2+""));
                parameters2.add(new BasicNameValuePair("sign", md5Sign2));
                String s2 = sendPost("http://weixin.droi.com/health/phone/index.php/SendWechat/stepSubmit", parameters2);
                JSONObject jsonObject1 = JSONObject.parseObject(s2);
                message = jsonObject1.getString("messsage");
            } else {
                message = jsonObject.getString("messsage");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return message;
    }



    public  String getMD5Sign(long l,String account) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        String s = account + salt + l;
        m.update(s.getBytes());
        byte[] digest = m.digest();
        String result = "";
        for (int i = 0; i < digest.length; i++) {
            result += Integer.toHexString((0x000000FF & digest[i]) | 0xFFFFFF00).substring(6);
        }
        return result;
    }

    public  String getMD5Sign(long l,String openId,String account,String step) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        String s = account + salt + step + salt + l + salt + openId;
        m.update(s.getBytes());
        byte[] digest = m.digest();
        String result = "";
        for (int i = 0; i < digest.length; i++) {
            result += Integer.toHexString((0x000000FF & digest[i]) | 0xFFFFFF00).substring(6);
        }
        return result;
    }


    //unicode解码
    public  String unicodeToString(String unicode) {
        if (unicode == null || "".equals(unicode)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;
        while ((i = unicode.indexOf("\\u", pos)) != -1) {
            sb.append(unicode.substring(pos, i));
            if (i + 5 < unicode.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16));
            }
        }
        return sb.toString();
    }


    //httpclient发送http post请求
    public  String sendPost(String uriPath, List<NameValuePair> ns) throws URISyntaxException, ClientProtocolException, IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(ns);
        HttpPost httpPost = new HttpPost(uriPath);
        httpPost.setEntity(formEntity);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                // 解析响应体
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
        return "";
    }
}
