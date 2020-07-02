package net.northking.atp.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.HisCaseDesignInfo;
import net.northking.atp.db.persistent.MdCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseDesignMenutree;
import net.northking.atp.db.persistent.ReCaseSetLink;
import net.northking.atp.db.persistent.ReComponentInfo;
import net.northking.atp.db.service.HisCaseDesignInfoService;
import net.northking.atp.db.service.MdCaseDesignInfoService;
import net.northking.atp.db.service.ReCaseDesignInfoService;
import net.northking.atp.db.service.ReCaseDesignMenutreeService;
import net.northking.atp.db.service.ReCaseSetLinkService;
import net.northking.atp.db.service.ReCaseStepService;
import net.northking.atp.db.service.ReComponentStepService;
import net.northking.atp.db.service.ReStepParameterService;
import net.northking.atp.entity.InterfaceCaseInfo;
import net.northking.atp.impl.CaseDesignHisServiceImpl;
import net.northking.atp.impl.CaseDesignModifyServiceImpl;
import net.northking.atp.impl.CaseStepComponentServiceImpl;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.RedisTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;

/**
 * Created by Administrator on 2019/3/12 0012.
 */
@RestController
@Api(tags = {"案例设计"}, description = "案例设计_案例信息的维护")
@RequestMapping(value = "/caseDesign/caseInfo")
public class caseInfoController {
    private static final Logger logger = LoggerFactory.getLogger(caseInfoController.class);
    @Autowired
    private ReCaseDesignInfoService reCaseDesignInfoService;
    @Autowired
    private ReCaseSetLinkService reCaseSetLinkService;
    @Autowired
    private ReCaseStepService reCaseStepService;
    @Autowired
    private ReComponentStepService reComponentStepService;
    @Autowired
    private ReStepParameterService reStepParameterService;
    @Autowired
    private HisCaseDesignInfoService hisCaseDesignInfoService;
    @Autowired
    private CaseStepComponentServiceImpl caseStepComponentService;
    @Autowired
    private CaseDesignModifyServiceImpl caseDesignModifyService;
    @Autowired
    private MdCaseDesignInfoService mdCaseDesignInfoService;
    @Autowired
    private CaseDesignHisServiceImpl caseDesignHisService;
    @Autowired
    private RedisTools redisTools;
    @Autowired
    private ReCaseDesignMenutreeService reCaseDesignMenutreeService;
    /**
     * 分页查询 案例信息表
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 案例信息表", notes = "分页查询 案例信息表")
    @RequestMapping(value = "/queryByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<ReCaseDesignInfo>> queryByPage(@RequestBody QueryByPage<ReCaseDesignInfo> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        ReCaseDesignInfo caseDesignInfo = queryByPage.getQuery();
        Pagination<ReCaseDesignInfo> result = reCaseDesignInfoService.selectTreeCaseInfo(caseDesignInfo, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<ReCaseDesignInfo>>().success(result);
    }

    /**
     * 分页查询 案例信息表_修改表
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 案例信息表", notes = "分页查询 案例信息表")
    @RequestMapping(value = "/queryCaseInfoModifyByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<MdCaseDesignInfo>> queryCaseInfoModifyByPage(@RequestBody QueryByPage<MdCaseDesignInfo> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        MdCaseDesignInfo caseDesignInfo = queryByPage.getQuery();
        Pagination<MdCaseDesignInfo> result = mdCaseDesignInfoService.selectModifyCaseInfo(
                caseDesignInfo.toMap(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<MdCaseDesignInfo>>().success(result);
    }


    /**
     * 新增 测试案例表_修改表
     *
     * @param target 测试案例表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "测试案例表 自定义新增", notes = "测试案例表 自定义新增")
    @RequestMapping(value = "/insertCaseInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insertCaseInfo(@RequestBody InterfaceCaseInfo target)
    {
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("caseStep0001","请确认参数是否完整");
        }
        caseDesignModifyService.insertModifyCaseInfo(target);
        return new ResultWrapper().success(target);
    }

    /**
     * 修改 案例步骤关联
     *
     * @param target 组件信息
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "修改 案例步骤组件管理", notes = "修改 案例步骤组件关联")
    @RequestMapping(value = "/modifyCaseInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper modifyCaseInfo(@RequestBody InterfaceCaseInfo target)
    {
        //此接口同时增加案例步骤关联，步骤组件管理，步骤参数_删除新增的方式
        target.setCaseId(target.getId());
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getCaseId()==null || target.getProjectId()==null){
            return new ResultWrapper().fail("caseStep0001","请确认参数是否完整");
        }
        //插入现有数据
        caseDesignModifyService.updateModifyCaseInfo(target);
        return new ResultWrapper().success();
    }

    /**
     * 保存 案例步骤信息
     *
     * @param target 组件信息
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "保存 案例步骤信息(基础+步骤)", notes = "修改 案例步骤信息(基础+步骤)")
    @RequestMapping(value = "/saveCaseInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper saveCaseInfo(@RequestHeader(name = "Authorization") String authorization, @RequestBody InterfaceCaseInfo target)
    {
        //保存案例步骤信息，包含新增以及编辑
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("caseStep0001","请确认参数是否完整");
        }
        String staff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        target.setModifyStaff(staff);
        logger.info("=================Key:"+authorization+"登录人:"+staff);
        if(caseDesignModifyService.checkCaseDesignExist(target)){
            //重名
            return new ResultWrapper().fail("caseInfo0000005","案例已存在,请搜索查看或者更换案例名称");
        }
        if(target.getId() == null || "".equals(target.getId())){
            //为新增
            target.setCreateStaff(staff);
            caseDesignModifyService.insertModifyCaseInfo(target);
        }else{
            //更新
            target.setCaseId(target.getId());
            caseDesignModifyService.updateModifyCaseInfo(target);
        }
        return new ResultWrapper().success();
    }

    /**
     * 删除案例 案例信息相关
     *
     * @param target 删除对象
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "删除 案例信息相关", notes = "删除 案例信息相关")
    @RequestMapping(value = "/deleteCaseInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteCaseInfo(@RequestBody ReCaseDesignInfo target) {
        if(target.getId()==null || target.getProjectId()==null){
            return new ResultWrapper().fail("caseInfo0001","请确认参数是否完整");
        }
        //校验是否可以删除
        ReCaseSetLink check = new ReCaseSetLink();
        check.setCaseId(target.getId());
        check.setProjectId(target.getProjectId());
        List<ReCaseSetLink> linkList = reCaseSetLinkService.query(check);
        if(linkList != null && linkList.size()>0){
            return new ResultWrapper().fail("caseInfo0003","案例已关联案例集,无法删除!");
        }
        //修改表删除
        caseDesignModifyService.deleteModifyCaseInfo(target.getId(),target.getProjectId());
        //正式表删除
        caseStepComponentService.deleteCaseInfo(target.getId(),target.getProjectId());
        return new ResultWrapper().success();
    }

    /**
     * 用例集关联 备选用例列表查询
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "案例列表查询 去除已关联", notes = "案例列表查询 去除已关联")
    @RequestMapping(value = "/queryCaseListUnSelect", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<ReCaseDesignInfo>> queryCaseListUnSelect(@RequestBody QueryByPage<InterfaceCaseInfo> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        InterfaceCaseInfo caseDesignInfo = queryByPage.getQuery();
        CaseDesignTools tools = new CaseDesignTools();
        tools.setLikeKey(caseDesignInfo);
        Map<String,Object> query = caseDesignInfo.toMap();
        //从列表中去除已存在的案例信息
        if(caseDesignInfo.getId() != null){
            ReCaseSetLink reCaseSetLink = new ReCaseSetLink();
            reCaseSetLink.setSetId(caseDesignInfo.getId());
            reCaseSetLink.setProjectId(caseDesignInfo.getProjectId());
            List<ReCaseSetLink> caseList = reCaseSetLinkService.query(reCaseSetLink);
            String caseAll = "";
            for(ReCaseSetLink link : caseList){
                if("".equals(caseAll)){
                    caseAll = caseAll + "'"+link.getCaseId()+"'";
                }else{
                    caseAll = caseAll + ",'"+link.getCaseId()+"'";
                }
            }
            if(!"".equals(caseAll)){
                query.put("caseAll",caseAll);
            }
        }
        query.remove("id");
        Pagination<ReCaseDesignInfo> result = reCaseDesignInfoService.selectCaseLinkUnSelect(
                query, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());

        return new ResultWrapper<Pagination<ReCaseDesignInfo>>().success(result);
    }

    /**
     * 查询全部 案例信息版本表
     *
     * @param target 查询条件
     * @return 接口返回
     */
    @ApiOperation(value = "查询全部 案例信息版本表", notes = "查询全部 案例信息版本表")
    @RequestMapping(value = "/queryAllHisCaseInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<HisCaseDesignInfo>> queryAllHisCaseInfo(@RequestBody HisCaseDesignInfo target)
    {
        List<HisCaseDesignInfo> result = hisCaseDesignInfoService.queryAllHisCaseInfo(target);
        return new ResultWrapper<List<HisCaseDesignInfo>>().success(result);
    }

    /**
     * 版本详情 案例信息指定版本的详细信息
     *
     * @param target 查询条件
     * @return 接口返回
     */
    @ApiOperation(value = "历史查询 案例信息指定版本的详细信息", notes = "历史查询 案例信息指定版本的详细信息")
    @RequestMapping(value = "/queryDetailHisCaseInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<InterfaceCaseInfo> queryDetailHisCaseInfo(@RequestBody HisCaseDesignInfo target)
    {
        HisCaseDesignInfo hisInfo = hisCaseDesignInfoService.findByPrimaryKey(target.getId());
        InterfaceCaseInfo result = new InterfaceCaseInfo();
        result.setComponentList(caseDesignHisService.queryDetailHisCaseInfo(hisInfo));
        result.setCaseName(hisInfo.getCaseName());
        result.setCaseNo(hisInfo.getCaseNo());
        result.setVersion(hisInfo.getVersion());
        result.setCreateTime(hisInfo.getCreateTime());
        result.setCreateStaff(hisInfo.getCreateStaff());
        return new ResultWrapper<InterfaceCaseInfo>().success(result);
    }

    /**
     * 查询 案例基本信息
     *
     * @param target 查询条件
     * @return 接口返回
     */
    @ApiOperation(value = "查询 案例基本信息", notes = "查询 案例基本信息")
    @RequestMapping(value = "/queryCaseBasicInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<MdCaseDesignInfo> queryCaseBasicInfo(@RequestBody MdCaseDesignInfo target)
    {
        MdCaseDesignInfo caseInfo = mdCaseDesignInfoService.findByPrimaryKey(target.getId());
        ReCaseDesignMenutree menuTree = reCaseDesignMenutreeService.findByPrimaryKey(caseInfo.getModuleId());
        if(menuTree!=null){
            caseInfo.setModuleName(menuTree.getMenuName());
        }
        return new ResultWrapper<MdCaseDesignInfo>().success(caseInfo);
    }

    /**
     * 分页查询 组件引用列表
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 组件引用列表", notes = "分页查询 组件引用列表")
    @RequestMapping(value = "/queryCaseListForComQuote", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<ReCaseDesignInfo>> queryCaseListForComQuote(@RequestBody QueryByPage<ReComponentInfo> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        ReComponentInfo comInfo = queryByPage.getQuery();
        Pagination<ReCaseDesignInfo> result = reCaseDesignInfoService.queryCaseListForComQuote(
                comInfo.toMap(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<ReCaseDesignInfo>>().success(result);
    }

    /**
     * 处理案例类型的多选问题
     * @param caseType
     * @return
     */
    private List<String> dealCaseType(String caseType){
        List<String> result = new ArrayList<String>();
        if(caseType.contains(",")){
            String[] arr = caseType.split(",");
            for (String str :arr){
                result.add(str);
            }
        }else{
            result.add(caseType);
        }
        return result;
    }
}
