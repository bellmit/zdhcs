package net.northking.cloudtest.controller;

import net.northking.cloudtest.dto.SummaryBulletin;
import net.northking.cloudtest.dto.report.CoverageReportDTO;
import net.northking.cloudtest.libreoffice.DocConverter;
import net.northking.cloudtest.query.project.ProjectQuery;
import net.northking.cloudtest.query.report.CoverageReportQuery;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenChartService;
import net.northking.cloudtest.service.GenTestBugChartService;
import net.northking.cloudtest.service.SummaryBulletinService;
import net.northking.cloudtest.utils.WordUtil;
import net.northking.cloudtest.utils.ZipUtils;

import org.apache.ibatis.plugin.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @Title: 总体测试报告
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/9
 * @UpdateUser:
 * @Version:0.1
 */
@RestController
@RequestMapping(value = "/report/summaryBulletin")
public class SummaryBulletinController {

    private final static Logger logger = LoggerFactory.getLogger(SummaryBulletinController.class);

    @Autowired
    private GenChartService genChartService;

    @Autowired
    private SummaryBulletinService summaryBulletinService;

    @Autowired
    private GenTestBugChartService genTestBugChartService;

    @Value("${report.chart.prepath}")
    private String preFilePath;

    /**
     * 总结报告
     * @param projectId
     */
    @GetMapping("/summaryReport")
    public void summaryReport(@RequestParam("projectId") String projectId, Invocation inv) throws Exception{
        WordUtil wordUtil = new WordUtil();

        File dirFile = new File(preFilePath + projectId);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //生成图表
        genChartService.genChart(projectId);
        //生成缺陷趋势图表
        genTestBugChartService.genTestBugChart(projectId);

        //获取数据
        Map dataMap = new HashMap();
        try {
            summaryBulletinService.dealWithReport(projectId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        wordUtil.createWord("summeryModel.ftl", preFilePath + projectId + "/summeryReport.xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/summeryReport.zip");
            //if(cfgFile.exists()) {
                ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
                ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/summeryReport.docx")));
                String[] itemname = {"word/document.xml", "word/media/image1.png", "word/media/image2.png", "word/media/image3.png", "word/media/image4.png", "word/media/image5.png", "word/media/image6.png"};
                String[] itemInputFile = {preFilePath + projectId + "/summeryReport.xml", preFilePath + projectId + "/images/totalProgress.png", preFilePath + projectId + "/images/demandProgress.png", preFilePath + projectId + "/images/caseDesignProgress.png", preFilePath + projectId + "/images/caseExecuteProgress.png", preFilePath + projectId + "/images/testTeam.png", preFilePath + projectId + "/images/testBugTrend.png"};
                ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}

            DocConverter d = new DocConverter(preFilePath + projectId + "/summeryReport.docx");
            d.conver();

            //往数据库插入数据
            summaryBulletinService.insertOrUpdateData(projectId, null, "/bulletin/" + projectId + "/summeryReport.docx", "/bulletin/" + projectId + "/summeryReport.swf");

        } catch (Exception e) {
            logger.error("error：", e);
        }


    }


    @GetMapping("/summaryTest")
    public void summaryTest(String projectId){
        WordUtil wordUtil = new WordUtil();
        //获取数据
        Map dataMap = new HashMap();
        try {
            summaryBulletinService.dealWithReport(projectId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        wordUtil.createWord("summeryModel.ftl", preFilePath + projectId + "/summeryReport.xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/summeryReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/summeryReport.docx")));
            String[] itemname = {"word/document.xml"};
            String[] itemInputFile = {preFilePath + projectId + "/summeryReport.xml"};
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
        genChartService.genChart(projectId);
    }



}
