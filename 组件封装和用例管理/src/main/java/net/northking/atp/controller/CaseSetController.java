package net.northking.atp.controller;

import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseSet;
import net.northking.atp.db.persistent.ReCaseSetLink;
import net.northking.atp.db.service.ReCaseDesignInfoService;
import net.northking.atp.db.service.ReCaseSetLinkService;
import net.northking.atp.db.service.ReCaseSetService;
import net.northking.atp.entity.InterfaceCaseInfo;
import net.northking.atp.entity.InterfaceCaseSetLink;
import net.northking.atp.entity.InterfaceSetLinkInfo;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.RedisUtil;
import net.northking.atp.utils.BeanUtil;
import net.northking.db.DefaultPagination;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用例集维护
 * Created by Administrator on 2019/4/19 0019.
 */
@RestController
@Api(tags = {"用例集"}, description = "用例集设计维护")
@RequestMapping(value = "/caseSet")
public class CaseSetController {
    @Autowired
    private ReCaseSetService reCaseSetService;
    @Autowired
    private ReCaseDesignInfoService reCaseDesignInfoService;
    @Autowired
    private ReCaseSetLinkService reCaseSetLinkService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 新增 用例集列表
     *
     * @param target  用例集列表
     * @return 接口返回
     */
    @ApiOperation(value = "用例集列表  自定义新增", notes = "用例集列表  自定义新增")
    @RequestMapping(value = "/setList/insertCaseSet", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insertCaseSet(@RequestBody ReCaseSet target)
    {
        //重名校验
        ReCaseSet check = new ReCaseSet();
        check.setProjectId(target.getProjectId());
        check.setSetName(target.getSetName());
        List<ReCaseSet> checkList = reCaseSetService.query(check);
        if(checkList.size() > 0){
            return new ResultWrapper().fail("caseSet0000001","案例集已存在，请查找更新或更换案例集名称");
        }
        CaseDesignTools tools = new CaseDesignTools();
        target.setId(UUID.randomUUID().toString().replace("-", ""));
        String setNo = "TCSET-"+tools.generateBusinessNo();
        target.setSetNo(setNo);
        target.setModifyTime(new Date());
        target.setExecuteNumber("0"); //初始执行0次
        target.setSetStatus("0"); //初始状态为未完成
        target.setCaseNumber("0");
        reCaseSetService.insert(target);
        return new ResultWrapper().success(target);
    }



    /**
     * 分页查询 用例集列表
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 用例集列表", notes = "分页查询 用例集列表")
    @RequestMapping(value = "/setList/querySetListByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<ReCaseSet>> querySetListByPage(@RequestBody QueryByPage<ReCaseSet> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        ReCaseSet reCaseSet = queryByPage.getQuery();
        //模糊查询字段
        if(reCaseSet.getSetNo() != null){
            reCaseSet.setSetNo("%"+reCaseSet.getSetNo()+"%");
        }
        if(reCaseSet.getSetName() != null){
            reCaseSet.setSetName("%"+reCaseSet.getSetName()+"%");
        }
        Pagination<ReCaseSet> result = reCaseSetService.selectCaseSetInfo(reCaseSet, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<ReCaseSet>>().success(result);
    }

    /**
     *  批量插入用例集关联信息
     * @param target 插入的批量list
     * @return 接口返回
     */
    @ApiOperation(value = "用例集案例关联 关联表", notes = "用例集案例关联 关联表")
    @RequestMapping(value = "/setLink/insetCaseSetLinkBatch", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insetCaseSetLinkBatch(@RequestBody InterfaceCaseSetLink target){
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getId()==null || target.getProjectId()==null){
            return new ResultWrapper().fail("caseSet0001","请确认参数是否完整");
        }
        Gson gson = new Gson();
        //批量删除后进行新增
        int order = 1;
        List<InterfaceSetLinkInfo> linkList = target.getInterLinkList();
        List<ReCaseSetLink> insertList = new ArrayList<ReCaseSetLink>();
        if(linkList != null && linkList.size()>0){
            for(InterfaceSetLinkInfo link : linkList){
                String caseId = link.getId();
                link.setId(UUID.randomUUID().toString().replace("-", ""));
                link.setSetId(target.getId());
                link.setCaseId(caseId);
                link.setModifyTime(new Date());
                link.setLinkOrder(String.valueOf(order));
                link.setProjectId(target.getProjectId());
                if("1".equals(target.getSetFlag())){
                    Map<String,String> json = new HashMap<String,String>();
                    json.put("createStaff",link.getCreateStaff());
                    json.put("createTime",link.getCreateTime());
                    json.put("version",link.getVersion());
                    link.setCaseJson(gson.toJson(json));

                }
                insertList.add(link);
            }
            reCaseSetLinkService.insertByBatch(insertList);
        }
        //插入批量后更新案例集信息的关联案例数量
        ReCaseSetLink linkQuery = new ReCaseSetLink();
        linkQuery.setProjectId(target.getProjectId());
        linkQuery.setSetId(target.getId());
        List<ReCaseSetLink> newList = reCaseSetLinkService.query(linkQuery);
        ReCaseSet caseSet = new ReCaseSet();
        caseSet.setId(target.getId());
        caseSet.setCaseNumber(newList.size()+"");
        reCaseSetService.updateByPrimaryKey(caseSet);
        return new ResultWrapper().success(caseSet);
    }

    /**
     * 删除 用例集
     *
     * @param target  用例集列表
     * @return 接口返回
     */
    @ApiOperation(value = "用例集  删除信息及关联", notes = "用例集  删除信息及关联")
    @RequestMapping(value = "/setList/deleteCaseSet", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteCaseSet(@RequestBody ReCaseSet target)
    {
        if(target.getId()==null){
            return new ResultWrapper().fail("caseSet0001","请确认参数是否完整");
        }
        reCaseSetService.deleteByPrimaryKey(target.getId());
        //删除案例集关联
        ReCaseSetLink delete = new ReCaseSetLink();
        delete.setSetId(target.getId());
        reCaseSetLinkService.deleteByExample(delete);
        return new ResultWrapper().success(target);
    }

    /**
     * 关联列表查询 用例集列表
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 用例集关联列表", notes = "分页查询 用例集关联列表")
    @RequestMapping(value = "/setList/querySetLinkByPag", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<InterfaceCaseInfo>> querySetLinkByPag(@RequestBody QueryByPage<InterfaceCaseInfo> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        InterfaceCaseInfo caseInfo = queryByPage.getQuery();
        ReCaseSet setInfo = reCaseSetService.findByPrimaryKey(caseInfo.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Gson gson = new Gson();
        Pagination<InterfaceCaseInfo> result = new DefaultPagination<>();
        if("1".equals(setInfo.getSetFlag())){
            ReCaseSetLink query = new ReCaseSetLink();
            query.setProjectId(caseInfo.getProjectId());
            query.setSetId(caseInfo.getId());
            query.setCaseNo(caseInfo.getCaseNo());
            query.setCaseName(caseInfo.getCaseName());
            //数据测试
            Pagination<ReCaseSetLink> pageList = reCaseSetLinkService.selectCaseLinkByPage(
                    query, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize()
            );
            List<InterfaceCaseInfo> records = new ArrayList<InterfaceCaseInfo>();
            for (ReCaseSetLink link : pageList.getRecords()) {
                InterfaceCaseInfo info = new InterfaceCaseInfo();
                BeanUtils.copyProperties(link,info);
                Map<String,String> map = new HashMap<String,String>();
                map = gson.fromJson(link.getCaseJson(),map.getClass());
                info.setId(link.getId());
                info.setCaseId(link.getCaseId());
                info.setCreateStaff(redisUtil.get("username:" +map.get("createStaff"))+"");
                info.setVersion(map.get("version"));
                try {
                    info.setCreateTime(sdf.parse(map.get("createTime")+""));
                } catch (ParseException e) {
                    info.setCreateTime(new Date());
                }
                records.add(info);
            }
            pageList.setRecords(null);
            BeanUtils.copyProperties(pageList,result);
            result.setRecords(records);
        }else{
            //自动化
            Map<String,Object> query = new HashMap<String,Object>();
            query.put("projectId",caseInfo.getProjectId());
            query.put("setId",caseInfo.getId());
            if(caseInfo.getCaseNo()!=null){
                String caseNo= "%"+caseInfo.getCaseNo()+"%";
                query.put("caseNo",caseNo);
            }
            if(caseInfo.getCaseName()!=null){
                String caseName= "%"+caseInfo.getCaseName()+"%";
                query.put("caseName",caseName);
            }
            result = reCaseDesignInfoService.selectCaseLinkForSet(
                    query, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        }
        return new ResultWrapper<Pagination<InterfaceCaseInfo>>().success(result);
    }

    /**
     *  删除案例集关联
     * @param target 删除的批量list
     * @return 接口返回
     */
    @ApiOperation(value = "用例集案例关联 删除删除关联", notes = "用例集案例关联 删除关联")
    @RequestMapping(value = "/setLink/deleteCaseSetLinkBatch", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteCaseSetLinkBatch(@RequestBody InterfaceCaseSetLink target){
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getId()==null || target.getProjectId()==null){
            return new ResultWrapper().fail("caseSet0001","请确认参数是否完整");
        }
        ReCaseSet caseSet = new ReCaseSet();
        if(target.getLinkList() != null && target.getLinkList().size()>0){
            String[] ids = new String[target.getLinkList().size()];
            int i = 0;
            for(ReCaseSetLink link : target.getLinkList()){
                ids[i] = link.getId();
                i++;
            }
            reCaseSetLinkService.deleteByPrimaryKeys(ids);

            //更新案例集的关联案例数量
            ReCaseSetLink linkQuery = new ReCaseSetLink();
            linkQuery.setProjectId(target.getProjectId());
            linkQuery.setSetId(target.getId());
            List<ReCaseSetLink> newList = reCaseSetLinkService.query(linkQuery);
            caseSet.setId(target.getId());
            caseSet.setCaseNumber(newList.size()+"");
            reCaseSetService.updateByPrimaryKey(caseSet);
        }
        return new ResultWrapper().success(caseSet);
    }

    @ApiOperation(value = "根据测试计划查询计划下所有的案例集", notes = "根据测试计划查询计划下所有的案例集")
    @RequestMapping(value = "/setLink/queryAllCaseSetsByPlanId", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<ReCaseSet>> queryAllCaseSetsByPlanId(@RequestParam("planId") String planId) {
        List<ReCaseSet> caseSets = reCaseSetService.queryAllCaseSetsByPlanId(planId);
        ResultWrapper<List<ReCaseSet>> resultWrapper = new ResultWrapper<>();
        if (caseSets != null) {
            resultWrapper.success(caseSets);
        } else {
            resultWrapper.fail("00000001", "没有可查找的案例集");
        }
        return resultWrapper;
    }

    @ApiOperation(value = "根据用例集ID查询用例集下所有的用例", notes = "根据用例集ID查询用例集下所有的用例")
    @RequestMapping(value = "/setLink/queryAllCasesBySet", method = {RequestMethod.POST},
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
    consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<InterfaceCaseInfo>> queryAllCasesBySetId(@RequestBody QueryByPage<InterfaceCaseInfo> queryByPage) {
        ResultWrapper<Pagination<InterfaceCaseInfo>> pageWrapper = querySetLinkByPag(queryByPage);
        if (pageWrapper != null && pageWrapper.getData() != null && pageWrapper.getData().getRecords() != null) {
            List<InterfaceCaseInfo> records = pageWrapper.getData().getRecords();
            return new ResultWrapper<List<InterfaceCaseInfo>>().success(records);
        }
        return new ResultWrapper<List<InterfaceCaseInfo>>().fail("00000002", "没有找到的关联用例信息");
    }

}
