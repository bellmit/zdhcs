package net.northking.atp.producer;

import net.northking.atp.enums.ExecuteResult;
import net.northking.atp.mq.RabbitMQEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class TestReportProducer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(TestReportProducer.class);

    // rabbitMQ服务
    @Autowired
    private RabbitMQEndpoint rabbitMQEndpoint;

    // 执行报告队列名称
    @Value("${atp.mq.queue.plan-report:Q.plan.report}")
    private String reportQue;

    public void pushReoprtInfoToQueue(String planExecId) throws UnsupportedEncodingException {
        /*// 执行任务的执行结果情况
        Map<String, Long> execResultMap = ruEngineJobDao.countExecResultByPlanExecId(planExecId);
        Long jobNoRun = execResultMap.get(ExecuteResult.NO_RUN.code());
        Long jobRunning = execResultMap.get(ExecuteResult.RUNNING.code());
        // 执行任务下的步骤执行结果情况
        List<Map<String, BigDecimal>> proResultList = ruJobProDao.countNotFinish(planExecId);
        boolean proResultFlag = true;
        // 遍历执行任务下所有执行结果，确保执行任务的执行步骤和执行日志都有记录
        for (Map<String, BigDecimal> resultMap : proResultList) {
            BigDecimal failPro = resultMap.get(ExecuteResult.FAILURE.code());
            BigDecimal noRunPro = resultMap.get(ExecuteResult.NO_RUN.code());
            BigDecimal runningPro = resultMap.get(ExecuteResult.RUNNING.code());
            if (failPro.longValue() == 0L && (noRunPro.longValue() != 0L || runningPro.longValue() != 0L)) {
                proResultFlag = false;
                break;
            }
        }
        if (jobNoRun != 0 || jobRunning != 0 || !proResultFlag) {
            logger.info("执行记录 {}_{} 的测试报告重新放置队列中等待处理，请稍等", planExecInfo.getPlanName(), planExecInfo.getExecIdx());
            try {
                Thread.sleep(5000); // 5秒后重新推送到队列
                testReportProducer.pushReoprtInfoToQueue(planExecId);
                return;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        logger.info("测试报告{}进入队列中，正等待生成文件", planExecId);
        rabbitMQEndpoint.sendByText(reportQue, planExecId);
    }

}
