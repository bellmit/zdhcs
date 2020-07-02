package net.northking.atp.consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.ExecTaskEntity;
import net.northking.atp.entity.TaskJobInfo;
import net.northking.atp.enums.ComponentFlag;
import net.northking.atp.enums.ExecuteResult;
import net.northking.atp.enums.ParameterFlag;
import net.northking.atp.mq.RabbitMQEndpoint;
import net.northking.atp.utils.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskJobAckConsumer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(TaskJobAckConsumer.class);

    //    @Autowired
//    private CaseDesignFeignClient caseDesignFeignClient;
//
//
//    @Autowired
//    private RePlanExecInfoService rePlanExecInfoService;
//    @Autowired
//    private RuEngineJobService ruEngineJobService;
//    @Autowired
//    private RuJobProService ruJobProService;
//    @Autowired
//    private RuJobProParamService ruJobProParamService;
//    @Autowired
//    private RuJobProLogService ruJobProLogService;

    // ----------- 工具
    @Autowired
    private RabbitMQEndpoint rabbitMQEndpoint;

    // ----------- 业务
    @Autowired
    private ReCaseDesignInfoService reCaseDesignInfoService;
    @Autowired
    private ReCaseStepService reCaseStepService;
    @Autowired
    private ReComponentInfoService reComponentInfoService;
    @Autowired
    private ReComponentParameterService reComponentParameterService;
    @Autowired
    private ReStepParameterService reStepParameterService;
    @Autowired
    private ReComponentLibraryService reComponentLibraryService;
    @Autowired
    private ReComponentPackageService reComponentPackageService;

    // ----------- 队列名称
    @Value("${atp.mq.queue.job-update:Q.job.update}")
    private String jobUpdateQueue;     // 任务更新
    @Value("${atp.mq.queue.job-info:Q.job.jobInfo}")
    private String jobInfoQueue;       // 任务所有信息

    // ----------- 常量
    private final static String KEY_parentCmptId = "parentCmptId";

    private final static String nkEngineCmptLibName = "NK.Engine";

    private final static String startExtBlock = "StartExtBlock";

    private final static String endExtBlock = "EndExtBlock";

    private final static String NORAML_PROJECT_ID = "SYSTEM";

    private final static String KEY_PARENT_CMPT_FLAG = "parentCmptFlag";

    private Map<String, Object> dataMap = new HashMap<>();

    private Map<String, TaskJobInfo> infos = new HashMap<>();


    @RabbitListener(queues = {"${atp.mq.queue.plan-job:Q.plan.job}"}, concurrency = "4", containerFactory = "mqContainerFactory")
    public void addJobProAndParam(@Payload String body, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        ExecTaskEntity entity = null;
        try {
            entity = om.readValue(body, ExecTaskEntity.class);
        } catch (Exception e) {
            logger.error("队列消息转换失败", e);
            try {
                channel.basicAck(deliveryTag, false);
            } catch (Exception ioe) {
                logger.error("确认信息收到失败", ioe);
            }
            throw new RuntimeException(e);
        }
        initialJobInfoToMap(entity.getEngineJobId());
        RePlanExecInfo planExecInfo = entity.getPlanExecInfo();
        RuEngineJob engineJob = entity.getEngineJob();
        logger.debug("计划{}，任务{}，正在添加相关步骤、参数信息", entity.getRuProjectId(), entity.getEngineJobId());
        dataMap.put(entity.getEngineJobId(), deliveryTag);
        // 获取用例、执行记录、执行任务信息
        // 改本地调用
//            ReCaseDesignInfo caseInfo = caseDesignFeignClient.findCaseInfoById(entity.getCaseId()).getData();
        ReCaseDesignInfo caseInfo = reCaseDesignInfoService.findByPrimaryKey(entity.getCaseId());
        if (caseInfo == null || "".equals(caseInfo.getCaseName())) {
            String errorMsg = String.format("用例【{}】不存在，不能生成对应测试任务步骤", entity.getCaseId());
            logger.error(errorMsg);
            // 更新执行任务的状态为结束，并且执行结果为失败
            sendFailureJob(engineJob.getId(), errorMsg);
            return;
        }

        // 获取用例的所有步骤
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(ReCaseStep.KEY_caseId, entity.getCaseId());
        queryMap.put(ReCaseStep.KEY_projectId, caseInfo.getProjectId());
//            List<Map<String, Object>> stepList = caseDesignFeignClient.queryStepListByOrder(queryMap);
        List<Map<String, Object>> stepList = reCaseStepService.queryStepListByOrder(queryMap);

        // 重置任务步骤计数器
        int proIdx = 1;

        for (Map<String, Object> step : stepList) {
            // 重置计数器
            int runCmptIdx = 1;
            try {
                step.put("pacOrder", getOrder(proIdx));
                proIdx = addJobPros(caseInfo, step, planExecInfo, engineJob, proIdx, runCmptIdx, entity, 1, channel);
            } catch (Exception e) {
                logger.error("添加执行步骤失败", e);
                throw e;
            }
        }
        // todo: 在用例服务中改成发送结果到队列中，测试执行服务器监听进行状态更新
        // 数据处理后更新任务状态
        RuEngineJob job = new RuEngineJob();
        job.setId(engineJob.getId());
        job.setStatus(RuEngineJobStatus.Initial.getCode());
//            ruEngineJobService.updateByPrimaryKey(job);
        sendTaskInfoToQueue(job);
        // 删除临时信息
        dataMap.remove(entity.getEngineJobId());
    }

    /**
     * 添加任务步骤操作
     *
     * @param caseInfo     案例信息
     * @param step         案例步骤信息
     * @param planExecInfo 执行记录信息
     * @param engineJob    执行任务信息
     * @param runCmptIdx   高级组件内的顺序
     */
    private int addJobPros(ReCaseDesignInfo caseInfo,
                           Map<String, Object> step,
                           RePlanExecInfo planExecInfo,
                           RuEngineJob engineJob,
                           int proIdx,
                           int runCmptIdx,
                           ExecTaskEntity entity,
                           int forNum,
                           Channel channel) {
        Object cmptFlag = step.get(ReComponentInfo.KEY_componentFlag);
        // todo: 暂时添加ComponentFlag == 4 条件
        if (cmptFlag != null
                && (ComponentFlag.SENIOR.code().equals(cmptFlag) || ComponentFlag.INTERFACE.code().equals(cmptFlag))) {
            // 重置为1
            runCmptIdx = 1;
            // 处理高级组件
            if (forNum > 10) {
                return proIdx;
            }
            forNum++; //循环层数+1
            proIdx = seniorCmptParamToProParam(caseInfo, step, planExecInfo, engineJob, proIdx, runCmptIdx, entity, forNum, channel);
        } else {
            /* 处理基础组件 */
            Object basicCmpt = step.get(ReComponentPackage.KEY_basisComponentId);
            // 确定基础组件
            String componentId = basicCmpt != null ?
                    (String) step.get(ReComponentPackage.KEY_basisComponentId) : (String) step.get(ReComponentStep.KEY_componentId);
            // 添加任务步骤
            RuJobPro jobPro = new RuJobPro();
            // 获取组件信息
            ReComponentInfo cmptInfo = reComponentInfoService.findByPrimaryKey(componentId);
            if (cmptInfo == null) {
                String errorInfo = String.format("案例【%s】，步骤【%s】，执行步骤【%s】，组件【%s】存在问题，不能执行", caseInfo.getCaseName(), proIdx, runCmptIdx, componentId);
//                throw new RuntimeException(errorInfo);
                // 找不到组件，将情况写到执行日志里
                logger.error(errorInfo);
                //  更新执行任务的状态为结束，并且执行结果为失败
                sendFailureJob(engineJob.getId(), errorInfo);
                long tag = (Long) dataMap.get(engineJob.getId());
                try {
                    channel.basicAck(tag, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                throw new RuntimeException(errorInfo);
            }
            // 添加组件库信息
            proIdx = setJobProCmptLibInfo(caseInfo, planExecInfo, engineJob, proIdx, jobPro, cmptInfo);
            // 查询所用到的基础组件信息
            ReComponentParameter paramQuery = new ReComponentParameter();
            paramQuery.setProjectId(cmptInfo.getProjectId());
            if (basicCmpt != null) {
                paramQuery.setRunComponentId((String) basicCmpt);
                paramQuery.setRunComponentOrder(String.format("%03d", runCmptIdx));
            }
            // 判断是否有父级组件，有的使用父级组件查询父级组件获取参数值
            if (step.containsKey(KEY_parentCmptId)) {
                paramQuery.setComponentId((String) step.get(KEY_parentCmptId));
                // 确定projectId
                paramQuery.setProjectId(caseInfo.getProjectId());
                // 获取父组件信息
//                ReComponentInfo parentCmpt = caseDesignFeignClient.findCmptInfoById((String) step.get(KEY_parentCmptId)).getData();
                ReComponentInfo parentCmpt = reComponentInfoService.findByPrimaryKey((String) step.get(KEY_parentCmptId));
                paramQuery.setProjectId(parentCmpt.getProjectId());
            } else {
                paramQuery.setComponentId((String) step.get(ReComponentStep.KEY_componentId));
            }

            // 获取组件的参数列表
//            List<ReComponentParameter> params = caseDesignFeignClient.queryCmptParams(paramQuery).getData();
            List<ReComponentParameter> params = reComponentParameterService.query(paramQuery);
            addJobProParams(caseInfo, engineJob, planExecInfo, jobPro, params, step, entity);

            // 将步骤保存到数据库中
//            ruJobProService.insert(jobPro);
            addPro(jobPro);
        }
        return proIdx;
    }

    private int setJobProCmptLibInfo(ReCaseDesignInfo caseInfo, RePlanExecInfo planExecInfo, RuEngineJob engineJob, int proIdx, RuJobPro jobPro, ReComponentInfo cmptInfo) {
        // 组件库信息
//        ReComponentLibrary cmptLib = caseDesignFeignClient.findCmptLibById(cmptInfo.getLibraryId()).getData();
        ReComponentLibrary cmptLib = reComponentLibraryService.findByPrimaryKey(cmptInfo.getLibraryId());
        proIdx = getRuJobPro(jobPro, caseInfo, planExecInfo, engineJob, proIdx);
        jobPro.setCmptLibUrl(cmptLib.getIpPort() + "/" + (cmptLib.getFileId() == null ? cmptLib.getFileName() : cmptLib.getFileId()));

        if (cmptInfo.getComponentFlag() != null && ComponentFlag.SMART.code().equals(cmptInfo.getComponentFlag())) {
            //智能接口
            jobPro.setCmptName("requestSmartPort");
        } else {
            jobPro.setCmptName(cmptInfo.getComponentName());
        }
        jobPro.setCmptLibName(cmptLib.getLibraryName());
        jobPro.setCmptDataName(cmptInfo.getDataName());
        return proIdx;
    }

    /**
     * 查询高级组件下组件
     *
     * @param caseInfo
     * @param step
     * @param planExecInfo
     * @param engineJob
     * @param runCmptIdx
     */
    private int seniorCmptParamToProParam(ReCaseDesignInfo caseInfo,
                                          Map<String, Object> step,
                                          RePlanExecInfo planExecInfo,
                                          RuEngineJob engineJob,
                                          int proIdx,
                                          int runCmptIdx,
                                          ExecTaskEntity entity,
                                          int forNum,
                                          Channel channel) {
        // 高级组件的自定义输入参数
        proIdx = addSeniorCmptJobPro(caseInfo, step, planExecInfo, engineJob, proIdx, ParameterFlag.INPUT.code(), entity);
        // todo: 改成本地
//        ReComponentInfo cmpt = caseDesignFeignClient.findCmptInfoById((String) step.get(ReComponentPackage.KEY_componentId)).getData();
        ReComponentInfo cmpt = reComponentInfoService.findByPrimaryKey((String) step.get(ReComponentPackage.KEY_componentId));
        // 遍历高级组件内的组件
        ReComponentPackage packageQuery = new ReComponentPackage();
        // todo: 更改projectId
        String projectId = cmpt.getProjectId();
        packageQuery.setProjectId(projectId);
        packageQuery.setComponentId((String) step.get(ReComponentPackage.KEY_componentId));
        // todo: 改成本地
//        List<Map<String, Object>> packageList = caseDesignFeignClient.queryComponentByOrder(packageQuery.toMap());
        List<Map<String, Object>> packageList = reComponentPackageService.queryComponentByOrder(packageQuery.toMap());
        int pacOrder = 1;
        for (Map<String, Object> pacMap : packageList) {
            pacMap.put(ReComponentPackage.KEY_componentId, pacMap.get(ReComponentPackage.KEY_basisComponentId));
            pacMap.put(KEY_parentCmptId, step.get(ReComponentPackage.KEY_componentId)); // 父级组件id
            pacMap.put(ReCaseStep.KEY_stepOrder, step.get(ReCaseStep.KEY_stepOrder));   // 组件顺序
            pacMap.put(ReCaseStep.KEY_stepId, step.get(ReCaseStep.KEY_stepId));         // 案例步骤id
            pacMap.put(KEY_PARENT_CMPT_FLAG, step.get(ReComponentInfo.KEY_componentFlag));
            pacMap.put("pacOrder", getOrder(pacOrder));
            // 任务步骤
            proIdx = addJobPros(caseInfo, pacMap, planExecInfo, engineJob, proIdx, runCmptIdx, entity, forNum, channel);
            // +1
            runCmptIdx++;
            pacOrder++;
        }
        // 高级组件的自定义输出参数
        proIdx = addSeniorCmptJobPro(caseInfo, step, planExecInfo, engineJob, proIdx, ParameterFlag.OUTPUT.code(), entity);
        return proIdx;
    }

    /**
     * 高级组件的输入/输出生成对应的任务步骤
     *
     * @param caseInfo      用例信息
     * @param step          用例步骤信息
     * @param planExecInfo  执行记录信息
     * @param engineJob     引擎任务信息
     * @param parameterFlag 参数输入/输出 类型
     */
    private int addSeniorCmptJobPro(ReCaseDesignInfo caseInfo, Map<String, Object> step,
                                    RePlanExecInfo planExecInfo,
                                    RuEngineJob engineJob,
                                    int proIdx,
                                    String parameterFlag,
                                    ExecTaskEntity entity) {
        ReComponentParameter paramQuery = new ReComponentParameter();
        paramQuery.setProjectId(caseInfo.getProjectId());   // 项目
        String componentId = (String) step.get(ReComponentParameter.KEY_componentId);
        paramQuery.setComponentId(componentId); // 组件ID
        paramQuery.setInOrOut("1"); // 外部
        paramQuery.setParameterFlag(parameterFlag);   // 输入
        // todo: 改成本地
//        ReComponentInfo cmptInfo = caseDesignFeignClient.findCmptInfoById(componentId).getData();
        ReComponentInfo cmptInfo = reComponentInfoService.findByPrimaryKey(componentId);
        if (entity.getRuProjectId() != null && !"".equals(entity.getRuProjectId())
                && entity.getRuProjectId().equals(cmptInfo.getProjectId())) {
            paramQuery.setProjectId(entity.getRuProjectId());
        }
        // todo: 改成本地
//        List<ReComponentParameter> params = caseDesignFeignClient.queryCmptParams(paramQuery).getData();
        List<ReComponentParameter> params = reComponentParameterService.query(paramQuery);

        //TODO 添加方法块取出。不论输入输出是否有都需要增加默认的start/end组件
        // 添加任务步骤
        RuJobPro jobPro = new RuJobPro();
        proIdx = getRuJobPro(jobPro, caseInfo, planExecInfo, engineJob, proIdx);
        jobPro.setCmptLibName(nkEngineCmptLibName);
        if (ParameterFlag.INPUT.code().equals(parameterFlag)) {
            jobPro.setCmptName(startExtBlock);
        } else {
            jobPro.setCmptName(endExtBlock);
            if (params != null && params.size() > 0) {
                jobPro.setCmptOutput(params.get(0).getParameterName());
            }
        }
        jobPro.setCmptDataName(step.get(RuJobPro.KEY_cmptDataName) + "");

        if (params != null && params.size() > 0) {
            addJobProParams(caseInfo, engineJob, planExecInfo, jobPro, params, step, entity);
        }
        // 添加步骤
//        ruJobProService.insert(jobPro);
        addPro(jobPro);
        return proIdx;
    }

    /**
     * 产生新的任务步骤
     *
     * @param jobPro       执行步骤
     * @param caseInfo     用例信息
     * @param planExecInfo 执行记录信息
     * @param engineJob    执行任务信息
     * @return 步骤序号
     */
    private int getRuJobPro(RuJobPro jobPro, ReCaseDesignInfo caseInfo, RePlanExecInfo planExecInfo, RuEngineJob engineJob, int proIdx) {
//        RuJobPro jobPro = new RuJobPro();
        jobPro.setId(UUIDUtil.getUUIDWithoutDash());
        jobPro.setCaseId(caseInfo.getId());
        jobPro.setCaseVersion(caseInfo.getVersion());
        jobPro.setJobId(engineJob.getId());
        jobPro.setPlanId(planExecInfo.getId());
        jobPro.setIdx(proIdx++);
        // 步骤递增
        return proIdx;
    }

    /**
     * 添加任务步骤参数
     *
     * @param caseInfo     用例信息
     * @param engineJob    执行任务信息
     * @param planExecInfo 执行记录信息
     * @param jobPro       任务步骤信息
     * @param paramList    任务步骤参数集合
     */
    private void addJobProParams(ReCaseDesignInfo caseInfo,
                                 RuEngineJob engineJob,
                                 RePlanExecInfo planExecInfo,
                                 RuJobPro jobPro,
                                 List<ReComponentParameter> paramList,
                                 Map<String, Object> step,
                                 ExecTaskEntity entity) {
        int num = 0;
        for (ReComponentParameter customParam : paramList) {
            // 确定参数输入/输出类型
            String ioType = ParameterFlag.INPUT.code().equals(customParam.getParameterFlag())
                    ? ParameterFlag.INPUT.msg() : ParameterFlag.OUTPUT.msg();
            // 案例步骤对应的组件参数/参数值
            RuJobProParam proParam = getRuJobProParam(caseInfo, engineJob, planExecInfo, jobPro, customParam, ioType);
            // 获取步骤参数
            ReStepParameter stepParamQuery = new ReStepParameter();
            stepParamQuery.setParameterId(customParam.getId());
//            if (entity.getRuProjectId() != null && !"".equals(entity.getRuProjectId())) {
//
//            }
            stepParamQuery.setStepId((String) step.get(ReCaseStep.KEY_stepId));
            // todo: 查询步骤的参数列表
//            List<ReStepParameter> stepParams = caseDesignFeignClient.queryStepParams(stepParamQuery).getData();
            List<ReStepParameter> stepParams = reStepParameterService.query(stepParamQuery);
            ReStepParameter stepParam = stepParams.size() > 0 ? stepParams.get(0) : new ReStepParameter();
            // 组件标记
            boolean cmptFlag = step.containsKey(ReComponentInfo.KEY_componentFlag) && ComponentFlag.SENIOR.code().equals(step.get(ReComponentInfo.KEY_componentFlag));
            boolean interFlag = step.containsKey(ReComponentInfo.KEY_componentFlag) && (ComponentFlag.SENIOR.code().equals(step.get(ReComponentInfo.KEY_componentFlag))
                    || ComponentFlag.INTERFACE.code().equals(step.get(ReComponentInfo.KEY_componentFlag)));

            //TODO 特殊处理高级组件外部参数应由高级组件父组件内部参数替换_zcy 20200315
            if (step.containsKey(KEY_parentCmptId) && interFlag) {
                ReComponentParameter faQuery = new ReComponentParameter();
                faQuery.setComponentId(step.get(KEY_parentCmptId) + "");
                faQuery.setRunComponentId(step.get(ReComponentPackage.KEY_componentId) + "");
                faQuery.setProjectId(customParam.getProjectId());
                faQuery.setInOrOut("0");
                faQuery.setParameterFlag(customParam.getParameterFlag());
                faQuery.setParameterOrder(customParam.getParameterOrder());
                faQuery.setRunComponentOrder(step.get("pacOrder") + "");
                // todo: 查询组件的参数列表
//                List<ReComponentParameter> paList=caseDesignFeignClient.queryCmptParams(faQuery).getData();
                List<ReComponentParameter> paList = reComponentParameterService.query(faQuery);
                if (paList.size() > 0) {
                    customParam.setDefaultValue(paList.get(0).getDefaultValue());
                } else {
                    faQuery.setProjectId("DEBUG01");
                    // todo: 查询组件的参数列表
//                    List<ReComponentParameter> paListDebug =caseDesignFeignClient.queryCmptParams(faQuery).getData();
                    List<ReComponentParameter> paListDebug = reComponentParameterService.query(faQuery);
                    if (paListDebug.size() > 0) {
                        customParam.setDefaultValue(paListDebug.get(0).getDefaultValue());
                    }
                }
            }//更新体完毕

            if (stepParams.size() == 0) {
                // 步骤没有设置参数则使用组件参数值名称
                // 如果该参数类型是output类型，则不需要设置参数值
                if (step.containsKey(KEY_parentCmptId) && ParameterFlag.OUTPUT.msg().equals(ioType)) {
                    proParam.setValueName(customParam.getDefaultValue());
                    //TODO 高级组件增加参数对应关系_zcy 20200317
                    if (interFlag && "1".equals(customParam.getInOrOut())) {
                        proParam.setValue(customParam.getParameterName());
//                        RuJobProParam seniorInputParamFromOutput = getRuJobProParam(caseInfo, engineJob, planExecInfo, jobPro, customParam, ParameterFlag.INPUT.msg());
//                        seniorInputParamFromOutput.setValueName(customParam.getDefaultValue());
//                        seniorInputParamFromOutput.setValue(customParam.getParameterName());
//                        ruJobProParamService.insert(seniorInputParamFromOutput);
                    }//更新体完毕
                } else {
//                    proParam.setValueName(customParam.getParameterName() + paramIdx);
                    proParam.setValueName(customParam.getParameterName());
                }
                if (ParameterFlag.INPUT.msg().equals(ioType)) {
                    // input类型参数，设置参数值
                    proParam.setValue(customParam.getDefaultValue());
                }
            } else {
                // 步骤设置了当前参数的值
                if (ParameterFlag.OUTPUT.msg().equals(ioType)) {
                    // ouput类型则不需要设置参数值，设置该名称为步骤参数值
                    proParam.setValueName(stepParam.getParameterValue());
                    // 如果是高级组件的输出，则需要增加步骤参数与组件参数的对应关系
                    if (interFlag && "1".equals(customParam.getInOrOut())) {
                        proParam.setValue(customParam.getDefaultValue());
//                        RuJobProParam seniorInputParamFromOutput = getRuJobProParam(caseInfo, engineJob, planExecInfo, jobPro, customParam, ParameterFlag.INPUT.msg());
//                        seniorInputParamFromOutput.setValueName(stepParam.getParameterValue());
//                        seniorInputParamFromOutput.setValue(customParam.getParameterName());
//                        ruJobProParamService.insert(seniorInputParamFromOutput);
                    }
                } else {
                    // input类型则设置参数值名称为当前参数值名称，参数值则为步骤参数值
//                    proParam.setValueName(customParam.getParameterName() + paramIdx);
                    proParam.setValueName(customParam.getParameterName());
                    proParam.setValue(stepParam.getParameterValue());
                }
            }
            //  todo: 发送执行任务的参数到队列中
//            ruJobProParamService.insert(proParam);
            addProParam(proParam);
            if (ParameterFlag.OUTPUT.msg().equals(ioType) && !interFlag) {
//                RuJobPro update = new RuJobPro();
//                update.setId(jobPro.getId());
//                update.setCmptOutput(proParam.getValueName());
//                ruJobProService.updateByPrimaryKey(update);
                jobPro.setCmptOutput(proParam.getValueName());
            }
            if (num == 0 && ComponentFlag.SMART.code().equals(step.get("componentFlag"))) {
                //智能接口第一行参数额外增加默认的接口Id
                RuJobProParam smartPortParam = new RuJobProParam();
                BeanUtils.copyProperties(proParam, smartPortParam);
                smartPortParam.setId(UUIDUtil.getUUIDWithoutDash());
                smartPortParam.setInOrOut("input");
                smartPortParam.setName("智能接口主键");
                smartPortParam.setType("STRING");
                smartPortParam.setValueName("SmartPort_defaultId");
                smartPortParam.setValue("\"" + step.get("componentId") + "\"");
//                ruJobProParamService.insert(proParam);
                addProParam(smartPortParam);
            }
            num++;
        }
    }

    /**
     * 创建任务步骤参数对象
     *
     * @param caseInfo     用例信息
     * @param engineJob    执行任务
     * @param planExecInfo 执行记录
     * @param jobPro       执行步骤
     * @param customParam  组件参数
     * @param ioType       输入|输出类型
     * @return
     */
    private RuJobProParam getRuJobProParam(ReCaseDesignInfo caseInfo,
                                           RuEngineJob engineJob,
                                           RePlanExecInfo planExecInfo,
                                           RuJobPro jobPro,
                                           ReComponentParameter customParam,
                                           String ioType) {
        RuJobProParam proParam = new RuJobProParam();
        proParam.setId(UUIDUtil.getUUIDWithoutDash());
        proParam.setPlanId(planExecInfo.getId());
        proParam.setCaseId(caseInfo.getId());
        proParam.setCaseVersion(caseInfo.getVersion());
        proParam.setJobId(engineJob.getId());
        proParam.setProId(jobPro.getId());
        proParam.setInOrOut(ioType);
        proParam.setName(customParam.getParameterComment());
        proParam.setType(customParam.getParameterType());
        proParam.setIdx(jobPro.getIdx());
        return proParam;
    }

    private String getOrder(int i) {
        String order = "00" + i;
        return order.substring(order.length() - 3, order.length());
    }

    /**
     * 初始化数据
     *
     * @param engineJobId
     */
    private void initialJobInfoToMap(String engineJobId) {
        RuEngineJob ruEngineJob = new RuEngineJob();
        List<RuJobPro> jobPros = new ArrayList<>();
        List<RuJobProParam> ruJobProParams = new ArrayList<>();

        TaskJobInfo taskJobInfo = new TaskJobInfo();
        taskJobInfo.setRuEngineJob(ruEngineJob);
        taskJobInfo.setRuJobPros(jobPros);
        taskJobInfo.setRuJobProParams(ruJobProParams);

        infos.put(engineJobId, taskJobInfo);
    }


    /**
     * 发送执行任务更新信息失败
     *
     * @param engineJob
     */
    private void sendTaskInfoToQueue(RuEngineJob engineJob) {
        TaskJobInfo taskJobInfo = infos.get(engineJob.getId());
        if (taskJobInfo != null) {
            taskJobInfo.setRuEngineJob(engineJob);
            try {
                rabbitMQEndpoint.sendByJson(jobInfoQueue, taskJobInfo);
            } catch (Exception e) {
                logger.error("发送执行任务更新信息失败", e);
            }
        }

    }

    /**
     * 发送执行步骤参数信息
     *
     * @param proParam
     */
    private void addProParam(RuJobProParam proParam) {
        TaskJobInfo taskJobInfo = infos.get(proParam.getJobId());
        if (taskJobInfo != null) {
            taskJobInfo.getRuJobProParams().add(proParam);
        }
//        try {
//            rabbitMQEndpoint.sendByJson(jobProParamQueue, proParam);
//        } catch (Exception e) {
//            logger.error("发送执行步骤参数失败", e);
//        }
    }

    /**
     * 发送执行步骤信息
     *
     * @param jobPro
     */
    private void addPro(RuJobPro jobPro) {
        TaskJobInfo taskJobInfo = infos.get(jobPro.getJobId());
        if (taskJobInfo != null) {
            taskJobInfo.getRuJobPros().add(jobPro);
        }

//        try {
//            rabbitMQEndpoint.sendByJson(jobProQueue, jobPro);
//        } catch (Exception e) {
//            logger.error("任务步骤信息发送失败", e);
//        }
    }

    /**
     * 发送失败的任务到队列中，等待后续处理
     *
     * @param jobId    任务ID
     * @param errorMsg 生成任务过程中的错误信息
     */
    private void sendFailureJob(String jobId, String errorMsg) {
        try {
            RuEngineJob job = new RuEngineJob();
            job.setId(jobId);
            job.setNote(errorMsg);
            job.setStatus(RuEngineJobStatus.Finished.getCode());
            job.setResult(ExecuteResult.FAILURE.code());
            rabbitMQEndpoint.sendByJson(jobUpdateQueue, job);
        } catch (Exception e) {
            logger.error("任务失败信息发送失败", e);
        }
//        ruEngineJobService.updateByPrimaryKey(job);
    }

    enum RuEngineJobStatus {
        Queueing("Queueing", "数据处理中"),
        Initial("Initial", "初始"),
        Distribution("Distribution", "待分配"),
        Ready("Ready", "准备就绪"),
        Received("Received", "引擎已接收"),
        Executing("Executing", "执行中"),
        Finished("Finished", "已完成");

        /**
         * 执行状态代码
         */
        private String code;

        /**
         * 执行状态名称
         */
        private String name;

        RuEngineJobStatus(final String code, final String name) {
            this.code = code;
            this.name = name;
        }

        /**
         * 获取 执行状态代码
         *
         * @return the value of code
         */
        public String getCode() {
            return code;
        }

        /**
         * 获取 执行状态名称
         *
         * @return the value of name
         */
        public String getName() {
            return name;
        }
    }
}
