/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.ctrl;

import net.northking.atp.db.persistent.ReObjectAttribute;
import net.northking.atp.db.service.ReObjectAttributeService;

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
*  API
*
* <p>文件内容由代码生成器产生，请不要手动修改！ <br>
  * createdate:  2019-05-23 10:05:48  <br>
  * @author:  database-mybatis-maven-plugin  <br>
  * @since:  1.0 <br>
*/
@Api(tags = {"底层数据服务--"}, description = "由代码生成器自动生成")
@RestController
@RequestMapping(value = "/genCode")
public class ReObjectAttributeController
{

  /**
   *  数据库持久层服务
   */
  @Autowired
  private ReObjectAttributeService service;


  /**
   * 新增 
   *
   * @param target 
   * @return 接口返回
   */
  @ApiOperation(value = "新增 ", notes = "新增 ")
  @RequestMapping(value = "/ReObjectAttribute/insert", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReObjectAttribute> insert(@RequestBody ReObjectAttribute target)
  {
    service.insert(target);
    return new ResultWrapper<ReObjectAttribute>().success(target);
  }

  /**
   * 新增批量 
   *
   * @param list 批量
   * @return 接口返回
   */
  @ApiOperation(value = "批量新增 ", notes = "批量新增 ")
  @RequestMapping(value = "/ReObjectAttribute/insertBatch", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper insertBatch(@RequestBody List<ReObjectAttribute> list)
  {
    service.insertByBatch(list);
    return new ResultWrapper().success();
  }

  /**
   * 根据主键修改 
   *
   * @param target 
   * @return 接口返回
   */
  @ApiOperation(value = "修改 ", notes = "根据主键修改 ")
  @RequestMapping(value = "/ReObjectAttribute/updateByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReObjectAttribute> updateByPrimaryKey(@RequestBody ReObjectAttribute target)
  {
    service.updateByPrimaryKey(target);
    ReObjectAttribute newOne = service.findByPrimaryKey(target);
    return new ResultWrapper<ReObjectAttribute>().success(newOne);
  }

  /**
   * 根据主键删除 
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "删除 ", notes = "根据主键删除 ",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReObjectAttribute/deleteByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKey(@RequestParam("id") String id)
  {
    service.deleteByPrimaryKey(id);
    return new ResultWrapper().success();
  }

  /**
  * 根据主键批量删除 
  *
  * @param ids 主键数组
  * @return 接口返回
  */
  @ApiOperation(value = "批量删除 ", notes = "根据主键批量删除 ",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReObjectAttribute/deleteByPrimaryKeys", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKeys(@RequestParam("ids") String[] ids)
  {
    service.deleteByPrimaryKey(ids);
    return new ResultWrapper().success();
  }

  /**
   * 查询全部 
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询全部 ", notes = "查询全部 ")
  @RequestMapping(value = "/ReObjectAttribute/query", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<List<ReObjectAttribute>> query(@RequestBody ReObjectAttribute target)
  {
    return new ResultWrapper<List<ReObjectAttribute>>().success(service.query(target));
  }

  /**
   * 分页查询 
   * @param queryByPage 分页查询对象
   * @return 接口返回
   */
  @ApiOperation(value = "分页查询 ", notes = "分页查询 ")
  @RequestMapping(value = "/ReObjectAttribute/queryByPage", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Pagination<ReObjectAttribute>> queryByPage(@RequestBody QueryByPage<ReObjectAttribute> queryByPage)
  {
    OrderBy orderBy = new SqlOrderBy();
    for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
    {
      orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
    }
    Pagination<ReObjectAttribute> result = service.query(queryByPage.getQuery(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
    return new ResultWrapper<Pagination<ReObjectAttribute>>().success(result);
  }

  /**
   * 查询记录数 
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询记录数 ", notes = "查询记录数 ")
  @RequestMapping(value = "/ReObjectAttribute/queryCount", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Long> queryCount(@RequestBody ReObjectAttribute target)
  {
    return new ResultWrapper<Long>().success(service.queryCount(target));
  }

  /**
   * 根据主键查询 
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "查询 ", notes = "根据主键查询 ",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReObjectAttribute/findByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReObjectAttribute> findByPrimaryKey(@RequestParam("id") String id)
  {
    ResultWrapper<ReObjectAttribute> resultWrapper = new ResultWrapper<ReObjectAttribute>();
    ReObjectAttribute record = service.findByPrimaryKey(id);
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

