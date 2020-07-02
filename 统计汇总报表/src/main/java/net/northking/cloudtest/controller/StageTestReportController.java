package net.northking.cloudtest.controller;

import net.northking.cloudtest.constants.SuccessConstants;
import net.northking.cloudtest.domain.report.CltReport;
import net.northking.cloudtest.query.report.CltReportQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.StageTestReportService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import net.northking.cloudtest.common.Page;

import java.util.List;

/**
 * Created by liujinghao on 2018/5/11.
 */
@RequestMapping("/report/stageTestReport")
@RestController
public class StageTestReportController {

    @Autowired
    private StageTestReportService stageTestReportService;

    private final static Logger logger = LoggerFactory.getLogger(StageTestReportController.class);

    /**
     * 查询报告信息
     * @param cltReport
     * @return
     * @throws Exception
     */
    @PostMapping("/queryReport")
    public  ResultInfo<CltReport> queryReport(@RequestBody CltReport cltReport) throws Exception {
        System.out.println("报告id"+cltReport.getId());
        logger.info("queryReport" + cltReport.toString());

        CltReport clt = stageTestReportService.queryReport(cltReport);

        logger.info("queryReport end");

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.DOWNLOAD_TEST_REPORT_SUCCESS,clt);
    }

    /**
     * 报告预览
     * @param cltReport
     * @return
     * @throws Exception
     */
    @PostMapping("/previewReport")
    ResultInfo<CltReport>  previewReport(@RequestBody CltReport cltReport) throws Exception {
        logger.info("previewReport" + cltReport.toString());

        CltReport clt = stageTestReportService.previewReport(cltReport);

        if(clt == null){
            clt = new CltReport();
        }
        logger.info("previewReport end");

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.PREVIEW_TEST_REPORT_SUCCESS,clt);
    }

    /**
     * 报告预览列表
     * @param query
     * @return
     * @throws Exception
     */
    @PostMapping("/previewTestReports")
    ResultInfo<Page<CltReport>> previewTestReports(@RequestBody CltReportQuery query) throws Exception{
        logger.info("previewReport" + query.toString());

        Page<CltReport>clt= stageTestReportService.previewTestReports(query);

        logger.info("previewReport end");

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.PREVIEW_TEST_REPORT_SUCCESS,clt);
    }

    /**
     * 重新生成
     * @param cltReport
     * @return
     * @throws Exception
     */
    @PostMapping("/regenReport")
    ResultInfo<CltReport> regenReport(@RequestBody CltReport cltReport) throws Exception {
        logger.info("previewReport" + cltReport.toString());

        CltReport clt= stageTestReportService.reGenReport(cltReport);

        logger.info("previewReport end");

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.REGEN_TEST_REPORT_SUCCESS,clt);
    }
}
