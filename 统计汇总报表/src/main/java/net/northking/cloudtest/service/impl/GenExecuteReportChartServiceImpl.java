package net.northking.cloudtest.service.impl;

import com.alibaba.fastjson.JSON;
import net.northking.cloudtest.constants.ErrorConstants;
import net.northking.cloudtest.dao.project.CltProjectCountMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.domain.analyse.CltRound;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.project.CltProjectCountExample;
import net.northking.cloudtest.dto.report.ProgressReportDTO;
import net.northking.cloudtest.dto.report.ProjectProgressReportData;
import net.northking.cloudtest.exception.GlobalException;
import net.northking.cloudtest.feign.analyse.CltRoundFeignClient;
import net.northking.cloudtest.feign.report.ProgressReportFeignClient;
import net.northking.cloudtest.query.analyse.CltRoundQuery;
import net.northking.cloudtest.query.report.ProgressReportQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.*;
import net.northking.cloudtest.service.impl.report.GenChartExecuteServiceImpl;
import net.northking.cloudtest.utils.CltUtils;
import net.northking.cloudtest.utils.ServiceUtil;
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
 * @Author:zwy
 * @Despriction:
 * @Date:Create in 10:57 2018/5/24
 * @Modify By:
 */
@Service
public class GenExecuteReportChartServiceImpl implements GenExecuteReportChartService {
    private final static Logger logger = LoggerFactory.getLogger(GenExecuteReportChartServiceImpl.class);
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
    @Autowired
    private GenTestCaseTrendChartService genTestCaseTrendChartService;
    @Autowired
    private GenChartExecuteService genChartExecuteService;
    @Autowired
    private GenTestBugChartService genTestBugChartService;
    @Autowired
    private GenChartService genChartService;
    @Autowired
    private CltRoundFeignClient cltRoundFeignClient;
    @Override
    public int genChart(String projectId, String roundId) {

       // String outputJsPath = "D:/nodeChart/output.js";

        File dirFile = new File(preFilePath + projectId + "/json/" + roundId);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File dirFile2 = new File(preFilePath + projectId + "/images/" + roundId);
        if (!dirFile2.exists()) {
            dirFile2.mkdirs();
        }

        int num = 0;

        try {
            num+=genCaseExecuteSingleRound(projectId,roundId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        genChartService.genChart(projectId);
        //用例执行趋势图
        try {
            num+=genTestCaseTrendChartService.genTestCaseTrendChart(projectId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        //生成团队人员表
        int i = genTestTeamService.genTestTeam(projectId);
        num+=i;
        //生成覆盖率分析表
        int i1 = genCoverageChartReport.genCoverageChart(projectId);
        num+=i1;

        //用例执行情况图表按人员
        try {
            num+=genChartExecuteService.GenChartExecuteByUser(projectId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        //缺陷趋势图testBugTrend.png
        try {
            num+=genTestBugChartService.genTestBugChart(projectId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        //生成缺陷状态分布图testBugMoudle.png
        try {
            num+=genTestBugChartService.genTestBugMoudle(projectId);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
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


    //用例执行阶段进度报告（单轮）

    private ResultInfo<ProgressReportDTO> executeSingleRoundReport(ProgressReportQuery query)throws Exception {
        CltUtils.printStartInfo(query, "executeSingleRoundReport");



        ResultInfo<ProgressReportDTO> result  = null;

        String typeId=query.getTypeId();


        try {
            //判断前端是否传递typeId;
            if(StringUtils.isEmpty(typeId)){

                //根据项目Id查询伦次
                CltRoundQuery cltRoundQuery=new CltRoundQuery();
                cltRoundQuery.setProId(query.getProId());
                ResultInfo<List<CltRound>> allRoundByProId = cltRoundFeignClient.findAllRoundByProId(cltRoundQuery);

                if(allRoundByProId!=null &&allRoundByProId.getCode().equals(ResultCode.SUCCESS.code())){

                    List<CltRound> resultData = allRoundByProId.getData();

                    if(resultData!=null && resultData.size()>0){

                        CltRound cltRound = resultData.get(0);

                        String strTypeId=cltRound.getRoundId();

                        Integer planNum = getTestCaseNumByRoundId(cltRound.getProId(), strTypeId);

                        query.setExecutePlanNum(planNum);
                        query.setTypeId(strTypeId);
                        query.setStartDate(cltRound.getStartDate());
                        query.setEndDate(cltRound.getEndDate());




                    }

                }

            }else{

                CltRoundQuery cltRoundQuery=new CltRoundQuery();

                cltRoundQuery.setRoundId(query.getTypeId());

                ResultInfo<CltRound> round = cltRoundFeignClient.round(cltRoundQuery);

                if(round!=null &&round.getCode().equals(ResultCode.SUCCESS.code())){

                    Integer planNum = getTestCaseNumByRoundId(round.getData().getProId(), round.getData().getRoundId());
                    query.setStartDate(round.getData().getStartDate());
                    query.setEndDate(round.getData().getEndDate());
                    query.setExecutePlanNum(planNum);
                }

            }


            //调用微服务接口
            result = progressReportFeignClient.executeSingleRoundReport(query);

        }catch (Exception e){

            logger.error("executeSingleRoundReport",e);

            throw new GlobalException(ErrorConstants.CLT_WEB_REPORT_ERROR_CODE,ErrorConstants.CLT_WEB_REPORT_ERROR_MESSAGE);

        }





        return   result;
    }

    //查询轮次下面的用例数量
    private Integer getTestCaseNumByRoundId(String proId,String roundId){

        Integer planNum=0;

        try {
            //调用数据库

            CltRoundQuery cltRoundQuery=new CltRoundQuery();
            cltRoundQuery.setRoundId(roundId);
            cltRoundQuery.setProId(proId);
            ResultInfo<Integer> integerResultInfo = cltRoundFeignClient.queryTestCaseNumByRoundId(cltRoundQuery);

            if(integerResultInfo!=null&& integerResultInfo.getCode().equals(ResultCode.SUCCESS.code())){

                planNum=integerResultInfo.getData();

            }


        } catch (Exception e) {

            throw new GlobalException(ErrorConstants.CLT_WEB_ANALYSE_ERROR_CODE, ErrorConstants.CLT_WEB_ANALYSE_ERROR_CODE);

        }
        return planNum;
    }

    //生成用例执行阶段进度跟踪表(单轮)
    public Integer genCaseExecuteSingleRound(String proId,String roundId) throws Exception {
        Runtime run = Runtime.getRuntime();
        ProgressReportQuery query = new ProgressReportQuery();
        query.setProId(proId);
        if(StringUtils.isNotEmpty(roundId)){
            query.setTypeId(roundId);
        }

        int num=0;
        ResultInfo<ProgressReportDTO> caseDesignResultInfo = executeSingleRoundReport(query);

        if(caseDesignResultInfo!=null && caseDesignResultInfo.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("title", "用例执行阶段进度跟踪表(单轮)");

            List<String> xAxis = caseDesignResultInfo.getData().getxAxis();
            List<String> yAxis = caseDesignResultInfo.getData().getyAxis();
            dataMap.put("xAxis", listChangeToString(xAxis));

            /**
             实际进度:=total/计划数量
             计划进度::=100%/(结束时间-开始时间+1)*(x轴时间-开始时间)
             */
            dataMap.put("yAxisActual", actualCaseDesignProgress(yAxis, caseDesignResultInfo.getData().getPlanNum(),"case_design"));
            dataMap.put("yAxisPlan", planProgress(xAxis, caseDesignResultInfo.getData().getStartDate(), caseDesignResultInfo.getData().getEndDate()));
            wordUtil.createWord("progress.json", preFilePath + proId + "/json/caseDesignProgressSingle.json", dataMap);

            String progressCmd = "node " + outputJsPath + " " + preFilePath + proId + "/json/caseDesignProgressSingle.json " + preFilePath + proId + "/images/caseDesignProgressSingle.png";

            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                logger.error("error:", e);
            }
            num ++;
        }
      return num;
    }

    private String actualCaseDesignProgress(List<String> ylist, int planNum,String code){
        Double[] actualValues = new Double[ylist.size()];
        for(int i=0; i<ylist.size(); i++){
            String actualValue = ylist.get(i);
            logger.info("actualValue:" + actualValue);
            if(StringUtils.isNotEmpty(actualValue)){
                Map resultToMap = JSON.parseObject(actualValue);


                    actualValues[i] = (Integer.parseInt((String)resultToMap.get("1"))+Integer.parseInt((String)resultToMap.get("2"))+Integer.parseInt((String)resultToMap.get("6")))*1.0/planNum;


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





}
