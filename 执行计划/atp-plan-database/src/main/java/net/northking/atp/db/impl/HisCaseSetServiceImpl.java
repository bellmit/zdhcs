/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.HisCaseSetSrvAdapter;
import net.northking.atp.db.dao.HisCaseSetDao;
import net.northking.atp.db.persistent.HisCaseSet;
import net.northking.atp.db.service.HisCaseSetService;
import net.northking.db.BasicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 测试案例集历史表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-05-05 17:22:21  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class HisCaseSetServiceImpl extends HisCaseSetSrvAdapter implements HisCaseSetService
{
private static final Logger logger = LoggerFactory.getLogger(HisCaseSetServiceImpl.class);

  @Autowired
  private HisCaseSetDao hisCaseSetDao;

  protected BasicDao<HisCaseSet> getDao()
  {
  return hisCaseSetDao;
  }
// ----      The End by Generator     ----//




}

