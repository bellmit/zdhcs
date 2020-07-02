package net.northking.atp.entity;

import com.sun.org.apache.regexp.internal.RE;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReBusinessRules;
import net.northking.atp.db.persistent.ReBusinessRulesParameter;

import java.util.List;

/**
 * Created by Administrator on 2019/7/18 0018.
 */
@ApiModel(
        description = "业务规则"
)
public class InterfaceBusinessRule extends ReBusinessRules{
    public static final String KEY_paramList = "paramList";
    public static final String KEY_idList = "idList";
    public static final String KEY_num = "num";

    @ApiModelProperty("规则参数list")
    private List<ReBusinessRulesParameter> paramList;
    @ApiModelProperty("idList")
    private List<String> idList;
    @ApiModelProperty("生成数量")
    private Integer num;

    public List<ReBusinessRulesParameter> getParamList() {
        return paramList;
    }

    public void setParamList(List<ReBusinessRulesParameter> paramList) {
        this.paramList = paramList;
    }

    public List<String> getIdList() {
        return idList;
    }

    public void setIdList(List<String> idList) {
        this.idList = idList;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}
