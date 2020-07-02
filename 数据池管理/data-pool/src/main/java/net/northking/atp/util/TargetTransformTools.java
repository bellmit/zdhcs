package net.northking.atp.util;

import net.northking.atp.db.persistent.*;
import net.northking.atp.entity.InterfaceBusinessRule;
import net.northking.atp.entity.InterfaceReRulePluginInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/6/5 0005.
 */
public class TargetTransformTools {
    /**
     * 业务规则基础信息转换为interface
     * @param reInfo
     * @return
     */
    public InterfaceBusinessRule transReRuleToInterface(ReBusinessRules reInfo){
        InterfaceBusinessRule result =  new InterfaceBusinessRule();
        result.setId(reInfo.getId());
        result.setRuleName(reInfo.getRuleName());
        result.setRuleType(reInfo.getRuleType());
        result.setRuleStatus(reInfo.getRuleStatus());
        result.setBelongModule(reInfo.getBelongModule());
        result.setBelongFunction(reInfo.getBelongFunction());
        result.setBelongSubfunction(reInfo.getBelongSubfunction());
        result.setBelongTestItem(reInfo.getBelongTestItem());
        result.setPluginId(reInfo.getPluginId());
        result.setIsReusable(reInfo.getIsReusable());
        result.setUseTimeStart(reInfo.getUseTimeStart());
        result.setUseTimeStop(reInfo.getUseTimeStop());
        result.setCreateStaff(reInfo.getCreateStaff());
        result.setCreateTime(reInfo.getCreateTime());
        result.setProjectId(reInfo.getProjectId());
        return result;
    }

    /**
     * 插件基础信息转换为interface
     * @param reInfo
     * @return
     */
    public InterfaceReRulePluginInfo transRePluginToInterface(ReRulePluginInfo reInfo){
        InterfaceReRulePluginInfo result =  new InterfaceReRulePluginInfo();
        result.setId(reInfo.getId());
        result.setRuleName(reInfo.getRuleName());
        result.setDataName(reInfo.getDataName());
        result.setRuleType(reInfo.getRuleType());
        result.setDescription(reInfo.getDescription());
        result.setVersion(reInfo.getVersion());
        result.setLibraryId(reInfo.getLibraryId());
        result.setIsValid(reInfo.getIsValid());
        result.setParameterNumber(reInfo.getParameterNumber());
        return result;
    }
}
