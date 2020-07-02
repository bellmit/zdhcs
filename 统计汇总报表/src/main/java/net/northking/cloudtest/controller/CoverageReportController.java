package net.northking.cloudtest.controller;

import net.northking.cloudtest.constants.SuccessConstants;
import net.northking.cloudtest.dto.report.CoverageReportDTO;
import net.northking.cloudtest.dto.report.CoverageReportData;
import net.northking.cloudtest.dto.report.ProgressReportDTO;
import net.northking.cloudtest.feign.analyse.DemandFeignClient;
import net.northking.cloudtest.query.analyse.CltDemandDto;
import net.northking.cloudtest.query.analyse.DemandQuery;
import net.northking.cloudtest.query.report.CoverageReportQuery;
import net.northking.cloudtest.query.report.ProgressReportQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.CoverageReportService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description:
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-08 15:20
 * @UpdateUser:
 * @Version:0.1
 */

@RestController
@RequestMapping(value = "/report/coverageReport")
public class CoverageReportController {


   /* @Autowired
    private CoverageReportService coverageReportService;*/
    @Autowired
    private DemandFeignClient demandFeignClient;

    //日志
    private final static Logger logger = LoggerFactory.getLogger(CoverageReportController.class);


    //需求分析/用例覆盖报告-交易/模块
    @PostMapping("/testcaseDemandCoverageReport")
    public ResultInfo<CoverageReportDTO> testcaseDemandCoverageReport(@RequestBody CoverageReportQuery coverageReportQuery) throws Exception {

//        logger.info("testcaseDemandCoverageReport start paramData" + coverageReportQuery.toString());

        //参数校验
        init(coverageReportQuery, "testcaseDemandCoverageReport");

//        if (StringUtils.isEmpty(coverageReportQuery.getType())) {
//            coverageReportQuery.setType("T");
//
//        }

//        CoverageReportDTO coverageReportDTO = coverageReportService.testcaseDemandCoverageReport(coverageReportQuery);

//        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_COVERAGE_PROGRESS_REPORT_SUCCESS, coverageReportDTO);

//        logger.info("testcaseDemandCoverageReport end paramData" + coverageReportQuery.toString());


        // TODO: 2020/1/16  重构放入service层
        DemandQuery query = new DemandQuery();
        query.setProjectId(coverageReportQuery.getProId());
        List<CltDemandDto> cltDemandDtos = demandFeignClient.statisticeNumber(query).getData();

        ArrayList<String> x = new ArrayList<>();
        List<CoverageReportData> y = new ArrayList<>();
        List<Integer> demData = new ArrayList<>();
        List<Integer> testcaseData = new ArrayList<>();
        //组装数据
        cltDemandDtos.forEach(cltDemandDto -> {
            x.add(cltDemandDto.getName());


            demData.add(cltDemandDto.getDemandNumber() == null ? 0 : cltDemandDto.getDemandNumber());
            testcaseData.add(cltDemandDto.getCaseNumber() == null ? 0 : cltDemandDto.getCaseNumber());

        });
        CoverageReportData demReportData = new CoverageReportData();
        CoverageReportData testcaseReportData = new CoverageReportData();

        testcaseReportData.setName("测试用例数量");
        testcaseReportData.setData(testcaseData);

        demReportData.setName("需求分析数量");
        demReportData.setData(demData);

        y.add(testcaseReportData);
        y.add(demReportData);

        CoverageReportDTO coverageReportDTO = new CoverageReportDTO();
        coverageReportDTO.setxAxis(x);
        coverageReportDTO.setyAxis(y);

        return new ResultInfo<>(ResultCode.SUCCESS, "success", coverageReportDTO);


    }


    //参数检验的方法
    public static void init(CoverageReportQuery coverageReportQuery, String funcCode) throws Exception {

        ParamVerifyUtil paramVerifyUtil = new ParamVerifyUtil();

        Map<String, Object> dataMap = CltUtils.beanToMap(coverageReportQuery);

        if ("testcaseDemandCoverageReport".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        }

    }

}
