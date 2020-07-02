/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReScheduleTaskSrvAdapter;
import net.northking.atp.db.dao.ReScheduleTaskDao;
import net.northking.atp.db.service.ReScheduleTaskService;
import net.northking.atp.db.persistent.ReScheduleTask;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
   * 定时触发任务清单表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-05 16:48:29  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReScheduleTaskServiceImpl extends ReScheduleTaskSrvAdapter implements ReScheduleTaskService
{
private static final Logger logger = LoggerFactory.getLogger(ReScheduleTaskServiceImpl.class);

  @Autowired
  private ReScheduleTaskDao reScheduleTaskDao;

  protected BasicDao<ReScheduleTask> getDao()
  {
  return reScheduleTaskDao;
  }

// ----      The End by Generator     ----//

  @Override
  public List<ReScheduleTask> queryNotCreateTask(ReScheduleTask task) {
    return reScheduleTaskDao.queryNotCreateTask(task);
  }



}

