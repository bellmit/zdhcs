package net.northking.cloudtest.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.northking.cloudtest.dto.SummaryBulletin;



public class XMlToDoc{

    private  static  final  String basePath = "D:\\工作\\云测\\svn\\设计文档\\报告\\openoffice_dependencies\\com\\";   //模版存储路径，项目里我就放在resource下
   public static void main(String[] args)  throws  Exception{

     makeWord();
     //makePdfByXcode();

    }

    /**
     * 生成docx
     * @throws Exception
     */
     static  void makeWord() throws Exception{
        /** 初始化配置文件 **/
        Configuration configuration = new Configuration();
        String fileDirectory = basePath;
        /** 加载文件 **/
        configuration.setDirectoryForTemplateLoading(new File(fileDirectory));
        /** 加载模板 **/
        Template template = configuration.getTemplate("summeryReport.xml");
        /** 准备数据 **/
        Map dataMap = new HashMap<String, String>();
        SummaryBulletin summaryBulletin = new SummaryBulletin();
         summaryBulletin.setCustName("京北方");
        /** 在ftl文件中有${textDeal}这个标签**/
        dataMap.put("summaryBulletin",summaryBulletin);
        dataMap.put("number","20");
        dataMap.put("language","java,php,python,c++.......");
        dataMap.put("example","Hello World!");

        /** 指定输出word文件的路径 **/
        String outFilePath = basePath+"data.xml";
        File docFile = new File(outFilePath);
        FileOutputStream fos = new FileOutputStream(docFile);
        Writer out = new BufferedWriter(new OutputStreamWriter(fos),10240);
        template.process(dataMap,out);
        if(out != null){
            out.close();
        }
        try {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(new FileInputStream(new File(basePath+"summeryReport.zip")));
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(basePath+"test22.docx")));
            String[] itemname = {"word/document.xml","word/media/image2.png"};
            String[] itemInputFile = {basePath+"data.xml",basePath+"demandProgress.png"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            System.out.println("success");

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }



}
































































