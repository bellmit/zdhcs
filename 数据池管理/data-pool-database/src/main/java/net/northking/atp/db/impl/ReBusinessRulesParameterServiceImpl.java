/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReBusinessRulesParameterSrvAdapter;
import net.northking.atp.db.dao.ReBusinessRulesParameterDao;
import net.northking.atp.db.service.ReBusinessRulesParameterService;
import net.northking.atp.db.persistent.ReBusinessRulesParameter;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 业务规则参数
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-17 18:02:31  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReBusinessRulesParameterServiceImpl extends ReBusinessRulesParameterSrvAdapter implements ReBusinessRulesParameterService
{
private static final Logger logger = LoggerFactory.getLogger(ReBusinessRulesParameterServiceImpl.class);

  @Autowired
  private ReBusinessRulesParameterDao reBusinessRulesParameterDao;

  protected BasicDao<ReBusinessRulesParameter> getDao()
  {
  return reBusinessRulesParameterDao;
  }
// ----      The End by Generator     ----//




}

