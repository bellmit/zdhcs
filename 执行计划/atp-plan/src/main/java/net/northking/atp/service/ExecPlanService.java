package net.northking.atp.service;

import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.db.persistent.ReTestEnvInfo;
import net.northking.atp.entity.ExecutePlan;
import net.northking.atp.entity.TestEnvInfo;
import net.northking.db.Pagination;

public interface ExecPlanService {

    ExecutePlan addExecPlan(ExecutePlan target);

    Pagination<ExecutePlan> queryExecutePlan(QueryByPage<ExecutePlan> target);

    ExecutePlan queryPlanSettingById(String planId);

    void updateExecPlan(ExecutePlan plan);

    int deleteExecPlan(String planId);

    boolean pluginIsEnable(String planPluginId);

}
