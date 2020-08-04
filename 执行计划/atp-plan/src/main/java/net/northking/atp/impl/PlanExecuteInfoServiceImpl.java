package net.northking.atp.impl;

import com.github.pagehelper.PageHelper;
import io.micrometer.core.instrument.util.StringUtils;
import net.northking.atp.CaseDesignFeignClient;
import net.northking.atp.DistributedLockFeignClient;
import net.northking.atp.db.dao.RePlanExecInfoDao;
import net.northking.atp.db.dao.RuEngineJobDao;
import net.northking.atp.db.enums.RuEngineJobStatus;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.*;
import net.northking.atp.enums.ExecuteResult;
import net.northking.atp.enums.LockStatus;
import net.northking.atp.mq.RabbitMQEndpoint;
import net.northking.atp.producer.PlanExecInfoProducer;
import net.northking.atp.producer.TaskBugProducer;
import net.northking.atp.producer.TestReportProducer;
import net.northking.atp.service.ExecPlanService;
import net.northking.atp.service.PlanExecuteInfoService;
import net.northking.atp.utils.LockEntityUtil;
import net.northking.atp.utils.UUIDUtil;
import net.northking.db.DefaultPagination;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.*;


/**
 * 执行记录相关服务实现类
 *
 * @author jieying.li
 */
@Service
public class PlanExecuteInfoServiceImpl implements PlanExecuteInfoService {

    private static final Logger logger = LoggerFactory.getLogger(PlanExecuteInfoServiceImpl.class);

    @Autowired
    private CaseDesignFeignClient caseDesignFeignClient;
    @Autowired
    private ReExecPlanService reExecPlanService;
    @Autowired
    private ReExecPlanCaseSetRelService reExecPlanCaseSetRelService;
    @Autowired
    private HisExecPlanCaseSetRelService hisExecPlanCaseSetRelService;
    @Autowired
    private RePlanExecInfoDao rePlanExecInfoDao;
    @Autowired
    private RePlanExecInfoService rePlanExecInfoService;
    @Autowired
    private RuEngineJobService ruEngineJobService;  // 引擎任务
    @Autowired
    private RuEngineJobDao ruEngineJobDao;
    @Autowired
    private RuJobProService ruJobProService;
    @Autowired
    private RuJobProLogService ruJobProLogService;
    @Autowired
    private TestReportProducer testReportProducer;
    @Autowired
    private PlanExecInfoProducer planExecInfoProducer;
    @Autowired
    private RePlanTriggerRelService rePlanTriggerRelService;
    @Autowired
    private ExecPlanService execPlanService;
    @Autowired
    private RuJobProParamService ruJobProParamService;
    @Autowired
    private RuJobProAttachmentService ruJobProAttachmentService;
    @Autowired
    private RePlanEnvRelService rePlanEnvRelService;
    @Autowired
    private HisRunEnvInfoService hisRunEnvInfoService;
    @Autowired
    private ReTestEnvInfoService reTestEnvInfoService;
    @Autowired
    private TaskBugProducer taskBugProducer;
    @Autowired
    private DistributedLockFeignClient distributedLockFeignClient;

    @Value("${atp.bug.auto-submit:true}")
    private boolean bugAutoSubmit;

    public final static String DEBUG_PROJECT_ID = "DEBUG01";

    @Value("${atp.attach.url.ip:192.168.0.130}")
    private String attachUrlIp;

    @Value("${atp.attach.url.port:9631}")
    private String attachUrlPort;

    @Value("${atp.attach.url.path:/download/}")
    private String attachUrlPath;

    @Value("${atp.report.url:http://192.168.0.130:8082}")
    private String testReportUrl;

    // rabbitMQ服务
    @Autowired
    private RabbitMQEndpoint rabbitMQEndpoint;

    // 执行报告队列名称
    @Value("${atp.mq.queue.plan-report:Q.plan.report}")
    private String reportQue;

