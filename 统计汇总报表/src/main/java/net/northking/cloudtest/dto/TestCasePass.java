package net.northking.cloudtest.dto;

/**
 * @Title: 案例通过率
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/11
 * @UpdateUser:
 * @Version:0.1
 */
public class TestCasePass {
    /**
     * 交易名称
     */
    private String tradeName;

    /**
     * 用例数
     */
    private int testCaseNum;

    /**
     * 用例通过数
     */
    private int testCasePassNum;

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public int getTestCaseNum() {
        return testCaseNum;
    }

    public void setTestCaseNum(int testCaseNum) {
        this.testCaseNum = testCaseNum;
    }

    public int getTestCasePassNum() {
        return testCasePassNum;
    }

    public void setTestCasePassNum(int testCasePassNum) {
        this.testCasePassNum = testCasePassNum;
    }
}
