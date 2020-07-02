package net.northking.cloudtest.controller;

import net.northking.cloudtest.dao.analyse.CltRoundMapper;
import net.northking.cloudtest.domain.analyse.CltRound;
import net.northking.cloudtest.libreoffice.DocConverter;
import net.northking.cloudtest.service.*;
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
import java.util.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by liujinghao on 2018/5/24.
 */
@RestController
@RequestMapping("/report/weekTestReport")
public class WeekReportController {
    private final static Logger logger = LoggerFactory.getLogger(WeekReportController.class);

    @Autowired
    private GenChartService genChartService;

    @Autowired
    private GenCompleteChartWeekService genCompleteChartWeekService;

    @Autowired
    private  WeekReportService weekReportService;

    @Autowired
    private GenExecuteChartWeekService genExecuteChartWeekService;

    @Autowired
    private GenTestBugChartWeekService genTestBugChartWeekService;

    @Autowired
    private CltRoundMapper cltRoundMapper;


    @Value("${report.chart.prepath}")
    private String preFilePath;

    @GetMapping("/weekReport")
    public void weekReportBuild(@RequestParam("proId") String proId, Invocation inv) throws Exception {
        WordUtil wordUtil = new WordUtil();
        File dirFile = new File(preFilePath + proId+"/images");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //生成项目进度图表
        genChartService.genChart(proId);

       //生成完成情况图表
        genCompleteChartWeekService.genCompleteChartWeek(proId);

        //生成执行完成情况图表
        genExecuteChartWeekService.GenExecuteWeekChart(proId);

        //生成缺陷图
        genTestBugChartWeekService.genTestBugChart(proId);

        //获取数据
        Map dataMap = new HashMap();
        try {
            weekReportService.dealWithWeekReport(proId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        wordUtil.createWord("weekModel.ftl", preFilePath + proId + "/weekReport.xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/weekReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + proId + "/weekReport.docx")));

            List<CltRound> list = cltRoundMapper.selectRoundInfoByDate(proId, new Date());
            List<String> round = new ArrayList<>();
            for (CltRound cltRound : list
                    ) {
                round.add(cltRound.getRoundId());
            }
            int length = 15 + round.size() * 2;
            String[] itemname1 = new String[length];
            String[] itemname = {"word/document.xml", "word/media/image1.png","word/media/image2.png","word/media/image3.png","word/media/image4.png","word/media/image5.png",
                    "word/media/image6.png","word/media/image7.png","word/media/image8.png", "word/media/image9.png","word/media/image10.png"
                    ,"word/media/image21.wmf","word/media/image22.emf","word/media/image23.png","word/media/image24.png"};

            for (int i = 0; i < itemname1.length; i++) {
                if (i <= 10) {
                    itemname1[i] = itemname[i];
                }
                if (i > 10 && i < itemname1.length - 4) {

                    itemname1[i] = "word/media/image" + i + ".png";

                }
                if (i == (itemname1.length - 4)) {
                    itemname1[length - 4] = "word/media/image21.wmf";
                }
                if (i == (itemname1.length - 3)) {
                    itemname1[length - 3] = "word/media/image22.emf";
                }
                if (i == (itemname1.length - 2)) {
                    itemname1[length - 2] = "word/media/image23.png";
                }
                if (i == (itemname1.length - 1)) {
                    itemname1[length - 1] = "word/media/image24.png";
                }

            }
            String[] itemInputFile1 = new String[length];
            String[] itemInputFile = {preFilePath + proId + "/weekReport.xml",
                    preFilePath + proId + "/images/totalProgress.png", preFilePath + proId + "/images/demandProgress.png",
                    preFilePath + proId + "/images/caseDesignProgress.png",preFilePath + proId + "/images/caseExecuteProgress.png",
                    preFilePath + proId + "/images/demandCompleteByAllWeek.png", preFilePath + proId + "/images/demandCompleteByWeek.png",
                    preFilePath + proId + "/images/caseCompleteByModuleAndWeek.png", preFilePath + proId + "/images/caseCompleteByUserAndWeek.png",
                    preFilePath + proId + "/images/executeByUserAndWeek.png", preFilePath + proId + "/images/executeByModuleAndWeek.png",
                    preFilePath + proId + "/images/testBugByWeek.png", preFilePath + proId + "/images/testBugModuleByWeek.png"
                    ,preFilePath + proId + "/images/testBugTrendByWeek.png",preFilePath + proId + "/images/testBugLife.png"};

            int j = 1;
            int k = 1;
            for (int i = 0; i < itemInputFile1.length; i++) {
                if (i <= 10) {
                    itemInputFile1[i] = itemInputFile[i];
                }
                if (i > 10 && i < itemInputFile1.length - 4) {
                    if (i % 2 != 0) {
                        itemInputFile1[i] = preFilePath + proId + "/images/executeAllByUserAndRound" + j + ".png";
                        j++;
                    } else {
                        itemInputFile1[i] = preFilePath + proId + "/images/executeAllByModuleAndRound" + k + ".png";
                        k++;
                    }

                }
                if (i == (itemInputFile1.length - 4)) {
                    itemInputFile1[length - 4] =preFilePath + proId + "/images/testBugByWeek.png";
                }
                if (i == (itemInputFile1.length - 3)) {
                    itemInputFile1[length - 3] =preFilePath + proId + "/images/testBugModuleByWeek.png";
                }
                if (i == (itemInputFile1.length - 2)) {
                    itemInputFile1[length - 2] =preFilePath + proId + "/images/testBugTrendByWeek.png";
                }
                if (i == (itemInputFile1.length - 1)) {
                    itemInputFile1[length - 1] = preFilePath + proId + "/images/testBugLife.png";
                }
            }
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname1, itemInputFile1);
            //}
            DocConverter d = new DocConverter(preFilePath + proId + "/weekReport.docx");
            d.conver();

            //往数据库插入数据
            weekReportService.insertOrUpdateWeekReportData(proId, null, "/bulletin/" + proId + "/weekReport.docx", "/bulletin/" + proId + "/weekReport.swf");

        } catch (Exception e) {
            logger.error("error：", e);
        }
    }
}
