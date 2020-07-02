package net.northking.cloudtest.service;

import org.springframework.stereotype.Component;

import net.northking.cloudtest.dto.report.CoverageReportDTO;
import net.northking.cloudtest.query.report.CoverageReportQuery;

/**
 * @Title:
 * @Description: 覆盖报告统计报表
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-08 15:15
 * @UpdateUser:
 * @Version:0.1
 */

@Component
public interface CoverageReportService {


    CoverageReportDTO testcaseDemandCoverageReport(CoverageReportQuery coverageReportQuery);



}
