/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReExecPlanPluginSettingSrvAdapter;
import net.northking.atp.db.dao.ReExecPlanPluginSettingDao;
import net.northking.atp.db.persistent.ReExecPlanPluginSetting;
import net.northking.atp.db.service.ReExecPlanPluginSettingService;
import net.northking.db.BasicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
   * 测试计划插件设置表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-04-17 11:18:53  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReExecPlanPluginSettingServiceImpl extends ReExecPlanPluginSettingSrvAdapter implements ReExecPlanPluginSettingService
{
private static final Logger logger = LoggerFactory.getLogger(ReExecPlanPluginSettingServiceImpl.class);

  @Autowired
  private ReExecPlanPluginSettingDao reExecPlanPluginSettingDao;

  protected BasicDao<ReExecPlanPluginSetting> getDao()
  {
  return reExecPlanPluginSettingDao;
  }

  @Override
  public void batchUpdateSetting(List<ReExecPlanPluginSetting> pluginSettingList) {
    reExecPlanPluginSettingDao.batchUpdateSetting(pluginSettingList);
  }
// ----      The End by Generator     ----//




}

