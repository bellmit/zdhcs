package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReBusinessRulesParameter;
import net.northking.atp.db.persistent.ReRulePluginInfo;
import net.northking.atp.db.persistent.ReRulePluginParameter;

import java.util.List;

/**
 * Created by Administrator on 2019/7/24 0024.
 */
@ApiModel(
        description = "业务规则"
)
public class InterfaceReRulePluginInfo extends ReRulePluginInfo {
    public static final String KEY_paramList = "paramList";

    @ApiModelProperty("规则参数list")
    private List<ReBusinessRulesParameter> paramList;

    public List<ReBusinessRulesParameter> getParamList() {
        return paramList;
    }

    public void setParamList(List<ReBusinessRulesParameter> paramList) {
        this.paramList = paramList;
    }
}
