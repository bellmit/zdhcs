package net.northking.atp.service;

import net.northking.atp.db.persistent.ReBusinessRules;
import net.northking.atp.db.persistent.ReBusinessRulesParameter;
import net.northking.atp.entity.InterfaceBusinessRule;

import java.util.List;

/**
 * Created by Administrator on 2019/7/16 0016.
 */
public interface BusinessRulesService {
    boolean checkBusinessRulesExist(ReBusinessRules info);
    int insertRulesInfo(ReBusinessRules info);
    void updateRulesInfo(ReBusinessRules info);
    void deleteRulesInfo(ReBusinessRules info);
    void saveRuleParam(InterfaceBusinessRule info);
    String queryBusinessRuleValue(ReBusinessRules info);
    List<String> genDataListByRuleId(InterfaceBusinessRule info);
}
