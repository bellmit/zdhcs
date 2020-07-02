/*
 * Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
 *
 */
package net.northking.atp.db.dao;

import net.northking.atp.db.mapper.RePlanExecInfoMapper;
import net.northking.atp.db.persistent.RePlanExecInfo;
import net.northking.atp.entity.CaseAndSetDetails;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * 计划执行记录表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-04-26 15:26:29  <br>
 *
 * @author: database-mybatis-maven-plugin  <br>
 * @since: 1.0 <br>
 */
public interface RePlanExecInfoDao extends RePlanExecInfoMapper {
    // ----      The End by Generator     ----//
    RePlanExecInfo queryFirstById(@Param("planId") String planId);

    List<RePlanExecInfo> queryInfoStatusIsNotFinished(@Param("query") Map<String, Object> query);

    List<CaseAndSetDetails> queryCasesByPlanId(String planId);

    //add by chuangsheng.huang 数据测试用
    List<CaseAndSetDetails> queryDataCasesByPlanId(String planId);

    List<RePlanExecInfo> queryExecInfoWithoutUrl();

    List<String> queryByPlanIds(@Param("planIds") List<String> planIds);

    long batchDeleteByPlanIds(@Param("planIds") List<String> planIds);
}
