/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReWebhookInfoSrvAdapter;
import net.northking.atp.db.dao.ReWebhookInfoDao;
import net.northking.atp.db.service.ReWebhookInfoService;
import net.northking.atp.db.persistent.ReWebhookInfo;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * webhook触发信息表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-02 18:58:32  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReWebhookInfoServiceImpl extends ReWebhookInfoSrvAdapter implements ReWebhookInfoService
{
private static final Logger logger = LoggerFactory.getLogger(ReWebhookInfoServiceImpl.class);

  @Autowired
  private ReWebhookInfoDao reWebhookInfoDao;

  protected BasicDao<ReWebhookInfo> getDao()
  {
  return reWebhookInfoDao;
  }
// ----      The End by Generator     ----//




}

