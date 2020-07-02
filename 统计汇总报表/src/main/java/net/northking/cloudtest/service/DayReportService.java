package net.northking.cloudtest.service;

import java.util.Map;

/**
 * Created by liujinghao on 2018/5/21.
 */
public interface DayReportService {
    /**
     * 获取日报表数据
     * @param proId
     * @param dataMap
     * @throws Exception
     */
    void dealWithDayReport(String proId, Map dataMap) throws Exception;

    /**
     * 插入或更新报告数据
     * @param proId
     * @param operatorUserId
     * @param docPath
     * @param swfPath
     * @return
     */
    Integer insertOrUpdateDayReportData(String proId, String operatorUserId, String docPath, String swfPath) throws Exception;
}
