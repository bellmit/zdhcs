/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import net.northking.atp.db.adapter.ReExecPlanSrvAdapter;
import net.northking.atp.db.dao.ReExecPlanDao;
import net.northking.atp.db.persistent.ReExecPlan;
import net.northking.atp.db.service.ReExecPlanService;
import net.northking.db.BasicDao;
import net.northking.db.DefaultPagination;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
   * 测试执行计划定义表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-04-26 12:05:24  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReExecPlanServiceImpl extends ReExecPlanSrvAdapter implements ReExecPlanService
{
private static final Logger logger = LoggerFactory.getLogger(ReExecPlanServiceImpl.class);

  @Autowired
  private ReExecPlanDao reExecPlanDao;

  protected BasicDao<ReExecPlan> getDao()
  {
  return reExecPlanDao;
  }

// ----      The End by Generator     ----//

  /**
   * 分页模糊查询执行计划列表
   * @param reExecPlan
   * @param orderBy
   * @param pageNo
   * @param pageSize
   * @return
   */
  @Override
  public Pagination selectPlanListByPage(ReExecPlan reExecPlan, OrderBy orderBy, int pageNo, int pageSize) {
    if (pageNo <= 0)
    {
      pageNo = 1;
    }
    if (pageSize <= 0 || pageSize > 1000)
    {
      pageSize = 1000;
    }

    DefaultPagination pagination = new DefaultPagination<>();
    pagination.setPageSize(pageSize);
    pagination.setPageNo(pageNo);

    if (orderBy != null && orderBy.size() > 0)
    {
      PageHelper.orderBy(orderBy.toString());
    }

    Page page = PageHelper.startPage(pageNo, pageSize);
    List result = reExecPlanDao.queryForLike(reExecPlan);
    pagination.setRecords(result);
    pagination.setRecordCount(page.getTotal());
    return pagination;
  }


}

