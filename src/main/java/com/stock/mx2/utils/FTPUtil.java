package com.stock.mx2.utils;


import java.io.File;

import java.io.FileInputStream;

import java.io.IOException;

import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;


public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");

    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private String ip;
    private int port;
    private String user;

    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");
    private String pwd;
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {

        this.ip = ip;

        this.port = port;

        this.user = user;

        this.pwd = pwd;

    }

    public static boolean uploadFile(List<File> fileList) throws IOException {

        FTPUtil ftpUtil = new FTPUtil(ftpIp, 39036, ftpUser, ftpPass);

        logger.info("開始連接ftp服務器");


        boolean result = ftpUtil.uploadFile("img", fileList);

        logger.info("開始連接ftp服務器,結束上傳,上傳結果:{}", Boolean.valueOf(result));

        return result;

    }


    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {

        boolean uploaded = true;

        FileInputStream fis = null;


        if (connectServer(this.ip, this.port, this.user, this.pwd)) {

            try {

                this.ftpClient.changeWorkingDirectory(remotePath);

                this.ftpClient.setBufferSize(1024);

                this.ftpClient.setControlEncoding("UTF-8");

                this.ftpClient.setFileType(2);

                this.ftpClient.enterLocalPassiveMode();

                for (File fileItem : fileList) {

                    fis = new FileInputStream(fileItem);

                    this.ftpClient.storeFile(fileItem.getName(), fis);

                }


            } catch (IOException e) {

                logger.error("上傳文件異常", e);

                uploaded = false;

                e.printStackTrace();

            } finally {

                fis.close();

                this.ftpClient.disconnect();

            }

        }

        return uploaded;

    }


    private boolean connectServer(String ip, int port, String user, String pwd) {

        boolean isSuccess = false;

        this.ftpClient = new FTPClient();

        try {

            this.ftpClient.connect(ip,port);

            isSuccess = this.ftpClient.login(user, pwd);

        } catch (IOException e) {

            logger.error("連接FTP服務器異常", e);

        }

        return isSuccess;

    }


    public String getIp() {
        return this.ip;
    }


    public void setIp(String ip) {
        this.ip = ip;
    }


    public int getPort() {
        return this.port;
    }


    public void setPort(int port) {
        this.port = port;
    }


    public String getUser() {
        return this.user;
    }


    public void setUser(String user) {
        this.user = user;
    }


    public String getPwd() {
        return this.pwd;
    }


    public void setPwd(String pwd) {
        this.pwd = pwd;
    }


    public FTPClient getFtpClient() {
        return this.ftpClient;
    }


    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

}
