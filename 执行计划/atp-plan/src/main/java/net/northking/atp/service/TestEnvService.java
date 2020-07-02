package net.northking.atp.service;

import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.db.persistent.ReTestEnvInfo;
import net.northking.atp.entity.TestEnvInfo;
import net.northking.atp.enums.PlanClass;
import net.northking.db.Pagination;

import java.util.List;

public interface TestEnvService {
    Pagination<TestEnvInfo> queryTestEnvList(QueryByPage<ReTestEnvInfo> query);

    TestEnvInfo getTestEnvInfo(ReTestEnvInfo envInfo);

    ReTestEnvInfo checkAndAddDefaultEnv(String projectId,
                               String osType, String osVersion,
                               String bwType, String bwVersion,
                               PlanClass envType);

    List<TestEnvInfo> queryEnvInfoByOsInfo(TestEnvInfo testEnvInfo);
}
