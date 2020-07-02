package net.northking.atp.service;

import net.northking.atp.db.persistent.RePlanExecInfo;
import net.northking.atp.db.persistent.RuEngineJob;
import net.northking.atp.db.persistent.RuJobProAttachment;
import net.northking.atp.entity.EngineJob;
import net.northking.atp.entity.JobPro;
import net.northking.atp.entity.JobProAttachment;
import net.northking.atp.entity.PlanExecInfo;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;

import java.util.List;
import java.util.Map;

public interface PlanExecuteInfoService {

    /**
     * 生成计划执行记录和测试记录
     *
     * @param planId 执行计划主键id
     * @return 执行计划记录信息
     */
    RePlanExecInfo doPlanExecute(String planId);

    RePlanExecInfo doPlanExecute(String planId, String runProjectId);

    void updateExecuteInfo();

    PlanExecInfo queryPlanExecInfo(String planExecId);

    Pagination<EngineJob> queryEngineJobsByPage(Map<String, Object> query);

    Pagination<JobPro> queryJobProsByPage(Map<String, Object> query, OrderBy orderBy, Integer pageNo, Integer pageSize);

    void getTestReportByPlanExecId(String planExecId);

    List<EngineJob> getEngineJobs(List<RuEngineJob> ruEngineJobs);

    void planGetTriggeredByPlanId(String sourcePlanId);

    void updateEngineJobStatus();

    void deleteExecRecord(String planId);

    void addUrlToAttachment(List<RuJobProAttachment> resultList, List<JobProAttachment> attachments);
}
