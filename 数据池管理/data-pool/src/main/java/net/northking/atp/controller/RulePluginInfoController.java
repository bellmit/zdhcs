package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReRulePluginInfo;
import net.northking.atp.db.persistent.ReRulePluginParameter;
import net.northking.atp.db.service.ReRulePluginInfoService;
import net.northking.atp.db.service.ReRulePluginParameterService;
import net.northking.atp.entity.InterfaceReRulePluginInfo;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/7/16 0016.
 */
@RestController
@Api(tags = {"规则插件信息"}, description = "插件_规则插件信息")
@RequestMapping(value = "/rulePluginInfo")
public class RulePluginInfoController {

    @Autowired
    private ReRulePluginParameterService reRulePluginParameterService;
    @Autowired
    private ReRulePluginInfoService reRulePluginInfoService;

    /**
     * 查询 规则列表
     *
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "条件查询 插件列表查询", notes = "条件查询 插件列表查询")
    @RequestMapping(value = "/queryRuleInfoByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<Map<String,Object>>> queryRuleInfoByPage(@RequestBody QueryByPage<ReRulePluginInfo> queryByPage) {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList()) {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }

        ReRulePluginInfo reRulePluginInfo = queryByPage.getQuery();
        reRulePluginInfo.setIsValid("1");
        Pagination<Map<String,Object>> result = reRulePluginInfoService.selectRulesByPage(
                reRulePluginInfo, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<Map<String,Object>>>().success(result);
    }

    /**
     * 查询 规则参数
     *
     * @param target 插件信息
     * @return 接口返回
     */
    @ApiOperation(value = "插件参数查询 业务规则信息", notes = "插件参数查询 业务规则信息")
    @RequestMapping(value = "/queryRuleParamList", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<ReRulePluginParameter>> queryRuleParamList(@RequestBody ReRulePluginInfo target) {
        ReRulePluginParameter query = new ReRulePluginParameter();
        query.setRuleId(target.getId());
        List<ReRulePluginParameter> result = reRulePluginParameterService.query(query);
        return new ResultWrapper<List<ReRulePluginParameter>>().success(result);
    }
}
