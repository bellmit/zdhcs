package net.northking.cloudtest.service;

import java.util.Map;

/**
 * @Author:zwy
 * @Despriction:
 * @Date:Create in 10:59 2018/5/24
 * @Modify By:
 */
public interface ExecuteBulletinService {
    void dealWithReport(String projectId, String roundId, Map dataMap) throws Exception ;
    Integer insertOrUpdateData(String projectId, String roundId, String operatorUserId, String docPath, String swfPath);
}
