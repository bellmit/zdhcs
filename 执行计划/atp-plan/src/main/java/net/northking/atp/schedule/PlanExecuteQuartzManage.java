package net.northking.atp.schedule;

import net.northking.atp.DistributedLockFeignClient;
import net.northking.atp.db.persistent.ReScheduleTask;
import net.northking.atp.db.service.ReScheduleTaskService;
import net.northking.atp.entity.RequestWrapper;
import net.northking.atp.entity.ResultWrapper;
import net.northking.atp.enums.LockStatus;
import net.northking.atp.service.ExecPlanService;
import net.northking.atp.utils.LockEntityUtil;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class PlanExecuteQuartzManage {
    // 日志
    private static final Logger logger = LoggerFactory.getLogger(PlanExecuteQuartzManage.class);

    public final static String TASK_KEY = "task";

    @Autowired
    private ReScheduleTaskService reScheduleTaskService;

    @Autowired
    private ExecPlanService execPlanService;

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private DistributedLockFeignClient distributedLockFeignClient;

    private final static String PLAN_EXECUTE_SCHEDULE = "planExecuteSchedule";

    @Scheduled(cron = "0/30 * * * * ?")
    public void createPlanExecuteScheduled() {
        // 请求分布式锁
        RequestWrapper requestWrapper = LockEntityUtil.getRequestWrapper(PLAN_EXECUTE_SCHEDULE);
        try {
            ResultWrapper resultWrapper = distributedLockFeignClient.immediateLock(requestWrapper);
            if (LockStatus.SUCCESS.code().equals(resultWrapper.getResult())) {
                scanPlanExecuteScheduled();
            }
        } catch (Exception e) {
            logger.error("创建定时执行计划失败，未找到微服务{}", "atp-distributed-lock");
        } finally {
            // 释放分布式锁
            try {
                distributedLockFeignClient.unlock(requestWrapper);
            } catch (Exception e) {
                //
            }
        }
    }


    /**
     * 给执行计划创建定时执行任务
     */
    private void scanPlanExecuteScheduled() {
        // 扫描记下来30分钟的需要执行的计划
        logger.info("扫描定时任务操作开始====>");
        // 根据条件获取需要产生定时任务的任务列表
        List<ReScheduleTask> taskList = getReScheduleTasks();
        // 遍历结果添加定时任务
        for (ReScheduleTask task : taskList) {
            try {
                // 添加定时任务
                boolean flag = addScheduledTask(task);
                if (flag) {
                    // 更新表数据，创建任务时间
                    task.setTaskCreateTime(new Date());
                    reScheduleTaskService.updateByPrimaryKey(task);
                }
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
        logger.info("扫描定时任务操作结束<====");
    }

    /**
     * 获取定时任务列表
     *
     * @return 定时任务集合
     */
    private List<ReScheduleTask> getReScheduleTasks() {
        ReScheduleTask taskQuery = new ReScheduleTask();
        long currentTime = System.currentTimeMillis();
        taskQuery.setNextExecTimeGe(new Date(currentTime - 60 * 1000L));
        long targetTime = 10 * 60 * 1000L + currentTime;
        taskQuery.setNextExecTimeLt(new Date(targetTime));
        return reScheduleTaskService.query(taskQuery);
    }

    /**
     * 创建定时任务
     *
     * @param task 定时任务信息
     * @return 创建定时任务标识
     * @throws SchedulerException
     */
    private boolean addScheduledTask(ReScheduleTask task) throws SchedulerException {
        if (!execPlanService.pluginIsEnable(task.getPlanPluginId())) {
            return false;
        }
        // 定时任务标识
        JobKey jobKey = new JobKey(task.getId());
        // 创建调度器
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        // 任务存在则返回
        if (scheduler.checkExists(jobKey)) {
            scheduler.getJobDetail(jobKey).getJobDataMap().put(TASK_KEY, task);
            logger.debug("定时任务{}已添加，不需要重复添加", task.getId());
            return false;
        }
        // 任务参数
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(TASK_KEY, task);
        // 创建jobDetail
        JobDetail detail = JobBuilder.newJob(PlanExecuteJob.class)
                .withIdentity(jobKey).setJobData(dataMap).build();
        // 创建触发器
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(task.getId())
                .startAt(task.getNextExecTime())
                .build();
        // 绑定job和触发器
        scheduler.scheduleJob(detail, trigger);
        // 定时任务开始
        scheduler.start();
        logger.info("计划【{}】已添加定时任务{}，下次执行时间{}", task.getPlanId(), task.getId(), task.getNextExecTime());
        // 成功标识
        return true;
    }

    /**
     * todo: 更新定时任务信息
     */
    public void updateTaskInfo(ReScheduleTask task) {
        TriggerKey triggerKey = TriggerKey.triggerKey(task.getId());
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            Trigger trigger = scheduler.getTrigger(triggerKey);

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * todo: 移除定时任务
     *
     * @param task
     */
    public void removeJob(ReScheduleTask task) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(task.getId());
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            // 停止触发器
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器
            scheduler.unscheduleJob(triggerKey);
            // 删除任务
            scheduler.deleteJob(JobKey.jobKey(task.getId()));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
