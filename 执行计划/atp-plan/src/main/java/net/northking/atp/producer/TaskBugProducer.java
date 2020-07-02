package net.northking.atp.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.northking.atp.db.persistent.RuEngineJob;
import net.northking.atp.db.service.RuEngineJobService;
import net.northking.atp.enums.ExecuteResult;
import net.northking.atp.mq.RabbitMQEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 任务缺陷的处理
 */
@Component
public class TaskBugProducer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(TaskBugProducer.class);

    // rabbitMQ服务
    @Autowired
    private RabbitMQEndpoint rabbitMQEndpoint;

    @Autowired
    private RuEngineJobService ruEngineJobService;

    @Value("${atp.mq.queue.bug:Q.task.bug}")
    private String bugQueue;

    public void pushFailExecInfoToBugQueue(String execInfoId) {
        // 获取当前执行记录失败的任务
        RuEngineJob jobQuery = new RuEngineJob();
        jobQuery.setPlanId(execInfoId);
        jobQuery.setResult(ExecuteResult.FAILURE.code());
        List<RuEngineJob> failList = ruEngineJobService.query(jobQuery);
        if (failList != null && failList.size() > 0) {
            for (RuEngineJob ruEngineJob : failList) {
                try {
                    rabbitMQEndpoint.sendByJson(bugQueue, ruEngineJob);
                    logger.info("任务{}执行失败，提交信息到缺陷处理队列", ruEngineJob.getId());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
