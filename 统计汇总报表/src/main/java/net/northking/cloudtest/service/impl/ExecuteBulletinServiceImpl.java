package net.northking.cloudtest.service.impl;

import com.alibaba.fastjson.JSON;
import net.northking.cloudtest.dao.analyse.*;
import net.northking.cloudtest.dao.cust.CltCustomerMapper;
import net.northking.cloudtest.dao.project.CltProjectCountMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.report.CltReportMapper;
import net.northking.cloudtest.dao.report.CltStsProgressMapper;
import net.northking.cloudtest.dao.task.CltTestcaseExecuteMapper;
import net.northking.cloudtest.dao.testBug.CltBugLogMapper;
import net.northking.cloudtest.domain.analyse.*;
import net.northking.cloudtest.domain.cust.CltCustomer;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.project.CltProjectCountExample;
import net.northking.cloudtest.domain.report.CltReport;
import net.northking.cloudtest.domain.report.CltReportExample;
import net.northking.cloudtest.domain.report.CltStsProgress;
import net.northking.cloudtest.domain.report.CltStsProgressExample;
import net.northking.cloudtest.domain.task.CltTestcaseExecuteExample;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.domain.testBug.CltBugLogExample;
import net.northking.cloudtest.dto.*;
import net.northking.cloudtest.dto.analyse.CltBatchDTO;
import net.northking.cloudtest.dto.analyse.CltRoundDTO;
import net.northking.cloudtest.dto.report.*;
import net.northking.cloudtest.dto.report.TestCasePass;
import net.northking.cloudtest.enums.CltBulletinCatalog;
import net.northking.cloudtest.enums.CltStsProgressCatalog;
import net.northking.cloudtest.enums.CltTestStatus;
import net.northking.cloudtest.query.analyse.DemandQuery;
import net.northking.cloudtest.service.ExecuteBulletinService;
import net.northking.cloudtest.utils.BeanUtil;
import net.northking.cloudtest.utils.CltUtils;
import net.northking.cloudtest.utils.WeekUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author:zwy
 * @Despriction:
 * @Date:Create in 11:01 2018/5/24
 * @Modify By:
 */
@Service
public class ExecuteBulletinServiceImpl implements ExecuteBulletinService {
    private Logger logger = LoggerFactory.getLogger(ExecuteBulletinServiceImpl.class);
    @Autowired
    private DemandMapper demandMapper;
    @Autowired
    private CltMapNodeMapper cltMapNodeMapper;
    @Autowired
    private CltProjectMapper cltProjectMapper;
    @Autowired
    private CltProjectCountMapper cltProjectCountMapper;
    @Autowired
    private TestCaseMapper testCaseMapper;
    @Autowired
    private CltReportMapper cltReportMapper;
    @Autowired
    private CltTestcaseExecuteMapper cltTestcaseExecuteMapper;
    @Autowired
    private CltCustomerMapper cltCustomerMapper;

    @Autowired
    private CltStsProgressMapper cltStsProgressMapper;
    @Autowired
    private CltBugLogMapper cltBugLogMapper;

    @Autowired
    private CltRoundMapper cltRoundMapper;

    @Autowired
    private CltBatchMapper cltBatchMapper;


    public void dealWithReport(String projectId, String roundId, Map dataMap) throws Exception {
        ExecuteBulletin executeBulletin = new ExecuteBulletin();
        DemandMapCountDto demandMapCountDto = getDemandMapCountDto(projectId);
        executeBulletin.setDemandMapCountDto(demandMapCountDto);
        //查出项目
        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);

