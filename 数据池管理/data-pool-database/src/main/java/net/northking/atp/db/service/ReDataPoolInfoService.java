/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.service;

import net.northking.db.BasicService;
import net.northking.atp.db.persistent.ReDataPoolInfo;

import java.util.List;

public interface ReDataPoolInfoService extends BasicService<ReDataPoolInfo>
{
// ----      The End by Generator     ----//
    String queryMaxName(ReDataPoolInfo nameQuery);
    List<ReDataPoolInfo> queryIdForInsert(ReDataPoolInfo query);
}
