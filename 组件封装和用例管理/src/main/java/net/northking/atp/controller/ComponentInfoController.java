package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.element.core.keyword.KeywordInfo;
import net.northking.atp.entity.*;
import net.northking.atp.impl.*;
import net.northking.atp.service.*;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.RedisTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Target;
import java.util.*;

/**
 * 案例组件信息维护
 * Created by Administrator on 2019/3/28 0028.
 */
@RestController
@Api(tags = {"案例组件"}, description = "案例组件_组件信息维护")
@RequestMapping(value = "/caseComponent")
public class ComponentInfoController {
    @Autowired
    private ReComponentInfoService reComponentInfoService; //组件信息表
    @Autowired
    private HisComponentInfoService hisComponentInfoService; //组件信息版本表
    @Autowired
    private ComponentInfoService componentInfoService; //正式表操作服务
    @Autowired
    private ComponentModifyService componentModifyService;//修改表操作服务
    @Autowired
    private MdComponentInfoService mdComponentInfoService;
    @Autowired
    private CaseDesignVersionService caseDesignVersionService;
    @Autowired
    private ComponentHisInfoService componentHisInfoService;
    @Autowired
    private RedisTools redisTools;
    //高级组件数据统一新增先进入MD表，确定提交时在更新到RE表以及留档HIS表
    /**
     * 新增 组件注册表
     *
     * @param target 组件注册表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "新增 组件注册表", notes = "新增 组件注册表")
    @RequestMapping(value = "/ReComponentInfo/insertComponentInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insertComponentInfo(@RequestHeader(name = "Authorization") String authorization, @RequestBody InterfaceComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        //组件名校验
        if(target.getProjectId()==null){
            //参数确实
            return new ResultWrapper().fail("componentInfo0000004","参数缺失，请确认交易内容");
        }
        if(componentInfoService.checkComponentExist(target)){
            //组件名已经存在
            return new ResultWrapper().fail("componentInfo0000003","组件已存在，请更换组件名或查找此组件进行编辑更新");
        }
        String modifyStaff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        target.setModifyStaff(modifyStaff);
        target.setCreateStaff(modifyStaff);
        componentModifyService.insertComponentModifyInfo(target);
        return new ResultWrapper().success(target);
    }

    /**
     * 修改 组件注册表
     *
     * @param target 组件注册表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "修改 组件注册表", notes = "修改 组件注册表")
    @RequestMapping(value = "/ReComponentInfo/updateComponentInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper updateComponentInfo(@RequestHeader(name = "Authorization") String authorization,@RequestBody InterfaceComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getId() == null || target.getProjectId() == null){
            //参数确实
            return new ResultWrapper().fail("componentInfo0000004","参数缺失，请确认交易内容");
        }
        String modifyStaff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        target.setModifyStaff(modifyStaff);
        componentModifyService.updateComponentModifyInfo(target);
        return new ResultWrapper().success(target);
    }

    /**
     * 保存 组件相关信息
     *
     * @param target 组件注册表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "保存 组件全部相关信息", notes = "保存 组件全部相关信息")
    @RequestMapping(value = "/ReComponentInfo/saveComponentInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper saveComponentInfo(@RequestHeader(name = "Authorization") String authorization,@RequestBody InterfaceComponentInfo target) {
        if(target.getProjectId() == null){
            //参数确实
            return new ResultWrapper().fail("componentInfo0000004","参数缺失，请确认交易内容");
        }
        if(componentInfoService.checkComponentExist(target)){
            //组件名已经存在
            return new ResultWrapper().fail("componentInfo0000003","组件已存在，请更换组件名或查找此组件进行编辑更新");
        }
        String modifyStaff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        target.setModifyStaff(modifyStaff);
        if(target.getId() == null || "".equals(target.getId())){
            //新增
            target.setCreateStaff(modifyStaff);
            componentModifyService.insertComponentModifyInfo(target);
        }else {
            //修改
            if(target.getPackageList().size()>0){
                for (InterfaceComponentPackage one : target.getPackageList()){
                    if(target.getId().equals(one.getId())){
                        return new ResultWrapper().fail("componentInfo0000003","业务组件封装内容包含业务组件本身,平台暂不支持递归调用！");
                    }
                }
            }
            componentModifyService.updateComponentModifyInfo(target);
        }
        return new ResultWrapper().success(target);
    }

    /**
     * 删除组件 组件注册表
     *
     * @param target 删除对象
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "删除 组件注册表", notes = "删除 组件注册表")
    @RequestMapping(value = "/ReComponentInfo/deleteComponentInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteComponentInfo(@RequestBody ReComponentInfo target) {
        if(target.getId()==null || target.getProjectId()==null){
            return new ResultWrapper().fail("componentInfo0001","请确认参数是否完整");
        }
        //正式表删除
        componentInfoService.deleteComponentInfo(target);
        //修改表数据删除
        componentModifyService.deleteComponentModifyInfo(target.getId(),target.getProjectId());
        return new ResultWrapper().success();
    }

    /**
     * 查询 组件注册表
     *
     * @param target 组件注册表
     * @return 接口返回
     */
    @ApiOperation(value = "组件查询 基础信息及自定义参数", notes = "组件查询 基础信息及自定义参数")
    @RequestMapping(value = "/ReComponentInfo/queryComponentDetailInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<InterfaceComponentInfo> queryComponentDetailInfo(@RequestBody InterfaceComponentInfo target) {
        String id = target.getId();
        InterfaceComponentInfo interCompInfo = componentModifyService.queryComponentModifyInfo(id);

        return new ResultWrapper<InterfaceComponentInfo>().success(interCompInfo);
    }

