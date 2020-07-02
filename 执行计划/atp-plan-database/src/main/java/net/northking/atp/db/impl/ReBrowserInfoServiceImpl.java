/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReBrowserInfoSrvAdapter;
import net.northking.atp.db.dao.ReBrowserInfoDao;
import net.northking.atp.db.service.ReBrowserInfoService;
import net.northking.atp.db.persistent.ReBrowserInfo;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 浏览器信息表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-08-05 17:11:32  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReBrowserInfoServiceImpl extends ReBrowserInfoSrvAdapter implements ReBrowserInfoService
{
private static final Logger logger = LoggerFactory.getLogger(ReBrowserInfoServiceImpl.class);

  @Autowired
  private ReBrowserInfoDao reBrowserInfoDao;

  protected BasicDao<ReBrowserInfo> getDao()
  {
  return reBrowserInfoDao;
  }
// ----      The End by Generator     ----//




}

