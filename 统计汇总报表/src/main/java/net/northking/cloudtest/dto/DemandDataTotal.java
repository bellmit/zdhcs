package net.northking.cloudtest.dto;

/**
 * Created by liujinghao on 2018/5/23.
 */
public class DemandDataTotal {
    /**
     * 未生成需求分析数
     */
    private Integer analysingTotal;

    /**
     * 已生成
     */
    private Integer approveTotal;

    /**
     * 待修改
     */
    private Integer forUpdateTotal;

    /**
     * 评审通过数
     */
    private Integer doneTotal;

    public Integer getAnalysingTotal() {
        return analysingTotal;
    }

    public void setAnalysingTotal(Integer analysingTotal) {
        this.analysingTotal = analysingTotal;
    }

    public Integer getApproveTotal() {
        return approveTotal;
    }

    public void setApproveTotal(Integer approveTotal) {
        this.approveTotal = approveTotal;
    }

    public Integer getForUpdateTotal() {
        return forUpdateTotal;
    }

    public void setForUpdateTotal(Integer forUpdateTotal) {
        this.forUpdateTotal = forUpdateTotal;
    }

    public Integer getDoneTotal() {
        return doneTotal;
    }

    public void setDoneTotal(Integer doneTotal) {
        this.doneTotal = doneTotal;
    }
}
