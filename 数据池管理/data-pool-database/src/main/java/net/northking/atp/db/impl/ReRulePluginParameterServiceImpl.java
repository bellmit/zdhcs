/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReRulePluginParameterSrvAdapter;
import net.northking.atp.db.dao.ReRulePluginParameterDao;
import net.northking.atp.db.service.ReRulePluginParameterService;
import net.northking.atp.db.persistent.ReRulePluginParameter;

import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
   * 规则插件参数
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-07-15 20:45:49  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReRulePluginParameterServiceImpl extends ReRulePluginParameterSrvAdapter implements ReRulePluginParameterService
{
private static final Logger logger = LoggerFactory.getLogger(ReRulePluginParameterServiceImpl.class);

  @Autowired
  private ReRulePluginParameterDao reRulePluginParameterDao;

  protected BasicDao<ReRulePluginParameter> getDao()
  {
  return reRulePluginParameterDao;
  }

// ----      The End by Generator     ----//

  /**
   * 根据插件Id查询对应参数
   * @param query
   * @return
   */
  @Override
  public List<ReRulePluginParameter> queryParamForRuleList(Map<String, Object> query) {
    return reRulePluginParameterDao.queryParamForRuleList(query);
  }


}

