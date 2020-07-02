package net.northking.cloudtest.dto;

/**
 * @Title: 缺陷总计
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/12
 * @UpdateUser:
 * @Version:0.1
 */
public class PorjectDatasTotal {
    /**
     * 案例数总计
     */
    private int caseNumTotal;

    /**
     * 交易数总计（目前等于案例数)
     */
    private int transNumTotal;

    /**
     * 模块交易数总计
     */
    private int moduleTransNumTotal;

    /**
     * 测试覆盖率总计
     */
    private String casePersentTotal;

    /**
     * 执行案例数总计
     */
    private int caseExecuteNumTotal;

    /**
     * 通过案例数总计
     */
    private int caseExecutePassNumTotal;

    /**
     * 通过交易数总计
     */
    private int transPassNumTotal;

    /**
     * 案例通过率总计
     */
    private String caseExecutePassPersentTotal;

    /**
     * 交易通过率总计
     */
    private String transPassPersentTotal;

    /**
     * 有效缺陷数总计
     */
    private int effectiveNumTotal;

    /**
     * 总缺陷数总计
     */
    private int totalNumTotal;

    /**
     * 缺陷有效率总计
     */
    private String effectivePersentTotal;

    /**
     * 关闭缺陷数总计
     */
    private int closeNumTotal;

    /**
     * 缺陷修复率总计
     */
    private String repairPersentTotal;

    /**
     * 有效案例数总计
     */
    private int effectiveCaseNumTotal;

    /**
     * 缺陷密度总计
     */
    private String testBugCasePersentTotal;

    /**
     * A类
     */
    private int ATotal;
    private int BTotal;
    private int CTotal;
    private int DTotal;
    private int ETotal;
    /**
     * 总数总计
     */
    private int totalTotal;

    public int getEffectiveNumTotal() {
        return effectiveNumTotal;
    }

    public void setEffectiveNumTotal(int effectiveNumTotal) {
        this.effectiveNumTotal = effectiveNumTotal;
    }

    public int getTotalNumTotal() {
        return totalNumTotal;
    }

    public void setTotalNumTotal(int totalNumTotal) {
        this.totalNumTotal = totalNumTotal;
    }

    public String getEffectivePersentTotal() {
        return effectivePersentTotal;
    }

    public void setEffectivePersentTotal(String effectivePersentTotal) {
        this.effectivePersentTotal = effectivePersentTotal;
    }

    public int getCloseNumTotal() {
        return closeNumTotal;
    }

    public void setCloseNumTotal(int closeNumTotal) {
        this.closeNumTotal = closeNumTotal;
    }

    public String getRepairPersentTotal() {
        return repairPersentTotal;
    }

    public void setRepairPersentTotal(String repairPersentTotal) {
        this.repairPersentTotal = repairPersentTotal;
    }

    public int getEffectiveCaseNumTotal() {
        return effectiveCaseNumTotal;
    }

    public void setEffectiveCaseNumTotal(int effectiveCaseNumTotal) {
        this.effectiveCaseNumTotal = effectiveCaseNumTotal;
    }

    public String getTestBugCasePersentTotal() {
        return testBugCasePersentTotal;
    }

    public void setTestBugCasePersentTotal(String testBugCasePersentTotal) {
        this.testBugCasePersentTotal = testBugCasePersentTotal;
    }

    public int getATotal() {
        return ATotal;
    }

    public void setATotal(int ATotal) {
        this.ATotal = ATotal;
    }

    public int getBTotal() {
        return BTotal;
    }

    public void setBTotal(int BTotal) {
        this.BTotal = BTotal;
    }

    public int getCTotal() {
        return CTotal;
    }

    public void setCTotal(int CTotal) {
        this.CTotal = CTotal;
    }

    public int getDTotal() {
        return DTotal;
    }

    public void setDTotal(int DTotal) {
        this.DTotal = DTotal;
    }

    public int getETotal() {
        return ETotal;
    }

    public void setETotal(int ETotal) {
        this.ETotal = ETotal;
    }

    public int getTotalTotal() {
        return totalTotal;
    }

    public void setTotalTotal(int totalTotal) {
        this.totalTotal = totalTotal;
    }

    public int getCaseNumTotal() {
        return caseNumTotal;
    }

    public void setCaseNumTotal(int caseNumTotal) {
        this.caseNumTotal = caseNumTotal;
    }

    public String getCasePersentTotal() {
        return casePersentTotal;
    }

    public void setCasePersentTotal(String casePersentTotal) {
        this.casePersentTotal = casePersentTotal;
    }

    public int getCaseExecuteNumTotal() {
        return caseExecuteNumTotal;
    }

    public void setCaseExecuteNumTotal(int caseExecuteNumTotal) {
        this.caseExecuteNumTotal = caseExecuteNumTotal;
    }

    public int getCaseExecutePassNumTotal() {
        return caseExecutePassNumTotal;
    }

    public void setCaseExecutePassNumTotal(int caseExecutePassNumTotal) {
        this.caseExecutePassNumTotal = caseExecutePassNumTotal;
    }

    public String getCaseExecutePassPersentTotal() {
        return caseExecutePassPersentTotal;
    }

    public void setCaseExecutePassPersentTotal(String caseExecutePassPersentTotal) {
        this.caseExecutePassPersentTotal = caseExecutePassPersentTotal;
    }

    public int getTransNumTotal() {
        return transNumTotal;
    }

    public void setTransNumTotal(int transNumTotal) {
        this.transNumTotal = transNumTotal;
    }

    public int getTransPassNumTotal() {
        return transPassNumTotal;
    }

    public void setTransPassNumTotal(int transPassNumTotal) {
        this.transPassNumTotal = transPassNumTotal;
    }

    public String getTransPassPersentTotal() {
        return transPassPersentTotal;
    }

    public void setTransPassPersentTotal(String transPassPersentTotal) {
        this.transPassPersentTotal = transPassPersentTotal;
    }

    public int getModuleTransNumTotal() {
        return moduleTransNumTotal;
    }

    public void setModuleTransNumTotal(int moduleTransNumTotal) {
        this.moduleTransNumTotal = moduleTransNumTotal;
    }

    public void init(){
        this.setEffectiveNumTotal(0);
        this.setTotalNumTotal(0);
        this.setEffectivePersentTotal("");
        this.setCloseNumTotal(0);
        this.setRepairPersentTotal("");
        this.setEffectiveCaseNumTotal(0);
        this.setTestBugCasePersentTotal("");
        this.setATotal(0);
        this.setBTotal(0);
        this.setCTotal(0);
        this.setDTotal(0);
        this.setETotal(0);
        this.setTotalTotal(0);
        this.setCaseNumTotal(0);
        this.setCasePersentTotal("");
        this.setCaseExecuteNumTotal(0);
        this.setCaseExecutePassNumTotal(0);
        this.setCaseExecutePassPersentTotal("");
        this.setTransNumTotal(0);
        this.setTransNumTotal(0);
        this.setTransPassNumTotal(0);
        this.setTransPassPersentTotal("");
        this.setModuleTransNumTotal(0);

    }


}
