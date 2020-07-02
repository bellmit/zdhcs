package net.northking.cloudtest.service;

/**
 * Created by liujinghao on 2018/5/25.
 */
public interface GenExecuteChartWeekService {
    /**
     * 生成执行完成情况图表
     */
    int GenExecuteWeekChart(String proId)throws Exception;
}
