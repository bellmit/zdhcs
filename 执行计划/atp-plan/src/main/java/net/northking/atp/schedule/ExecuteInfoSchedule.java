package net.northking.atp.schedule;

import net.northking.atp.DistributedLockFeignClient;
import net.northking.atp.db.persistent.ReExecPlan;
import net.northking.atp.db.persistent.RePlanEnvRel;
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
public class ExecuteInfoSchedule {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(ExecuteInfoSchedule.class);

    @Autowired
    private PlanExecuteInfoService planExecuteInfoService;    // 引擎任务

    @Autowired
    private DistributedLockFeignClient distributedLockFeignClient;

    private final static String UPDATE_EXECUTEINFO_KEY = "updateExecuteInfo";

    private final static String UPDATE_ENGINE_JOS_STATUS = "updateEngineJobStatus";

    private final static String DELETE_TEMP_INFOS = "deleteTempInfos";

    /**
     * 定时更新执行记录的执行状态和执行结果
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void updateExecuteInfo() {
        RequestWrapper requestWrapper = LockEntityUtil.getRequestWrapper(UPDATE_EXECUTEINFO_KEY);
        try {
            ResultWrapper resultWrapper = distributedLockFeignClient.immediateLock(requestWrapper);
            if (LockStatus.SUCCESS.code().equals(resultWrapper.getResult())) {
                planExecuteInfoService.updateExecuteInfo();
            }
        } catch (Exception e) {
            logger.error("更新记录状态和结果失败，未找到微服务{}", "atp-distributed-lock");
        } finally {
            try {
                distributedLockFeignClient.unlock(requestWrapper);
            } catch (Exception e) {
                logger.error("distributedLockFeignClient释放锁失败");
            }
        }
    }

    /**
     * 定时更新执行任务步骤下的执行结果
     */
//    @Scheduled(cron = "0/30 * * * * ?")
    public void updateEngineJobStatus() {
        RequestWrapper requestWrapper = LockEntityUtil.getRequestWrapper(UPDATE_ENGINE_JOS_STATUS);
        ResultWrapper resultWrapper = distributedLockFeignClient.immediateLock(requestWrapper);
        try {
            if (LockStatus.SUCCESS.code().equals(resultWrapper.getResult())) {
                planExecuteInfoService.updateEngineJobStatus();
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

}
