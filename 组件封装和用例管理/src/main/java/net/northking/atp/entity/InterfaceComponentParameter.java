package net.northking.atp.entity;

import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReComponentParameter;

/**
 * Created by Administrator on 2020/3/12 0012.
 */
public class InterfaceComponentParameter extends ReComponentParameter {
    public static final String KEY_paramCode = "paramCode";

    @ApiModelProperty("数据设计替换主键")
    private String paramCode;

    public String getParamCode() {
        return paramCode;
    }

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }
}
