package net.northking.cloudtest.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.northking.cloudtest.constants.ErrorConstants;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.report.CltStsProgressMapper;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.report.CltStsProgress;
import net.northking.cloudtest.dto.report.GanttReportData;
import net.northking.cloudtest.dto.report.GanttReportDto;
import net.northking.cloudtest.dto.report.TrendReportDTO;
import net.northking.cloudtest.exception.GlobalException;
import net.northking.cloudtest.query.report.TrendReportQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.service.TrendReportService;
import net.northking.cloudtest.utils.MapperUtils;
import net.northking.cloudtest.utils.TextUtils;

/**
 * @Title:
 * @Description: 趋势报告逻辑实现层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-11 16:57
 * @UpdateUser:
 * @Version:0.1
 */

@Service
public class TrendReportServiceImpl implements TrendReportService {

    //日志
    private final static Logger logger = LoggerFactory.getLogger(TrendReportServiceImpl.class);

    @Autowired
    private CltStsProgressMapper cltStsProgressMapper;

    @Autowired
    private CltProjectMapper cltProjectMapper;



    /**
     * 缺陷趋势报告
     *
     * @param trendReportQuery
     * @return
     * @throws Exception
     */
    @Override
    public TrendReportDTO testBugTrendReport(TrendReportQuery trendReportQuery) throws Exception {
        //计划开始时间
        Date startDateUI = trendReportQuery.getStartDate();

        //计划结束时间
        Date endDateUI = trendReportQuery.getEndDate();


        List<CltStsProgress> testBugTrend = getTestBugTrend(trendReportQuery);

        //组装数据
        TrendReportDTO trendReportDTO = getTrendReportDTO(testBugTrend, startDateUI, endDateUI);

        return trendReportDTO;
    }


    /**
     * 获取甘特图数据
     *
     * @param trendReportQuery
     * @return
     * @throws Exception
     */
    @Override
    public GanttReportDto getGanttReport(TrendReportQuery trendReportQuery) throws Exception {

        GanttReportDto ganttReportDto = new GanttReportDto();
        List<GanttReportData> ganttReportDataList = new ArrayList<>();

        if (trendReportQuery.getProId() == null) {
            throw new GlobalException(ResultCode.INVALID_PARAM.msg(), "参数中需要提供ProId");
        }

        //查询父项目下的子项目
        List<CltProject> sonProjectList = cltProjectMapper.querySonCltProject(trendReportQuery.getProId());

        //遍历子项目，把值赋予ganttReportDto
        for (CltProject cltProject : sonProjectList) {

            GanttReportData ganttReportData = new GanttReportData();
            ganttReportData.setProName(cltProject.getProName());
            ganttReportData.setStartTime(cltProject.getTestPlanStartTime());
            ganttReportData.setEndTime(cltProject.getTestPlanEndTime());

            ganttReportDataList.add(ganttReportData);
        }

        //按计划日期开始时间顺序排序
        ganttReportDataList.sort((o1, o2) -> {
            Date startTime = o1.getStartTime();
            long l = o1.getStartTime().getTime() - o2.getStartTime().getTime();
            return l > 0 ? 1 : -1;
        });

        ganttReportDto.setGanttReportList(ganttReportDataList);

        return ganttReportDto;
    }


    /**
     * 项目整体缺陷趋势报告
     *
     * @param trendReportQuery
     * @return
     * @throws Exception
     */
    @Override
    public TrendReportDTO testWholeBugTrendReport(TrendReportQuery trendReportQuery) throws Exception {

        //计划开始时间
        Date startDateUI = trendReportQuery.getStartDate();

        //计划结束时间
        Date endDateUI = trendReportQuery.getEndDate();

        List<CltProject> sonProjectList = cltProjectMapper.querySonCltProject(trendReportQuery.getProId());  //查询父项目下有多少子项目

        //以下为将多个子项目的缺陷趋势数据合并
        //横坐标的时间轴
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String start = sdf.format(startDateUI);

        String end = sdf.format(endDateUI);

        //横坐标的时间轴
        List<String> xAxis = collectLocalDates(LocalDate.parse(start), LocalDate.parse(end));//时间轴


        int lengX = xAxis.size();
        int lengY = 12;

        //用于保存多个子项目二维数组的集合
        List<int[][]> sonProjectDataList = new ArrayList<>();

        for (CltProject sonProject : sonProjectList) {

            //获取一个子工程的趋势报告
            trendReportQuery.setProId(sonProject.getProId());
            trendReportQuery.setType(sonProject.getProType());
            TrendReportDTO trendReportDTO = testBugTrendReport(trendReportQuery);

            //将一个子工程的趋势报告组装到一个二维数组中
            List<String> yAxis = trendReportDTO.getyAxis();
            int[][] sonProjectData = new int[lengX][lengY];

            for (int i = 0; i < yAxis.size(); i++) {
                if (TextUtils.isEmpty(yAxis.get(i))) {
                    continue;
                }

                Map<String, Object> yItem = MapperUtils.json2map(yAxis.get(i));
                for (int j = 1; j <= lengY; j++) {
                    String key = null;
                    if (j == lengY) {
                        key = "total";
                    } else {
                        key = j + "";
                    }
                    sonProjectData[i][j - 1] = Integer.parseInt(yItem.get(key).toString());
                }
            }
            sonProjectDataList.add(sonProjectData);
        }

        //将每个工程的二维数组按坐标相加
        int[][] total = new int[lengX][lengY];
        for (int[][] sonProjectData : sonProjectDataList) {
            for (int i = 0; i < lengX; i++) {
                for (int j = 0; j < lengY; j++) {
                    total[i][j] += sonProjectData[i][j];
                }
            }
        }

        //创建TrendReportDTO用于返回
        TrendReportDTO trendReportDTO = new TrendReportDTO();
        trendReportDTO.setStartDate(startDateUI);
        trendReportDTO.setEndDate(endDateUI);
        trendReportDTO.setxAxis(xAxis);

        List<String> yAxisTotal = new ArrayList<>();

        //组装yAxis
        for (int i = 0; i < lengX; i++) {
            StringBuffer bf = new StringBuffer();
            for (int j = 0; j < lengY; j++) {
                if (j == 0) {
                    bf.append("{");
                }
                if (j == lengY - 1) {
                    bf.append("\"total\":\"" + total[i][j] + "\"}");
                } else {
                    bf.append("\"" + (j + 1) + "\":\"" + total[i][j] + "\",");
                }
            }
            yAxisTotal.add(bf.toString());
        }
        trendReportDTO.setyAxis(yAxisTotal);
        return trendReportDTO;
    }


