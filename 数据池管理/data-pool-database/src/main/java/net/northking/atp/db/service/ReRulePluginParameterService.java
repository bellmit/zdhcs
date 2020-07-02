/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.service;

import net.northking.db.BasicService;
import net.northking.atp.db.persistent.ReRulePluginParameter;

import java.util.List;
import java.util.Map;

public interface ReRulePluginParameterService extends BasicService<ReRulePluginParameter>
{
// ----      The End by Generator     ----//
    List<ReRulePluginParameter> queryParamForRuleList(Map<String,Object> query);
}
