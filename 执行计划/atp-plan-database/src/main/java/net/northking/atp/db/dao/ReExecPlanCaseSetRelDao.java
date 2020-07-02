/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.dao;

import net.northking.atp.db.mapper.ReExecPlanCaseSetRelMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 测试执行计划_测试用例_关系表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-04-17 11:18:53  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
public interface ReExecPlanCaseSetRelDao extends ReExecPlanCaseSetRelMapper
{
    long batchDeleteByPlanIds(@Param("planIds") List<String> planIds);
    // ----      The End by Generator     ----//

}
