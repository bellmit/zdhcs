/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.service;

import net.northking.db.BasicService;
import net.northking.atp.db.persistent.ReRulePluginLibrary;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;

public interface ReRulePluginLibraryService extends BasicService<ReRulePluginLibrary>
{
// ----      The End by Generator     ----//
    Pagination selectRulePlugins(ReRulePluginLibrary info, OrderBy orderBy, int pageNo, int pageSize);
}
