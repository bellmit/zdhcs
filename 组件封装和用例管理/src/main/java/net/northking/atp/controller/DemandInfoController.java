package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReCaseDemandInfo;
import net.northking.atp.db.persistent.ReCaseDemandLink;
import net.northking.atp.db.service.ReCaseDemandInfoService;
import net.northking.atp.db.service.ReCaseDemandLinkService;
import net.northking.atp.entity.InterfaceDemandLink;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import net.northking.atp.util.RedisTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by Administrator on 2019/3/14 0014.
 */
@RestController
@Api(tags = {"需求设计"}, description = "测试需求相关的接口_需求信息维护")
@RequestMapping(value = "/caseDesign/demandInfo")
public class DemandInfoController {
    @Autowired
    private ReCaseDemandInfoService caseDemandInfoService;

    @Autowired
    private ReCaseDemandLinkService caseDemandLinkService;
    @Autowired
    private RedisTools redisTools;
    /**
     * 分页查询 需求信息表
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 需求表", notes = "分页查询 需求信息表")
    @RequestMapping(value = "/queryByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<ReCaseDemandInfo>> queryByPage(@RequestBody QueryByPage<ReCaseDemandInfo> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        ReCaseDemandInfo caseDemandInfo = queryByPage.getQuery();
        if(caseDemandInfo.getDemandNo()!=null){
            String demandNo= "%"+caseDemandInfo.getDemandNo()+"%";
            caseDemandInfo.setDemandName(demandNo);
        }
        if(caseDemandInfo.getDemandName()!=null){
            String demandName = "%"+caseDemandInfo.getDemandName()+"%";
            caseDemandInfo.setDemandName(demandName);
        }
        Pagination<ReCaseDemandInfo> result = caseDemandInfoService.selectTreeDemandInfo(
                caseDemandInfo, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<ReCaseDemandInfo>>().success(result);
    }

    /**
     * 新增 测试需求
     *
     * @param target 测试案例需求表
     * @return 接口返回
     */
    @ApiOperation(value = "测试需求表 自定义新增", notes = "测试需求表 自定义新增")
    @RequestMapping(value = "/insertDemandInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<ReCaseDemandInfo> insertDemandInfo(@RequestHeader(name = "Authorization") String authorization, @RequestBody ReCaseDemandInfo target)
    {
        CaseDesignTools tools = new CaseDesignTools();
        target.setId(UUID.randomUUID().toString().replace("-", ""));
        String demandNo = "TR-"+target.getProjectId()+"-"+tools.generateBusinessNo();
        target.setDemandNo(demandNo);
        target.setCreateStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        target.setCreateTime(new Date());
        caseDemandInfoService.insert(target);
        return new ResultWrapper<ReCaseDemandInfo>().success(target);
    }

    /**
     *  批量插入需求案例关联信息
     * @param target 插入的批量list
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "案例需求关联 关联表", notes = "案例需求关联 关联表")
    @RequestMapping(value = "/insertDemandLinkBatch", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insertDemandLinkBatch(@RequestBody InterfaceDemandLink target){
        if(target.getDemandId()==null || target.getProjectId()==null){
            return new ResultWrapper().fail("demandLink0001","请确认参数是否完整");
        }
        //先批量清除
        caseDemandLinkService.deleteLinkBatch(target.toMap());
        List<Map<String,Object>> linkList =target.getLinkList();
        if(linkList != null && linkList.size()>0){
            List<ReCaseDemandLink> inset = new ArrayList<ReCaseDemandLink>();
            int order = 1;
            for(Map<String,Object> map : linkList){
                ReCaseDemandLink caseDemandLink = new ReCaseDemandLink();
                caseDemandLink.setId(UUID.randomUUID().toString().replace("-", ""));
                caseDemandLink.setProjectId(target.getProjectId());
                caseDemandLink.setLinkOrder(String.valueOf(order));
                caseDemandLink.setDemandId(target.getDemandId());
                caseDemandLink.setCaseId(map.get("caseId")+"");
                order++;
                inset.add(caseDemandLink);
            }
            caseDemandLinkService.insertByBatch(inset);
        }
        return new ResultWrapper().success();
    }
}
