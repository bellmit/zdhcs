package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

@ApiModel(description = "测试执行计划定义表")
public class ExecutePlan extends ReExecPlan {

    public static final String KEY_pluginLists = "pluginLists";
    public static final String KEY_caseSets = "caseSets";
    public static final String KEY_caseSetsInfo = "caseSetsInfo";
    public static final String KEY_envInfos = "envInfos";


    @ApiModelProperty(value = "测试计划插件设置")
    private List<ReExecPlanPluginSetting> pluginLists;

    @ApiModelProperty(value = "关联测试集")
    private String[] caseSets;

    @ApiModelProperty(value = "关联测试集")
    private List<ReCaseSet> caseSetsInfo;

    @ApiModelProperty(value = "关联的测试环境")
    private List<TestEnvInfo> envInfos;

    @ApiModelProperty(value = "最近一次执行结果")
    private RePlanExecInfo rePlanExecInfo;

    @ApiModelProperty(value = "计划开始时间")
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private Date startTime;

    @ApiModelProperty(value = "计划结束时间")
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private Date endTime;

    @ApiModelProperty(value = "数据环境信息")
    private ReProfileInfo reProfileInfo;

    @ApiModelProperty(value = "执行引擎所属地区")
    private String envArea;

    public ExecutePlan() {
    }

    public ReProfileInfo getReProfileInfo() {
        return reProfileInfo;
    }

    public void setReProfileInfo(ReProfileInfo reProfileInfo) {
        this.reProfileInfo = reProfileInfo;
    }

    public List<ReExecPlanPluginSetting> getPluginLists() {
        return pluginLists;
    }

    public void setPluginLists(List<ReExecPlanPluginSetting> pluginLists) {
        this.pluginLists = pluginLists;
    }

    public String[] getCaseSets() {
        return caseSets;
    }

    public void setCaseSets(String[] caseSets) {
        this.caseSets = caseSets;
    }

    public RePlanExecInfo getRePlanExecInfo() {
        return rePlanExecInfo;
    }

    public void setRePlanExecInfo(RePlanExecInfo rePlanExecInfo) {
        this.rePlanExecInfo = rePlanExecInfo;
    }

    public List<TestEnvInfo> getEnvInfos() {
        return envInfos;
    }

    public void setEnvInfos(List<TestEnvInfo> envInfos) {
        this.envInfos = envInfos;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<ReCaseSet> getCaseSetsInfo() {
        return caseSetsInfo;
    }

    public void setCaseSetsInfo(List<ReCaseSet> caseSetsInfo) {
        this.caseSetsInfo = caseSetsInfo;
    }

    public String getEnvArea() {
        return envArea;
    }

    public void setEnvArea(String envArea) {
        this.envArea = envArea;
    }

    @Override
    public Map<String, Object> toMap(){
        Map<String, Object> valueMap = super.toMap();
        if(pluginLists != null){
            valueMap.put(KEY_pluginLists, pluginLists);
        }
        if(caseSets != null){
            valueMap.put(KEY_caseSets, caseSets);
        }
        if(caseSetsInfo != null){
            valueMap.put(KEY_caseSetsInfo, caseSetsInfo);
        }
        if (envInfos != null) {
            valueMap.put(KEY_envInfos, envInfos);
        }
        return valueMap;
    }
}
