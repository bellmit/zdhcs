package net.northking.cloudtest.service.impl.report;

import com.alibaba.fastjson.JSON;
import net.northking.cloudtest.dao.project.CltProjectCountMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.report.CltStsProgressMapper;
import net.northking.cloudtest.domain.user.CltRole;
import net.northking.cloudtest.dto.analyse.MapNodeNumDTO;
import net.northking.cloudtest.dto.report.QualityReportDTO;
import net.northking.cloudtest.dto.report.TestBugGradeData;
import net.northking.cloudtest.dto.report.TestBugQualityReportDTO;
import net.northking.cloudtest.dto.report.TrendReportDTO;
import net.northking.cloudtest.dto.user.UserCount;
import net.northking.cloudtest.feign.report.ProgressReportFeignClient;
import net.northking.cloudtest.feign.report.QualityReportFeignClient;
import net.northking.cloudtest.feign.report.TestBugQualityReportFeignClient;
import net.northking.cloudtest.feign.report.TrendReportFeignClient;
import net.northking.cloudtest.feign.user.RoleFeignClient;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.query.report.TestBugQualityReportQuery;
import net.northking.cloudtest.query.report.TrendReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenTestBugChartService;
import net.northking.cloudtest.utils.WordUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liujinghao on 2018/5/15.
 */
@Service
public class GenTestBugChartServiceImpl  implements GenTestBugChartService {

    private final static Logger logger = LoggerFactory.getLogger(GenTestBugChartServiceImpl.class);


    @Autowired
    private TrendReportFeignClient trendReportFeignClient;

    @Autowired
    private QualityReportFeignClient qualityReportFeignClient;

    @Autowired
    private TestBugQualityReportFeignClient testBugQualityReportFeignClient;

    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;

    /**
     * 缺陷图表
     *
     * @param proId
     * @return
     */
    @Override
    public int genTestBugChart(String proId) throws Exception{
        Runtime run = Runtime.getRuntime();

        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int num = 0;
        TestBugQualityReportQuery query= new TestBugQualityReportQuery();
        query.setProId(proId);
        query.setType("U");
      /*  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");*/
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, - 6);
        Date monday = c.getTime();

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
            System.out.println("xAxis="+dataMap.get("xAxis"));
            System.out.println("111!!!"+dataMap.get("xAxis"));
            dataMap.put("yAxis1", countNum(yAxis,"1","2","3","4","5","6","7","8","11"));
            dataMap.put("yAxis2", countNum(yAxis,"9","10"));
            dataMap.put("yAxisTotal", counttotal(yAxis));
            wordUtil.createWord("testBugTrend.json", preFilePath + query.getProId() + "/json/testBugTrend.json", dataMap);
            String progressCmd ="node " + outputJsPath + " " +preFilePath + query.getProId() + "/json/testBugTrend.json " + preFilePath +query.getProId() + "/images/testBugTrend.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num ++;
        }

        return num;

    }
    //缺陷状态分布图
    @Override
    public int genTestBugMoudle(String proId) throws Exception {
        Runtime run = Runtime.getRuntime();

        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int num = 0;
        //缺陷状态分布图
        TestBugQualityReportQuery query= new TestBugQualityReportQuery();
        query.setProId(proId);
        query.setType("U");
       /* SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");*/
        /*Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, - 6);
        Date monday = c.getTime();*/
        Date date = new Date();
        query.setStartDate(date);
        //System.out.println("缺陷截止日期"+monday);
        query.setEndDate(date);
        query.setTimeType("D");
        ResultInfo<TestBugQualityReportDTO> result = testBugQualityReportFeignClient.testBugStatusReportByModuleOrUser(query);
        //System.out.println("@@@@@@@!!!!!!!!!!"+result.getResultData().toString());
        if (result != null && result.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "缺陷状态分布");
            List<String> xAxis = result.getData().getxAxisData();
            List<List<TestBugGradeData>> yAxis = result.getData().getyAxisData();
            if (yAxis.size() > 0) {

                List<String> yAxisResult = new ArrayList<>();
                for(List<TestBugGradeData> alist:yAxis){
                    List<TestBugGradeData> yAxisLast = alist;
                    for (TestBugGradeData list : yAxisLast) {
                        yAxisResult.add(list.getResult());
                        // System.out.println("缺陷状态分布y轴"+yAxisResult);
                    }
                }


                //System.out.println("11111111111"+yAxisResult);
                dataMap.put("xAxis", listChangeToString(xAxis));
                System.out.println("111!!!" + dataMap.get("xAxis"));
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
                //dataMap.put("yAxisTotal", counttotal(yAxisResult));
                wordUtil.createWord("testBug.json", preFilePath + query.getProId() + "/json/testBugByWeek.json", dataMap);
                String progressCmd = "node " + outputJsPath + " " + preFilePath + query.getProId() + "/json/testBugByWeek.json " + preFilePath + query.getProId() + "/images/testBugByWeek.png";
                try {
                    Process p = run.exec(progressCmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                num++;
                return num;
            }
        }


        return 0;
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
                actualValues[i] =0;
            }
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }
}
