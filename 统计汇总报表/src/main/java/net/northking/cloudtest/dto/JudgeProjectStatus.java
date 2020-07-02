package net.northking.cloudtest.dto;

/**
 * Created by liujinghao on 2018/6/5.
 */
public class JudgeProjectStatus {
    Integer demandStatus;
    Integer testCaseStatus;
    Integer executeStatus;

    public Integer getDemandStatus() {
        return demandStatus;
    }

    public void setDemandStatus(Integer demandStatus) {
        this.demandStatus = demandStatus;
    }

    public Integer getTestCaseStatus() {
        return testCaseStatus;
    }

    public void setTestCaseStatus(Integer testCaseStatus) {
        this.testCaseStatus = testCaseStatus;
    }

    public Integer getExecuteStatus() {
        return executeStatus;
    }

    public void setExecuteStatus(Integer executeStatus) {
        this.executeStatus = executeStatus;
    }

    @Override
    public String toString() {
        return "JudgeProjectStatus{" +
                "demandStatus=" + demandStatus +
                ", testCaseStatus=" + testCaseStatus +
                ", executeStatus=" + executeStatus +
                '}';
    }
}
