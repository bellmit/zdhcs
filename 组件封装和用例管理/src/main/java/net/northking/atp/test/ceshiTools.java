package net.northking.atp.test;

import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * Created by Administrator on 2019/4/25 0025.
 */
public class ceshiTools {
    public static void main(String[] args) throws ParseException {
//        String[] keys = new String[]{"ID","COMPONENT_ID","PARAMETER_TYPE","PARAMETER_FLAG","PARAMETER_ORDER",
//                "ELEMENT_TYPE","MODIFY_STAFF","MODIFY_TIME","PROJECT_ID","VERSION","PARAMETER_NAME",
//                "PARAMETER_COMMENT","DEFAULT_VALUE","REQUIRED","RUN_COMPONENT_ID","RUN_COMPONENT_ORDER","IN_OR_OUT"};
//        String newTable = "reParam";
//        String oldTable = "one";
//        String result = productCode(keys,newTable,oldTable);
//        System.out.println(result);
        Map<String,Object> ceshi = new HashMap<String,Object>();
        List<Map<String,Object>> packList = (List<Map<String,Object>>)ceshi.get("packList");
        for(Map<String,Object> a : packList){

        }
        System.out.println(packList);
    }

    private static String 中文(String 文字){
        return 文字;
    }

    private static String productCode(String[] keys,String newTable,String oldTable){
        String result = "";
        for(String key : keys){
            result = result + newTable+".set"+alanyStr(key)+"("+oldTable+".get"+alanyStr(key)+"());\n";
        }
        return result;
    }
    private static String alanyStr(String key){
        String result = "";
        for(int i = 0;i<key.length();i++){
            char k = key.charAt(i);
            if("_".equals(String.valueOf(k))){
                continue;
            }
            if(i==0 || "_".equals(String.valueOf(key.charAt(i-1)))){
                result = result+k;
            }else{
                result = result+String.valueOf(k).toLowerCase();
            }
        }
        return result;
    }


    private static void check(){
        String filePath = "/home/data/html/test";
        String ip = "192.168.0.130";
        String port = "8082";

        String path = filePath+"/componentFile/"+"1101"+"/";
        System.out.println("获取路径"+path);
        String fileName = "ceShi.txt";
        System.out.println("获取文件"+fileName);
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        String saveName = path+fileName;


        FTPClient ftp = new FTPClient();
        FileInputStream fis = null;
        try {
            ftp.setControlEncoding("UTF-8");
            ftp.connect(ip,21);

            boolean login = ftp.login("root", "123456");
            int replyCode = ftp.getReplyCode();
            if(login){
                System.out.println("登录成功");
            }else{
                System.out.println("登录失败");
            }

            ftp.setFileType(ftp.BINARY_FILE_TYPE);//设置为二进制文件
            if(ftp.makeDirectory(path)){
                System.out.println("目录创建成功");
            }else{
                System.out.println("目录已存在");
            }
            fis = new FileInputStream(new File("D:/nkTest/test/ceShi.txt"));
            ftp.storeFile(fileName,fis);
            System.out.println("文件上传成功");
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(fis!=null){
                try {
                    fis.close();
                    ftp.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *
     * @throws JSchException
     */
    private static ChannelSftp connect() throws JSchException {
        String username = "root";
        String host = "192.168.0.130";
        // 1.声明连接Sftp的通道
        ChannelSftp nChannelSftp = null;
        // 2.实例化JSch
        JSch nJSch = new JSch();
        // 3.获取session
        Session nSShSession = nJSch.getSession(username, host, 8082);
        System.out.println("Session创建成功");
        // 4.设置密码
        nSShSession.setPassword("123456");
        // 5.实例化Properties
        Properties nSSHConfig = new Properties();
        // 6.设置配置信息
        nSSHConfig.put("StrictHostKeyChecking", "no");
        // 7.session中设置配置信息
        nSShSession.setConfig(nSSHConfig);
        // 8.session连接
        nSShSession.connect();
        System.out.println("Session已连接");
        // 9.打开sftp通道
        Channel channel = nSShSession.openChannel("sftp");
        // 10.开始连接
        channel.connect();
        nChannelSftp = (ChannelSftp) channel;
        System.out.println("连接到主机" + host + ".");
        return nChannelSftp;
    }

}
