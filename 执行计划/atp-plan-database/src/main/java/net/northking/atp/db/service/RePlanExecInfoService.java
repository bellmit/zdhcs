/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.service;

import net.northking.atp.db.persistent.RePlanExecInfo;
import net.northking.db.BasicService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RePlanExecInfoService extends BasicService<RePlanExecInfo>
{
// ----      The End by Generator     ----//
    // ----      The End by Generator     ----//

    RePlanExecInfo queryFirstById(String planId);

    long batchDeleteByPlanIds(List<String> planIds);

    List<String> queryByPlanIds(List<String> planIds);
}
