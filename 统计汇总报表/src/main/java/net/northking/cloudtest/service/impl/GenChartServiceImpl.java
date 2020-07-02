package net.northking.cloudtest.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.northking.cloudtest.dao.project.CltProjectCountMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.report.CltStsProgressMapper;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.project.CltProjectCountExample;
import net.northking.cloudtest.domain.report.CltStsProgress;
import net.northking.cloudtest.domain.report.CltStsProgressExample;
import net.northking.cloudtest.domain.user.CltRole;
import net.northking.cloudtest.dto.report.ProgressReportDTO;
import net.northking.cloudtest.dto.report.ProjectProgressReportData;
import net.northking.cloudtest.dto.report.UserCountDtoReprot;
import net.northking.cloudtest.dto.user.UserCount;
import net.northking.cloudtest.enums.CltStsProgressCatalog;
import net.northking.cloudtest.feign.report.ProgressReportFeignClient;
import net.northking.cloudtest.feign.user.RoleFeignClient;
import net.northking.cloudtest.query.report.ProgressReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenChartService;
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
 * @Title: 生成图表服务
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/12
 * @UpdateUser:
 * @Version:0.1
 */
@Service
public class GenChartServiceImpl implements GenChartService{

    private final static Logger logger = LoggerFactory.getLogger(GenChartServiceImpl.class);

    @Autowired
    private CltProjectMapper cltProjectMapper;

    @Autowired
    private CltStsProgressMapper cltStsProgressMapper;

    @Autowired
    private ProgressReportFeignClient progressReportFeignClient;

    @Autowired
    private RoleFeignClient roleFeignClient;

    @Autowired
    private CltProjectCountMapper cltProjectCountMapper;

    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;

    @Override
    public int genChart(String projectId) {

        File dirFile = new File(preFilePath + projectId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        File dirFile2 = new File(preFilePath + projectId + "/images");
        if (!dirFile2.exists()) {
            dirFile2.mkdir();
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
             实际进度:=(n50.5 + n70.7 + n8 0.8 + n101)/计划数量
             计划进度::=100%/(结束时间-开始时间+1)*(x轴时间-开始时间)
             */
            dataMap.put("yAxisActual", actualTotalProgress(demandyAxis, demandPlanNum, testcaseyAxis, testcasePlanNum, executeyAxis, executePlanNum));
            dataMap.put("yAxisPlan", planProgress(xAxis, totalResultInfo.getData().getStartDate(), totalResultInfo.getData().getEndDate()));
            wordUtil.createWord("progress.json", preFilePath + projectId + "/json/totalProgress.json", dataMap);

            String demandProgressCmd = "node " + outputJsPath + " " + preFilePath + projectId + "/json/totalProgress.json " + preFilePath + projectId + "/images/totalProgress.png";

            try {
                Process p = run.exec(demandProgressCmd);
            } catch (Exception e) {
                logger.error("error:", e);
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
                logger.error("error:", e);
            }
            num ++;
        }

        //设计阶段进度表
        ResultInfo<ProgressReportDTO> caseDesignResultInfo =  progressReportFeignClient.testCaseProgressReportByDay(progressReportQuery);
        if(caseDesignResultInfo!=null && caseDesignResultInfo.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "用例设计阶段进度跟踪表");

            List<String> xAxis = caseDesignResultInfo.getData().getxAxis();
            List<String> yAxis = caseDesignResultInfo.getData().getyAxis();
            dataMap.put("xAxis", listChangeToString(xAxis));

            /**
             实际进度:=total/计划数量
             计划进度::=100%/(结束时间-开始时间+1)*(x轴时间-开始时间)
             */
            dataMap.put("yAxisActual", actualCaseDesignProgress(yAxis, caseDesignResultInfo.getData().getPlanNum(),"case_design"));
            dataMap.put("yAxisPlan", planProgress(xAxis, caseDesignResultInfo.getData().getStartDate(), caseDesignResultInfo.getData().getEndDate()));
            wordUtil.createWord("progress.json", preFilePath + projectId + "/json/caseDesignProgress.json", dataMap);

            String progressCmd = "node " + outputJsPath + " " + preFilePath + projectId + "/json/caseDesignProgress.json " + preFilePath + projectId + "/images/caseDesignProgress.png";

            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                logger.error("error:", e);
            }
            num ++;
        }

        //执行阶段总进度表
        ResultInfo<ProgressReportDTO> caseExecuteResultInfo =  progressReportFeignClient.executeTotalReport(progressReportQuery);
        if(caseExecuteResultInfo!=null && caseExecuteResultInfo.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "用例执行阶段进度跟踪表(总体)");

