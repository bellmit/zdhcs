package net.northking.atp.impl;

import net.northking.atp.db.persistent.ReBusinessRules;
import net.northking.atp.db.service.ReBusinessRulesService;
import net.northking.atp.service.BusinessRulesService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2019/7/15 0015.
 */
@Service
public class BusinessRulesServiceImpl implements BusinessRulesService{
    @Autowired
    private ReBusinessRulesService reBusinessRulesService;

    /**
     * 校验业务规则是否存在
     * @param info
     * @return
     */
    @Override
    public boolean checkBusinessRulesExist(ReBusinessRules info) {
        ReBusinessRules check = new ReBusinessRules();
        check.setProjectId(info.getProjectId());
        check.setRuleName(info.getRuleName());
        check.setRuleType(info.getRuleType());
        List<ReBusinessRules> checkList = new ArrayList<ReBusinessRules>();
        if(checkList != null && checkList.size()>0){
            if(checkList.get(0).getId().equals(info.getRuleName())){
                //业务规则校验通过
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * 插入业务规则数据
     * @param info
     * @return
     */
    @Override
    public int insertRulesInfo(ReBusinessRules info) {
        CaseDesignTools tools = new CaseDesignTools();
        //新增
        info.setId(tools.getUUID());
        info.setCreateTime(new Date());
        return reBusinessRulesService.insert(info);
    }

    /**
     * 修改业务规则数据
     * @param info
     */
    @Override
    public void updateRulesInfo(ReBusinessRules info) {
        reBusinessRulesService.updateByPrimaryKey(info);
    }

    /**
     * 删除业务规则
     * @param info
     */
    @Override
    public void deleteRulesInfo(ReBusinessRules info) {
        reBusinessRulesService.deleteByPrimaryKey(info.getId());
    }
}
