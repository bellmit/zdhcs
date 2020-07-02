/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReProfileInfoSrvAdapter;
import net.northking.atp.db.dao.ReProfileInfoDao;
import net.northking.atp.db.service.ReProfileInfoService;
import net.northking.atp.db.persistent.ReProfileInfo;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 描述环境的基础信息
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-05-10 10:20:33  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReProfileInfoServiceImpl extends ReProfileInfoSrvAdapter implements ReProfileInfoService
{
private static final Logger logger = LoggerFactory.getLogger(ReProfileInfoServiceImpl.class);

  @Autowired
  private ReProfileInfoDao reProfileInfoDao;

  protected BasicDao<ReProfileInfo> getDao()
  {
  return reProfileInfoDao;
  }
// ----      The End by Generator     ----//




}

