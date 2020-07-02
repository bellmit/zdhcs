/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.service;

import net.northking.db.BasicService;
import net.northking.atp.db.persistent.ReBusinessRules;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;

public interface ReBusinessRulesService extends BasicService<ReBusinessRules>
{
// ----      The End by Generator     ----//
    Pagination selectBusinessRules(ReBusinessRules info, OrderBy orderBy, int pageNo, int pageSize);

}
