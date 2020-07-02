package net.northking.cloudtest.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import net.northking.cloudtest.domain.analyse.*;
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
import net.northking.cloudtest.domain.task.CltTestcaseExecuteExample;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.domain.testBug.CltBugLogExample;
import net.northking.cloudtest.domain.user.CltRole;
import net.northking.cloudtest.dto.*;
import net.northking.cloudtest.dto.analyse.CltBatchDTO;
import net.northking.cloudtest.dto.analyse.CltRoundDTO;
import net.northking.cloudtest.dto.report.*;
import net.northking.cloudtest.dto.report.TestCasePass;
import net.northking.cloudtest.dto.user.UserCount;
import net.northking.cloudtest.enums.CltBulletinCatalog;
import net.northking.cloudtest.enums.CltStsProgressCatalog;
import net.northking.cloudtest.enums.CltTestCaseStatus;
import net.northking.cloudtest.enums.CltTestStatus;
import net.northking.cloudtest.feign.report.ProgressReportFeignClient;
import net.northking.cloudtest.feign.user.RoleFeignClient;
import net.northking.cloudtest.query.analyse.DemandQuery;
import net.northking.cloudtest.query.report.ProgressReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.*;
import net.northking.cloudtest.utils.BeanUtil;
import net.northking.cloudtest.utils.CltUtils;
import net.northking.cloudtest.utils.WordUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Title: 设计阶段报告处理服务
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/11
 * @UpdateUser:
 * @Version:0.1
 */

@Service
public class DesignBulletinServiceImpl implements DesignBulletinService {

    private final static Logger logger = LoggerFactory.getLogger(DesignBulletinServiceImpl.class);

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
    private CltRoundMapper cltRoundMapper;

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
    private RoleFeignClient roleFeignClient;

    @Autowired
    private ProgressReportFeignClient progressReportFeignClient;

    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;

    @Autowired
    private GenTestTeamService genTestTeamService;

