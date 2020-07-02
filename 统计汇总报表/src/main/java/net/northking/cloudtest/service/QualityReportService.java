package net.northking.cloudtest.service;

import net.northking.cloudtest.dto.report.QualityReportDTO;
import net.northking.cloudtest.query.report.QualityReportQuery;

/**
 * @Title:
 * @Description: 质量报告逻辑层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-09 9:14
 * @UpdateUser:
 * @Version:0.1
 */
public interface QualityReportService {

    //查询测试用例设计质量报告
    QualityReportDTO queryTestCaseQualityReport(QualityReportQuery query) throws Exception;

    //查询需求分析质量报告
    QualityReportDTO queryDemandQualityReport(QualityReportQuery query) throws Exception;


    //用例执行完成情况
    QualityReportDTO  queryTestExecuteQualityReport(QualityReportQuery query) throws Exception;



}
