package net.northking.cloudtest.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
/**
 * @Title: 文档工具类
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/10
 * @UpdateUser:
 * @Version:0.1
 */
public class WordUtil {

    private final static Logger logger = LoggerFactory.getLogger(WordUtil.class);

    private Configuration configuration = null;

    public WordUtil() {
        configuration = new Configuration();
        configuration.setDefaultEncoding("utf-8");

    }

    public void createWord(String templetName, String filePathName, Map<String, Object> dataMap) {
        configuration.setClassForTemplateLoading(this.getClass(), "/ftl"); // FTL文件所存在的位置
        Template t = null;
        try {
            // 获取模版文件
            t = configuration.getTemplate(templetName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 生成文件的路径和名称
        File outFile = new File(filePathName);
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outFile)));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        try {
            t.process(dataMap, out);
        } catch (TemplateException e) {
            logger.error("TemplateException:", e);
        } catch (IOException e) {
            logger.error("IOException:", e);
        }
    }

    public void xml2Word(String prePath, String fileName){
        try {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(new FileInputStream(new File(prePath + "/" + fileName + ".zip")));
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(prePath + "/" + fileName + ".docx")));
            String itemname = "word/document.xml";
            ZipUtils.replaceItem(zipInputStream, zipOutputStream, itemname, new FileInputStream(new File(prePath + "/" + fileName + ".doc")));
            System.out.println("success");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getImageStr(String imgFile) {
        InputStream in = null;
        byte[] data = null;
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    public String getStringByEnter(int length, String string) throws Exception
    {
        for (int i = 1; i <= string.length(); i++)
        {
            if (string.substring(0, i).getBytes("utf-8").length > length)
            {
                return string.substring(0, i - 1) + "\r\n" +
                        getStringByEnter(length, string.substring(i - 1));
            }
        }
        return string;
    }
}
