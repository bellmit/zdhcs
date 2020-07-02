/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.RePluginInfoSrvAdapter;
import net.northking.atp.db.dao.RePluginInfoDao;
import net.northking.atp.db.persistent.RePluginInfo;
import net.northking.atp.db.service.RePluginInfoService;
import net.northking.db.BasicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 插件信息表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-04-17 11:18:53  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class RePluginInfoServiceImpl extends RePluginInfoSrvAdapter implements RePluginInfoService
{
private static final Logger logger = LoggerFactory.getLogger(RePluginInfoServiceImpl.class);

  @Autowired
  private RePluginInfoDao rePluginInfoDao;

  protected BasicDao<RePluginInfo> getDao()
  {
  return rePluginInfoDao;
  }
// ----      The End by Generator     ----//




}

