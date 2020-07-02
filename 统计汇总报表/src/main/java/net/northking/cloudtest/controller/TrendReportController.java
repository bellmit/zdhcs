package net.northking.cloudtest.controller;

import io.swagger.annotations.Api;
import net.northking.cloudtest.constants.SuccessConstants;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectDto;
import net.northking.cloudtest.domain.project.CltProjectQuery;
import net.northking.cloudtest.dto.report.GanttReportDto;
import net.northking.cloudtest.dto.report.TrendReportDTO;
import net.northking.cloudtest.feign.project.ProjectFeignClient;
import net.northking.cloudtest.query.report.TrendReportQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.TrendReportService;
import net.northking.cloudtest.utils.CltUtils;
import net.northking.cloudtest.utils.ParamVerifyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description: 趋势报告表现层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-11 16:58
 * @UpdateUser:
 * @Version:0.1
 */

@RestController
@RequestMapping(value = "/report/trendReport")
@Api(tags = "报表api")
public class TrendReportController {

    //日志
    private final static Logger logger = LoggerFactory.getLogger(TrendReportController.class);


    @Autowired
    private TrendReportService trendReportService;
    @Autowired
    private ProjectFeignClient projectFeignClient;


    /**
     * 缺陷趋势报告
     *
     * @param trendReportQuery
     * @return
     * @throws Exception
     */
    @PostMapping("/testBugTrendReport")
    public ResultInfo<TrendReportDTO> testBugTrendReport(@RequestBody TrendReportQuery trendReportQuery) throws Exception {

        logger.info("testBugTrendReport start paramData" + trendReportQuery.toString());

        //参数校验
        init(trendReportQuery, "testBugTrendReport");

        //初始化查询时间，默认查询时间为 项目计划开始时间 到 当前
        if (trendReportQuery.getStartDate() == null || trendReportQuery.getEndDate() == null) {

            //查询项目信息
            CltProjectQuery query = new CltProjectQuery();
            query.setProId(trendReportQuery.getProId());
            CltProject project = projectFeignClient.queryProjectSimpleInfoById(query).getData();

            //设置开始 结束时间
            trendReportQuery.setStartDate(project.getTestPlanStartTime());

            //设置结束时间
            if (project.getTestPlanStartTime().before(new Date())) {

                //如果计划开始时间在当前时间之前，则结束时间为当前时间
                trendReportQuery.setEndDate(new Date());

            } else {

                //否则结束时间为计划开始时间6天后
                trendReportQuery.setEndDate(getDateBeforeDay(new Date(), -6));

            }

        }


        //查询缺陷趋势报告
        TrendReportDTO trendReportDTO = trendReportService.testBugTrendReport(trendReportQuery);

        if (trendReportDTO == null) {

            return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_TREND_REPORT_SUCCESS, new TrendReportDTO());
        }

        logger.info("testBugTrendReport end paramData" + trendReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_TREND_REPORT_SUCCESS, trendReportDTO);

    }

    /**
     * 项目整体缺陷趋势报告
     *
     * @param trendReportQuery 主要父项目ID
     * @return
     * @throws Exception
     */
    @PostMapping("/testWholeBugTrendReport")
    public ResultInfo<TrendReportDTO> testWholeBugTrendReport(@RequestBody TrendReportQuery trendReportQuery) throws Exception {

        logger.info("testWholeBugTrendReport start paramData" + trendReportQuery.toString());

        //参数校验
        init(trendReportQuery, "testWholeBugTrendReport");

        //初始化查询时间：从最早子项目计划开始时间查
        if (trendReportQuery.getStartDate() == null || trendReportQuery.getEndDate() == null) {

            //查询所有子项目
            CltProjectQuery query = new CltProjectQuery();
            query.setProId(trendReportQuery.getProId());
            List<CltProjectDto> sonProjectList = projectFeignClient.findSonProjectList(query).getData();

            Date startDate = null;

            //遍历子项目，查询到最早的时间
            for (CltProjectDto sonProject : sonProjectList) {

                Date sonStartTime = sonProject.getTestPlanStartTime();

                //如果子项目开始时间在startDate之前
                if (startDate == null || sonStartTime.before(startDate)) {

                    startDate = sonStartTime;

                }

            }

            //开始时间
            trendReportQuery.setStartDate(startDate);

            //结束时间为当前
            trendReportQuery.setEndDate(new Date());

            //设置结束时间
            if (startDate.before(new Date())) {

                //如果计划开始时间在当前时间之前，则结束时间为当前时间
                trendReportQuery.setEndDate(new Date());

            } else {

                //否则结束时间为计划开始时间6天后
                trendReportQuery.setEndDate(getDateBeforeDay(new Date(), -6));

            }

        }

        TrendReportDTO trendReportDTO = trendReportService.testWholeBugTrendReport(trendReportQuery);

        logger.info("testWholeBugTrendReport end paramData" + trendReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_TREND_REPORT_SUCCESS, trendReportDTO);

    }


    /**
     * 父项目下所有子项目的甘特图
     *
     * @param trendReportQuery 主要父项目ID
     * @return
     * @throws Exception
     */
    @PostMapping("/ganttReport")
    public ResultInfo<GanttReportDto> ganttReport(@RequestBody TrendReportQuery trendReportQuery) throws Exception {

        logger.info("ganttReport start paramData" + trendReportQuery.toString());

        //参数校验
        init(trendReportQuery, "ganttReport");

        GanttReportDto ganttReport = trendReportService.getGanttReport(trendReportQuery);

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_GANTT_REPORT_SUCCESS, ganttReport);

    }


    //测试执行趋势报告
    @PostMapping("/testExecuteTrendReport")
    public ResultInfo<TrendReportDTO> testExecuteTrendReport(@RequestBody TrendReportQuery trendReportQuery) throws Exception {

        logger.info("testExecuteTrendReport start paramData" + trendReportQuery.toString());


        if (trendReportQuery.getStartDate() == null || trendReportQuery.getEndDate() == null) {
            Date endDate = new Date();

            Date startDate = getDateBeforeDay(endDate, 6);

            trendReportQuery.setStartDate(startDate);
            trendReportQuery.setEndDate(endDate);

        }


        //参数校验
        init(trendReportQuery, "testExecuteTrendReport");

        TrendReportDTO trendReportDTO = trendReportService.testExecuteTrendReport(trendReportQuery);
        if (trendReportDTO == null) {

            return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_TREND_REPORT_SUCCESS, new TrendReportDTO());
        }


        logger.info("testExecuteTrendReport end paramData" + trendReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TEST_BUG_TREND_REPORT_SUCCESS, trendReportDTO);


    }

    //参数检验的方法
    public static void init(TrendReportQuery trendReportQuery, String funcCode) throws Exception {

        ParamVerifyUtil paramVerifyUtil = new ParamVerifyUtil();

        Map<String, Object> dataMap = CltUtils.beanToMap(trendReportQuery);

        if ("testBugTrendReport".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("testExecuteTrendReport".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("testWholeBugTrendReport".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("ganttReport".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        }
    }


    /**
     * 获取前几天的日期
     *
     * @param date
     * @return
     */
    public Date getDateBeforeDay(Date date, int before) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -before);
        date = calendar.getTime();

        return date;
    }

}
