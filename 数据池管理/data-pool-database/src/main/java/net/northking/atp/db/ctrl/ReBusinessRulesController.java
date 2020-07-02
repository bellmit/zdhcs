/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.ctrl;

import net.northking.atp.db.persistent.ReBusinessRules;
import net.northking.atp.db.service.ReBusinessRulesService;

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
* 业务规则信息_基础信息 API
*
* <p>文件内容由代码生成器产生，请不要手动修改！ <br>
  * createdate:  2019-07-16 09:25:02  <br>
  * @author:  database-mybatis-maven-plugin  <br>
  * @since:  1.0 <br>
*/
@Api(tags = {"底层数据服务--业务规则信息_基础信息"}, description = "由代码生成器自动生成")
@RestController
@RequestMapping(value = "/genCode")
public class ReBusinessRulesController
{

  /**
   * 业务规则信息_基础信息 数据库持久层服务
   */
  @Autowired
  private ReBusinessRulesService service;


  /**
   * 新增 业务规则信息_基础信息
   *
   * @param target 业务规则信息_基础信息
   * @return 接口返回
   */
  @ApiOperation(value = "新增 业务规则信息_基础信息", notes = "新增 业务规则信息_基础信息")
  @RequestMapping(value = "/ReBusinessRules/insert", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReBusinessRules> insert(@RequestBody ReBusinessRules target)
  {
  target.setId(UUID.randomUUID().toString().replace("-", ""));
    service.insert(target);
    return new ResultWrapper<ReBusinessRules>().success(target);
  }

  /**
   * 新增批量 业务规则信息_基础信息
   *
   * @param list 批量业务规则信息_基础信息
   * @return 接口返回
   */
  @ApiOperation(value = "批量新增 业务规则信息_基础信息", notes = "批量新增 业务规则信息_基础信息")
  @RequestMapping(value = "/ReBusinessRules/insertBatch", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper insertBatch(@RequestBody List<ReBusinessRules> list)
  {
    for (ReBusinessRules target : list)
    {
      target.setId(UUID.randomUUID().toString().replace("-", ""));
    }
    service.insertByBatch(list);
    return new ResultWrapper().success();
  }

  /**
   * 根据主键修改 业务规则信息_基础信息
   *
   * @param target 业务规则信息_基础信息
   * @return 接口返回
   */
  @ApiOperation(value = "修改 业务规则信息_基础信息", notes = "根据主键修改 业务规则信息_基础信息")
  @RequestMapping(value = "/ReBusinessRules/updateByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReBusinessRules> updateByPrimaryKey(@RequestBody ReBusinessRules target)
  {
    service.updateByPrimaryKey(target);
    ReBusinessRules newOne = service.findByPrimaryKey(target);
    return new ResultWrapper<ReBusinessRules>().success(newOne);
  }

  /**
   * 根据主键删除 业务规则信息_基础信息
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "删除 业务规则信息_基础信息", notes = "根据主键删除 业务规则信息_基础信息",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReBusinessRules/deleteByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKey(@RequestParam("id") String id)
  {
    service.deleteByPrimaryKey(id);
    return new ResultWrapper().success();
  }

  /**
  * 根据主键批量删除 业务规则信息_基础信息
  *
  * @param ids 主键数组
  * @return 接口返回
  */
  @ApiOperation(value = "批量删除 业务规则信息_基础信息", notes = "根据主键批量删除 业务规则信息_基础信息",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReBusinessRules/deleteByPrimaryKeys", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKeys(@RequestParam("ids") String[] ids)
  {
    service.deleteByPrimaryKey(ids);
    return new ResultWrapper().success();
  }

  /**
   * 查询全部 业务规则信息_基础信息
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询全部 业务规则信息_基础信息", notes = "查询全部 业务规则信息_基础信息")
  @RequestMapping(value = "/ReBusinessRules/query", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<List<ReBusinessRules>> query(@RequestBody ReBusinessRules target)
  {
    return new ResultWrapper<List<ReBusinessRules>>().success(service.query(target));
  }

  /**
   * 分页查询 业务规则信息_基础信息
   * @param queryByPage 分页查询对象
   * @return 接口返回
   */
  @ApiOperation(value = "分页查询 业务规则信息_基础信息", notes = "分页查询 业务规则信息_基础信息")
  @RequestMapping(value = "/ReBusinessRules/queryByPage", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Pagination<ReBusinessRules>> queryByPage(@RequestBody QueryByPage<ReBusinessRules> queryByPage)
  {
    OrderBy orderBy = new SqlOrderBy();
    for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
    {
      orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
    }
    Pagination<ReBusinessRules> result = service.query(queryByPage.getQuery(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
    return new ResultWrapper<Pagination<ReBusinessRules>>().success(result);
  }

  /**
   * 查询记录数 业务规则信息_基础信息
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询记录数 业务规则信息_基础信息", notes = "查询记录数 业务规则信息_基础信息")
  @RequestMapping(value = "/ReBusinessRules/queryCount", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Long> queryCount(@RequestBody ReBusinessRules target)
  {
    return new ResultWrapper<Long>().success(service.queryCount(target));
  }

  /**
   * 根据主键查询 业务规则信息_基础信息
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "查询 业务规则信息_基础信息", notes = "根据主键查询 业务规则信息_基础信息",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReBusinessRules/findByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReBusinessRules> findByPrimaryKey(@RequestParam("id") String id)
  {
    ResultWrapper<ReBusinessRules> resultWrapper = new ResultWrapper<ReBusinessRules>();
    ReBusinessRules record = service.findByPrimaryKey(id);
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

