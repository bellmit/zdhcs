package net.northking.cloudtest.service;

/**
 * Created by 老邓 on 2018/5/21.
 */
public interface GenDemandReportChartService {
    /**
     * 生成图表
     * @param projectId
     * @return
     */
    int genChart(String projectId);
}
