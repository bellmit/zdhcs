package net.northking.cloudtest.service.impl.report;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.northking.cloudtest.assist.RedisUtil;
import net.northking.cloudtest.dao.analyse.CltRoundMapper;
import net.northking.cloudtest.dao.task.CltTestcaseExecuteMapper;
import net.northking.cloudtest.domain.analyse.CltRound;
import net.northking.cloudtest.dto.report.QualityReportDTO;
import net.northking.cloudtest.dto.report.TestCaseExecuteReportDTO;
import net.northking.cloudtest.feign.analyse.CltRoundFeignClient;
import net.northking.cloudtest.feign.report.QualityReportFeignClient;
import net.northking.cloudtest.query.analyse.CltRoundQuery;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenChartExecuteService;
import net.northking.cloudtest.service.QualityReportService;
import net.northking.cloudtest.utils.WordUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liujinghao on 2018/5/20.
 */
@Service
public class GenChartExecuteServiceImpl implements GenChartExecuteService {

    private final static Logger logger = LoggerFactory.getLogger(GenChartExecuteServiceImpl.class);

    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;
    @Autowired
    private QualityReportService qualityReportService;

    @Autowired
    private CltRoundFeignClient cltRoundFeignClient;

    @Autowired
    private CltRoundMapper cltRoundMapper;

