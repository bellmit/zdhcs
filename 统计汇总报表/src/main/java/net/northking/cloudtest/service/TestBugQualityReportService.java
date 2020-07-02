package net.northking.cloudtest.service;

import net.northking.cloudtest.common.Page;
import net.northking.cloudtest.dto.report.TestBugQualityReportDTO;
import net.northking.cloudtest.dto.testBug.TestBugDTO;
import net.northking.cloudtest.query.report.TestBugQualityReportQuery;
import net.northking.cloudtest.query.testBug.TestBugQuery;

/**
 * @Title:
 * @Description: 缺陷质量报告逻辑层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-09 9:54
 * @UpdateUser:
 * @Version:0.1
 */
public interface TestBugQualityReportService {

    //缺陷密度质量统计表(模块)
    TestBugQualityReportDTO testBugDensityReportByModule(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception;


    //缺陷密度质量统计表(人员)
    TestBugQualityReportDTO testBugDensityReportByUser(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception;

    //缺陷严重程度分布图(模块/人员)

    TestBugQualityReportDTO testBugGradeReportByModuleOrUser(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception;

    //缺陷状态分布图(模块/人员)
    TestBugQualityReportDTO testBugStatusReportByModuleOrUser(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception;

    //缺陷严重程度分布汇总分布图
    TestBugQualityReportDTO testBugGradeReportByTotal(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception;

    //缺陷状态分布汇总分布图
    TestBugQualityReportDTO testBugStatusReportByTotal(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception;

    //总体缺陷分布图
    TestBugQualityReportDTO allTestBugStatusReportBySonOrUser(TestBugQualityReportQuery query) throws Exception;

    /**
     * 根基查询高级缺陷列表
     *
     * @param query
     * @return
     */
    Page<TestBugDTO> selectHighTestBugByProIds(TestBugQuery query);

    /**
     * 查询该父项目下所有子项目的所有高级缺陷总和
     *
     * @param query
     * @return
     */
    Page<TestBugDTO> selectHighTestBugForParentProj(TestBugQuery query);

}
