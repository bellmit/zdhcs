package net.northking.cloudtest.service;

import java.util.Map;

/**
 * Created by liujinghao on 2018/5/25.
 */
public interface GenCompleteChartWeekService {

    /**
     *生成周报告完成情况图表
     */
    int genCompleteChartWeek(String proId)throws Exception;
}
