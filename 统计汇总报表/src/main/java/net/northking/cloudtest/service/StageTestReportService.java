package net.northking.cloudtest.service;

import net.northking.cloudtest.common.Page;
import net.northking.cloudtest.domain.report.CltReport;
import net.northking.cloudtest.query.cermanager.CerManagerQuery;
import net.northking.cloudtest.query.report.CltReportQuery;

import java.util.List;


/**
 * Created by liujinghao on 2018/5/11.
 */
public interface StageTestReportService {
    //报告预览
    CltReport previewReport(CltReport cltReport) throws Exception;


    //重新生成报告
    CltReport reGenReport(CltReport cltReport) throws Exception;

    /**
     * 查看列表
     * @param query
     * @return
     * @throws Exception
     */
    Page<CltReport> previewTestReports(CltReportQuery query)throws Exception;

    /**
     * 查询报告信息
     * @param cltReport
     * @return
     * @throws Exception
     */
    CltReport queryReport(CltReport cltReport)throws Exception;

}
