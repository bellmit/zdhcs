/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.dao;

import net.northking.atp.db.persistent.RePlanEnvRel;
import net.northking.atp.db.mapper.RePlanEnvRelMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 执行计划-环境关联表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-08-05 16:56:26  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
public interface RePlanEnvRelDao extends RePlanEnvRelMapper
{
    long batchDeleteByPlanIds(@Param("planIds") List<String> planIds);
    // ----      The End by Generator     ----//

}
