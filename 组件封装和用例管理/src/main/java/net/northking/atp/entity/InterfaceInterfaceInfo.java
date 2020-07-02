package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.MdInterfaceInfo;
import net.northking.atp.db.persistent.ReInterfaceInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/6/24 0024.
 */
@ApiModel(
        description = "接口封装信息"
)
public class InterfaceInterfaceInfo extends MdInterfaceInfo {
    public static final String KEY_dataList = "dataList";

    @ApiModelProperty("参数list")
    private Map<String,Object> dataMap;

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }
}