    @Autowired
    private CltTestcaseExecuteMapper cltTestcaseExecuteMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 生成用例执行图表
     *
     * @param proId
     * @return
     * @throws Exception
     */
    @Override
    public int GenChartExecute(String proId) throws Exception {
        Runtime run = Runtime.getRuntime();
        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int num = 0;
        //当日用例执行完成情况(人员)
        QualityReportQuery qualityReportQuery = new QualityReportQuery();
        qualityReportQuery.setProId(proId);
        qualityReportQuery.setStartDate(new Date());
        qualityReportQuery.setEndDate(new Date());
        qualityReportQuery.setType("U");
        QualityReportDTO qualityReportDTO = qualityReportService.queryTestExecuteQualityReport(qualityReportQuery);

        if (qualityReportDTO != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "用例执行完成情况");
            List<String> xAxis = qualityReportDTO.getxAxis();
            List<String> yAxis = qualityReportDTO.getyAxis();
            dataMap.put("xAxis", listChangeToString(xAxis));
            dataMap.put("yAxis1", countNum(yAxis, "0"));
            dataMap.put("yAxis2", countNum(yAxis, "1"));
            dataMap.put("yAxis3", countNum(yAxis, "2"));
            dataMap.put("yAxis4", countNum(yAxis, "3"));
            dataMap.put("yAxis5", countNum(yAxis, "4"));
            dataMap.put("yAxis6", countNum(yAxis, "5"));
            dataMap.put("yAxis7", countNum(yAxis, "6"));
            wordUtil.createWord("execute.json", preFilePath + qualityReportQuery.getProId() + "/json/executeByUser.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + qualityReportQuery.getProId() + "/json/executeByUser.json " + preFilePath + qualityReportQuery.getProId() + "/images/executeByUser.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        //当日用例执行完成情况(模块)
        QualityReportQuery query = new QualityReportQuery();
        query.setProId(proId);
        query.setStartDate(new Date());
        query.setEndDate(new Date());
        query.setType("M");
        QualityReportDTO qualityReportDTO1 = qualityReportService.queryTestExecuteQualityReport(qualityReportQuery);
        if (qualityReportDTO1 != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "用例执行完成情况");
            List<String> xAxis = qualityReportDTO1.getxAxis();
            List<String> yAxis = qualityReportDTO1.getyAxis();
            dataMap.put("xAxis", listChangeToString(xAxis));
            dataMap.put("yAxis1", countNum(yAxis, "0"));
            dataMap.put("yAxis2", countNum(yAxis, "1"));
            dataMap.put("yAxis3", countNum(yAxis, "2"));
            dataMap.put("yAxis4", countNum(yAxis, "3"));
            dataMap.put("yAxis5", countNum(yAxis, "4"));
            dataMap.put("yAxis6", countNum(yAxis, "5"));
            dataMap.put("yAxis7", countNum(yAxis, "6"));
            // dataMap.put("yAxisTotal", counttotal(yAxis));
            wordUtil.createWord("execute.json", preFilePath + qualityReportQuery.getProId() + "/json/executeByModule.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + qualityReportQuery.getProId() + "/json/executeByModule.json " + preFilePath + qualityReportQuery.getProId() + "/images/executeByModule.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        //当前轮次下的用例执行情况(人员)
      /*  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -8);
        Date date = c.getTime();*/
        List<CltRound> list = cltRoundMapper.selectRoundInfoByDate(proId, new Date());
        if (list.size() > 0) {
            int i=0;
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            for (CltRound cltRound : list
                    ) {
                String roundId = cltRound.getRoundId();
                List<TestCaseExecuteReportDTO> testCaseChartDate = cltTestcaseExecuteMapper.queryCaseExecuteChartByRoundIdAndUser(roundId);
                List<String> xAxis=new ArrayList<>();
                List<Integer> yAxis1=new ArrayList<>();
                List<Integer> yAxis2=new ArrayList<>();
                List<Integer> yAxis3=new ArrayList<>();
                List<Integer> yAxis4=new ArrayList<>();
                List<Integer> yAxis5=new ArrayList<>();
                List<Integer> yAxis6=new ArrayList<>();
                List<Integer> yAxis7=new ArrayList<>();
                if (testCaseChartDate.size()>0){
                    for (TestCaseExecuteReportDTO testCaseExecuteReportDTO:testCaseChartDate
                            ) {
                        testCaseExecuteReportDTO.setReceiveUserName((String) redisUtil.get("username:" + testCaseExecuteReportDTO.getReceiveUser()));
                        xAxis.add(testCaseExecuteReportDTO.getReceiveUserName());
                        yAxis1.add(testCaseExecuteReportDTO.getExecute0());
                        yAxis2.add(testCaseExecuteReportDTO.getExecute1());
                        yAxis3.add(testCaseExecuteReportDTO.getExecute2());
                        yAxis4.add(testCaseExecuteReportDTO.getExecute3());
                        yAxis5.add(testCaseExecuteReportDTO.getExecute4());
                        yAxis6.add(testCaseExecuteReportDTO.getExecute5());
                        yAxis7.add(testCaseExecuteReportDTO.getExecute6());
                    }
                }
                dataMap.put("title", "用例执行完成情况");
                dataMap.put("xAxis", listChangeToString(xAxis));
                dataMap.put("yAxis1", JSONObject.toJSONString(yAxis1));
                dataMap.put("yAxis2", JSONObject.toJSONString(yAxis2));
                dataMap.put("yAxis3", JSONObject.toJSONString(yAxis3));
                dataMap.put("yAxis4", JSONObject.toJSONString(yAxis4));
                dataMap.put("yAxis5", JSONObject.toJSONString(yAxis5));
                dataMap.put("yAxis6", JSONObject.toJSONString(yAxis6));
                dataMap.put("yAxis7", JSONObject.toJSONString(yAxis7));
                 i++;
                wordUtil.createWord("execute.json", preFilePath + proId + "/json/executeAllByUser"+i+".json", dataMap);
                String progressCmd = "node " + outputJsPath + " " + preFilePath + proId + "/json/executeAllByUser"+i+".json " + preFilePath + proId + "/images/executeAllByUser"+i+".png";
                try {
                    Process p = run.exec(progressCmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                num++;
            }
        }
        //当前轮次下的用例执行情况(模块)
        if (list.size() > 0) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            int j=0;
            for (CltRound cltRound : list
                    ) {
                String roundId = cltRound.getRoundId();
                List<TestCaseExecuteReportDTO> testCaseChartDate = cltTestcaseExecuteMapper.queryCaseExecuteChartByRoundIdAndModule(roundId);
                List<String> xAxis=new ArrayList<>();
                List<Integer> yAxis1=new ArrayList<>();
                List<Integer> yAxis2=new ArrayList<>();
                List<Integer> yAxis3=new ArrayList<>();
                List<Integer> yAxis4=new ArrayList<>();
                List<Integer> yAxis5=new ArrayList<>();
                List<Integer> yAxis6=new ArrayList<>();
                List<Integer> yAxis7=new ArrayList<>();
                if (testCaseChartDate.size()>0){
                    for (TestCaseExecuteReportDTO testCaseExecuteReportDTO:testCaseChartDate
                            ) {
                        xAxis.add(testCaseExecuteReportDTO.getModule());
                        yAxis1.add(testCaseExecuteReportDTO.getExecute0());
                        yAxis2.add(testCaseExecuteReportDTO.getExecute1());
                        yAxis3.add(testCaseExecuteReportDTO.getExecute2());
                        yAxis4.add(testCaseExecuteReportDTO.getExecute3());
                        yAxis5.add(testCaseExecuteReportDTO.getExecute4());
                        yAxis6.add(testCaseExecuteReportDTO.getExecute5());
                        yAxis7.add(testCaseExecuteReportDTO.getExecute6());
                    }
                }
                dataMap.put("title", "用例执行完成情况");
                dataMap.put("xAxis", listChangeToString(xAxis));
                dataMap.put("yAxis1", JSONObject.toJSONString(yAxis1));
                dataMap.put("yAxis2", JSONObject.toJSONString(yAxis2));
                dataMap.put("yAxis3", JSONObject.toJSONString(yAxis3));
                dataMap.put("yAxis4", JSONObject.toJSONString(yAxis4));
                dataMap.put("yAxis5", JSONObject.toJSONString(yAxis5));
                dataMap.put("yAxis6", JSONObject.toJSONString(yAxis6));
                dataMap.put("yAxis7", JSONObject.toJSONString(yAxis7));
                j++;
                wordUtil.createWord("execute.json", preFilePath + proId + "/json/executeAllByModule"+j+".json", dataMap);
                String progressCmd = "node " + outputJsPath + " " + preFilePath + proId +  "/json/executeAllByModule"+j+".json "+ preFilePath + proId + "/images/executeAllByModule"+j+".png";
                try {
                    Process p = run.exec(progressCmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                num++;
            }
        }


        //当前轮次下的用例执行情况
    /*    QualityReportDTO qualityReportDTO2 = qualityReportService.queryTestExecuteQualityReport(qualityReportQuery);
        if (qualityReportDTO2 != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "用例执行完成情况");
            List<String> xAxis = qualityReportDTO2.getxAxis();
            List<String> yAxis = qualityReportDTO2.getyAxis();
            System.out.println("3333333333333" + yAxis);
            System.out.println("11111111111" + yAxis);
            dataMap.put("xAxis", listChangeToString(xAxis));
            System.out.println("111!!!" + dataMap.get("xAxis"));
            dataMap.put("yAxis1", countNum(yAxis, "0"));
            dataMap.put("yAxis2", countNum(yAxis, "1"));
            dataMap.put("yAxis3", countNum(yAxis, "2"));
            dataMap.put("yAxis4", countNum(yAxis, "3"));
            dataMap.put("yAxis5", countNum(yAxis, "4"));
            dataMap.put("yAxis6", countNum(yAxis, "5"));
            dataMap.put("yAxis7", countNum(yAxis, "6"));
            wordUtil.createWord("execute.json", preFilePath + qualityReportQuery1.getProId() + "/json/executeAllByUser.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + qualityReportQuery1.getProId() + "/json/executeAllByUser.json " + preFilePath + qualityReportQuery1.getProId() + "/images/executeAllByUser.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }*/
        /*QualityReportQuery qualityReportQuery2 = new QualityReportQuery();
        qualityReportQuery2.setProId(proId);
        qualityReportQuery2.setEndDate(new Date());
        qualityReportQuery2.setStartDate(starDate);
        qualityReportQuery2.setType("M");
        QualityReportDTO qualityReportDTO3 = qualityReportService.queryTestExecuteQualityReport(qualityReportQuery);
        if (qualityReportDTO3 != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "用例执行完成情况");
            List<String> xAxis = qualityReportDTO3.getxAxis();
            List<String> yAxis = qualityReportDTO3.getyAxis();
            System.out.println("3333333333333" + yAxis);
            System.out.println("11111111111" + yAxis);
            dataMap.put("xAxis", listChangeToString(xAxis));
            System.out.println("111!!!" + dataMap.get("xAxis"));
            dataMap.put("yAxis1", countNum(yAxis, "0"));
            dataMap.put("yAxis2", countNum(yAxis, "1"));
            dataMap.put("yAxis3", countNum(yAxis, "2"));
            dataMap.put("yAxis4", countNum(yAxis, "3"));
            dataMap.put("yAxis5", countNum(yAxis, "4"));
            dataMap.put("yAxis6", countNum(yAxis, "5"));
            dataMap.put("yAxis7", countNum(yAxis, "6"));
            wordUtil.createWord("execute.json", preFilePath + qualityReportQuery2.getProId() + "/json/executeAllByModule.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + qualityReportQuery2.getProId() + "/json/executeAllByModule.json " + preFilePath + qualityReportQuery2.getProId() + "/images/executeAllByModule.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }*/
        return num;
    }

    @Override
    public int GenChartExecuteByUser(String proId) throws Exception {
        Runtime run = Runtime.getRuntime();
        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int num = 0;
        //当日用例执行完成情况(人员)
        QualityReportQuery qualityReportQuery = new QualityReportQuery();
        qualityReportQuery.setProId(proId);
        Date date = new Date();
        qualityReportQuery.setStartDate(getDate(date));
        qualityReportQuery.setEndDate(date);
        qualityReportQuery.setType("U");
        QualityReportDTO qualityReportDTO = qualityReportService.queryTestExecuteQualityReport(qualityReportQuery);

        if (qualityReportDTO != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "用例执行完成情况");
            List<String> xAxis = qualityReportDTO.getxAxis();
            List<String> yAxis = qualityReportDTO.getyAxis();
            dataMap.put("xAxis", listChangeToString(xAxis));
            dataMap.put("yAxis1", countNum(yAxis, "0"));
            dataMap.put("yAxis2", countNum(yAxis, "1"));
            dataMap.put("yAxis3", countNum(yAxis, "2"));
            dataMap.put("yAxis4", countNum(yAxis, "3"));
            dataMap.put("yAxis5", countNum(yAxis, "4"));
            dataMap.put("yAxis6", countNum(yAxis, "5"));
            dataMap.put("yAxis7", countNum(yAxis, "6"));
            wordUtil.createWord("execute.json", preFilePath + qualityReportQuery.getProId() + "/json/executeByUser.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + qualityReportQuery.getProId() + "/json/executeByUser.json " + preFilePath + qualityReportQuery.getProId() + "/images/executeByUserWeek.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        return num;
    }

    /**
     * list转换成string
     *
     * @param list
     * @return
     */
    private String listChangeToString(List<String> list) {
        String rtnStr = "[";
        for (int i = 0; i < list.size(); i++) {
            rtnStr += "\"" + list.get(i) + "\",";
        }
        if (rtnStr.indexOf(",") > 0) {
            rtnStr = rtnStr.substring(0, rtnStr.length() - 1);
        }
        return rtnStr + "]";
    }

    /**
     * 统计各执行状态数量
     *
     * @param ylist
     * @return
     */
    private String countNum(List<String> ylist, String num) {
        Double[] actualValues = new Double[ylist.size()];
        for (int i = 0; i < ylist.size(); i++) {
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if (StringUtils.isNotEmpty(actualValue)) {
                Map resultToMap = JSON.parseObject(actualValue);
                actualValues[i] = Integer.parseInt((String) resultToMap.get(num)) * 1.0;
               /* DecimalFormat dFormat=new DecimalFormat("#.00");*/
              /*  String yearString=dFormat.format(actualValues[i]);
                actualValues[i] = Double.valueOf(yearString) * 100;*/
            } else {
                actualValues[i] = 0.0;
            }
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }

    /**
     * 统计缺陷总数
     *
     * @param ylist
     * @return
     */
    private String counttotal(List<String> ylist) {
        Double[] actualValues = new Double[ylist.size()];
        for (int i = 0; i < ylist.size(); i++) {
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if (StringUtils.isNotEmpty(actualValue)) {
                Map resultToMap = JSON.parseObject(actualValue);
                actualValues[i] = Integer.parseInt((String) resultToMap.get("total")) * 1.0;
               /* DecimalFormat dFormat=new DecimalFormat("#.00");
                String yearString=dFormat.format(actualValues[i]);*/
               /* actualValues[i] = Double.valueOf(yearString) * 100;*/
            } else {
                actualValues[i] = 0.0;
            }
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }

    //获取前几天的日期
    public Date getDate(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        date = calendar.getTime();

        return date;
    }
}