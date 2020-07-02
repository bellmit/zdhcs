package net.northking.cloudtest.service.impl;

import net.northking.cloudtest.assist.RedisLock;
import net.northking.cloudtest.constants.ErrorConstants;
import net.northking.cloudtest.dao.analyse.CltMapNodeMapper;
import net.northking.cloudtest.dao.analyse.TestCaseMapper;
import net.northking.cloudtest.dao.project.CltProjectCountMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.report.CltStsProgressMapper;
import net.northking.cloudtest.dao.task.CltTestcaseExecuteMapper;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.project.CltProjectCountExample;
import net.northking.cloudtest.domain.report.CltStsProgress;
import net.northking.cloudtest.dto.analyse.MapNodeNumDTO;
import net.northking.cloudtest.dto.report.ProgressReportDTO;
import net.northking.cloudtest.dto.report.ProjectProgressReportData;
import net.northking.cloudtest.dto.report.TestCaseExecuteReportDTO;
import net.northking.cloudtest.exception.GlobalException;
import net.northking.cloudtest.feign.analyse.CaseAppBizParamFeignClient;
import net.northking.cloudtest.feign.analyse.DemandFeignClient;
import net.northking.cloudtest.feign.execute.ManualExecFeignClient;
import net.northking.cloudtest.query.analyse.DemandQuery;
import net.northking.cloudtest.query.report.ProgressReportQuery;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.service.ProgressReportService;
import net.northking.cloudtest.utils.BigDecimalUtil;
import net.northking.cloudtest.utils.MapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Title:
 * @Description:
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-04 19:23
 * @UpdateUser:
 * @Version:0.1
 */

@Service
public class ProgressReportServiceImpl implements ProgressReportService {


    //日志
    private final static Logger logger = LoggerFactory.getLogger(ProgressReportServiceImpl.class);

    @Autowired
    private CltProjectCountMapper cltProjectCountMapper;

    @Autowired
    private CltStsProgressMapper cltStsProgressMapper;

    @Autowired
    private CltMapNodeMapper cltMapNodeMapper;

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Autowired
    private CltTestcaseExecuteMapper testcaseExecuteMapper;
    @Autowired
    private CaseAppBizParamFeignClient caseAppBizParamFeignClient;


    @Autowired
    ManualExecFeignClient manualExecFeignClient;

    @Autowired
    RedisLock redisLock;


    //需求分析阶段进度报告（每日）
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public ProgressReportDTO analyseProgressReportByDay(ProgressReportQuery progressReportQuery) throws Exception {

        // funcCode:1.需求分析,2.用例测试.3.用例执行阶段
        String funcCode = "1";


        //判断前端是否传递计划数,和计划开始时间和结束时间
        Integer demandPlanNumUI = progressReportQuery.getDemandPlanNum();//计划数(前端)

        Date demandStartDateUI = progressReportQuery.getStartDate();//计划开始时间(前端)

        Date demandendDateUI = progressReportQuery.getEndDate();//计划结束时间(前端)

        //查询数据库是否存在这三个数
        CltProjectCount cltProjectCount = queryCltProjectCountByProId(progressReportQuery.getProId(), demandPlanNumUI, demandStartDateUI, demandendDateUI, funcCode);

        Integer demandPlanNumDB = null;

        Date demandStartDateDB = null;

        Date demandendDateDB = null;

        if (cltProjectCount != null) {

            demandPlanNumDB = cltProjectCount.getDemandPlanNum();//计划数(数据库)

            demandStartDateDB = cltProjectCount.getDemandStartDate();//计划开始时间(数据库)

            demandendDateDB = cltProjectCount.getDemandEndDate();//计划结束时间(数据库)
        }


        if (demandPlanNumUI == null || demandStartDateUI == null || demandendDateUI == null) {  //1.如果前端没有传递

            if (demandPlanNumDB == null || demandStartDateDB == null || demandendDateDB == null) {//1).如果后台不存在数据

                return null;//返回空值

            } else {  //2)如果后台不为空

                progressReportQuery.setDemandPlanNum(demandPlanNumDB);
                progressReportQuery.setDemandStartDate(demandStartDateDB);
                progressReportQuery.setDemandEndDate(demandendDateDB);

                //funcCode:1.需求分析,2.用例测试.3.用例执行阶段
                ProgressReportDTO progressReportDTO = queryProgressReportDataInfo(progressReportQuery, funcCode);

                return progressReportDTO;

            }

        } else {

            updateCltProjectCountInfoByProId(progressReportQuery, "D"); //更新前端数据到数据库

            ProgressReportDTO progressReportDTO = queryProgressReportDataInfo(progressReportQuery, funcCode);

            return progressReportDTO;

        }

    }


