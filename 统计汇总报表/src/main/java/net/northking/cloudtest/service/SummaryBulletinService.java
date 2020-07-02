package net.northking.cloudtest.service;

import net.northking.cloudtest.domain.report.CltReport;
import net.northking.cloudtest.dto.SummaryBulletin;

import java.util.Map;

/**
 * @Title: 总结报告处理服务
 * @Description:
 * @Company: Northking
 * @Author: chuangsheng.huang
 * @CreateDate: 2018/5/11
 * @UpdateUser:
 * @Version:0.1
 */
public interface SummaryBulletinService {

    /**
     * 获取总结报告数据
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
    CltReport insertOrUpdateData(String projectId, String operatorUserId, String docPath, String swfPath) throws Exception;
}
