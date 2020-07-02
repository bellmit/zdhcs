package net.northking.cloudtest.service;

/**
 * @Title: 生成图表服务
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/12
 * @UpdateUser:
 * @Version:0.1
 */
public interface GenChartService {
    /**
     * 生成图表
     * @param projectId
     * @return
     */
    int genChart(String projectId);
}
