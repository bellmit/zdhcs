package net.northking.atp.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.northking.atp.db.persistent.HisRunEnvInfo;
import net.northking.atp.db.persistent.RePlanExecInfo;
import net.northking.atp.db.service.HisRunEnvInfoService;
import net.northking.atp.entity.CaseAndSetDetails;
import net.northking.atp.entity.ExecTaskEntity;
import net.northking.atp.entity.InterfaceCaseSetLink;
import net.northking.atp.mq.RabbitMQEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@Component
public class PlanExecInfoProducer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(PlanExecInfoProducer.class);

    // rabbitMQ服务
    @Autowired
    private RabbitMQEndpoint rabbitMQEndpoint;

    @Autowired
    private HisRunEnvInfoService hisRunEnvInfoService;

    // 队列名称
    @Value("${atp.mq.queue.plan-task:Q.plan.task}")
    private String planExecQueue;

    @Value("${atp.mq.queue.plan-task:Q.plan.datatest.task}")
    private String dataPlanExecQueue;

    public void pushExecInfoToQueue(RePlanExecInfo planExecInfo, String runProjectId, List<CaseAndSetDetails> caseAndSetList) {
//        String planExecId = (String) planMap.get(RePlanExecInfo.KEY_id);
        String planExecId = planExecInfo.getId();
        logger.debug("执行记录 {} 进入队列，等待生成测试任务", planExecId);
        logger.debug("runProjectId -->【{}】", runProjectId);
        // 查询运行环境信息
        List<HisRunEnvInfo> runEnvInfos = queryRunEnvInfosByPlanExecId(planExecId);
        for (CaseAndSetDetails caseAndSet : caseAndSetList) {
            // 遍历运行环境，生成对应运行环境的任务信息
            for (HisRunEnvInfo runEnvInfo : runEnvInfos) {
                ExecTaskEntity execTaskEntity = new ExecTaskEntity();
                execTaskEntity.setPlanExecId(planExecId);
                execTaskEntity.setCaseId(caseAndSet.getReCaseDesignInfo().getId());
                execTaskEntity.setCaseSetId(caseAndSet.getReCaseSet().getId());
                execTaskEntity.setRunTestEnvId(runEnvInfo.getId());
                execTaskEntity.setPlanExecInfo(planExecInfo);
//                Object runProjectId = planMap.get(InterfaceCaseSetLink.KEY_runProjectId);
                if (runProjectId != null && !"".equals(runProjectId)) {
                    execTaskEntity.setRuProjectId(runProjectId);
                }
                try {
                    // todo: 队列名称
                    rabbitMQEndpoint.sendByJson(planExecQueue, execTaskEntity);
//                    rabbitMQEndpoint.getConnectionFactory().createConnection().createChannel(true).queueDeclare()
                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
                    logger.error("编码出错，执行记录{}中用例集{}的用例{}不能生成测试任务", planExecId, caseAndSet.getReCaseSet().getId(), caseAndSet.getReCaseDesignInfo().getId());
                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
                    logger.error("Json转化错误，执行记录{}中用例集{}的用例{}不能生成测试任务", planExecId, caseAndSet.getReCaseSet().getId(), caseAndSet.getReCaseDesignInfo().getId());
                }
            }
        }
    }

    private List<HisRunEnvInfo> queryRunEnvInfosByPlanExecId(String planExecId) {
        HisRunEnvInfo runEnvQuery = new HisRunEnvInfo();
        runEnvQuery.setPlanExecId(planExecId);
        return hisRunEnvInfoService.query(runEnvQuery);
    }

    /**
     * 数据测试将任务放入队列
     * @param caseAndSetList
     */
    public void pushDataExecInfoToQueue(RePlanExecInfo planExecInfo, String runProjectId, List<CaseAndSetDetails> caseAndSetList) {
//        String planExecId = (String) planMap.get(RePlanExecInfo.KEY_id);
        String planExecId = planExecInfo.getId();
        logger.debug("执行记录 {} 进入队列，等待生成测试任务", planExecId);
//        logger.debug("runProjectId -->【{}】", planMap.get(InterfaceCaseSetLink.KEY_runProjectId));
        logger.debug("runProjectId -->【{}】", runProjectId);
        for (CaseAndSetDetails caseAndSet : caseAndSetList) {
                ExecTaskEntity execTaskEntity = new ExecTaskEntity();
                execTaskEntity.setPlanExecId(planExecId);
                execTaskEntity.setCaseId(caseAndSet.getReCaseDesignInfo().getId());
                execTaskEntity.setCaseSetId(caseAndSet.getReCaseSet().getId());
//                Object runProjectId = planMap.get(InterfaceCaseSetLink.KEY_runProjectId);
                if (runProjectId != null && !"".equals(runProjectId)) {
                    execTaskEntity.setRuProjectId((String)runProjectId);
                }
                try {
                    // todo: 队列名称
                    rabbitMQEndpoint.sendByJson(dataPlanExecQueue, execTaskEntity);
//                    rabbitMQEndpoint.getConnectionFactory().createConnection().createChannel(true).queueDeclare()
                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
                    logger.error("编码出错，执行记录{}中用例集{}的用例{}不能生成测试任务", planExecId, caseAndSet.getReCaseSet().getId(), caseAndSet.getReCaseDesignInfo().getId());
                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
                    logger.error("Json转化错误，执行记录{}中用例集{}的用例{}不能生成测试任务", planExecId, caseAndSet.getReCaseSet().getId(), caseAndSet.getReCaseDesignInfo().getId());
                }

        }
    }
}
