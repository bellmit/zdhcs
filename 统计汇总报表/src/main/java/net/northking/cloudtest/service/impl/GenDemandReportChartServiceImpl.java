package net.northking.cloudtest.service.impl;

import com.alibaba.fastjson.JSON;
import net.northking.cloudtest.dao.project.CltProjectCountMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.project.CltProjectCountExample;
import net.northking.cloudtest.dto.report.ProgressReportDTO;
import net.northking.cloudtest.dto.report.ProjectProgressReportData;
import net.northking.cloudtest.feign.report.ProgressReportFeignClient;
import net.northking.cloudtest.query.report.ProgressReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenCoverageChartReport;
import net.northking.cloudtest.service.GenDemandReportChartService;
import net.northking.cloudtest.service.GenTestTeamService;
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
 * Created by 老邓 on 2018/5/21.
 */
@Service
public class GenDemandReportChartServiceImpl implements GenDemandReportChartService {
    private final static Logger logger = LoggerFactory.getLogger(GenChartServiceImpl.class);
    @Autowired
    private CltProjectMapper cltProjectMapper;
    @Autowired
    private CltProjectCountMapper cltProjectCountMapper;
    @Autowired
    private ProgressReportFeignClient progressReportFeignClient;
    @Autowired
    private GenTestTeamService genTestTeamService;
    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;

