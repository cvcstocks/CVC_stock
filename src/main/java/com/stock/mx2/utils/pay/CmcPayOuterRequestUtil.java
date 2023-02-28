package com.stock.mx2.utils.pay;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CmcPayOuterRequestUtil {

    public static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {

            URL realUrl = new URL(url);
            // 打開和URL之間的連接
            URLConnection connection = realUrl.openConnection();
            // 設置通用的請求屬性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立實際的連接
            connection.connect();

            // 定義 BufferedReader輸入流來讀取URL的響應
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 使用finally塊來關閉輸入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打開和URL之間的連接
            URLConnection connection = realUrl.openConnection();
            // 設置通用的請求屬性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立實際的連接
            connection.connect();

            // 定義 BufferedReader輸入流來讀取URL的響應
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 使用finally塊來關閉輸入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String sendGet(String url, LinkedMap params) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = getUrlString(url, params);
            URL realUrl = new URL(urlNameString);
            // 打開和URL之間的連接
            URLConnection connection = realUrl.openConnection();
            // 設置通用的請求屬性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立實際的連接
            connection.connect();

            // 定義 BufferedReader輸入流來讀取URL的響應
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 使用finally塊來關閉輸入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String getUrlString(String url, LinkedMap params) throws UnsupportedEncodingException {
        return url + "?" + getParamsString(params);
    }

    /**
     * 封裝HTTP POST方法
     * @param
     * @param （如JSON串）
     * @return
     * @throws ClientProtocolException
     * @throws java.io.IOException
     */
    public static String post(String url, Map<String, String> paramMap) throws ClientProtocolException, IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> formparams = setHttpParams(paramMap);
        UrlEncodedFormEntity param = new UrlEncodedFormEntity(formparams, "utf-8");
        httpPost.setEntity(param);
        HttpResponse response = httpClient.execute(httpPost);
        String httpEntityContent = getHttpEntityContent(response);
        httpPost.abort();
        return httpEntityContent;
    }

    /**
     * 設置請求參數
     * @param
     * @return
     */
    private static List<NameValuePair> setHttpParams(Map<String, String> paramMap) {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        Set<Map.Entry<String, String>> set = paramMap.entrySet();
        for (Map.Entry<String, String> entry : set) {
            formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return formparams;
    }

    /**
     * 獲得響應HTTP實體內容
     * @param response
     * @return
     * @throws java.io.IOException
     * @throws java.io.UnsupportedEncodingException
     */
    private static String getHttpEntityContent(HttpResponse response) throws IOException, UnsupportedEncodingException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream is = entity.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = br.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line + "\n");
                line = br.readLine();
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * 向指定 URL 發送POST方法的請求
     *
     * @param url    發送請求的 URL
     * @param params 請求參數，請求參數应该是 name1=value1&name2=value2 的形式。
     * @return 所代表遠程資源的響應結果
     */
    public static String sendPost(String url, LinkedMap params) throws IOException {
        // Post請求的url，與get不同的是不需要帶參數
//        URL postUrl = new URL("http://127.0.0.1:8088/mall/user/test");
        String result = "";
        URL postUrl = new URL(url);
        // 打開連接
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();

        // 設置是否向connection輸出，因為這個是post請求，參數要放在
        // http正文內，因此需要設為true
        connection.setDoOutput(true);
        // Read from the connection. Default is true.
        connection.setDoInput(true);
        // 默認是 GET方式
        connection.setRequestMethod("POST");

        // Post 請求不能使用緩存
        connection.setUseCaches(false);

        connection.setInstanceFollowRedirects(true);

        // 配置本次連接的Content-type，配置為application/x-www-form-urlencoded的
        // 意思是正文是urlencoded編碼過的form參數，下面我們可以看到我們對正文內容使用URLEncoder.encode
        // 進行編碼
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // 連接，從postUrl.openConnection()至此的配置必須要在connect之前完成，
        // 要注意的是connection.getOutputStream會隱含的進行connect。


        connection.connect();
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        // The URL-encoded contend
        // 正文，正文內容其實跟get的URL中 '? '后的參数字符串一致
//        String content = "account=" + URLEncoder.encode("一個大肥人", "UTF-8");
//        content +="&pswd="+URLEncoder.encode("兩個個大肥人", "UTF-8");
//        // DataOutputStream.writeBytes將字符串中的16位的unicode字符以8位的字符形式寫到流裏面
//        out.writeBytes(content);
        String content = getParamsString(params);
        out.writeBytes(content);
        out.flush();
        out.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            result += line;
        }

        reader.close();
        connection.disconnect();
        return result;
    }

    public static String getParamsString(LinkedMap params) throws UnsupportedEncodingException {
        String content = new String();
        Set<String> keySet = params.keySet();
        String[] keys = keySet.toArray(new String[keySet.size()]);
        for (String key : keys) {
            String value = (String)params.get(key);
            if (value == null) continue;
            if (content.length() != 0) content += "&";
            content += key + "=" + URLEncoder.encode(value, "UTF-8");
        }
        return content;
    }

    private static final String SIGN_KEY = "sign";

    /** 密鑰屬性名key**/
    private static final String SECRET_KEY = "key";
    /**
     * 計算簽名
     *
     * @param jsonObj
     *            要參与簽名的json數據
     * @param md5Key
     *            密鑰
     * @return 簽名
     */
    public static String getSign(LinkedMap jsonObj, String md5Key) {
        if (jsonObj == null || jsonObj.isEmpty()) {
            return null;
        }
        String str2Sign = buildParam4Sign(jsonObj, SIGN_KEY, md5Key);
        try {
            byte[] data = str2Sign.getBytes("utf-8");
            String result = DigestUtils.md5Hex(data);
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 拼接用於簽名的參數
     * @param jsonObj
     * @return
     */
    private static String buildParam4Sign(LinkedMap jsonObj, String signKey, String md5Key) {
        Set<String> keySet = jsonObj.keySet();
        StringBuilder param = new StringBuilder(20 * keySet.size());
        String[] keys = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keys, String.CASE_INSENSITIVE_ORDER);
        /*for (String key : keys) {
            // 排除sign
            if (signKey.equals(key)) {
                continue;
            }
            Object value = jsonObj.get(key);
            // 排除值為null的情況
            param.append(key).append("=").append(value).append("&");
            //param.append(key).append("=").append(value);
        }
        param.deleteCharAt(param.length()-1);
        param.append(md5Key);*/
        param.append(jsonObj.get("merchantid"));
        param.append(jsonObj.get("orderno"));
        param.append(jsonObj.get("orderamount"));
        param.append(jsonObj.get("serverbackurl"));
        param.append(jsonObj.get("callbackurl"));
        param.append(md5Key);
        System.out.println(param.toString());
        return param.toString();
    }

    public static String getSignH5(LinkedMap jsonObj, String md5Key)  throws Exception{
        if (jsonObj == null || jsonObj.isEmpty()) {
            return null;
        }
        StringBuilder param = new StringBuilder();
        param.append("appid="+jsonObj.get("appid"));
        param.append("&data="+jsonObj.get("data"));
        param.append("&money="+jsonObj.get("money"));
        param.append("&type="+jsonObj.get("type"));
        param.append("&uip="+jsonObj.get("uip"));
        param.append("&appkey="+md5Key);
        String str2Sign = param.toString().toLowerCase();
        try {
            /*byte[] data = str2Sign.getBytes("utf-8");
            String result = DigestUtils.md5Hex(data);*/
            String result = encryption(str2Sign);
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * md5加密 32位 小寫
     * @param plainText
     * @return
     */
    public static String encryption(String plainText)  throws Exception{
        String re_md5 = new String();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return re_md5;
    }


}
