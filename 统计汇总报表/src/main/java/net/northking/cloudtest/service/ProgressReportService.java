package net.northking.cloudtest.service;

import net.northking.cloudtest.dto.report.ProgressReportDTO;
import net.northking.cloudtest.query.report.ProgressReportQuery;

/**
 * @Title:
 * @Description: 需求分析进度报告实现类
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-04 18:07
 * @UpdateUser:
 * @Version:0.1
 */


public interface ProgressReportService {


    //需求分析阶段进度报告（每日）
    ProgressReportDTO analyseProgressReportByDay(ProgressReportQuery progressReportQuery) throws Exception;

    //需求分析阶段进度报告（饼图）
    ProgressReportDTO analyseProgressReportByPie(ProgressReportQuery progressReportQuery) throws Exception;

    //用例设计阶段进度报告（每日）
    ProgressReportDTO testCaseProgressReportByDay(ProgressReportQuery progressReportQuery) throws Exception;

    //用例设计阶段进度报告（饼图）
    ProgressReportDTO testCaseProgressReportByPie(ProgressReportQuery progressReportQuery) throws Exception;

    //用例执行阶段进度报告(饼图)
    ProgressReportDTO executeProgressReportByPie(ProgressReportQuery progressReportQuery) throws Exception;

    //用例执行阶段进度报告（单轮）
    ProgressReportDTO executeSingleRoundReport(ProgressReportQuery progressReportQuery) throws Exception;

    //用例执行阶段进度报告（总体）
    ProgressReportDTO executeTotalReport(ProgressReportQuery progressReportQuery) throws Exception;

    //项目的总体报告
    ProgressReportDTO projectProgressReportByTotal(ProgressReportQuery progressReportQuery) throws Exception;

    //获取项目总体进度
    Float getProjectProgress(ProgressReportQuery progressReportQuery) throws Exception;


}
