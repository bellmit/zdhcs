package net.northking.cloudtest.service.impl.report;

import net.northking.cloudtest.assist.RedisUtil;
import net.northking.cloudtest.dao.analyse.*;
import net.northking.cloudtest.dao.cust.CltCustomerMapper;
import net.northking.cloudtest.dao.project.CltProjectCountMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.report.CltReportMapper;
import net.northking.cloudtest.dao.task.CltTestcaseExecuteMapper;
import net.northking.cloudtest.dao.testBug.CltBugLogMapper;
import net.northking.cloudtest.dao.testBug.CltTestBugMapper;
import net.northking.cloudtest.domain.analyse.CltRound;
import net.northking.cloudtest.domain.analyse.CltRoundExample;
import net.northking.cloudtest.domain.analyse.Demand;
import net.northking.cloudtest.domain.analyse.DemandExample;
import net.northking.cloudtest.domain.cust.CltCustomer;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.report.CltReport;
import net.northking.cloudtest.domain.report.CltReportExample;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.domain.testBug.CltBugLogExample;
import net.northking.cloudtest.domain.testBug.CltTestBug;
import net.northking.cloudtest.domain.testBug.CltTestBugExample;
import net.northking.cloudtest.dto.*;
import net.northking.cloudtest.dto.report.DemandMapNodeDTO;
import net.northking.cloudtest.dto.report.TestCaseExecuteReportDTO;
import net.northking.cloudtest.dto.report.TestCaseReportDTO;
import net.northking.cloudtest.dto.testBug.TestBugDTO;
import net.northking.cloudtest.enums.CltBulletinCatalog;
import net.northking.cloudtest.enums.CltTestBugStatus;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.service.DayReportService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liujinghao on 2018/5/21.
 */
@Service
public class DayReportServiceImpl implements DayReportService {

    private final static Logger logger = LoggerFactory.getLogger(DayReportServiceImpl.class);

    @Autowired
    private DemandMapper demandMapper;

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Autowired
    private CltMapNodeMapper cltMapNodeMapper;

    @Autowired
    private CltProjectMapper cltProjectMapper;

    @Autowired
    private CltCustomerMapper cltCustomerMapper;

    @Autowired
    private CltReportMapper cltReportMapper;

    @Autowired
    private CltRoundMapper cltRoundMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CltTestBugMapper cltTestBugMapper;

    @Autowired
    private CltBugLogMapper cltBugLogMapper;

    @Autowired
    private CltTestcaseExecuteMapper cltTestcaseExecuteMapper;

    @Autowired
    private CltProjectCountMapper cltProjectCountMapper;