    /**
     * 需求分析阶段进度报告（饼图）
     *
     * @param progressReportQuery
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public ProgressReportDTO analyseProgressReportByPie(ProgressReportQuery progressReportQuery) throws Exception {

        //funcCode:1.需求分析,2.用例测试.3.用例执行阶段

        String funcCode = "1";


        Integer demandPlanNumUI = progressReportQuery.getDemandPlanNum();  //前端传递的计划数

        Date demandStartDateUI = progressReportQuery.getStartDate();//计划开始时间(前端)

        Date demandEndDateUI = progressReportQuery.getEndDate();//计划结束时间(前端)

        Integer demandPlanNumDB = null;

        Date demandStartDateDB = null;

        Date demandEndDate = null;

        CltProjectCount cltProjectCount = queryCltProjectCountByProId(progressReportQuery.getProId(), demandPlanNumUI, demandStartDateUI, demandEndDateUI, funcCode);

        if (cltProjectCount != null) {

            demandPlanNumDB = cltProjectCount.getDemandPlanNum();

            demandStartDateDB = cltProjectCount.getDemandStartDate();

            demandEndDate = cltProjectCount.getDemandEndDate();

        }

        if (demandPlanNumUI == null) { //1.如果前端传没有传递计划数量

            if (demandPlanNumDB == null) {  //1).如果数据库没有计划数量

                ProgressReportDTO progressReportDTO = queryCltMapNodeListByProId(progressReportQuery.getProId(), null, null, null); //查询所有每个状态对应的数量,返回一个空的百分比给前端

                return progressReportDTO;
            } else {  //2).如果数据库存在计划数量


                ProgressReportDTO progressReportDTO = queryCltMapNodeListByProId(progressReportQuery.getProId(), demandPlanNumDB, demandStartDateDB, demandEndDate);//查询所有的状态对应的数量,并返回一个百分比给前端(判断计划数是否大于需求节点总数)

                return progressReportDTO;
            }

        } else { //2.如果前端有传递计划数量

            updateCltProjectCountInfoByProId(progressReportQuery, "D");//更新该计划数量到统计表中

            ProgressReportDTO progressReportDTO = queryCltMapNodeListByProId(progressReportQuery.getProId(), demandPlanNumUI, demandStartDateUI, demandEndDateUI);

            return progressReportDTO;

        }

    }

    //用例设计阶段进度报告（每日）
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public ProgressReportDTO testCaseProgressReportByDay(ProgressReportQuery progressReportQuery) throws Exception {
        //funcCode:1.需求分析,2.用例测试.3.用例执行阶段

        String funcCode = "2";


        //判断前端是否传递计划数,和计划开始时间和结束时间
        Integer testPlanNumUI = progressReportQuery.getTestPlanNum();//计划数(前端)

        Date testStartDateUI = progressReportQuery.getStartDate();//计划开始时间(前端)

        Date testEndDateUI = progressReportQuery.getEndDate();//计划结束时间(前端)

        //查询数据库是否存在这三个数
        CltProjectCount cltProjectCount = queryCltProjectCountByProId(progressReportQuery.getProId(), testPlanNumUI, testStartDateUI, testEndDateUI, funcCode);

        Integer testPlanNumDB = null;

        Date testStartDateDB = null;

        Date testendDateDB = null;

        if (cltProjectCount != null) {

            testPlanNumDB = cltProjectCount.getTestPlanNum();//计划数(数据库)

            testStartDateDB = cltProjectCount.getTestStartDate();//计划开始时间(数据库)

            testendDateDB = cltProjectCount.getTestEndDate();//计划结束时间(数据库)
        }


        if (testPlanNumUI == null || testStartDateUI == null || testEndDateUI == null) {  //1.如果前端没有传递

            if (testPlanNumDB == null || testStartDateDB == null || testendDateDB == null) {//1).如果后台不存在数据

                return null;//返回空值

            } else {  //2)如果后台不为空

                progressReportQuery.setTestPlanNum(testPlanNumDB);
                progressReportQuery.setTestStartDate(testStartDateDB);
                progressReportQuery.setTestEndDate(testendDateDB);

                ProgressReportDTO progressReportDTO = queryProgressReportDataInfo(progressReportQuery, funcCode);

                return progressReportDTO;

            }

        } else {

            updateCltProjectCountInfoByProId(progressReportQuery, "T"); //更新前端数据到数据库

            ProgressReportDTO progressReportDTO = queryProgressReportDataInfo(progressReportQuery, funcCode);

            return progressReportDTO;

        }

    }


    //用例设计阶段进度报告（饼图）
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public ProgressReportDTO testCaseProgressReportByPie(ProgressReportQuery progressReportQuery) throws Exception {

        //funcCode:1.需求分析,2.用例测试.3.用例执行阶段

        String funcCode = "2";


        Integer testPlanNumUI = progressReportQuery.getTestPlanNum();  //前端传递的计划数

        Date testStartDateUI = progressReportQuery.getStartDate();//计划开始时间(前端)

        Date testEndDateUI = progressReportQuery.getEndDate();//计划结束时间(前端)


        Integer testPlanNumDB = null;

        Date testStartDateDB = null;

        Date testEndDateDB = null;

        CltProjectCount cltProjectCount = queryCltProjectCountByProId(progressReportQuery.getProId(), testPlanNumUI, testStartDateUI, testEndDateUI, funcCode);

        if (cltProjectCount != null) {

            testPlanNumDB = cltProjectCount.getTestPlanNum();

            testStartDateDB = cltProjectCount.getTestStartDate();

            testEndDateDB = cltProjectCount.getTestEndDate();
        }


        if (testPlanNumUI == null) { //1.如果前端传没有传递计划数量

            if (testPlanNumDB == null) {  //1).如果数据库没有计划数量

                ProgressReportDTO progressReportDTO = queryTestCaseNumByProId(progressReportQuery.getProId(), null, null, null); //查询所有每个状态对应的数量,返回一个空的百分比给前端

                return progressReportDTO;
            } else {  //2).如果数据库存在计划数量

                ProgressReportDTO progressReportDTO = queryTestCaseNumByProId(progressReportQuery.getProId(), testPlanNumDB, testStartDateDB, testEndDateDB);//查询所有的状态对应的数量,并返回一个百分比给前端(判断计划数是否大于需求节点总数)

                return progressReportDTO;
            }

        } else { //2.如果前端有传递计划数量

            updateCltProjectCountInfoByProId(progressReportQuery, "T");//更新该计划数量到统计表中

            ProgressReportDTO progressReportDTO = queryTestCaseNumByProId(progressReportQuery.getProId(), testPlanNumUI, testStartDateUI, testEndDateUI);//查询所有的状态对应的数量,并返回一个百分比给前端(判断计划数是否大于需求节点的总数)

            return progressReportDTO;

        }

    }


    //用例执行阶段进度报告(饼图)
    @Override
    public ProgressReportDTO executeProgressReportByPie(ProgressReportQuery progressReportQuery) throws Exception {
        //funcCode:1.需求分析,2.用例测试.3.用例执行阶段
        String funcCode = "3";

        //判断前端是否传递计划数,和计划开始时间和结束时间
        Integer executePlanNumUI = progressReportQuery.getExecutePlanNum();//计划数(前端)

        Date executeStartDateUI = progressReportQuery.getStartDate();//计划开始时间(前端)

        Date executeEndDateUI = progressReportQuery.getEndDate();//计划结束时间(前端)

        //查询数据库是否存在这三个数
        CltProjectCount cltProjectCount = queryCltProjectCountByProId(progressReportQuery.getProId(), executePlanNumUI, executeStartDateUI, executeEndDateUI, funcCode);

        Integer executePlanNumDB = null;

        Date executeStartDateDB = null;

        Date executeEndDateDB = null;

        if (cltProjectCount != null) {

            executePlanNumDB = cltProjectCount.getExecutePlanNum();//计划数(数据库)

            executeStartDateDB = cltProjectCount.getExecuteStartDate();//计划开始时间(数据库)

            executeEndDateDB = cltProjectCount.getExecuteEndDate();//计划结束时间(数据库)
        }


        if (executePlanNumUI == null) {  //1.如果前端没有传递

            if (executePlanNumDB == null) {//1).如果后台不存在数据

                ProgressReportDTO testExecuteDataByPie = getTestExecuteDataByPie(progressReportQuery.getProId(), null, null, null);


                return testExecuteDataByPie;

            } else {  //2)如果后台不为空

                ProgressReportDTO testExecuteDataByPie = getTestExecuteDataByPie(progressReportQuery.getProId(), executePlanNumDB, executeStartDateDB, executeEndDateDB);

                return testExecuteDataByPie;

            }

        } else {

            updateCltProjectCountInfoByProId(progressReportQuery, "E"); //更新前端数据到数据库

            ProgressReportDTO testExecuteDataByPie = getTestExecuteDataByPie(progressReportQuery.getProId(), executePlanNumUI, executeStartDateUI, executeEndDateUI);

            return testExecuteDataByPie;

        }

    }


    //用例执行阶段进度报告（单轮）
    @Override
    public ProgressReportDTO executeSingleRoundReport(ProgressReportQuery progressReportQuery) throws Exception {

        //funcCode:1.需求分析,2.用例测试.3.用例执行阶段
        String funcCode = "3";

        //判断前端是否传递计划数,和计划开始时间和结束时间
        // Integer executePlanNumUI=progressReportQuery.getExecutePlanNum();//计划数(前端)

       /* Date executeStartDateUI=progressReportQuery.getStartDate();//计划开始时间(前端)

        Date executeEndDateUI=progressReportQuery.getEndDate();//计划结束时间(前端)*/

