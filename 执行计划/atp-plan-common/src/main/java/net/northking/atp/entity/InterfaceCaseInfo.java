package net.northking.atp.entity;

import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReCaseDesignInfo;

import java.util.List;

/**
 * Created by Administrator on 2019/4/22 0022.
 */
public class InterfaceCaseInfo extends ReCaseDesignInfo {
    public static final String KEY_setId = "setId";
    public static final String KEY_componentList = "componentList";
    public static final String KEY_caseId = "caseId";

    @ApiModelProperty("用例集ID")
    private String setId;
    @ApiModelProperty("组件list")
    private List<InterfaceStepComponent> componentList;
    @ApiModelProperty("主键")
    private String caseId;

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public List<InterfaceStepComponent> getComponentList() {
        return componentList;
    }

    public void setComponentList(List<InterfaceStepComponent> componentList) {
        this.componentList = componentList;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
}
