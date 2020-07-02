package net.northking.atp.controller;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.CaseDesignFeignClient;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.*;
import net.northking.atp.enums.ComponentFlag;
import net.northking.atp.enums.DefaultEnv;
import net.northking.atp.enums.PlanClass;
import net.northking.atp.enums.PlanType;
import net.northking.atp.impl.PlanExecuteInfoServiceImpl;
import net.northking.atp.service.ExecPlanService;
import net.northking.atp.service.PlanExecuteInfoService;
import net.northking.atp.service.TestEnvService;
import net.northking.atp.utils.BeanUtil;
import net.northking.atp.utils.UUIDUtil;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@Api(tags = {"测试执行记录"}, description = "测试执行记录")
@RequestMapping(value = "/planExecute")
public class PlanExecuteInfoController {

    private static final Logger logger = LoggerFactory.getLogger(PlanExecuteInfoController.class);

    @Autowired
    private RePlanExecInfoService rePlanExecInfoService;

    @Autowired
    private PlanExecuteInfoService planExecuteInfoService;

    @Autowired
    private ReWebhookInfoService reWebhookInfoService;

    @Autowired
    private ReWebhookTriggerLogService reWebhookTriggerLogService;

    @Autowired
    private CaseDesignFeignClient caseDesignFeignClient;

    @Autowired
    private ExecPlanService execPlanService;

    @Autowired
    private ReExecPlanCaseSetRelService reExecPlanCaseSetRelService;

    @Autowired
    private RuEngineJobService ruEngineJobService;

    @Autowired
    private RuJobProLogService ruJobProLogService;

    @Autowired
    private ReExecPlanService reExecPlanService;

    @Autowired
    private RuJobProAttachmentService ruJobProAttachmentService;

    @Autowired
    private RuJobProService ruJobProService;

    @Autowired
    private ReTestEnvInfoService reTestEnvInfoService;

    @Autowired
    private TestEnvService testEnvService;

    private final static String LOG_IDX = "logIdx";

    private final static String PRO_IDX = "proIdx";

    private final static String ATT_IDX = "attIdx";


    protected HttpServletRequest request;

//    @ModelAttribute
    public void getRequest(HttpServletRequest request) {
        this.request = request;
        logger.debug("请求的接口地址 --> {}", this.request.getRequestURL());
        logger.debug("请求的用户 --> {}", this.request.getRemoteUser());
        logger.debug("请求的客户机IP地址 --> {}", this.request.getRemoteAddr());
        logger.debug("请求的web服务器的ip地址 --> {}", this.request.getLocalAddr());
    }

    // 调试项目ID
//    private final static String DEBUG_PROJECT_ID = "DEBUG0001";

