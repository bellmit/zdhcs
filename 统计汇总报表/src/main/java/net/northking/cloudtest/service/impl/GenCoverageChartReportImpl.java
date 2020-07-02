package net.northking.cloudtest.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.northking.cloudtest.dto.report.CoverageReportDTO;
import net.northking.cloudtest.dto.report.CoverageReportData;
import net.northking.cloudtest.query.report.CoverageReportQuery;
import net.northking.cloudtest.service.CoverageReportService;
import net.northking.cloudtest.service.GenCoverageChartReport;
import net.northking.cloudtest.utils.WordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成覆盖率分析图
 * Created by 老邓 on 2018/5/21.
 */
@Service
public class GenCoverageChartReportImpl implements GenCoverageChartReport {
    @Autowired
    private CoverageReportService coverageReportService;
    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;

    @Override
    public int genCoverageChart(String proId) {
        Runtime run = Runtime.getRuntime();
        //String outputJsPath = "D:/nodeChart/output.js";

        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        int num = 0;
        CoverageReportQuery coverageReportQuery = new CoverageReportQuery();
        coverageReportQuery.setProId(proId);
        CoverageReportDTO coverageReportDTO = coverageReportService.testcaseDemandCoverageReport(coverageReportQuery);
        if(coverageReportDTO!=null){
            WordUtil wordUtil = new WordUtil();
            List<String> xAxis = coverageReportDTO.getxAxis();
            List<CoverageReportData> yAxis = coverageReportDTO.getyAxis();
            List<CoverageReportData> coverageReportDatas = null;
            coverageReportDatas = coverageReportDTO.getyAxis();
            CoverageReportData coverageReportData = coverageReportDatas.get(0);
            List<Integer> data = coverageReportData.getData();
            String y1 = JSONObject.toJSONString(data);
            coverageReportData = coverageReportDatas.get(1);
            data = coverageReportData.getData();
            Map dataMap = new HashMap();
            String x = JSONObject.toJSONString(xAxis);
            String y2 = JSONObject.toJSONString(data);
            dataMap.put("xAxis",x);
            dataMap.put("y1Axis",y1);
            dataMap.put("y2Axis",y2);
            wordUtil.createWord("coverageReport.json", preFilePath + proId + "/json/coverageReport.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + proId + "/json/coverageReport.json " + preFilePath +proId + "/images/coverageReport.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        return num;
    }
}