    /**
     * 查询 组件注册表_关联
     *
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 组件注册表", notes = "分页查询 组件注册表")
    @RequestMapping(value = "/ReComponentInfo/queryComponentInfoByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<Map<String,Object>>> queryComponentInfoByPage(@RequestBody QueryByPage<InterfaceComponentInfo> queryByPage) {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList()) {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }

        InterfaceComponentInfo reComponentInfo = queryByPage.getQuery();
        Pagination<Map<String,Object>> result = reComponentInfoService.selectComponentInfo(
                reComponentInfo, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<Map<String,Object>>>().success(result);
    }

    /**
     * 查询 组件注册表_修改页面列表
     *
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 组件注册表_修改", notes = "分页查询 组件注册表_修改")
    @RequestMapping(value = "/ReComponentInfo/queryComponentModify", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<Map<String,Object>>> queryComponentModify(@RequestBody QueryByPage<InterfaceComponentInfo> queryByPage) {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList()) {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }

        InterfaceComponentInfo reComponentInfo = queryByPage.getQuery();
        Pagination<Map<String,Object>> result = mdComponentInfoService.selectComponentInfoModify(
                reComponentInfo.toMap(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<Map<String,Object>>>().success(result);
    }

    /**
     * 提交修改到版本
     *
     * @param target 组件信息
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "提交组件版本更新", notes = "将改动提交到历史库")
    @RequestMapping(value = "/ReComponentParameter/commitHighComponentModify", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper commitHighComponentModify(@RequestHeader(name = "Authorization") String authorization,@RequestBody MdComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getId()==null || target.getProjectId()==null){
            return new ResultWrapper().fail("componentInfo0001","请确认参数是否完整");
        }
        //版本校验
        String result = componentModifyService.checkComPackageVersion(target);
        if(!"none".equals(result)){
            return new ResultWrapper().fail("componentInfo0005",result);
        }
        target.setModifyStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        caseDesignVersionService.commitComponentVersion(target);
        return new ResultWrapper().success(target);
    }

    /**
     * 根据版本回滚历史信息
     *
     * @param target
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "回滚历史信息", notes = "回滚历史数据")
    @RequestMapping(value = "/ReComponentInfo/componentInfoHisRollback", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper componentInfoHisRollback(@RequestHeader(name = "Authorization") String authorization,@RequestBody HisComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getId()==null || target.getProjectId()==null){
            return new ResultWrapper().fail("componentInfo0001","请确认参数是否完整");
        }
        target.setModifyStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        caseDesignVersionService.rollbackComponentVersion(target);
        return new ResultWrapper().success();
    }

    /**
     * 保存并提交 组件注册表
     *
     * @param target 组件注册表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "保存提交 组件注册表", notes = "保存提交 组件注册表")
    @RequestMapping(value = "/ReComponentInfo/componentInfoSaveAndCommit", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper componentInfoSaveAndCommit(@RequestHeader(name = "Authorization") String authorization,@RequestBody InterfaceComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getProjectId() == null){
            //参数确实
            return new ResultWrapper().fail("componentInfo0000004","参数缺失，请确认交易内容");
        }
        if(componentInfoService.checkComponentExist(target)){
            //组件名已经存在
            return new ResultWrapper().fail("componentInfo0000003","组件已存在，请更换组件名或查找此组件进行编辑更新");
        }
        String modifyStaff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        target.setModifyStaff(modifyStaff);
        if(target.getId() == null || "".equals(target.getId())){
            //新增
            target.setCreateStaff(modifyStaff);
            componentModifyService.insertComponentModifyInfo(target);
        }else {
            //修改
            if(target.getPackageList().size()>0){
                for (InterfaceComponentPackage one : target.getPackageList()){
                    if(target.getId().equals(one.getId())){
                        return new ResultWrapper().fail("componentInfo0000003","业务组件封装内容包含业务组件本身,平台暂不支持递归调用！");
                    }
                }
            }
            componentModifyService.updateComponentModifyInfo(target);
        }
        //版本校验
        MdComponentInfo check = new MdComponentInfo();
        check.setId(target.getId());
        check.setProjectId(target.getProjectId());
        String result = componentModifyService.checkComPackageVersion(check);
        if(!"none".equals(result)){
            return new ResultWrapper().fail("componentInfo0005",result);
        }else{
            check.setModifyStaff(modifyStaff);
            caseDesignVersionService.commitComponentVersion(check);
        }
        return new ResultWrapper().success(target);
    }


    /**
     * 查询全部 组件注册表_版本
     *
     * @param target 查询条件
     * @return 接口返回
     */
    @ApiOperation(value = "查询全部 组件注册表_版本", notes = "查询全部 组件注册表_版本")
    @RequestMapping(value = "/HisComponentInfo/queryAllHisComponentInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<HisComponentInfo>> queryAllHisComponentInfo(@RequestBody HisComponentInfo target) {
        return new ResultWrapper<List<HisComponentInfo>>().success(hisComponentInfoService.queryAllHisComponentInfo(target));
    }

