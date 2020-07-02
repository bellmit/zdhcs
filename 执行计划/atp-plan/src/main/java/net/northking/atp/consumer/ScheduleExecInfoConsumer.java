package net.northking.atp.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.northking.atp.db.persistent.ReExecPlan;
import net.northking.atp.db.persistent.ReExecPlanPluginSetting;
import net.northking.atp.db.persistent.ReScheduleTask;
import net.northking.atp.db.service.ReExecPlanService;
import net.northking.atp.db.service.ReScheduleTaskService;
import net.northking.atp.utils.UUIDUtil;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定时任务信息处理
 */
@Component
public class ScheduleExecInfoConsumer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(ScheduleExecInfoConsumer.class);

    @Autowired
    private ReScheduleTaskService reScheduleTaskService;

    @Autowired
    private ReExecPlanService reExecPlanService;

    private final static String cronExps = "crons";

//    @Autowired
//    public void

    @RabbitListener(queues = {"${atp.mq.queue.scheduleExec:Q.pi.schedule}"})
    public void addScheduleExecInfo(@Payload byte[] body) {
        try {
            ObjectMapper om = new ObjectMapper();
            ReExecPlanPluginSetting setting = om.readValue(body, ReExecPlanPluginSetting.class);
            String paramValue = setting.getPluginParamValue();
            // 从paramValue插件值的获取信息
            JSONObject jo = JSON.parseObject(paramValue);
            String cronString = jo.getString(cronExps);
            if (cronString != null) {   // 表达式不为空
                String[] cronArray = cronString.split("\r\n");
                ReScheduleTask query = new ReScheduleTask();
                query.setPlanPluginId(setting.getId());
                List<ReScheduleTask> oldList = reScheduleTaskService.query(query);
                ArrayList<String> oldIds = new ArrayList<>();   // 原有的信息id
                for (ReScheduleTask task : oldList) {
                    oldIds.add(task.getId());
                }
                if (cronArray.length > 0) { // 表达式长度大于0
                    for (String cron : cronArray) {
                        if (CronExpression.isValidExpression(cron.trim())) {
                            List<ReScheduleTask> resultList = getReScheduleTasks(setting, cron);
                            if (resultList.size() > 0) {
                                ReScheduleTask resultTask = resultList.get(0);
                                if (oldIds.contains(resultTask.getId())) {
                                    oldIds.remove(resultTask.getId());  // 保留准备删除关联id
                                } else {
                                    // 存在记录则修改记录
                                    resultTask.setCronExp(cron);
                                    reScheduleTaskService.updateByPrimaryKey(resultTask);
                                }
                            } else {
                                // 新增记录
                                addNewTask(setting, cron);
                            }
                        } else {
                            String msg = String.format("表达式【%s】不符合cron表达式的规范，请修改", cron);
//                            throw new RuntimeException(msg);
                            return;
                        }
                    }
                    if (oldIds.size() > 0) {
                        reScheduleTaskService.deleteByPrimaryKeys(oldIds.toArray());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private List<ReScheduleTask> getReScheduleTasks(ReExecPlanPluginSetting setting, String cron) {
        ReScheduleTask taskQuery = new ReScheduleTask();
        taskQuery.setPlanId(setting.getPlanId());
        taskQuery.setCronExp(cron);
        taskQuery.setPlanPluginId(setting.getId());
        return reScheduleTaskService.query(taskQuery);
    }

    private void addNewTask(ReExecPlanPluginSetting setting, String cron) throws ParseException {
        ReScheduleTask task = new ReScheduleTask();
        task.setId(UUIDUtil.getUUIDWithoutDash());
        task.setCreateTime(new Date());
        task.setPlanId(setting.getPlanId());
        task.setPlanPluginId(setting.getId());
        task.setCronExp(cron.trim());
        CronExpression ce = new CronExpression(cron);
        Date next = ce.getNextValidTimeAfter(task.getCreateTime());
        task.setNextExecTime(next);
        reScheduleTaskService.insert(task);
        ReExecPlan plan = reExecPlanService.findByPrimaryKey(task.getPlanId());
        logger.info("执行计划{}定时任务添加成功，定时执行表达式{}", plan.getPlanName(), cron);
    }
}
