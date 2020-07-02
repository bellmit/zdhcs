/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import net.northking.atp.db.adapter.ReBusinessRulesSrvAdapter;
import net.northking.atp.db.dao.ReBusinessRulesDao;
import net.northking.atp.db.service.ReBusinessRulesService;
import net.northking.atp.db.persistent.ReBusinessRules;

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
   * 业务规则信息_基础信息
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-16 09:25:01  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReBusinessRulesServiceImpl extends ReBusinessRulesSrvAdapter implements ReBusinessRulesService
{
private static final Logger logger = LoggerFactory.getLogger(ReBusinessRulesServiceImpl.class);

  @Autowired
  private ReBusinessRulesDao reBusinessRulesDao;

  protected BasicDao<ReBusinessRules> getDao()
  {
  return reBusinessRulesDao;
  }
// ----      The End by Generator     ----//

  /**
   * 列表分页查询
   * @param info
   * @param orderBy
   * @param pageNo
   * @param pageSize
   * @return
   */
  @Override
  public Pagination selectBusinessRules(ReBusinessRules info, OrderBy orderBy, int pageNo, int pageSize) {
    if(pageNo <= 0) {
      pageNo = 1;
    }

    if(pageSize <= 0 || pageSize > 1000) {
      pageSize = 1000;
    }

    DefaultPagination pagination = new DefaultPagination();
    pagination.setPageSize(pageSize);
    pagination.setPageNo(pageNo);
    if(orderBy != null && orderBy.size() > 0) {
      PageHelper.orderBy(orderBy.toString());
    }

    Page page = PageHelper.startPage(pageNo, pageSize);
    List result = reBusinessRulesDao.queryForLike(info);
    pagination.setRecords(result);
    pagination.setRecordCount(page.getTotal());
    return pagination;
  }



}