    @Autowired
    private GenCoverageChartReport genCoverageChartReport;
    @Override
    public int genChart(String projectId) {

        File dirFile = new File(preFilePath + projectId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        int num = 0;

        //查出项目
        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);

        //测试准出准则
        CltProjectCount cltProjectCount = null;
        CltProjectCountExample projectCountExample = new CltProjectCountExample();
        CltProjectCountExample.Criteria projectCountCriteria = projectCountExample.createCriteria();
        projectCountCriteria.andProIdEqualTo(projectId);
        List<CltProjectCount> cltProjectCountList = cltProjectCountMapper.selectByExample(projectCountExample);
        if(cltProjectCountList.size()<=0){
            return num;
        }

        cltProjectCount = cltProjectCountList.get(0);

        Runtime run = Runtime.getRuntime();

        //查询条件
        ProgressReportQuery progressReportQuery = new ProgressReportQuery();
        progressReportQuery.setProId(projectId);
        progressReportQuery.setDemandStartDate(cltProjectCount.getDemandStartDate());
        progressReportQuery.setDemandEndDate(cltProjectCount.getDemandEndDate());
        progressReportQuery.setProjectStartDate(cltProject.getTestPlanStartTime());
        progressReportQuery.setProjectEndDate(cltProject.getTestPlanEndTime());

        //项目总体报告
        ResultInfo<ProgressReportDTO> totalResultInfo =  progressReportFeignClient.projectProgressReportByTotal(progressReportQuery);
        if(totalResultInfo!=null && totalResultInfo.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "项目总体进度跟踪表");

            List<String> xAxis = totalResultInfo.getData().getxAxis();
            List<String> yAxis = new ArrayList<>();
            List<String> demandyAxis = null;
            int demandPlanNum = 0;
            List<String> testcaseyAxis = null;
            int testcasePlanNum = 0;
            List<String> executeyAxis = null;
            int executePlanNum = 0;

            List<ProjectProgressReportData> projectDataList = totalResultInfo.getData().getProjectData();
            for(ProjectProgressReportData progressReportData : projectDataList){
                if(progressReportData.getName().equals("需求分析阶段")){
                    demandyAxis = progressReportData.getyAxis();
                    demandPlanNum = progressReportData.getPlanNum();
                }else if(progressReportData.getName().equals("用例设计阶段")){
                    testcaseyAxis = progressReportData.getyAxis();
                    testcasePlanNum = progressReportData.getPlanNum();
                }else{
                    executeyAxis = progressReportData.getyAxis();
                    executePlanNum = progressReportData.getPlanNum();
                }
            }

            dataMap.put("xAxis", listChangeToString(xAxis));

            /**
             实际进度:=（(n5*0.5 + n7*0.7 + n8* 0.8 + n10)/计划数量）*0.22+(ss/planNum)*0.25+(("1"+"2"+"6")/plan)0.53
             计划进度::=100%/(结束时间-开始时间+1)*(x轴时间-开始时间)
             */
            dataMap.put("yAxisActual", actualTotalProgress(demandyAxis, demandPlanNum, testcaseyAxis, testcasePlanNum, executeyAxis, executePlanNum));
            dataMap.put("yAxisPlan", planProgress(xAxis, totalResultInfo.getData().getStartDate(), totalResultInfo.getData().getEndDate()));
            wordUtil.createWord("progress.json", preFilePath + projectId + "/json/totalProgress.json", dataMap);

            String demandProgressCmd = "node " + outputJsPath + " " + preFilePath + projectId + "/json/totalProgress.json " + preFilePath + projectId + "/images/totalProgress.png";

            try {
                Process p = run.exec(demandProgressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num ++;
        }


        //需求分析阶段进度表
        ResultInfo<ProgressReportDTO> progressReportDTOResultInfo =  progressReportFeignClient.analyseProgressReportByDay(progressReportQuery);

        if(progressReportDTOResultInfo!=null && progressReportDTOResultInfo.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "需求分析阶段进度跟踪表");

            List<String> xAxis = progressReportDTOResultInfo.getData().getxAxis();
            List<String> yAxis = progressReportDTOResultInfo.getData().getyAxis();
            dataMap.put("xAxis", listChangeToString(xAxis));

            /**
             实际进度:=(n50.5 + n70.7 + n8 0.8 + n101)/计划数量
             计划进度::=100%/(结束时间-开始时间+1)*(x轴时间-开始时间)
             */
            dataMap.put("yAxisActual", actualProgress(yAxis, progressReportDTOResultInfo.getData().getPlanNum()));
            dataMap.put("yAxisPlan", planProgress(xAxis, progressReportDTOResultInfo.getData().getStartDate(), progressReportDTOResultInfo.getData().getEndDate()));
            wordUtil.createWord("progress.json", preFilePath + projectId + "/json/demandProgress.json", dataMap);

            String demandProgressCmd = "node " + outputJsPath + " " + preFilePath + projectId + "/json/demandProgress.json " + preFilePath + projectId + "/images/demandProgress.png";

            try {
                Process p = run.exec(demandProgressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num ++;
        }

        //生成团队人员表
        int i = genTestTeamService.genTestTeam(projectId);
        num+=i;
        //生成覆盖率分析表
        int i1 = genCoverageChartReport.genCoverageChart(projectId);
        num+=i1;
        return num;
    }


    private String listChangeToString(List<String> list){
        String rtnStr = "[";
        for(int i=0; i<list.size(); i++){
            rtnStr += "\"" + list.get(i) + "\",";
        }
        if(rtnStr.indexOf(",")>0){
            rtnStr = rtnStr.substring(0, rtnStr.length()-1);
        }
        return rtnStr + "]";
    }

    /**
     * 总体计算实际进度，实际进度:=需求的实际进度0.22+用例设计的实际进度0.25+用例执行的实际进度*0.53
     * 需求的实际进度:(n50.5 + n70.7 + n8 0.8 + n101)/计划数量
     * 用例设计的实际进度:(total)/计划数量
     * 用例执行的实际进度:(total)/计划数量
     * @param
     * @return
     */
    private String actualTotalProgress(List<String> demandylist, int demandPlanNum, List<String> caseylist, int casePlanNum, List<String> executeylist, int executePlanNum){
        Double[] actualValues = new Double[demandylist.size()];

        for(int i=0; i<demandylist.size(); i++){
            double i1=0.0;
            double i2=0.0;
            double i3=0.0;
            String demainActualValue = demandylist.get(i);
            String caseActualValue = caseylist.get(i);
            String excActualValue = executeylist.get(i);
            logger.info("actualValue:" + demainActualValue);
            if(StringUtils.isNotEmpty(demainActualValue)){
                Map resultToMap = JSON.parseObject(demainActualValue);

                double demandValue = (double) (Integer.parseInt((String)resultToMap.get("n5"))*0.5 + Integer.parseInt((String)resultToMap.get("n7"))*0.7 + Integer.parseInt((String)resultToMap.get("n8"))*0.8 + Integer.parseInt((String)resultToMap.get("n10")))/demandPlanNum;
                i1 = demandValue*0.22;

            }else{
                i1 = 0.0;
            }
            if(StringUtils.isNotEmpty(caseActualValue)){
                double caseValue = 0;
                Map resultToMapTemp = JSON.parseObject(caseActualValue);
                caseValue = (double) Integer.parseInt((String)resultToMapTemp.get("ss"))/casePlanNum;
                i2=caseValue*0.25;
            }else{
                i2=0.0;
            }
            if(StringUtils.isNotEmpty(excActualValue)){

                double executeValue = 0;
                Map resultToMapTemp = JSON.parseObject(excActualValue);
                executeValue =(double) (Integer.parseInt((String)resultToMapTemp.get("1"))+Integer.parseInt((String)resultToMapTemp.get("2"))+Integer.parseInt((String)resultToMapTemp.get("6")))/executePlanNum*1.0;
                i3=executeValue*0.53;
            }else {
                i3=0.0;
            }
            actualValues[i]=i1+i2+i3;
            DecimalFormat dFormat=new DecimalFormat("#.00");
            String yearString=dFormat.format(actualValues[i]);
            actualValues[i] = Double.valueOf(yearString) * 100;
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }


        /**
         * 需求分析阶段计算实际进度，实际进度:=(n50.5 + n70.7 + n8 0.8 + n101)/计划数量
         * @param ylist
         * @return
         */
    private String actualProgress(List<String> ylist, int planNum){
        Double[] actualValues = new Double[ylist.size()];
        for(int i=0; i<ylist.size(); i++){
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if(StringUtils.isNotEmpty(actualValue)){
                Map resultToMap = JSON.parseObject(actualValue);

                actualValues[i] = (Integer.parseInt((String)resultToMap.get("n5"))*0.5 + Integer.parseInt((String)resultToMap.get("n7"))*0.7 + Integer.parseInt((String)resultToMap.get("n8"))*0.8 + Integer.parseInt((String)resultToMap.get("n10")))/planNum;
                DecimalFormat dFormat=new DecimalFormat("#.00");
                String yearString=dFormat.format(actualValues[i]);
                actualValues[i] = Double.valueOf(yearString) * 100;
            }else{
                actualValues[i] = 0.0;
            }
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }

    /**
     * 用例设计阶段计算实际进度，实际进度:=total/计划数量,与执行阶段通用
     * @param ylist
     * @return
     */
    private String actualCaseDesignProgress(List<String> ylist, int planNum){
        Double[] actualValues = new Double[ylist.size()];
        for(int i=0; i<ylist.size(); i++){
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if(StringUtils.isNotEmpty(actualValue)){
                Map resultToMap = JSON.parseObject(actualValue);

                actualValues[i] = Integer.parseInt((String)resultToMap.get("total"))*1.0/planNum;
                DecimalFormat dFormat=new DecimalFormat("#.00");
                String yearString=dFormat.format(actualValues[i]);
                actualValues[i] = Double.valueOf(yearString) * 100;
            }else{
                actualValues[i] = 0.0;
            }
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }

    /**
     * 计划进度::=100%/(结束时间-开始时间+1)*（x轴时间-开始时间)
     * @param xlist
     * @param
     * @return
     */
    private String planProgress(List<String> xlist, Date startDate, Date endDate) {
        Double[] planValues = new Double[xlist.size()];
        int size = xlist.size();
        for (int i = 0; i <size; i++) {
            double v = (double) i / (size-1);
            double a =  v*100d;
            planValues[i] = a<0.0d?0.0d:a;

        }
        return "[" + StringUtils.join(planValues, ",") + "]";
    }

    /**
     * 计算两个日期之间相差的天数
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public int daysBetween(Date smdate,Date bdate) throws ParseException
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        smdate=sdf.parse(sdf.format(smdate));
        bdate=sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days=(time2-time1)/(1000*3600*24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 计算两个日期之间相差的天数
     * @param smdate 较小的时间
     * @param xdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public int daysBetween(Date smdate,String xdate) throws ParseException
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        smdate=sdf.parse(sdf.format(smdate));
        Date bdate=sdf.parse(xdate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days=(time2-time1)/(1000*3600*24);

        return Integer.parseInt(String.valueOf(between_days));
    }
}
