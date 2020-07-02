package net.northking.cloudtest.service;

/**
 * Created by liujinghao on 2018/5/20.
 */
public interface GenChartExecuteService {
    /**
     * 生成用例执行图表
     */
    int GenChartExecute(String proId)throws Exception;
    //按人员一周测试执行完成情况
    int GenChartExecuteByUser(String proId) throws Exception;
}
