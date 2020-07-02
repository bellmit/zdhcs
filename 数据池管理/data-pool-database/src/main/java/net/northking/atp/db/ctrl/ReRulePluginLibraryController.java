/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.ctrl;

import net.northking.atp.db.persistent.ReRulePluginLibrary;
import net.northking.atp.db.service.ReRulePluginLibraryService;

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
* 数据规则插件库 API
*
* <p>文件内容由代码生成器产生，请不要手动修改！ <br>
  * createdate:  2019-07-15 20:45:49  <br>
  * @author:  database-mybatis-maven-plugin  <br>
  * @since:  1.0 <br>
*/
@Api(tags = {"底层数据服务--数据规则插件库"}, description = "由代码生成器自动生成")
@RestController
@RequestMapping(value = "/genCode")
public class ReRulePluginLibraryController
{

  /**
   * 数据规则插件库 数据库持久层服务
   */
  @Autowired
  private ReRulePluginLibraryService service;


  /**
   * 新增 数据规则插件库
   *
   * @param target 数据规则插件库
   * @return 接口返回
   */
  @ApiOperation(value = "新增 数据规则插件库", notes = "新增 数据规则插件库")
  @RequestMapping(value = "/ReRulePluginLibrary/insert", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReRulePluginLibrary> insert(@RequestBody ReRulePluginLibrary target)
  {
  target.setId(UUID.randomUUID().toString().replace("-", ""));
    service.insert(target);
    return new ResultWrapper<ReRulePluginLibrary>().success(target);
  }

  /**
   * 新增批量 数据规则插件库
   *
   * @param list 批量数据规则插件库
   * @return 接口返回
   */
  @ApiOperation(value = "批量新增 数据规则插件库", notes = "批量新增 数据规则插件库")
  @RequestMapping(value = "/ReRulePluginLibrary/insertBatch", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper insertBatch(@RequestBody List<ReRulePluginLibrary> list)
  {
    for (ReRulePluginLibrary target : list)
    {
      target.setId(UUID.randomUUID().toString().replace("-", ""));
    }
    service.insertByBatch(list);
    return new ResultWrapper().success();
  }

  /**
   * 根据主键修改 数据规则插件库
   *
   * @param target 数据规则插件库
   * @return 接口返回
   */
  @ApiOperation(value = "修改 数据规则插件库", notes = "根据主键修改 数据规则插件库")
  @RequestMapping(value = "/ReRulePluginLibrary/updateByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReRulePluginLibrary> updateByPrimaryKey(@RequestBody ReRulePluginLibrary target)
  {
    service.updateByPrimaryKey(target);
    ReRulePluginLibrary newOne = service.findByPrimaryKey(target);
    return new ResultWrapper<ReRulePluginLibrary>().success(newOne);
  }

  /**
   * 根据主键删除 数据规则插件库
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "删除 数据规则插件库", notes = "根据主键删除 数据规则插件库",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReRulePluginLibrary/deleteByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKey(@RequestParam("id") String id)
  {
    service.deleteByPrimaryKey(id);
    return new ResultWrapper().success();
  }

  /**
  * 根据主键批量删除 数据规则插件库
  *
  * @param ids 主键数组
  * @return 接口返回
  */
  @ApiOperation(value = "批量删除 数据规则插件库", notes = "根据主键批量删除 数据规则插件库",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReRulePluginLibrary/deleteByPrimaryKeys", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKeys(@RequestParam("ids") String[] ids)
  {
    service.deleteByPrimaryKey(ids);
    return new ResultWrapper().success();
  }

  /**
   * 查询全部 数据规则插件库
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询全部 数据规则插件库", notes = "查询全部 数据规则插件库")
  @RequestMapping(value = "/ReRulePluginLibrary/query", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<List<ReRulePluginLibrary>> query(@RequestBody ReRulePluginLibrary target)
  {
    return new ResultWrapper<List<ReRulePluginLibrary>>().success(service.query(target));
  }

  /**
   * 分页查询 数据规则插件库
   * @param queryByPage 分页查询对象
   * @return 接口返回
   */
  @ApiOperation(value = "分页查询 数据规则插件库", notes = "分页查询 数据规则插件库")
  @RequestMapping(value = "/ReRulePluginLibrary/queryByPage", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Pagination<ReRulePluginLibrary>> queryByPage(@RequestBody QueryByPage<ReRulePluginLibrary> queryByPage)
  {
    OrderBy orderBy = new SqlOrderBy();
    for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
    {
      orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
    }
    Pagination<ReRulePluginLibrary> result = service.query(queryByPage.getQuery(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
    return new ResultWrapper<Pagination<ReRulePluginLibrary>>().success(result);
  }

  /**
   * 查询记录数 数据规则插件库
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询记录数 数据规则插件库", notes = "查询记录数 数据规则插件库")
  @RequestMapping(value = "/ReRulePluginLibrary/queryCount", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Long> queryCount(@RequestBody ReRulePluginLibrary target)
  {
    return new ResultWrapper<Long>().success(service.queryCount(target));
  }

  /**
   * 根据主键查询 数据规则插件库
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "查询 数据规则插件库", notes = "根据主键查询 数据规则插件库",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReRulePluginLibrary/findByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReRulePluginLibrary> findByPrimaryKey(@RequestParam("id") String id)
  {
    ResultWrapper<ReRulePluginLibrary> resultWrapper = new ResultWrapper<ReRulePluginLibrary>();
    ReRulePluginLibrary record = service.findByPrimaryKey(id);
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

