package net.northking.cloudtest.service.impl.report;

import net.northking.cloudtest.assist.RedisUtil;
import net.northking.cloudtest.dao.analyse.CltMapNodeMapper;
import net.northking.cloudtest.dao.analyse.CltRoundMapper;
import net.northking.cloudtest.dao.analyse.DemandMapper;
import net.northking.cloudtest.dao.analyse.TestCaseMapper;
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
import net.northking.cloudtest.domain.testBug.*;
import net.northking.cloudtest.dto.*;
import net.northking.cloudtest.dto.report.DemandMapNodeDTO;
import net.northking.cloudtest.dto.report.TestCaseExecuteReportDTO;
import net.northking.cloudtest.dto.report.TestCaseReportDTO;
import net.northking.cloudtest.dto.testBug.TestBugDTO;
import net.northking.cloudtest.enums.CltBulletinCatalog;
import net.northking.cloudtest.enums.CltTestBugStatus;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.service.WeekReportService;
import net.northking.cloudtest.utils.WeekUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liujinghao on 2018/5/24.
 */
@Service
public class WeekReportServiceImpl implements WeekReportService {

    private final static Logger logger = LoggerFactory.getLogger(WeekReportServiceImpl.class);

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
    private CltProjectCountMapper cltProjectCountMapper;

