package net.northking.cloudtest.dto;

import net.northking.cloudtest.domain.analyse.CltRound;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.dto.report.DemandMapNodeDTO;
import net.northking.cloudtest.dto.report.TestCaseExecuteReportDTO;
import net.northking.cloudtest.dto.report.TestCaseReportDTO;
import net.northking.cloudtest.dto.testBug.TestBugDTO;

import java.util.Date;
import java.util.List;

/**
 * Created by liujinghao on 2018/5/24.
 */
public class WeekReportBulletin {
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
     * 轮次信息
     */
    private  List<CltRound>cltRound;

    /**
     * 轮次信息
     */
    private CltRound cltRoundDto;

    public CltRound getCltRoundDto() {
        return cltRoundDto;
    }

    public void setCltRoundDto(CltRound cltRoundDto) {
        this.cltRoundDto = cltRoundDto;
    }

    /**
     * 测试需求分析完成情况(模块)
     */
    private List<DemandMapNodeDTO> demandByModuleQualityByWeek;

    /**
     * 本周测试需求分析完成情况(人员)
     */
    private List<DemandMapNodeDTO> demandByUserQualityByWeek;
    /**
     * 测试用例设计完成情况(模块)
     */
    private List<TestCaseReportDTO> testCaseByModuleQualityByWeek;

    /**
     * 本周测试用例设计完成情况(人员)
     */
    private List<TestCaseReportDTO> testCaseByUserQuality;

    /**
     * 用例完成执行完成情况
     */
    private List<TestCaseExecuteReportDTO> testCaseExecuteReport;

    /**
     * 本周用例执行情况
     */
    private List<TestCaseExecuteReportDTO> testCaseExecuteReportByWeek;

    /**
     * 当前轮次用例执行情况
     */
    private List<TestCaseExecuteReportDTO> testCaseExecuteReportByRound;

    //判断项目在哪个阶段
    private JudgeProjectStatus judgeProjectStatus;

    public JudgeProjectStatus getJudgeProjectStatus() {
        return judgeProjectStatus;
    }

    public void setJudgeProjectStatus(JudgeProjectStatus judgeProjectStatus) {
        this.judgeProjectStatus = judgeProjectStatus;
    }

    public List<TestCaseExecuteReportDTO> getTestCaseExecuteReportByRound() {
        return testCaseExecuteReportByRound;
    }

    public void setTestCaseExecuteReportByRound(List<TestCaseExecuteReportDTO> testCaseExecuteReportByRound) {
        this.testCaseExecuteReportByRound = testCaseExecuteReportByRound;
    }

    public List<TestCaseExecuteReportDTO> getTestCaseExecuteReportByWeek() {
        return testCaseExecuteReportByWeek;
    }

    public void setTestCaseExecuteReportByWeek(List<TestCaseExecuteReportDTO> testCaseExecuteReportByWeek) {
        this.testCaseExecuteReportByWeek = testCaseExecuteReportByWeek;
    }

    public List<TestCaseExecuteReportDTO> getTestCaseExecuteReport() {
        return testCaseExecuteReport;
    }

    public void setTestCaseExecuteReport(List<TestCaseExecuteReportDTO> testCaseExecuteReport) {
        this.testCaseExecuteReport = testCaseExecuteReport;
    }

    /**
     * 存在的风险
     */
    private List<CltBugLog> cltBugLogList;
    /**

     * 本周缺陷发现情况
     */
    private List<TestBugDTO> testBugQualityByWeek;
    /**
     * 缺陷总体情况
     */
    private  List<TestBugDTO> testBugAllQualityByWeek;


    public List<TestCaseReportDTO> getTestCaseByUserQuality() {
        return testCaseByUserQuality;
    }

    public void setTestCaseByUserQuality(List<TestCaseReportDTO> testCaseByUserQuality) {
        this.testCaseByUserQuality = testCaseByUserQuality;
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

    public List<DemandMapNodeDTO> getDemandByModuleQualityByWeek() {
        return demandByModuleQualityByWeek;
    }

    public void setDemandByModuleQualityByWeek(List<DemandMapNodeDTO> demandByModuleQualityByWeek) {
        this.demandByModuleQualityByWeek = demandByModuleQualityByWeek;
    }

    public List<DemandMapNodeDTO> getDemandByUserQualityByWeek() {
        return demandByUserQualityByWeek;
    }

    public void setDemandByUserQualityByWeek(List<DemandMapNodeDTO> demandByUserQualityByWeek) {
        this.demandByUserQualityByWeek = demandByUserQualityByWeek;
    }

    public List<TestCaseReportDTO> getTestCaseByModuleQualityByWeek() {
        return testCaseByModuleQualityByWeek;
    }

    public void setTestCaseByModuleQualityByWeek(List<TestCaseReportDTO> testCaseByModuleQualityByWeek) {
        this.testCaseByModuleQualityByWeek = testCaseByModuleQualityByWeek;
    }

    public List<CltBugLog> getCltBugLogList() {
        return cltBugLogList;
    }

    public void setCltBugLogList(List<CltBugLog> cltBugLogList) {
        this.cltBugLogList = cltBugLogList;
    }

    public List<TestBugDTO> getTestBugQualityByWeek() {
        return testBugQualityByWeek;
    }

    public void setTestBugQualityByWeek(List<TestBugDTO> testBugQualityByWeek) {
        this.testBugQualityByWeek = testBugQualityByWeek;
    }

    public List<TestBugDTO> getTestBugAllQualityByWeek() {
        return testBugAllQualityByWeek;
    }

    public void setTestBugAllQualityByWeek(List<TestBugDTO> testBugAllQualityByWeek) {
        this.testBugAllQualityByWeek = testBugAllQualityByWeek;
    }

    public List<CltRound> getCltRound() {
        return cltRound;
    }

    public void setCltRound(List<CltRound> cltRound) {
        this.cltRound = cltRound;
    }
}
