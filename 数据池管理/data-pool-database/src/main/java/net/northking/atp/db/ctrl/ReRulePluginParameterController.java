/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.ctrl;

import net.northking.atp.db.persistent.ReRulePluginParameter;
import net.northking.atp.db.service.ReRulePluginParameterService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
* 规则插件参数 API
*
* <p>文件内容由代码生成器产生，请不要手动修改！ <br>
  * createdate:  2019-07-15 20:45:49  <br>
  * @author:  database-mybatis-maven-plugin  <br>
  * @since:  1.0 <br>
*/
@Api(tags = {"底层数据服务--规则插件参数"}, description = "由代码生成器自动生成")
@RestController
@RequestMapping(value = "/genCode")
public class ReRulePluginParameterController
{

  /**
   * 规则插件参数 数据库持久层服务
   */
  @Autowired
  private ReRulePluginParameterService service;


  /**
   * 新增 规则插件参数
   *
   * @param target 规则插件参数
   * @return 接口返回
   */
  @ApiOperation(value = "新增 规则插件参数", notes = "新增 规则插件参数")
  @RequestMapping(value = "/ReRulePluginParameter/insert", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReRulePluginParameter> insert(@RequestBody ReRulePluginParameter target)
  {
  target.setId(UUID.randomUUID().toString().replace("-", ""));
    service.insert(target);
    return new ResultWrapper<ReRulePluginParameter>().success(target);
  }

  /**
   * 新增批量 规则插件参数
   *
   * @param list 批量规则插件参数
   * @return 接口返回
   */
  @ApiOperation(value = "批量新增 规则插件参数", notes = "批量新增 规则插件参数")
  @RequestMapping(value = "/ReRulePluginParameter/insertBatch", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper insertBatch(@RequestBody List<ReRulePluginParameter> list)
  {
    for (ReRulePluginParameter target : list)
    {
      target.setId(UUID.randomUUID().toString().replace("-", ""));
    }
    service.insertByBatch(list);
    return new ResultWrapper().success();
  }

  /**
   * 根据主键修改 规则插件参数
   *
   * @param target 规则插件参数
   * @return 接口返回
   */
  @ApiOperation(value = "修改 规则插件参数", notes = "根据主键修改 规则插件参数")
  @RequestMapping(value = "/ReRulePluginParameter/updateByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReRulePluginParameter> updateByPrimaryKey(@RequestBody ReRulePluginParameter target)
  {
    service.updateByPrimaryKey(target);
    ReRulePluginParameter newOne = service.findByPrimaryKey(target);
    return new ResultWrapper<ReRulePluginParameter>().success(newOne);
  }

  /**
   * 根据主键删除 规则插件参数
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "删除 规则插件参数", notes = "根据主键删除 规则插件参数",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReRulePluginParameter/deleteByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKey(@RequestParam("id") String id)
  {
    service.deleteByPrimaryKey(id);
    return new ResultWrapper().success();
  }

  /**
  * 根据主键批量删除 规则插件参数
  *
  * @param ids 主键数组
  * @return 接口返回
  */
  @ApiOperation(value = "批量删除 规则插件参数", notes = "根据主键批量删除 规则插件参数",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReRulePluginParameter/deleteByPrimaryKeys", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKeys(@RequestParam("ids") String[] ids)
  {
    service.deleteByPrimaryKey(ids);
    return new ResultWrapper().success();
  }

  /**
   * 查询全部 规则插件参数
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询全部 规则插件参数", notes = "查询全部 规则插件参数")
  @RequestMapping(value = "/ReRulePluginParameter/query", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<List<ReRulePluginParameter>> query(@RequestBody ReRulePluginParameter target)
  {
    return new ResultWrapper<List<ReRulePluginParameter>>().success(service.query(target));
  }

  /**
   * 分页查询 规则插件参数
   * @param queryByPage 分页查询对象
   * @return 接口返回
   */
  @ApiOperation(value = "分页查询 规则插件参数", notes = "分页查询 规则插件参数")
  @RequestMapping(value = "/ReRulePluginParameter/queryByPage", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Pagination<ReRulePluginParameter>> queryByPage(@RequestBody QueryByPage<ReRulePluginParameter> queryByPage)
  {
    OrderBy orderBy = new SqlOrderBy();
    for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
    {
      orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
    }
    Pagination<ReRulePluginParameter> result = service.query(queryByPage.getQuery(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
    return new ResultWrapper<Pagination<ReRulePluginParameter>>().success(result);
  }

  /**
   * 查询记录数 规则插件参数
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询记录数 规则插件参数", notes = "查询记录数 规则插件参数")
  @RequestMapping(value = "/ReRulePluginParameter/queryCount", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Long> queryCount(@RequestBody ReRulePluginParameter target)
  {
    return new ResultWrapper<Long>().success(service.queryCount(target));
  }

  /**
   * 根据主键查询 规则插件参数
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "查询 规则插件参数", notes = "根据主键查询 规则插件参数",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReRulePluginParameter/findByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReRulePluginParameter> findByPrimaryKey(@RequestParam("id") String id)
  {
    ResultWrapper<ReRulePluginParameter> resultWrapper = new ResultWrapper<ReRulePluginParameter>();
    ReRulePluginParameter record = service.findByPrimaryKey(id);
    if (record == null)
    {
      resultWrapper.fail("0000001", "不存在[主键=" + id + "]的记录！");
    } else
    {
      resultWrapper.success(service.findByPrimaryKey(id));
    }
    return resultWrapper;
  }


}