        if (cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
            //查出客户
            CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
            executeBulletin.setCustName(cltCustomer.getCustName());
            executeBulletin.setProject(cltProject);

            //获取上一版本号
            CltReportExample reportExample = new CltReportExample();
            CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
            reportCriteria.andProjectIdEqualTo(projectId);
            reportCriteria.andCatalogEqualTo(CltBulletinCatalog.CE.getCode());
            reportCriteria.andRoundIdEqualTo(roundId);
            reportExample.setOrderByClause("CREATE_DATE DESC");
            List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
            if (cltReportList.size() == 0) {
                executeBulletin.setVersion("1.0");
            } else {
                executeBulletin.setVersion("1." + (cltReportList.get(0).getVersion() + 1));
            }

            //报告日期
            executeBulletin.setReportDate(new Date());

            //分析模块数量
            DemandExample demandExample = new DemandExample();
            DemandExample.Criteria demandCriteria = demandExample.createCriteria();
            demandCriteria.andProjectIdEqualTo(projectId);
            demandCriteria.andLevelNoEqualTo(3);
            demandCriteria.andLogicDelEqualTo("N");
            demandExample.setOrderByClause("CREATE_TIME ASC");
            int demandMudelNum = demandMapper.countByExample(demandExample);

            //还需要列出LevelNo为2且是叶子节点的
            DemandExample leafExample = new DemandExample();
            leafExample.setOrderByClause("CREATE_TIME ASC");
            DemandExample.Criteria leafCriteria = leafExample.createCriteria();
            leafCriteria.andProjectIdEqualTo(projectId);
            leafCriteria.andLogicDelEqualTo("N");
            leafCriteria.andLevelNoEqualTo(2);
            leafCriteria.andLeafEqualTo("Y");

            demandMudelNum += demandMapper.countByExample(leafExample);

            executeBulletin.setDemandMudelNum(demandMudelNum);

            //分析交易数量
            DemandExample demandTransExample = new DemandExample();
            DemandExample.Criteria demandTransCriteria = demandTransExample.createCriteria();
            demandTransCriteria.andProjectIdEqualTo(projectId);
            demandTransCriteria.andLeafEqualTo("Y");
            demandTransCriteria.andLogicDelEqualTo("N");
            int demandTransNum = demandMapper.countByExample(demandTransExample);
            executeBulletin.setDemandTransNum(demandTransNum);

            //获取项目内的用例数
            TestCaseExample testCaseExample = new TestCaseExample();
            TestCaseExample.Criteria testCaseCriteria = testCaseExample.createCriteria();
            testCaseCriteria.andProjIdEqualTo(projectId);
            testCaseCriteria.andLogicDelEqualTo("N");
            int countCaseNum = testCaseMapper.countByExample(testCaseExample);
            executeBulletin.setCountCaseNum(countCaseNum);

            //未设计用例数
            TestCaseExample testCaseIExample = new TestCaseExample();
            TestCaseExample.Criteria testCaseICriteria = testCaseIExample.createCriteria();
            testCaseICriteria.andProjIdEqualTo(projectId);
            testCaseICriteria.andLogicDelEqualTo("N");
            testCaseICriteria.andStatusEqualTo(CltTestStatus.INIT.getCode());
            int countCaseINum = testCaseMapper.countByExample(testCaseIExample);
            executeBulletin.setCountCaseINum(countCaseINum);

            //设计中用例数
            TestCaseExample testCaseDExample = new TestCaseExample();
            TestCaseExample.Criteria testCaseDCriteria = testCaseDExample.createCriteria();
            testCaseDCriteria.andProjIdEqualTo(projectId);
            testCaseDCriteria.andLogicDelEqualTo("N");
            testCaseDCriteria.andStatusEqualTo(CltTestStatus.DESIGN.getCode());
            int countCaseDNum = testCaseMapper.countByExample(testCaseDExample);
            executeBulletin.setCountCaseDNum(countCaseDNum);

            //已完成用例数
            TestCaseExample testCaseSExample = new TestCaseExample();
            TestCaseExample.Criteria testCaseSCriteria = testCaseSExample.createCriteria();
            testCaseSCriteria.andProjIdEqualTo(projectId);
            testCaseSCriteria.andLogicDelEqualTo("N");
            testCaseSCriteria.andStatusEqualTo(CltTestStatus.FINISH_CASE.getCode());
            int countCaseSNum = testCaseMapper.countByExample(testCaseSExample);
            executeBulletin.setCountCaseSNum(countCaseSNum);


            //获取需求树
            List<DemandQuery> demandTree = getDemandTree(projectId);
            executeBulletin.setDemandQueryList(demandTree);

            //测试准出准则
            CltProjectCount cltProjectCount = getCltProjectCount(projectId);
            if(cltProjectCount!=null){
                executeBulletin.setCltProjectCount(cltProjectCount);
            }

            //存放总计的数据
            PorjectDatasTotal porjectDatasTotal = new PorjectDatasTotal();
            porjectDatasTotal.init();
            //测试覆盖率分析
            List<CoverageReportDataWord> coverageReportDataWordList = getCoverageReportDataWordList(projectId, porjectDatasTotal);
            executeBulletin.setCoverageReportDataWordList(coverageReportDataWordList);
            //案例通过率分析
            List<TestCaseExecuteReportDTO> testCaseExecuteReportDTOS = cltTestcaseExecuteMapper.queryCaseExecuteByRoundAndModule(projectId, roundId);
            List<TestCasePass> testCasePassList = getTestCasePass(testCaseExecuteReportDTOS, porjectDatasTotal);
            executeBulletin.setTestCasePassList(testCasePassList);

            //交易通过率分析
            //查询有哪些模块
            List<Demand> demandModules = demandMapper.queryAllDemandByExecute(projectId, roundId);
            List<TestTransPass> testTransPassList = getTestTransPass(demandModules, porjectDatasTotal, roundId);
            executeBulletin.setTestTransPassList(testTransPassList);

            CltRound currRound = cltRoundMapper.selectByPrimaryKey(roundId);

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
            cltStsProgressCaseCriteria.andLevelEqualTo("R");
            cltStsProgressCaseCriteria.andTypeIdEqualTo(roundId);
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
                    porjectDatasTotal.setEffectiveCaseNumTotal(porjectDatasTotal.getEffectiveCaseNumTotal() + Integer.parseInt((String) resultToMap.get("total")));
                }catch (Exception e){
                    e.printStackTrace();
                }
                beginNum ++;
            }

            //缺陷有效率(除了10的都是有效，INVALID("10","无效"))，缺陷修复率:CLOSE("9","关闭")
            List<TestBugPersent> testBugPersentList = new ArrayList<>();
            CltStsProgressExample cltStsProgressExample = new CltStsProgressExample();
            CltStsProgressExample.Criteria cltStsProgressCriteria = cltStsProgressExample.createCriteria();
            cltStsProgressCriteria.andProjectIdEqualTo(projectId);
            cltStsProgressCriteria.andCatalogEqualTo(CltStsProgressCatalog.BS.getCode());
            cltStsProgressCriteria.andTypeEqualTo("M"); //按模块
            cltStsProgressCriteria.andLevelEqualTo("R");
            cltStsProgressCriteria.andTypeIdEqualTo(roundId);
            cltStsProgressCriteria.andStsDataEqualTo(stsDate);
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
                    testBugPersent.setTestBugCasePersent(getChu(testBugPersent.getEffectiveNum(), testBugPersent.getEffectiveCaseNum()));
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
            porjectDatasTotal.setTestBugCasePersentTotal(getChu(porjectDatasTotal.getEffectiveNumTotal(), porjectDatasTotal.getEffectiveCaseNumTotal()));
            porjectDatasTotal.setRepairPersentTotal(getPersent(porjectDatasTotal.getCloseNumTotal(), porjectDatasTotal.getEffectiveNumTotal()));

            executeBulletin.setTestBugPersentList(testBugPersentList);


            //遗留缺陷业务及严重级分布
            CltStsProgressExample cltStsProgressDimensionsExample = new CltStsProgressExample();
            CltStsProgressExample.Criteria cltStsProgressDimensionsCriteria = cltStsProgressDimensionsExample.createCriteria();
            cltStsProgressDimensionsCriteria.andProjectIdEqualTo(projectId);
            cltStsProgressDimensionsCriteria.andCatalogEqualTo(CltStsProgressCatalog.BL.getCode());
            cltStsProgressDimensionsCriteria.andTypeEqualTo("M"); //按模块
            cltStsProgressDimensionsCriteria.andLevelEqualTo("R");
            cltStsProgressDimensionsCriteria.andTypeIdEqualTo(roundId);
            cltStsProgressDimensionsCriteria.andStsDataEqualTo(stsDate);
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
            executeBulletin.setTestBugDimensionsList(testBugDimensionsList);

            /*//缺陷趋势图base64
            String bugBTChartPath = reportChartPrePath + projectId +"/bugBTChart.png";
            if(new File(bugBTChartPath).exists()) {
                dataMap.put("bugBTImage", wordUtil.getImageStr(bugBTChartPath));
            }*/


            //获取项目内的轮次，包括批次信息
           // List<CltRoundDTO> roundDtoList = new ArrayList<>();
