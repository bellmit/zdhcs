package net.northking.atp.entity;

import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReCaseDesignInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/4/22 0022.
 */
public class InterfaceCaseInfo extends ReCaseDesignInfo{
    public static final String KEY_setId = "setId";
    public static final String KEY_componentList = "componentList";
    public static final String KEY_caseId = "caseId";
    public static final String KEY_dataDesignDataList = "dataDesignDataList";
    public static final String KEY_addType = "addType";
    public static final String KEY_access_token = "access_token";
    public static final String KEY_caseJson = "caseJson";

    @ApiModelProperty("用例集ID")
    private String setId;
    @ApiModelProperty("组件list")
    private List<InterfaceStepComponent> componentList;
    @ApiModelProperty("主键")
    private String caseId;
    @ApiModelProperty("参数list")
    private List<Map<String,String>> dataDesignDataList;
    @ApiModelProperty("增加方式0-增量，1-覆盖")
    private String addType;
    @ApiModelProperty("token")
    private String access_token;
    @ApiModelProperty("caseJson")
    private String caseJson;


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

    public List<Map<String, String>> getDataDesignDataList() {
        return dataDesignDataList;
    }

    public void setDataDesignDataList(List<Map<String, String>> dataDesignDataList) {
        this.dataDesignDataList = dataDesignDataList;
    }

    public String getAddType() {
        return addType;
    }

    public void setAddType(String addType) {
        this.addType = addType;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getCaseJson() {
        return caseJson;
    }

    public void setCaseJson(String caseJson) {
        this.caseJson = caseJson;
    }
}
