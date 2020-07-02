package net.northking.cloudtest.service.impl;

import com.alibaba.fastjson.JSON;
import net.northking.cloudtest.dto.analyse.MapNodeNumDTO;
import net.northking.cloudtest.dto.report.TrendReportDTO;
import net.northking.cloudtest.query.report.TestBugQualityReportQuery;
import net.northking.cloudtest.query.report.TrendReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenTestCaseTrendChartService;
import net.northking.cloudtest.service.TrendReportService;
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
 * @Author:zwy
 * @Despriction:
 * @Date:Create in 15:22 2018/5/25
 * @Modify By:
 */
@Service
public class GenTestCaseTrendChartServiceImpl implements GenTestCaseTrendChartService {
    private Logger logger = LoggerFactory.getLogger(GenTestCaseTrendChartServiceImpl.class);
    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;
    @Autowired
    private TrendReportService trendReportService;
    @Override
    public int genTestCaseTrendChart(String proId) throws Exception {
        Runtime run = Runtime.getRuntime();

        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int num = 0;
        //用例执行趋势图
       // TestBugQualityReportQuery query= new TestBugQualityReportQuery();
        //query.setProId(proId);
       // query.setType("U");


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date=sdf.parse(sdf.format(new Date()));
        Calendar lastDate = Calendar.getInstance();
        lastDate.roll(Calendar.DATE, -6);//日期回滚6天,查询一周
        sdf.format(lastDate.getTime());
        //用例执行趋势图
        TrendReportQuery q= new TrendReportQuery();
        q.setProId(proId);
        q.setStartDate(sdf.parse(sdf.format(lastDate.getTime())));
        q.setEndDate(date);
        q.setType("U");
        TrendReportDTO trendReportDTO = trendReportService.testExecuteTrendReport(q);
        if (trendReportDTO != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "用例执行趋势图");
            List<String> xAxis = trendReportDTO.getxAxis();
            List<String> yAxis = trendReportDTO.getyAxis();
            String jxAxis=listChangeToString(xAxis);
            dataMap.put("xAxis", jxAxis);
            dataMap.put("yAxis1", countNum(yAxis,"total"));
            dataMap.put("yAxis2", countNum(yAxis,"1","2","6"));//1 2 6已完成数量
            dataMap.put("yAxis3", countNum(yAxis,"0","3","4","5"));//0 3 5 4未完成数量
            wordUtil.createWord("testCaseTrend.json", preFilePath + proId + "/json/testCaseTrend.json", dataMap);
            String progressCmd ="node " + outputJsPath + " " +preFilePath + proId + "/json/testCaseTrend.json " + preFilePath +proId + "/images/testCaseTrend.png";
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
    private String ObjectToString(List<MapNodeNumDTO> list) {
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
     * 将对象转换成string
     * @param list
     * @return
     */
    public String ojectToJsonString(List<MapNodeNumDTO>list) {
        String map="[";

        for (MapNodeNumDTO mapNodeNumDTO:
                list) {
            map+="{\"name"+"\":"+"\""+mapNodeNumDTO.getStatus()+"\","+"\"value"+"\":" + mapNodeNumDTO.getNodeNum() +"}"+ ",";

        }
        if (map.indexOf(",") > 0) {
            map = map.substring(0, map.length() - 1);
        }
        return map+"]";
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
     * 统计各缺陷状态数量
     * @param ylist
     * @return
     */
    private String countNum(List<String> ylist,String...num){
        Double[] actualValues = new Double[ylist.size()];
        for(int i=0; i<ylist.size(); i++){
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if(StringUtils.isNotEmpty(actualValue)){
                Map resultToMap = JSON.parseObject(actualValue);
                if(resultToMap!=null){
                    double j=0;
                    int size = num.length;
                    for(int k=0;k<num.length;k++){
                        j+=Integer.parseInt((String)resultToMap.get(num[k]))*1.0;
                    }
                    actualValues[i] = j;
                }
                /* DecimalFormat dFormat=new DecimalFormat("#.00");*/
              /*  String yearString=dFormat.format(actualValues[i]);
                actualValues[i] = Double.valueOf(yearString) * 100;*/
            }else{
                actualValues[i] = 0.0;
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
        Double[] actualValues = new Double[ylist.size()];
        for(int i=0; i<ylist.size(); i++){
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if(StringUtils.isNotEmpty(actualValue)){
                Map resultToMap = JSON.parseObject(actualValue);
                actualValues[i] = Integer.parseInt((String)resultToMap.get("total"))*1.0;
               /* DecimalFormat dFormat=new DecimalFormat("#.00");
                String yearString=dFormat.format(actualValues[i]);*/
                /* actualValues[i] = Double.valueOf(yearString) * 100;*/
            }else{
                actualValues[i] = 0.0;
            }
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }
}
