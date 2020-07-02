package net.northking.cloudtest.service.impl;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import net.northking.cloudtest.dao.analyse.CltBatchCaseSetMapper;
import net.northking.cloudtest.dao.analyse.CltBatchMapper;
import net.northking.cloudtest.dao.analyse.CltRoundMapper;
import net.northking.cloudtest.dao.analyse.DemandMapper;
import net.northking.cloudtest.dao.analyse.TestCaseMapper;
import net.northking.cloudtest.dao.attach.CltAttachMapper;
import net.northking.cloudtest.dao.cust.CltCustomerMapper;
import net.northking.cloudtest.dao.project.CltProjectCountMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.report.CltReportMapper;
import net.northking.cloudtest.dao.report.CltStsProgressMapper;
import net.northking.cloudtest.dao.task.CltTestcaseExecuteMapper;
import net.northking.cloudtest.dao.testBug.CltBugLogMapper;
import net.northking.cloudtest.dao.user.CltProjectTeamMapper;
import net.northking.cloudtest.domain.analyse.CltRound;
import net.northking.cloudtest.domain.analyse.CltRoundExample;
import net.northking.cloudtest.domain.analyse.Demand;
import net.northking.cloudtest.domain.analyse.DemandExample;
import net.northking.cloudtest.domain.analyse.TestCase;
import net.northking.cloudtest.domain.analyse.TestCaseExample;
import net.northking.cloudtest.domain.attach.CltAttach;
import net.northking.cloudtest.domain.attach.CltAttachExample;
import net.northking.cloudtest.domain.cust.CltCustomer;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.project.CltProjectCountExample;
import net.northking.cloudtest.domain.report.CltReport;
import net.northking.cloudtest.domain.report.CltReportExample;
import net.northking.cloudtest.domain.report.CltStsProgress;
import net.northking.cloudtest.domain.report.CltStsProgressExample;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.domain.testBug.CltBugLogExample;
import net.northking.cloudtest.domain.user.CltRole;
import net.northking.cloudtest.dto.CoverageReportDataWord;
import net.northking.cloudtest.dto.PorjectDatasTotal;
import net.northking.cloudtest.dto.SummaryBulletin;
import net.northking.cloudtest.dto.TestBugDimensions;
import net.northking.cloudtest.dto.TestBugPersent;
import net.northking.cloudtest.dto.analyse.CltBatchDTO;
import net.northking.cloudtest.dto.analyse.CltRoundDTO;
import net.northking.cloudtest.dto.report.TestCaseExecuteReportDTO;
import net.northking.cloudtest.dto.report.TestCasePass;
import net.northking.cloudtest.dto.report.TestTransPass;
import net.northking.cloudtest.dto.user.UserCount;
import net.northking.cloudtest.enums.CltBulletinCatalog;
import net.northking.cloudtest.enums.CltStsProgressCatalog;
import net.northking.cloudtest.feign.user.RoleFeignClient;
import net.northking.cloudtest.query.analyse.DemandQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.SummaryBulletinService;
import net.northking.cloudtest.utils.BeanUtil;
import net.northking.cloudtest.utils.CltUtils;

/**
 * @Title: 总结报告处理服务
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/11
 * @UpdateUser:
 * @Version:0.1
 */

@Service
public class SummaryBulletinServiceImpl implements SummaryBulletinService {

    private final static Logger logger = LoggerFactory.getLogger(SummaryBulletinServiceImpl.class);

    @Autowired
    private CltProjectMapper cltProjectMapper;

    @Autowired
    private CltCustomerMapper cltCustomerMapper;

    @Autowired
    private CltReportMapper cltReportMapper;

    @Autowired
    private CltAttachMapper cltAttachMapper;

    @Autowired
    private DemandMapper demandMapper;

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Autowired
    private CltBatchMapper cltBatchMapper;

    @Autowired
    private CltProjectCountMapper cltProjectCountMapper;

    @Autowired
    private CltStsProgressMapper cltStsProgressMapper;

    @Autowired
    private CltBugLogMapper cltBugLogMapper;

    @Autowired
    private CltTestcaseExecuteMapper cltTestcaseExecuteMapper;

    @Autowired
    private CltRoundMapper cltRoundMapper;

    @Autowired
    private CltBatchCaseSetMapper cltBatchCaseSetMapper;

    @Autowired
    private RoleFeignClient roleFeignClient;

    @Autowired
    private CltProjectTeamMapper cltProjectTeamMapper;

    @Value("${report.chart.prepath}")
    private String reportChartPrePath;

    @Override
    public void dealWithReport(String projectId, Map dataMap) throws Exception {
        //用于存放word数据
        SummaryBulletin summaryBulletin = new SummaryBulletin();

        //查出项目
        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);

