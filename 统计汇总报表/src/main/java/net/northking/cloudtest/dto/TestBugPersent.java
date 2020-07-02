package net.northking.cloudtest.dto;

import com.alibaba.fastjson.JSON;

import java.text.NumberFormat;
import java.util.Map;

/**
 * @Title: 缺陷有效性
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/12
 * @UpdateUser:
 * @Version:0.1
 */
public class TestBugPersent {
    /**
     * 模块名称
     */
    private String tradeName;

    /**
     * 有效缺陷数
     */
    private int effectiveNum;

    /**
     * 总缺陷数
     */
    private int totalNum;

    /**
     * 缺陷有效率
     */
    private String effectivePersent;

    /**
     * 关闭缺陷数
     */
    private int closeNum;

    /**
     * 缺陷修复率
     */
    private String repairPersent;

    /**
     * 有效案例数
     */
    private int effectiveCaseNum;

    /**
     * 缺陷密度
     */
    private String testBugCasePersent;

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public int getEffectiveNum() {
        return effectiveNum;
    }

    public void setEffectiveNum(int effectiveNum) {
        this.effectiveNum = effectiveNum;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public String getEffectivePersent() {
        return effectivePersent;
    }

    public void setEffectivePersent(String effectivePersent) {
        this.effectivePersent = effectivePersent;
    }

    public String getRepairPersent() {
        return repairPersent;
    }

    public void setRepairPersent(String repairPersent) {
        this.repairPersent = repairPersent;
    }

    public int getCloseNum() {
        return closeNum;
    }

    public void setCloseNum(int closeNum) {
        this.closeNum = closeNum;
    }

    public int getEffectiveCaseNum() {
        return effectiveCaseNum;
    }

    public void setEffectiveCaseNum(int effectiveCaseNum) {
        this.effectiveCaseNum = effectiveCaseNum;
    }

    public String getTestBugCasePersent() {
        return testBugCasePersent;
    }

    public void setTestBugCasePersent(String testBugCasePersent) {
        this.testBugCasePersent = testBugCasePersent;
    }

    public static void main(String args[]){
        String str = "{\"1\":\"7\",\"2\":\"0\",\"3\":\"6\",\"4\":\"0\",\"5\":\"0\",\"6\":\"0\",\"7\":\"0\",\"8\":\"0\",\"9\":\"0\",\"10\":\"1\",\"11\":\"0\",\"total\":\"14\"}";
        Map resultToMap = JSON.parseObject(str);
        System.out.println( Integer.parseInt((String)resultToMap.get("total")));
        System.out.println(Integer.parseInt((String) resultToMap.get("total")) - Integer.parseInt((String) resultToMap.get("10")));
        System.out.println(getPersent((Integer.parseInt((String) resultToMap.get("total")) - Integer.parseInt((String) resultToMap.get("10"))), Integer.parseInt((String) resultToMap.get("total"))));
        System.out.println(Integer.parseInt((String) resultToMap.get("9")));
        System.out.println(getPersent(Integer.parseInt((String) resultToMap.get("9")), Integer.parseInt((String) resultToMap.get("total")) - Integer.parseInt((String) resultToMap.get("10"))));
    }

    private static String getPersent(int num1, int num2){
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();

        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);

        String result = numberFormat.format((float) num1 / (float) num2 * 100);

        return result + "%";
    }
}
