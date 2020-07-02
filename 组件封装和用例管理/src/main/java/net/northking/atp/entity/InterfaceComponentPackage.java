package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReComponentInfo;
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
public class InterfaceComponentPackage extends ReComponentPackage implements POJO {
    public static final String KEY_componentNo = "componentNo";
    public static final String KEY_componentName = "componentName";
    public static final String KEY_complexity = "complexity";
    public static final String KEY_staffName = "staffName";
    public static final String KEY_paramList = "paramList";
    public static final String KEY_paramCheck = "paramCheck";
    public static final String KEY_checkRemark = "checkRemark";
    public static final String KEY_version = "version";
    public static final String KEY_parameterNumber = "parameterNumber";
    public static final String KEY_comFlag = "componentFlag";
    public static final String KEY_libraryName = "libraryName";

    @ApiModelProperty("组件编号")
    private String componentNo;
    @ApiModelProperty("组件名称")
    private String componentName;
    @ApiModelProperty("组件复杂度")
    private String complexity;
    @ApiModelProperty("更改人")
    private String staffName;
    @ApiModelProperty("参数list")
    private List<ReComponentParameter> paramList;
    @ApiModelProperty("参数校验")
    private String paramCheck;
    @ApiModelProperty("校验信息")
    private String checkMessage;
    @ApiModelProperty("版本号")
    private String version;
    @ApiModelProperty("组件参数个数")
    private String parameterNumber;
    @ApiModelProperty("组件标识:0或者不写为组件，1-智能设计")
    private String componentFlag;
    @ApiModelProperty("从属库名")
    private String libraryName;

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

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public List<ReComponentParameter> getParamList() {
        return paramList;
    }

    public void setParamList(List<ReComponentParameter> paramList) {
        this.paramList = paramList;
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

    public String getParameterNumber() {
        return parameterNumber;
    }

    public void setParameterNumber(String parameterNumber) {
        this.parameterNumber = parameterNumber;
    }

    public String getComponentFlag() {
        return componentFlag;
    }

    public void setComponentFlag(String componentFlag) {
        this.componentFlag = componentFlag;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    @Override
    public Map<String, Object> toMap() {
        HashMap valueMap = new HashMap();
        if(this.componentNo != null) {
            valueMap.put("componentNo", this.componentNo);
        }
        if(this.componentName != null) {
            valueMap.put("componentName", this.componentName);
        }
        if(this.paramList != null){
            valueMap.put("paramList", this.paramList);
        }
        return valueMap;
    }
}