    @Autowired
    private GenCoverageChartReport genCoverageChartReport;
    @Autowired
    private GenChartService genChartService;
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
        genChartService.genChart(projectId);
//        //查出项目
//        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);
//
//        //测试准出准则
//        CltProjectCount cltProjectCount = null;
//        CltProjectCountExample projectCountExample = new CltProjectCountExample();
//        CltProjectCountExample.Criteria projectCountCriteria = projectCountExample.createCriteria();
//        projectCountCriteria.andProIdEqualTo(projectId);
//        List<CltProjectCount> cltProjectCountList = cltProjectCountMapper.selectByExample(projectCountExample);
//        if(cltProjectCountList.size()<=0){
//            return num;
//        }
//
//        cltProjectCount = cltProjectCountList.get(0);
//
//        Runtime run = Runtime.getRuntime();
//
//        //查询条件
//        ProgressReportQuery progressReportQuery = new ProgressReportQuery();
//        progressReportQuery.setProId(projectId);
//        progressReportQuery.setDemandStartDate(cltProjectCount.getDemandStartDate());
//        progressReportQuery.setDemandEndDate(cltProjectCount.getDemandEndDate());
//        progressReportQuery.setProjectStartDate(cltProject.getTestPlanStartTime());
//        progressReportQuery.setProjectEndDate(cltProject.getTestPlanEndTime());
//
//        //项目总体报告
//        ResultInfo<ProgressReportDTO> totalResultInfo =  progressReportFeignClient.projectProgressReportByTotal(progressReportQuery);
//        if(totalResultInfo!=null && totalResultInfo.getData() != null) {
//            WordUtil wordUtil = new WordUtil();
//            Map<String, Object> dataMap = new HashMap<String, Object>();
//            dataMap.put("title", "项目总体进度跟踪表");
//
//            List<String> xAxis = totalResultInfo.getData().getxAxis();
//            List<String> yAxis = new ArrayList<>();
//            List<String> demandyAxis = null;
//            int demandPlanNum = 0;
//            List<String> testcaseyAxis = null;
//            int testcasePlanNum = 0;
//            List<String> executeyAxis = null;
//            int executePlanNum = 0;
//
//            List<ProjectProgressReportData> projectDataList = totalResultInfo.getData().getProjectData();
//            for(ProjectProgressReportData progressReportData : projectDataList){
//                if(progressReportData.getName().equals("需求分析阶段")){
//                    demandyAxis = progressReportData.getyAxis();
//                    demandPlanNum = progressReportData.getPlanNum();
//                }else if(progressReportData.getName().equals("用例设计阶段")){
//                    testcaseyAxis = progressReportData.getyAxis();
//                    testcasePlanNum = progressReportData.getPlanNum();
//                }else{
//                    executeyAxis = progressReportData.getyAxis();
//                    executePlanNum = progressReportData.getPlanNum();
//                }
//            }
//
//            dataMap.put("xAxis", listChangeToString(xAxis));
//
//            /**
//             实际进度:=(n50.5 + n70.7 + n8 0.8 + n101)/计划数量
//             计划进度::=100%/(结束时间-开始时间+1)*(x轴时间-开始时间)
//             */
//            dataMap.put("yAxisActual", actualTotalProgress(demandyAxis, demandPlanNum, testcaseyAxis, testcasePlanNum, executeyAxis, executePlanNum));
//            dataMap.put("yAxisPlan", planProgress(xAxis, totalResultInfo.getData().getStartDate(), totalResultInfo.getData().getEndDate()));
//            wordUtil.createWord("progress.json", preFilePath + projectId + "/json/totalProgress.json", dataMap);
//
//            String demandProgressCmd = "node " + outputJsPath + " " + preFilePath + projectId + "/json/totalProgress.json" + preFilePath + projectId + "/images/totalProgress.png";
//
//            try {
//                Process p = run.exec(demandProgressCmd);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            num ++;
//        }
//
//
//        //需求分析阶段进度表
//        ResultInfo<ProgressReportDTO> progressReportDTOResultInfo =  progressReportFeignClient.analyseProgressReportByDay(progressReportQuery);
//
//        if(progressReportDTOResultInfo!=null && progressReportDTOResultInfo.getData() != null) {
//            WordUtil wordUtil = new WordUtil();
//            Map<String, Object> dataMap = new HashMap<String, Object>();
//            dataMap.put("title", "需求分析阶段进度跟踪表");
//
//            List<String> xAxis = progressReportDTOResultInfo.getData().getxAxis();
//            List<String> yAxis = progressReportDTOResultInfo.getData().getyAxis();
//            dataMap.put("xAxis", listChangeToString(xAxis));
//
//            /**
//             实际进度:=(n50.5 + n70.7 + n8 0.8 + n101)/计划数量
//             计划进度::=100%/(结束时间-开始时间+1)*(x轴时间-开始时间)
//             */
//            dataMap.put("yAxisActual", actualProgress(yAxis, progressReportDTOResultInfo.getData().getPlanNum()));
//            dataMap.put("yAxisPlan", planProgress(xAxis, progressReportDTOResultInfo.getData().getStartDate(), progressReportDTOResultInfo.getData().getEndDate()));
//            wordUtil.createWord("progress.json", preFilePath + projectId + "/json/demandProgress.json", dataMap);
//
//            String demandProgressCmd = "node " + outputJsPath + " " + preFilePath + projectId + "/json/demandProgress.json" + preFilePath + projectId + "/images/demandProgress.png";
//
//            try {
//                Process p = run.exec(demandProgressCmd);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            num ++;
//        }


        //生成团队人员表
        int i = genTestTeamService.genTestTeam(projectId);
        num+=i;
        //生成覆盖率分析表
        int i1 = genCoverageChartReport.genCoverageChart(projectId);
        num+=i1;