    /**
     * 生成计划执行记录和测试记录
     *
     * @param planId 执行计划主键id
     * @return 执行计划记录信息
     */
    @Override
    public RePlanExecInfo doPlanExecute(String planId) {
        ReExecPlan execPlan = reExecPlanService.findByPrimaryKey(planId);
        return doPlanExecute(planId, execPlan.getProjectId());
    }

    /**
     * 生成计划执行记录和测试记录
     * 将相关信息转移到执行引擎相关表中
     * 计划 -> 案例集 -> 案例 -> 步骤 -> （高级组件 ->） 组件 -> 参数
     * 1. 生成执行记录
     * 用例信息case -> 引擎任务job （一条用例信息生成一个任务）
     * 3. 步骤、组件、参数信息 -> 任务步骤、参数信息
     *
     * @param planId 执行计划主键id
     * @return 执行计划记录信息
     */
    @Override
    @Transactional
    public RePlanExecInfo doPlanExecute(String planId, String runProjectId) {
        ReExecPlan execPlan = reExecPlanService.findByPrimaryKey(planId);


        // 获取所有被关联的测试案例，检测测试计划是否有可执行的用例
        List<CaseAndSetDetails> caseAndSetList = null;
        if(StringUtils.isNotEmpty(execPlan.getDomainType()) && execPlan.getDomainType().equals("1")) { //数据测试
            caseAndSetList = rePlanExecInfoDao.queryDataCasesByPlanId(planId);
            for(CaseAndSetDetails caseAndSetDetail : caseAndSetList) {
                logger.info("caseAndset::::::::::::::" + caseAndSetDetail.getReCaseDesignInfo().getId());
            }
        }else{
            caseAndSetList = rePlanExecInfoDao.queryCasesByPlanId(planId);
        }

        if (caseAndSetList == null || caseAndSetList.size() == 0) {
            throw new RuntimeException("当前测试计划没有关联测试案例集，\r\n或用例集中没有关联测试案例。");
        }


        // 生成执行记录
        RePlanExecInfo planExecInfo = addPlanExecInfo(planId, execPlan);
        if (planExecInfo == null) {
            throw new RuntimeException("连接超时，无法生成执行记录，请稍后重试");
        }
        // todo: 添加计划-用例集关联历史信息 改成 消息
        addPlanCaseSetRelToHis(planId, planExecInfo);
        // todo: 添加用例集-用例关联历史信息
//        addCaseSetToHis(planId);
//        Map<String, Object> planMap = new HashMap<>();
//        planMap.put(RePlanExecInfo.KEY_id, planExecInfo.getId());
//        planMap.put(InterfaceCaseSetLink.KEY_runProjectId, runProjectId);
        // 将信息推送到队列等待处理
        /*************** 判断是数据测试的执行计划还是数据测试的执行计划 add by hcs ***********/
        if(StringUtils.isNotEmpty(execPlan.getDomainType()) && execPlan.getDomainType().equals("1")){ //数据测试
            planExecInfoProducer.pushDataExecInfoToQueue(planExecInfo, runProjectId, caseAndSetList);
        }else { //自动化测试
            // 将信息推送到队列等待处理
            planExecInfoProducer.pushExecInfoToQueue(planExecInfo, runProjectId, caseAndSetList);
//            planExecInfoProducer.pushExecInfoToQueue(planMap, caseAndSetList);
        }
        // 返回执行记录
        return planExecInfo;
    }

