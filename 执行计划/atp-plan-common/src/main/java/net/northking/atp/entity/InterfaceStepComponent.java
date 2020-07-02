package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReComponentParameter;

import java.util.List;

/**
 * 步骤组件关联
 * Created by Administrator on 2019/4/9 0009.
 */
@ApiModel(
        description = "步骤组件关联"
)
public class InterfaceStepComponent {
    public static final String KEY_id = "id";
    public static final String KEY_componentId = "componentId";
    public static final String KEY_stepId= "stepId";
    public static final String KEY_componentNo = "componentNo";
    public static final String KEY_componentName = "componentName";
    public static final String KEY_componentFlag = "componentFlag";
    public static final String KEY_version = "version";
    public static final String KEY_paramCheck = "paramCheck";
    public static final String KEY_checkRemark = "checkRemark";
    public static final String KEY_projectId = "projectId";
    public static final String KEY_staffName = "staffName";
    public static final String KEY_paramList = "paramList";
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("组件Id")
    private String componentId;
    @ApiModelProperty("步骤Id")
    private String stepId;
    @ApiModelProperty("组件编号")
    private String componentNo;
    @ApiModelProperty("组件名称")
    private String componentName;
    @ApiModelProperty("组件标志")
    private String componentFlag;
    @ApiModelProperty("组件版本")
    private String version;
    @ApiModelProperty("校验信息")
    private String paramCheck;
    @ApiModelProperty("组件名称")
    private String checkMessage;
    @ApiModelProperty("系统编号")
    private String projectId;
    @ApiModelProperty("更改人")
    private String staffName;
    @ApiModelProperty("参数list")
    private List<ReComponentParameter> paramList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getComponentNo() {
        return componentNo;
    }

    public void setComponentNo(String componentNo) {
        this.componentNo = componentNo;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentFlag() {
        return componentFlag;
    }

    public void setComponentFlag(String componentFlag) {
        this.componentFlag = componentFlag;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public List<ReComponentParameter> getParamList() {
        return paramList;
    }

    public void setParamList(List<ReComponentParameter> paramList) {
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
}
