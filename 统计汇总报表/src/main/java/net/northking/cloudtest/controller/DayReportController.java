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
 * Created by liujinghao on 2018/5/16.
 */
@RequestMapping(value = "/report/dayTestReport")
@RestController
public class DayReportController {
    private final static Logger logger = LoggerFactory.getLogger(DayReportController.class);

    @Autowired
    private GenTestBugChartService genTestBugChartService;

    @Autowired
    private GenCompleteChartService genCompleteChartService;

    @Autowired
    private GenChartExecuteService genChartExecuteService;

    @Autowired
    private GenChartService genChartService;

    @Autowired
    private DayReportService dayReportService;

    @Autowired
    private CltRoundMapper cltRoundMapper;

    @Value("${report.chart.prepath}")
    private String preFilePath;

    @GetMapping("/dayReport")
    public void dayReportBuild(@RequestParam("proId") String proId, Invocation inv) throws Exception {
        WordUtil wordUtil = new WordUtil();
        File dirFile = new File(preFilePath + proId);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        //生成缺陷图表
        genTestBugChartService.genTestBugChart(proId);
        //生成完成情况图表
        genCompleteChartService.GenCompleteChart(proId);
        //生成用例执行完成情况
        genChartExecuteService.GenChartExecute(proId);
        //生成进度表
        genChartService.genChart(proId);

        //获取数据
        Map dataMap = new HashMap();
        try {
            dayReportService.dealWithDayReport(proId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        wordUtil.createWord("dayModel.ftl", preFilePath + proId + "/dayReport.xml", dataMap);

        try {
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/dayReport.zip");
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + proId + "/dayReport.docx")));
            List<CltRound> list = cltRoundMapper.selectRoundInfoByDate(proId, new Date());
            List<String> round = new ArrayList<>();
            for (CltRound cltRound : list
                    ) {
                round.add(cltRound.getRoundId());
            }
            int length = 12 + round.size() * 2;
            String[] itemname1 = new String[length];
            String[] itemname = {"word/document.xml", "word/media/image1.png", "word/media/image2.png", "word/media/image3.png", "word/media/image4.png", "word/media/image5.png",
                    "word/media/image6.png", "word/media/image7.png", "word/media/image8.png",
                    "word/media/image9.png", "word/media/image10.png", "word/media/image21.png"};
            for (int i = 0; i < itemname1.length; i++) {
                if (i <= 10) {
                    itemname1[i] = itemname[i];
                }
                if (i > 10 && i < itemname1.length - 1) {

                    itemname1[i] = "word/media/image" + i + ".png";

                }
                if (i == (itemname1.length - 1)) {
                    itemname1[length - 1] = "word/media/image21.png";
                }

            }
            String[] itemInputFile1 = new String[length];
            String[] itemInputFile = {preFilePath + proId + "/dayReport.xml", preFilePath + proId + "/images/totalProgress.png",
                    preFilePath + proId + "/images/demandProgress.png", preFilePath + proId + "/images/caseDesignProgress.png",
                    preFilePath + proId + "/images/caseExecuteProgress.png", preFilePath + proId + "/images/demandComplete.png",
                    preFilePath + proId + "/images/demandCompleteByDay.png", preFilePath + proId + "/images/caseComplete.png",
                    preFilePath + proId + "/images/caseCompleteByDay.png", preFilePath + proId + "/images/executeByUser.png",
                    preFilePath + proId + "/images/executeByModule.png", preFilePath + proId + "/images/testBugTrend.png"
            };
            int j = 1;
            int k = 1;
            for (int i = 0; i < itemInputFile1.length; i++) {
                if (i <= 10) {
                    itemInputFile1[i] = itemInputFile[i];
                }
                if (i > 10 && i < itemname1.length - 1) {
                    if (i % 2 != 0) {
                        itemInputFile1[i] = preFilePath + proId + "/images/executeAllByUser" + j + ".png";
                        j++;
                    } else {
                        itemInputFile1[i] = preFilePath + proId + "/images/executeAllByModule" + k + ".png";
                        k++;
                    }

                }
                if (i == (itemInputFile1.length - 1)) {
                    itemInputFile1[length - 1] = preFilePath + proId + "/images/testBugTrend.png";
                }

            }
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname1, itemInputFile1);
            //}
            DocConverter d = new DocConverter(preFilePath + proId + "/dayReport.docx");
            d.conver();

            //往数据库插入数据
            dayReportService.insertOrUpdateDayReportData(proId, null, "/bulletin/" + proId + "/dayReport.docx", "/bulletin/" + proId + "/dayReport.swf");

        } catch (Exception e) {
            logger.error("error：", e);
        }

    }

}
