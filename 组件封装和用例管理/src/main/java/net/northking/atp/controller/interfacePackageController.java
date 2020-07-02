package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceInterfaceInfo;
import net.northking.atp.impl.*;
import net.northking.atp.util.RedisTools;
import net.northking.atp.util.TargetTransformTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 接口封装controller
 * Created by Administrator on 2019/6/24 0024.
 */
@RestController
@Api(tags = {"接口封装"}, description = "接口封装相关操作")
@RequestMapping(value = "/interfacePackage")
public class interfacePackageController {
    @Autowired
    private InterfaceInfoModifyServiceImpl interfaceInfoModifyService;
    @Autowired
    private MdInterfaceInfoService mdInterfaceInfoService;
    @Autowired
    private MdInterfaceDataService mdInterfaceDataService;
    @Autowired
    private CaseDesignVersionServiceImpl caseDesignVersionService;
    @Autowired
    private ReInterfaceInfoService reInterfaceInfoService;
    @Autowired
    private HisInterfaceInfoService hisInterfaceInfoService;
    @Autowired
    private InterfaceInfoHisServiceImpl interfaceInfoHisService;
    @Autowired
    private InterfaceInfoFormalServiceImpl interfaceInfoFormalService;
    @Autowired
    private ComponentInfoServiceImpl componentInfoService; //组件正式表操作服务
    @Autowired
    private ReComponentInfoService reComponentInfoService;
    @Autowired
    private RedisTools redisTools;
    /**
     * 新增 接口测试_修改表
     *
     * @param target 接口测试相关表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "接口测试 接口信息新增", notes = "接口测试 接口信息新增")
    @RequestMapping(value = "/insertInterfaceInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<MdInterfaceInfo> insertInterfaceInfo(@RequestHeader(name = "Authorization") String authorization, @RequestBody InterfaceInterfaceInfo target)
    {
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        if(interfaceInfoFormalService.checkInterfaceExist(target)){
            return new ResultWrapper().fail("interfacePackage0003","接口名称已存在请重新命名");
        }
        //增加基础信息
        MdInterfaceInfo insertInfo = target;
        if(insertInfo.getUrl() != null){
            insertInfo.setUrl(insertInfo.getUrl().trim());
        }
        target.setCreateStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        interfaceInfoModifyService.insertInterfaceInfo(insertInfo);
        //参数数据保存库表
        if(target.getDataMap() != null && !"".equals(target.getDataMap())){
            Map<String,Object> dataMap = target.getDataMap();
            interfaceInfoModifyService.saveInterfaceData(dataMap,insertInfo);
        }else{
            return new ResultWrapper().fail("interfacePackage0002","list数据不完整");
        }
        return new ResultWrapper<MdInterfaceInfo>().success(insertInfo);
    }

    /**
     * 修改 接口测试_修改表
     *
     * @param target 接口测试相关表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "接口测试 接口信息修改", notes = "接口测试 接口信息修改")
    @RequestMapping(value = "/updateInterfaceInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<ReInterfaceInfo> updateInterfaceInfo(@RequestHeader(name = "Authorization") String authorization, @RequestBody InterfaceInterfaceInfo target)
    {
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        if(interfaceInfoFormalService.checkInterfaceExist(target)){
            return new ResultWrapper().fail("interfacePackage0003","接口名称已存在请重新命名");
        }
        //修改基础信息
        MdInterfaceInfo insertInfo = target;
        if(insertInfo.getUrl() != null){
            insertInfo.setUrl(insertInfo.getUrl().trim());
        }
        target.setCreateStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        mdInterfaceInfoService.updateByPrimaryKey(insertInfo);
        //参数数据保存库表
        MdInterfaceData delete = new MdInterfaceData();
        delete.setProjectId(target.getProjectId());
        delete.setInterfaceId(target.getId());
        mdInterfaceDataService.deleteByExample(delete);
        if(target.getDataMap() != null && !"".equals(target.getDataMap())){
            Map<String,Object> dataMap = target.getDataMap();
            interfaceInfoModifyService.saveInterfaceData(dataMap,insertInfo);
        }else{
            return new ResultWrapper().fail("interfacePackage0002","list数据不完整");
        }
        return new ResultWrapper<ReInterfaceInfo>().success();
    }

    /**
     * 保存 接口测试所有数据
     *
     * @param target 接口测试相关表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "保存信息 接口信息新增或修改", notes = "保存信息 接口信息新增或修改")
    @RequestMapping(value = "/saveInterfaceInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<MdInterfaceInfo> saveInterfaceInfo(@RequestHeader(name = "Authorization") String authorization,@RequestBody InterfaceInterfaceInfo target)
    {
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        if(interfaceInfoFormalService.checkInterfaceExist(target)){
            return new ResultWrapper().fail("interfacePackage0003","接口名称已存在请重新命名");
        }
        MdInterfaceInfo insertInfo = target;
        if(insertInfo.getUrl() != null){
            insertInfo.setUrl(insertInfo.getUrl().trim());
        }
        target.setCreateStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        if(target.getId() == null || "".equals(target.getId())){
            //新增
            interfaceInfoModifyService.insertInterfaceInfo(insertInfo);
        }else{
            //修改
            mdInterfaceInfoService.updateByPrimaryKey(insertInfo);
            //参数数据保存库表
            MdInterfaceData delete = new MdInterfaceData();
            delete.setProjectId(target.getProjectId());
            delete.setInterfaceId(target.getId());
            mdInterfaceDataService.deleteByExample(delete);
        }
        if(target.getDataMap() != null && !"".equals(target.getDataMap())){
            Map<String,Object> dataMap = target.getDataMap();
            interfaceInfoModifyService.saveInterfaceData(dataMap,insertInfo);
        }else{
            return new ResultWrapper().fail("interfacePackage0002","list数据不完整");
        }
        return new ResultWrapper<MdInterfaceInfo>().success(insertInfo);
    }

    /**
     * 列表查询 接口测试_修改表
     *
     * @param queryByPage 接口测试相关表
     * @return 接口返回
     */
    @ApiOperation(value = "接口测试 接口信息查询_修改表", notes = "接口测试 接口信息查询_修改表")
    @RequestMapping(value = "/queryInterfaceInfoModifyByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<MdInterfaceInfo>> queryInterfaceInfoModifyByPage(@RequestBody QueryByPage<MdInterfaceInfo> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        MdInterfaceInfo mdInterfaceInfo = queryByPage.getQuery();
        Pagination<MdInterfaceInfo> result = mdInterfaceInfoService.selectModifyInterfaceInfo(
                mdInterfaceInfo, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<MdInterfaceInfo>>().success(result);
    }

    /**
     * 列表查询 接口测试_正式表
     *
     * @param queryByPage 接口测试相关表
     * @return 接口返回
     */
    @ApiOperation(value = "接口测试 接口信息查询_正式表", notes = "接口测试 接口信息查询_正式表")
    @RequestMapping(value = "/queryInterfaceInfoByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<ReInterfaceInfo>> queryInterfaceInfoByPage(@RequestHeader(name = "Authorization") String authorization,@RequestBody QueryByPage<ReInterfaceInfo> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        ReInterfaceInfo reInterfaceInfo = queryByPage.getQuery();
        Pagination<ReInterfaceInfo> result = reInterfaceInfoService.selectInterfaceInfo(
                reInterfaceInfo, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<ReInterfaceInfo>>().success(result);
    }

    /**
     * 接口详细信息查询 接口测试_修改表
     *
     * @param target 接口测试
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "接口测试 接口详细信息查询", notes = "接口测试 接口详细信息查询")
    @RequestMapping(value = "/queryDetailInfoModify", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<InterfaceInterfaceInfo> queryDetailInfoModify(@RequestBody MdInterfaceInfo target)
    {
        if(target.getId() == null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        //获得基础信息
        MdInterfaceInfo info = mdInterfaceInfoService.findByPrimaryKey(target.getId());
        TargetTransformTools tools = new TargetTransformTools();
        InterfaceInterfaceInfo result = tools.transMdInterfaceToInterface(info);
        result.setDataMap(interfaceInfoModifyService.queryDataList(info));
        return new ResultWrapper<InterfaceInterfaceInfo>().success(result);
    }

    /**
     * 删除 接口测试_修改表
     *
     * @param target 接口测试相关表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "接口测试 接口信息删除", notes = "接口测试 接口信息删除")
    @RequestMapping(value = "/deleteInterfaceInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteInterfaceInfo(@RequestBody MdInterfaceInfo target)
    {
        if(target.getId() == null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        //删除修改表数据
        interfaceInfoModifyService.deleteInterfaceData(target.getId(),target.getProjectId());
        //删除正式表数据
        interfaceInfoFormalService.deleteInterfaceFormalData(target.getId(),target.getProjectId());
        //删除组件正式表数据
        ReComponentInfo delete = new ReComponentInfo();
        delete.setId(target.getId());
        delete.setProjectId(target.getProjectId());
        componentInfoService.deleteComponentInfo(delete);
        return new ResultWrapper().success();
    }

    /**
     * 版本提交 接口测试
     *
     * @param target 接口测试相关表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "接口测试 接口测试信息版本更新", notes = "接口测试 接口测试信息版本更新")
    @RequestMapping(value = "/commitHighInterfaceInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper commitHighInterfaceInfo(@RequestHeader(name = "Authorization") String authorization,@RequestBody MdInterfaceInfo target)
    {
        if(target.getProjectId()==null || target.getId() == null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        MdInterfaceInfo checkInfo = mdInterfaceInfoService.findByPrimaryKey(target.getId());
        ReComponentInfo check = new ReComponentInfo();
        check.setComponentName(checkInfo.getInterfaceName());
        check.setProjectId(target.getProjectId());
        check.setId(target.getId());
        if(componentInfoService.checkComponentExist(check)){
            //组件名已经存在
            return new ResultWrapper().fail("componentInfo0000003","组件已存在，请更换组件名或查找此组件进行编辑更新");
        }
        target.setCreateStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        caseDesignVersionService.commitInterfaceVersion(target);
        return new ResultWrapper().success();
    }

    /**
     * 版本回滚 接口测试
     *
     * @param target 接口测试相关表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "接口测试 接口测试信息回滚至历史版本", notes = "接口测试 接口测试信息回滚至历史版本")
    @RequestMapping(value = "/interfaceInfoHisRollback", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper interfaceInfoHisRollback(@RequestHeader(name = "Authorization") String authorization,@RequestBody HisInterfaceInfo target)
    {
        if(target.getProjectId()==null || target.getId() == null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        target.setCreateStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        caseDesignVersionService.rollbackInterfaceVersion(target);
        return new ResultWrapper().success();
    }

    /**
     *版本记录查询 接口测试
     *
     * @param target 查询条件
     * @return 接口返回
     */
    @ApiOperation(value = "接口测试 历史版本列表查询", notes = "查询全部 历史版本列表查询")
    @RequestMapping(value = "/queryAllHisInterfaceInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<HisInterfaceInfo>> queryAllHisComponentInfo(@RequestBody HisInterfaceInfo target) {
        return new ResultWrapper<List<HisInterfaceInfo>>().success(hisInterfaceInfoService.queryAllHisInterfaceInfo(target));
    }

    /**
     * 指定版本接口详细信息查询 接口测试_历史表
     *
     * @param target 接口测试
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "接口测试 历史版本接口详情查询", notes = "接口测试 历史版本接口详情查询")
    @RequestMapping(value = "/queryHisDetailInterfaceInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<InterfaceInterfaceInfo> queryHisDetailInterfaceInfo(@RequestBody HisInterfaceInfo target)
    {
        if(target.getId() == null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        //获得基础信息
        HisInterfaceInfo info = hisInterfaceInfoService.findByPrimaryKey(target.getId());
        TargetTransformTools tools = new TargetTransformTools();
        InterfaceInterfaceInfo result = tools.transHisInterfaceToInterface(info);
        result.setDataMap(interfaceInfoHisService.queryHisDataList(info));
        return new ResultWrapper<InterfaceInterfaceInfo>().success(result);
    }

    /**
     * 保存并提交 接口测试所有数据
     *
     * @param target 接口测试相关表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "保存并提交 接口信息新增或修改并提交", notes = "保存并提交 接口信息新增或修改并提交")
    @RequestMapping(value = "/interfaceInfoSaveAndCommit", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<MdInterfaceInfo> interfaceInfoSaveAndCommit(@RequestHeader(name = "Authorization") String authorization,@RequestBody InterfaceInterfaceInfo target)
    {
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        if(interfaceInfoFormalService.checkInterfaceExist(target)){
            return new ResultWrapper().fail("interfacePackage0003","接口名称已存在请重新命名");
        }
        MdInterfaceInfo insertInfo = target;
        if(insertInfo.getUrl() != null){
            insertInfo.setUrl(insertInfo.getUrl().trim());
        }
        target.setCreateStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        if(target.getId() == null || "".equals(target.getId())){
            //新增
            interfaceInfoModifyService.insertInterfaceInfo(insertInfo);
        }else{
            //修改
            mdInterfaceInfoService.updateByPrimaryKey(insertInfo);
            //参数数据保存库表
            MdInterfaceData delete = new MdInterfaceData();
            delete.setProjectId(target.getProjectId());
            delete.setInterfaceId(target.getId());
            mdInterfaceDataService.deleteByExample(delete);
        }
        if(target.getDataMap() != null && !"".equals(target.getDataMap())){
            Map<String,Object> dataMap = target.getDataMap();
            interfaceInfoModifyService.saveInterfaceData(dataMap,insertInfo);
        }else{
            return new ResultWrapper().fail("interfacePackage0002","list数据不完整");
        }
        //提交数据
        caseDesignVersionService.commitInterfaceVersion(target);
        return new ResultWrapper<MdInterfaceInfo>().success(insertInfo);
    }

    /**
     * 保存并提交 接口测试所有数据_DEBUG
     *
     * @param target 接口测试相关表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "保存并提交 接口信息DEBUG测试用", notes = "保存并提交 接口信息DEBUG测试用")
    @RequestMapping(value = "/interfaceInfoSaveAndCommitForDebug", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<MdInterfaceInfo> interfaceInfoSaveAndCommitForDebug(@RequestBody InterfaceInterfaceInfo target)
    {
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("interfacePackage0001","请确认参数是否完整");
        }
        if(interfaceInfoFormalService.checkInterfaceExist(target)){
            return new ResultWrapper().fail("interfacePackage0003","接口名称已存在请重新命名");
        }
        MdInterfaceInfo insertInfo = target;
        if(insertInfo.getUrl() != null){
            insertInfo.setUrl(insertInfo.getUrl().trim());
        }
        if(target.getId() == null || "".equals(target.getId())){
            //新增
            interfaceInfoModifyService.insertInterfaceInfo(insertInfo);
        }
        if(target.getDataMap() != null && !"".equals(target.getDataMap())){
            Map<String,Object> dataMap = target.getDataMap();
            interfaceInfoModifyService.saveInterfaceData(dataMap,insertInfo);
        }else{
            return new ResultWrapper().fail("interfacePackage0002","list数据不完整");
        }
        //提交数据
        caseDesignVersionService.commitInterfaceVersion(target);
        return new ResultWrapper<MdInterfaceInfo>().success(insertInfo);
    }
}
