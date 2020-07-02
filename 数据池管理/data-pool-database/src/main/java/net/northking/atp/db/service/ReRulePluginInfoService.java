/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.service;

import net.northking.db.BasicService;
import net.northking.atp.db.persistent.ReRulePluginInfo;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;

public interface ReRulePluginInfoService extends BasicService<ReRulePluginInfo>
{
// ----      The End by Generator     ----//
    Pagination selectRulesByPage(ReRulePluginInfo info, OrderBy orderBy, int pageNo, int pageSize);
    int updateByLibraryId(ReRulePluginInfo info);
}
