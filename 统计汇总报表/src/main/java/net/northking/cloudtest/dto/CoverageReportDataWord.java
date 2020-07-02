package net.northking.cloudtest.dto;

/**
 * @Title: 覆盖率对象
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/11
 * @UpdateUser:
 * @Version:0.1
 */
public class CoverageReportDataWord {
    /**
     * 交易名称
     */
    private String tradeName;

    /**
     * 需求数
     */
    private int demandNum;

    /**
     * 用例数
     */
    private int testCaseNum;

    /**
     * 覆盖率
     */
    private String coveragePersent;

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public int getDemandNum() {
        return demandNum;
    }

    public void setDemandNum(int demandNum) {
        this.demandNum = demandNum;
    }

    public int getTestCaseNum() {
        return testCaseNum;
    }

    public void setTestCaseNum(int testCaseNum) {
        this.testCaseNum = testCaseNum;
    }

    public String getCoveragePersent() {
        return coveragePersent;
    }

    public void setCoveragePersent(String coveragePersent) {
        this.coveragePersent = coveragePersent;
    }
}
