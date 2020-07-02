/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.impl;


import net.northking.atp.db.dao.ReProfileInfoDao;
import net.northking.atp.db.persistent.ReProfileInfo;

import net.northking.atp.service.ReProfileInfoServiceEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
   * 
 *
 * createdate:  2019-05-07 17:10:00  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReProfileInfoServiceExImpl implements ReProfileInfoServiceEx
{
private static final Logger logger = LoggerFactory.getLogger(ReProfileInfoServiceExImpl.class);


  @Autowired
  private ReProfileInfoDao reProfileInfoDao;
// ----      The End by Generator     ----//

  /**
   * 查询非本ID对应数据除外的数据
   * @param var1
   * @return
   */
  public List<ReProfileInfo> queryForUpdate(ReProfileInfo var1) {
    return reProfileInfoDao.queryForUpdate(var1);
  }


}