    /**
     * 处理日报表数据
     *
     * @param proId
     * @param dataMap
     * @throws Exception
     */
    @Override
    public void dealWithDayReport(String proId, Map dataMap) throws Exception {
        //存放word数据
        DayReport dayReport = new DayReport();
        //查出项目
        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(proId);
        if (cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
            //查出客户
            CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
            dayReport.setCustName(cltCustomer.getCustName());
            dayReport.setProject(cltProject);

            //获取上一版本号
            CltReportExample reportExample = new CltReportExample();
            CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
            reportCriteria.andProjectIdEqualTo(proId);
            reportCriteria.andCatalogEqualTo(CltBulletinCatalog.TD.getCode());
            reportExample.setOrderByClause("CREATE_DATE DESC");
            List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
            if (cltReportList.size() == 0) {
                dayReport.setVersion("1.0");
            } else {
                dayReport.setVersion("1." + (cltReportList.get(0).getVersion() + 1));
            }

            //报告日期
            dayReport.setReportDate(new Date());
         /*   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, -8);
            Date date = c.getTime();*/
            //获取当前所处的轮次信息

            System.out.println("----+++++++查询轮次信息");
            List<CltRound>list=cltRoundMapper.selectRoundInfoByDate(proId, new Date());
            dayReport.setCltRound(list);

            //判断项目执行所处的阶段
            CltProjectCount cltProjectCount=new CltProjectCount() ;
            cltProjectCount.setProId(proId);
            CltProjectCount clt=cltProjectCountMapper.selectByProId(cltProjectCount);
            JudgeProjectStatus judgeProjectStatus= new JudgeProjectStatus();
        /*    System.out.println("现在日期"+new Date());
            System.out.println("需求设计阶段"+clt.getDemandStartDate()+clt.getDemandEndDate());
            System.out.println("用例设计阶段"+clt.getTestStartDate()+clt.getTestEndDate());*/
              if(clt!=null){
                  if (clt.getDemandStartDate().before(new Date())&&clt.getDemandEndDate().after(new Date())){
                      judgeProjectStatus.setDemandStatus(1);
                  }else{
                      judgeProjectStatus.setDemandStatus(2);
                  }
                  if (clt.getTestStartDate().before(new Date())&&clt.getTestEndDate().after(new Date())){
                      judgeProjectStatus.setTestCaseStatus(1);
                  }else{
                      judgeProjectStatus.setTestCaseStatus(2);
                  }
                  if (clt.getExecuteStartDate().before(new Date())&&clt.getExecuteEndDate().after(new Date())){
                      judgeProjectStatus.setExecuteStatus(1);
                  }else{
                      judgeProjectStatus.setExecuteStatus(2);
                  }
                  System.out.println("项目进行到的阶段"+judgeProjectStatus.toString());
              }
            dayReport.setJudgeProjectStatus(judgeProjectStatus);


            //获取需求分析完成情况(人员)
            QualityReportQuery query = new QualityReportQuery();
            query.setProId(proId);
            query.setStartDate(new Date());
            query.setEndDate(new Date());
            List<DemandMapNodeDTO> userChart = getDemandAnalyseData(query, "U");
            System.out.println("需求分析人员------"+userChart);
            DemandDataTotal dataTotal = new DemandDataTotal();
            Integer num = 0;
            Integer num1 = 0;
            Integer num2 = 0;
            Integer num3 = 0;
            for (DemandMapNodeDTO demandMapNodeDTO : userChart
                    ) {
                demandMapNodeDTO.setModifierName((String) redisUtil.get("username:" + demandMapNodeDTO.getLastModifier()));
                num += demandMapNodeDTO.getAnalysing();
                num1 += demandMapNodeDTO.getApprove();
                num2 += demandMapNodeDTO.getForUpdate();
                num3 += demandMapNodeDTO.getDone();
            }
            dataTotal.setAnalysingTotal(num);
            dataTotal.setApproveTotal(num1);
            dataTotal.setForUpdateTotal(num2);
            dataTotal.setDoneTotal(num3);
            dataMap.put("demandDataTotal", dataTotal);
            dayReport.setDemandByUserQuality(userChart);
            System.out.println("userChart=" + userChart);
            //获取需求分析完成情况(模块)
            QualityReportQuery query1 = new QualityReportQuery();
            query1.setProId(proId);
            query1.setStartDate(cltProject.getCreateDate());
            query1.setEndDate(new Date());
            List<DemandMapNodeDTO> moduleChart = getDemandAnalyseData(query1, "M");
            DemandDataTotal demandDataTotal = new DemandDataTotal();
            Integer num4 = 0;
            Integer num5 = 0;
            Integer num6 = 0;
            Integer num7 = 0;
            for (DemandMapNodeDTO demandMapNodeDTO : moduleChart
                    ) {
                num4 += demandMapNodeDTO.getAnalysing();
                num5 += demandMapNodeDTO.getApprove();
                num6 += demandMapNodeDTO.getForUpdate();
                num7 += demandMapNodeDTO.getDone();
            }
            demandDataTotal.setAnalysingTotal(num4);
            demandDataTotal.setApproveTotal(num5);
            demandDataTotal.setForUpdateTotal(num6);
            demandDataTotal.setDoneTotal(num7);
            dataMap.put("demandDataTotal1", demandDataTotal);
            dayReport.setDemandByModuleQuality(moduleChart);
            System.out.println("需求分析模块---moduleChart=" + moduleChart);

            //获取测试用例设计完成情况
            QualityReportQuery qualityReportQuery = new QualityReportQuery();
            qualityReportQuery.setProId(proId);
            qualityReportQuery.setStartDate(new Date());
            qualityReportQuery.setEndDate(new Date());
            //按人员(当日)
            List<TestCaseReportDTO> testCaseReportUserList = testCaseMapper.queryCaseReportByUserByTime(qualityReportQuery);
            TestCaseCountTotal testCaseCountTotal = new TestCaseCountTotal();
            Integer caseNum = 0;
            Integer caseNum1 = 0;
            Integer caseNum2 = 0;
            for (TestCaseReportDTO testCaseReportDTO : testCaseReportUserList
                    ) {
                testCaseReportDTO.setModifierName((String) redisUtil.get("username:" + testCaseReportDTO.getLastModifier()));
                caseNum += testCaseReportDTO.getSi();
                caseNum1 += testCaseReportDTO.getSd();
                caseNum2 += testCaseReportDTO.getSs();
                System.out.println("用例设计人员"+testCaseReportDTO.getModifierName());
            }
            testCaseCountTotal.setSiTotal(caseNum);
            testCaseCountTotal.setSdTotal(caseNum1);
            testCaseCountTotal.setSsTotal(caseNum2);
            dataMap.put("TestCaseCountTotal", testCaseCountTotal);
            System.out.println("用例设计人员的各种用例个数"+testCaseCountTotal.getSiTotal()+testCaseCountTotal.getSdTotal());
            dayReport.setTestCaseByUserQuality(testCaseReportUserList);
            QualityReportQuery qualityReportQuery1 = new QualityReportQuery();
            qualityReportQuery1.setProId(proId);
            qualityReportQuery1.setStartDate(cltProject.getCreateDate());
            qualityReportQuery1.setEndDate(new Date());
            //按模块(整体)
            List<TestCaseReportDTO> testCaseReportModuleList = testCaseMapper.queryCaseReportByModuleByTime(qualityReportQuery1);
            TestCaseCountTotal testCaseCountTotal1 = new TestCaseCountTotal();
            Integer caseNum3 = 0;
            Integer caseNum4 = 0;
            Integer caseNum5 = 0;
            for (TestCaseReportDTO testCaseReportDTO : testCaseReportModuleList
                    ) {
                caseNum3 += testCaseReportDTO.getSi();
                caseNum4 += testCaseReportDTO.getSd();
                caseNum5 += testCaseReportDTO.getSs();

            }
            testCaseCountTotal1.setSiTotal(caseNum3);
            testCaseCountTotal1.setSsTotal(caseNum5);
            testCaseCountTotal1.setSdTotal(caseNum4);
            dataMap.put("testCaseCountTotal1",testCaseCountTotal1);
            dayReport.setTestCaseByModuleQuality(testCaseReportModuleList);

            CltTestBug cltTestBug = new CltTestBug();
            cltTestBug.setProId(proId);
            //缺陷总体情况
            List<TestBugDTO> testBug= cltTestBugMapper.selectAllTestBugByProId(cltTestBug);
            TestBugCountNum testBugCountNum= new TestBugCountNum();
            Integer alValueTotal=0;
            Integer blValueTotal=0;
            Integer clValueTotal=0;
            Integer dlValueTotal=0;
            Integer elValueTotal=0;
            Integer case1=0;
            Integer case2=0;
            Integer case3=0;
            Integer case4=0;
            Integer case5=0;
            Integer case6=0;
            Integer case7=0;
            Integer case8=0;
            Integer case9=0;
            Integer case10=0;
            Integer case11=0;
            Integer total=0;
            for (TestBugDTO testBugDTO:testBug
                    ) {
                alValueTotal+=testBugDTO.getCaseA();
                blValueTotal+=testBugDTO.getCaseB();
                clValueTotal+=testBugDTO.getCaseC();
                dlValueTotal+=testBugDTO.getCaseD();
                elValueTotal+=testBugDTO.getCaseE();
                total+=testBugDTO.getCaseTotal();
                case1+=testBugDTO.getCase1();
                case2+=testBugDTO.getCase2();
                case3+=testBugDTO.getCase3();
                case4+=testBugDTO.getCase4();
                case5+=testBugDTO.getCase5();
                case6+=testBugDTO.getCase6();
                case7+=testBugDTO.getCase7();
                case8+=testBugDTO.getCase8();
                case9+=testBugDTO.getCase9();
                case10+=testBugDTO.getCase10();
                case11+=testBugDTO.getCase11();
            }
            testBugCountNum.setAlValueTotal(alValueTotal);
            testBugCountNum.setBlValueTotal(blValueTotal);
            testBugCountNum.setClValueTotal(clValueTotal);
            testBugCountNum.setDlValueTotal(dlValueTotal);
            testBugCountNum.setElValueTotal(elValueTotal);
            testBugCountNum.setTotal(total);
            testBugCountNum.setCase1(case1);
            testBugCountNum.setCase2(case2);
            testBugCountNum.setCase3(case3);
            testBugCountNum.setCase4(case4);
            testBugCountNum.setCase5(case5);
            testBugCountNum.setCase6(case6);
            testBugCountNum.setCase7(case7);
            testBugCountNum.setCase8(case8);
            testBugCountNum.setCase9(case9);
            testBugCountNum.setCase10(case10);
            testBugCountNum.setCase11(case11);
            dataMap.put("testBugCountNum",testBugCountNum);
            dayReport.setTestBugAllQuality(testBug);
            //当日缺陷情况
            cltTestBug.setCreateDate(new Date());
            List<TestBugDTO> testBugDTOS = cltTestBugMapper.selectAllTestBugByProIdAndDate(cltTestBug);
            System.out.println("testBugDTOS=" + testBugDTOS);
            TestBugCountNum testBugCountNum1= new TestBugCountNum();
            Integer alValueCountTotal=0;
            Integer blValueCountTotal=0;
            Integer clValueCountTotal=0;
            Integer dlValueCountTotal=0;
            Integer elValueCountTotal=0;
            Integer caseCount1=0;
            Integer caseCount2=0;
            Integer caseCount3=0;
            Integer caseCount4=0;
            Integer caseCount5=0;
            Integer caseCount6=0;
            Integer caseCount7=0;
            Integer caseCount8=0;
            Integer caseCount9=0;
            Integer caseCount10=0;
            Integer caseCount11=0;
            Integer totalCount=0;
            for (TestBugDTO testBugDTO:testBug
                    ) {
                alValueCountTotal+=testBugDTO.getCaseA();
                blValueCountTotal+=testBugDTO.getCaseB();
                clValueCountTotal+=testBugDTO.getCaseC();
                dlValueCountTotal+=testBugDTO.getCaseD();
                elValueCountTotal+=testBugDTO.getCaseE();
                totalCount+=testBugDTO.getCaseTotal();
                caseCount1+=testBugDTO.getCase1();
                caseCount2+=testBugDTO.getCase2();
                caseCount3+=testBugDTO.getCase3();
                caseCount4+=testBugDTO.getCase4();
                caseCount5+=testBugDTO.getCase5();
                caseCount6+=testBugDTO.getCase6();
                caseCount7+=testBugDTO.getCase7();
                caseCount8+=testBugDTO.getCase8();
                caseCount9+=testBugDTO.getCase9();
                caseCount10+=testBugDTO.getCase10();
                caseCount11+=testBugDTO.getCase11();
            }
            testBugCountNum1.setAlValueTotal(alValueCountTotal);
            testBugCountNum1.setBlValueTotal(blValueCountTotal);
            testBugCountNum1.setClValueTotal(clValueCountTotal);
            testBugCountNum1.setDlValueTotal(dlValueCountTotal);
            testBugCountNum1.setElValueTotal(elValueCountTotal);
            testBugCountNum1.setTotal(totalCount);
            testBugCountNum1.setCase1(caseCount1);
            testBugCountNum1.setCase2(caseCount2);
            testBugCountNum1.setCase3(caseCount3);
            testBugCountNum1.setCase4(caseCount4);
            testBugCountNum1.setCase5(caseCount5);
            testBugCountNum1.setCase6(caseCount6);
            testBugCountNum1.setCase7(caseCount7);
            testBugCountNum1.setCase8(caseCount8);
            testBugCountNum1.setCase9(caseCount9);
            testBugCountNum1.setCase10(caseCount10);
            testBugCountNum1.setCase11(caseCount11);
            dataMap.put("testBugCountNum1",testBugCountNum1);
            dayReport.setTestBugQuality(testBugDTOS);
            //存在的风险
            CltBugLogExample cltBugLogExample = new CltBugLogExample();
            CltBugLogExample.Criteria cltBugLogCriteria = cltBugLogExample.createCriteria();
            cltBugLogCriteria.andBugIdEqualTo(proId);
            cltBugLogExample.setOrderByClause("LOG_TIME DESC");
            List<CltBugLog> cltBugLogList = cltBugLogMapper.selectByExample(cltBugLogExample);
            dayReport.setCltBugLogList(cltBugLogList);
            dataMap.put("dayReport", dayReport);

        }
    }