        if(cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
            //查出客户
            CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
            summaryBulletin.setCustName(cltCustomer.getCustName());
            summaryBulletin.setProject(cltProject);

            //获取上一版本号
            CltReportExample reportExample = new CltReportExample();
            CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
            reportCriteria.andProjectIdEqualTo(projectId);
            reportCriteria.andCatalogEqualTo(CltBulletinCatalog.SM.getCode());
            reportExample.setOrderByClause("CREATE_DATE DESC");
            List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
            if(cltReportList.size()==0){
                summaryBulletin.setVersion("1.0");
            }else{
                summaryBulletin.setVersion("1." + (cltReportList.get(0).getVersion() + 1));
            }

            //报告日期
            summaryBulletin.setReportDate(new Date());

            //获取项目详情下的附件
            CltAttachExample attachExample = new CltAttachExample();
            CltAttachExample.Criteria attachCriteria = attachExample.createCriteria();
            attachCriteria.andBugIdEqualTo(projectId);
            List<CltAttach> cltAttachList = cltAttachMapper.selectByExample(attachExample);
            summaryBulletin.setCltAttachList(cltAttachList);

            //获取需求树
            List<DemandQuery> treeList = new ArrayList<>();
            try{
                List<Demand> list = demandMapper.selectByCondition(null,null, projectId,"N");
                if(list.size()>0){
                    List<DemandQuery> lst = new ArrayList<DemandQuery>();
                    for(Demand d : list){
                        DemandQuery dq = new DemandQuery();
                        BeanUtil.copyProperties(d,dq);
                        lst.add(dq);
                    }
                    treeList = getDemandTree(lst);
                }
            }catch (Exception e){
                logger.error("queryForGridLike",e);
            }
            summaryBulletin.setDemandQueryList(treeList);



            //获取项目内的用例数
            TestCaseExample testCaseExample = new TestCaseExample();
            TestCaseExample.Criteria testCaseCriteria = testCaseExample.createCriteria();
            testCaseCriteria.andProjIdEqualTo(projectId);
            testCaseCriteria.andLogicDelEqualTo("N");
            testCaseCriteria.andStatusEqualTo("S");
            int countCaseNum = testCaseMapper.countByExample(testCaseExample);

            //判断是否全量回归
            Integer isAllRegressionTest = 0;
            CltRound currRound = getCurrRound(projectId); //获取当前轮次
            if(currRound == null){
                summaryBulletin.setBackTestPercent("0%");
            }else{
                //当前轮次的执行用例数
                Integer backTestNum = cltBatchCaseSetMapper.countByRoundId(currRound.getRoundId());
                if(backTestNum != null){
                    summaryBulletin.setBackTestPercent(getPersent(backTestNum, countCaseNum));
                    if(backTestNum>=countCaseNum){
                        isAllRegressionTest=1; //全量
                    }
                }else{
                    summaryBulletin.setBackTestPercent("0%");
                }
            }
            summaryBulletin.setAllRegressionTest(isAllRegressionTest);


            //测试准出准则
            CltProjectCountExample projectCountExample = new CltProjectCountExample();
            CltProjectCountExample.Criteria projectCountCriteria = projectCountExample.createCriteria();
            projectCountCriteria.andProIdEqualTo(projectId);
            List<CltProjectCount> cltProjectCountList = cltProjectCountMapper.selectByExample(projectCountExample);
            if(cltProjectCountList.size()>0){
                summaryBulletin.setCltProjectCount(cltProjectCountList.get(0));
            }

            /*WordUtil wordUtil = new WordUtil();
            //总体进度表统计图base64
            String summaryChartPath = reportChartPrePath + projectId +"/summaryChart.png";
            if(new File(summaryChartPath).exists()) {
                dataMap.put("summaryImage", wordUtil.getImageStr(summaryChartPath));
            }

            //需求分析阶段统计图base64
            String analyseChartPath = reportChartPrePath + projectId +"/demandProgress.png";
            if(new File(analyseChartPath).exists()) {
                dataMap.put("analyseImage", wordUtil.getImageStr(analyseChartPath));
            }

            //用例设计阶段统计图base64
            String designChartPath = reportChartPrePath + projectId +"/caseDesignProgress.png";
            if(new File(designChartPath).exists()) {
                dataMap.put("designImage", wordUtil.getImageStr(designChartPath));
            }

            //执行阶段统计图base64
            String executeChartPath = reportChartPrePath + projectId +"/caseExecuteProgress.png";
            if(new File(executeChartPath).exists()) {
                dataMap.put("executeImage", wordUtil.getImageStr(executeChartPath));
            }

            //人力资源图base64
            String hrChartPath = reportChartPrePath + projectId +"/hrChart.png";
            if(new File(hrChartPath).exists()) {
                dataMap.put("hrImage", wordUtil.getImageStr(hrChartPath));
            }*/

            //测试环境
            summaryBulletin.setTestEnv(cltProject.getTestEnvi());

            //存放总计的数据
            PorjectDatasTotal porjectDatasTotal = new PorjectDatasTotal();
            porjectDatasTotal.init();

            //测试覆盖率分析
            List<Demand> demandList = demandMapper.queryDemandNumByTrade(projectId); //导图需求点
            List<TestCase> testCaseList=testCaseMapper.queryTestCaseNumByTrade(projectId);
            List<CoverageReportDataWord> coverageReportDataWordList = getCoverageReportDataWord(demandList, testCaseList, porjectDatasTotal);
            summaryBulletin.setCoverageReportDataWordList(coverageReportDataWordList);

            //案例通过率分析
            List<TestCase> testCaseModuleList=testCaseMapper.queryTestCaseNumByModule(projectId);
            List<TestCasePass> testCasePassList = getTestCasePass(testCaseModuleList, porjectDatasTotal, projectId);
            summaryBulletin.setTestCasePassList(testCasePassList);


            //交易通过率分析
            //查询有哪些模块
            List<Demand> demandModules = getModule(projectId);
            List<TestTransPass> testTransPassList = getTestTransPass(demandModules, porjectDatasTotal);
            summaryBulletin.setTestTransPassList(testTransPassList);


            //获取项目内的轮，包括批次信息
            List<CltRoundDTO> roundDtoList = new ArrayList<>();
            CltRoundExample roundExample = new CltRoundExample();
            CltRoundExample.Criteria roundCriteria = roundExample.createCriteria();
            roundCriteria.andProIdEqualTo(projectId);
            roundCriteria.andLogicDelEqualTo("N");
            roundExample.setOrderByClause("START_DATE DESC,CREATE_DATE DESC");
            List<CltRound> roundList = cltRoundMapper.selectByExample(roundExample);
            for(CltRound cltRound : roundList){
                CltRoundDTO cltRoundDTO=new CltRoundDTO();
                BeanUtil.copyProperties(cltRound,cltRoundDTO);
                //根据轮次Id查询轮次下的批次列表
                List<CltBatchDTO> cltBatchDTOS=cltBatchMapper.queryCltBatchListByRoundId(cltRound.getRoundId());
                cltRoundDTO.setCltBatchDTOS(cltBatchDTOS);

                //查询测试执行统计
                List<TestCaseExecuteReportDTO> reportDTO = cltTestcaseExecuteMapper.queryCaseExecuteReportByRoundAndModule(cltRound.getRoundId());
                cltRoundDTO.setTestCaseExecuteReportDTOS(reportDTO);

                //保存执行总计
                TestCaseExecuteReportDTO totalTestCaseExecuteReportDTO = new TestCaseExecuteReportDTO();
                totalTestCaseExecuteReportDTO.setExecute0(0);
                totalTestCaseExecuteReportDTO.setExecute1(0);
                totalTestCaseExecuteReportDTO.setExecute2(0);
                totalTestCaseExecuteReportDTO.setExecute3(0);
                totalTestCaseExecuteReportDTO.setExecute4(0);
                totalTestCaseExecuteReportDTO.setExecute5(0);
                totalTestCaseExecuteReportDTO.setExecute6(0);
                totalTestCaseExecuteReportDTO.setExeTotal(0);

                for(TestCaseExecuteReportDTO testCaseExecuteReportDTO : reportDTO){
                    totalTestCaseExecuteReportDTO.setExecute0(totalTestCaseExecuteReportDTO.getExecute0() + testCaseExecuteReportDTO.getExecute0());
                    totalTestCaseExecuteReportDTO.setExecute1(totalTestCaseExecuteReportDTO.getExecute1() + testCaseExecuteReportDTO.getExecute1());
                    totalTestCaseExecuteReportDTO.setExecute2(totalTestCaseExecuteReportDTO.getExecute2() + testCaseExecuteReportDTO.getExecute2());
                    totalTestCaseExecuteReportDTO.setExecute3(totalTestCaseExecuteReportDTO.getExecute3() + testCaseExecuteReportDTO.getExecute3());
                    totalTestCaseExecuteReportDTO.setExecute4(totalTestCaseExecuteReportDTO.getExecute4() + testCaseExecuteReportDTO.getExecute4());
                    totalTestCaseExecuteReportDTO.setExecute5(totalTestCaseExecuteReportDTO.getExecute5() + testCaseExecuteReportDTO.getExecute5());
                    totalTestCaseExecuteReportDTO.setExecute6(totalTestCaseExecuteReportDTO.getExecute6() + testCaseExecuteReportDTO.getExecute6());
                    totalTestCaseExecuteReportDTO.setExeTotal(totalTestCaseExecuteReportDTO.getExeTotal() + testCaseExecuteReportDTO.getExeTotal());
                }

                cltRoundDTO.setTotalTestCaseExecuteReportDTO(totalTestCaseExecuteReportDTO);

                TestCasePass totalTestCasePass = new TestCasePass();
                List<TestCasePass> testRoundCasePassList = getTestCasePassByRound(projectId, reportDTO, cltRound.getRoundId(), totalTestCasePass);

                cltRoundDTO.setTestCasePassList(testRoundCasePassList);
                cltRoundDTO.setTotalTestCasePass(totalTestCasePass);

                roundDtoList.add(cltRoundDTO);
            }
            summaryBulletin.setRoundDtoList(roundDtoList);

            //默认当天
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            String stsDate = sdf.format(new Date());

            //案例缺陷密度
            Map testBugCaseMap = new HashMap(); //存有效案例数,目前是判断用例，后面改为执行时用例有效状态
            CltStsProgressExample cltStsProgressCaseExample = new CltStsProgressExample();
            CltStsProgressExample.Criteria cltStsProgressCaseCriteria = cltStsProgressCaseExample.createCriteria();
            cltStsProgressCaseCriteria.andProjectIdEqualTo(projectId);
            cltStsProgressCaseCriteria.andCatalogEqualTo(CltStsProgressCatalog.CE.getCode());
            cltStsProgressCaseCriteria.andTypeEqualTo("M"); //按模块
            cltStsProgressCaseCriteria.andLevelIsNull();
            cltStsProgressCaseExample.setOrderByClause("STS_DATA DESC");
            List<CltStsProgress> cltStsProgressCaseList = cltStsProgressMapper.selectByExample(cltStsProgressCaseExample);
            int beginNum = 0;
            for(CltStsProgress cltStsProgress : cltStsProgressCaseList){
                try {
                    if(beginNum>0 && !stsDate.equals(cltStsProgress.getStsData())){
                        break;
                    }
                    stsDate = cltStsProgress.getStsData();
                    Map resultToMap = JSON.parseObject(cltStsProgress.getResult());
                    testBugCaseMap.put(cltStsProgress.getTypeName(), Integer.parseInt((String) resultToMap.get("total")));
                }catch (Exception e){
                    e.printStackTrace();
                }
                beginNum ++;
            }


            //去掉无效用例
            CltStsProgressExample cltStsProgressPassCaseExample = new CltStsProgressExample();
            CltStsProgressExample.Criteria cltStsProgressPassCaseCriteria = cltStsProgressPassCaseExample.createCriteria();
            cltStsProgressPassCaseCriteria.andProjectIdEqualTo(projectId);
            cltStsProgressPassCaseCriteria.andCatalogEqualTo(CltStsProgressCatalog.CE.getCode());
            cltStsProgressPassCaseCriteria.andTypeEqualTo("M"); //按模块
            cltStsProgressPassCaseCriteria.andLevelIsNull();
            cltStsProgressPassCaseCriteria.andStsDataEqualTo(stsDate);
            cltStsProgressPassCaseExample.setOrderByClause("STS_DATA DESC");
            List<CltStsProgress> cltStsProgressPassCaseList = cltStsProgressMapper.selectByExample(cltStsProgressPassCaseExample);
            for(CltStsProgress cltStsProgress : cltStsProgressPassCaseList){
                try {
                    Map resultToMap = JSON.parseObject(cltStsProgress.getResult());
                    if(testBugCaseMap.get(cltStsProgress.getTypeName()) != null) {
                        testBugCaseMap.put(cltStsProgress.getTypeName(), (int)testBugCaseMap.get(cltStsProgress.getTypeName()) - Integer.parseInt((String) resultToMap.get("1")));
                        porjectDatasTotal.setEffectiveCaseNumTotal(porjectDatasTotal.getEffectiveCaseNumTotal() + (int)testBugCaseMap.get(cltStsProgress.getTypeName()));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }


            Map<String, TestBugDimensions> dimensionsMap = new HashMap(); //暂存严重程度的缺陷个数，Key为模块名称

            //遗留缺陷业务及严重级分布
            CltStsProgressExample cltStsProgressDimensionsExample = new CltStsProgressExample();
            CltStsProgressExample.Criteria cltStsProgressDimensionsCriteria = cltStsProgressDimensionsExample.createCriteria();
            cltStsProgressDimensionsCriteria.andProjectIdEqualTo(projectId);
            cltStsProgressDimensionsCriteria.andCatalogEqualTo(CltStsProgressCatalog.BL.getCode());
            cltStsProgressDimensionsCriteria.andTypeEqualTo("M"); //按模块
            cltStsProgressDimensionsCriteria.andStsDataEqualTo(stsDate);
            cltStsProgressDimensionsCriteria.andLevelIsNull();
            List<CltStsProgress> cltStsProgressDimensionsList = cltStsProgressMapper.selectByExample(cltStsProgressDimensionsExample);
            List<TestBugDimensions> testBugDimensionsList = new ArrayList<>();
            for(CltStsProgress cltStsProgress : cltStsProgressDimensionsList){
                try {
                    Map resultToMap = JSON.parseObject(cltStsProgress.getResult());
                    TestBugDimensions testBugDimensions = new TestBugDimensions();
                    testBugDimensions.setTransName(cltStsProgress.getTypeName());
                    testBugDimensions.setAlValue(Integer.parseInt((String) resultToMap.get("A")));
                    testBugDimensions.setBlValue(Integer.parseInt((String) resultToMap.get("B")));
                    testBugDimensions.setClValue(Integer.parseInt((String) resultToMap.get("C")));
                    testBugDimensions.setDlValue(Integer.parseInt((String) resultToMap.get("D")));
                    testBugDimensions.setElValue(Integer.parseInt((String) resultToMap.get("E")));
                    testBugDimensions.setTotal(Integer.parseInt((String) resultToMap.get("total")));
                    testBugDimensionsList.add(testBugDimensions);

                    dimensionsMap.put(testBugDimensions.getTransName(), testBugDimensions);

                    //总计
                    porjectDatasTotal.setATotal(porjectDatasTotal.getATotal() + testBugDimensions.getAlValue());
                    porjectDatasTotal.setBTotal(porjectDatasTotal.getBTotal() + testBugDimensions.getBlValue());
                    porjectDatasTotal.setCTotal(porjectDatasTotal.getCTotal() + testBugDimensions.getClValue());
                    porjectDatasTotal.setDTotal(porjectDatasTotal.getDTotal() + testBugDimensions.getDlValue());
                    porjectDatasTotal.setETotal(porjectDatasTotal.getETotal() + testBugDimensions.getElValue());
                    porjectDatasTotal.setTotalTotal(porjectDatasTotal.getTotalTotal() + testBugDimensions.getTotal());

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            summaryBulletin.setTestBugDimensionsList(testBugDimensionsList);


            //缺陷有效率(除了10的都是有效，INVALID("10","无效"))，缺陷修复率:CLOSE("9","关闭")
            List<TestBugPersent> testBugPersentList = new ArrayList<>();
            CltStsProgressExample cltStsProgressExample = new CltStsProgressExample();
            CltStsProgressExample.Criteria cltStsProgressCriteria = cltStsProgressExample.createCriteria();
            cltStsProgressCriteria.andProjectIdEqualTo(projectId);
            cltStsProgressCriteria.andCatalogEqualTo(CltStsProgressCatalog.BS.getCode());
            cltStsProgressCriteria.andTypeEqualTo("M"); //按模块
            cltStsProgressCriteria.andStsDataEqualTo(stsDate);
            cltStsProgressCriteria.andLevelIsNull();
            List<CltStsProgress> cltStsProgressList = cltStsProgressMapper.selectByExample(cltStsProgressExample);
            for(CltStsProgress cltStsProgress : cltStsProgressList){
                try {
                    Map resultToMap = JSON.parseObject(cltStsProgress.getResult());
                    TestBugPersent testBugPersent = new TestBugPersent();
                    testBugPersent.setTradeName(cltStsProgress.getTypeName());
                    testBugPersent.setTotalNum(Integer.parseInt((String) resultToMap.get("total"))); //总缺陷数
                    testBugPersent.setEffectiveNum(testBugPersent.getTotalNum() - Integer.parseInt((String) resultToMap.get("10"))); //有效缺陷数
                    testBugPersent.setEffectivePersent(getPersent(testBugPersent.getEffectiveNum(), testBugPersent.getTotalNum())); //缺陷有效率
                    testBugPersent.setCloseNum(Integer.parseInt((String) resultToMap.get("9"))); //关闭缺陷数
                    testBugPersent.setRepairPersent(getPersent(testBugPersent.getCloseNum(), testBugPersent.getEffectiveNum()));
                    testBugPersent.setEffectiveCaseNum(testBugCaseMap.get(cltStsProgress.getTypeName())==null?0:(int)testBugCaseMap.get(cltStsProgress.getTypeName()));

                    //缺陷密度公式：系数：可以维护，但默认： 5, 3, 1, 0.5, 0.2
                    TestBugDimensions tempDimension = dimensionsMap.get(cltStsProgress.getTypeName());
                    if(null != tempDimension) {
                        testBugPersent.setTestBugCasePersent(getChu((tempDimension.getAlValue()*5 + tempDimension.getBlValue()*3 + tempDimension.getClValue()*1 + tempDimension.getDlValue()*0.5 + tempDimension.getElValue()*0.2), testBugPersent.getEffectiveCaseNum()));
                    }
                    testBugPersentList.add(testBugPersent);

                    //总计
                    porjectDatasTotal.setEffectiveNumTotal(porjectDatasTotal.getEffectiveNumTotal() + testBugPersent.getEffectiveNum());
                    porjectDatasTotal.setCloseNumTotal(porjectDatasTotal.getCloseNumTotal() + testBugPersent.getCloseNum());
                    porjectDatasTotal.setTotalNumTotal(porjectDatasTotal.getTotalNumTotal() + testBugPersent.getTotalNum());

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            //总计
            porjectDatasTotal.setEffectivePersentTotal(getPersent(porjectDatasTotal.getEffectiveNumTotal(), porjectDatasTotal.getTotalNumTotal()));

            //缺陷密度公式：系数：可以维护，但默认： 5, 3, 1, 0.5, 0.2
            porjectDatasTotal.setTestBugCasePersentTotal(getChu((porjectDatasTotal.getATotal()*5 + porjectDatasTotal.getBTotal()*3 + porjectDatasTotal.getCTotal()*1 + porjectDatasTotal.getDTotal()*0.5 + porjectDatasTotal.getETotal()*0.2), porjectDatasTotal.getEffectiveCaseNumTotal()));
            porjectDatasTotal.setRepairPersentTotal(getPersent(porjectDatasTotal.getCloseNumTotal(), porjectDatasTotal.getEffectiveNumTotal()));

            summaryBulletin.setTestBugPersentList(testBugPersentList);

            /*//缺陷趋势图base64
            String bugBTChartPath = reportChartPrePath + projectId +"/bugBTChart.png";
            if(new File(bugBTChartPath).exists()) {
                dataMap.put("bugBTImage", wordUtil.getImageStr(bugBTChartPath));
            }*/

            //存在的风险
            CltBugLogExample cltBugLogExample = new CltBugLogExample();
            CltBugLogExample.Criteria cltBugLogCriteria = cltBugLogExample.createCriteria();
            cltBugLogCriteria.andBugIdEqualTo(projectId);
            cltBugLogExample.setOrderByClause("LOG_TIME DESC");
            List<CltBugLog> cltBugLogList = cltBugLogMapper.selectByExample(cltBugLogExample);
            summaryBulletin.setCltBugLogList(cltBugLogList);




            //是否达到准出标准
            Integer allPassTestOut = 1;
            CltProjectCount currCltProjectCount = summaryBulletin.getCltProjectCount();
            if(null != currCltProjectCount) {
                try {
                    if(currCltProjectCount.getCaseAll() == 0 || currCltProjectCount.getCaseDone()==0 || currCltProjectCount.getTransAll()==0 || currCltProjectCount.getBugAll()==0){
                        allPassTestOut = 0;
                    }else {
                        //测试覆盖率是否满足准出.(执行覆盖数/用例总数)*100与测试覆盖率比较
                        if ((currCltProjectCount.getCaseCover() / currCltProjectCount.getCaseAll()) * 100 < currCltProjectCount.getTestOut()) {
                            allPassTestOut = 0;
                        } else {
                            //案例通过率是否满足准出.(通过案例数/执行案例数)*100与案例通过率比较
                            if ((currCltProjectCount.getCasePass() / currCltProjectCount.getCaseDone()) * 100 < currCltProjectCount.getCaseOut()) {
                                allPassTestOut = 0;
                            } else {
                                //交易通过率是否满足准出.(通过交易数/交易总数)*100与交易通过率比较
                                if ((currCltProjectCount.getTransPass() / currCltProjectCount.getTransAll()) * 100 < currCltProjectCount.getTransOut()) {
                                    allPassTestOut = 0;
                                } else {
                                    //是否遗留A级缺陷，当准出值填否的时候，实际值如果大于0就为否，其它情况为是
                                    if (null != currCltProjectCount.getBugaOut() && "否".equals(currCltProjectCount.getBugaOut()) && (StringUtils.isNotEmpty(currCltProjectCount.getBugaReal()) && Integer.parseInt(currCltProjectCount.getBugaReal()) > 0)) {
                                        allPassTestOut = 0;
                                    } else {
                                        //是否遗留B级缺陷，当准出值填否的时候，实际值如果大于0就为否，其它情况为是
                                        if (null != currCltProjectCount.getBugbOut() && "否".equals(currCltProjectCount.getBugbOut()) && (StringUtils.isNotEmpty(currCltProjectCount.getBugbReal()) && Integer.parseInt(currCltProjectCount.getBugbReal()) > 0)) {
                                            allPassTestOut = 0;
                                        } else {
                                            //遗留C,D,E级别缺陷率是否满足准出.(遗留一般，建议，轻微级别缺陷数/缺陷总数)*100与是否遗留C,D,E级别缺陷率比较
                                            if ((currCltProjectCount.getBugcdeRest() / currCltProjectCount.getBugAll()) * 100 < currCltProjectCount.getBugcdeOut()) {
                                                allPassTestOut = 0;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    logger.error("error:", e);
                }
            }else{
                allPassTestOut = 0;
            }
            summaryBulletin.setAllPassTestOut(allPassTestOut);

            //团队参与人员
            //总参与人员人数
            Integer allTeams = cltProjectTeamMapper.selectAllTeams(projectId);


            CltRole cltRole = new CltRole();
            cltRole.setProId(projectId);
            ResultInfo<UserCount> userCountResultInfo = roleFeignClient.selectRoleCount(cltRole);
            UserCount userCount = userCountResultInfo.getData();

            dataMap.put("allTeams", allTeams);
            dataMap.put("userCount", userCount);
            dataMap.put("porjectDatasTotal", porjectDatasTotal);
            dataMap.put("summaryBulletin", summaryBulletin);

        }
    }


    /**
     * 插入或更新报告数据
     * @param projectId
     * @param operatorUserId
     * @param docPath
     * @param swfPath
     * @return
     */
    public CltReport insertOrUpdateData(String projectId, String operatorUserId, String docPath, String swfPath) throws Exception{
        CltReport cltReport = null;
        if(operatorUserId == null) operatorUserId = "system";
        CltReportExample reportExample = new CltReportExample();
        CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
        reportCriteria.andProjectIdEqualTo(projectId);
        reportCriteria.andCatalogEqualTo(CltBulletinCatalog.SM.getCode());
        reportExample.setOrderByClause("CREATE_DATE DESC");
        List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
        if(cltReportList.size()>0){ //修改
            cltReport = cltReportList.get(0);
            cltReport.setUpdateDate(new Date());
            cltReport.setUpdateUser(operatorUserId);
            cltReport.setDocPath(docPath);
            cltReport.setSwfPath(swfPath);
            cltReport.setVersion(cltReport.getVersion() + 1);
            cltReportMapper.updateByPrimaryKeySelective(cltReport);
        }else{ //新增
            //查出项目
            CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);
            if(cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
                //查出客户
                CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
                cltReport = new CltReport();
                cltReport.setCustomId(cltCustomer.getCustId());
                cltReport.setProjectId(projectId);
                cltReport.setReportName(cltProject.getProName() + "总体测试报告");
                cltReport.setCatalog(CltBulletinCatalog.SM.getCode());
                cltReport.setReportDate(new Date());
                cltReport.setCreateUser(operatorUserId);
                cltReport.setCreateDate(new Date());
                cltReport.setDocPath(docPath);
                cltReport.setSwfPath(swfPath);
                cltReport.setVersion(0);
                cltReportMapper.insertSelective(cltReport);
            }
        }
        return cltReport;
    }


    //组装数据（树状数据）
    public static List<DemandQuery> getDemandTree(List<DemandQuery> list){
        List<DemandQuery> treeList = new ArrayList<>();
        for (int i=0;i<list.size();i++) {
            DemandQuery dem = (DemandQuery)list.get(i);
            //获取最上级节点
            if("TOP".equalsIgnoreCase(dem.getParentId())){
                Map map = CltUtils.beanToMap(dem);
                Map jsonMap = new HashMap();
                jsonMap.putAll(map);
                //包装下级
                getSonTree(jsonMap,list);
                CltUtils.mapToBean(jsonMap,dem);
                treeList.add(dem);
            }

        }
        return treeList;
    }
    //递归方法
    private static Map<String,Object> getSonTree(Map<String,Object> parentMap,List<DemandQuery> itemList){

        List<DemandQuery> sonList = new ArrayList<>();

        Map<String, Object> treeMap;
        for (DemandQuery item : itemList){
            if(org.springframework.util.StringUtils.hasText(item.getParentId())&&(parentMap.get("id").toString().equals(item.getParentId().toString()))){

                treeMap = CltUtils.beanToMap(item);

                getSonTree(treeMap,itemList);
                CltUtils.mapToBean(treeMap,item);
                sonList.add(item);

            }
        }

        parentMap.put("children", sonList);

        return parentMap;
    }

    //组装数据
    public List<CoverageReportDataWord> getCoverageReportDataWord(List<Demand> demandList, List<TestCase> testCaseList, PorjectDatasTotal porjectDatasTotal){
        List<CoverageReportDataWord> coverageReportDataWordList = new ArrayList<>();
        Map<String,Integer> testCaseMap = new HashMap<>();//存储测试用例数据

        for(TestCase testCase : testCaseList){
            testCaseMap.put(testCase.getTestItem(), testCase.getCaseNum());
        }

        int caseNumTotal = 0;
        for(Demand demand : demandList){
            CoverageReportDataWord coverageReportDataWord = new CoverageReportDataWord();
            coverageReportDataWord.setTradeName(demand.getName());
            coverageReportDataWord.setDemandNum(demand.getPointNum()==null?0:demand.getPointNum());
            coverageReportDataWord.setTestCaseNum(testCaseMap.get(demand.getName())==null?0:testCaseMap.get(demand.getName()));
            coverageReportDataWord.setCoveragePersent(getPersent(coverageReportDataWord.getTestCaseNum(), coverageReportDataWord.getDemandNum()));
            coverageReportDataWordList.add(coverageReportDataWord);
            porjectDatasTotal.setCaseNumTotal(porjectDatasTotal.getCaseNumTotal() + coverageReportDataWord.getTestCaseNum());
            porjectDatasTotal.setTransNumTotal(porjectDatasTotal.getTransNumTotal() + coverageReportDataWord.getDemandNum());
            if(coverageReportDataWord.getTestCaseNum() > coverageReportDataWord.getDemandNum()){ //如果案例数大于测试点数，算比例时就取测试点数
                caseNumTotal += coverageReportDataWord.getDemandNum();
            }else{
                caseNumTotal += coverageReportDataWord.getTestCaseNum();
            }
        }

        porjectDatasTotal.setCasePersentTotal(getPersent(caseNumTotal, porjectDatasTotal.getTransNumTotal()));
        return coverageReportDataWordList;
    }

    //组装数据
    public List<TestCasePass> getTestCasePass(List<TestCase> testCaseModuleList, PorjectDatasTotal porjectDatasTotal, String projectId){
        List<TestCasePass> testCasePassList = new ArrayList<>();

        for(TestCase testCase : testCaseModuleList){
            TestCasePass testCasePass = new TestCasePass();
            testCasePass.setTradeName(testCase.getModule());

            //按模块查询执行用例数

            testCasePass.setTestCaseNum(cltTestcaseExecuteMapper.getCaseNumsByModule(projectId, testCasePass.getTradeName(), null));
            //按模块查询通过的用例数

            testCasePass.setTestCasePassNum(cltTestcaseExecuteMapper.getPassCaseNumsByModule(projectId, testCasePass.getTradeName(), null));
            testCasePassList.add(testCasePass);

            porjectDatasTotal.setCaseExecuteNumTotal(porjectDatasTotal.getCaseExecuteNumTotal() + testCasePass.getTestCaseNum());
            porjectDatasTotal.setCaseExecutePassNumTotal(porjectDatasTotal.getCaseExecutePassNumTotal() + testCasePass.getTestCasePassNum());
        }
        porjectDatasTotal.setCaseExecutePassPersentTotal(getPersent(porjectDatasTotal.getCaseExecutePassNumTotal(), porjectDatasTotal.getCaseExecuteNumTotal()));
        return testCasePassList;
    }

    public List<TestCasePass> getTestCasePassByRound(String projectId,  List<TestCaseExecuteReportDTO> reportDTO, String roundId, TestCasePass totalTestCasePass){
        List<TestCasePass> testCasePassList = new ArrayList<>();

        for(TestCaseExecuteReportDTO testCaseExecuteReportDTO : reportDTO){
            TestCasePass testCasePass = new TestCasePass();
            testCasePass.setTradeName(testCaseExecuteReportDTO.getModule());

            testCasePass.setTestCaseNum(testCaseExecuteReportDTO.getExeTotal()-testCaseExecuteReportDTO.getExecute0()-testCaseExecuteReportDTO.getExecute1()); //总的-未执行-无效

            //按模块查询通过的用例数

            testCasePass.setTestCasePassNum(cltTestcaseExecuteMapper.getPassCaseNumsByModule(projectId, testCasePass.getTradeName(), roundId));
            testCasePassList.add(testCasePass);

            totalTestCasePass.setTestCaseNum(totalTestCasePass.getTestCaseNum() + testCasePass.getTestCaseNum());
            totalTestCasePass.setTestCasePassNum(totalTestCasePass.getTestCasePassNum() + testCasePass.getTestCasePassNum());

        }

        return testCasePassList;
    }

    private String getPersent(int num1, int num2){
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();

        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);

        String result = numberFormat.format((float) num1 / (float) num2 * 100);

        return result + "%";
    }

    /**
     * 两个数相除，保留两位小数
     * @param num1
     * @param num2
     * @return
     */
    private String getChu(int num1, int num2){
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();

        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);

        String result = numberFormat.format((float) num1 / (float) num2);

        return result;
    }

    /**
     * 两个数相除，保留两位小数
     * @param num1
     * @param num2
     * @return
     */
    private String getChu(double num1, int num2){
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();

        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);

        String result = numberFormat.format( num1 / (float) num2);

        return result;
    }

    private CltRound getCurrRound(String projectId){
        CltRoundExample example = new CltRoundExample();
        example.setOrderByClause("START_DATE DESC,CREATE_DATE DESC");
        CltRoundExample.Criteria criteria = example.createCriteria();
        criteria.andProIdEqualTo(projectId);
        criteria.andLogicDelEqualTo("N");

        List<CltRound> cltRounds = cltRoundMapper.selectByExample(example);
        if(cltRounds.size()>0){
            return cltRounds.get(0);
        }else{
            return null;
        }
    }

    /**
     * 查询项目下的模块
     * @param projectId
     * @return
     */
    private List<Demand> getModule(String projectId){
        DemandExample example = new DemandExample();
        example.setOrderByClause("CREATE_TIME ASC");
        DemandExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        criteria.andLogicDelEqualTo("N");
        criteria.andLevelNoEqualTo(3);

        List<Demand> demandList = demandMapper.selectByExample(example);

        //还需要列出LevelNo为2且是叶子节点的
        DemandExample leafExample = new DemandExample();
        leafExample.setOrderByClause("CREATE_TIME ASC");
        DemandExample.Criteria leafCriteria = leafExample.createCriteria();
        leafCriteria.andProjectIdEqualTo(projectId);
        leafCriteria.andLogicDelEqualTo("N");
        leafCriteria.andLevelNoEqualTo(2);
        leafCriteria.andLeafEqualTo("Y");

        List<Demand> demandLeafList = demandMapper.selectByExample(leafExample);
        demandList.addAll(demandLeafList);

        return demandList;
    }

    public List<TestTransPass> getTestTransPass(List<Demand> demandModules, PorjectDatasTotal porjectDatasTotal){
        List<TestTransPass> testTransPassList = new ArrayList<>();

        for(Demand demand : demandModules){
            TestTransPass testTransPass = new TestTransPass();
            testTransPass.setModuleName(demand.getName());

            //按模块查询交易数
            DemandExample example = new DemandExample();
            DemandExample.Criteria criteria = example.createCriteria();
            criteria.andLogicDelEqualTo("N");
            criteria.andLeafEqualTo("Y");
            criteria.andPathLike(demand.getPath()+"%");
            List<Demand> demandTransList = demandMapper.selectByExample(example);


            //按模块查询通过的交易数
            int modulePassNum = 0;int moduleTransNum = 0;
            for(Demand demandTrans : demandTransList){
                Integer itemNums = cltTestcaseExecuteMapper.checkTestCase(demandTrans.getId());
                if(itemNums>0){
                    moduleTransNum ++;
                }

                Integer itemPass = cltTestcaseExecuteMapper.checkTestItemPass(demandTrans.getId());
                if(itemPass==0 && itemNums>0){
                    modulePassNum ++;
                }
            }
            testTransPass.setTestTransNum(demandTransList.size());
            testTransPass.setTestTransPassNum(modulePassNum);

            testTransPassList.add(testTransPass);

            porjectDatasTotal.setModuleTransNumTotal(porjectDatasTotal.getModuleTransNumTotal() + testTransPass.getTestTransNum());
            porjectDatasTotal.setTransPassNumTotal(porjectDatasTotal.getTransPassNumTotal() + testTransPass.getTestTransPassNum());
        }
        porjectDatasTotal.setTransPassPersentTotal(getPersent(porjectDatasTotal.getTransPassNumTotal(), porjectDatasTotal.getModuleTransNumTotal()));
        return testTransPassList;
    }
}
