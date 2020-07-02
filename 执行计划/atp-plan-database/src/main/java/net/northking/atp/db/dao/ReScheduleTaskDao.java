/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.dao;

import net.northking.atp.db.persistent.ReScheduleTask;
import net.northking.atp.db.mapper.ReScheduleTaskMapper;

import java.util.List;


/**
 * 定时触发任务清单表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-05 16:48:29  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
public interface ReScheduleTaskDao extends ReScheduleTaskMapper
{
    // ----      The End by Generator     ----//
    List<ReScheduleTask> queryNotCreateTask(ReScheduleTask task);
}
