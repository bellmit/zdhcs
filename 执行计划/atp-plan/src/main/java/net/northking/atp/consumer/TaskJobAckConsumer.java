package net.northking.atp.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import net.northking.atp.CaseDesignFeignClient;
import net.northking.atp.db.enums.RuEngineJobStatus;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.RePlanExecInfoService;
import net.northking.atp.db.service.RuEngineJobService;
import net.northking.atp.db.service.RuJobProParamService;
import net.northking.atp.db.service.RuJobProService;
import net.northking.atp.engine.common.ExecLogMessage;
import net.northking.atp.entity.ExecTaskEntity;
import net.northking.atp.enums.ComponentFlag;
import net.northking.atp.enums.ExecuteResult;
import net.northking.atp.enums.ParameterFlag;
import net.northking.atp.mq.RabbitMQService;
import net.northking.atp.utils.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskJobAckConsumer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(TaskJobAckConsumer.class);

    @Autowired
    private CaseDesignFeignClient caseDesignFeignClient;

    @Autowired
    private RePlanExecInfoService rePlanExecInfoService;
    @Autowired
    private RuEngineJobService ruEngineJobService;
    @Autowired
    private RuJobProService ruJobProService;
    @Autowired
    private RuJobProParamService ruJobProParamService;

    private final static String KEY_parentCmptId = "parentCmptId";

    private final static String nkEngineCmptLibName = "NK.Engine";

    private final static String startExtBlock = "StartExtBlock";

    private final static String endExtBlock = "EndExtBlock";

    private final static String NORAML_PROJECT_ID = "SYSTEM";

    private final static String KEY_PARENT_CMPT_FLAG = "parentCmptFlag";

    private Channel channel;

    private Map<String, Object> dataMap = new HashMap<>();

    /**
     * rabbitMQ服务
     */
    @Autowired
    private RabbitMQService rabbitMQService;

    /**
     * 任务日志的队列名称
     */
    @Value("${atp.mq.queue.job-result.log:Q.jobResult.log}")
    private String logQueue;


//    @RabbitListener(queues = {"${atp.mq.queue.plan-job:Q.plan.job}"})
//    @Transactional
    public void addJobProAndParam(@Payload String body, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel chan) {
//        logger.info("deliveryTag => 【{}】", deliveryTag);
        this.channel = chan;
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
            } catch (IOException ioe) {
                logger.error("确认信息收到失败", ioe);
            }
            throw new RuntimeException(e);
        }
            logger.info("计划{}，任务{}，正在添加相关步骤、参数信息", entity.getRuProjectId(), entity.getEngineJobId());
            dataMap.put(entity.getEngineJobId(), deliveryTag);
            // 获取用例、执行记录、执行任务信息
//            ReCaseDesignInfo caseInfo = reCaseDesignInfoService.findByPrimaryKey(entity.getCaseId());
            // todo:
            ReCaseDesignInfo caseInfo = caseDesignFeignClient.findCaseInfoById(entity.getCaseId()).getData();
            // todo: 改成从队列中获取
            RePlanExecInfo planExecInfo = rePlanExecInfoService.findByPrimaryKey(entity.getPlanExecId());
            // todo: 改成从队列中获取
            RuEngineJob engineJob = ruEngineJobService.findByPrimaryKey(entity.getEngineJobId());
            // 获取用例的所有步骤
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put(ReCaseStep.KEY_caseId, entity.getCaseId());
            queryMap.put(ReCaseStep.KEY_projectId, caseInfo.getProjectId());
