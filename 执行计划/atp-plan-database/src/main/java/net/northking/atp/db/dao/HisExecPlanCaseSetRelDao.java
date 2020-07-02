/*
 * Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
 *
 */
package net.northking.atp.db.dao;

import net.northking.atp.db.mapper.HisExecPlanCaseSetRelMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 测试计划-用例集关系历史表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-05-05 17:22:21  <br>
 *
 * @author: database-mybatis-maven-plugin  <br>
 * @since: 1.0 <br>
 */
public interface HisExecPlanCaseSetRelDao extends HisExecPlanCaseSetRelMapper {

    /**
     * // 根据计划集合批量删除历史用例集
     *
     * @param planIds
     * @return
     */
    long batchDeleteByPlanIds(@Param("planIds") List<String> planIds);
    // ----      The End by Generator     ----//

}
