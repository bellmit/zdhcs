package net.northking.cloudtest.service;

import java.util.Map;

/**
 * Created by liujinghao on 2018/5/24.
 */
public interface WeekReportService {
    /**
     * 获取日报表数据
     * @param proId
     * @param dataMap
     * @throws Exception
     */
    void dealWithWeekReport(String proId, Map dataMap) throws Exception;

    /**
     * 插入或更新报告数据
     * @param proId
     * @param operatorUserId
     * @param docPath
     * @param swfPath
     * @return
     */
    Integer insertOrUpdateWeekReportData(String proId, String operatorUserId, String docPath, String swfPath) throws Exception;
}
