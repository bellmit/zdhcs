package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReBusinessRules;
import net.northking.atp.db.service.ReBusinessRulesService;
import net.northking.atp.impl.BusinessRulesServiceImpl;
import net.northking.atp.service.BusinessRulesService;
import net.northking.atp.util.RedisTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by Administrator on 2019/7/15 0015.
 */
@RestController
@Api(tags = {"业务规则"}, description = "业务规则_业务规则库的管理")
@RequestMapping(value = "/businessRules")
public class BusinessRulesController {
    @Autowired
    private BusinessRulesServiceImpl businessRulesService;
    @Autowired
    private ReBusinessRulesService reBusinessRulesService;
    @Autowired
    private RedisTools redisTools;
    /**
     * 保存 业务规则库信息
     * @param target 业务规则库
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "保存规则信息(新增修改)", notes = "保存规则信息(新增修改)")
    @RequestMapping(value = "/saveBusinessRules", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper saveBusinessRules(@RequestHeader(name = "Authorization") String authorization, @RequestBody ReBusinessRules target)
    {
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("businessRules0001","请确认参数是否完整");
        }

        if(businessRulesService.checkBusinessRulesExist(target)){
            //重名
            return new ResultWrapper().fail("businessRules0002","当前规则类型下规则名已存在,请搜索查看或者更换规则名称");
        }
        String modifyName = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        if(target.getId() == null || "".equals(target.getId())){
            //为新增
            target.setCreateStaff(modifyName);
            businessRulesService.insertRulesInfo(target);
        }else{
            //更新
            businessRulesService.updateRulesInfo(target);
        }
        return new ResultWrapper().success(target);
    }

    /**
     * 删除 业务规则库信息
     * @param target 业务规则库
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "删除规则信息", notes = "删除规则信息")
    @RequestMapping(value = "/deleteBusinessRules", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteBusinessRules(@RequestBody ReBusinessRules target)
    {
        if(target.getId()==null){
            return new ResultWrapper().fail("businessRules0001","请确认参数是否完整");
        }
        return new ResultWrapper().success(target);
    }

    /**
     * 查询 业务规则库信息
     *
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "条件查询 业务规则库", notes = "条件查询 业务规则库")
    @RequestMapping(value = "/queryBusinessRules", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<Map<String,Object>>> queryBusinessRules(@RequestBody QueryByPage<ReBusinessRules> queryByPage) {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList()) {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }

        ReBusinessRules reBusinessRules = queryByPage.getQuery();
        Pagination<Map<String,Object>> result = reBusinessRulesService.selectBusinessRules(
                reBusinessRules, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<Map<String,Object>>>().success(result);
    }
}