        //查询数据库是否存在这三个数
        // CltProjectCount cltProjectCount = queryCltProjectCountByProId(progressReportQuery.getProId(), executePlanNumUI, executeStartDateUI, executeEndDateUI,funcCode);

       /* Integer executePlanNumDB=null;

        Date executeStartDateDB=null;

        Date executeEndDateDB=null;

        if(cltProjectCount!=null){

            executePlanNumDB=cltProjectCount.getExecutePlanNum();//计划数(数据库)

            executeStartDateDB=cltProjectCount.getExecuteStartDate();//计划开始时间(数据库)

            executeEndDateDB=cltProjectCount.getExecuteEndDate();//计划结束时间(数据库)
        }*/


        //    if(executePlanNumUI==null || executePlanNumUI==0 ){  //1.如果前端没有传递

        //  if(executePlanNumDB==null ||executeStartDateDB==null || executeEndDateDB==null){//1).如果后台不存在数据

        //   return null;//返回空值

        //  throw new GlobalException(ResultCode.EXCEPTION.code(),"该轮次没有用例数!");
            /*}else{  //2)如果后台不为空

                progressReportQuery.setExecutePlanNum(executePlanNumDB);
                progressReportQuery.setExecuteStartDate(executeStartDateDB);
                progressReportQuery.setExecuteEndDate(executeEndDateDB);
                ProgressReportDTO progressReportDTO = queryProgressReportDataInfo(progressReportQuery,funcCode);

                return progressReportDTO;

            }*/

        //   }else{

        //updateCltProjectCountInfoByProId(progressReportQuery,"E"); //更新前端数据到数据库

        if (StringUtils.isEmpty(progressReportQuery.getTypeId())) {
            throw new GlobalException(ResultCode.EXCEPTION.code(), "暂无轮次");
        }


        ProgressReportDTO progressReportDTO = queryProgressReportDataInfo(progressReportQuery, funcCode);

        return progressReportDTO;

