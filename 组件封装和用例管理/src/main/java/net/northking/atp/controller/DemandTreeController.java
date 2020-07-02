package net.northking.atp.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReCaseDemandTree;
import net.northking.atp.db.service.ReCaseDemandTreeService;
import net.northking.atp.impl.CaseDemandServiceImpl;
import net.northking.atp.util.CaseDesignTools;
import net.northking.db.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 案例菜单树的维护
 * Created by Administrator on 2019/3/5 0005.
 */
@RestController
@Api(tags = {"需求设计"}, description = "测试需求相关的接口_需求树")
@RequestMapping(value = "/caseDesign/caseDemandTree")
public class DemandTreeController {

    @Autowired
    private ReCaseDemandTreeService caseDemandTreeService;
    @Autowired
    private CaseDemandServiceImpl caseDamandService;
    /**
     * 新增 测试案例_菜单树表
     *
     * @param target 测试案例_菜单树表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "测试案例_菜单树表_自定义插入", notes = "测试案例_菜单树表_自定义插入")
    @RequestMapping(value = "/insertDemandTree", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insertDemandTree(@RequestBody ReCaseDemandTree target){
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("componentInfo0001","请确认参数是否完整");
        }
        //插入公有数据
        target.setId(UUID.randomUUID().toString().replace("-", ""));//主键
        target.setIsValid("1");
        target.setIsLeaf("1");//插入节点标志-1（新节点必为叶子节点）

        //查询父级节点
        if(target.getTopMenuId()==null){
            //顶层菜单
            target.setTopMenuId("top");//默认顶层id为"top"
            target.setMenuLevel("1");
            target.setMenuOrder("1");
        }else{
            ReCaseDemandTree caseDemand = new ReCaseDemandTree();
            caseDemand.setMenuId(target.getTopMenuId());
            caseDemand.setProjectId(target.getProjectId());
            ReCaseDemandTree father = caseDemandTreeService.query(caseDemand).get(0);
            //父级节点层级+1
            target.setMenuLevel(String.valueOf(Integer.parseInt(father.getMenuLevel())+1));
            //设置顺序
            String menuOrder = caseDemandTreeService.queryMaxMenuOrder(target.toMap());
            if(menuOrder==null){
                target.setMenuOrder("1");
            }else{
                target.setMenuOrder(String.valueOf(Integer.parseInt(menuOrder)+1));
            }
            //此时将父级节点更改为非叶子节点
            ReCaseDemandTree caseDemandTree = new ReCaseDemandTree();
            caseDemandTree.setId(father.getId());
            caseDemandTree.setIsLeaf("0");
            if(caseDemandTreeService.updateByPrimaryKey(caseDemandTree)<1){
                return new ResultWrapper().fail("caseDemandDelete0002","父节点信息修改失败");
            }
        }
        //插入menuId
        String topMenuId = target.getTopMenuId();
        String menuId = topMenuId+"_"+tools.generateMenuId();
        target.setMenuId(menuId);
        if(caseDemandTreeService.insert(target)<1){
            return new ResultWrapper().fail("caseDemandDelete0003","数据插入失败");
        }
        return new ResultWrapper().success();
    }


    /**
     * 删除 测试案例_菜单树表
     *
     * @param id 节点主键
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "测试案例_菜单树表_自定义删除", notes = "测试案例_菜单树表_自定义删除")
    @RequestMapping(value = "/deleteDemandTree", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteDemandTree(@RequestParam("id") String id){
        if(id==null){
            return new ResultWrapper().fail("demandTree0001","请确认参数是否完整");
        }
        ReCaseDemandTree target = caseDemandTreeService.findByPrimaryKey(id);
        //删除所有的当前节点以及子节点
        String menuId = target.getMenuId()+"%";
        target.setMenuId(menuId);
        boolean check = caseDemandTreeService.deleteMenuByMenuIdForLike(target);
        if(check){
            //删除成功，重新判断上层节点是否为叶子节点
            if(!"top".equals(target.getTopMenuId())){
                ReCaseDemandTree cdt = new ReCaseDemandTree();
                cdt.setMenuId(target.getTopMenuId());
                cdt.setProjectId(target.getProjectId());
                String menuOrder = caseDemandTreeService.queryMaxMenuOrder(target.toMap());
                if(menuOrder == null || "".equals(menuOrder)){
                    //此时将父级节点更改为叶子节点
                    ReCaseDemandTree fa = caseDemandTreeService.query(cdt).get(0);
                    cdt = new ReCaseDemandTree();
                    cdt.setId(fa.getId());
                    cdt.setIsLeaf("1");
                    caseDemandTreeService.updateByPrimaryKey(cdt);
                }
            }
            return new ResultWrapper().success();
        }else{
            return new ResultWrapper().fail("caseDesignDelete0001","数据库删除失败");
        }

    }

    /**
     * 模糊查询 测试案例_菜单树表
     *
     * @param target 测试案例_菜单树表
     * @return 接口返回
     */
    @ApiOperation(value = "测试案例_菜单树表_自定义查询", notes = "测试案例_菜单树表_自定义查询")
    @RequestMapping(value = "/selectDemandTreeFroLike", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<Map<String,Object>>> selectDemandTreeFroLike(@RequestBody ReCaseDemandTree target){
        CaseDesignTools tools = new CaseDesignTools();
        ResultWrapper<List<Map<String,Object>>> resultWrapper = new ResultWrapper<List<Map<String,Object>>>();
        //查询所有节点
        ReCaseDemandTree query = new ReCaseDemandTree();
        query.setProjectId(target.getProjectId());
        //查询所有相关节点
        String menuName = target.getMenuName();
        query.setMenuName("%" + menuName + "%");
        List<ReCaseDemandTree> likeNodeList = caseDemandTreeService.queryMenuForList(query);
        List<Map<String,Object>> resultList = new ArrayList<Map<String, Object>>();
        if(likeNodeList == null){
        } else if(likeNodeList.size()<2){
            resultList.add(likeNodeList.get(0).toMap());
        }else{
            List<ReCaseDemandTree> allNodeList = caseDemandTreeService.queryAllMenuForOrder(query);
            List<Map<String,Object>> toolList = caseDemandTreeService.queryMenuForListCount(query);
            resultList = caseDamandService.generateListForQuery(allNodeList,likeNodeList,toolList);
        }
        System.out.println("返回结果："+resultList);
        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        for(Map<String,Object> result : resultList){
            if(result.containsKey("childList") || (result.get("menuName")+"").indexOf(menuName)!=-1){
                //过滤掉此顶级菜单
                list.add(result);
            }
        }
        return resultWrapper.success(list);
    }
}