    /**
     * 插入或更新数据
     *
     * @param proId
     * @param operatorUserId
     * @param docPath
     * @param swfPath
     * @return
     * @throws Exception
     */
    @Override
    public Integer insertOrUpdateDayReportData(String proId, String operatorUserId, String docPath, String swfPath) throws Exception {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal= Calendar.getInstance();
        cal.add(Calendar.DATE,-1);
        Date yestoday=cal.getTime();

        int result = 0;
        if (operatorUserId == null) operatorUserId = "system";
        CltReportExample reportExample = new CltReportExample();
        CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
        reportCriteria.andProjectIdEqualTo(proId);
        reportCriteria.andCatalogEqualTo(CltBulletinCatalog.TD.getCode());
        reportCriteria.andReportDateGreaterThan(yestoday);
        reportCriteria.andReportDateLessThanOrEqualTo(new Date());
        reportExample.setOrderByClause("CREATE_DATE DESC");
        List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
        if (cltReportList.size() > 0) { //修改
            CltReport cltReport = cltReportList.get(0);
            cltReport.setUpdateDate(new Date());
            cltReport.setUpdateUser(operatorUserId);
            cltReport.setDocPath(docPath);
            cltReport.setSwfPath(swfPath);
            cltReport.setVersion(cltReport.getVersion() + 1);
            result = cltReportMapper.updateByPrimaryKeySelective(cltReport);
        } else { //新增
            //查出项目
            CltProject cltProject = cltProjectMapper.selectByPrimaryKey(proId);
            if (cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
                //查出客户
                CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
                CltReport cltReport = new CltReport();
                cltReport.setCustomId(cltCustomer.getCustId());
                cltReport.setProjectId(proId);
                cltReport.setReportName(cltProject.getProName() + sdf.format(new Date()) + "测试日报");
                cltReport.setCatalog(CltBulletinCatalog.TD.getCode());
                cltReport.setReportDate(new Date());
                cltReport.setCreateUser(operatorUserId);
                cltReport.setCreateDate(new Date());
                cltReport.setDocPath(docPath);
                cltReport.setSwfPath(swfPath);
                cltReport.setVersion(0);
                result = cltReportMapper.insertSelective(cltReport);
            }
        }
        return result;
    }