            List<String> xAxis = caseExecuteResultInfo.getData().getxAxis();
            List<String> yAxis = caseExecuteResultInfo.getData().getyAxis();
            dataMap.put("xAxis", listChangeToString(xAxis));

            /**
             实际进度:=total/计划数量
             计划进度::=100%/(结束时间-开始时间+1)*(x轴时间-开始时间)
             */
            dataMap.put("yAxisActual", actualCaseDesignProgress(yAxis, caseExecuteResultInfo.getData().getPlanNum(),"case_execute"));
            dataMap.put("yAxisPlan", planProgress(xAxis, caseExecuteResultInfo.getData().getStartDate(), caseExecuteResultInfo.getData().getEndDate()));
            wordUtil.createWord("progress.json", preFilePath + projectId + "/json/caseExecuteProgress.json", dataMap);

            String progressCmd = "node " + outputJsPath + " " + preFilePath + projectId + "/json/caseExecuteProgress.json " + preFilePath + projectId + "/images/caseExecuteProgress.png";

            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                logger.error("error:", e);
            }
            num ++;
        }

        //生成团队人员结构图
        CltRole cltRole = new CltRole();
        cltRole.setProId(projectId);
        ResultInfo<UserCount> userCountResultInfo = roleFeignClient.selectRoleCount(cltRole);
        UserCount userCount = userCountResultInfo.getData();
        if (userCount != null) {
            WordUtil wordUtil = new WordUtil();
            UserCountDtoReprot userCountDtoReprot = get(userCount);
            //放到集合里是因为模板前后有[ ]
            List<UserCountDtoReprot> list = new ArrayList<>();
            list.add(userCountDtoReprot);
            //把集合转成json
            String json = JSONObject.toJSONString(list);
            Map dataMap = new HashMap();
            dataMap.put("xAxis", json);
            wordUtil.createWord("team.json", preFilePath + projectId + "/json/testTeam.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + projectId + "/json/testTeam.json " + preFilePath +projectId + "/images/testTeam.png";
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
     * 获取当时日期前7天日期
     * @return
     */
    private String[] getStartAndEndDate(){
        String[] dates = new String[2];
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        Date nowDate = new Date();

        Calendar sdate = Calendar.getInstance();
        sdate.setTime(nowDate);
        sdate.set(Calendar.DATE, sdate.get(Calendar.DATE) - 7);

        String endDate = dft.format(nowDate.getTime());
        String startDate = dft.format(sdate.getTime());
        dates[0] = startDate;
        dates[1] = endDate;

        return dates;
    }

    public static void main(String args[]){
        List<String> list = new ArrayList<>();
        list.add("2018-093");
        list.add("23834");
        System.out.println(new GenChartServiceImpl().listChangeToString(list));
    }

    /**
     * list转换成string
     * @param list
     * @return
     */
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

                double demandValue = (double) ( Integer.parseInt((String)resultToMap.get("n10")))/demandPlanNum;
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

                actualValues[i] = (double)(Integer.parseInt((String)resultToMap.get("n10")))/planNum;
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
    private String actualCaseDesignProgress(List<String> ylist, int planNum,String code){
        Double[] actualValues = new Double[ylist.size()];
        for(int i=0; i<ylist.size(); i++){
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if(StringUtils.isNotEmpty(actualValue)){
                Map resultToMap = JSON.parseObject(actualValue);
                if("case_design".equals(code)){

                    actualValues[i] = Integer.parseInt((String)resultToMap.get("ss"))*1.0/planNum;
                }
                if("case_execute".equals(code)){

                    actualValues[i] = (double) (Integer.parseInt((String)resultToMap.get("1"))+Integer.parseInt((String)resultToMap.get("2"))+Integer.parseInt((String)resultToMap.get("6")))/planNum*1.0;

                }
                DecimalFormat dFormat=new DecimalFormat("#.00");
                String yearString=dFormat.format(actualValues[i]);
                actualValues[i] = Double.valueOf(yearString) * 100;
            }else{
                actualValues[i] = 0.0;
            }
        }
        return "[" + StringUtils.join(actualValues, ",") + "]";
    }


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





    public UserCountDtoReprot get(UserCount userCount) {
        UserCountDtoReprot userCountDtoReprot = new UserCountDtoReprot();
        String roleName = userCount.getRoleName();
        int count = userCount.getCount();
        userCountDtoReprot.setName(roleName+"("+count+")");
        List<UserCount> children =  userCount.getChildren();
        List<UserCountDtoReprot> list = new ArrayList<>();
        //如果存在children
        if(children.size()>0){
            for (UserCount userCount1:children){
                UserCountDtoReprot userCountDtoReprot1 = get(userCount1);
                list.add(userCountDtoReprot1);
            }

            userCountDtoReprot.setChildren(list);

        }
        return userCountDtoReprot;
    }
}
