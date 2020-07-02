package net.northking.cloudtest.controller;

import io.swagger.annotations.ApiOperation;
import net.northking.cloudtest.common.Page;
import net.northking.cloudtest.constants.SuccessConstants;
import net.northking.cloudtest.dto.report.TestBugQualityReportDTO;
import net.northking.cloudtest.dto.testBug.TestBugDTO;
import net.northking.cloudtest.query.report.TestBugQualityReportQuery;
import net.northking.cloudtest.query.testBug.TestBugQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.TestBugQualityReportService;
import net.northking.cloudtest.utils.CltUtils;
import net.northking.cloudtest.utils.ParamVerifyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @Title:
 * @Description: 缺陷质量报告表现层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-09 15:29
 * @UpdateUser:
 * @Version:0.1
 */

@RestController
@RequestMapping(value = "/report/testBugQualityReport")
public class TestBugQualityReportController {


    //日志
    private final static Logger logger = LoggerFactory.getLogger(TestBugQualityReportController.class);

    @Autowired
    private TestBugQualityReportService testBugQualityReportService;


    //缺陷密度质量统计表(模块)
    @PostMapping("/testBugDensityReportByModule")
    public ResultInfo<TestBugQualityReportDTO> testBugDensityReportByModule(@RequestBody TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {

        logger.info("testBugDensityReportByModule start paramData" + testBugQualityReportQuery.toString());

        //参数校验
        init(testBugQualityReportQuery, "testBugDensityReportByModule");

        TestBugQualityReportDTO testBugQualityReportDTO = testBugQualityReportService.testBugDensityReportByModule(testBugQualityReportQuery);

        logger.info("testBugDensityReportByModule end paramData" + testBugQualityReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_QUALITY_REPORT_SUCCESS, testBugQualityReportDTO);


    }

    //缺陷密度质量统计表(用户)
    @PostMapping("/testBugDensityReportByUser")
    public ResultInfo<TestBugQualityReportDTO> testBugDensityReportByUser(@RequestBody TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {

        logger.info("testBugDensityReportByUser start paramData" + testBugQualityReportQuery.toString());

        //参数校验
        init(testBugQualityReportQuery, "testBugDensityReportByUser");

        TestBugQualityReportDTO testBugQualityReportDTO = testBugQualityReportService.testBugDensityReportByUser(testBugQualityReportQuery);

        logger.info("testBugDensityReportByUser end paramData" + testBugQualityReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_QUALITY_REPORT_SUCCESS, testBugQualityReportDTO);


    }


    //缺陷严重程度分布图(模块/人员)
    @PostMapping("/testBugGradeReportByModuleOrUser")
    public ResultInfo<TestBugQualityReportDTO> testBugGradeReportByModuleOrUser(@RequestBody TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {

        logger.info("testBugGradeReportByModuleOrUser start paramData" + testBugQualityReportQuery.toString());

        //参数校验
        init(testBugQualityReportQuery, "testBugGradeReportByModuleOrUser");

        if (testBugQualityReportQuery.getStartDate() == null || testBugQualityReportQuery.getEndDate() == null) {
            Date endDate = new Date();

            Date startDate = getDate(endDate);

            testBugQualityReportQuery.setStartDate(startDate);
            testBugQualityReportQuery.setEndDate(endDate);


        }

        //默认情况是按照人员
        if (StringUtils.isEmpty(testBugQualityReportQuery.getType())) {
            testBugQualityReportQuery.setType("U");

        }
        //默认查询前7天
        testBugQualityReportQuery.setTimeType("D");


        TestBugQualityReportDTO testBugQualityReportDTO = testBugQualityReportService.testBugGradeReportByModuleOrUser(testBugQualityReportQuery);

        logger.info("testBugDensityReportByModule end paramData" + testBugQualityReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_QUALITY_REPORT_SUCCESS, testBugQualityReportDTO);


    }

    //缺陷状态分布图(模块/人员)
    @ApiOperation("缺陷状态分布图(模块/人员)")
    @PostMapping("/testBugStatusReportByModuleOrUser")
    public ResultInfo<TestBugQualityReportDTO> testBugStatusReportByModuleOrUser(@RequestBody TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {

        logger.info("testBugStatusReportByModuleOrUser start paramData" + testBugQualityReportQuery.toString());

        //参数校验
        init(testBugQualityReportQuery, "testBugStatusReportByModuleOrUser");

        if (testBugQualityReportQuery.getStartDate() == null || testBugQualityReportQuery.getEndDate() == null) {
            Date endDate = new Date();

            Date startDate = getDate(endDate);

            testBugQualityReportQuery.setStartDate(startDate);
            testBugQualityReportQuery.setEndDate(endDate);

        }

        testBugQualityReportQuery.setTimeType("D");


        TestBugQualityReportDTO testBugQualityReportDTO = testBugQualityReportService.testBugStatusReportByModuleOrUser(testBugQualityReportQuery);

        logger.info("testBugStatusReportByModuleOrUser end paramData" + testBugQualityReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_QUALITY_REPORT_SUCCESS, testBugQualityReportDTO);


    }

    //缺陷严重程度汇总分布图
    @PostMapping("/testBugGradeReportByTotal")
    public ResultInfo<TestBugQualityReportDTO> testBugGradeReportByTotal(@RequestBody TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {

        logger.info("testBugGradeReportByTotal start paramData" + testBugQualityReportQuery.toString());

        //参数校验
        init(testBugQualityReportQuery, "testBugGradeReportByTotal");

        TestBugQualityReportDTO testBugQualityReportDTO = testBugQualityReportService.testBugGradeReportByTotal(testBugQualityReportQuery);

        logger.info("testBugGradeReportByTotal end paramData" + testBugQualityReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_QUALITY_REPORT_SUCCESS, testBugQualityReportDTO);


    }

    //缺陷状态分布汇总分布图
    @PostMapping("/testBugStatusReportByTotal")
    public ResultInfo<TestBugQualityReportDTO> testBugStatusReportByTotal(@RequestBody TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {

        logger.info("testBugStatusReportByTotal start paramData" + testBugQualityReportQuery.toString());

        //参数校验
        init(testBugQualityReportQuery, "testBugStatusReportByTotal");

        TestBugQualityReportDTO testBugQualityReportDTO = testBugQualityReportService.testBugStatusReportByTotal(testBugQualityReportQuery);

        logger.info("testBugStatusReportByTotal end paramData" + testBugQualityReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_QUALITY_REPORT_SUCCESS, testBugQualityReportDTO);


    }

    //缺陷状态分布汇总分布图
    @PostMapping("/allTestBugStatusReportBySonOrUser")
    public ResultInfo<TestBugQualityReportDTO> allTestBugStatusReportBySonOrUser(@RequestBody TestBugQualityReportQuery query) throws Exception {

        logger.info("allTestBugStatusReportBySonOrUser start paramData" + query.toString());

        //参数校验
        init(query, "testBugStatusReportByTotal");

        TestBugQualityReportDTO testBugQualityReportDTO = testBugQualityReportService.allTestBugStatusReportBySonOrUser(query);

        logger.info("allTestBugStatusReportBySonOrUser end paramData" + query.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_QUALITY_REPORT_SUCCESS, testBugQualityReportDTO);


    }


    /**
     * 查询该父项目下所有子项目的所有高级缺陷总和
     *
     * @param query 需带 proId  分页数据
     * @return
     */
    @PostMapping("/selectHighTestBugForParentProj")
    public ResultInfo<Page<TestBugDTO>> selectHighTestBugForParentProj(@RequestBody TestBugQuery query) throws Exception {

        logger.info("selectHighTestBugForParentProj start paramData" + query.toString());

        Page<TestBugDTO> testBugDTOPage = testBugQualityReportService.selectHighTestBugForParentProj(query);

        logger.info("selectHighTestBugForParentProj end paramData" + query.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_QUALITY_REPORT_SUCCESS, testBugDTOPage);


    }


    //参数检验的方法
    public static void init(TestBugQualityReportQuery testBugQualityReportQuery, String funcCode) throws Exception {

        ParamVerifyUtil paramVerifyUtil = new ParamVerifyUtil();

        Map<String, Object> dataMap = CltUtils.beanToMap(testBugQualityReportQuery);

        if ("testBugDensityReportByModule".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("testBugGradeReportByModuleOrUser".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("testBugDensityReportByUser".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("testBugStatusReportByModuleOrUser".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("testBugGradeReportByTotal".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("testBugStatusReportByTotal".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("allTestBugStatusReportBySonOrUser".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        }

    }

    //获取前几天的日期
    public Date getDate(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        date = calendar.getTime();

        return date;
    }


}