//            CltRoundExample roundExample = new CltRoundExample();
//            CltRoundExample.Criteria roundCriteria = roundExample.createCriteria();
//            roundCriteria.andProIdEqualTo(projectId);
//            roundCriteria.andLogicDelEqualTo("N");
//            List<CltRound> roundList = cltRoundMapper.selectByExample(roundExample);
            //CltRound cltRound = roundList.get(0);
                CltRound cltRound = cltRoundMapper.selectByPrimaryKey(roundId);
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
                List<TestCasePass> testRoundCasePassList = getTestCasePassByRound(testCaseExecuteReportDTOS, totalTestCasePass);

                cltRoundDTO.setTestCasePassList(testRoundCasePassList);
                cltRoundDTO.setTotalTestCasePass(totalTestCasePass);




            executeBulletin.setCltRoundDTO(cltRoundDTO);







            //存在的风险
            CltBugLogExample cltBugLogExample = new CltBugLogExample();
            CltBugLogExample.Criteria cltBugLogCriteria = cltBugLogExample.createCriteria();
            cltBugLogCriteria.andBugIdEqualTo(projectId);
            cltBugLogExample.setOrderByClause("LOG_TIME DESC");
            List<CltBugLog> cltBugLogList = cltBugLogMapper.selectByExample(cltBugLogExample);
            executeBulletin.setCltBugLogList(cltBugLogList);

            dataMap.put("porjectDatasTotal", porjectDatasTotal);
            dataMap.put("executeBulletin", executeBulletin);

        }

    }






    public Integer insertOrUpdateData(String projectId, String roundId, String operatorUserId, String docPath, String swfPath) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

        CltRound cltRound = cltRoundMapper.selectByPrimaryKey(roundId);

        int result = 0;
        if (operatorUserId == null) operatorUserId = "system";
        CltReportExample reportExample = new CltReportExample();
        CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
        reportCriteria.andProjectIdEqualTo(projectId);
        reportCriteria.andCatalogEqualTo(CltBulletinCatalog.CE.getCode());
        reportCriteria.andRoundIdEqualTo(roundId);
        reportExample.setOrderByClause("CREATE_DATE DESC");
        List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
        if (cltReportList.size() > 0) { //修改
            CltReport cltReport = cltReportList.get(0);
            cltReport.setUpdateDate(new Date());
            cltReport.setUpdateUser(operatorUserId);
            cltReport.setDocPath(docPath);
            cltReport.setSwfPath(swfPath);
            cltReport.setVersion(cltReport.getVersion() + 1);
            cltReport.setRoundId(roundId);
            result = cltReportMapper.updateByPrimaryKeySelective(cltReport);
        } else { //新增
            //查出项目
            CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);
            if (cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
                //查出客户
                CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
                CltReport cltReport = new CltReport();
                cltReport.setCustomId(cltCustomer.getCustId());
                cltReport.setProjectId(projectId);
                cltReport.setReportName(cltProject.getProName() + cltRound.getRoundName() + "执行报告");
                cltReport.setCatalog(CltBulletinCatalog.CE.getCode());
                cltReport.setReportDate(new Date());
                cltReport.setCreateUser(operatorUserId);
                cltReport.setCreateDate(new Date());
                cltReport.setDocPath(docPath);
                cltReport.setSwfPath(swfPath);
                cltReport.setVersion(0);
                cltReport.setRoundId(roundId);
                result = cltReportMapper.insertSelective(cltReport);
            }
        }
        return result;

    }

    //t统计数量
    public DemandMapCountDto getDemandMapCountDto(String projectId){

        DemandMapCountDto demandMapCountDto  = demandMapper.queryCountByproId(projectId);
        Integer deLeafNum = demandMapCountDto.getDeLeafNum();
        Integer leveNoNum = demandMapCountDto.getLeveNoNum();

        demandMapCountDto = cltMapNodeMapper.queryCountStatus(projectId);
        demandMapCountDto.setDeLeafNum(deLeafNum);
        demandMapCountDto.setLeveNoNum(leveNoNum);
        return demandMapCountDto;

    }

    //获取需求树
    public List<DemandQuery> getDemandTree(String projectId){
        List<DemandQuery> treeList = new ArrayList<>();
        try{
            List<Demand> list = demandMapper.queryAllDemandByProId(projectId);
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

        return treeList;
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

    //读取需求分析情况图表数据
    public List<DemandMapDto> getDemandMapDtos(String projectId){
        DemandExample demandExample = new DemandExample();
        DemandExample.Criteria criteria = demandExample.createCriteria();
        criteria.andLevelNoEqualTo(3);
        criteria.andLogicDelEqualTo("N");
        criteria.andProjectIdEqualTo(projectId);
        demandExample.setOrderByClause("CREATE_TIME ASC");
        List<DemandMapDto> list = new ArrayList<>();

        List<Demand> demands = demandMapper.selectByExample(demandExample);
        //还需要列出LevelNo为2且是叶子节点的
        DemandExample leafExample = new DemandExample();
        leafExample.setOrderByClause("CREATE_TIME ASC");
        DemandExample.Criteria leafCriteria = leafExample.createCriteria();
        leafCriteria.andProjectIdEqualTo(projectId);
        leafCriteria.andLogicDelEqualTo("N");
        leafCriteria.andLevelNoEqualTo(2);
        leafCriteria.andLeafEqualTo("Y");

        List<Demand> demandLeafList = demandMapper.selectByExample(leafExample);
        demands.addAll(demandLeafList);

        for(Demand demand:demands){
            DemandMapDto demandMapDto = demandMapper.selectCoutSataus(demand.getPath());
            demandMapDto.setName(demand.getName());
            list.add(demandMapDto);
        }
        return list;
    }
    //测试准出准则
    public CltProjectCount getCltProjectCount(String projectId) {
        CltProjectCountExample projectCountExample = new CltProjectCountExample();
        CltProjectCountExample.Criteria projectCountCriteria = projectCountExample.createCriteria();
        projectCountCriteria.andProIdEqualTo(projectId);
        List<CltProjectCount> cltProjectCountList = cltProjectCountMapper.selectByExample(projectCountExample);
        if (cltProjectCountList.size() > 0) {
            CltProjectCount cltProjectCount = cltProjectCountList.get(0);
            return cltProjectCount;
        }
        return null;
    }
    //测试覆盖率分析
    public List<CoverageReportDataWord> getCoverageReportDataWordList(String projectId,PorjectDatasTotal porjectDatasTotal){

        List<Demand> demandList = demandMapper.queryDemandNumByTrade(projectId); //导图需求点
        List<TestCase> testCaseList=testCaseMapper.queryTestCaseNumByTrade(projectId);
        List<CoverageReportDataWord> coverageReportDataWordList = getCoverageReportDataWord(demandList, testCaseList, porjectDatasTotal);

        return coverageReportDataWordList;
    }


    //组装数据
    public List<CoverageReportDataWord> getCoverageReportDataWord(List<Demand> demandList, List<TestCase> testCaseList, PorjectDatasTotal porjectDatasTotal){
        List<CoverageReportDataWord> coverageReportDataWordList = new ArrayList<>();
        Map<String,Integer> testCaseMap = new HashMap<>();//存储测试用例数据

        for(TestCase testCase : testCaseList){
            testCaseMap.put(testCase.getTestItem(), testCase.getCaseNum());
        }

        for(Demand demand : demandList){
            CoverageReportDataWord coverageReportDataWord = new CoverageReportDataWord();
            coverageReportDataWord.setTradeName(demand.getName());
            coverageReportDataWord.setDemandNum(demand.getPointNum()==null?0:demand.getPointNum());
            coverageReportDataWord.setTestCaseNum(testCaseMap.get(demand.getName())==null?0:testCaseMap.get(demand.getName()));
            coverageReportDataWord.setCoveragePersent(getPersent(coverageReportDataWord.getTestCaseNum(), coverageReportDataWord.getDemandNum()));
            coverageReportDataWordList.add(coverageReportDataWord);
            porjectDatasTotal.setCaseNumTotal(porjectDatasTotal.getCaseNumTotal() + coverageReportDataWord.getTestCaseNum());
            porjectDatasTotal.setTransNumTotal(porjectDatasTotal.getTransNumTotal() + coverageReportDataWord.getDemandNum());
        }

        porjectDatasTotal.setCasePersentTotal(getPersent(porjectDatasTotal.getCaseNumTotal(), porjectDatasTotal.getTransNumTotal()));
        return coverageReportDataWordList;
    }

    //组装数据
    public List<TestCasePass> getTestCasePass(List<TestCaseExecuteReportDTO> testCaseExecuteReportDTOS, PorjectDatasTotal porjectDatasTotal){
        List<TestCasePass> testCasePassList = new ArrayList<>();

        for(TestCaseExecuteReportDTO testCase : testCaseExecuteReportDTOS){
            TestCasePass testCasePass = new TestCasePass();
            testCasePass.setTradeName(testCase.getModule());
            testCasePass.setTestCaseNum(testCase.getExeTotal());
            //按模块查询通过的用例数

            testCasePass.setTestCasePassNum(testCase.getExecute6());
            testCasePassList.add(testCasePass);

            porjectDatasTotal.setCaseExecuteNumTotal(porjectDatasTotal.getCaseExecuteNumTotal() + testCasePass.getTestCaseNum());
            porjectDatasTotal.setCaseExecutePassNumTotal(porjectDatasTotal.getCaseExecutePassNumTotal() + testCasePass.getTestCasePassNum());
        }
        porjectDatasTotal.setCaseExecutePassPersentTotal(getPersent(porjectDatasTotal.getCaseExecutePassNumTotal(), porjectDatasTotal.getCaseExecuteNumTotal()));
        return testCasePassList;
    }

    public List<TestCasePass> getTestCasePassByRound(List<TestCaseExecuteReportDTO> testCaseExecuteReportDTOS, TestCasePass totalTestCasePass){
        List<TestCasePass> testCasePassList = new ArrayList<>();

        for(TestCaseExecuteReportDTO testCase : testCaseExecuteReportDTOS){
            TestCasePass testCasePass = new TestCasePass();
            testCasePass.setTradeName(testCase.getModule());
            testCasePass.setTestCaseNum(testCase.getExeTotal());
            //按模块查询通过的用例数
            testCasePass.setTestCasePassNum(testCase.getExecute6());
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

    public List<TestTransPass> getTestTransPass(List<Demand> demandModules, PorjectDatasTotal porjectDatasTotal, String roundId){
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
                Integer itemNums = cltTestcaseExecuteMapper.checkTestCaseByRound(roundId, demandTrans.getId());
                if(itemNums>0){
                    moduleTransNum ++;
                }
                Integer itemPass = cltTestcaseExecuteMapper.checkTestItemPassByRound(roundId, demandTrans.getId());
                if(itemPass==0 && itemNums>0){
                    modulePassNum ++;
                }
            }
            testTransPass.setTestTransNum(moduleTransNum);
            testTransPass.setTestTransPassNum(modulePassNum);

            testTransPassList.add(testTransPass);

            porjectDatasTotal.setModuleTransNumTotal(porjectDatasTotal.getModuleTransNumTotal() + testTransPass.getTestTransNum());
            porjectDatasTotal.setTransPassNumTotal(porjectDatasTotal.getTransPassNumTotal() + testTransPass.getTestTransPassNum());
        }
        porjectDatasTotal.setTransPassPersentTotal(getPersent(porjectDatasTotal.getTransPassNumTotal(), porjectDatasTotal.getModuleTransNumTotal()));
        return testTransPassList;
    }

}
