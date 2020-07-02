/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReDataPoolSrvAdapter;
import net.northking.atp.db.dao.ReDataPoolDao;
import net.northking.atp.db.service.ReDataPoolService;
import net.northking.atp.db.persistent.ReDataPool;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 数据池
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-18 20:11:30  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReDataPoolServiceImpl extends ReDataPoolSrvAdapter implements ReDataPoolService
{
private static final Logger logger = LoggerFactory.getLogger(ReDataPoolServiceImpl.class);

  @Autowired
  private ReDataPoolDao reDataPoolDao;

  protected BasicDao<ReDataPool> getDao()
  {
  return reDataPoolDao;
  }
// ----      The End by Generator     ----//




}

