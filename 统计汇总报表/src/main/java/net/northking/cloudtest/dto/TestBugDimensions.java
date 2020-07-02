package net.northking.cloudtest.dto;

/**
 * @Title: 缺陷严重程度
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/12
 * @UpdateUser:
 * @Version:0.1
 */
public class TestBugDimensions {

    /**
     * 交易名称
     */
    private String transName;
    private int alValue;
    private int blValue;
    private int clValue;
    private int dlValue;
    private int elValue;
    private int total;

    public int getAlValue() {
        return alValue;
    }

    public void setAlValue(int alValue) {
        this.alValue = alValue;
    }

    public int getBlValue() {
        return blValue;
    }

    public void setBlValue(int blValue) {
        this.blValue = blValue;
    }

    public int getClValue() {
        return clValue;
    }

    public void setClValue(int clValue) {
        this.clValue = clValue;
    }

    public int getDlValue() {
        return dlValue;
    }

    public void setDlValue(int dlValue) {
        this.dlValue = dlValue;
    }

    public int getElValue() {
        return elValue;
    }

    public void setElValue(int elValue) {
        this.elValue = elValue;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }
}
