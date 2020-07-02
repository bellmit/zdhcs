/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.dao;

import net.northking.atp.db.persistent.ReRulePluginInfo;
import net.northking.atp.db.mapper.ReRulePluginInfoMapper;

import java.util.List;
import java.util.Map;


/**
 * 规则插件信息
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-15 20:45:49  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
public interface ReRulePluginInfoDao extends ReRulePluginInfoMapper
{
  // ----      The End by Generator     ----//
    List<Map<String,Object>> queryForLike(ReRulePluginInfo info);
    int updateByLibraryId(ReRulePluginInfo info);
}
