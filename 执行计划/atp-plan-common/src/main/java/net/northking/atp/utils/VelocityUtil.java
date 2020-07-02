package net.northking.atp.utils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 模板引擎的工具类
 */
public class VelocityUtil {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(VelocityUtil.class);

    /**
     * 通过模板文件vm生成html文件
     * @param vmPath        模板文件的路径
     * @param outputFile    文件输出路径
     * @param context       引擎上下文对象
     * @throws IOException
     */
    public static void generateFileByVM(String vmPath, String outputFile, VelocityContext context) throws IOException {
        Template template = getTemplate(vmPath, context);
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        System.setProperty("sun.jnu.encoding", "utf-8");
        try {
            File file = new File(outputFile);
            file.setWritable(true, false);
            boolean parentFolderFlag = file.getParentFile().exists();
            logger.info("父路径存在与否：{}", parentFolderFlag);
            if (!parentFolderFlag) {
                logger.info("生成的文件父路径：{}", file.getParentFile().getAbsolutePath());
                file.getParentFile().setWritable(true, false);
                file.getParentFile().mkdirs();
            }
            boolean isNewFile = file.createNewFile();
            logger.info("{}文件创建情况：{}", file.getName(), isNewFile);
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            // 将context与模板文件进行合并
            template.merge(context, bw);
            bw.flush();
            logger.info("测试报告位置：{}", file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (osw != null) {
                osw.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * 生成数据字符串
     * @param vmPath    模板文件路径
     * @param context
     * @return
     */
    public static String generateDataString(String vmPath, VelocityContext context) {
        Template template = getTemplate(vmPath, context);
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();
    }

    /**
     * 获取模板文件对象
     * @param vmPath
     * @param context
     * @return
     */
    private static Template getTemplate(String vmPath, VelocityContext context) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.setProperty(VelocityEngine.ENCODING_DEFAULT, "UTF-8");
        ve.setProperty(VelocityEngine.INPUT_ENCODING, "UTF-8");
        ve.setProperty(VelocityEngine.OUTPUT_ENCODING, "UTF-8");
        ve.init();
        Template template = ve.getTemplate(vmPath);
        return template;
    }

    /**
     * 时间格式转换工具方法
     * @param date
     * @return
     */
    public static String dateFormat(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 数字转成字符串
     * @param number
     * @return
     */
    public static String numberToString(Number number) {
        return String.valueOf(number);
    }

}
