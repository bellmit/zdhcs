package net.northking.cloudtest.dto;

import net.northking.cloudtest.domain.analyse.Demand;
import net.northking.cloudtest.domain.attach.CltAttach;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.dto.analyse.CltRoundDTO;
import net.northking.cloudtest.dto.report.TestCasePass;
import net.northking.cloudtest.dto.report.TestCaseReportDTO;
import net.northking.cloudtest.query.analyse.DemandQuery;

import java.util.Date;
import java.util.List;

/**
 * @Title: 设计阶段报告
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/11
 * @UpdateUser:
 * @Version:0.1
 */
public class DesignBulletin {
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
     * 分析模块数量
     */
    private int demandMudelNum;

    /**
     * 分析交易数量
     */
    private int demandTransNum;

    /**
     * 项目内的用例数
     */
    private int countCaseNum;

    //未设计用例数
    private int countCaseINum;

    //设计中用例数
    private int countCaseDNum;

    //已完成用例数
    private int countCaseSNum;

    //需求树
    private List<DemandQuery> treeList;

    //按模块统计测试用例状态
    private List<TestCaseReportDTO> testCaseReportDTOList;

    //测试准出准则
    private CltProjectCount cltProjectCount;

    /**
     * 存在的风险
     */
    private List<CltBugLog> cltBugLogList;

    //用例设计总计
    private TestCaseReportDTO testCaseDTOTotal;

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

    public int getDemandMudelNum() {
        return demandMudelNum;
    }

    public void setDemandMudelNum(int demandMudelNum) {
        this.demandMudelNum = demandMudelNum;
    }

    public int getDemandTransNum() {
        return demandTransNum;
    }

    public void setDemandTransNum(int demandTransNum) {
        this.demandTransNum = demandTransNum;
    }

    public List<CltBugLog> getCltBugLogList() {
        return cltBugLogList;
    }

    public void setCltBugLogList(List<CltBugLog> cltBugLogList) {
        this.cltBugLogList = cltBugLogList;
    }

    public int getCountCaseNum() {
        return countCaseNum;
    }

    public void setCountCaseNum(int countCaseNum) {
        this.countCaseNum = countCaseNum;
    }

    public int getCountCaseINum() {
        return countCaseINum;
    }

    public void setCountCaseINum(int countCaseINum) {
        this.countCaseINum = countCaseINum;
    }

    public int getCountCaseDNum() {
        return countCaseDNum;
    }

    public void setCountCaseDNum(int countCaseDNum) {
        this.countCaseDNum = countCaseDNum;
    }

    public int getCountCaseSNum() {
        return countCaseSNum;
    }

    public void setCountCaseSNum(int countCaseSNum) {
        this.countCaseSNum = countCaseSNum;
    }

    public List<DemandQuery> getTreeList() {
        return treeList;
    }

    public void setTreeList(List<DemandQuery> treeList) {
        this.treeList = treeList;
    }

    public List<TestCaseReportDTO> getTestCaseReportDTOList() {
        return testCaseReportDTOList;
    }

    public void setTestCaseReportDTOList(List<TestCaseReportDTO> testCaseReportDTOList) {
        this.testCaseReportDTOList = testCaseReportDTOList;
    }

    public CltProjectCount getCltProjectCount() {
        return cltProjectCount;
    }

    public void setCltProjectCount(CltProjectCount cltProjectCount) {
        this.cltProjectCount = cltProjectCount;
    }

    public TestCaseReportDTO getTestCaseDTOTotal() {
        return testCaseDTOTotal;
    }

    public void setTestCaseDTOTotal(TestCaseReportDTO testCaseDTOTotal) {
        this.testCaseDTOTotal = testCaseDTOTotal;
    }
}
