package net.northking.cloudtest.controller;

import io.swagger.annotations.ApiOperation;
import net.northking.cloudtest.constants.SuccessConstants;
import net.northking.cloudtest.dto.report.ProgressReportDTO;
import net.northking.cloudtest.query.report.ProgressReportQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.ProgressReportService;
import net.northking.cloudtest.utils.CltUtils;
import net.northking.cloudtest.utils.ParamVerifyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description: 进度报告统计表
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-04 18:06
 * @UpdateUser:
 * @Version:0.1
 */

@RestController
@RequestMapping("report/progressReport")
public class ProgressReportController {

    @Autowired
    private ProgressReportService progressReportService;

    //日志
    private final static Logger logger = LoggerFactory.getLogger(ProgressReportController.class);


    /**
     * 需求分析阶段进度报告(每日)
     *
     * @param progressReportQuery
     * @return
     * @throws Exception
     */
    @PostMapping("/analyseProgressReportByDay")
    public ResultInfo<ProgressReportDTO> analyseProgressReportByDay(@RequestBody ProgressReportQuery progressReportQuery) throws Exception {

        logger.info("queryAnalyseReport start paramData" + progressReportQuery.toString());

        //参数校验
        init(progressReportQuery, "analyseProgressReportByDay");

        ProgressReportDTO progressReportDTO = progressReportService.analyseProgressReportByDay(progressReportQuery);

        logger.info("queryAnalyseReport end paramData" + progressReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_ANALYSE_PROGRESS_REPORT_SUCCESS, progressReportDTO);


    }


    //需求分析阶段进度报告（饼图）
    @PostMapping("/analyseProgressReportByPie")
    public ResultInfo<ProgressReportDTO> analyseProgressReportByPie(@RequestBody ProgressReportQuery progressReportQuery) throws Exception {

        logger.info("analyseProgressReportByPie start paramData" + progressReportQuery.toString());

        //参数校验
        init(progressReportQuery, "analyseProgressReportByPie");

        ProgressReportDTO progressReportDTO = progressReportService.analyseProgressReportByPie(progressReportQuery);

        if (progressReportDTO.getSchedule() == null) {
            progressReportDTO.setSchedule(0D);
        }

        logger.info("analyseProgressReportByPie end paramData" + progressReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_ANALYSE_PROGRESS_REPORT_SUCCESS, progressReportDTO);


    }

    //用例设计阶段报告（每日）
    @PostMapping("/testCaseProgressReportByDay")
    public ResultInfo<ProgressReportDTO> testCaseProgressReportByDay(@RequestBody ProgressReportQuery progressReportQuery) throws Exception {

        logger.info("testCaseProgressReportByDay start paramData" + progressReportQuery.toString());

        //参数校验
        init(progressReportQuery, "testCaseProgressReportByDay");

        ProgressReportDTO progressReportDTO = progressReportService.testCaseProgressReportByDay(progressReportQuery);

        logger.info("testCaseProgressReportByDay end paramData" + progressReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TESTCASE_PROGRESS_REPORT_SUCCESS, progressReportDTO);


    }


    /**
     * 用例设计阶段报告（饼图）
     *
     * @param progressReportQuery proId testPlanNum  startDate  endDate
     * @return
     * @throws Exception
     */
    @PostMapping("/testCaseProgressReportByPie")
    public ResultInfo<ProgressReportDTO> testCaseProgressReportByPie(@RequestBody ProgressReportQuery progressReportQuery) throws Exception {

        logger.info("testCaseProgressReportByPie start paramData" + progressReportQuery.toString());

        //参数校验
        init(progressReportQuery, "testCaseProgressReportByPie");

        ProgressReportDTO progressReportDTO = progressReportService.testCaseProgressReportByPie(progressReportQuery);

        logger.info("testCaseProgressReportByPie end paramData" + progressReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_TESTCASE_PROGRESS_REPORT_SUCCESS, progressReportDTO);

    }


    //用例执行阶段进度报告（单轮）
    @PostMapping("/executeSingleRoundReport")
    public ResultInfo<ProgressReportDTO> executeSingleRoundReport(@RequestBody ProgressReportQuery progressReportQuery) throws Exception {

        logger.info("executeSingleRoundReport start paramData" + progressReportQuery.toString());

        //参数校验
        init(progressReportQuery, "executeSingleRoundReport");

        ProgressReportDTO progressReportDTO = progressReportService.executeSingleRoundReport(progressReportQuery);

        logger.info("executeSingleRoundReport end paramData" + progressReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_EXECUTE_PROGRESS_REPORT_SUCCESS, progressReportDTO);


    }


