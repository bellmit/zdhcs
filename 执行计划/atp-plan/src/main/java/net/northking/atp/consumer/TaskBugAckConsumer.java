package net.northking.atp.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.northking.atp.CaseDesignFeignClient;
import net.northking.atp.CltTestBugFeignClient;
import net.northking.atp.CltUserFeignClient;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.CloudTest.CltUserAndLogin;
import net.northking.atp.entity.CltProjectTeamQuery;
import net.northking.atp.entity.TestEnvInfo;
import net.northking.atp.enums.ExecuteResult;
import net.northking.atp.service.TestEnvService;
import net.northking.atp.utils.UUIDUtil;
import net.northking.cloudtest.enums.DefectCreateType;
import net.northking.cloudtest.query.attach.AttachQuery;
import net.northking.cloudtest.query.testBug.TestBugQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提交任务失败的缺陷
 */
@Component
public class TaskBugAckConsumer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(TaskBugAckConsumer.class);

    // 开发经理角色
    private final static String ROLE_DEVELOPER_MANAGER = "ROLE_DEVELOPER_MANAGER";

    @Autowired
    private RePlanExecInfoService rePlanExecInfoService;
    @Autowired
    private CaseDesignFeignClient caseDesignFeignClient;
    @Autowired
    private RuJobProService ruJobProService;
    @Autowired
    private CltTestBugFeignClient cltTestBugFeignClient;
    @Autowired
    private RuJobProParamService ruJobProParamService;
    @Autowired
    private RuEngineJobService ruEngineJobService;
    @Autowired
    private TestEnvService testEnvService;
    @Autowired
    private ReTestEnvInfoService reTestEnvInfoService;
    @Autowired
    private HisRunEnvInfoService hisRunEnvInfoService;
    @Autowired
    private CltUserFeignClient cltUserFeignClient;
    @Autowired
    private RuJobProAttachmentService ruJobProAttachmentService;

    @RabbitListener(queues = {"${atp.mq.queue.bug:Q.task.bug}"}, containerFactory = "mqContainerFactory")
    public void getMsgFromQueue(@Payload byte[] body) {
        // 申请锁
        logger.debug("从队列中获取组件执行失败信息");
        addTaskBug(body);
        // 释放锁
    }

    private void addTaskBug(byte[] body) {
        ObjectMapper om = new ObjectMapper();
        try {
            // 失败的任务信息
            RuEngineJob job = om.readValue(body, RuEngineJob.class);
            // 失败的步骤信息
            RuJobPro proQuery = new RuJobPro();
            proQuery.setJobId(job.getId());
            proQuery.setResult(ExecuteResult.FAILURE.code());
            List<RuJobPro> proList = ruJobProService.query(proQuery);
            RuJobPro jobPro;
            if (proList != null && proList.size() > 0) {
                jobPro = proList.get(0);
            } else {
                logger.error("任务{}没有失败的步骤", job.getId());
                return;
            }
            logger.debug("任务{}中组件{}执行失败，正提交断言失败的相关信息", jobPro.getJobId(), jobPro.getCmptName());
            if ("NK.Assert".equals(jobPro.getCmptLibName()) || "NK.Appium".equals(jobPro.getCmptLibName()) || "NK.Selenium".equals(jobPro.getCmptLibName())) {
                // 执行记录信息
                RePlanExecInfo execInfo = rePlanExecInfoService.findByPrimaryKey(job.getPlanId());
                // 用例信息
                ReCaseDesignInfo caseInfo = caseDesignFeignClient.findCaseInfoById(job.getCaseId()).getData();
                if(caseInfo == null ){
                    logger.error("用例【{}】已不存在，无法提交缺陷信息", job.getCaseId());
                    return;
                }
                // 缺陷信息
                TestBugQuery cltTestBug = getCltTestBugInfo(jobPro, execInfo, caseInfo);
                if (cltTestBug == null) {
                    logger.debug("计划【{}】中的任务【{}】缺乏必要的信息，无法提交缺陷信息", execInfo.getProjectId(), job.getId());
                    return;
                }
                // 提交缺陷信息
                ResultWrapper resultWrapper = cltTestBugFeignClient.addCltTestBug(cltTestBug);
                logger.debug("缺陷提交【{}】，返回信息：{}", resultWrapper.getCode(), resultWrapper.getMessage());
                // 提交附件信息
                addAttachments(cltTestBug, jobPro, execInfo, caseInfo);
            } else {
                logger.error("当前报错组件{}属于{}库中断言组件，不属于【NK.Assert】或者【NK.Appium】库,", jobPro.getCmptName(), jobPro.getCmptLibName());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param cltTestBug
     * @param jobPro
     * @param execInfo
     * @param caseInfo
     */
    private void addAttachments(TestBugQuery cltTestBug, RuJobPro jobPro, RePlanExecInfo execInfo, ReCaseDesignInfo caseInfo) {
        logger.debug("缺陷【{}】正进行提交附件操作", cltTestBug.getBugId());
        RuJobProAttachment proAttachQuery = new RuJobProAttachment();
        proAttachQuery.setProId(jobPro.getId());
        List<RuJobProAttachment> resultProAttaches = ruJobProAttachmentService.query(proAttachQuery);
        if (resultProAttaches != null && resultProAttaches.size() > 0) {
            for (RuJobProAttachment proAttach : resultProAttaches) {
                AttachQuery attachQuery = new AttachQuery();
//                attachQuery.setId(UUIDUtil.getUUIDWithoutDash());
                attachQuery.setBugId(cltTestBug.getBugId());
                attachQuery.setExecuteId(jobPro.getJobId());
                attachQuery.setFuncId(jobPro.getId());
                attachQuery.setAttachModule(caseInfo.getModuleName());
                attachQuery.setAttachFunc("自动化缺陷");
                attachQuery.setAttachType(proAttach.getAttachType());
//                attachQuery.setSize(proAttach.getAttachSize());
                attachQuery.setAttachSize(proAttach.getAttachSize().floatValue());
                attachQuery.setAttachName(proAttach.getAttachName());
//                attachQuery.setAttachPath("download/" + proAttach.getAttachId());
                attachQuery.setAttachPath(proAttach.getAttachId());
                attachQuery.setOrderSeq(proAttach.getIdx());
                try {
                    ResultWrapper<Integer> submitNumWrapper = cltTestBugFeignClient.addCltAttachInfo(attachQuery);
                    if (submitNumWrapper != null && submitNumWrapper.isSuccess() && submitNumWrapper.getData() > 0) {
                        logger.debug("缺陷【{}】附件【{}】提交成功", cltTestBug.getBugId(), attachQuery.getAttachName());
                    } else {
                        logger.debug("缺陷【{}】附件【{}】提交失败，返回信息{}", cltTestBug.getBugId(), attachQuery.getAttachName(), submitNumWrapper.getMessage());
                    }
                } catch (Exception e) {
                    logger.error("调用CLOUDTEST-TESTBUG服务失败");
                }
            }
        } else {
            logger.debug("缺陷{}没有任何附件信息，不需要进行附件添加操作", cltTestBug.getBugId());
        }
    }

    /**
     * 缺陷的信息
     * @param jobPro    步骤信息
     * @param execInfo  执行记录信息
     * @param caseInfo  用例信息
     * @return  返回对象
     */
    private TestBugQuery getCltTestBugInfo(RuJobPro jobPro, RePlanExecInfo execInfo, ReCaseDesignInfo caseInfo) {
        TestBugQuery cltTestBug = new TestBugQuery();
        cltTestBug.setBugId(UUIDUtil.getUUIDWithoutDash());
        String title = String.format("%s_第%s次执行_%s",
                execInfo.getPlanName(), execInfo.getExecIdx(), caseInfo.getCaseName());
        cltTestBug.setBugTitle(title);
        cltTestBug.setBugTitleName(title);
        cltTestBug.setBugGrade("C");
        cltTestBug.setBugType("3");
        cltTestBug.setEmergency("2");
        cltTestBug.setIsReappear("1");
        String module = "自动化自动提交缺陷调试";
        if (caseInfo.getModuleName() != null) {
            module = caseInfo.getModuleName();
        } else if (caseInfo.getModuleId() != null){
            module = caseInfo.getModuleId();
        }
        cltTestBug.setModule(module);
        cltTestBug.setRoundId(execInfo.getPlanName());
        cltTestBug.setBatchId(execInfo.getPlanName());
        cltTestBug.setCaseId(caseInfo.getId());             // 2020.01.06 使用id，一期用例信息合并
//        cltTestBug.setCaseId(caseInfo.getCaseName());   // 暂时使用用例名称
        cltTestBug.setExecuteId(jobPro.getJobId());
        cltTestBug.setStepId(jobPro.getId());
        // 分配给
        CltProjectTeamQuery teamQuery = new CltProjectTeamQuery();
        teamQuery.setProId(execInfo.getProjectId());
        teamQuery.setRoleCode(ROLE_DEVELOPER_MANAGER);    // 默认开发经理
        // todo: 找不到开发经理
        logger.debug("查询项目【{}】的开发经理角色的用户【{}】", execInfo.getProjectId(), ROLE_DEVELOPER_MANAGER);
        ResultWrapper<List<CltUserAndLogin>> resultInfo = cltUserFeignClient.findTestAnalyserOrDesignByProId(teamQuery);
        logger.info("查询项目开发经理【{}】，具体：【{}】", resultInfo.isSuccess(), resultInfo.getData());
        if (resultInfo != null && resultInfo.isSuccess()
                && resultInfo.getData() != null && resultInfo.getData().size() > 0) {
            CltUserAndLogin cltUserAndLogin = resultInfo.getData().get(0);
//            cltTestBug.setReceiver(cltUserAndLogin.getUserChnName());
            cltTestBug.setReceiver(cltUserAndLogin.getUserId());
        } else {
            logger.info("项目缺乏必要的信息，没有给定项目的【开发经理】");
            return null;
        }
//        cltTestBug.setCreateType("S");  // 来自自动化测试的缺陷
        cltTestBug.setCreateType(DefectCreateType.AUTO_TEST.getCode());  // 来自自动化测试的缺陷
        // 拼接执行步骤信息
        StringBuffer stringBuffer = getExecuteStepInfo(jobPro);
        cltTestBug.setOperStep(stringBuffer.toString());
        // 实际结果
        List<RuJobProParam> paramList = setActualResult(jobPro, cltTestBug);
        // 预期结果
        setPreResult(cltTestBug, paramList);
        cltTestBug.setAccess_token("000000");
//        cltTestBug.setCreateUser(caseInfo.getCreateStaff() != null ? caseInfo.getCreateStaff() : "autoTest");
        // 缺陷创建人，默认给用例创建人
        cltTestBug.setCreateUser(caseInfo.getCreateStaff());
        // 缺陷所在计划
        cltTestBug.setProId(execInfo.getProjectId());
        // 环境信息
        RuEngineJob engineJob = ruEngineJobService.findByPrimaryKey(jobPro.getJobId());
        HisRunEnvInfo runEnvInfo = hisRunEnvInfoService.findByPrimaryKey(engineJob.getRunEnvId());  // 执行运行环境历史
        TestEnvInfo envInfo = reTestEnvInfoService.findTestEnvInfoById(runEnvInfo.getEnvId());
        cltTestBug.setEnvMessage(envInfo.getReOsInfo().getOsName() + "_" + envInfo.getReBrowserInfo().getBwName());
        return cltTestBug;
    }

    private List<RuJobProParam> setActualResult(RuJobPro jobPro, TestBugQuery cltTestBug) {
        cltTestBug.setActualResult("【实际结果，不符合预期】");
        RuJobProParam paramQuery = new RuJobProParam();
        paramQuery.setProId(jobPro.getId());
        List<RuJobProParam> paramList = ruJobProParamService.query(paramQuery);
        for (RuJobProParam ruJobProParam : paramList) {
            if (ruJobProParam.getValueName().equals("actual")) {
                cltTestBug.setActualResult("实际结果为：" + ruJobProParam.getValue() + "，不符合预期");
                break;
            }
        }
        return paramList;
    }

    private void setPreResult(TestBugQuery cltTestBug, List<RuJobProParam> paramList) {
        cltTestBug.setPerResult("【预期结果为页面内容】");
        for (RuJobProParam ruJobProParam : paramList) {
            if (ruJobProParam.getValueName().equals("expected")) {
                cltTestBug.setPerResult("预期结果为：" + ruJobProParam.getValue());
                break;
            }
        }
    }

    private StringBuffer getExecuteStepInfo(RuJobPro jobPro) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(RuJobPro.KEY_jobId, jobPro.getJobId());
        queryMap.put(RuJobPro.KEY_idx, jobPro.getIdx());
        queryMap.put("method", 1);
        List<RuJobPro> ruJobPros = ruJobProService.queryJobProOnCondition(queryMap);
        StringBuffer stringBuffer = new StringBuffer();
        for (RuJobPro ruJobPro : ruJobPros) {
            stringBuffer.append(ruJobPro.getIdx());
            stringBuffer.append(".");
            stringBuffer.append(ruJobPro.getCmptName());
            stringBuffer.append("_");
            stringBuffer.append(ruJobPro.getCmptDataName());
            stringBuffer.append("\r\n");
        }
        return stringBuffer;
    }
}
