/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.dao;

import net.northking.atp.db.persistent.ReBusinessRules;
import net.northking.atp.db.mapper.ReBusinessRulesMapper;

import java.util.List;
import java.util.Map;


/**
 * 业务规则信息_基础信息
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-16 09:25:01  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
public interface ReBusinessRulesDao extends ReBusinessRulesMapper
{
  // ----      The End by Generator     ----//
  List<Map<String,Object>> queryForLike(ReBusinessRules info);
}