    //需求分析完成情况
    public List<DemandMapNodeDTO> getDemandAnalyseData(QualityReportQuery qualityReportQuery, String type) {


        try {

            if ("U".equals(type)) {
                List<DemandMapNodeDTO> demandMapNodeDTOS = cltMapNodeMapper.queryMapNodeOnUserByTime(qualityReportQuery);

                return demandMapNodeDTOS;
            }

            if ("M".equals(type)) {

                List<DemandMapNodeDTO> demandMapNodeDTOS = new ArrayList<>();
                //查询所有的模块
                DemandExample example = new DemandExample();
                DemandExample.Criteria criteria = example.createCriteria();
                criteria.andLevelNoEqualTo(3);
                criteria.andLogicDelEqualTo("N");
                criteria.andProjectIdEqualTo(qualityReportQuery.getProId());
                example.setOrderByClause("CREATE_TIME ASC");
                List<Demand> demandList = demandMapper.selectByExample(example);

                //还需要列出LevelNo为2且是叶子节点的
                DemandExample leafExample = new DemandExample();
                leafExample.setOrderByClause("CREATE_TIME ASC");
                DemandExample.Criteria leafCriteria = leafExample.createCriteria();
                leafCriteria.andProjectIdEqualTo(qualityReportQuery.getProId());
                leafCriteria.andLogicDelEqualTo("N");
                leafCriteria.andLevelNoEqualTo(2);
                leafCriteria.andLeafEqualTo("Y");

                List<Demand> demandLeafList = demandMapper.selectByExample(leafExample);
                demandList.addAll(demandLeafList);

                if (demandList != null && demandList.size() > 0) {
                    for (int i = 0; i < demandList.size(); i++) {
                        Demand demand = demandList.get(i);
                        qualityReportQuery.setPath(demand.getPath());
                        DemandMapNodeDTO demandMapNodeDTO = cltMapNodeMapper.queryMapNodeOnModuleByTime(qualityReportQuery);
                        demandMapNodeDTO.setModule(demand.getName());
                        demandMapNodeDTOS.add(demandMapNodeDTO);
                    }

                }

                return demandMapNodeDTOS;
            }


        } catch (Exception e) {

            e.printStackTrace();

            logger.info("getdemandAnalyseData", e);
        }
        return null;


    }
}