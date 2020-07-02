package net.northking.cloudtest.dto;

/**
 * Created by liujinghao on 2018/5/23.
 */
public class TestCaseCountTotal {
    /**
     * 未设计
     */
    private Integer siTotal;

    /**
     * 设计中
     */
    private Integer sdTotal;

    /**
     * 已完成
     */
    private Integer ssTotal;

    public Integer getSiTotal() {
        return siTotal;
    }

    public void setSiTotal(Integer siTotal) {
        this.siTotal = siTotal;
    }

    public Integer getSdTotal() {
        return sdTotal;
    }

    public void setSdTotal(Integer sdTotal) {
        this.sdTotal = sdTotal;
    }

    public Integer getSsTotal() {
        return ssTotal;
    }

    public void setSsTotal(Integer ssTotal) {
        this.ssTotal = ssTotal;
    }

    @Override
    public String toString() {
        return "TestCaseCountTotal{" +
                "siTotal=" + siTotal +
                ", sdTotal=" + sdTotal +
                ", ssTotal=" + ssTotal +
                '}';
    }
}
