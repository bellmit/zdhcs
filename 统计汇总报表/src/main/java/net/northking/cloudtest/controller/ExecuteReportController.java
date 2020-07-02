package net.northking.cloudtest.controller;

import net.northking.cloudtest.libreoffice.DocConverter;
import net.northking.cloudtest.service.ExecuteBulletinService;
import net.northking.cloudtest.service.GenExecuteReportChartService;
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
 * @Author:zwy
 * @Despriction:用例执行阶段测试报告
 * @Date:Create in 10:49 2018/5/24
 * @Modify By:
 */
@RestController
@RequestMapping(value = "/report")
public class ExecuteReportController {
    private Logger logger = LoggerFactory.getLogger(ExecuteReportController.class);
    @Value("${report.chart.prepath}")
    private String preFilePath;
    @Autowired
    private GenExecuteReportChartService genExecuteReportChartService;
    @Autowired
    private ExecuteBulletinService executeBulletinService;
    @GetMapping("/executeReport")
    public void executeReport(@RequestParam("projectId") String projectId, @RequestParam("roundId")String roundId,Invocation inv) throws Exception{

        WordUtil wordUtil = new WordUtil();

        File dirFile = new File(preFilePath + projectId+"/images");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File dirFile2 = new File(preFilePath + projectId+"/json");
        if (!dirFile2.exists()) {
            dirFile2.mkdirs();
        }
        //生成图表
        genExecuteReportChartService.genChart(projectId, roundId);

        //获取数据
        Map dataMap = new HashMap();


        executeBulletinService.dealWithReport(projectId, roundId, dataMap);

       wordUtil.createWord("executeModel.ftl", preFilePath + projectId + "/executeReport_" + roundId + ".xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/executeReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/executeReport_" + roundId + ".docx")));
            String[] itemname = {"word/document.xml","word/media/image1.png", "word/media/image2.png", "word/media/image3.png","word/media/image4.png","word/media/image5.png","word/media/image6.png","word/media/image7.png"};
            String[] itemInputFile = {preFilePath + projectId + "/executeReport_" + roundId + ".xml", preFilePath + projectId + "/images/totalProgress.png",  preFilePath + projectId + "/images/caseDesignProgress.png", preFilePath + projectId + "/images/testTeam.png",preFilePath + projectId + "/images/testCaseTrend.png", preFilePath + projectId + "/images/executeByUserWeek.png", preFilePath + projectId + "/images/testBugMoudle.png", preFilePath + projectId + "/images/testBugTrend.png"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}

            DocConverter d = new DocConverter(preFilePath + projectId + "/executeReport_" + roundId + ".docx");
            d.conver();

            //往数据库插入数据
            executeBulletinService.insertOrUpdateData(projectId, roundId, null, "/bulletin/" + projectId + "/executeReport_" + roundId + ".docx", "/bulletin/" + projectId + "/executeReport_" + roundId + ".swf");


        } catch (Exception e) {
            logger.error("error：", e);
        }

    }


    @GetMapping("/executeReportTest")
    public void executeReportTest(@RequestParam("projectId") String projectId, @RequestParam("roundId")String roundId,Invocation inv) throws Exception{

        WordUtil wordUtil = new WordUtil();

        File dirFile = new File(preFilePath + projectId);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        //获取数据
        Map dataMap = new HashMap();

        executeBulletinService.dealWithReport(projectId, roundId, dataMap);
        wordUtil.createWord("executeModel.ftl", preFilePath + projectId + "/executeReport_" + roundId + ".xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/executeReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/executeReport_" + roundId + ".docx")));
            String[] itemname = {"word/document.xml"};
            String[] itemInputFile = {preFilePath + projectId + "/executeReport_" + roundId + ".xml"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}



        } catch (Exception e) {
            logger.error("error：", e);
        }

    }



}
