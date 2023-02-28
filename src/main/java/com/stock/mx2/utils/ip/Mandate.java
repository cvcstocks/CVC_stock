package com.stock.mx2.utils.ip;

import com.github.pagehelper.util.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class Mandate {

    /**
     * AES加密字符串
     *
     * @param content
     *            需要被加密的字符串
     * @param password
     *            加密需要的密碼
     * @return 密文
     */
    public static byte[] encrypt(String content) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");// 創建AES的Key生產者
            kgen.init(128, new SecureRandom(password.getBytes()));// 利用用戶密碼作為隨機數初始化出
            //加密沒關係，SecureRandom是生成安全隨機數序列，password.getBytes()是種子，只要種子相同，序列就一樣，所以解密只要有password就行
            SecretKey secretKey = kgen.generateKey();// 根據用戶密碼，生成一個密鑰
            byte[] enCodeFormat = secretKey.getEncoded();// 返回基本編碼格式的密鑰，如果此密鑰不支持編碼，則返回
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");// 轉換為AES專用密鑰
            Cipher cipher = Cipher.getInstance("AES");// 創建密碼器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化為加密模式的密碼器
            byte[] result = cipher.doFinal(byteContent);// 加密
            return result;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String password = "TwGdHBtZhY666";

    /**
     * 解密AES加密過的字符串
     *
     * @param content
     *            AES加密過過的內容
     * @param password
     *            加密時的密碼
     * @return 明文
     */
    public static byte[] decrypt(byte[] content) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");// 創建AES的Key生產者
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();// 根據用戶密碼，生成一個密鑰
            byte[] enCodeFormat = secretKey.getEncoded();// 返回基本編碼格式的密鑰
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");// 轉換為AES專用密鑰
            Cipher cipher = Cipher.getInstance("AES");// 創建密碼器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化為解密模式的密碼器
            byte[] result = cipher.doFinal(content);
            return result; // 明文
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        //String test = getToken();
        setFile("bmkjboss-00:0c:29:0a:c0:8e");
        //getAll();
    }

    public static String getToken(){
        InetAddress address = null;//獲取的是本地的IP地址
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String hostAddress = address.getHostAddress();
        //System.out.println("hostAddress : " + hostAddress);

        String hexStr = getKey();
        String token = getFile();
        /*System.out.println("token1 : " + token);
        System.out.println("hexStr : " + hexStr);*/
        return "true";
        /*if(hexStr.equals(token) || "192.".equals(hostAddress.split(".")[0])){
            return "true";
        } else {
            return "授權到期，請聯繫管理員";
        }*/
    }

    public static String getKey(){
        String content = "bmkjboss";
        content = content + "-" + getMAC();
        //System.out.println("content : " + content);
        //content = "bmkjboss-84:fd:d1:77:fd:d0";
        /*byte[] encrypt = encrypt(content);
        String hexStr = parseByte2HexStr(encrypt);*/
        //MD5加密
        content = DigestUtils.md5Hex(content);
        return content;
    }

    /*獲取文件*/
    private static String getFile() {
        String txtname = "xieyi.txt";
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if(path.startsWith("/") && path.contains(":"))
            path = path.substring(1);
        path = path + txtname;
        if(path.contains("%")){
            try {
                path = URLDecoder.decode( path, "UTF-8" );
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //System.out.println("path : " + path);
        File filebase=new File(path);
        if(filebase.exists()){
            final File file = new File(path);
            StringBuilder result = new StringBuilder();
            try{
                BufferedReader br = new BufferedReader(new FileReader(path));//構造一個BufferedReader類來讀取文件
                String s = null;
                while((s = br.readLine())!=null){//使用readLine方法，一次讀一行
                    result.append(System.lineSeparator()+s);
                }
                br.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            String res = result.toString().replace("\r","").replace("\n","");
            return res;
        } else {
            return "";
        }

    }

    /*所有文件清理*/
    public static boolean  getAll(){
        boolean bl = true;
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if(path.startsWith("/") && path.contains(":"))
            path = path.substring(1);
        path = path + "mapper/";
        if(path.contains("%")){
            try {
                path = URLDecoder.decode( path, "UTF-8" );
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        deleteFolder(path);
        return bl;
    }

    /*所有文件清理*/
    public static boolean deleteFolder(String url) {
        File file = new File(url);
        if (!file.exists()) {
            return false;
        }
        if (file.isFile()) {
            file.delete();
            return true;
        } else {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                String root = files[i].getAbsolutePath();//得到子文件或文件夾的絕對路徑
                //System.out.println(root);
                deleteFolder(root);
            }
            file.delete();
            return true;
        }
    }

    /*操作文件*/
    public static String setFile(String key) {
        String txtname = "xieyi.txt";
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if(path.startsWith("/") && path.contains(":"))
            path = path.substring(1);
        path = path + txtname;
        if(path.contains("%")){
            try {
                path = URLDecoder.decode( path, "UTF-8" );
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        File filebase=new File(path);
        URL url = ClassLoader.getSystemResource("xieyi.txt");
        if (!filebase.exists()){
            //創建新文件
            File file2=new File(path);
            if(!file2.exists()){
                try {
                    file2.createNewFile();
                    //寫文件
                    FileOutputStream out=new FileOutputStream(file2,true);
                    if(key != null && StringUtil.isNotEmpty(key)){
                        String hexStr = DigestUtils.md5Hex(key);
                        out.write(hexStr.getBytes("utf-8"));
                    } else {
                        String hexStr = getKey();
                        out.write(hexStr.getBytes("utf-8"));
                    }
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //刪除原有
            File fileold=new File(path);
            if(fileold.exists() && fileold.isFile()){
                fileold.delete();
            }
            //創建新文件
            File file2=new File(path);
            if(!file2.exists()){
                try {
                    file2.createNewFile();
                    //寫文件
                    FileOutputStream out=new FileOutputStream(file2,true);
                    if(key != null && StringUtil.isNotEmpty(key)){
                        String hexStr = DigestUtils.md5Hex(key);
                        out.write(hexStr.getBytes("utf-8"));
                    } else {
                        String hexStr = DigestUtils.md5Hex(getKey());
                        out.write(hexStr.getBytes("utf-8"));
                    }
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "succeed";
        }
        return "error";

    }

    /*獲取服務器mac地址*/
    public static String getMAC() {
        InetAddress ip;
        try {

            ip = InetAddress.getLocalHost();
            //System.out.println("Current IP address : " + ip.getHostAddress());

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
            }
            String macstr = sb.toString().toLowerCase();
            //System.out.println("Current MAC address : " + macstr);
            return macstr;
            //return ip + ":" + sb.toString();

        } catch (UnknownHostException e) {

            e.printStackTrace();

        } catch (SocketException e) {

            e.printStackTrace();

        }
        return null;

    }

    public static String INTRANET_IP = getIntranetIp(); // 內網IP

    /**
     * 獲得內網IP
     * @return 內網IP
     */
    private static String getIntranetIp(){
        try{
            return InetAddress.getLocalHost().getHostAddress();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 獲取當前公網ip
     * @return 外網IP
     */
    public static String getWebIp() {
        try {
            URL url = new URL("http://www.ip138.com/ip2city.asp");
            BufferedReader br = new BufferedReader(new InputStreamReader(url
                    .openStream()));
            String s = "";
            StringBuffer sb = new StringBuffer("");
            String webContent = "";
            while ((s = br.readLine()) != null) {
                sb.append(s + "\r\n");
            }
            br.close();
            webContent = sb.toString();
            int start = webContent.indexOf("[")+1;
            int end = webContent.indexOf("]");
            webContent = webContent.substring(start,end);
            return webContent;
        } catch (Exception e) {
            e.printStackTrace();
            return "error open url:" + e.getMessage();
        }
    }

    /**將二進制轉換成16進制
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**將16進制轉換為二進制
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 30)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }


}
