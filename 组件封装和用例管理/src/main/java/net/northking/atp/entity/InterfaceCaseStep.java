package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * 案例步骤关联
 * Created by Administrator on 2019/4/9 0009.
 */
@ApiModel(
        description = "案例步骤关联"
)
public class InterfaceCaseStep {
    public static final String KEY_projectId = "projectId";
    public static final String KEY_caseNo = "caseNo";
    public static final String KEY_caseId = "caseId";
    public static final String KEY_id = "id";
    public static final String KEY_componentList = "componentList";
    @ApiModelProperty("案例编号")
    private String caseNo;
    @ApiModelProperty("案例主键")
    private String caseId;
    @ApiModelProperty("主键")
    private String id;
    @ApiModelProperty("项目编号")
    private String projectId;
    @ApiModelProperty("组件list")
    private List<InterfaceStepComponent> componentList;

    public String getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(String caseNo) {
        this.caseNo = caseNo;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<InterfaceStepComponent> getComponentList() {
        return componentList;
    }

    public void setComponentList(List<InterfaceStepComponent> componentList) {
        this.componentList = componentList;
    }
}