    /**
     * 查询详情 历史版本表
     *
     * @param target 查询条件
     * @return 接口返回
     */
    @ApiOperation(value = "查询详情 查询某历史版本详情", notes = "查询详情 查询某历史版本详情")
    @RequestMapping(value = "/HisComponentInfo/queryHisDetailComponentInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<InterfaceComponentInfo> queryHisDetailComponentInfo(@RequestBody HisComponentInfo target) {
        String id = target.getId();
        InterfaceComponentInfo interCompInfo = componentHisInfoService.queryHisDetailComponentInfo(target);
        return new ResultWrapper<InterfaceComponentInfo>().success(interCompInfo);
    }

    /**
     * 复制业务组件
     * @param target 组件注册表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "复制组件 组件以及相关信息", notes = "复制组件 组件以及相关信息")
    @RequestMapping(value = "/ReComponentInfo/CopyComponentData", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper CopyComponentData(@RequestHeader(name = "Authorization") String authorization,@RequestBody InterfaceComAndCaseCopy target) {
        if(target.getProjectId() == null){
            //参数确实
            return new ResultWrapper().fail("componentInfo0000004","参数缺失，请确认交易内容");
        }
        String modifyStaff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        if(modifyStaff==null){
            return new ResultWrapper().fail("login0000001","用户获取信息失败!");
        }
        target.setModifyStaff(modifyStaff);
        componentModifyService.CopyComponentDataToMenu(target);
        return new ResultWrapper().success(target);
    }
}
