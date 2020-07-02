/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReDataPoolValueSrvAdapter;
import net.northking.atp.db.dao.ReDataPoolValueDao;
import net.northking.atp.db.service.ReDataPoolValueService;
import net.northking.atp.db.persistent.ReDataPoolValue;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-05-15 11:55:40  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReDataPoolValueServiceImpl extends ReDataPoolValueSrvAdapter implements ReDataPoolValueService
{
private static final Logger logger = LoggerFactory.getLogger(ReDataPoolValueServiceImpl.class);

  @Autowired
  private ReDataPoolValueDao reDataPoolValueDao;

  protected BasicDao<ReDataPoolValue> getDao()
  {
  return reDataPoolValueDao;
  }
// ----      The End by Generator     ----//




}