//            List<Map<String, Object>> stepList = reCaseStepService.queryStepListByOrder(queryMap);
            List<Map<String, Object>> stepList = caseDesignFeignClient.queryStepListByOrder(queryMap);
            // 重置任务步骤计数器
            int proIdx = 1;

            for (Map<String, Object> step : stepList) {
                // 重置计数器
//                logger.info("==========================> 进入步骤循环");
                int runCmptIdx = 1;
                try {
                    step.put("pacOrder",getOrder(proIdx));
                    proIdx = addJobPros(caseInfo, step, planExecInfo, engineJob, proIdx, runCmptIdx, entity,1);
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
            ruEngineJobService.updateByPrimaryKey(job);

    }

    private void updateExecInfo(RePlanExecInfo planExecInfo) {
        RePlanExecInfo infoQuery = new RePlanExecInfo();
        infoQuery.setId(planExecInfo.getId());
        infoQuery.setExecEndTime(new Date());
        infoQuery.setExecStatus(RuEngineJobStatus.Finished.getCode());
        infoQuery.setExecResult(ExecuteResult.FAILURE.code());
        rePlanExecInfoService.updateByPrimaryKey(infoQuery);
    }

    private RuEngineJob updateJobInfo(RuEngineJob engineJob) {
        RuEngineJob tempJob = new RuEngineJob();
        tempJob.setId(engineJob.getId());
        tempJob.setStatus(RuEngineJobStatus.Finished.getCode());
        tempJob.setResult(ExecuteResult.FAILURE.code());
        ruEngineJobService.updateByPrimaryKey(tempJob);
        return tempJob;
    }

    /**
     * 保存日志到数据库
     * @param step
     * @param tempJob
     * @param e
     * @throws UnsupportedEncodingException
     * @throws JsonProcessingException
     */
    private void sendExceptionMsg(Map<String, Object> step, RuEngineJob tempJob, Exception e) throws UnsupportedEncodingException, JsonProcessingException {
        ExecLogMessage execLogMessage = new ExecLogMessage();
        execLogMessage.setId(UUIDUtil.getUUIDWithoutDash());
        execLogMessage.setJobId(tempJob.getId());
        execLogMessage.setComponentId((String)step.get(ReComponentPackage.KEY_componentId));
//        execLogMessage.setLevel(Level.ERROR.toString());
        execLogMessage.setMessage(e.getMessage());
        execLogMessage.setTimestamp(System.currentTimeMillis());
        execLogMessage.setIdx(new Long(1));
        rabbitMQService.sendByJson(logQueue, execLogMessage);
    }

    /**
     * 添加任务步骤操作
     *
     * @param caseInfo      案例信息
     * @param step          案例步骤信息
     * @param planExecInfo  执行记录信息
     * @param engineJob     执行任务信息
     * @param runCmptIdx    高级组件内的顺序
     */
    private int addJobPros(ReCaseDesignInfo caseInfo,
                           Map<String, Object> step,
                           RePlanExecInfo planExecInfo,
                           RuEngineJob engineJob,
                           int proIdx,
                           int runCmptIdx,
                           ExecTaskEntity entity,
                           int forNum) {
        Object cmptFlag = step.get(ReComponentInfo.KEY_componentFlag);
        // todo: 暂时添加ComponentFlag == 4 条件
        if (cmptFlag != null
                && (ComponentFlag.SENIOR.code().equals(cmptFlag) || ComponentFlag.INTERFACE.code().equals(cmptFlag))) {
            // 重置为1
            runCmptIdx = 1;
            // 处理高级组件
            if(forNum>10){
                return proIdx;
            }
            forNum++; //循环层数+1
            proIdx = seniorCmptParamToProParam(caseInfo, step, planExecInfo, engineJob, proIdx, runCmptIdx, entity,forNum);
        } else {
            /* 处理基础组件 */
            Object basicCmpt = step.get(ReComponentPackage.KEY_basisComponentId);
            // 确定基础组件
            String componentId = basicCmpt != null ?
                    (String) step.get(ReComponentPackage.KEY_basisComponentId) : (String) step.get(ReComponentStep.KEY_componentId);
            // 添加任务步骤
            RuJobPro jobPro = new RuJobPro();
            // todo: 改成本地
            ReComponentInfo cmptInfo = caseDesignFeignClient.findCmptInfoById(componentId).getData();
            if (cmptInfo == null) {
                String errorInfo = String.format("案例【%s】，步骤【%s】，执行步骤【%s】，组件【%s】存在问题，不能执行", caseInfo.getCaseName(), proIdx, runCmptIdx, componentId);
//                throw new RuntimeException(errorInfo);
                // 找不到组件，将情况写到执行日志里
                logger.error(errorInfo);
                RuEngineJob job = new RuEngineJob();
                job.setId(engineJob.getId());
                job.setStatus(RuEngineJobStatus.Finished.getCode());
                job.setResult(ExecuteResult.FAILURE.code());
                ruEngineJobService.updateByPrimaryKey(job);
                long tag = (Long) dataMap.get(job.getId());
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
                // todo: 改成本地
                ReComponentInfo parentCmpt = caseDesignFeignClient.findCmptInfoById((String) step.get(KEY_parentCmptId)).getData();
//                if (entity.getRuProjectId() != null && !"".equals(entity.getRuProjectId())
//                        && !ComponentFlag.INTERFACE.code().equals(step.get(KEY_PARENT_CMPT_FLAG)) && entity.getRuProjectId().equals(parentCmpt.getProjectId())) {
//                    paramQuery.setProjectId(entity.getRuProjectId());
//                }

                paramQuery.setProjectId(parentCmpt.getProjectId());
            } else {
                paramQuery.setComponentId((String) step.get(ReComponentStep.KEY_componentId));
            }
            // todo: 改成本地
            List<ReComponentParameter> params = caseDesignFeignClient.queryCmptParams(paramQuery).getData();
            // todo: 发送到队列中
            ruJobProService.insert(jobPro);
            addJobProParams(caseInfo, engineJob, planExecInfo, jobPro, params, step, entity);
        }
        return proIdx;
    }

    private int setJobProCmptLibInfo(ReCaseDesignInfo caseInfo, RePlanExecInfo planExecInfo, RuEngineJob engineJob, int proIdx, RuJobPro jobPro, ReComponentInfo cmptInfo) {
        // 组件库信息
        ReComponentLibrary cmptLib = caseDesignFeignClient.findCmptLibById(cmptInfo.getLibraryId()).getData();
        proIdx = getRuJobPro(jobPro, caseInfo, planExecInfo, engineJob, proIdx);
        jobPro.setCmptLibUrl(cmptLib.getIpPort() + "/" + (cmptLib.getFileId()==null?cmptLib.getFileName():cmptLib.getFileId()));

        if (cmptInfo.getComponentFlag() != null && ComponentFlag.SMART.code().equals(cmptInfo.getComponentFlag())){
            //智能接口
            jobPro.setCmptName("requestSmartPort");
        }else{
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
                                          int forNum) {
        // 高级组件的自定义输入参数
        proIdx = addSeniorCmptJobPro(caseInfo, step, planExecInfo, engineJob, proIdx, ParameterFlag.INPUT.code(), entity);
        // todo: 改成本地
        ReComponentInfo cmpt = caseDesignFeignClient.findCmptInfoById((String) step.get(ReComponentPackage.KEY_componentId)).getData();
        // 遍历高级组件内的组件
        ReComponentPackage packageQuery = new ReComponentPackage();
        // 确定ProjectId
//        String projectId = caseInfo.getProjectId();
//        if (entity.getRuProjectId() != null && !"".equals(entity.getRuProjectId())
//                && !ComponentFlag.INTERFACE.code().equals(step.get(ReComponentInfo.KEY_componentFlag))
//                && entity.getRuProjectId().equals(cmpt.getProjectId())) {
//            projectId = entity.getRuProjectId();
//        }
        // todo: 更改projectId
        String projectId = cmpt.getProjectId();
        packageQuery.setProjectId(projectId);
        packageQuery.setComponentId((String) step.get(ReComponentPackage.KEY_componentId));
        List<Map<String, Object>> packageList = caseDesignFeignClient.queryComponentByOrder(packageQuery.toMap());
        int pacOrder = 1;
        for (Map<String, Object> pacMap : packageList) {
            pacMap.put(ReComponentPackage.KEY_componentId, pacMap.get(ReComponentPackage.KEY_basisComponentId));
            pacMap.put(KEY_parentCmptId, step.get(ReComponentPackage.KEY_componentId)); // 父级组件id
            pacMap.put(ReCaseStep.KEY_stepOrder, step.get(ReCaseStep.KEY_stepOrder));   // 组件顺序
            pacMap.put(ReCaseStep.KEY_stepId, step.get(ReCaseStep.KEY_stepId));         // 案例步骤id
            pacMap.put(KEY_PARENT_CMPT_FLAG, step.get(ReComponentInfo.KEY_componentFlag));
            pacMap.put("pacOrder",getOrder(pacOrder));
            // 任务步骤
            proIdx = addJobPros(caseInfo, pacMap, planExecInfo, engineJob, proIdx, runCmptIdx, entity,forNum);
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
        ReComponentInfo cmptInfo = caseDesignFeignClient.findCmptInfoById(componentId).getData();
        if (entity.getRuProjectId() != null && !"".equals(entity.getRuProjectId())
                && entity.getRuProjectId().equals(cmptInfo.getProjectId())) {
            paramQuery.setProjectId(entity.getRuProjectId());
        }
//        List<ReComponentParameter> params = reComponentParameterService.query(paramQuery);
        List<ReComponentParameter> params = caseDesignFeignClient.queryCmptParams(paramQuery).getData();

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
        // todo: 发送到队列
        ruJobProService.insert(jobPro);

        if (params != null && params.size() > 0) {
            addJobProParams(caseInfo, engineJob, planExecInfo, jobPro, params, step, entity);
        }
        return proIdx;
    }

    /**
     * 产生新的任务步骤
     *
     * @param jobPro        执行步骤
     * @param caseInfo      用例信息
     * @param planExecInfo  执行记录信息
     * @param engineJob     执行任务信息
     * @return              步骤序号
     */
    private int getRuJobPro(RuJobPro jobPro,ReCaseDesignInfo caseInfo, RePlanExecInfo planExecInfo, RuEngineJob engineJob, int proIdx) {
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
     * @param caseInfo      用例信息
     * @param engineJob     执行任务信息
     * @param planExecInfo  执行记录信息
     * @param jobPro        任务步骤信息
     * @param paramList     任务步骤参数集合
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
            // todo:
            if (entity.getRuProjectId() != null && !"".equals(entity.getRuProjectId())) {

            }
            stepParamQuery.setStepId((String) step.get(ReCaseStep.KEY_stepId));
            List<ReStepParameter> stepParams = caseDesignFeignClient.queryStepParams(stepParamQuery).getData();
            ReStepParameter stepParam = stepParams.size() > 0 ? stepParams.get(0) : new ReStepParameter();
            // 组件标记
            boolean cmptFlag = step.containsKey(ReComponentInfo.KEY_componentFlag) && ComponentFlag.SENIOR.code().equals(step.get(ReComponentInfo.KEY_componentFlag));
            boolean interFlag = step.containsKey(ReComponentInfo.KEY_componentFlag) && (ComponentFlag.SENIOR.code().equals(step.get(ReComponentInfo.KEY_componentFlag))
                    ||ComponentFlag.INTERFACE.code().equals(step.get(ReComponentInfo.KEY_componentFlag)));

            //TODO 特殊处理高级组件外部参数应由高级组件父组件内部参数替换_zcy 20200315
            if(step.containsKey(KEY_parentCmptId) && interFlag){
                ReComponentParameter faQuery = new ReComponentParameter();
                faQuery.setComponentId(step.get(KEY_parentCmptId)+"");
                faQuery.setRunComponentId(step.get(ReComponentPackage.KEY_componentId)+"");
                faQuery.setProjectId(customParam.getProjectId());
                faQuery.setInOrOut("0");
                faQuery.setParameterFlag(customParam.getParameterFlag());
                faQuery.setParameterOrder(customParam.getParameterOrder());
                faQuery.setRunComponentOrder(step.get("pacOrder")+"");
                List<ReComponentParameter> paList=caseDesignFeignClient.queryCmptParams(faQuery).getData();
                if(paList.size()>0){
                    customParam.setDefaultValue(paList.get(0).getDefaultValue());
                }else{
                    faQuery.setProjectId("DEBUG01");
                    List<ReComponentParameter> paListDebug =caseDesignFeignClient.queryCmptParams(faQuery).getData();
                    if(paListDebug.size()>0){
                        customParam.setDefaultValue(paListDebug.get(0).getDefaultValue());
                    }
                }
            }//更新体完毕

            String paramIdx = getParamIdx(engineJob);

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
            ruJobProParamService.insert(proParam);
            if (ParameterFlag.OUTPUT.msg().equals(ioType) && !interFlag) {
                RuJobPro update = new RuJobPro();
                update.setId(jobPro.getId());
                update.setCmptOutput(proParam.getValueName());
                ruJobProService.updateByPrimaryKey(update);
            }
            if(num==0 && ComponentFlag.SMART.code().equals(step.get("componentFlag"))){
                //智能接口第一行参数额外增加默认的接口Id
                proParam.setId(UUIDUtil.getUUIDWithoutDash());
                proParam.setInOrOut("input");
                proParam.setName("智能接口主键");
                proParam.setType("STRING");
                proParam.setValueName("SmartPort_defaultId");
                proParam.setValue("\""+step.get("componentId")+"\"");
                ruJobProParamService.insert(proParam);
            }
            num++;
        }
    }

    private String getParamIdx(RuEngineJob engineJob) {
        // 查询当前任务的参数数量
        RuJobProParam paramQuery = new RuJobProParam();
        paramQuery.setJobId(engineJob.getId());
        long paramCount = ruJobProParamService.queryCount(paramQuery);
        return "_" + paramCount;
    }

    /**
     *  创建任务步骤参数对象
     *
     * @param caseInfo          用例信息
     * @param engineJob         执行任务
     * @param planExecInfo      执行记录
     * @param jobPro            执行步骤
     * @param customParam       组件参数
     * @param ioType            输入|输出类型
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

    private String getOrder(int i){
        String order = "00"+i;
        return order.substring(order.length()-3,order.length());
    }
}
