package net.northking.atp.entity;

import net.northking.atp.db.persistent.RePlanExecInfo;
import net.northking.atp.db.persistent.RuEngineJob;

public class ExecTaskEntity {

    private String planExecId;  // 执行记录id

    private String caseId;      // 用例id

    private String caseSetId;   // 案例集id

    private String engineJobId; // 执行任务id

    private String ruProjectId; // 运行项目id

    private String runTestEnvId;    // 用例运行环境id

    private RePlanExecInfo planExecInfo;

    private RuEngineJob engineJob;

    public String getPlanExecId() {
        return planExecId;
    }

    public void setPlanExecId(String planExecId) {
        this.planExecId = planExecId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getCaseSetId() {
        return caseSetId;
    }

    public void setCaseSetId(String caseSetId) {
        this.caseSetId = caseSetId;
    }

    public String getEngineJobId() {
        return engineJobId;
    }

    public void setEngineJobId(String engineJobId) {
        this.engineJobId = engineJobId;
    }

    public String getRuProjectId() {
        return ruProjectId;
    }

    public void setRuProjectId(String ruProjectId) {
        this.ruProjectId = ruProjectId;
    }

    public String getRunTestEnvId() {
        return runTestEnvId;
    }

    public void setRunTestEnvId(String runTestEnvId) {
        this.runTestEnvId = runTestEnvId;
    }

    public RePlanExecInfo getPlanExecInfo() {
        return planExecInfo;
    }

    public void setPlanExecInfo(RePlanExecInfo planExecInfo) {
        this.planExecInfo = planExecInfo;
    }

    public RuEngineJob getEngineJob() {
        return engineJob;
    }

    public void setEngineJob(RuEngineJob engineJob) {
        this.engineJob = engineJob;
    }

    @Override
    public String toString() {
        return "ExecTaskEntity{" +
                "planExecId='" + planExecId + '\'' +
                ", caseId='" + caseId + '\'' +
                ", caseSetId='" + caseSetId + '\'' +
                ", engineJobId='" + engineJobId + '\'' +
                ", ruProjectId='" + ruProjectId + '\'' +
                ", runTestEnvId='" + runTestEnvId + '\'' +
                ", planExecInfo=" + planExecInfo +
                ", engineJob=" + engineJob +
                '}';
    }
}