    /**
     * 添加用例集的历史记录
     *
     * @param planId
     */
    private void addCaseSetToHis(String planId) {
        List<ReCaseSet> caseSetList = (List<ReCaseSet>) caseDesignFeignClient.queryAllCaseSetsByPlanId(planId).getData();
        ArrayList<HisCaseSet> hisCaseSetList = new ArrayList<>();
        // 将用例集数据搬运到HIS_CASE_SET表
        caseSetList.forEach(caseSet -> {
            HisCaseSet hisCaseSet = new HisCaseSet();
            hisCaseSet.setId(UUIDUtil.getUUIDWithoutDash());    // id
            hisCaseSet.setSetNo(caseSet.getSetNo());            // 用例集编号
            hisCaseSet.setSetName(caseSet.getSetName());        // 用例集名称
            hisCaseSet.setDescription(caseSet.getDescription());// 用例集描述
            hisCaseSet.setModifyTime(new Date().toString());    // 修改时间
            hisCaseSet.setSetStatus(caseSet.getSetStatus());    // 用例集状态
            hisCaseSet.setModifyName(caseSet.getSetName());     // 修改人
            hisCaseSetList.add(hisCaseSet);
        });
        // 插入用例集记录
        caseDesignFeignClient.addHisCaseSetsByBatch(hisCaseSetList);
    }

    /**
     *
     * @param planId
     * @param planExecInfo
     */
    private void addPlanCaseSetRelToHis(String planId, RePlanExecInfo planExecInfo) {
        /*  计划-案例集关联关系 */
        ReExecPlanCaseSetRel planCaseSetRelQuery = new ReExecPlanCaseSetRel();
        planCaseSetRelQuery.setPlanId(planId);
        List<ReExecPlanCaseSetRel> planSetRelList = reExecPlanCaseSetRelService.query(planCaseSetRelQuery);
        List<HisExecPlanCaseSetRel> hisPlanSetRels = new ArrayList<>();
        planSetRelList.forEach(rel -> {
            HisExecPlanCaseSetRel hisRel = new HisExecPlanCaseSetRel();
            hisRel.setId(UUIDUtil.getUUIDWithoutDash());
            hisRel.setCaseSetId(rel.getCaseSet());
            hisRel.setPlanId(planId);
            hisRel.setPlanExecId(planExecInfo.getId());
            hisRel.setCreateTime(new Date());
            hisPlanSetRels.add(hisRel);
        });
        hisExecPlanCaseSetRelService.insertByBatch(hisPlanSetRels);
    }

    /**
     * 添加执行记录信息
     * 2019.08.15 增加等待锁，统计执行次数
     *
     * @param planId    执行计划id
     * @param execPlan  执行计划信息
     * @return
     */
    private RePlanExecInfo addPlanExecInfo(String planId, ReExecPlan execPlan) {
//        RequestWrapper requestWrapper = LockEntityUtil.getRequestWrapper("addPlanExecInfo", "10");
//        ResultWrapper resultWrapper = distributedLockFeignClient.lock(requestWrapper);
        RePlanExecInfo planExecInfo = new RePlanExecInfo();     // 执行记录对象
        planExecInfo.setId(UUIDUtil.getUUIDWithoutDash());      // 执行记录主键id
        planExecInfo.setPlanName(execPlan.getPlanName());       // 执行记录名称
        planExecInfo.setPlanId(planId);                         // 执行计划id
        planExecInfo.setProfileId(execPlan.getProfileId());     // 环境
        planExecInfo.setExecBeginTime(new Date());              // 执行开始时间
        planExecInfo.setProjectId(execPlan.getProjectId());     // 项目编号
        // 查询条件
        RePlanExecInfo execInfoRecord = new RePlanExecInfo();
        execInfoRecord.setPlanId(planId);
        long planExecCount = rePlanExecInfoService.queryCount(execInfoRecord);
        planExecInfo.setExecIdx((int) (planExecCount + 1));     // 执行次数 （count + 1）次
        // 添加关联的测试环境
        addRunTestEnv(planExecInfo);
        // 插入计划测试记录
        rePlanExecInfoService.insert(planExecInfo);
        return planExecInfo;
//        if (LockStatus.SUCCESS.code().equals(resultWrapper.getResult())) {
//        }
//        return null;
    }

