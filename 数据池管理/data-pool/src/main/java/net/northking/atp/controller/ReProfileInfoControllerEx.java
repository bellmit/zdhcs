/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReDataPoolInfo;
import net.northking.atp.db.persistent.ReProfileInfo;
import net.northking.atp.db.service.ReProfileInfoService;
import net.northking.atp.service.ReDataPoolInfoServiceEx;
import net.northking.atp.service.ReProfileInfoServiceEx;
import net.northking.atp.util.FunctionTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
* 描述环境的基础信息 API
  * createdate:  2019-05-10 16:16:28  <br>
  * @author:  database-mybatis-maven-plugin  <br>
  * @since:  1.0 <br>
*/
@Api(tags = {"底层数据服务--描述环境的基础信息"}, description = "测试数据池-环境信息")
@RestController
@RequestMapping(value = "/extend")
public class ReProfileInfoControllerEx
{

  /**
   * 描述环境的基础信息 数据库持久层服务
   */
  @Autowired
  private ReProfileInfoService reProfileInfoService;

  @Autowired
  private ReProfileInfoServiceEx reProfileInfoServiceEx;
  @Autowired
  private FunctionTools functionTools;
  /**
   * 新增 描述环境的基础信息
   *
   * @param target 描述环境的基础信息
   * @return 接口返回
   */
  @ApiOperation(value = "新增 描述环境的基础信息", notes = "新增 描述环境的基础信息")
  @RequestMapping(value = "/ReProfileInfo/insert", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReProfileInfo> insert(@RequestBody ReProfileInfo target)
  {
    ReProfileInfo param=new ReProfileInfo();
    param.setProfile(target.getProfile());
    param.setProjectId(target.getProjectId());
    List<ReProfileInfo> reProfileInfoList= reProfileInfoService.query(param);
    if(reProfileInfoList!=null && reProfileInfoList.size()>0){
      ResultWrapper<ReProfileInfo> result=new ResultWrapper<ReProfileInfo>();
      result.fail("1", "项目的环境"+target.getProfile()+"已经存在!");
      return result;
    }

    target.setId(functionTools.getUUID());
    reProfileInfoService.insert(target);
    return new ResultWrapper<ReProfileInfo>().success(target);
  }

  /**
   * 根据主键修改 描述环境的基础信息
   *
   * @param target 描述环境的基础信息
   * @return 接口返回
   */
  @ApiOperation(value = "修改 描述环境的基础信息", notes = "根据主键修改 描述环境的基础信息")
  @RequestMapping(value = "/ReProfileInfo/updateByPrimaryKey", method = {RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReProfileInfo> updateByPrimaryKey(@RequestBody ReProfileInfo target)
  {

    ReProfileInfo param=new ReProfileInfo();
    param.setId(target.getId());
    param.setProfile(target.getProfile());
    param.setProjectId(target.getProjectId());
    List<ReProfileInfo> reProfileInfoList= reProfileInfoServiceEx.queryForUpdate(param);
    if(reProfileInfoList!=null && reProfileInfoList.size()>0){
      ResultWrapper<ReProfileInfo> result=new ResultWrapper<ReProfileInfo>();
      result.fail("1", "项目的环境"+target.getProfile()+"已经存在!");
      return result;
    }
    reProfileInfoService.updateByPrimaryKey(target);
    ReProfileInfo newOne = reProfileInfoService.findByPrimaryKey(target);
    return new ResultWrapper<ReProfileInfo>().success(newOne);
  }

  /**
   * 根据主键删除 描述环境的基础信息
   *
   * @param id 主键
   * @return 接口返回
   */
  @ApiOperation(value = "删除 描述环境的基础信息", notes = "根据主键删除 描述环境的基础信息",
          produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  @RequestMapping(value = "/ReProfileInfo/deleteByPrimaryKey", method = {RequestMethod.POST},
          produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper deleteByPrimaryKey(@RequestParam("id") String id)
  {
    if(id!=null && !"".equals(id)){
      reProfileInfoService.deleteByPrimaryKey(id);
    }else{
      ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
      result.fail("1", "请传入参数ID ");
      return result;
    }
    return new ResultWrapper().success();
  }

  /**
   * 根据主键查询 描述环境的基础信息
   *
   * @param target 描述环境的基础信息
   * @return 接口返回
   */
  @ApiOperation(value = "查询 描述环境的基础信息", notes = "查询 描述环境的基础信息")
  @RequestMapping(value = "/ReProfileInfo/queryByPrimaryKey", method = {RequestMethod.POST},
          produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
          consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResultWrapper<ReProfileInfo> queryByPrimaryKey(@RequestBody ReProfileInfo target)
  {
    ReProfileInfo result = reProfileInfoService.findByPrimaryKey(target.getId());
    return new ResultWrapper<ReProfileInfo>().success(result);
  }
}

