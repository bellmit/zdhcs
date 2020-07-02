package net.northking.atp.db.ctrl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReServiceConfig;
import net.northking.atp.db.service.ReServiceConfigService;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags={"底层数据服务--平台服务参数表"}, description="由代码生成器自动生成")
@RestController
@RequestMapping({"/genCode"})
public class ReServiceConfigController
{
  @Autowired
  private ReServiceConfigService service;
  
  @ApiOperation(value="新增 平台服务参数表", notes="新增 平台服务参数表")
  @RequestMapping(value={"/ReServiceConfig/insert"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"application/json;charset=UTF-8"}, consumes={"application/json;charset=UTF-8"})
  public ResultWrapper<ReServiceConfig> insert(@RequestBody ReServiceConfig target)
  {
    this.service.insert(target);
    return new ResultWrapper().success(target);
  }
  
  @ApiOperation(value="批量新增 平台服务参数表", notes="批量新增 平台服务参数表")
  @RequestMapping(value={"/ReServiceConfig/insertBatch"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"application/json;charset=UTF-8"}, consumes={"application/json;charset=UTF-8"})
  public ResultWrapper insertBatch(@RequestBody List<ReServiceConfig> list)
  {
    this.service.insertByBatch(list);
    return new ResultWrapper().success();
  }
  
  @ApiOperation(value="修改 平台服务参数表", notes="根据主键修改 平台服务参数表")
  @RequestMapping(value={"/ReServiceConfig/updateByPrimaryKey"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"application/json;charset=UTF-8"}, consumes={"application/json;charset=UTF-8"})
  public ResultWrapper<ReServiceConfig> updateByPrimaryKey(@RequestBody ReServiceConfig target)
  {
    this.service.updateByPrimaryKey(target);
    ReServiceConfig newOne = (ReServiceConfig)this.service.findByPrimaryKey(target);
    return new ResultWrapper().success(newOne);
  }
  
  @ApiOperation(value="删除 平台服务参数表", notes="根据主键删除 平台服务参数表", produces="application/json;charset=UTF-8")
  @RequestMapping(value={"/ReServiceConfig/deleteByPrimaryKey"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"application/json;charset=UTF-8"})
  public ResultWrapper deleteByPrimaryKey(@RequestParam("id") String id)
  {
    this.service.deleteByPrimaryKey(id);
    return new ResultWrapper().success();
  }
  
  @ApiOperation(value="批量删除 平台服务参数表", notes="根据主键批量删除 平台服务参数表", produces="application/json;charset=UTF-8")
  @RequestMapping(value={"/ReServiceConfig/deleteByPrimaryKeys"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"application/json;charset=UTF-8"})
  public ResultWrapper deleteByPrimaryKeys(@RequestParam("ids") String[] ids)
  {
    this.service.deleteByPrimaryKey(ids);
    return new ResultWrapper().success();
  }
  
  @ApiOperation(value="查询全部 平台服务参数表", notes="查询全部 平台服务参数表")
  @RequestMapping(value={"/ReServiceConfig/query"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"application/json;charset=UTF-8"}, consumes={"application/json;charset=UTF-8"})
  public ResultWrapper<List<ReServiceConfig>> query(@RequestBody ReServiceConfig target)
  {
    return new ResultWrapper().success(this.service.query(target));
  }
  
  @ApiOperation(value="分页查询 平台服务参数表", notes="分页查询 平台服务参数表")
  @RequestMapping(value={"/ReServiceConfig/queryByPage"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"application/json;charset=UTF-8"}, consumes={"application/json;charset=UTF-8"})
  public ResultWrapper<Pagination<ReServiceConfig>> queryByPage(@RequestBody QueryByPage<ReServiceConfig> queryByPage)
  {
    OrderBy orderBy = new SqlOrderBy();
    for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList()) {
      orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
    }
    Object result = this.service.query(queryByPage.getQuery(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
    return new ResultWrapper().success(result);
  }
  
  @ApiOperation(value="查询记录数 平台服务参数表", notes="查询记录数 平台服务参数表")
  @RequestMapping(value={"/ReServiceConfig/queryCount"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"application/json;charset=UTF-8"}, consumes={"application/json;charset=UTF-8"})
  public ResultWrapper<Long> queryCount(@RequestBody ReServiceConfig target)
  {
    return new ResultWrapper().success(Long.valueOf(this.service.queryCount(target)));
  }
  
  @ApiOperation(value="查询 平台服务参数表", notes="根据主键查询 平台服务参数表", produces="application/json;charset=UTF-8")
  @RequestMapping(value={"/ReServiceConfig/findByPrimaryKey"}, method={org.springframework.web.bind.annotation.RequestMethod.POST}, produces={"application/json;charset=UTF-8"})
  public ResultWrapper<ReServiceConfig> findByPrimaryKey(@RequestParam("id") String id)
  {
    ResultWrapper<ReServiceConfig> resultWrapper = new ResultWrapper();
    ReServiceConfig record = (ReServiceConfig)this.service.findByPrimaryKey(id);
    if (record == null) {
      resultWrapper.fail("0000001", "不存在[主键=" + id + "]的记录！");
    } else {
      resultWrapper.success(this.service.findByPrimaryKey(id));
    }
    return resultWrapper;
  }
}