    /**
     * 添加测试记录的环境信息关联
     *
     * @param planExecInfo
     */
    private void addRunTestEnv(RePlanExecInfo planExecInfo) {
        RePlanEnvRel relQuery = new RePlanEnvRel();
        relQuery.setPlanId(planExecInfo.getPlanId());
        List<RePlanEnvRel> resultRels = rePlanEnvRelService.query(relQuery);
        if (resultRels != null && resultRels.size() > 0) {
            for (RePlanEnvRel resultRel : resultRels) {
                // 获取测试环境信息
                TestEnvInfo envInfo = reTestEnvInfoService.findTestEnvInfoById(resultRel.getEnvId());
                HisRunEnvInfo hisRunEnvInfo = new HisRunEnvInfo();
                hisRunEnvInfo.setId(UUIDUtil.getUUIDWithoutDash());
                hisRunEnvInfo.setPlanId(planExecInfo.getPlanId());
                hisRunEnvInfo.setPlanExecId(planExecInfo.getId());
                hisRunEnvInfo.setProjectId(planExecInfo.getProjectId());
                hisRunEnvInfo.setEnvId(envInfo.getId());
                hisRunEnvInfo.setEnvType(envInfo.getEnvType());
                hisRunEnvInfo.setOsInfoId(envInfo.getReOsInfo().getId());
                hisRunEnvInfo.setOsName(envInfo.getReOsInfo().getOsName());
                hisRunEnvInfo.setBwInfoId(envInfo.getBwInfoId());
                hisRunEnvInfo.setBwName(envInfo.getReBrowserInfo().getBwName());
                hisRunEnvInfo.setCreateTime(new Date());
                hisRunEnvInfo.setEnvArea(resultRel.getEnvArea());
                hisRunEnvInfoService.insert(hisRunEnvInfo);
            }
        }
    }

