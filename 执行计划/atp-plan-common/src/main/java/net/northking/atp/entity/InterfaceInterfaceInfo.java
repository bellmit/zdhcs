package net.northking.atp.entity;

import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.MdInterfaceInfo;

import java.util.Map;

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
