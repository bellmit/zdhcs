package net.northking.cloudtest.service;

import java.util.Map;

/**
 * @Title: 设计阶段报告处理服务
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/11
 * @UpdateUser:
 * @Version:0.1
 */
public interface DesignBulletinService {

    /**
     * 获取设计报告数据
     * @param projectId
     * @param dataMap
     * @throws Exception
     */
    void dealWithReport(String projectId, Map dataMap) throws Exception;

    /**
     * 插入或更新报告数据
     * @param projectId
     * @param operatorUserId
     * @param docPath
     * @param swfPath
     * @return
     */
    Integer insertOrUpdateData(String projectId, String operatorUserId, String docPath, String swfPath) throws Exception;

    /**
     * 生成图表
     * @param projectId
     * @return
     */
    int genChart(String projectId);
}
