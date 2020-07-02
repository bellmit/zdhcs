package net.northking.cloudtest.service;

/**
 * Created by liujinghao on 2018/5/25.
 */
public interface GenTestBugChartWeekService {

    //缺陷趋势图周报
    int genTestBugChart(String proId)throws  Exception;
}