        //  }

    }


    //用例执行阶段进度报告（总体）
    @Override
    public ProgressReportDTO executeTotalReport(ProgressReportQuery progressReportQuery) throws Exception {

        //funcCode:1.需求分析,2.用例测试.3.用例执行阶段
        String funcCode = "4";

        //判断前端是否传递计划数,和计划开始时间和结束时间
        Integer executePlanNumUI = progressReportQuery.getExecutePlanNum();//计划数(前端)

        Date executeStartDateUI = progressReportQuery.getStartDate();//计划开始时间(前端)

        Date executeEndDateUI = progressReportQuery.getEndDate();//计划结束时间(前端)

        //查询数据库是否存在这三个数
        CltProjectCount cltProjectCount = queryCltProjectCountByProId(progressReportQuery.getProId(), executePlanNumUI, executeStartDateUI, executeEndDateUI, funcCode);

        Integer executePlanNumDB = null;

        Date executeStartDateDB = null;

        Date executeEndDateDB = null;

        if (cltProjectCount != null) {

            executePlanNumDB = cltProjectCount.getExecutePlanNum();//计划数(数据库)

            executeStartDateDB = cltProjectCount.getExecuteStartDate();//计划开始时间(数据库)

            executeEndDateDB = cltProjectCount.getExecuteEndDate();//计划结束时间(数据库)
        }


        if (executePlanNumUI == null || executeStartDateUI == null || executeEndDateUI == null) {  //1.如果前端没有传递

            if (executePlanNumDB == null || executeStartDateDB == null || executeEndDateDB == null) {//1).如果后台不存在数据

                return null;//返回空值

            } else {  //2)如果后台不为空

                progressReportQuery.setExecutePlanNum(executePlanNumDB);
                progressReportQuery.setStartDate(executeStartDateDB);
                progressReportQuery.setEndDate(executeEndDateDB);
                ProgressReportDTO progressReportDTO = queryProgressReportDataInfo(progressReportQuery, funcCode);

                return progressReportDTO;

            }

        } else {

            updateCltProjectCountInfoByProId(progressReportQuery, "E"); //更新前端数据到数据库

            ProgressReportDTO progressReportDTO = queryProgressReportDataInfo(progressReportQuery, funcCode);

            return progressReportDTO;

        }

    }

    /**
     * 获取项目总体进度值
     *
     * @param progressReportQuery
     * @return
     * @throws Exception
     */
    @Override
    public Float getProjectProgress(ProgressReportQuery progressReportQuery) throws Exception {

        double analysisProgree = 0;
        double designProgree = 0;
        double executeProgree = 0;

        //根据项目ID查询需求分析,用例设计,用例执行对应的计划数量
        Integer demandPlanNum = null;
        Integer testPlanNum = null;
        Integer executePlanNum = null;
        CltProjectCountExample cltProjectCountExample = new CltProjectCountExample();

        CltProjectCountExample.Criteria criteria = cltProjectCountExample.createCriteria();

        criteria.andProIdEqualTo(progressReportQuery.getProId());

        List<CltProjectCount> cltProjectCounts = cltProjectCountMapper.selectByExample(cltProjectCountExample);

        CltProjectCount cltProjectCount = null;
        if (cltProjectCounts != null && cltProjectCounts.size() > 0) {

            cltProjectCount = cltProjectCounts.get(0);

        }
        if (cltProjectCount != null) {

            demandPlanNum = cltProjectCount.getDemandPlanNum();
            testPlanNum = cltProjectCount.getTestPlanNum();
            executePlanNum = cltProjectCount.getExecutePlanNum();
            if (demandPlanNum != null) {
                //查询需求进度
                ProgressReportQuery reportQueryByDemand = new ProgressReportQuery();
                reportQueryByDemand.setProId(progressReportQuery.getProId());
                reportQueryByDemand.setDemandPlanNum(demandPlanNum);
                CltStsProgress cltStsProgressListByDemand = cltStsProgressMapper.queryAnalyseReportDataGrossLastDay(reportQueryByDemand);
                if (cltStsProgressListByDemand != null) {
                    Map<String, Object> map1 = MapperUtils.json2map(cltStsProgressListByDemand.getResult());
                    analysisProgree = BigDecimalUtil.div(Integer.parseInt(map1.get("n10").toString()), demandPlanNum, 4);
                }

            } else {
                analysisProgree = 0;
            }
            if (testPlanNum != null) {
                //查询用例设计阶段进度
                ProgressReportQuery reportQueryByTest = new ProgressReportQuery();
                reportQueryByTest.setProId(progressReportQuery.getProId());
                reportQueryByTest.setTestPlanNum(testPlanNum);
                CltStsProgress cltStsProgressListByTest = cltStsProgressMapper.queryTestCaseReportDataGrossLastDay(reportQueryByTest);
                if (cltStsProgressListByTest != null) {
                    Map<String, Object> map2 = MapperUtils.json2map(cltStsProgressListByTest.getResult());
                    designProgree = BigDecimalUtil.div(Integer.parseInt(map2.get("ss").toString()), testPlanNum, 4);
                }
            } else {
                designProgree = 0;
            }

            if (executePlanNum != null) {
                //查询用例执行阶段进度
                ProgressReportQuery reportQueryByExecute = new ProgressReportQuery();
                reportQueryByExecute.setProId(progressReportQuery.getProId());
                reportQueryByExecute.setExecutePlanNum(testPlanNum);
                CltStsProgress cltStsProgressListByExecute = cltStsProgressMapper.queryExecuteReportDataTotalLastDay(reportQueryByExecute);
                if (cltStsProgressListByExecute != null) {
                    Map<String, Object> map3 = MapperUtils.json2map(cltStsProgressListByExecute.getResult());
                    executeProgree = BigDecimalUtil.div(Integer.parseInt(map3.get("total").toString()), executePlanNum, 4);
                }
            }


            float result = (float) (BigDecimalUtil.mul(analysisProgree, 0.22) + BigDecimalUtil.mul(designProgree, 0.25) + BigDecimalUtil.mul(executeProgree, 0.53));

            //如果进度大于1，则设为1
            if (result > 1) {
                result = 1;
            }

            return result;
        } else {
            logger.error("ProjectCount no find");
            return 0F;
        }

    }


    //项目的总体进度报告图表
    @Override
    public ProgressReportDTO projectProgressReportByTotal(ProgressReportQuery progressReportQuery) throws Exception {
        //根据项目ID查询需求分析,用例设计,用例执行对应的计划数量


        Integer demandPlanNum = null;
        Integer testPlanNum = null;
        Integer executePlanNum = null;
        CltProjectCountExample cltProjectCountExample = new CltProjectCountExample();

        CltProjectCountExample.Criteria criteria = cltProjectCountExample.createCriteria();

        criteria.andProIdEqualTo(progressReportQuery.getProId());

        List<CltProjectCount> cltProjectCounts = cltProjectCountMapper.selectByExample(cltProjectCountExample);

        CltProjectCount cltProjectCount = null;
        if (cltProjectCounts != null && cltProjectCounts.size() > 0) {

            cltProjectCount = cltProjectCounts.get(0);

        }
        if (cltProjectCount != null) {

            demandPlanNum = cltProjectCount.getDemandPlanNum();
            testPlanNum = cltProjectCount.getTestPlanNum();
            executePlanNum = cltProjectCount.getExecutePlanNum();

            if (demandPlanNum == null || testPlanNum == null || executePlanNum == null) {
                return null;
            } else {

                //查询需求进度

                ProgressReportQuery reportQueryByDemand = new ProgressReportQuery();
                reportQueryByDemand.setProId(progressReportQuery.getProId());
                reportQueryByDemand.setDemandPlanNum(demandPlanNum);
                reportQueryByDemand.setDemandStartDate(progressReportQuery.getProjectStartDate());
                reportQueryByDemand.setDemandEndDate(progressReportQuery.getProjectEndDate());
                List<CltStsProgress> cltStsProgressListByDemand = cltStsProgressMapper.queryAnalyseReportDataGross(reportQueryByDemand);

                //查询用例设计阶段进度
                ProgressReportQuery reportQueryByTest = new ProgressReportQuery();
                reportQueryByTest.setProId(progressReportQuery.getProId());
                reportQueryByTest.setTestPlanNum(testPlanNum);
                reportQueryByTest.setTestStartDate(progressReportQuery.getProjectStartDate());
                reportQueryByTest.setTestEndDate(progressReportQuery.getProjectEndDate());
                List<CltStsProgress> cltStsProgressListByTest = cltStsProgressMapper.queryTestCaseReportDataGross(reportQueryByTest);

                //查询用例执行阶段进度
                ProgressReportQuery reportQueryByExecute = new ProgressReportQuery();
                reportQueryByExecute.setProId(progressReportQuery.getProId());
                reportQueryByExecute.setExecutePlanNum(testPlanNum);
                reportQueryByExecute.setStartDate(progressReportQuery.getProjectStartDate());
                reportQueryByExecute.setEndDate(progressReportQuery.getProjectEndDate());
                List<CltStsProgress> cltStsProgressListByExecute = cltStsProgressMapper.queryExecuteReportDataTotal(reportQueryByExecute);


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                String start = sdf.format(getDate(progressReportQuery.getProjectStartDate()));

                String end = sdf.format(progressReportQuery.getProjectEndDate());

                //遍历输入的开始时间和结束时间
                List<String> xAxis = collectLocalDates(LocalDate.parse(start), LocalDate.parse(end));//时间轴

                Map<String, String> demandMap = new HashMap<>();
                ProjectProgressReportData projectProgressReportDemandData = new ProjectProgressReportData();

                List<String> demandDatas = new ArrayList<>();

                for (int i = 0; i < cltStsProgressListByDemand.size(); i++) {
                    CltStsProgress cltStsProgressDemand = cltStsProgressListByDemand.get(i);

                    demandMap.put(cltStsProgressDemand.getStsData(), cltStsProgressDemand.getResult());

                }

                /*for (int i = 0; i < xAxis.size(); i++) {
                    String s =  xAxis.get(i);

                    if(demandMap.containsKey(s)){
                        demandDatas.add(demandMap.get(s));

                    }else{
                        demandDatas.add("");
                    }

                }

*/
                getResultData(xAxis, demandMap, demandDatas);


                projectProgressReportDemandData.setName("需求分析阶段");
                projectProgressReportDemandData.setyAxis(demandDatas);
                projectProgressReportDemandData.setPlanNum(demandPlanNum);


                Map<String, String> TestMap = new HashMap<>();
                ProjectProgressReportData ProjectProgressReportTestData = new ProjectProgressReportData();

                List<String> testDatas = new ArrayList<>();

                for (int i = 0; i < cltStsProgressListByTest.size(); i++) {
                    CltStsProgress cltStsProgressTest = cltStsProgressListByTest.get(i);

                    TestMap.put(cltStsProgressTest.getStsData(), cltStsProgressTest.getResult());

                }

                getResultData(xAxis, TestMap, testDatas);


               /* for (int i = 0; i < xAxis.size(); i++) {
                    String s =  xAxis.get(i);

                    if(TestMap.containsKey(s)){
                        testDatas.add(TestMap.get(s));

                    }else{
                        testDatas.add("");





                    }

                }*/

                ProjectProgressReportTestData.setName("用例设计阶段");
                ProjectProgressReportTestData.setyAxis(testDatas);
                ProjectProgressReportTestData.setPlanNum(testPlanNum);


                Map<String, String> ExecuteMap = new HashMap<>();
                ProjectProgressReportData ProjectProgressReportExecuteData = new ProjectProgressReportData();

                List<String> executeDatas = new ArrayList<>();

                for (int i = 0; i < cltStsProgressListByExecute.size(); i++) {
                    CltStsProgress cltStsProgressExecute = cltStsProgressListByExecute.get(i);

                    ExecuteMap.put(cltStsProgressExecute.getStsData(), cltStsProgressExecute.getResult());

                }

               /* for (int i = 0; i < xAxis.size(); i++) {
                    String s =  xAxis.get(i);

                    if(ExecuteMap.containsKey(s)){
                        executeDatas.add(ExecuteMap.get(s));

                    }else{
                        executeDatas.add("");
                    }

                }*/

                getResultData(xAxis, ExecuteMap, executeDatas);

                ProjectProgressReportExecuteData.setName("用例执行阶段");
                ProjectProgressReportExecuteData.setyAxis(executeDatas);
                ProjectProgressReportExecuteData.setPlanNum(executePlanNum);


                List<ProjectProgressReportData> projectProgressReportData = new ArrayList<>();

                projectProgressReportData.add(projectProgressReportDemandData);
                projectProgressReportData.add(ProjectProgressReportTestData);
                projectProgressReportData.add(ProjectProgressReportExecuteData);


                ProgressReportDTO progressReportDTO = new ProgressReportDTO();

                progressReportDTO.setxAxis(xAxis);

                progressReportDTO.setProjectData(projectProgressReportData);

                progressReportDTO.setStartDate(progressReportQuery.getProjectStartDate());

                progressReportDTO.setEndDate(progressReportQuery.getProjectEndDate());

                return progressReportDTO;


            }


        } else {

            return null;
        }

    }