        return num;
    }

    @Override
    public void dealWithReport(String projectId, Map dataMap) throws Exception {
        //用于存放word数据
        DesignBulletin designBulletin = new DesignBulletin();

        //查出项目
        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);

        if(cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
            //查出客户
            CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
            designBulletin.setCustName(cltCustomer.getCustName());
            designBulletin.setProject(cltProject);

            //获取上一版本号
            CltReportExample reportExample = new CltReportExample();
            CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
            reportCriteria.andProjectIdEqualTo(projectId);
            reportCriteria.andCatalogEqualTo(CltBulletinCatalog.CD.getCode());
            reportExample.setOrderByClause("CREATE_DATE DESC");
            List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
            if(cltReportList.size()==0){
                designBulletin.setVersion("1.0");
            }else{
                designBulletin.setVersion("1." + (cltReportList.get(0).getVersion() + 1));
            }

            //报告日期
            designBulletin.setReportDate(new Date());

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

            designBulletin.setDemandMudelNum(demandMudelNum);

            //分析交易数量
            DemandExample demandTransExample = new DemandExample();
            DemandExample.Criteria demandTransCriteria = demandTransExample.createCriteria();
            demandTransCriteria.andProjectIdEqualTo(projectId);
            demandTransCriteria.andLeafEqualTo("Y");
            demandTransCriteria.andLogicDelEqualTo("N");
            int demandTransNum = demandMapper.countByExample(demandTransExample);
            designBulletin.setDemandTransNum(demandTransNum);

            //获取项目内的用例数
            TestCaseExample testCaseExample = new TestCaseExample();
            TestCaseExample.Criteria testCaseCriteria = testCaseExample.createCriteria();
            testCaseCriteria.andProjIdEqualTo(projectId);
            testCaseCriteria.andLogicDelEqualTo("N");
            int countCaseNum = testCaseMapper.countByExample(testCaseExample);
            designBulletin.setCountCaseNum(countCaseNum);

            //未设计用例数
            TestCaseExample testCaseIExample = new TestCaseExample();
            TestCaseExample.Criteria testCaseICriteria = testCaseIExample.createCriteria();
            testCaseICriteria.andProjIdEqualTo(projectId);
            testCaseICriteria.andLogicDelEqualTo("N");
            testCaseICriteria.andStatusEqualTo(CltTestStatus.INIT.getCode());
            int countCaseINum = testCaseMapper.countByExample(testCaseIExample);
            designBulletin.setCountCaseINum(countCaseINum);

            //设计中用例数
            TestCaseExample testCaseDExample = new TestCaseExample();
            TestCaseExample.Criteria testCaseDCriteria = testCaseDExample.createCriteria();
            testCaseDCriteria.andProjIdEqualTo(projectId);
            testCaseDCriteria.andLogicDelEqualTo("N");
            testCaseDCriteria.andStatusEqualTo(CltTestStatus.DESIGN.getCode());
            int countCaseDNum = testCaseMapper.countByExample(testCaseDExample);
            designBulletin.setCountCaseDNum(countCaseDNum);

            //已完成用例数
            TestCaseExample testCaseSExample = new TestCaseExample();
            TestCaseExample.Criteria testCaseSCriteria = testCaseSExample.createCriteria();
            testCaseSCriteria.andProjIdEqualTo(projectId);
            testCaseSCriteria.andLogicDelEqualTo("N");
            testCaseSCriteria.andStatusEqualTo(CltTestStatus.FINISH_CASE.getCode());
            int countCaseSNum = testCaseMapper.countByExample(testCaseSExample);
            designBulletin.setCountCaseSNum(countCaseSNum);

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
            designBulletin.setTreeList(treeList);

            //按模块统计测试用例状态
            List<TestCaseReportDTO> testCaseReportDTOList = testCaseMapper.queryCaseReportByProj(projectId);
            designBulletin.setTestCaseReportDTOList(testCaseReportDTOList);

            TestCaseReportDTO testCaseDTOTotal = new TestCaseReportDTO();
            testCaseDTOTotal.setSi(0);
            testCaseDTOTotal.setSd(0);
            testCaseDTOTotal.setSs(0);
            for(TestCaseReportDTO dto : testCaseReportDTOList){
                testCaseDTOTotal.setSi(testCaseDTOTotal.getSi() + dto.getSi());
                testCaseDTOTotal.setSd(testCaseDTOTotal.getSd() + dto.getSd());
                testCaseDTOTotal.setSs(testCaseDTOTotal.getSs() + dto.getSs());
            }
            designBulletin.setTestCaseDTOTotal(testCaseDTOTotal);

            //测试准出准则
            CltProjectCountExample projectCountExample = new CltProjectCountExample();
            CltProjectCountExample.Criteria projectCountCriteria = projectCountExample.createCriteria();
            projectCountCriteria.andProIdEqualTo(projectId);
            List<CltProjectCount> cltProjectCountList = cltProjectCountMapper.selectByExample(projectCountExample);
            if(cltProjectCountList.size()>0){
                designBulletin.setCltProjectCount(cltProjectCountList.get(0));
            }


            //存在的风险
            CltBugLogExample cltBugLogExample = new CltBugLogExample();
            CltBugLogExample.Criteria cltBugLogCriteria = cltBugLogExample.createCriteria();
            cltBugLogCriteria.andBugIdEqualTo(projectId);
            cltBugLogExample.setOrderByClause("LOG_TIME DESC");
            List<CltBugLog> cltBugLogList = cltBugLogMapper.selectByExample(cltBugLogExample);
            designBulletin.setCltBugLogList(cltBugLogList);

            dataMap.put("designBulletin", designBulletin);


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
    public Integer insertOrUpdateData(String projectId, String operatorUserId, String docPath, String swfPath) throws Exception{
        int result = 0;
        if(operatorUserId == null) operatorUserId = "system";
        CltReportExample reportExample = new CltReportExample();
        CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
        reportCriteria.andProjectIdEqualTo(projectId);
        reportCriteria.andCatalogEqualTo(CltBulletinCatalog.CD.getCode());
        reportExample.setOrderByClause("CREATE_DATE DESC");
        List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
        if(cltReportList.size()>0){ //修改
            CltReport cltReport = cltReportList.get(0);
            cltReport.setUpdateDate(new Date());
            cltReport.setUpdateUser(operatorUserId);
            cltReport.setDocPath(docPath);
            cltReport.setSwfPath(swfPath);
            cltReport.setVersion(cltReport.getVersion() + 1);
            result = cltReportMapper.updateByPrimaryKeySelective(cltReport);
        }else{ //新增
            //查出项目
            CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);
            if(cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
                //查出客户
                CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
                CltReport cltReport = new CltReport();
                cltReport.setCustomId(cltCustomer.getCustId());
                cltReport.setProjectId(projectId);
                cltReport.setReportName(cltProject.getProName() + "用例设计阶段测试报告");
                cltReport.setCatalog(CltBulletinCatalog.CD.getCode());
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
            String demainActualValue = demandylist.get(i);
            String caseActualValue = caseylist.get(i);
            String excActualValue = executeylist.get(i);
            logger.info("actualValue:" + demainActualValue);
            if(StringUtils.isNotEmpty(demainActualValue)){
                Map resultToMap = JSON.parseObject(demainActualValue);

                double demandValue = (Integer.parseInt((String)resultToMap.get("n5"))*0.5 + Integer.parseInt((String)resultToMap.get("n7"))*0.7 + Integer.parseInt((String)resultToMap.get("n8"))*0.8 + Integer.parseInt((String)resultToMap.get("n10")))/demandPlanNum;
                double caseValue = 0;
                double executeValue = 0;
                if(StringUtils.isNotEmpty(caseActualValue)){
                    Map resultToMapTemp = JSON.parseObject(caseActualValue);
                    caseValue = Integer.parseInt((String)resultToMapTemp.get("total"))/casePlanNum;
                }
                if(StringUtils.isNotEmpty(excActualValue)){
                    Map resultToMapTemp = JSON.parseObject(excActualValue);
                    executeValue = Integer.parseInt((String)resultToMapTemp.get("total"))/executePlanNum;
                }

                actualValues[i] = demandValue*0.22 + caseValue*0.25 + executeValue*0.53;
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
        for (int i = 0; i < xlist.size(); i++) {
            try {
                planValues[i] = (100 / (daysBetween(startDate, endDate))) * daysBetween(startDate, xlist.get(i))*1.0;
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
