package net.northking.cloudtest.service;

import org.springframework.stereotype.Component;

/**
 * Created by liujinghao on 2018/5/20.
 */
public interface GenCompleteChartService {
    /**
     * 生成完成情况图表
     */
    int GenCompleteChart(String proId)throws Exception;
}
