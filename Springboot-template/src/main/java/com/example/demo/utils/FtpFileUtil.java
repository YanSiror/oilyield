package com.example.demo.utils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FtpFileUtil {
    public static boolean uploadToFtp(String name, InputStream inputStream){
        FTPClient ftpClient = new FTPClient();
        try {
            //连接ftp服务器 参数填服务器的ip
            ftpClient.connect("162.14.77.92");

            //进行登录 参数分别为账号 密码
            ftpClient.login("yanjing","yan19991001.");
            //改变工作目录（按自己需要是否改变）
            //只能选择local_root下已存在的目录
            ftpClient.changeWorkingDirectory("images");
            //设置文件类型为二进制文件
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //开启被动模式（按自己如何配置的ftp服务器来决定是否开启）
            //ftpClient.enterLocalPassiveMode();
            //上传文件 参数：上传后的文件名，输入流
            ftpClient.storeFile(name, new FileInputStream(String.valueOf(inputStream)));

            ftpClient.disconnect();
            System.out.println(name);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
