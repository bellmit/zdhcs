package net.northking.atp.consumer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.northking.atp.db.enums.RuEngineJobStatus;
import net.northking.atp.db.persistent.RePlanExecInfo;
import net.northking.atp.db.persistent.RuEngineJob;
import net.northking.atp.db.service.RuEngineJobService;
import net.northking.atp.entity.ExecTaskEntity;
import net.northking.atp.mq.RabbitMQEndpoint;
import net.northking.atp.utils.UUIDUtil;

@Component
public class PlanExecInfoConsumer {
    // 日志
    private static final Logger logger = LoggerFactory.getLogger(PlanExecInfoConsumer.class);

    @Autowired
    private RuEngineJobService ruEngineJobService;

    @Autowired
    private RabbitMQEndpoint rabbitMQEndpoint;

    @Value("${atp.mq.queue.plan-job:Q.plan.job}")
    private String taskJobQueue;

    @RabbitListener(queues = {"${atp.mq.queue.plan-task:Q.plan.task}"}, concurrency = "3", containerFactory = "mqContainerFactory")
    @Transactional
    public void addEngineJob(@Payload byte[] body) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        ExecTaskEntity entity = om.readValue(body, ExecTaskEntity.class);
//        RePlanExecInfo planExecInfo = rePlanExecInfoService.findByPrimaryKey(entity.getPlanExecId());
        RePlanExecInfo planExecInfo = entity.getPlanExecInfo();
        RuEngineJob engineJob = new RuEngineJob();
        engineJob.setId(UUIDUtil.getUUIDWithoutDash());
        engineJob.setCaseId(entity.getCaseId());
        engineJob.setCaseSetId(entity.getCaseSetId());
        engineJob.setCreateTme(new Date());
        engineJob.setPlanId(entity.getPlanExecId());    // 执行记录id
        engineJob.setProfileId(planExecInfo.getProfileId());    // 数据环境
        engineJob.setProjectId(planExecInfo.getProjectId());    // 项目
        engineJob.setRunEnvId(entity.getRunTestEnvId());  // 执行环境
        if (entity.getRuProjectId() != null && !"".equals(entity.getRuProjectId())) {
            engineJob.setProjectId(entity.getRuProjectId());
        }
        engineJob.setStatus(RuEngineJobStatus.Queueing.getCode());
        ruEngineJobService.insert(engineJob);
        // 处理案例步骤
        entity.setEngineJobId(engineJob.getId());
        entity.setEngineJob(engineJob);
        pushTaskJobToQueue(entity);
        logger.info("任务{}已记录，等待执行引擎作业。", engineJob.getId());
    }

    public void pushTaskJobToQueue(ExecTaskEntity entity) {
        logger.info("正在添加测试任务信息：" + entity.toString());
        try {
            ObjectMapper om = new ObjectMapper();
            om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//            rabbitMQEndpoint.sendByJson(taskJobQueue, entity);
            rabbitMQEndpoint.sendByText(taskJobQueue, om.writeValueAsString(entity));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
