/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.HisRunEnvInfoSrvAdapter;
import net.northking.atp.db.dao.HisRunEnvInfoDao;
import net.northking.atp.db.service.HisRunEnvInfoService;
import net.northking.atp.db.persistent.HisRunEnvInfo;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 测试环境执行历史表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-08-08 19:11:46  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class HisRunEnvInfoServiceImpl extends HisRunEnvInfoSrvAdapter implements HisRunEnvInfoService
{
private static final Logger logger = LoggerFactory.getLogger(HisRunEnvInfoServiceImpl.class);

  @Autowired
  private HisRunEnvInfoDao hisRunEnvInfoDao;

  protected BasicDao<HisRunEnvInfo> getDao()
  {
  return hisRunEnvInfoDao;
  }
// ----      The End by Generator     ----//




}

