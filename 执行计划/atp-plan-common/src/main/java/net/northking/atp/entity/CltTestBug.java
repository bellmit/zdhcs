package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 缺陷实体类
 *
 */
@ApiModel(
        description = "缺陷实体类"
)
public class CltTestBug {

    @ApiModelProperty("主键")
    private String bugId;
    @ApiModelProperty("缺陷标题 -> 断言失败信息")
    private String bugTitle;
    @ApiModelProperty("缺陷标题名称 -> 计划名称-断言失败信息")
    private String bugTitleName;
    @ApiModelProperty("缺陷等级，默认C")
    private String bugGrade;
    @ApiModelProperty("缺陷类型，默认3")
    private String bugType;
    @ApiModelProperty("缺陷紧急程度，默认2")
    private String emergency;
    @ApiModelProperty("是否重现，默认1")
    private String isReappear;
    @ApiModelProperty("用例所属模块")
    private String module;
    @ApiModelProperty("执行计划id")
    private String roundId;
    @ApiModelProperty("执行计划id")
    private String batchId;
    @ApiModelProperty("用例id")
    private String caseId;
    @ApiModelProperty("步骤id")
    private String stepId;
    @ApiModelProperty("缺陷接收人")
    private String receiver;
    @ApiModelProperty("缺陷创建类型，默认A")
    private String createType;
    @ApiModelProperty("步骤信息，拼接步骤")
    private String operStep;
    @ApiModelProperty("断言期望值")
    private String perResult;
    @ApiModelProperty("断言实际值")
    private String actualResult;
    @ApiModelProperty("执行记录日志")
    private String message;
    @ApiModelProperty("令牌")
    private String access_token;
    @ApiModelProperty("缺陷创建人，默认autoTest")
    private String createUser;
    @ApiModelProperty("项目id")
    private String proId;
    @ApiModelProperty("环境信息")
    private String envMessage;

    public String getBugId() {
        return bugId;
    }

    public void setBugId(String bugId) {
        this.bugId = bugId;
    }

    public String getBugTitle() {
        return bugTitle;
    }

    public void setBugTitle(String bugTitle) {
        this.bugTitle = bugTitle;
    }

    public String getBugTitleName() {
        return bugTitleName;
    }

    public void setBugTitleName(String bugTitleName) {
        this.bugTitleName = bugTitleName;
    }

    public String getBugGrade() {
        return bugGrade;
    }

    public void setBugGrade(String bugGrade) {
        this.bugGrade = bugGrade;
    }

    public String getBugType() {
        return bugType;
    }

    public void setBugType(String bugType) {
        this.bugType = bugType;
    }

    public String getEmergency() {
        return emergency;
    }

    public void setEmergency(String emergency) {
        this.emergency = emergency;
    }

    public String getIsReappear() {
        return isReappear;
    }

    public void setIsReappear(String isReappear) {
        this.isReappear = isReappear;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getRoundId() {
        return roundId;
    }

    public void setRoundId(String roundId) {
        this.roundId = roundId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getCreateType() {
        return createType;
    }

    public void setCreateType(String createType) {
        this.createType = createType;
    }

    public String getOperStep() {
        return operStep;
    }

    public void setOperStep(String operStep) {
        this.operStep = operStep;
    }

    public String getPerResult() {
        return perResult;
    }

    public void setPerResult(String perResult) {
        this.perResult = perResult;
    }

    public String getActualResult() {
        return actualResult;
    }

    public void setActualResult(String actualResult) {
        this.actualResult = actualResult;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getProId() {
        return proId;
    }

    public void setProId(String proId) {
        this.proId = proId;
    }

    public String getEnvMessage() {
        return envMessage;
    }

    public void setEnvMessage(String envMessage) {
        this.envMessage = envMessage;
    }

    @Override
    public String toString() {
        return "CltTestBug{" +
                "bugId='" + bugId + '\'' +
                ", bugTitle='" + bugTitle + '\'' +
                ", bugTitleName='" + bugTitleName + '\'' +
                ", bugGrade='" + bugGrade + '\'' +
                ", bugType='" + bugType + '\'' +
                ", emergency='" + emergency + '\'' +
                ", isReappear='" + isReappear + '\'' +
                ", module='" + module + '\'' +
                ", roundId='" + roundId + '\'' +
                ", batchId='" + batchId + '\'' +
                ", caseId='" + caseId + '\'' +
                ", stepId='" + stepId + '\'' +
                ", receiver='" + receiver + '\'' +
                ", createType='" + createType + '\'' +
                ", operStep='" + operStep + '\'' +
                ", perResult='" + perResult + '\'' +
                ", actualResult='" + actualResult + '\'' +
                ", message='" + message + '\'' +
                ", access_token='" + access_token + '\'' +
                ", createUser='" + createUser + '\'' +
                ", proId='" + proId + '\'' +
                ", envMessage='" + envMessage + '\'' +
                '}';
    }
}