    //测试执行趋势报告
    @Override
    public TrendReportDTO testExecuteTrendReport(TrendReportQuery trendReportQuery) throws Exception {

        Date startDateUI = trendReportQuery.getStartDate();//计划开始时间(前端)

        Date endDateUI = trendReportQuery.getEndDate();//计划结束时间(前端)

        List<CltStsProgress> testBugTrend = getTestExecuteTrendByStatus(trendReportQuery);

        //组装数据
        TrendReportDTO trendReportDTO = getTrendReportDTO(testBugTrend, startDateUI, endDateUI);

        return trendReportDTO;


    }


    //查询缺陷趋势的数据
    public List<CltStsProgress> getTestBugTrend(TrendReportQuery trendReportQuery) {

        List<CltStsProgress> cltStsProgressList = null;

        try {

            cltStsProgressList = cltStsProgressMapper.queryTestBugTrendData(trendReportQuery);

        } catch (Exception e) {

            logger.info("testBugTrendReport", e);

            throw new GlobalException(ErrorConstants.TEST_BUG_TREND_REPORT_ERROR_CODE, ErrorConstants.TEST_BUG_TREND_REPORT_ERROR_MESSAGE);

        }

        return cltStsProgressList;
    }


    //查询测试执行趋势的数据(根据状态)
    public List<CltStsProgress> getTestExecuteTrendByStatus(TrendReportQuery trendReportQuery) {

        List<CltStsProgress> cltStsProgressList = null;

        try {

            cltStsProgressList = cltStsProgressMapper.queryTestExecuteTrendDataByStatus(trendReportQuery);

        } catch (Exception e) {

            logger.info("getTestExecuteTrend", e);

            throw new GlobalException(ErrorConstants.TEST_EXECUTE_TREND_REPORT_ERROR_CODE, ErrorConstants.TEST_EXECUTE_TREND_REPORT_ERROR_MESSAGE);

        }

        return cltStsProgressList;

    }


    //组装数据
    public TrendReportDTO getTrendReportDTO(List<CltStsProgress> cltStsProgressList, Date startDate, Date endDate) throws Exception {


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String start = sdf.format(startDate);

        String end = sdf.format(endDate);

        //横坐标的时间轴
        List<String> xAxis = collectLocalDates(LocalDate.parse(start), LocalDate.parse(end));//时间轴

        //存放result的集合
        List<String> results = new ArrayList<>();

        Map<String, String> cltStsProgressMap = new HashMap<>();

        for (int i = 0; i < cltStsProgressList.size(); i++) {

            CltStsProgress cltStsProgress = cltStsProgressList.get(i);


            cltStsProgressMap.put(cltStsProgress.getStsData(), cltStsProgress.getResult());

        }

       /* for (int i = 0; i < xAxis.size(); i++) {
            String s =  xAxis.get(i);

            if(cltStsProgressMap.containsKey(s)){
                results.add(cltStsProgressMap.get(s));

            }else{

                results.add("");
            }

        }*/

        getResultData(xAxis, cltStsProgressMap, results);


        TrendReportDTO trendReportDTO = new TrendReportDTO();

        trendReportDTO.setxAxis(xAxis);

        trendReportDTO.setyAxis(results);

        trendReportDTO.setStartDate(startDate);

        trendReportDTO.setEndDate(endDate);

        return trendReportDTO;

    }


    public List<String> collectLocalDates(LocalDate start, LocalDate end) {
        // 用起始时间作为流的源头，按照每次加一天的方式创建一个无限流
        return Stream.iterate(start, localDate -> localDate.plusDays(1))
                // 截断无限流，长度为起始时间和结束时间的差+1个
                .limit(ChronoUnit.DAYS.between(start, end) + 1)
                // 由于最后要的是字符串，所以map转换一下
                .map(LocalDate::toString)
                // 把流收集为List
                .collect(Collectors.toList());
    }


    //获取日期对应的result
    private void getResultData(List<String> timeList, Map<String, String> map, List<String> results) throws Exception {

        String s = "";
        for (int i = 0; i < timeList.size(); i++) {
            String time = timeList.get(i);
            if (i == 0) {
                if (map.containsKey(time)) {
                    results.add(map.get(time));
                    s = map.get(time);
                } else {
                    results.add(s);
                }
            } else {
                if (map.containsKey(time)) {
                    results.add(map.get(time));
                    s = map.get(time);
                } else {
                    results.add(s);
                }

            }

        }

    }

}
