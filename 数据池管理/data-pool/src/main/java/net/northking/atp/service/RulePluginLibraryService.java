package net.northking.atp.service;

import net.northking.atp.db.persistent.ReRulePluginInfo;
import net.northking.atp.db.persistent.ReRulePluginLibrary;
import net.northking.atp.db.persistent.ReRulePluginParameter;
import net.northking.atp.element.core.Library;
import net.northking.atp.element.core.keyword.KeywordInfo;
import net.northking.atp.rule.core.RuleLibrary;
import net.northking.atp.rule.core.rule.RuleInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/7/16 0016.
 */
public interface RulePluginLibraryService {
    int versionCheck(RuleLibrary libraryInfo , ReRulePluginLibrary target,
                     List<ReRulePluginLibrary> checkList);
    public void  getListForRulePluginInsert(List<ReRulePluginInfo> comList, Map<String,List<ReRulePluginParameter>> paramMap,
                                            ReRulePluginLibrary target, List<RuleInfo> keyList, String version, String libraryId);
    void insertRulePluginInfo(List<ReRulePluginInfo> infoList, Map<String,List<ReRulePluginParameter>> paramMap,
                                 int checkResult, String id, ReRulePluginLibrary target, Map<String,Object> toolMap);

    void insertRulePluginParam(List<ReRulePluginParameter> paramList,int checkResult,
                                      String id,ReRulePluginLibrary target,Map<String,Object> toolMap);
}
