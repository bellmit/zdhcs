package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.MdComponentPackage;
import net.northking.atp.db.persistent.MdComponentParameter;
import net.northking.atp.db.persistent.ReComponentPackage;
import net.northking.atp.db.persistent.ReComponentParameter;
import net.northking.db.POJO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/4/1 0001.
 */
@ApiModel(
        description = "组件封装"
)
public class InterfaceComponentPacModify extends MdComponentPackage implements POJO {
    public static final String KEY_componentNo = "componentNo";
    public static final String KEY_componentId = "componentId";
    public static final String KEY_componentName = "componentName";
    public static final String KEY_complexity = "complexity";
    public static final String KEY_projectId = "projectId";
    public static final String KEY_staffName = "staffName";
    public static final String KEY_paramList = "paramList";
    public static final String KEY_paramCheck = "paramCheck";
    public static final String KEY_checkRemark = "checkRemark";
    public static final String KEY_version = "version";

    @ApiModelProperty("组件编号")
    private String componentNo;
    @ApiModelProperty("组件Id")
    private String componentId;
    @ApiModelProperty("组件名称")
    private String componentName;
    @ApiModelProperty("组件复杂度")
    private String complexity;
    @ApiModelProperty("系统编号")
    private String projectId;
    @ApiModelProperty("更改人")
    private String staffName;
    @ApiModelProperty("参数list")
    private List<MdComponentParameter> paramList;
    @ApiModelProperty("参数校验")
    private String paramCheck;
    @ApiModelProperty("校验信息")
    private String checkMessage;
    @ApiModelProperty("版本号")
    private String version;

    public String getComponentNo() {
        return componentNo;
    }

    public void setComponentNo(String componentNo) {
        this.componentNo = componentNo;
    }

    @Override
    public String getComponentId() {
        return componentId;
    }

    @Override
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public List<MdComponentParameter> getParamList() {
        return paramList;
    }

    public void setParamList(List<MdComponentParameter> paramList) {
        this.paramList = paramList;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getParamCheck() {
        return paramCheck;
    }

    public void setParamCheck(String paramCheck) {
        this.paramCheck = paramCheck;
    }

    public String getCheckMessage() {
        return checkMessage;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public Map<String, Object> toMap() {
        HashMap valueMap = new HashMap();
        if(this.componentName != null) {
            valueMap.put("componentName", this.componentName);
        }
        if(this.paramList != null){
            valueMap.put("paramList", this.paramList);
        }
        return valueMap;
    }
}