    /**
     * 定时任务实现：
     * 根据任务引擎的所有任务状态更新执行记录的状态
     */
    @Override
    public void updateExecuteInfo() {
        // 查询EXEC_STATUS执行状态不为Finished
        logger.debug("查询EXEC_STATUS执行状态不为Finished ========= 查询开始");
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(RePlanExecInfo.KEY_execStatus, RuEngineJobStatus.Finished.getCode());
//        queryMap.put(RePlanExecInfo.KEY_projectId, DEBUG_PROJECT_ID);
        List<RePlanExecInfo> execInfos = rePlanExecInfoDao.queryInfoStatusIsNotFinished(queryMap);
        logger.debug("当前共有{}条记录未完成", execInfos.size());
        // 检查未完成的任务下的所有任务步骤执行情况
        for (RePlanExecInfo execInfo : execInfos) {
            updateExecInfoStatus(execInfo);
            RePlanExecInfo postInfo = rePlanExecInfoService.findByPrimaryKey(execInfo.getId());
            String execResult = postInfo.getExecResult();
            // 根据执行记录的执行结果（成功|失败）生成测试报告，使用mq方式进行

            if (ExecuteResult.FAILURE.code().equals(execResult) || ExecuteResult.SUCCESS.code().equals(execResult)) {
                logger.debug("执行记录：{}_{}，执行结果：{}", postInfo.getPlanName(), postInfo.getExecIdx(), postInfo.getExecResult());
                if (DEBUG_PROJECT_ID.equals(execInfo.getProjectId())) {
                    return;
                }
                // 是否需要自动提交断言失败的缺陷
                logger.debug("【bugAutoSubmit】 -> {}", bugAutoSubmit);
                if (bugAutoSubmit) {
                    taskBugProducer.pushFailExecInfoToBugQueue(postInfo.getId());
                }
                try {
//                    testReportProducer.pushReoprtInfoToQueue(execInfo.getId());
                    logger.info("测试报告{}进入队列中，正等待生成文件", execInfo.getId());
                    rabbitMQEndpoint.sendByText(reportQue, execInfo.getId());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        logger.debug("查询EXEC_STATUS执行状态不为Finished ========= 查询结束");
    }

    /**
     * 更新执行记录得状态和结果
     *
     * @param execInfo 执行记录信息
     */
    @Transactional
    public void updateExecInfoStatus(RePlanExecInfo execInfo) {
//        logger.debug("-->正在扫描{}计划任务执行情况", execInfo.getPlanName());
        String execStatus;
        Map<String, Long> statusMap = ruEngineJobDao.countExecStatusByPlanExecId(execInfo.getId());
        Long total = statusMap.get("Total");
        Long initial = statusMap.get(RuEngineJobStatus.Initial.getCode());
        Long finished = statusMap.get(RuEngineJobStatus.Finished.getCode());
        if (total == 0) {
            //  没有可执行的任务，不进行状态/结果修改
//            logger.debug("<--执行计划：{}，第{} 次执行，当前计划没有可执行的任务，不进行状态修改", execInfo.getPlanName(), execInfo.getExecIdx());
            // 由于没有可执行完成，将状态改成 完成，执行结果改成成功
            RePlanExecInfo pi = new RePlanExecInfo();
            pi.setId(execInfo.getId());
            pi.setExecStatus(RuEngineJobStatus.Finished.getCode()); // 完成
            pi.setExecResult(ExecuteResult.SUCCESS.code()); // 成功
            return;
        }
        if (finished.longValue() == total.longValue()) {
            // 任务全部执行完毕
            execStatus = RuEngineJobStatus.Finished.getCode();
            // 处理结果
        } else if (initial.longValue() == total.longValue()) {
            // 任务未开始，处于初始化
            execStatus = RuEngineJobStatus.Initial.getCode();
        } else {
            // 任务正在被执行
            execStatus = RuEngineJobStatus.Executing.getCode();
        }
        RePlanExecInfo condition = new RePlanExecInfo();
        condition.setId(execInfo.getId());
        condition.setExecStatus(execStatus);
        // 更新执行状态
        rePlanExecInfoService.updateByPrimaryKey(condition);
        // 更新执行结果
        updateExecuteResult(execInfo, execStatus);

    }

    /**
     * 根据执行执行状态更新执行任务的执行结果
     *
     * @param execInfo   执行任务信息
     * @param execStatus 执行任务状态
     * @return 执行任务结果
     */
    private String updateExecuteResult(RePlanExecInfo execInfo, String execStatus) {
        Map<String, Long> execResultMap = ruEngineJobDao.countExecResultByPlanExecId(execInfo.getId());
        Long fail = execResultMap.get(ExecuteResult.FAILURE.code());
        // 执行结果 -> 默认：未执行
        String executeResult;
        RePlanExecInfo updateResultInfo = new RePlanExecInfo();
        updateResultInfo.setId(execInfo.getId());
        if (RuEngineJobStatus.Finished.getCode().equals(execStatus)) {
            updateResultInfo.setExecEndTime(new Date());    // 计划完成时间
            if (fail > 0L) {
                executeResult = ExecuteResult.FAILURE.code();
            } else {
                executeResult = ExecuteResult.SUCCESS.code();
            }
        } else if (RuEngineJobStatus.Executing.getCode().equals(execStatus)) {
            executeResult = ExecuteResult.RUNNING.code();
        } else {
            executeResult = ExecuteResult.NO_RUN.code();
        }
        updateResultInfo.setExecResult(executeResult);  // 执行结果
        rePlanExecInfoService.updateByPrimaryKey(updateResultInfo);
        return executeResult;
    }


    /**
     * 根据执行记录id查询记录详情
     *
     * @param planExecId 执行记录id
     * @return 执行记录信息
     */
    @Override
    public PlanExecInfo queryPlanExecInfo(String planExecId) {

        RePlanExecInfo sourceInfo = rePlanExecInfoService.findByPrimaryKey(planExecId);
        // 执行记录详情对象
        PlanExecInfo planExecInfo = new PlanExecInfo();
        BeanUtils.copyProperties(sourceInfo, planExecInfo);
        if (planExecInfo.getReportUrl() != null && !planExecInfo.getReportUrl().startsWith("http")) {
            planExecInfo.setReportUrl(testReportUrl + "/" + planExecInfo.getReportUrl());
        }
        // 执行结果统计
        Map<String, Long> resultMap = ruEngineJobDao.countExecResultByPlanExecId(planExecId);
        planExecInfo.setResultMap(resultMap);
        return planExecInfo;
    }

    /**
     * 分页查询引擎执行任务
     *
     * @param query 查询条件
     * @return 执行任务信息
     */
    @Override
    public Pagination<EngineJob> queryEngineJobsByPage(Map<String, Object> query) {
        logger.debug("分页查询引擎任务");
        logger.debug("查询参数-----> " + query);
        Integer pageNo = (Integer) query.get("pageNo");
        if (pageNo == null || pageNo <= 0) {
            pageNo = 0;
        }
        Integer pageSize = (Integer) query.get("pageSize");
        if (pageSize < 0 || pageSize > 1000) {
            pageSize = 1000;
        }
        // 排序并分页
        PageHelper.startPage(pageNo, pageSize);
        List<RuEngineJob> ruEngineJobs = ruEngineJobService.queryEngineJobsByPage(query);

        List<EngineJob> engineJobs = getEngineJobs(ruEngineJobs);
        // 条件查询总数
        RuEngineJob countRecord = new RuEngineJob();
        countRecord.setPlanId((String) query.get(RePlanExecInfo.KEY_id));
        long jobCount = ruEngineJobService.queryCount(countRecord);

        Pagination<EngineJob> page = new DefaultPagination<>();
        page.setRecords(engineJobs);
        page.setRecordCount(jobCount);
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }

    @Override
    public List<EngineJob> getEngineJobs(List<RuEngineJob> ruEngineJobs) {
        List<EngineJob> engineJobs = new ArrayList<>();
        ruEngineJobs.forEach(job -> {
            EngineJob engineJob = new EngineJob();
            BeanUtils.copyProperties(job, engineJob);
            Long finishedTme = 0L;
            Long receivedTme = 0L;
            if (job.getFinishedTme() != null) {
                finishedTme = job.getFinishedTme().getTime();
            }
            if (job.getReceivedTme() != null) {
                receivedTme = job.getReceivedTme().getTime();
            }
            engineJob.setElapsedTime(finishedTme - receivedTme);
//            ReCaseDesignInfo caseInfo = reCaseDesignInfoService.findByPrimaryKey(job.getCaseId());
            ReCaseDesignInfo caseInfo = caseDesignFeignClient.findCaseInfoById(job.getCaseId()).getData();
            engineJob.setReCaseDesignInfo(caseInfo);
//            ReCaseSet caseSet = reCaseSetService.findByPrimaryKey(job.getCaseSetId());
            ReCaseSet caseSet = caseDesignFeignClient.findCaseSetInfo(job.getCaseSetId()).getData();
            engineJob.setReCaseSet(caseSet);
            engineJobs.add(engineJob);
        });
        return engineJobs;
    }

    /**
     * 通过计划id触发其他计划的执行
     *
     * @param sourcePlanId 触发计划id
     */
    @Override
    public void planGetTriggeredByPlanId(String sourcePlanId) {
        // 1.查询被此计划触发的所有计划
        RePlanTriggerRel queryTri = new RePlanTriggerRel();
        queryTri.setTriggerPlanId(sourcePlanId);
        List<RePlanTriggerRel> rels = rePlanTriggerRelService.query(queryTri);
        // 2.对结果遍历执行
        if (rels != null && rels.size() > 0) {
            for (RePlanTriggerRel rel : rels) {
                if (execPlanService.pluginIsEnable(rel.getPlanPluginId())) {
                    // 被触发的插件
                    ReExecPlan planInfo = reExecPlanService.findByPrimaryKey(rel.getPlanId());
                    ReExecPlan triPlanInfo = reExecPlanService.findByPrimaryKey(rel.getTriggerPlanId());
                    logger.info("计划【{}】已完成，触发计划【{}】的执行", triPlanInfo.getPlanName(), planInfo.getPlanName());
                    doPlanExecute(rel.getPlanId());
                }
            }
        }

    }

    /**
     * 更新任务的状态
     * 当任务结果为fail或者success，任务状态仍为Received
     */
    @Override
    @Transactional
    public void updateEngineJobStatus() {
        List<RuEngineJob> notFinishedJobs = ruEngineJobDao.queryJobStatusIsNotFinished();
        for (RuEngineJob job : notFinishedJobs) {
            logger.info("测试记录{}下任务{}，执行结果为{}，但执行状态仍为{}，准备进行状态修改", job.getPlanId(), job.getId(), job.getResult(), job.getStatus());
            RuEngineJob tempJob = new RuEngineJob();
            tempJob.setId(job.getId());
            tempJob.setStatus(RuEngineJobStatus.Finished.getCode());
            ruEngineJobService.updateByPrimaryKey(tempJob);
        }
    }


    /**
     * 分页查询任务步骤信息
     *
     * @param query    查询条件
     * @param orderBy  排列顺序
     * @param pageNo   页码
     * @param pageSize 页面大小
     * @return 任务步骤信息
     */
    @Override
    public Pagination<JobPro> queryJobProsByPage(Map<String, Object> query, OrderBy orderBy, Integer pageNo, Integer pageSize) {
        logger.info("分页查询");
        logger.info("查询条件：" + query);
        logger.info("排序方式：" + orderBy.toString());
        logger.info("页码：" + pageNo);
        logger.info("页面大小：" + pageSize);
        RuJobPro proRecord = new RuJobPro();
        proRecord.setJobId((String) query.get(RuJobPro.KEY_jobId));
//        Pagination<RuJobPro> proPage = ruJobProService.query(proRecord, orderBy, pageNo, pageSize);
        Pagination<RuJobPro> proPage = ruJobProService.queryForLike(query, orderBy, pageNo, pageSize);
        long proCount = ruJobProService.queryCount(proRecord);  // 统计任务步骤数量

        ArrayList<JobPro> jobPros = new ArrayList<>();
        proPage.getRecords().forEach(ruJobPro -> {
            JobPro jobPro = new JobPro();
            BeanUtils.copyProperties(ruJobPro, jobPro);
            Long finishedTme = 0L;
            Long startTme = 0L;
            if (ruJobPro.getEndTme() != null) {
                finishedTme = ruJobPro.getEndTme().getTime();
            }
            if (ruJobPro.getStartTme() != null) {
                startTme = ruJobPro.getStartTme().getTime();
            }
            jobPro.setElapsedTime(finishedTme - startTme);
            List<RuJobProLog> logs = getRuJobProLogs(ruJobPro);
            jobPro.setProLogs(logs);
            List<RuJobProParam> proParams = getRuJobProParams(ruJobPro);
            jobPro.setProParams(proParams);
            List<JobProAttachment> proAttachments = getJobProAttachments(ruJobPro);
            jobPro.setProAttachments(proAttachments);
            jobPros.add(jobPro);
        });

        Pagination<JobPro> page = new DefaultPagination<>();
        page.setRecords(jobPros);
        page.setRecordCount(proCount);
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }

    /**
     * 获取执行任务步骤的附件信息
     *
     * @param ruJobPro
     * @return
     */
    private List<JobProAttachment> getJobProAttachments(RuJobPro ruJobPro) {
        // 获取步骤的附件信息
        RuJobProAttachment attachmentQuery = new RuJobProAttachment();
        attachmentQuery.setJobId(ruJobPro.getJobId());
        attachmentQuery.setProId(ruJobPro.getId());
        OrderBy orderBy = new SqlOrderBy();
        orderBy.addOrderBy(RuJobProAttachment.KEY_idx, OrderBy.ASC);
        List<RuJobProAttachment> resultList = ruJobProAttachmentService.query(attachmentQuery, orderBy);
        // 处理附件链接
        List<JobProAttachment> attachments = new ArrayList<>();
        addUrlToAttachment(resultList, attachments);
        return attachments;
    }

    @Override
    public void addUrlToAttachment(List<RuJobProAttachment> resultList, List<JobProAttachment> attachments) {
        for (RuJobProAttachment attachment : resultList) {
            JobProAttachment proAttachment = new JobProAttachment();
            BeanUtils.copyProperties(attachment, proAttachment);
//            String url = "http://192.168.0.130:9631/download/" + attachment.getAttachId();
            String url = "http://" + attachUrlIp + ":" + attachUrlPort + attachUrlPath + attachment.getAttachId();
            proAttachment.setUrl(url);
            attachments.add(proAttachment);
        }
    }

    /**
     * 获取执行任务步骤的参数信息
     *
     * @param ruJobPro
     * @return
     */
    private List<RuJobProParam> getRuJobProParams(RuJobPro ruJobPro) {
        RuJobProParam paramQuery = new RuJobProParam();
        paramQuery.setProId(ruJobPro.getId());
        OrderBy paramOrder = new SqlOrderBy();
        paramOrder.addOrderBy(RuJobProParam.KEY_idx, OrderBy.ASC);
        return ruJobProParamService.query(paramQuery, paramOrder);
    }

    /**
     * 获取执行任务步骤日志信息
     *
     * @param ruJobPro
     * @return
     */
    private List<RuJobProLog> getRuJobProLogs(RuJobPro ruJobPro) {
        RuJobProLog logRecord = new RuJobProLog();
        logRecord.setProId(ruJobPro.getId());
        OrderBy logOrder = new SqlOrderBy();
        logOrder.addOrderBy(RuJobPro.KEY_idx, OrderBy.ASC);
        logOrder.addOrderBy(RuJobProLog.KEY_recordTme, OrderBy.ASC);
        return ruJobProLogService.query(logRecord, logOrder);
    }

    @Override
    public void getTestReportByPlanExecId(String planExecId) {
//        generateTestReport(planExecId);
    }

    @Override
    @Transactional
    public void deleteExecRecord(String planId) {
        RePlanExecInfo execInfoQuery = new RePlanExecInfo();
        execInfoQuery.setPlanId(planId);
        List<RePlanExecInfo> execInfoList = rePlanExecInfoService.query(execInfoQuery);
        for (RePlanExecInfo rePlanExecInfo : execInfoList) {
            String planExecId = rePlanExecInfo.getId();
            // 删除相关任务
            RuEngineJob ruEngineJob = new RuEngineJob();
            ruEngineJob.setPlanId(planExecId);
            ruEngineJobService.deleteByExample(ruEngineJob);
            // 删除相关任务步骤
            RuJobPro ruJobPro = new RuJobPro();
            ruJobPro.setPlanId(planExecId);
            ruJobProService.deleteByExample(ruJobPro);
            // 删除相关任务日志
            RuJobProLog ruJobProLog = new RuJobProLog();
            ruJobProLog.setPlanId(planExecId);
            ruJobProLogService.deleteByExample(ruJobProLog);
            // 删除相关任务步骤附件
            RuJobProAttachment ruJobProAttachment = new RuJobProAttachment();
            ruJobProAttachment.setPlanId(planExecId);
            ruJobProAttachmentService.deleteByExample(ruJobProAttachment);
        }
    }

}
