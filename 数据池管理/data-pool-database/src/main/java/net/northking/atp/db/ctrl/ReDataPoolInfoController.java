/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.ctrl;

import net.northking.atp.db.persistent.ReDataPoolInfo;
import net.northking.atp.db.service.ReDataPoolInfoService;

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
* 描述测试数据池信息 API
*
* <p>文件内容由代码生成器产生，请不要手动修改！ <br>
  * createdate:  2019-05-23 10:05:48  <br>
  * @author:  database-mybatis-maven-plugin  <br>
  * @since:  1.0 <br>
*/
@Api(tags = {"底层数据服务--描述测试数据池信息"}, description = "由代码生成器自动生成")
@RestController
@RequestMapping(value = "/genCode")
public class ReDataPoolInfoController
{

  /**
   * 描述测试数据池信息 数据库持久层服务
   */
  @Autowired
  private ReDataPoolInfoService service;


  /**
   * 新增 描述测试数据池信息
   *
   * @param target 描述测试数据池信息
   * @return 接口返回
   */
  @ApiOperation(value = "新增 描述测试数据池信息", notes = "新增 描述测试数据池信息")
  @RequestMapping(value = "/ReDataPoolInfo/insert", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReDataPoolInfo> insert(@RequestBody ReDataPoolInfo target)
  {
    service.insert(target);
    return new ResultWrapper<ReDataPoolInfo>().success(target);
  }

  /**
   * 新增批量 描述测试数据池信息
   *
   * @param list 批量描述测试数据池信息
   * @return 接口返回
   */
  @ApiOperation(value = "批量新增 描述测试数据池信息", notes = "批量新增 描述测试数据池信息")
  @RequestMapping(value = "/ReDataPoolInfo/insertBatch", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper insertBatch(@RequestBody List<ReDataPoolInfo> list)
  {
    service.insertByBatch(list);
    return new ResultWrapper().success();
  }

  /**
   * 根据主键修改 描述测试数据池信息
   *
   * @param target 描述测试数据池信息
   * @return 接口返回
   */
  @ApiOperation(value = "修改 描述测试数据池信息", notes = "根据主键修改 描述测试数据池信息")
  @RequestMapping(value = "/ReDataPoolInfo/updateByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReDataPoolInfo> updateByPrimaryKey(@RequestBody ReDataPoolInfo target)
  {
    service.updateByPrimaryKey(target);
    ReDataPoolInfo newOne = service.findByPrimaryKey(target);
    return new ResultWrapper<ReDataPoolInfo>().success(newOne);
  }

  /**
   * 根据主键删除 描述测试数据池信息
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "删除 描述测试数据池信息", notes = "根据主键删除 描述测试数据池信息",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReDataPoolInfo/deleteByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKey(@RequestParam("id") String id)
  {
    service.deleteByPrimaryKey(id);
    return new ResultWrapper().success();
  }

  /**
  * 根据主键批量删除 描述测试数据池信息
  *
  * @param ids 主键数组
  * @return 接口返回
  */
  @ApiOperation(value = "批量删除 描述测试数据池信息", notes = "根据主键批量删除 描述测试数据池信息",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReDataPoolInfo/deleteByPrimaryKeys", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKeys(@RequestParam("ids") String[] ids)
  {
    service.deleteByPrimaryKey(ids);
    return new ResultWrapper().success();
  }

  /**
   * 查询全部 描述测试数据池信息
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询全部 描述测试数据池信息", notes = "查询全部 描述测试数据池信息")
  @RequestMapping(value = "/ReDataPoolInfo/query", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<List<ReDataPoolInfo>> query(@RequestBody ReDataPoolInfo target)
  {
    return new ResultWrapper<List<ReDataPoolInfo>>().success(service.query(target));
  }

  /**
   * 分页查询 描述测试数据池信息
   * @param queryByPage 分页查询对象
   * @return 接口返回
   */
  @ApiOperation(value = "分页查询 描述测试数据池信息", notes = "分页查询 描述测试数据池信息")
  @RequestMapping(value = "/ReDataPoolInfo/queryByPage", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Pagination<ReDataPoolInfo>> queryByPage(@RequestBody QueryByPage<ReDataPoolInfo> queryByPage)
  {
    OrderBy orderBy = new SqlOrderBy();
    for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
    {
      orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
    }
    Pagination<ReDataPoolInfo> result = service.query(queryByPage.getQuery(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
    return new ResultWrapper<Pagination<ReDataPoolInfo>>().success(result);
  }

  /**
   * 查询记录数 描述测试数据池信息
   *
   * @param target 查询条件
   * @return 接口返回
   */
  @ApiOperation(value = "查询记录数 描述测试数据池信息", notes = "查询记录数 描述测试数据池信息")
  @RequestMapping(value = "/ReDataPoolInfo/queryCount", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<Long> queryCount(@RequestBody ReDataPoolInfo target)
  {
    return new ResultWrapper<Long>().success(service.queryCount(target));
  }

  /**
   * 根据主键查询 描述测试数据池信息
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "查询 描述测试数据池信息", notes = "根据主键查询 描述测试数据池信息",
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReDataPoolInfo/findByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReDataPoolInfo> findByPrimaryKey(@RequestParam("id") String id)
  {
    ResultWrapper<ReDataPoolInfo> resultWrapper = new ResultWrapper<ReDataPoolInfo>();
    ReDataPoolInfo record = service.findByPrimaryKey(id);
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