    @ApiOperation(value = "测试执行记录 计划执行", notes = "测试执行记录 计划执行")
    @RequestMapping(value = "/doExecute", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper planExecute(@RequestBody ReExecPlan plan) {
        long startTime = System.currentTimeMillis();
        // 构建测试计划
        try {
            logger.debug("计划ID: " + plan.getId());
            logger.debug("执行测试操作开始");
            RePlanExecInfo planExecInfo = planExecuteInfoService.doPlanExecute(plan.getId());
            logger.info("doExecute操作耗时：" + (System.currentTimeMillis() - startTime) + " 毫秒");
            return new ResultWrapper().success(planExecInfo);
        } catch (Exception e) {
//            logger.error(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            return new ResultWrapper().fail("ERROD00002", e.getMessage());
        }
    }

    @ApiOperation(value = "测试执行记录 查看执行记录列表", notes = "测试执行记录 查看执行记录列表")
    @RequestMapping(value = "/queryPlanExecInfoList", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<RePlanExecInfo>> queryPlanExecInfoList(@RequestBody QueryByPage<PlanExecInfo> query) {
        PlanExecInfo record = new PlanExecInfo();
        BeanUtils.copyProperties(query.getQuery(), record);
        OrderBy orderBy = new SqlOrderBy();
        orderBy.addOrderBy(PlanExecInfo.KEY_execIdx, OrderBy.DESC);
        Pagination<RePlanExecInfo> resultPage = rePlanExecInfoService.query(record, orderBy, query.getPageNo(), query.getPageSize());
        return new ResultWrapper<Pagination<RePlanExecInfo>>().success(resultPage);
    }

    @ApiOperation(value = "测试执行记录 查看执行记录详情", notes = "测试执行记录 查看执行记录详情")
    @RequestMapping(value = "/queryPlanExecInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<PlanExecInfo> queryPlanExecInfo(@RequestBody PlanExecInfo planExecInfo) {
        PlanExecInfo planExecInfo1 = planExecuteInfoService.queryPlanExecInfo(planExecInfo.getId());
        return new ResultWrapper<PlanExecInfo>().success(planExecInfo1);
    }

    @ApiOperation(value = "测试执行记录 查看执行任务步骤列表", notes = "测试执行记录 查看执行任务步骤列表")
    @RequestMapping(value = "/queryEngineJobsByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<EngineJob>> queryEngineJobsByPage(@RequestBody Map<String, Object> query) {
        Pagination<EngineJob> page = planExecuteInfoService.queryEngineJobsByPage(query);
        return new ResultWrapper<Pagination<EngineJob>>().success(page);
    }

    @ApiOperation(value = "测试执行记录 查看执行任务步骤详情", notes = "测试执行记录 查看执行任务步骤详情")
    @RequestMapping(value = "/queryJobProsByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<JobPro>> queryJobProsByPage(@RequestBody Map<String, Object> query) {
        Integer pageNo = (Integer) query.get("pageNo");
        if (pageNo == null || pageNo <= 0) {
            pageNo = 0;
        }
        Integer pageSize = (Integer) query.get("pageSize");
        if (pageSize < 0 || pageSize > 1000) {
            pageSize = 1000;
        }
        // 2019.05.21 暂时不分页，固定pageSize，pageNo
//        pageSize = 2000;
//        pageNo = 0;
        OrderBy orderBy = new SqlOrderBy();
        orderBy.addOrderBy(RuJobPro.KEY_idx, OrderBy.ASC);
        orderBy.addOrderBy(RuJobPro.KEY_startTme, OrderBy.ASC);
        Pagination<JobPro> page = planExecuteInfoService.queryJobProsByPage(query, orderBy, pageNo, pageSize);
        return new ResultWrapper<Pagination<JobPro>>().success(page);
    }

    @ApiOperation(value = "执行计划 获取测试报告", notes = "执行计划 获取测试报告")
    @RequestMapping(value = "/getTestReport", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper getTestReport(@RequestBody Map<String, Object> query) {
        try {
            planExecuteInfoService.getTestReportByPlanExecId((String) query.get(RePlanExecInfo.KEY_id));
            return new ResultWrapper().success();
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultWrapper().fail("0000002", e.getMessage());
        }
    }

    /**
     * 通过webhook方式触发测试计划的执行
     */
    @ApiOperation(value = "执行计划 webhook触发执行", notes = "执行计划 webhook触发执行")
    @RequestMapping(value = "/trigger/{id}", method = {RequestMethod.GET},
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper getExecutedByWebhook(@PathVariable String id,
                                              @RequestParam(value = "token", required = true) String token,
                                              @RequestParam(value = "username", required = false, defaultValue = "defaulUser") String username,
                                              @RequestParam(value = "desc", required = false, defaultValue = "") String desc) {
        // 验证处理
        logger.info("webhook触发的参数：id——{}，token——{}，username——{}，desc——{}", id, token, username, desc);
        List<RePlanExecInfo> execInfoList = new ArrayList<>();
        if (token != null && !"".equals(token)) {
            // 验证通过，记录触发信息，触发计划
            ReWebhookInfo wbQuery = new ReWebhookInfo();
            wbQuery.setToken(token);
            List<ReWebhookInfo> wbList = reWebhookInfoService.query(wbQuery);
            if (wbList != null && wbList.size() != 0) {
                for (ReWebhookInfo wbInfo : wbList) {
                    try {
                        RePlanExecInfo execInfo = planExecuteInfoService.doPlanExecute(wbInfo.getPlanId());
                        execInfoList.add(execInfo);
                        String log = String.format("用户%s通过webhook方式触发计划%s，触发说明%s", username, execInfo.getPlanName(), desc);
                        // 添加日志
                        addTriLog(token, username, desc, log, wbInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return new ResultWrapper().fail("0000003", "请检查验证信息是否正确");
        }
        return new ResultWrapper().success(execInfoList);
    }

    /**
     * 添加webhook触发日志
     *
     * @param token    token验证信息
     * @param username 用户信息
     * @param desc     说明信息
     * @param log      日志信息
     * @param wbInfo   webhook触发方式信息
     */
    private void addTriLog(String token, String username, String desc, String log, ReWebhookInfo wbInfo) {
        ReWebhookTriggerLog triLog = new ReWebhookTriggerLog();
        triLog.setWbId(wbInfo.getId());
        triLog.setId(UUIDUtil.getUUIDWithoutDash());
        triLog.setCreateTime(new Date());
        triLog.setToken(token);
        triLog.setUserName(username);
        triLog.setResult(log);
        triLog.setDescription(desc);
        reWebhookTriggerLogService.insert(triLog);
    }


    @ApiOperation(value = "执行计划 调试功能", notes = "执行计划 调试功能")
    @RequestMapping(value = "/doInterfaceDebug/{planId}", method = {RequestMethod.POST}, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper doInterfaceDebug(@PathVariable String planId, @RequestBody InterfaceCaseSetLink caseSetLink) {
        logger.info("进行调试功能，输入参数：planId -->【{}】，caseSetlink -->【{}】", planId, caseSetLink);
        // todo 调试下无法获取用例信息
        for (ReCaseSetLink link : caseSetLink.getLinkList()) {
            logger.info("用例集id -->【{}】，用例id -->【{}】", link.getCaseId(), link.getSetId());
            ReCaseDesignInfo caseInfo = caseDesignFeignClient.findCaseInfoById(link.getCaseId()).getData();
            if (caseInfo == null) {
                logger.info("用例【{}】不存在", link.getCaseId());
            } else {
                logger.info("用例【{}】存在",caseInfo.getCaseName());
            }
        }
        // 创建临时计划
        ResultWrapper planWrapper = createTempExecPlan(planId, caseSetLink);
        if (!planWrapper.isSuccess()) {
            return planWrapper;
        }
        ExecutePlan execPlan = (ExecutePlan) planWrapper.getData();
        // 执行doExecute计划执行方法
        RePlanExecInfo execInfo = planExecuteInfoService.doPlanExecute(execPlan.getId(), caseSetLink.getRunProjectId());
        // 返回执行记录
        logger.info("<-----临时计划{}调试进入等待队列，runProject = 【{}】", planId, caseSetLink.getRunProjectId());
        if (execInfo == null) {
            return new ResultWrapper().fail("接口调试功能错误0003", "无法对临时计划进行操作");
        }
        return new ResultWrapper().success(execInfo);
    }

    /**
     * 创建临时执行计划
     *
     * @param planId      执行计划id，页面产生
     * @param caseSetLink 用例集
     */
    private ResultWrapper createTempExecPlan(String planId, InterfaceCaseSetLink caseSetLink) {
        ExecutePlan executePlan = new ExecutePlan();
        executePlan.setId(planId);
        executePlan.setPlanName("临时执行计划" + caseSetLink.getId());
        executePlan.setIsUsed(1);
        executePlan.setProjectId(PlanExecuteInfoServiceImpl.DEBUG_PROJECT_ID);
        executePlan.setProfileId(String.valueOf(26));
        executePlan.setPlanType(PlanType.TEMPORARY.code());
        executePlan.setPlanClass(PlanClass.WEB_UI.code());
        String[] caseSets = {caseSetLink.getId()};
        // 创建计划-用例集临时关联
        executePlan.setCaseSets(caseSets);
        // todo: 确实计划类别：webui 还是 移动测试 -> 暂定使用默认测试环境进行webui测试
        setPlanTestEnv(executePlan, caseSetLink);
        ExecutePlan resultPlan = execPlanService.addExecPlan(executePlan);
        if (resultPlan == null) {
            return new ResultWrapper().fail("接口调试功能错误00002", "无法创建临时计划");
        }
        return new ResultWrapper().success(resultPlan);
    }

    /**
     * 添加临时计划的默认测试环境
     *
     * @param executePlan   临时计划信息对象
     * @param caseSetLink   用例集
     */
    private void setPlanTestEnv(ExecutePlan executePlan, InterfaceCaseSetLink caseSetLink) {
        ArrayList<TestEnvInfo> envInfos = new ArrayList<>();
//        ReTestEnvInfo envQuery = new ReTestEnvInfo();
//        envQuery.setProjectId(caseSetLink.getRunProjectId());
//        envQuery.setEnvType(executePlan.getPlanClass());
//        envQuery.setDefaultEnv(DefaultEnv.DEFAULT.code());
//        List<ReTestEnvInfo> resultInfos = reTestEnvInfoService.query(envQuery);
        ReTestEnvInfo reTestEnvInfo = testEnvService.checkAndAddDefaultEnv(caseSetLink.getRunProjectId(),
                "windows", "10", "Internet Explorer", "11", PlanClass.WEB_UI);
//        if (resultInfos != null && resultInfos.size() == 1) {
//            ReTestEnvInfo defaultInfo = resultInfos.get(0);
//            TestEnvInfo testEnvInfo = reTestEnvInfoService.setTestEnvInfoDetails(defaultInfo);
//        }
        TestEnvInfo testEnvInfo = testEnvService.getTestEnvInfo(reTestEnvInfo);
        envInfos.add(testEnvInfo);
        executePlan.setEnvInfos(envInfos);
    }


    /**
     * 查询调试时返回的调试日志信息，页面web定时发出请求
     *
     * @param query 查询条件（idx，planId->执行记录id）
     * @return 调试信息
     */
    @ApiOperation(value = "执行计划 查询调试日志功能", notes = "执行计划 查询调试日志功能")
    @RequestMapping(value = "/getDebugInfo", method = {RequestMethod.POST},
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper getDebugInfo(@RequestBody Map<String, Object> query) {
        // query中字段，startIdx，表明日志查询序号
        if (!query.containsKey(RuEngineJob.KEY_planId)) {
            return new ResultWrapper().fail("00002", "缺少参数执行记录id：planId");
        }
        if (!query.containsKey(LOG_IDX)) {
            return new ResultWrapper().fail("00002", "缺少参数日志序号：logIdx");
        }
//        if (!query.containsKey(PRO_IDX)) {
//            return new ResultWrapper().fail("00005", "缺少任务步骤序号：proIdx");
//        }
//        if (!query.containsKey(ATT_IDX)) {
//            return new ResultWrapper().fail("00006", "缺少任务附件序号：attIdx");
//        }
        RuEngineJob job = new RuEngineJob();
        job.setPlanId(query.get(RuEngineJob.KEY_planId).toString());
        List<RuEngineJob> jobList = ruEngineJobService.query(job);
        if (jobList == null || jobList.size() == 0) {
            return new ResultWrapper().fail("00003", "找不到对应的测试任务");
        }
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(RuJobProLog.KEY_jobId, jobList.get(0).getId());
        queryMap.put(RuJobProLog.KEY_idx, query.get(LOG_IDX));
        List<RuJobProLog> ruJobPros = ruJobProLogService.queryDebugLogs(queryMap);
        if (ruJobPros == null) {
            return new ResultWrapper().fail("00004", "调试日志未找到");
        }
        // --------------------> start
        List<JobProLog> jobProLogs = new ArrayList<>();
        for (RuJobProLog ruJobProLog : ruJobPros) {
            JobProLog jobProLog = new JobProLog();
            BeanUtils.copyProperties(ruJobProLog, jobProLog);
            RuJobPro ruJobPro = ruJobProService.findByPrimaryKey(ruJobProLog.getProId());
            if(ruJobPro != null){
                jobProLog.setRuJobPro(ruJobPro);
                RuJobProAttachment attQuery = new RuJobProAttachment();
                attQuery.setProId(ruJobPro.getId());
                List<RuJobProAttachment> ruJobProAttachments = ruJobProAttachmentService.query(attQuery);
                ArrayList<JobProAttachment> jobProAttachments = new ArrayList<>();
                planExecuteInfoService.addUrlToAttachment(ruJobProAttachments, jobProAttachments);
                jobProLog.setProAttachments(jobProAttachments);
                jobProLogs.add(jobProLog);
           }
        }

        return new ResultWrapper().success(jobProLogs);
    }

    /**
     * 删除调试生成的临时信息
     *
     * @param planId 临时计划id
     * @return 返回信息
     */
    @ApiOperation(value = "执行计划 删除调试信息", notes = "执行计划 删除调试信息")
    @RequestMapping(value = "/deleteDebugInfo/{planId}", method = {RequestMethod.POST})
    public ResultWrapper deleteDebugInfoByPlanId(@PathVariable String planId) {
        ReExecPlanCaseSetRel planCaseSetRelQuery = new ReExecPlanCaseSetRel();
        planCaseSetRelQuery.setPlanId(planId);
        List<ReExecPlanCaseSetRel> planSetRelList = reExecPlanCaseSetRelService.query(planCaseSetRelQuery);
        for (ReExecPlanCaseSetRel rel : planSetRelList) {
            // 计划-用例集关联
            reExecPlanCaseSetRelService.deleteByExample(rel);
        }
        // 删除计划
        execPlanService.deleteExecPlan(planId);
        return new ResultWrapper().success("成功删除计划" + planId + "调试信息");
    }

    /**
     * 删除临时用例集
     *
     * @param rel
     */
    private void deleteCaseSet(ReExecPlanCaseSetRel rel) {
        ReCaseSet caseSet = new ReCaseSet();
        caseSet.setId(rel.getCaseSet());
        caseDesignFeignClient.deleteCaseSet(caseSet);
    }

    /**
     * 删除临时用例和临时组件信息
     *
     * @param executePlan 执行计划信息
     * @param rel         计划-用例集关联信息
     * @return 返回信息
     */
    private ResultWrapper deleteCasesAndCmpts(ReExecPlan executePlan, ReExecPlanCaseSetRel rel) {
        ResultWrapper<List<ReCaseDesignInfo>> listWrapper = queryCaseInfoList(executePlan, rel);
        if (listWrapper != null && listWrapper.getData() != null) {
            List<ReCaseDesignInfo> records = listWrapper.getData();
            for (ReCaseDesignInfo record : records) {
                ResultWrapper<ReCaseSetLink> caseSetLinkWrapper = caseDesignFeignClient.queryCaseSetLinkById(record.getId());
                if (!caseSetLinkWrapper.isSuccess()) {
                    return caseSetLinkWrapper;
                }
                InterfaceCaseStep caseStep = new InterfaceCaseStep();
                caseStep.setId(caseSetLinkWrapper.getData().getCaseId());
                caseStep.setProjectId(record.getProjectId());
                ResultWrapper<List<InterfaceStepComponent>> stepCmptWrapper = caseDesignFeignClient.queryStepComponentList(caseStep);
                if (stepCmptWrapper != null && stepCmptWrapper.getData() != null) {
                    // 删除组件
                    ResultWrapper cmptStepWrapper = deleteAllTempCmpts(stepCmptWrapper.getData());
                    if (cmptStepWrapper != null) return cmptStepWrapper;
                }
                record.setId(caseSetLinkWrapper.getData().getCaseId());
                // 删除用例
                caseDesignFeignClient.deleteCaseInfo(record);
            }
        }
        return null;
    }

    /**
     * 查询用例集下所有用例信息
     *
     * @param executePlan 执行计划信息
     * @param rel         计划-用例集关联信息
     * @return 返回信息
     */
    private ResultWrapper<List<ReCaseDesignInfo>> queryCaseInfoList(ReExecPlan executePlan, ReExecPlanCaseSetRel rel) {
        QueryByPage<InterfaceCaseInfo> caseInfoQuery = new QueryByPage<>();
        caseInfoQuery.setPageNo(0);
        caseInfoQuery.setPageSize(2000);
        caseInfoQuery.setOrderByList(new ArrayList<>());
        InterfaceCaseInfo caseInfo = new InterfaceCaseInfo();
        caseInfo.setProjectId(executePlan.getProjectId());
        caseInfo.setId(rel.getCaseSet());
        caseInfoQuery.setQuery(caseInfo);
        return caseDesignFeignClient.queryAllCasesBySetId(caseInfoQuery);
    }

    /**
     * 删除临时组件信息
     *
     * @param allInterfaceCmptList 接口组件信息集合
     * @return 返回信息
     */
    private ResultWrapper deleteAllTempCmpts(List<InterfaceStepComponent> allInterfaceCmptList) {
        for (InterfaceStepComponent stepComponent : allInterfaceCmptList) {
            MdInterfaceInfo mdInterfaceInfo = new MdInterfaceInfo();
            mdInterfaceInfo.setId(stepComponent.getId());
            mdInterfaceInfo.setProjectId(stepComponent.getProjectId());
            caseDesignFeignClient.deleteInterfaceInfo(mdInterfaceInfo);
        }
        return null;
    }
}
