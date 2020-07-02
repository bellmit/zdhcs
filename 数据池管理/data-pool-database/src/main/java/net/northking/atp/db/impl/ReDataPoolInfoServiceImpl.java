/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReDataPoolInfoSrvAdapter;
import net.northking.atp.db.dao.ReDataPoolInfoDao;
import net.northking.atp.db.service.ReDataPoolInfoService;
import net.northking.atp.db.persistent.ReDataPoolInfo;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
   * 
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-05-07 17:10:00  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReDataPoolInfoServiceImpl extends ReDataPoolInfoSrvAdapter implements ReDataPoolInfoService
{
private static final Logger logger = LoggerFactory.getLogger(ReDataPoolInfoServiceImpl.class);

  @Autowired
  private ReDataPoolInfoDao reDataPoolInfoDao;

  protected BasicDao<ReDataPoolInfo> getDao()
  {
  return reDataPoolInfoDao;
  }
// ----      The End by Generator     ----//



  @Override
  public String queryMaxName(ReDataPoolInfo nameQuery) {
    return reDataPoolInfoDao.queryMaxName(nameQuery);
  }

  @Override
  public List<ReDataPoolInfo> queryIdForInsert(ReDataPoolInfo query) {
    return reDataPoolInfoDao.queryIdForInsert(query);
  }

}

