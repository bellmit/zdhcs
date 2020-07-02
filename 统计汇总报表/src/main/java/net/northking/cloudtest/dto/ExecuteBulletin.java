package net.northking.cloudtest.dto;

import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.dto.analyse.CltRoundDTO;
import net.northking.cloudtest.dto.report.DemandMapCountDto;
import net.northking.cloudtest.dto.report.TestCasePass;
import net.northking.cloudtest.dto.report.TestTransPass;
import net.northking.cloudtest.query.analyse.DemandQuery;

import java.util.Date;
import java.util.List;

/**
 * @Author:zwy
 * @Despriction:
 * @Date:Create in 11:30 2018/5/24
 * @Modify By:
 */
public class ExecuteBulletin {
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
         * 需求树
         */
        private List<DemandQuery> demandQueryList;
    
        /**
         * 是否全量回归测试,1为全量，0不为全量
         */
        private Integer allRegressionTest;
    
        /**
         * 轮次信息，包括批次
         */
        private List<CltRoundDTO> roundDtoList;
    
        /**
         * 准出准则
         */
        private CltProjectCount cltProjectCount;
    
        /**
         * 测试环境
         */
        private String testEnv;
    
        /**
         * 覆盖率数据
         */
        private List<CoverageReportDataWord> coverageReportDataWordList;
    
        /**
         * 案例通过率
         */
        private List<TestCasePass> testCasePassList;
    
        /**
         * 缺陷有效率和修复率
         */
        private List<TestBugPersent> testBugPersentList;
    
        /**
         * 严重程度
         */
        private List<TestBugDimensions> testBugDimensionsList;
    
        /**
         * 存在的风险
         */
        private List<CltBugLog> cltBugLogList;
    
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

        private DemandMapCountDto demandMapCountDto;
    
        //轮次信息
        private CltRoundDTO cltRoundDTO;

    /**
     * 交易通过率
     */
    private List<TestTransPass> testTransPassList;


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

    public CltRoundDTO getCltRoundDTO() {
        return cltRoundDTO;
    }

    public void setCltRoundDTO(CltRoundDTO cltRoundDTO) {
        this.cltRoundDTO = cltRoundDTO;
    }

    public DemandMapCountDto getDemandMapCountDto() {
        return demandMapCountDto;
    }

    public void setDemandMapCountDto(DemandMapCountDto demandMapCountDto) {
        this.demandMapCountDto = demandMapCountDto;
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

    public List<DemandQuery> getDemandQueryList() {
        return demandQueryList;
    }

    public void setDemandQueryList(List<DemandQuery> demandQueryList) {
        this.demandQueryList = demandQueryList;
    }

    public Integer getAllRegressionTest() {
        return allRegressionTest;
    }

    public void setAllRegressionTest(Integer allRegressionTest) {
        this.allRegressionTest = allRegressionTest;
    }

    public List<CltRoundDTO> getRoundDtoList() {
        return roundDtoList;
    }

    public void setRoundDtoList(List<CltRoundDTO> roundDtoList) {
        this.roundDtoList = roundDtoList;
    }

    public CltProjectCount getCltProjectCount() {
        return cltProjectCount;
    }

    public void setCltProjectCount(CltProjectCount cltProjectCount) {
        this.cltProjectCount = cltProjectCount;
    }

    public String getTestEnv() {
        return testEnv;
    }

    public void setTestEnv(String testEnv) {
        this.testEnv = testEnv;
    }

    public List<CoverageReportDataWord> getCoverageReportDataWordList() {
        return coverageReportDataWordList;
    }

    public void setCoverageReportDataWordList(List<CoverageReportDataWord> coverageReportDataWordList) {
        this.coverageReportDataWordList = coverageReportDataWordList;
    }

    public List<TestCasePass> getTestCasePassList() {
        return testCasePassList;
    }

    public void setTestCasePassList(List<TestCasePass> testCasePassList) {
        this.testCasePassList = testCasePassList;
    }

    public List<TestBugPersent> getTestBugPersentList() {
        return testBugPersentList;
    }

    public void setTestBugPersentList(List<TestBugPersent> testBugPersentList) {
        this.testBugPersentList = testBugPersentList;
    }

    public List<TestBugDimensions> getTestBugDimensionsList() {
        return testBugDimensionsList;
    }

    public void setTestBugDimensionsList(List<TestBugDimensions> testBugDimensionsList) {
        this.testBugDimensionsList = testBugDimensionsList;
    }

    public List<CltBugLog> getCltBugLogList() {
        return cltBugLogList;
    }

    public void setCltBugLogList(List<CltBugLog> cltBugLogList) {
        this.cltBugLogList = cltBugLogList;
    }

    public List<TestTransPass> getTestTransPassList() {
        return testTransPassList;
    }

    public void setTestTransPassList(List<TestTransPass> testTransPassList) {
        this.testTransPassList = testTransPassList;
    }
}
