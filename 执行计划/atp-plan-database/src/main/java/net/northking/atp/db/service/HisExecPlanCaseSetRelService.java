/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.service;

import net.northking.atp.db.persistent.HisExecPlanCaseSetRel;
import net.northking.db.BasicService;

import java.util.List;

public interface HisExecPlanCaseSetRelService extends BasicService<HisExecPlanCaseSetRel>
{
    // ----      The End by Generator     ----//
    // 根据计划集合批量删除历史用例集
    long batchDeleteByPlanIds(List<String> planIds);
}
