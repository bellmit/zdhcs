package net.northking.cloudtest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.cloudtest.libreoffice.DocConverter;
import net.northking.cloudtest.service.DesignBulletinService;
import net.northking.cloudtest.service.GenChartService;
import net.northking.cloudtest.service.SummaryBulletinService;
import net.northking.cloudtest.utils.WordUtil;
import net.northking.cloudtest.utils.ZipUtils;

import org.apache.ibatis.plugin.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @Title: 设计阶段报告
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/23
 * @UpdateUser:
 * @Version:0.1
 */
@Api("用例设计阶段")
@RestController
@RequestMapping(value = "/report/designBulletin")
public class DesignBulletinController {

    private final static Logger logger = LoggerFactory.getLogger(DesignBulletinController.class);

    //@Autowired
    //private GenChartService genChartService;

    @Autowired
    private DesignBulletinService designBulletinService;

    @Value("${report.chart.prepath}")
    private String preFilePath;

    /**
     * 总结报告
     * @param projectId
     */
    @GetMapping("/designReport")
    @ApiOperation("用例设计报告")
    public void summaryReport(@RequestParam("projectId") String projectId, Invocation inv) throws Exception{
        WordUtil wordUtil = new WordUtil();

        File dirFile = new File(preFilePath + projectId);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //生成图表
        designBulletinService.genChart(projectId);

        //获取数据
        Map dataMap = new HashMap();
        try {
            designBulletinService.dealWithReport(projectId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        wordUtil.createWord("designModel.ftl", preFilePath + projectId + "/designReport.xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/designReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/designReport.docx")));
            String[] itemname = {"word/document.xml", "word/media/image1.png" , "word/media/image2.png", "word/media/image3.png", "word/media/image4.png"};
            String[] itemInputFile = {preFilePath + projectId + "/designReport.xml", preFilePath + projectId + "/images/totalProgress.png", preFilePath + projectId + "/images/caseDesignProgress.png", preFilePath + projectId + "/images/testTeam.png", preFilePath + projectId + "/images/coverageReport.png"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}

            DocConverter d = new DocConverter(preFilePath + projectId + "/designReport.docx");
            d.conver();

            //往数据库插入数据
            designBulletinService.insertOrUpdateData(projectId, null, "/bulletin/" + projectId + "/designReport.docx", "/bulletin/" + projectId + "/designReport.swf");

        } catch (Exception e) {
            logger.error("error：", e);
        }


    }


    @GetMapping("/designTest")
    public void summaryTest(String projectId){
        WordUtil wordUtil = new WordUtil();
        //获取数据
        Map dataMap = new HashMap();
        try {
            designBulletinService.dealWithReport(projectId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        wordUtil.createWord("designModel.ftl", preFilePath + projectId + "/designReport.xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/designReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/designReport.docx")));
            String[] itemname = {"word/document.xml"};
            String[] itemInputFile = {preFilePath + projectId + "/designReport.xml"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}

        } catch (Exception e) {
            logger.error("error：", e);
        }
    }

    @GetMapping("/genChartTest")
    public void genChartTest(String projectId){
        File dirFile = new File(preFilePath + projectId);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        //生成图表
        designBulletinService.genChart(projectId);
    }
}
