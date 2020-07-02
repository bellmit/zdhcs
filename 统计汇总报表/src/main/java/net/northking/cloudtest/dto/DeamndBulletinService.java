package net.northking.cloudtest.dto;

import java.util.Map;

/**
 * Created by 老邓 on 2018/5/22.
 */
public interface DeamndBulletinService {
    void dealWithReport(String projectId, Map dataMap);

    Integer insertOrUpdateData(String projectId, String operatorUserId, String docPath, String swfPath);
}
