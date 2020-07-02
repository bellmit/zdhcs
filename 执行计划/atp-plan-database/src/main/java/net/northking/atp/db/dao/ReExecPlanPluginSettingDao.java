/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.dao;

import net.northking.atp.db.mapper.ReExecPlanPluginSettingMapper;
import net.northking.atp.db.persistent.ReExecPlanPluginSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 测试计划插件设置表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-04-17 11:18:53  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
public interface ReExecPlanPluginSettingDao extends ReExecPlanPluginSettingMapper
{
  // ----      The End by Generator     ----//
    void batchUpdateSetting(@Param("list") List<ReExecPlanPluginSetting> pluginSettingList);
}
