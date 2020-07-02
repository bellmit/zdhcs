/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReScheduleTaskLogSrvAdapter;
import net.northking.atp.db.dao.ReScheduleTaskLogDao;
import net.northking.atp.db.service.ReScheduleTaskLogService;
import net.northking.atp.db.persistent.ReScheduleTaskLog;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
   * 定时触发任务日志表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-11 20:40:30  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReScheduleTaskLogServiceImpl extends ReScheduleTaskLogSrvAdapter implements ReScheduleTaskLogService
{
private static final Logger logger = LoggerFactory.getLogger(ReScheduleTaskLogServiceImpl.class);

  @Autowired
  private ReScheduleTaskLogDao reScheduleTaskLogDao;

  protected BasicDao<ReScheduleTaskLog> getDao()
  {
  return reScheduleTaskLogDao;
  }
// ----      The End by Generator     ----//




}

