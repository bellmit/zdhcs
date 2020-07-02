/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReOsInfoSrvAdapter;
import net.northking.atp.db.dao.ReOsInfoDao;
import net.northking.atp.db.service.ReOsInfoService;
import net.northking.atp.db.persistent.ReOsInfo;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 操作系统信息表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-08-05 16:56:26  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReOsInfoServiceImpl extends ReOsInfoSrvAdapter implements ReOsInfoService
{
private static final Logger logger = LoggerFactory.getLogger(ReOsInfoServiceImpl.class);

  @Autowired
  private ReOsInfoDao reOsInfoDao;

  protected BasicDao<ReOsInfo> getDao()
  {
  return reOsInfoDao;
  }
// ----      The End by Generator     ----//




}

