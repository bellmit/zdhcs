package net.northking.atp.schedule;

import net.northking.atp.DistributedLockFeignClient;
import net.northking.atp.db.persistent.ReExecPlan;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.RequestWrapper;
import net.northking.atp.entity.ResultWrapper;
import net.northking.atp.enums.LockStatus;
import net.northking.atp.service.PlanExecuteInfoService;
import net.northking.atp.utils.LockEntityUtil;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class TemplateDeleteSchedule {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(TemplateDeleteSchedule.class);

    @Autowired
    private ReExecPlanService reExecPlanService;

    @Autowired
    private HisExecPlanCaseSetRelService hisExecPlanCaseSetRelService;

    @Autowired
    private ReExecPlanCaseSetRelService reExecPlanCaseSetRelService;

    @Autowired
    private RePlanEnvRelService rePlanEnvRelService;

    @Autowired
    private RePlanExecInfoService rePlanExecInfoService;

    @Autowired
    private RuEngineJobService ruEngineJobService;

    @Autowired
    private RuJobProService ruJobProService;

    @Autowired
    private RuJobProParamService ruJobProParamService;

    @Autowired
    private RuJobProLogService ruJobProLogService;

    @Autowired
    private RuJobProAttachmentService ruJobProAttachmentService;

    @Autowired
    private DistributedLockFeignClient distributedLockFeignClient;

    @Value("${atp.plan.debug_project_id:DEBUG01}")
    private String DEBUG_PROJECT_ID;

    /**
     * 定时删除调试产生的临时信息
     * projectId -> DEBUG01
     * 每天凌晨3天删除当天之前的调试信息
     */
    @Scheduled(cron = "0 0 3 1/1 * ? ")
    public void deleteInfoScheduled() {
        RequestWrapper requestWrapper = LockEntityUtil.getRequestWrapper(DEBUG_PROJECT_ID);
        ResultWrapper resultWrapper = distributedLockFeignClient.immediateLock(requestWrapper);
        try {
            if (LockStatus.SUCCESS.code().equals(resultWrapper.getResult())) {
                deleteTempInfos();
            }
        } catch (Exception e) {
            logger.error("更新任务步骤信息失败，未找到微服务{}", "atp-distributed-lock");
        } finally {
            try {
                distributedLockFeignClient.unlock(requestWrapper);
            } catch (Exception e) {
                logger.error("distributedLockFeignClient释放锁失败");
            }
        }
    }

    /**
     * 删除调试产生的临时信息
     */
    public void deleteTempInfos() {
        logger.info("开始删除临时");
        // 查询所有临时计划
        ReExecPlan query = new ReExecPlan();
        query.setProjectId(DEBUG_PROJECT_ID);
        query.setCreateTimeLt(new Date());
        long total = reExecPlanService.queryCount(query);
        // 按时间排序
        OrderBy orderBy = new SqlOrderBy();
        orderBy.addOrderBy(ReExecPlan.KEY_createTime, OrderBy.ASC);
        int pageNo = 1;
        int pageSize = 50;
        int count = (int) total / pageSize;
        for (int i = 0; i < count; i++) {
            Pagination<ReExecPlan> page = reExecPlanService.query(query, orderBy, pageNo, pageSize);
            List<String> planIds = new ArrayList<>();
            for (ReExecPlan plan : page.getRecords()) {
                planIds.add(plan.getId());
            }
            if (planIds.size() == 0) {
                logger.info("没有可以删除的临时信息");
                return;
            }
            // 删除 his_exec_plan_case_set_rel
            hisExecPlanCaseSetRelService.batchDeleteByPlanIds(planIds);
            // 删除 re_exec_plan_case_set_rel
            reExecPlanCaseSetRelService.batchDeleteByPlanIds(planIds);
            // 删除 re_plan_env_rel
            rePlanEnvRelService.batchDeleteByPlanIds(planIds);
            // 删除 re_plan_exec_info ru_engine_job ru_job_pro ru_job_pro_param
            List<String> planExecIds = rePlanExecInfoService.queryByPlanIds(planIds);
            // 执行任务
            ruEngineJobService.batchDeleteByPlanExecIds(planExecIds);
            // 执行任务步骤
            ruJobProService.batchDeleteByPlanExecIds(planExecIds);
            // 执行任务步骤参数
            ruJobProParamService.batchDeleteByPlanExecIds(planExecIds);
            // 执行任务步骤日志
            ruJobProLogService.batchDeleteByPlanExecIds(planExecIds);
            // 执行任务步骤附件
            ruJobProAttachmentService.batchDeleteByPlanExecIds(planExecIds);
            // 执行记录
            rePlanExecInfoService.batchDeleteByPlanIds(planIds);
            // 删除临时计划
            reExecPlanService.deleteByPrimaryKeys(planIds.toArray());
        }
        logger.info("删除临时信息成功");

    }
}