    /**
     * 处理周报数据
     *
     * @param proId
     * @param dataMap
     * @throws Exception
     */
    @Override
    public void dealWithWeekReport(String proId, Map dataMap) throws Exception {
        //查出项目
        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(proId);
        WeekReportBulletin weekReportBulletin = new WeekReportBulletin();
        if (cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
            //查出客户
            CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
            weekReportBulletin.setCustName(cltCustomer.getCustName());
            weekReportBulletin.setProject(cltProject);

            //获取上一版本号
            CltReportExample reportExample = new CltReportExample();
            CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
            reportCriteria.andProjectIdEqualTo(proId);
            reportCriteria.andCatalogEqualTo(CltBulletinCatalog.TW.getCode());
            reportExample.setOrderByClause("CREATE_DATE DESC");
            List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
            if (cltReportList.size() == 0) {
                weekReportBulletin.setVersion("1.0");
            } else {
                weekReportBulletin.setVersion("1." + (cltReportList.get(0).getVersion() + 1));
            }

            //报告日期
            weekReportBulletin.setReportDate(new Date());

          /*  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");*/
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, -6);
            Date monday = c.getTime();
            //获取项目内的轮次信息
            List<CltRound> list = cltRoundMapper.selectRoundInfoByDate(proId, new Date());
            weekReportBulletin.setCltRound(list);

            //项目一开始周数

            long beginTime = cltProject.getCreateDate().getTime();

            long endTime = (new Date()).getTime();

            long betweenDays =(endTime - beginTime) / (1000 * 60 * 60 * 24);

            Integer numWeek = (int)(betweenDays / 7) + 1;

            System.out.println("项目处于第几周" +numWeek);

            dataMap.put("numWeek", numWeek);

            //判断项目执行所处的阶段
            CltProjectCount cltProjectCount=new CltProjectCount() ;
            cltProjectCount.setProId(proId);
            CltProjectCount clt=cltProjectCountMapper.selectByProId(cltProjectCount);
            JudgeProjectStatus judgeProjectStatus= new JudgeProjectStatus();
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
            weekReportBulletin.setJudgeProjectStatus(judgeProjectStatus);

            //缺陷生命周期
            CltTestBug ctb = new CltTestBug();
            ctb.setProId(proId);
          /*  DecimalFormat df = new DecimalFormat("#.00");*/
            if (cltTestBugMapper.selectAvgTestBugLife(ctb) != null) {
                Double ly = cltTestBugMapper.selectAvgTestBugLife(ctb).getLifeSycle();
                dataMap.put("testBugLife", String.format("%.2f", ly));
                Integer testNum = cltTestBugMapper.selectTestBugLifeCount(ctb).getNum();
                dataMap.put("testNum", testNum);
            }

            //本周需求完成情况(人员)
            QualityReportQuery query = new QualityReportQuery();
            query.setProId(proId);
            query.setStartDate(monday);
            query.setEndDate(new Date());
            List<DemandMapNodeDTO> userChart = getDemandAnalyseData(query, "U");
            DemandDataTotal dataTotal = new DemandDataTotal();
            Integer num = 0;
            Integer num1 = 0;
            Integer num2 = 0;
            Integer num3 = 0;
            for (DemandMapNodeDTO demandMapNodeDTO : userChart
                    ) {
                demandMapNodeDTO.setModifierName((String) redisUtil.get("username:" + demandMapNodeDTO.getLastModifier()));
                System.out.println("需求分析人员------" + demandMapNodeDTO.getModifierName());
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
            weekReportBulletin.setDemandByUserQualityByWeek(userChart);
            System.out.println("userChart=" + userChart);
            //项目至今需求完成情况(模块)
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
            weekReportBulletin.setDemandByModuleQualityByWeek(moduleChart);

            //测试用例设计完成情况(模块)
            QualityReportQuery qualityReport = new QualityReportQuery();
            qualityReport.setProId(proId);
            qualityReport.setStartDate(cltProject.getCreateDate());
            qualityReport.setEndDate(new Date());
            List<TestCaseReportDTO> testCaseReportModuleList = testCaseMapper.queryCaseReportByModuleByTime(qualityReport);
           /* for (TestCaseReportDTO testCaseReportDTO : testCaseReportModuleList
                    ) {
                System.out.println("测试");
            }*/
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
            dataMap.put("testCaseCountTotal1", testCaseCountTotal1);
            weekReportBulletin.setTestCaseByModuleQualityByWeek(testCaseReportModuleList);

            //本周测试用例完成情况(人员)
            QualityReportQuery qualityReportQuery = new QualityReportQuery();
            qualityReportQuery.setProId(proId);
            qualityReportQuery.setStartDate(monday);
            qualityReportQuery.setEndDate(new Date());
            //按人员
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
            }
            testCaseCountTotal.setSiTotal(caseNum);
            testCaseCountTotal.setSdTotal(caseNum1);
            testCaseCountTotal.setSsTotal(caseNum2);
            dataMap.put("TestCaseCountTotal", testCaseCountTotal);
            weekReportBulletin.setTestCaseByUserQuality(testCaseReportUserList);
            //缺陷总体情况
            CltTestBug cltTestBug = new CltTestBug();
            cltTestBug.setProId(proId);
            List<TestBugDTO> testBug = cltTestBugMapper.selectAllTestBugByProId(cltTestBug);
            TestBugCountNum testBugCountNum = new TestBugCountNum();
            Integer alValueTotal = 0;
            Integer blValueTotal = 0;
            Integer clValueTotal = 0;
            Integer dlValueTotal = 0;
            Integer elValueTotal = 0;
            Integer case1 = 0;
            Integer case2 = 0;
            Integer case3 = 0;
            Integer case4 = 0;
            Integer case5 = 0;
            Integer case6 = 0;
            Integer case7 = 0;
            Integer case8 = 0;
            Integer case9 = 0;
            Integer case10 = 0;
            Integer case11 = 0;
            Integer total = 0;
            for (TestBugDTO testBugDTO : testBug
                    ) {
                alValueTotal += testBugDTO.getCaseA();
                blValueTotal += testBugDTO.getCaseB();
                clValueTotal += testBugDTO.getCaseC();
                dlValueTotal += testBugDTO.getCaseD();
                elValueTotal += testBugDTO.getCaseE();
                total += testBugDTO.getCaseTotal();
                case1 += testBugDTO.getCase1();
                case2 += testBugDTO.getCase2();
                case3 += testBugDTO.getCase3();
                case4 += testBugDTO.getCase4();
                case5 += testBugDTO.getCase5();
                case6 += testBugDTO.getCase6();
                case7 += testBugDTO.getCase7();
                case8 += testBugDTO.getCase8();
                case9 += testBugDTO.getCase9();
                case10 += testBugDTO.getCase10();
                case11 += testBugDTO.getCase11();
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
            dataMap.put("testBugCountNum", testBugCountNum);
            weekReportBulletin.setTestBugAllQualityByWeek(testBug);
            //当周缺陷情况
            cltTestBug.setUpdateDate(new Date());
            cltTestBug.setCloseDate(monday);
            List<TestBugDTO> testBugDTOS = cltTestBugMapper.selectAllTestBugByProIdAndWeek(cltTestBug);
            TestBugCountNum testBugCountNum1 = new TestBugCountNum();
            Integer alValueCountTotal = 0;
            Integer blValueCountTotal = 0;
            Integer clValueCountTotal = 0;
            Integer dlValueCountTotal = 0;
            Integer elValueCountTotal = 0;
            Integer caseCount1 = 0;
            Integer caseCount2 = 0;
            Integer caseCount3 = 0;
            Integer caseCount4 = 0;
            Integer caseCount5 = 0;
            Integer caseCount6 = 0;
            Integer caseCount7 = 0;
            Integer caseCount8 = 0;
            Integer caseCount9 = 0;
            Integer caseCount10 = 0;
            Integer caseCount11 = 0;
            Integer totalCount = 0;
            for (TestBugDTO testBugDTO : testBug
                    ) {
                alValueCountTotal += testBugDTO.getCaseA();
                blValueCountTotal += testBugDTO.getCaseB();
                clValueCountTotal += testBugDTO.getCaseC();
                dlValueCountTotal += testBugDTO.getCaseD();
                elValueCountTotal += testBugDTO.getCaseE();
                totalCount += testBugDTO.getCaseTotal();
                caseCount1 += testBugDTO.getCase1();
                caseCount2 += testBugDTO.getCase2();
                caseCount3 += testBugDTO.getCase3();
                caseCount4 += testBugDTO.getCase4();
                caseCount5 += testBugDTO.getCase5();
                caseCount6 += testBugDTO.getCase6();
                caseCount7 += testBugDTO.getCase7();
                caseCount8 += testBugDTO.getCase8();
                caseCount9 += testBugDTO.getCase9();
                caseCount10 += testBugDTO.getCase10();
                caseCount11 += testBugDTO.getCase11();
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
            dataMap.put("testBugCountNum1", testBugCountNum1);
            weekReportBulletin.setTestBugQualityByWeek(testBugDTOS);

            //缺陷状态分布按模块
           /* TestBugQualityReportQuery testBugQualityReportQuery=new TestBugQualityReportQuery();
            testBugQualityReportQuery.setProId(proId);
            testBugQualityReportQuery.setTimeType("D");
            testBugQualityReportQuery.setType("M");
            testBugQualityReportQuery.setStartDate();
            testBugQualityReportQuery.setEndDate(new Date());
            List<CltStsProgress> cltStsProgresses = cltStsProgressMapper.queryTestBugNumStatusByModuleByDay(testBugQualityReportQuery);*/

            //存在的风险
            CltBugLogExample cltBugLogExample = new CltBugLogExample();
            CltBugLogExample.Criteria cltBugLogCriteria = cltBugLogExample.createCriteria();
            cltBugLogCriteria.andBugIdEqualTo(proId);
            cltBugLogExample.setOrderByClause("LOG_TIME DESC");
            List<CltBugLog> cltBugLogList = cltBugLogMapper.selectByExample(cltBugLogExample);
            weekReportBulletin.setCltBugLogList(cltBugLogList);
            dataMap.put("weekReportBulletin", weekReportBulletin);

            //查询拒绝，延迟状态缺陷
            List<String> statuss = new ArrayList<>();
            statuss.add(CltTestBugStatus.DELAY.getCode());
            statuss.add(CltTestBugStatus.REFUSE.getCode());

            CltTestBugExample cltTestBugExample = new CltTestBugExample();
            CltTestBugExample.Criteria cltTestBugExampleCriteria = cltTestBugExample.createCriteria();
            cltTestBugExampleCriteria.andProIdEqualTo(proId);
            cltTestBugExampleCriteria.andStatusIn(statuss);
            //cltTestBugExampleCriteria.andCreateDateLessThanOrEqualTo(new Date());

            cltTestBugExample.setOrderByClause("CREATE_DATE DESC");
            List<CltTestBug> cltTestBugList = cltTestBugMapper.selectByExample(cltTestBugExample);
            for(CltTestBug cltTestBug1 : cltTestBugList){
                if("1".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.NEW_STATE.getMsg());
                }else if("2".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.OPEN.getMsg());
                }else if("3".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.ALLOCATION.getMsg());
                }else if("4".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.REFUSE.getMsg());
                }else if("5".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.WAIT_VERIFY.getMsg());
                }else if("6".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.DONGING.getMsg());
                }else if("7".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.DELAY.getMsg());
                }else if("8".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.REPAIRED.getMsg());
                }else if("9".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.CLOSE.getMsg());
                }else if("10".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.INVALID.getMsg());
                }else if("11".equals(cltTestBug1.getStatus())){
                    cltTestBug1.setStatus(CltTestBugStatus.REOPEN.getMsg());
                }
            }
            dataMap.put("cltTestBugList", cltTestBugList);
        }
    }

    /**
     * 往数据库里面插入或更新数据
     *
     * @param proId
     * @param operatorUserId
     * @param docPath
     * @param swfPath
     * @return
     * @throws Exception
     */
    @Override
    public Integer insertOrUpdateWeekReportData(String proId, String operatorUserId, String docPath, String swfPath) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date lastModay = WeekUtil.getLastWeekMonday(new Date());
        Date lastSunday = WeekUtil.getLastWeekSunday(new Date());
        String preDate = sdf.format(lastModay); //上周一
        String lastSundayStr = sdf.format(lastSunday); //上周日

        int result = 0;
        if (operatorUserId == null) operatorUserId = "system";
        CltReportExample reportExample = new CltReportExample();
        CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
        reportCriteria.andProjectIdEqualTo(proId);
        reportCriteria.andCatalogEqualTo(CltBulletinCatalog.TW.getCode());
        reportCriteria.andReportDateGreaterThanOrEqualTo(lastModay);
        reportCriteria.andReportDateLessThan(new Date());
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
                cltReport.setReportName(cltProject.getProName() + preDate + "至" + lastSundayStr + "测试周报");
                cltReport.setCatalog(CltBulletinCatalog.TW.getCode());
                cltReport.setReportDate(lastModay);
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

