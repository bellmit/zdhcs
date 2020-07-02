package net.northking.atp.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.northking.atp.entity.ExecTaskEntity;
import net.northking.atp.mq.RabbitMQEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class TaskJobProducer {
    // 日志
    private static final Logger logger = LoggerFactory.getLogger(TaskJobProducer.class);

    @Autowired
    private RabbitMQEndpoint rabbitMQEndpoint;

    @Value("${atp.mq.queue.plan-job:Q.plan.job}")
    private String taskJobQueue;

    public void pushTaskJobToQueue(ExecTaskEntity entity) {
        logger.info("正在添加测试任务信息：" + entity.toString());
        try {
            rabbitMQEndpoint.sendByJson(taskJobQueue, entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
