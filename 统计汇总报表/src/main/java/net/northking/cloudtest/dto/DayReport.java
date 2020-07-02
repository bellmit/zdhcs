package net.northking.cloudtest.dto;

import net.northking.cloudtest.domain.analyse.CltRound;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.dto.analyse.CltRoundDTO;
import net.northking.cloudtest.dto.analyse.MapNodeNumDTO;
import net.northking.cloudtest.dto.report.DemandMapNodeDTO;
import net.northking.cloudtest.dto.report.QualityReportDTO;
import net.northking.cloudtest.dto.report.TestCaseExecuteReportDTO;
import net.northking.cloudtest.dto.report.TestCaseReportDTO;
import net.northking.cloudtest.dto.testBug.TestBugDTO;

import java.util.Date;
import java.util.List;

/**
 * Created by liujinghao on 2018/5/20.
 * 每日报告
 */
public class DayReport {
    /**
     * 客户名称
     */
    private String custName;
    /**
     * 项目名称
     */
    private CltProject project;

    /**
     * 版本
     */
    private String version;

    /**
     * 报告日期
     */
    private Date reportDate;

    /**
     * 存在的风险
     */
    private List<CltBugLog> cltBugLogList;

    //判断项目在哪个阶段
    private JudgeProjectStatus judgeProjectStatus;

    public JudgeProjectStatus getJudgeProjectStatus() {
        return judgeProjectStatus;
    }

    public void setJudgeProjectStatus(JudgeProjectStatus judgeProjectStatus) {
        this.judgeProjectStatus = judgeProjectStatus;
    }

    /**
     * 轮次信息
     */
    private  List<CltRound>cltRound;

    /**
     * 测试需求分析完成情况(模块)
     */
   private List<DemandMapNodeDTO> demandByModuleQuality;

    /**
     * 当日测试需求分析完成情况(人员)
     */
    private List<DemandMapNodeDTO> demandByUserQuality;
    /**
     * 测试用例设计完成情况(模块)
     */
    private List<TestCaseReportDTO> testCaseByModuleQuality;

    /**

     * 当日缺陷发现情况
     */
    private  List<TestBugDTO> testBugQuality;
    /**
     * 缺陷总体情况
     */
    private  List<TestBugDTO> testBugAllQuality;

    /**
     * 当日测试用例设计完成情况(人员)
     */
    private List<TestCaseReportDTO> testCaseByUserQuality;

    /**
     * 当前轮次用例完成执行完成情况
     */
    private List<TestCaseExecuteReportDTO> testCaseExecuteReport;

    /**
     * 当天用例完成执行完成情况
     */
    private List<TestCaseExecuteReportDTO> testCaseExecuteReportByDay;

    /**
     * 用例完成执行完成情况
     */
    private List<TestCaseExecuteReportDTO> testCaseExecuteReportByProject;

    public List<TestCaseExecuteReportDTO> getTestCaseExecuteReportByProject() {
        return testCaseExecuteReportByProject;
    }

    public void setTestCaseExecuteReportByProject(List<TestCaseExecuteReportDTO> testCaseExecuteReportByProject) {
        this.testCaseExecuteReportByProject = testCaseExecuteReportByProject;
    }

    public List<TestCaseExecuteReportDTO> getTestCaseExecuteReportByDay() {
        return testCaseExecuteReportByDay;
    }

    public void setTestCaseExecuteReportByDay(List<TestCaseExecuteReportDTO> testCaseExecuteReportByDay) {
        this.testCaseExecuteReportByDay = testCaseExecuteReportByDay;
    }

    public List<TestCaseExecuteReportDTO> getTestCaseExecuteReport() {
        return testCaseExecuteReport;
    }

    public void setTestCaseExecuteReport(List<TestCaseExecuteReportDTO> testCaseExecuteReport) {
        this.testCaseExecuteReport = testCaseExecuteReport;
    }

    public List<CltBugLog> getCltBugLogList() {
        return cltBugLogList;
    }

    public void setCltBugLogList(List<CltBugLog> cltBugLogList) {
        this.cltBugLogList = cltBugLogList;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public CltProject getProject() {
        return project;
    }

    public void setProject(CltProject project) {
        this.project = project;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public List<DemandMapNodeDTO> getDemandByModuleQuality() {
        return demandByModuleQuality;
    }

    public void setDemandByModuleQuality(List<DemandMapNodeDTO> demandByModuleQuality) {
        this.demandByModuleQuality = demandByModuleQuality;
    }

    public List<DemandMapNodeDTO> getDemandByUserQuality() {
        return demandByUserQuality;
    }

    public void setDemandByUserQuality(List<DemandMapNodeDTO> demandByUserQuality) {
        this.demandByUserQuality = demandByUserQuality;
    }

    public List<TestCaseReportDTO> getTestCaseByModuleQuality() {
        return testCaseByModuleQuality;
    }

    public void setTestCaseByModuleQuality(List<TestCaseReportDTO> testCaseByModuleQuality) {
        this.testCaseByModuleQuality = testCaseByModuleQuality;
    }

    public List<TestCaseReportDTO> getTestCaseByUserQuality() {
        return testCaseByUserQuality;
    }

    public void setTestCaseByUserQuality(List<TestCaseReportDTO> testCaseByUserQuality) {
        this.testCaseByUserQuality = testCaseByUserQuality;
    }

    public List<TestBugDTO> getTestBugQuality() {
        return testBugQuality;
    }

    public void setTestBugQuality(List<TestBugDTO> testBugQuality) {
        this.testBugQuality = testBugQuality;
    }

    public List<TestBugDTO> getTestBugAllQuality() {
        return testBugAllQuality;
    }

    public void setTestBugAllQuality(List<TestBugDTO> testBugAllQuality) {
        this.testBugAllQuality = testBugAllQuality;
    }

    public List<CltRound> getCltRound() {
        return cltRound;
    }

    public void setCltRound(List<CltRound> cltRound) {
        this.cltRound = cltRound;
    }
}
