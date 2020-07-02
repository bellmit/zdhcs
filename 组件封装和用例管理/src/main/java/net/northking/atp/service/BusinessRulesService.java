package net.northking.atp.service;

import net.northking.atp.db.persistent.ReBusinessRules;

/**
 * Created by Administrator on 2019/7/15 0015.
 */
public interface BusinessRulesService {
    boolean checkBusinessRulesExist(ReBusinessRules info);
    int insertRulesInfo(ReBusinessRules info);
    void updateRulesInfo(ReBusinessRules info);
    void deleteRulesInfo(ReBusinessRules info);

}
