package net.northking.atp.schedule;

import net.northking.atp.db.persistent.ReExecPlan;
import net.northking.atp.db.persistent.ReExecPlanPluginSetting;
import net.northking.atp.db.persistent.ReScheduleTask;
import net.northking.atp.db.persistent.ReScheduleTaskLog;
import net.northking.atp.db.service.ReExecPlanPluginSettingService;
import net.northking.atp.db.service.ReExecPlanService;
import net.northking.atp.db.service.ReScheduleTaskLogService;
import net.northking.atp.db.service.ReScheduleTaskService;
import net.northking.atp.enums.PluginSettingStatus;
import net.northking.atp.service.PlanExecuteInfoService;
import net.northking.atp.utils.UUIDUtil;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class PlanExecuteJob implements Job {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(PlanExecuteJob.class);

    @Autowired
    private ReExecPlanService reExecPlanService;

    @Autowired
    private PlanExecuteInfoService planExecuteInfoService;

    @Autowired
    private ReScheduleTaskService reScheduleTaskService;

    @Autowired
    private ReExecPlanPluginSettingService reExecPlanPluginSettingService;

    @Autowired
    private ReScheduleTaskLogService reScheduleTaskLogService;

    /**
     * 计划定时执行的任务设置
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        ReScheduleTask task = (ReScheduleTask) dataMap.get("task");
        // 1.判断执行时间是否在执行计划的有效时间范围
        ReExecPlan planInfo = reExecPlanService.findByPrimaryKey(task.getPlanId());
        if (planInfo.getStartTime() != null && planInfo.getEndTime() != null) {
            if (task.getNextExecTime().getTime() > planInfo.getEndTime().getTime()) {
                return;
            }
        }
        ReExecPlanPluginSetting setting = reExecPlanPluginSettingService.findByPrimaryKey(task.getPlanPluginId());
        // 2.判断计划的定时插件是否有使用
        if (PluginSettingStatus.DISABLE.code() == setting.getStatus()) {
            return;
        }
        // 3.计划的执行
        planExecuteInfoService.doPlanExecute(task.getPlanId());
        // 4.更新相关状态和执行时间
        try {
            task.setActuExecTime(new Date());   // 实际执行执行时间
            // 5.创建执行日志信息
            addTaskLog(task);
            // 6.更新下一次执行时间
            task.setLastExecTime(task.getActuExecTime());   // 最近一次执行时间
            CronExpression ce = new CronExpression(task.getCronExp());
            Date nextTime = ce.getNextValidTimeAfter(task.getActuExecTime());
            task.setNextExecTime(nextTime); // 设置下次执行时间
            task.setActuExecTime(null);     // 执行时间设置为空
            reScheduleTaskService.updateByPrimaryKey(task);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加定时执行的相关信息
     * @param task  定时任务信息
     */
    private void addTaskLog(ReScheduleTask task) {
        ReScheduleTaskLog taskLog = new ReScheduleTaskLog();
        taskLog.setId(UUIDUtil.getUUIDWithoutDash());
        taskLog.setCreateTime(new Date());
        taskLog.setCronExp(task.getCronExp());
        taskLog.setLastExecTime(task.getLastExecTime());
        taskLog.setPlanPluginId(task.getPlanPluginId());
        taskLog.setPlanId(task.getPlanId());
        taskLog.setTaskCreateTime(task.getTaskCreateTime());
        taskLog.setActuExecTime(task.getActuExecTime());
        taskLog.setNextExecTime(task.getNextExecTime()); // 下次执行时间
        reScheduleTaskLogService.insert(taskLog);
    }

}
