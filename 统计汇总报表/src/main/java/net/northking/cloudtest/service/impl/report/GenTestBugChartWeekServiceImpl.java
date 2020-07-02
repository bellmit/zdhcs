package net.northking.cloudtest.service.impl.report;

import com.alibaba.fastjson.JSON;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.testBug.CltTestBugMapper;
import net.northking.cloudtest.domain.testBug.CltBugLifeCount;
import net.northking.cloudtest.domain.testBug.CltTestBug;
import net.northking.cloudtest.dto.report.TestBugGradeData;
import net.northking.cloudtest.dto.report.TestBugQualityReportDTO;
import net.northking.cloudtest.dto.report.TrendReportDTO;
import net.northking.cloudtest.feign.report.QualityReportFeignClient;
import net.northking.cloudtest.feign.report.TestBugQualityReportFeignClient;
import net.northking.cloudtest.feign.report.TrendReportFeignClient;
import net.northking.cloudtest.query.report.TestBugQualityReportQuery;
import net.northking.cloudtest.query.report.TrendReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenTestBugChartWeekService;
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
 * Created by liujinghao on 2018/5/25.
 */
@Service
public class GenTestBugChartWeekServiceImpl implements GenTestBugChartWeekService{
    private final static Logger logger = LoggerFactory.getLogger(GenTestBugChartWeekServiceImpl.class);


    @Autowired
    private TrendReportFeignClient trendReportFeignClient;

    @Autowired
    private CltTestBugMapper cltTestBugMapper;

    @Autowired
    private TestBugQualityReportFeignClient testBugQualityReportFeignClient;

    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;