    //用例执行阶段进度报告（总体）
    @PostMapping("/executeTotalReport")
    public ResultInfo<ProgressReportDTO> executeTotalReport(@RequestBody ProgressReportQuery progressReportQuery) throws Exception {

        logger.info("executeTotalReport start paramData" + progressReportQuery.toString());

        //参数校验
        init(progressReportQuery, "executeTotalReport");

        ProgressReportDTO progressReportDTO = progressReportService.executeTotalReport(progressReportQuery);

        logger.info("executeTotalReport end paramData" + progressReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_EXECUTE_PROGRESS_REPORT_SUCCESS, progressReportDTO);


    }

    //用例执行阶段进度报告（饼图）
    @PostMapping("/executeProgressReportByPie")
    public ResultInfo<ProgressReportDTO> executeProgressReportByPie(@RequestBody ProgressReportQuery progressReportQuery) throws Exception {

        logger.info("executeProgressReportByPie start paramData" + progressReportQuery.toString());

        //参数校验
        init(progressReportQuery, "executeProgressReportByPie");

        ProgressReportDTO progressReportDTO = progressReportService.executeProgressReportByPie(progressReportQuery);

        logger.info("executeProgressReportByPie end paramData" + progressReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_EXECUTE_PROGRESS_REPORT_SUCCESS, progressReportDTO);


    }

    @ApiOperation("项目总体进度报告")
    @PostMapping("/projectProgressReportByTotal")
    public ResultInfo<ProgressReportDTO> projectProgressReportByTotal(@RequestBody ProgressReportQuery progressReportQuery) throws Exception {

        logger.info("projectProgressReportByTotal start paramData" + progressReportQuery.toString());

        //参数校验
        init(progressReportQuery, "projectProgressReportByTotal");

        ProgressReportDTO progressReportDTO = progressReportService.projectProgressReportByTotal(progressReportQuery);

        logger.info("projectProgressReportByTotal end paramData" + progressReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_PROJECT_TOATAL_REPORT_SUCCESS, progressReportDTO);

    }

    /**
     * 获取项目当前总进度
     * <p>
     * by liang.zhong 2019-12-1 15:32:12
     *
     * @param progressReportQuery 这里主要需要提供proId
     * @return
     * @throws Exception
     */
    @ApiOperation("获取项目当前总进度")
    @PostMapping("/projectTotalProgress")
    public ResultInfo<Float> projectTotalProgress(@RequestBody ProgressReportQuery progressReportQuery) throws Exception {

        logger.info("projectTotalProgress start paramData" + progressReportQuery.toString());

        //参数校验
        init(progressReportQuery, "projectTotalProgress");

        float allProgress = progressReportService.getProjectProgress(progressReportQuery);

        logger.info("projectTotalProgress end paramData" + progressReportQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_PROJECT_TOATAL_REPORT_SUCCESS, allProgress);

    }


    /**
     * 获取多个项目当前总进度
     * <p>
     * by zhognliang  2019年12月24日14:07:52
     *
     * @param proIds 这里主要需要提供proId集合
     * @return Map     key为proId  value为 totalProgress
     * @throws Exception
     */
    @ApiOperation("获取多个项目当前总进度")
    @PostMapping("/getTotalProgressesByProIds")
    public ResultInfo<Map<String, String>> getTotalProgressesByProIds(@RequestBody List<String> proIds) throws Exception {

        logger.info("projectTotalProgress start paramData" + proIds.toString());

        //参数校验
        if (proIds == null || proIds.size() < 1) {
            return new ResultInfo<>(ResultCode.INVALID_PARAM, "参数错误");
        }

        //创建map 用于存放返回数据
        HashMap<String, String> map = new HashMap<>();

        for (String proId : proIds) {
            ProgressReportQuery progressReportQuery = new ProgressReportQuery();
            progressReportQuery.setProId(proId);
            Float projectProgress = progressReportService.getProjectProgress(progressReportQuery);

            //格式化数据  构造方法的字符格式这里如果小数不足2位,会以0补足.
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String progress = decimalFormat.format(projectProgress * 100) + "%";

            map.put(proId, progress);
        }

        logger.info("getTotalProgressesByProIds end paramData");

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_PROJECT_TOATAL_REPORT_SUCCESS, map);

    }


    //参数检验的方法
    public static void init(ProgressReportQuery progressReportQuery, String funcCode) throws Exception {

        ParamVerifyUtil paramVerifyUtil = new ParamVerifyUtil();

        Map<String, Object> dataMap = CltUtils.beanToMap(progressReportQuery);

        if ("analyseProgressReportByDay".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("analyseProgressReportByPie".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("testCaseProgressReportByPie".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("testCaseProgressReportByDay".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("executeSingleRoundReport".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("executeProgressReportByPie".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        } else if ("projectTotalProgress".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        }

    }

}


