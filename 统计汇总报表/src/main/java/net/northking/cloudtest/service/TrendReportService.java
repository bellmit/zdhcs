package net.northking.cloudtest.service;

import net.northking.cloudtest.dto.report.GanttReportDto;
import net.northking.cloudtest.dto.report.TrendReportDTO;
import net.northking.cloudtest.query.report.TrendReportQuery;

/**
 * @Title:
 * @Description: 趋势报告逻辑层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-11 16:53
 * @UpdateUser:
 * @Version:0.1
 */


public interface TrendReportService {

    //缺陷趋势报告
    TrendReportDTO testBugTrendReport(TrendReportQuery trendReportQuery) throws Exception;

    //测试执行趋势报告
    TrendReportDTO testExecuteTrendReport(TrendReportQuery trendReportQuery) throws Exception;

    /**
     * 项目整体缺陷趋势报告
     *
     * @param trendReportQuery
     * @return
     * @throws Exception
     */
    TrendReportDTO testWholeBugTrendReport(TrendReportQuery trendReportQuery) throws Exception;

    GanttReportDto getGanttReport(TrendReportQuery trendReportQuery) throws Exception;
}
