/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.ctrl;

import net.northking.atp.db.persistent.ReWebhookTriggerLog;
import net.northking.atp.db.service.ReWebhookTriggerLogService;

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
* webhook触发日志表 API
*
* <p>文件内容由代码生成器产生，请不要手动修改！ <br>
  * createdate:  2019-08-08 19:11:46  <br>
  * @author:  database-mybatis-maven-plugin  <br>
  * @since:  1.0 <br>
*/
@Api(tags = {"底层数据服务--webhook触发日志表"}, description = "由代码生成器自动生成")
@RestController
@RequestMapping(value = "/genCode")
public class ReWebhookTriggerLogController
{

  /**
   * webhook触发日志表 数据库持久层服务
   */
  @Autowired
  private ReWebhookTriggerLogService service;


  /**
   * 新增 webhook触发日志表
   *
   * @param target webhook触发日志表
   * @return 接口返回
   */
  @ApiOperation(value = "新增 webhook触发日志表", notes = "新增 webhook触发日志表")
  @RequestMapping(value = "/ReWebhookTriggerLog/insert", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReWebhookTriggerLog> insert(@RequestBody ReWebhookTriggerLog target)
  {
  target.setId(UUID.randomUUID().toString().replace("-", ""));
    service.insert(target);
    return new ResultWrapper<ReWebhookTriggerLog>().success(target);
  }

  /**
   * 新增批量 webhook触发日志表
   *
   * @param list 批量webhook触发日志表
   * @return 接口返回
   */
  @ApiOperation(value = "批量新增 webhook触发日志表", notes = "批量新增 webhook触发日志表")
  @RequestMapping(value = "/ReWebhookTriggerLog/insertBatch", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper insertBatch(@RequestBody List<ReWebhookTriggerLog> list)
  {
    for (ReWebhookTriggerLog target : list)
    {
      target.setId(UUID.randomUUID().toString().replace("-", ""));
    }
    service.insertByBatch(list);
    return new ResultWrapper().success();
  }

  /**
   * 根据主键修改 webhook触发日志表
   *
   * @param target webhook触发日志表
   * @return 接口返回
   */
  @ApiOperation(value = "修改 webhook触发日志表", notes = "根据主键修改 webhook触发日志表")
  @RequestMapping(value = "/ReWebhookTriggerLog/updateByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReWebhookTriggerLog> updateByPrimaryKey(@RequestBody ReWebhookTriggerLog target)
  {
    service.updateByPrimaryKey(target);
    ReWebhookTriggerLog newOne = service.findByPrimaryKey(target);
    return new ResultWrapper<ReWebhookTriggerLog>().success(newOne);
  }

  /**
   * 根据主键删除 webhook触发日志表
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "删除 webhook触发日志表", notes = "根据主键删除 webhook触发日志表",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReWebhookTriggerLog/deleteByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKey(@RequestParam("id") String id)
  {
    service.deleteByPrimaryKey(id);
    return new ResultWrapper().success();
  }

  /**
  * 根据主键批量删除 webhook触发日志表
  *
  * @param ids 主键数组
  * @return 接口返回
  */
  @ApiOperation(value = "批量删除 webhook触发日志表", notes = "根据主键批量删除 webhook触发日志表",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReWebhookTriggerLog/deleteByPrimaryKeys", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKeys(@RequestParam("ids") String[] ids)
  {
    service.deleteByPrimaryKey(ids);
    return new ResultWrapper().success();
  }

  /**
   * 查询全部 webhook触发日志表
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询全部 webhook触发日志表", notes = "查询全部 webhook触发日志表")
  @RequestMapping(value = "/ReWebhookTriggerLog/query", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<List<ReWebhookTriggerLog>> query(@RequestBody ReWebhookTriggerLog target)
  {
    return new ResultWrapper<List<ReWebhookTriggerLog>>().success(service.query(target));
  }

  /**
   * 分页查询 webhook触发日志表
   * @param queryByPage 分页查询对象
   * @return 接口返回
   */
  @ApiOperation(value = "分页查询 webhook触发日志表", notes = "分页查询 webhook触发日志表")
  @RequestMapping(value = "/ReWebhookTriggerLog/queryByPage", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Pagination<ReWebhookTriggerLog>> queryByPage(@RequestBody QueryByPage<ReWebhookTriggerLog> queryByPage)
  {
    OrderBy orderBy = new SqlOrderBy();
    for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
    {
      orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
    }
    Pagination<ReWebhookTriggerLog> result = service.query(queryByPage.getQuery(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
    return new ResultWrapper<Pagination<ReWebhookTriggerLog>>().success(result);
  }

  /**
   * 查询记录数 webhook触发日志表
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询记录数 webhook触发日志表", notes = "查询记录数 webhook触发日志表")
  @RequestMapping(value = "/ReWebhookTriggerLog/queryCount", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Long> queryCount(@RequestBody ReWebhookTriggerLog target)
  {
    return new ResultWrapper<Long>().success(service.queryCount(target));
  }

  /**
   * 根据主键查询 webhook触发日志表
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "查询 webhook触发日志表", notes = "根据主键查询 webhook触发日志表",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReWebhookTriggerLog/findByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReWebhookTriggerLog> findByPrimaryKey(@RequestParam("id") String id)
  {
    ResultWrapper<ReWebhookTriggerLog> resultWrapper = new ResultWrapper<ReWebhookTriggerLog>();
    ReWebhookTriggerLog record = service.findByPrimaryKey(id);
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