    @Override
    public int genTestBugChart(String proId) throws Exception {

        Runtime run = Runtime.getRuntime();

        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int num = 0;
        //缺陷生命周期图
        CltTestBug testBug=  new CltTestBug();
        testBug.setProId(proId);
        CltBugLifeCount cltBugLifeCount=cltTestBugMapper.selectTestBugLifeByNum(testBug);
       /* System.out.println("缺陷生命周期个数"+cltBugLifeCount.getDiff1());*/
        if (cltBugLifeCount!=null){
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "缺陷生命周期");
            List<Integer>list= new ArrayList<>();
            list.add(cltBugLifeCount.getDiff1());
            list.add((cltBugLifeCount.getDiff2()-cltBugLifeCount.getDiff1()));
            list.add((cltBugLifeCount.getDiff3()-cltBugLifeCount.getDiff2()));
            list.add((cltBugLifeCount.getDiff4()-cltBugLifeCount.getDiff3()));
            list.add((cltBugLifeCount.getDiff5()-cltBugLifeCount.getDiff4()));
            list.add((cltBugLifeCount.getDiff6()-cltBugLifeCount.getDiff5()));
            list.add((cltBugLifeCount.getDiff7()-cltBugLifeCount.getDiff6()));
            list.add((cltBugLifeCount.getDiff8()-cltBugLifeCount.getDiff7()));
            list.add((cltBugLifeCount.getDiff9()-cltBugLifeCount.getDiff8()));
            list.add((cltBugLifeCount.getDiff10()-cltBugLifeCount.getDiff9()));
            list.add(cltBugLifeCount.getDiff11());
            dataMap.put("yAxis1",listToString(list));
            wordUtil.createWord("testBugLife.json", preFilePath + testBug.getProId() + "/json/testBugLife.json", dataMap);
            String progressCmd = "node " + outputJsPath + " "+preFilePath + testBug.getProId() + "/json/testBugLife.json " + preFilePath + testBug.getProId() + "/images/testBugLife.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }

        //缺陷状态分布图
        TestBugQualityReportQuery query= new TestBugQualityReportQuery();
        query.setProId(proId);
        query.setType("U");
       /* SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");*/
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, - 6);
        Date monday = c.getTime();
        Date date = new Date();
        query.setStartDate(date);
        query.setEndDate(date);
        query.setTimeType("D");
        ResultInfo<TestBugQualityReportDTO> result = testBugQualityReportFeignClient.testBugStatusReportByModuleOrUser(query);
        if (result != null && result.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "缺陷状态分布");
            List<String> xAxis = result.getData().getxAxisData();
            List<List<TestBugGradeData>> yAxis = result.getData().getyAxisData();
            if(yAxis.size()>0){
                List<String> yAxisResult=new ArrayList<>();
                for (List<TestBugGradeData> yAxisLast :yAxis){

                    for (TestBugGradeData list:yAxisLast) {
                        yAxisResult.add(list.getResult());
                        /*System.out.println("缺陷状态分布y轴"+yAxisResult);*/
                    }
                }
                dataMap.put("xAxis", listChangeToString(xAxis));
                dataMap.put("yAxis1", countNum(yAxisResult,"1"));
                dataMap.put("yAxis2", countNum(yAxisResult,"2"));
                dataMap.put("yAxis3", countNum(yAxisResult,"3"));
                dataMap.put("yAxis4", countNum(yAxisResult,"4"));
                dataMap.put("yAxis5", countNum(yAxisResult,"5"));
                dataMap.put("yAxis6", countNum(yAxisResult,"6"));
                dataMap.put("yAxis7", countNum(yAxisResult,"7"));
                dataMap.put("yAxis8", countNum(yAxisResult,"8"));
                dataMap.put("yAxis9", countNum(yAxisResult,"9"));
                dataMap.put("yAxis10", countNum(yAxisResult,"10"));
                dataMap.put("yAxis11", countNum(yAxisResult,"11"));
                dataMap.put("yAxisTotal", counttotal(yAxisResult));
            } else{
                dataMap.put("xAxis", "[]");
                dataMap.put("yAxis1", "[]");
                dataMap.put("yAxis2", "[]");
                dataMap.put("yAxis3", "[]");
                dataMap.put("yAxis4", "[]");
                dataMap.put("yAxis5", "[]");
                dataMap.put("yAxis6", "[]");
                dataMap.put("yAxis7", "[]");
                dataMap.put("yAxis8", "[]");
                dataMap.put("yAxis9", "[]");
                dataMap.put("yAxis10", "[]");
                dataMap.put("yAxis11", "[]");
                dataMap.put("yAxisTotal", "[]");
            }
                wordUtil.createWord("testBug.json", preFilePath + query.getProId() + "/json/testBugByWeek.json", dataMap);
                String progressCmd ="node " + outputJsPath + " " +preFilePath + query.getProId() + "/json/testBugByWeek.json " + preFilePath +query.getProId() + "/images/testBugByWeek.png";
                try {
                    Process p = run.exec(progressCmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            num ++;

        }
        //模块缺陷分布
        TestBugQualityReportQuery quer= new TestBugQualityReportQuery();
        quer.setProId(proId);
        quer.setType("M");
        quer.setStartDate(date);
        quer.setEndDate(date);
        quer.setTimeType("D");
        ResultInfo<TestBugQualityReportDTO> res = testBugQualityReportFeignClient.testBugStatusReportByModuleOrUser(quer);
        if (res != null && res.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "模块缺陷状态分布");
            List<String> xAxis = res.getData().getxAxisData();
            List<List<TestBugGradeData>> yAxis = res.getData().getyAxisData();
            if (yAxis.size()>0) {

                List<String> yAxisResult = new ArrayList<>();
                for (List<TestBugGradeData> yAxisLast : yAxis) {
                    for (TestBugGradeData list : yAxisLast) {
                        yAxisResult.add(list.getResult());
                       /* System.out.println("模块缺陷y轴"+yAxisResult);*/
                    }
                }

                //System.out.println("11111111111"+yAxisResult);
                dataMap.put("xAxis", listChangeToString(xAxis));
                dataMap.put("yAxis1", countNum(yAxisResult, "1"));
                dataMap.put("yAxis2", countNum(yAxisResult, "2"));
                dataMap.put("yAxis3", countNum(yAxisResult, "3"));
                dataMap.put("yAxis4", countNum(yAxisResult, "4"));
                dataMap.put("yAxis5", countNum(yAxisResult, "5"));
                dataMap.put("yAxis6", countNum(yAxisResult, "6"));
                dataMap.put("yAxis7", countNum(yAxisResult, "7"));
                dataMap.put("yAxis8", countNum(yAxisResult, "8"));
                dataMap.put("yAxis9", countNum(yAxisResult, "9"));
                dataMap.put("yAxis10", countNum(yAxisResult, "10"));
                dataMap.put("yAxis11", countNum(yAxisResult, "11"));
            }else{
                    dataMap.put("xAxis", "[]");
                    dataMap.put("yAxis1", "[]");
                    dataMap.put("yAxis2", "[]");
                    dataMap.put("yAxis3", "[]");
                    dataMap.put("yAxis4", "[]");
                    dataMap.put("yAxis5", "[]");
                    dataMap.put("yAxis6", "[]");
                    dataMap.put("yAxis7", "[]");
                    dataMap.put("yAxis8", "[]");
                    dataMap.put("yAxis9", "[]");
                    dataMap.put("yAxis10", "[]");
                    dataMap.put("yAxis11", "[]");
                    dataMap.put("yAxisTotal", "[]");
                }
                //dataMap.put("yAxisTotal", counttotal(yAxisResult));
                wordUtil.createWord("testBug.json", preFilePath + query.getProId() + "/json/testBugModuleByWeek.json", dataMap);
                String progressCmd ="node " + outputJsPath + " " +preFilePath + query.getProId() + "/json/testBugModuleByWeek.json " + preFilePath +query.getProId() + "/images/testBugModuleByWeek.png";
                try {
                    Process p = run.exec(progressCmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                num ++;
        }
        //缺陷趋势图
        TrendReportQuery q= new TrendReportQuery();
        q.setProId(proId);
        q.setStartDate(monday);
        q.setEndDate(new Date());
        ResultInfo<TrendReportDTO> testBugResult=trendReportFeignClient.testBugTrendReport(q);
        if (testBugResult != null && testBugResult.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "缺陷趋势分布图");
            List<String> xAxis = testBugResult.getData().getxAxis();
            List<String> yAxis = testBugResult.getData().getyAxis();
            dataMap.put("xAxis", listChangeToString(xAxis));
            dataMap.put("yAxis1", countNum(yAxis,"1","2","3","4","5","6","7","8","11"));
            dataMap.put("yAxis2", countNum(yAxis,"9","10"));
            dataMap.put("yAxisTotal", counttotal(yAxis));
            wordUtil.createWord("testBugTrend.json", preFilePath + query.getProId() + "/json/testBugTrendByWeek.json", dataMap);
            String progressCmd ="node " + outputJsPath + " " +preFilePath + query.getProId() + "/json/testBugTrendByWeek.json " + preFilePath +query.getProId() + "/images/testBugTrendByWeek.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num ++;
        }
        return num;
    }
    /**
     * inter类型list转换成string
     *
     * @param list
     * @return
     */
    private String listToString(List<Integer> list) {
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
     * 统计缺陷状态分布数量
     * @param ylist
     * @param num
     * @return
     */
    private String countNum(List<String> ylist,String...num){
        Integer[] actualValues = new Integer[ylist.size()];
        for(int i=0; i<ylist.size(); i++){
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if(StringUtils.isNotEmpty(actualValue)){
                Map resultToMap = JSON.parseObject(actualValue);
                if(resultToMap!=null){
                    int j=0;
                    int size = num.length;
                    for(int k=0;k<num.length;k++){
                        j+=Integer.parseInt((String)resultToMap.get(num[k]));
                    }
                    actualValues[i] = j;
                }
                /* DecimalFormat dFormat=new DecimalFormat("#.00");*/
              /*  String yearString=dFormat.format(actualValues[i]);
                actualValues[i] = Double.valueOf(yearString) * 100;*/
            }else{
                actualValues[i] = 0;
            }
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }



    /**
     * 统计缺陷总数
     * @param ylist
     * @return
     */
    private String counttotal(List<String> ylist){
        Integer[] actualValues = new Integer[ylist.size()];
        for(int i=0; i<ylist.size(); i++){
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if(StringUtils.isNotEmpty(actualValue)){
                Map resultToMap = JSON.parseObject(actualValue);
                actualValues[i] = Integer.parseInt((String)resultToMap.get("total"));
               /* DecimalFormat dFormat=new DecimalFormat("#.00");
                String yearString=dFormat.format(actualValues[i]);*/
               /* actualValues[i] = Double.valueOf(yearString) * 100;*/
            }else{
                actualValues[i] = 0;
            }
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }
}
