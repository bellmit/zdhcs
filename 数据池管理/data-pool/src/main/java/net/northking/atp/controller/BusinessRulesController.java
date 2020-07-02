package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.ReBusinessRulesParameterService;
import net.northking.atp.db.service.ReBusinessRulesService;
import net.northking.atp.db.service.ReDataPoolService;
import net.northking.atp.db.service.ReRulePluginInfoService;
import net.northking.atp.entity.InterfaceBusinessRule;
import net.northking.atp.entity.InterfaceReRulePluginInfo;
import net.northking.atp.impl.BusinessRulesServiceImpl;
import net.northking.atp.service.BusinessRulesService;
import net.northking.atp.service.DataPoolService;
import net.northking.atp.util.FunctionTools;
import net.northking.atp.util.RedisTools;
import net.northking.atp.util.TargetTransformTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/7/15 0015.
 */
@RestController
@Api(tags = {"业务规则"}, description = "业务规则_业务规则库的管理")
@RequestMapping(value = "/businessRules")
public class BusinessRulesController {
    @Autowired
    private BusinessRulesService businessRulesService;
    @Autowired
    private ReBusinessRulesService reBusinessRulesService;
    @Autowired
    private ReBusinessRulesParameterService reBusinessRulesParameterService;
    @Autowired
    private ReDataPoolService reDataPoolService;
    @Autowired
    private DataPoolService dataPoolService;
    @Autowired
    private ReRulePluginInfoService reRulePluginInfoService;
    @Autowired
    private RedisTools redisTools;

    private String project = "SYSTEM";
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
    public ResultWrapper saveBusinessRules(@RequestHeader(name = "Authorization") String authorization, @RequestBody InterfaceBusinessRule target)
    {
        FunctionTools tools = new FunctionTools();
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("businessRules0001","请确认参数是否完整");
        }

        if(businessRulesService.checkBusinessRulesExist(target)){
            //重名
            return new ResultWrapper().fail("businessRules0002","当前规则类型下规则名已存在,请搜索查看或者更换规则名称");
        }
        List<String> idList = new ArrayList<String>();
        ReDataPool data = new ReDataPool();
        data.setDataFalg("0");
        data.setDataName(target.getRuleName());
        data.setUseTimeStart(target.getUseTimeStart());
        data.setUseTimeStop(target.getUseTimeStop());
        data.setProjectId(target.getProjectId());
        if(dataPoolService.checkRecordExist(data)){
            return new ResultWrapper().fail("dataPool","有效数据在静态态数据中已存在同名");
        }
        if(target.getId() == null || "".equals(target.getId())){
            //为新增
            if(target.getParamList() != null && target.getParamList().size()>0){
                target.setPluginId(target.getParamList().get(0).getRuleId());
            }
            String modifyName = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
            String idPro = tools.getUUID();
            target.setCreateStaff(modifyName);
            target.setId(idPro);
            businessRulesService.insertRulesInfo(target);
            idList.add(idPro);
            if(!project.equals(target.getProjectId())){
                //非系统级数据需要入总池
                data.setDataId(target.getId());
                data.setIsReusable(target.getIsReusable());
                dataPoolService.saveDataRecord(data);
                //以及复制一份数据给系统级
                String idSys = tools.getUUID();
                target.setId(idSys);
                idList.add(idSys);
                target.setProjectId(project);
                businessRulesService.insertRulesInfo(target);
            }
        }else{
            //更新if
            if(target.getPluginId() == null || "".equals(target.getPluginId())){
                if(target.getParamList() != null && target.getParamList().size()>0){
                    target.setPluginId(target.getParamList().get(0).getRuleId());
                }
            }
            businessRulesService.updateRulesInfo(target);
            idList.add(target.getId());
            if(!project.equals(target.getProjectId())){
                //系统级只更改数据，不跟新总池
                data.setDataId(target.getId());
                data.setIsReusable(target.getIsReusable());
                dataPoolService.updateDataRecord(data);
            }
        }
        //更新规则参数
        target.setIdList(idList);
        businessRulesService.saveRuleParam(target);
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
        //删除基础信息以及参数
        businessRulesService.deleteRulesInfo(target);
        ReDataPool delete = new ReDataPool();
        delete.setDataId(target.getId());
        reDataPoolService.deleteByExample(delete);
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

    /**
     * 查询 业务规则详情
     *
     * @param target 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "信息查询 业务规则详情", notes = "信息查询 业务规则详情")
    @RequestMapping(value = "/queryBusinessRuleDetail", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<InterfaceBusinessRule> queryBusinessRuleDetail(@RequestBody ReBusinessRules target) {
        if(target.getId()==null){
            return new ResultWrapper().fail("businessRules0001","请确认参数是否完整");
        }
        TargetTransformTools tools = new TargetTransformTools();
        ReBusinessRules rule = reBusinessRulesService.findByPrimaryKey(target.getId());
        InterfaceBusinessRule result = tools.transReRuleToInterface(rule);
        ReBusinessRulesParameter query = new ReBusinessRulesParameter();
        query.setRuleId(query.getId());
        List<ReBusinessRulesParameter> paramList = reBusinessRulesParameterService.query(query);
        result.setParamList(paramList);
        return new ResultWrapper<InterfaceBusinessRule>().success(result);
    }

    /**
     * 查询 业务规则参数
     *
     * @param target 插件信息
     * @return 接口返回
     */
    @ApiOperation(value = "插件参数查询 业务规则信息", notes = "插件参数查询 业务规则信息")
    @RequestMapping(value = "/queryBusinessParamList", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<InterfaceReRulePluginInfo> queryBusinessParamList(@RequestBody ReBusinessRules target) {
        TargetTransformTools tools = new TargetTransformTools();
        InterfaceReRulePluginInfo result = new InterfaceReRulePluginInfo();
        String pluginId = reBusinessRulesService.findByPrimaryKey(target.getId()).getPluginId();
        if(!(pluginId == null || "".equals(pluginId))){
            ReRulePluginInfo info  = reRulePluginInfoService.findByPrimaryKey(pluginId);
            result = tools.transRePluginToInterface(info);
            ReBusinessRulesParameter query = new ReBusinessRulesParameter();
            query.setRuleId(target.getId());
            List<ReBusinessRulesParameter> paramList = reBusinessRulesParameterService.query(query);
            result.setParamList(paramList);
        }
        return new ResultWrapper<InterfaceReRulePluginInfo>().success(result);
    }


    /**
     * 查询 业务规则参数
     *
     * @param target 插件信息
     * @return 接口返回
     */
    @ApiOperation(value = "插件参数查询 业务规则信息", notes = "插件参数查询 业务规则信息")
    @RequestMapping(value = "/genDynDataByRules", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<String> genDynDataByRules(@RequestBody InterfaceBusinessRule target) {
        List<String> result = businessRulesService.genDataListByRuleId(target);
        return result;
    }


}