//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    /**
     * 查询项目统计表
     *
     * @param proId
     * @param planNum
     * @param startDate
     * @param endDate
     * @param funCode
     * @return
     * @throws Exception
     */
    public CltProjectCount queryCltProjectCountByProId(String proId, Integer planNum, Date startDate, Date endDate, String funCode) throws Exception {


        try {

            CltProjectCountExample cltProjectCountExample = new CltProjectCountExample();

            CltProjectCountExample.Criteria criteria = cltProjectCountExample.createCriteria();

            criteria.andProIdEqualTo(proId);

            List<CltProjectCount> cltProjectCounts = cltProjectCountMapper.selectByExample(cltProjectCountExample);

            if (cltProjectCounts != null && cltProjectCounts.size() > 0) {

                CltProjectCount cltProjectCount = cltProjectCounts.get(0);

                return cltProjectCount;
            } else {

                if ("1".equals(funCode)) {

                    CltProjectCount projectCount = new CltProjectCount();

                    projectCount.setProId(proId);

                    if (planNum != null) {

                        projectCount.setDemandPlanNum(planNum);
                    }
                    if (startDate != null) {

                        projectCount.setDemandStartDate(startDate);
                    }
                    if (endDate != null) {

                        projectCount.setDemandEndDate(endDate);
                    }
                    cltProjectCountMapper.insertSelective(projectCount);

                    return projectCount;


                } else if ("2".equals(funCode)) {

                    CltProjectCount projectCount = new CltProjectCount();

                    projectCount.setProId(proId);

                    if (planNum != null) {

                        projectCount.setTestPlanNum(planNum);
                    }
                    if (startDate != null) {

                        projectCount.setTestStartDate(startDate);
                    }
                    if (endDate != null) {

                        projectCount.setTestEndDate(endDate);
                    }
                    cltProjectCountMapper.insertSelective(projectCount);

                    return projectCount;

                } else {

                    CltProjectCount projectCount = new CltProjectCount();

                    projectCount.setProId(proId);

                    if (planNum != null) {

                        projectCount.setExecutePlanNum(planNum);
                    }
                    if (startDate != null) {

                        projectCount.setExecuteStartDate(startDate);
                    }
                    if (endDate != null) {

                        projectCount.setExecuteEndDate(endDate);
                    }
                    cltProjectCountMapper.insertSelective(projectCount);

                    return projectCount;

                }
            }

        } catch (Exception e) {

            logger.info("queryCltProjectCountByProId", e);
            return null;

        }

    }


    /**
     * 查询进度统计表信息
     *
     * @param progressReportQuery
     * @param funcode
     * @return
     */
    public ProgressReportDTO queryProgressReportDataInfo(ProgressReportQuery progressReportQuery, String funcode) {

        ProgressReportDTO progressReportDTO = null;

        List<CltStsProgress> cltStsProgresses = null;

        try {

            if ("1".equals(funcode)) {

                cltStsProgresses = cltStsProgressMapper.queryAnalyseReportDataGross(progressReportQuery);

                progressReportDTO = getAnalyseReportDTO(cltStsProgresses, progressReportQuery.getDemandPlanNum(), progressReportQuery.getDemandStartDate(), progressReportQuery.getDemandEndDate());
            }

            if ("2".equals(funcode)) {

                cltStsProgresses = cltStsProgressMapper.queryTestCaseReportDataGross(progressReportQuery);

                progressReportDTO = getAnalyseReportDTO(cltStsProgresses, progressReportQuery.getTestPlanNum(), progressReportQuery.getTestStartDate(), progressReportQuery.getTestEndDate());

            }
            if ("3".equals(funcode)) {

                cltStsProgresses = cltStsProgressMapper.queryExecuteReportDataSingleRound(progressReportQuery);

                progressReportDTO = getAnalyseReportDTO(cltStsProgresses, progressReportQuery.getExecutePlanNum(), progressReportQuery.getStartDate(), progressReportQuery.getEndDate());

                progressReportDTO.setRoundId(progressReportQuery.getTypeId());
            }
            if ("4".equals(funcode)) {

                cltStsProgresses = cltStsProgressMapper.queryExecuteReportDataTotal(progressReportQuery);

                progressReportDTO = getAnalyseReportDTO(cltStsProgresses, progressReportQuery.getExecutePlanNum(), progressReportQuery.getStartDate(), progressReportQuery.getEndDate());


            }


        } catch (Exception e) {

            logger.info("queryAnalyseReportDataInfo", e);

            throw new GlobalException(ErrorConstants.QUERY_CLT_STSPROGRESS_ERROR_CODE, ErrorConstants.QUERY_CLT_STSPROGRESS_ERROR_MESSAGE);

        }

        return progressReportDTO;

    }


    //更新计划数量
    public void updateCltProjectCountInfoByProId(ProgressReportQuery progressReportQuery, String funcCode) throws Exception {

        try {

            CltProjectCount cltProjectCount = new CltProjectCount();
            cltProjectCount.setProId(progressReportQuery.getProId());

            if ("D".equals(funcCode)) {
                cltProjectCount.setDemandPlanNum(progressReportQuery.getDemandPlanNum());
                cltProjectCount.setDemandStartDate(progressReportQuery.getStartDate());
                cltProjectCount.setDemandEndDate(progressReportQuery.getEndDate());
            }
            if ("T".equals(funcCode)) {
                cltProjectCount.setTestPlanNum(progressReportQuery.getTestPlanNum());
                cltProjectCount.setTestStartDate(progressReportQuery.getStartDate());
                cltProjectCount.setTestEndDate(progressReportQuery.getEndDate());

            }

            if ("E".equals(funcCode)) {
                cltProjectCount.setExecutePlanNum(progressReportQuery.getExecutePlanNum());
                cltProjectCount.setExecuteStartDate(progressReportQuery.getStartDate());
                cltProjectCount.setExecuteEndDate(progressReportQuery.getEndDate());
            }

            cltProjectCountMapper.updateByPrimaryKeySelective(cltProjectCount);


        } catch (Exception e) {


            logger.info("updateCltProjectCountInfoByProId", e);

        }

    }


    //组装返回的数据
    public ProgressReportDTO getAnalyseReportDTO(List<CltStsProgress> cltStsProgresses, Integer planNum, Date startDate, Date endDate) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        startDate = getDate(startDate);
        String start = sdf.format(startDate);

        String end = sdf.format(endDate);

        //遍历输入的开始时间和结束时间
        List<String> xAxis = collectLocalDates(LocalDate.parse(start), LocalDate.parse(end));//时间轴

        Map<String, String> reportMap = new HashMap();

        List<String> yAxis = new ArrayList<>();


        for (int i = 0; i < cltStsProgresses.size(); i++) {

            CltStsProgress cltStsProgress = cltStsProgresses.get(i);

            reportMap.put(cltStsProgress.getStsData(), cltStsProgress.getResult());

        }

        /*for (int i = 0; i < xAxis.size(); i++) {
            String s =  xAxis.get(i);
            if(reportMap.containsKey(s)){
                yAxis.add(reportMap.get(s));
            }else{
                yAxis.add("");
            }
        }*/
        getResultData(xAxis, reportMap, yAxis);


        ProgressReportDTO progressReportDTO = new ProgressReportDTO();

        progressReportDTO.setxAxis(xAxis);

        progressReportDTO.setyAxis(yAxis);

        progressReportDTO.setPlanNum(planNum);

        progressReportDTO.setStartDate(startDate);

        progressReportDTO.setEndDate(endDate);

        return progressReportDTO;

    }

    @Autowired
    DemandFeignClient demandFeignClient;

    //查询分析中、待评审、待修改、完成这四种状态的测试点在需求分析计划中的数量
    public ProgressReportDTO queryCltMapNodeListByProId(String proId, Integer planNum, Date startDate, Date endDate) throws Exception {

        List<MapNodeNumDTO> cltMapNodeList = null;

        Integer totalTestNum = 0;//总测试点数量

        Integer completedNum = 0;

        ProgressReportDTO progressReportDTO = new ProgressReportDTO();

        if (startDate != null) {

            progressReportDTO.setStartDate(startDate);
        }

        if (endDate != null) {

            progressReportDTO.setEndDate(endDate);
        }

        if (planNum != null) {

            progressReportDTO.setPlanNum(planNum);

        }


        try {

            //调用需求微服务查询MapNode
            DemandQuery demandQuery = new DemandQuery();
            demandQuery.setProjectId(proId);
            cltMapNodeList = demandFeignClient.queryCltMapNodeListByProId(demandQuery);


            if (cltMapNodeList != null && cltMapNodeList.size() > 0) {

                for (int i = 0; i < cltMapNodeList.size(); i++) {
                    MapNodeNumDTO mapNodeNumDTO = cltMapNodeList.get(i);
                    totalTestNum += mapNodeNumDTO.getTestNum();
                    if ("10".equals(mapNodeNumDTO.getStatus())) {
                        completedNum = mapNodeNumDTO.getTestNum();

                    }


                }
                progressReportDTO.setTotalTestNum(totalTestNum);

                if (planNum != null) {

                    double calcuteNum = calcuteNum(planNum, completedNum);//百分比

                    progressReportDTO.setSchedule(calcuteNum);


                    if (planNum > totalTestNum) {//如果计划数大于总测试点数,返回未完成数

                        Integer notAnanlyseNum = planNum - totalTestNum;

                        MapNodeNumDTO mapNodeNumDTO = new MapNodeNumDTO();

                        mapNodeNumDTO.setTestNum(notAnanlyseNum);

                        mapNodeNumDTO.setStatus("12");

                        cltMapNodeList.add(mapNodeNumDTO);
                    }

                }

                progressReportDTO.setMapNodeNumDTOS(cltMapNodeList);
            } else {
                if (planNum != null) {
                    MapNodeNumDTO mapNodeNumDTO = new MapNodeNumDTO();
                    mapNodeNumDTO.setTestNum(planNum);
                    mapNodeNumDTO.setStatus("12");
                    cltMapNodeList.add(mapNodeNumDTO);
                    progressReportDTO.setMapNodeNumDTOS(cltMapNodeList);
                }


            }
        } catch (Exception e) {

            logger.info("queryDemandByProId", e);

        }

        return progressReportDTO;


    }


    //查询未设计、设计中、已完成的数量
    public ProgressReportDTO queryTestCaseNumByProId(String proId, Integer planNum, Date startDate, Date endDate) throws Exception {

        List<MapNodeNumDTO> cltMapNodeList = null;

        Integer totalNodeNum = 0;//总测试点数量


        Integer completeNum = 0;//已完成的用例设计数

        //初始化的数量
        Integer initNum = 0;
        ProgressReportDTO progressReportDTO = new ProgressReportDTO();
        if (planNum != null) {
            progressReportDTO.setPlanNum(planNum);
        }
        if (startDate != null) {
            progressReportDTO.setStartDate(startDate);
        }
        if (endDate != null) {
            progressReportDTO.setEndDate(endDate);
        }


        try {

            HashMap<String, String> map = new HashMap<>();
            map.put("projectId", proId);

            //feign调用 查询各种状态的测试用例数量
            cltMapNodeList = caseAppBizParamFeignClient.queryTestCaseNumByProjectId(map).getData();
            if (cltMapNodeList != null && cltMapNodeList.size() > 0) {

                for (int i = 0; i < cltMapNodeList.size(); i++) {

                    MapNodeNumDTO mapNodeNumDTO = cltMapNodeList.get(i);

                    if ("0".equals(mapNodeNumDTO.getStatus())) {

                        //状态改为D 前端用D接收设计中
                        mapNodeNumDTO.setStatus("D");
                        initNum = mapNodeNumDTO.getTestNum();

                    }

                    if ("1".equals(mapNodeNumDTO.getStatus())) {
                        //状态改为S 前端用D接收已完成
                        mapNodeNumDTO.setStatus("S");
                        completeNum = mapNodeNumDTO.getTestNum();
                    }
                    totalNodeNum += mapNodeNumDTO.getTestNum();
                }

                progressReportDTO.setTotalTestNum(totalNodeNum);

                if (planNum != null) {

                    double calcuteNum = calcuteNum(planNum, completeNum);//百分比

                    progressReportDTO.setSchedule(calcuteNum);

                    if (planNum > totalNodeNum) { //如果计划数大于总测试点数,返回未完成数

                        Integer notAnanlyseNum = planNum - completeNum - initNum;

                        MapNodeNumDTO mapNodeNumDTO = new MapNodeNumDTO();

                        mapNodeNumDTO.setTestNum(notAnanlyseNum);

                        mapNodeNumDTO.setStatus("N");

                        cltMapNodeList.add(mapNodeNumDTO);
                    } else {


                        //如果计划数小于总测试点数,则分析中的用例数量就是未完成的
                        Integer notAnanlyseNum = initNum;

                        MapNodeNumDTO mapNodeNumDTO = new MapNodeNumDTO();

                        mapNodeNumDTO.setTestNum(notAnanlyseNum);

                        mapNodeNumDTO.setStatus("N");

                        cltMapNodeList.add(mapNodeNumDTO);

                    }

                }
                progressReportDTO.setMapNodeNumDTOS(cltMapNodeList);
            } else {
                if (planNum != null) {

                    MapNodeNumDTO mapNodeNumDTO = new MapNodeNumDTO();

                    mapNodeNumDTO.setTestNum(planNum);

                    mapNodeNumDTO.setStatus("N");

                    cltMapNodeList.add(mapNodeNumDTO);

                    progressReportDTO.setMapNodeNumDTOS(cltMapNodeList);

                }


            }

        } catch (Exception e) {

            logger.info("queryDemandByProId", e);

        }

        return progressReportDTO;


    }


    /**
     * 用例执行阶段进度报告(饼图)
     *
     * @param proId
     * @param planNum
     * @param startDate
     * @param endDate
     * @return
     */
    public ProgressReportDTO getTestExecuteDataByPie(String proId, Integer planNum, Date startDate, Date endDate) {


        ProgressReportDTO progressReportDTO = new ProgressReportDTO();

        if (planNum != null) {
            progressReportDTO.setPlanNum(planNum);
        }

        if (startDate != null) {
            progressReportDTO.setStartDate(startDate);
        }

        if (endDate != null) {

            progressReportDTO.setEndDate(endDate);
        }


        try {

            //通过proId查询用例执行
            QualityReportQuery query = new QualityReportQuery();
            query.setProId(proId);

            TestCaseExecuteReportDTO testCaseExecuteReportDTO = manualExecFeignClient.queryCaseExecByProId(query).getData().get(0);

            List<MapNodeNumDTO> mapNodeNumDTOS = new ArrayList<>();

            if (testCaseExecuteReportDTO != null) {

                MapNodeNumDTO mapNodeNumDTO0 = new MapNodeNumDTO();
                MapNodeNumDTO mapNodeNumDTO1 = new MapNodeNumDTO();
                MapNodeNumDTO mapNodeNumDTO2 = new MapNodeNumDTO();
                MapNodeNumDTO mapNodeNumDTO3 = new MapNodeNumDTO();
                MapNodeNumDTO mapNodeNumDTO4 = new MapNodeNumDTO();
                MapNodeNumDTO mapNodeNumDTO5 = new MapNodeNumDTO();
                MapNodeNumDTO mapNodeNumDTO6 = new MapNodeNumDTO();

                mapNodeNumDTO0.setStatus("0");
                mapNodeNumDTO0.setTestNum(testCaseExecuteReportDTO.getExecute0());

                mapNodeNumDTO1.setStatus("1");
                mapNodeNumDTO1.setTestNum(testCaseExecuteReportDTO.getExecute1());

                mapNodeNumDTO2.setStatus("2");
                mapNodeNumDTO2.setTestNum(testCaseExecuteReportDTO.getExecute2());

                mapNodeNumDTO3.setStatus("3");
                mapNodeNumDTO3.setTestNum(testCaseExecuteReportDTO.getExecute3());

                mapNodeNumDTO4.setStatus("4");
                mapNodeNumDTO4.setTestNum(testCaseExecuteReportDTO.getExecute4());

                mapNodeNumDTO5.setStatus("5");
                mapNodeNumDTO5.setTestNum(testCaseExecuteReportDTO.getExecute5());

                mapNodeNumDTO6.setStatus("6");
                mapNodeNumDTO6.setTestNum(testCaseExecuteReportDTO.getExecute6());

                mapNodeNumDTOS.add(mapNodeNumDTO0);

                mapNodeNumDTOS.add(mapNodeNumDTO1);

                mapNodeNumDTOS.add(mapNodeNumDTO2);

                mapNodeNumDTOS.add(mapNodeNumDTO3);

                mapNodeNumDTOS.add(mapNodeNumDTO4);

                mapNodeNumDTOS.add(mapNodeNumDTO5);

                mapNodeNumDTOS.add(mapNodeNumDTO6);
                Integer completeTotal = testCaseExecuteReportDTO.getExecute1() + testCaseExecuteReportDTO.getExecute2() + testCaseExecuteReportDTO.getExecute6();
                progressReportDTO.setMapNodeNumDTOS(mapNodeNumDTOS);
                progressReportDTO.setTotalTestNum(completeTotal);


                if (planNum != null) {

                    double calcuteNum = calcuteNum(planNum, completeTotal);//百分比

                    progressReportDTO.setSchedule(calcuteNum);

                }
            } else {

                if (planNum != null) {
                    MapNodeNumDTO mapNodeNumDTO = new MapNodeNumDTO();
                    mapNodeNumDTO.setStatus("5");
                    mapNodeNumDTO.setTestNum(planNum);
                    mapNodeNumDTOS.add(mapNodeNumDTO);
                    progressReportDTO.setMapNodeNumDTOS(mapNodeNumDTOS);

                }

            }


        } catch (Exception e) {

            logger.info("getTestExecuteDataByPie");
        }


        return progressReportDTO;

    }


    public List<String> collectLocalDates(LocalDate start, LocalDate end) {
        // 用起始时间作为流的源头，按照每次加一天的方式创建一个无限流
        return Stream.iterate(start, localDate -> localDate.plusDays(1))
                // 截断无限流，长度为起始时间和结束时间的差+1个
                .limit(ChronoUnit.DAYS.between(start, end) + 1)
                // 由于最后要的是字符串，所以map转换一下
                .map(LocalDate::toString)
                // 把流收集为List
                .collect(Collectors.toList());
    }


    //获取前几天的日期
    public Date getDate(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();

        return date;
    }


    //计算百分比
    private double calcuteNum(int plan, int total) {
        BigDecimal p = new BigDecimal(plan);
        BigDecimal t = new BigDecimal(total);
        double decimal = t.divide(p, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).doubleValue();
        return decimal;
    }


    //获取日期对应的result
    private List<String> getResultData(List<String> timeList, Map<String, String> map, List<String> results) throws Exception {

        String s = "";
        for (int i = 0; i < timeList.size(); i++) {
            String time = timeList.get(i);
            if (i == 0) {
                if (map.containsKey(time)) {
                    results.add(map.get(time));
                    s = map.get(time);
                } else {
                    results.add(s);
                }
            } else {
                if (map.containsKey(time)) {
                    results.add(map.get(time));
                    s = map.get(time);
                } else {
                    results.add(s);
                }

            }

        }

        return results;
    }


}
