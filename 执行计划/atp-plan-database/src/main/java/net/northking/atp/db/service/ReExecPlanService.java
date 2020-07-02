/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.service;

import net.northking.atp.db.persistent.ReExecPlan;
import net.northking.db.BasicService;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;

public interface ReExecPlanService extends BasicService<ReExecPlan>
{
// ----      The End by Generator     ----//
Pagination selectPlanListByPage(ReExecPlan reExecPlan, OrderBy orderBy, int pageNo, int pageSize);
}
