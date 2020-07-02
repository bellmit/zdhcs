/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReObjectAttributeSrvAdapter;
import net.northking.atp.db.dao.ReObjectAttributeDao;
import net.northking.atp.db.service.ReObjectAttributeService;
import net.northking.atp.db.persistent.ReObjectAttribute;

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
public class ReObjectAttributeServiceImpl extends ReObjectAttributeSrvAdapter implements ReObjectAttributeService
{
private static final Logger logger = LoggerFactory.getLogger(ReObjectAttributeServiceImpl.class);

  @Autowired
  private ReObjectAttributeDao reObjectAttributeDao;

  protected BasicDao<ReObjectAttribute> getDao()
  {
  return reObjectAttributeDao;
  }
// ----      The End by Generator     ----//




}

