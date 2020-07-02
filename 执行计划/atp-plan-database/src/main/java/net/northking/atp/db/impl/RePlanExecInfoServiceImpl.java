/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.RePlanExecInfoSrvAdapter;
import net.northking.atp.db.dao.RePlanExecInfoDao;
import net.northking.atp.db.persistent.RePlanExecInfo;
import net.northking.atp.db.service.RePlanExecInfoService;
import net.northking.db.BasicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
   * 计划执行记录表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-04-26 15:26:29  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class RePlanExecInfoServiceImpl extends RePlanExecInfoSrvAdapter implements RePlanExecInfoService
{
private static final Logger logger = LoggerFactory.getLogger(RePlanExecInfoServiceImpl.class);

  @Autowired
  private RePlanExecInfoDao rePlanExecInfoDao;

  protected BasicDao<RePlanExecInfo> getDao()
  {
  return rePlanExecInfoDao;
  }
// ----      The End by Generator     ----//
  /**
   * 获取最近一条执行记录
   * @param planId
   * @return
   */
  @Override
  public RePlanExecInfo queryFirstById(String planId) {
    return rePlanExecInfoDao.queryFirstById(planId);
  }

    @Override
    public long batchDeleteByPlanIds(List<String> planIds) {
      if (planIds != null && planIds.size() > 0) {
          return rePlanExecInfoDao.batchDeleteByPlanIds(planIds);
      }
      return 0;
    }

  @Override
  public List<String> queryByPlanIds(List<String> planIds) {
    if (planIds != null && planIds.size() > 0) {
      return rePlanExecInfoDao.queryByPlanIds(planIds);
    }
    return new ArrayList<String>();
  }


}

