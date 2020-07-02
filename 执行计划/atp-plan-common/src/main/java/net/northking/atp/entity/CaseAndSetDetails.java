package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseSet;

import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "用例-用例集包装类")
public class CaseAndSetDetails {

    public final static String KEY_reCaseDesignInfo = "reCaseDesignInfo";
    public final static String KEY_reCaseSet = "reCaseSet";

    @ApiModelProperty(value = "用例名称")
    private ReCaseDesignInfo reCaseDesignInfo;

    @ApiModelProperty(value = "用例集名称")
    private ReCaseSet reCaseSet;

    public ReCaseDesignInfo getReCaseDesignInfo() {
        return reCaseDesignInfo;
    }

    public void setReCaseDesignInfo(ReCaseDesignInfo reCaseDesignInfo) {
        this.reCaseDesignInfo = reCaseDesignInfo;
    }

    public ReCaseSet getReCaseSet() {
        return reCaseSet;
    }

    public void setReCaseSet(ReCaseSet reCaseSet) {
        this.reCaseSet = reCaseSet;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        if (reCaseDesignInfo != null) {
            map.put(KEY_reCaseDesignInfo, reCaseDesignInfo);
        }
        if (reCaseSet != null) {
            map.put(KEY_reCaseSet, reCaseSet);
        }
        return map;
    }
}
