/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.HisExecPlanCaseSetRelSrvAdapter;
import net.northking.atp.db.dao.HisExecPlanCaseSetRelDao;
import net.northking.atp.db.persistent.HisExecPlanCaseSetRel;
import net.northking.atp.db.service.HisExecPlanCaseSetRelService;
import net.northking.db.BasicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
   * 测试计划-用例集关系历史表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-05-05 17:22:21  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class HisExecPlanCaseSetRelServiceImpl extends HisExecPlanCaseSetRelSrvAdapter implements HisExecPlanCaseSetRelService
{
private static final Logger logger = LoggerFactory.getLogger(HisExecPlanCaseSetRelServiceImpl.class);

  @Autowired
  private HisExecPlanCaseSetRelDao hisExecPlanCaseSetRelDao;

  protected BasicDao<HisExecPlanCaseSetRel> getDao()
  {
  return hisExecPlanCaseSetRelDao;
  }

  /**
   * // 根据计划集合批量删除历史用例集
   *
   * @param planIds
   * @return
   */
  @Override
  public long batchDeleteByPlanIds(List<String> planIds) {
    if (planIds != null && planIds.size() > 0) {
      long deleteCount = hisExecPlanCaseSetRelDao.batchDeleteByPlanIds(planIds);
      return deleteCount;
    }
    return 0;
  }
// ----      The End by Generator     ----//




}

