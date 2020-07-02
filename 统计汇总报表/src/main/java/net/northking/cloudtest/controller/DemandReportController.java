package net.northking.cloudtest.controller;

import net.northking.cloudtest.dto.DeamndBulletinService;
import net.northking.cloudtest.libreoffice.DocConverter;
import net.northking.cloudtest.service.GenDemandReportChartService;
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
 * Created by 老邓 on 2018/5/21.
 */
@RestController
@RequestMapping(value = "/report")
public class DemandReportController {
    private Logger logger = LoggerFactory.getLogger(DemandReportController.class);
    @Value("${report.chart.prepath}")
    private String preFilePath;
    @Autowired
    private GenDemandReportChartService genDemandReportChartService;
    @Autowired
    private DeamndBulletinService deamndBulletinService;

    /**
     * 需求分析阶段测试报告
     *
     * @param projectId
     */
    @GetMapping("/demandReport")
    public void demandReport(@RequestParam("projectId") String projectId, Invocation inv) throws Exception {
        WordUtil wordUtil = new WordUtil();
        //preFilePath + projectId + "/images
        File dirFile = new File(preFilePath + projectId+"/images");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //生成图表
        genDemandReportChartService.genChart(projectId);

        //获取数据
        Map dataMap = new HashMap();

        deamndBulletinService.dealWithReport(projectId, dataMap);
        wordUtil.createWord("demandModel.ftl", preFilePath + projectId + "/demandReport.xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/demandReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/demandReport.docx")));
            String[] itemname = {"word/document.xml","word/media/image1.png", "word/media/image2.png", "word/media/image3.png","word/media/image4.png"};
            String[] itemInputFile = {preFilePath + projectId + "/demandReport.xml", preFilePath + projectId + "/images/totalProgress.png",  preFilePath + projectId + "/images/demandProgress.png", preFilePath + projectId + "/images/testTeam.png",preFilePath + projectId + "/images/coverageReport.png"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}

            DocConverter d = new DocConverter(preFilePath + projectId + "/demandReport.docx");
            d.conver();

            //往数据库插入数据
            deamndBulletinService.insertOrUpdateData(projectId, null, "/bulletin/" + projectId + "/demandReport.docx", "/bulletin/" + projectId + "/demandReport.swf");


        } catch (Exception e) {
            logger.error("error：", e);
        }

    }
}
