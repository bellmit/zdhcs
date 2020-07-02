package net.northking.cloudtest.controller;

import io.swagger.annotations.ApiOperation;
import net.northking.cloudtest.constants.SuccessConstants;
import net.northking.cloudtest.dto.report.QualityReportDTO;
import net.northking.cloudtest.feign.analyse.CaseDesignFeignClient;
import net.northking.cloudtest.feign.analyse.DemandFeignClient;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.QualityReportService;
import net.northking.cloudtest.utils.MapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;

/**
 * @Title:
 * @Description: 质量报告表现层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-09 9:15
 * @UpdateUser:
 * @Version:0.1
 */

@RestController
@RequestMapping(value = "/report/qualityReport")
public class QualityReportController {

    @Autowired
    private QualityReportService qualityReportService;
    @Autowired
    DemandFeignClient demandFeignClient;
    @Autowired
    CaseDesignFeignClient caseDesignFeignClient;

    //日志
    private final static Logger logger = LoggerFactory.getLogger(QualityReportController.class);

    /**
     * 测试用例设计完成情况
     *
     * @param query
     * @return
     * @throws Exception
     */
    @RequestMapping("/testCaseQuality")
    public ResultInfo<QualityReportDTO> queryTestCaseQualityReport(@RequestBody QualityReportQuery query) throws Exception {

        logger.info("queryTestCaseQualityReport start paramData" + query.toString());
//
//        if (query.getStartDate() == null || query.getEndDate() == null) {
//
//            Date endDate = new Date();
//
//            Date startDate = getDate(endDate);
//
//            query.setStartDate(startDate);
//            query.setEndDate(endDate);
//
//        }
//
//        QualityReportDTO qualityReportDTO = qualityReportService.queryTestCaseQualityReport(query);
//

//        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_EXECUTE_PROGRESS_REPORT_SUCCESS, qualityReportDTO);
        logger.info("queryTestCaseQualityReport end");

        return caseDesignFeignClient.queryTestCaseQualityReport(query);
    }

    /**
     * 需求分析完成情况
     *
     * @param query
     * @return
     * @throws Exception
     */
    @RequestMapping("/demandQuality")
    public ResultInfo<QualityReportDTO> queryDemandQualityReport(@RequestBody QualityReportQuery query) throws Exception {

        logger.info("queryDemandQualityReport start paramData" + query.toString());


//        if (query.getStartDate() == null || query.getEndDate() == null) {
//
//            Date endDate = new Date();
//
//            Date startDate = getDate(endDate);
//
//            query.setStartDate(startDate);
//            query.setEndDate(endDate);
//
//        }


//        QualityReportDTO qualityReportDTO = qualityReportService.queryDemandQualityReport(query);

//        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_EXECUTE_PROGRESS_REPORT_SUCCESS, qualityReportDTO);

        ResultInfo<QualityReportDTO> qualityReportDTOResultInfo = demandFeignClient.queryDemandQualityReport(query);

        logger.info("feignReturn: demandFeignClient.queryDemandQualityReport：return ： " + MapperUtils.obj2json(qualityReportDTOResultInfo));

        logger.info("queryDemandQualityReport end");

        return qualityReportDTOResultInfo;
    }


    @ApiOperation("用例执行完成情况")
    @RequestMapping("/queryTestExecuteQualityReport")
    public ResultInfo<QualityReportDTO> queryTestExecuteQualityReport(@RequestBody QualityReportQuery query) throws Exception {

        logger.info("queryTestExecuteQualityReport start paramData" + query.toString());

        if (StringUtils.isEmpty(query.getType())) {
            String type = "U";
            query.setType(type);

        }


        if (query.getStartDate() == null || query.getEndDate() == null) {

            Date endDate = new Date();

            Date startDate = getDate(endDate, 30);

            query.setStartDate(startDate);
            query.setEndDate(endDate);

        }


        QualityReportDTO qualityReportDTO = qualityReportService.queryTestExecuteQualityReport(query);

        logger.info("queryTestExecuteQualityReport end");
        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_EXECUTE_PROGRESS_REPORT_SUCCESS, qualityReportDTO);
    }


    /**
     * 获取前几天的日期
     *
     * @param date
     * @param beforeDay 提前的天数
     * @return
     */
    public Date getDate(Date date, int beforeDay) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -beforeDay);
        date = calendar.getTime();

        return date;
    }


}
