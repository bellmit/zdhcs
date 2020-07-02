package net.northking.cloudtest.service;

import org.springframework.stereotype.Component;

import net.northking.cloudtest.query.report.TestBugQualityReportQuery;

/**
 * Created by liujinghao on 2018/5/15.
 */
public interface GenTestBugChartService {
    //缺陷趋势图
    int genTestBugChart(String proId)throws  Exception;
    //缺陷状态分布图
    int genTestBugMoudle(String proId) throws  Exception;
}
