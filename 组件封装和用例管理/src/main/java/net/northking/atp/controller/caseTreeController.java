package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.MdCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseDesignMenutree;
import net.northking.atp.db.service.MdCaseDesignInfoService;
import net.northking.atp.db.service.ReCaseDesignInfoService;
import net.northking.atp.db.service.ReCaseDesignMenutreeService;
import net.northking.atp.impl.CaseDesignServiceImpl;
import net.northking.atp.util.CaseDesignTools;
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
@Api(tags = {"案例设计"}, description = "案例设计涉及的案例树，案例的自定义操作")
@RequestMapping(value = "/caseDesign/CaseDesignMenutree")
public class caseTreeController {

    @Autowired
    private ReCaseDesignMenutreeService caseTreeService;

    @Autowired
    private CaseDesignServiceImpl caseDesignService;

    @Autowired
    private MdCaseDesignInfoService mdCaseDesignInfoService;

    /**
     * 新增 测试案例_菜单树表
     *
     * @param target 测试案例_菜单树表
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "测试案例_菜单树表_自定义插入", notes = "测试案例_菜单树表_自定义插入")
    @RequestMapping(value = "/insertCaseTree", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insertCaseTree(@RequestBody ReCaseDesignMenutree target){
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getMenuName()==null || "".equals(target.getMenuName())){
            return new ResultWrapper().fail("caseTree0005","请填写节点名");
        }
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("caseTree0001","请确认参数是否完整");
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

            //根据父级节点Id查询同层级是否有同名节点存在
            ReCaseDesignMenutree checkCase = new ReCaseDesignMenutree();
            checkCase.setTopMenuId(target.getTopMenuId());
            checkCase.setProjectId(target.getProjectId());
            checkCase.setMenuName(target.getMenuName());
            List<ReCaseDesignMenutree> checkList = caseTreeService.query(checkCase);
            if(checkList.size()>0){
                return new ResultWrapper().fail("caseTree0004","同层级中已存在改名称节点，请更换节点名称");
            }
        }else{
            ReCaseDesignMenutree caseDesign = new ReCaseDesignMenutree();
            caseDesign.setMenuId(target.getTopMenuId());
            caseDesign.setProjectId(target.getProjectId());
            ReCaseDesignMenutree father = caseTreeService.query(caseDesign).get(0);
            //查询父级节点是否有用例
            MdCaseDesignInfo info = new MdCaseDesignInfo();
            info.setMenuId(father.getMenuId());
            info.setProjectId(father.getProjectId());
            int count = (int)mdCaseDesignInfoService.queryCount(info);
            if(count>0){
                return new ResultWrapper().fail("caseTree0005","节点已存在用例,无法添加子级节点");
            }

            //根据父级节点Id查询同层级是否有同名节点存在
            ReCaseDesignMenutree checkCase = new ReCaseDesignMenutree();
            checkCase.setTopMenuId(target.getTopMenuId());
            checkCase.setProjectId(target.getProjectId());
            checkCase.setMenuName(target.getMenuName());
            List<ReCaseDesignMenutree> checkList = caseTreeService.query(checkCase);
            if(checkList.size()>0){
                return new ResultWrapper().fail("caseTree0004","同层级中已存在改名称节点，请更换节点名称");
            }
            //父级节点层级+1
            target.setMenuLevel(String.valueOf(Integer.parseInt(father.getMenuLevel())+1));
            //设置顺序
            String menuOrder = caseTreeService.queryMaxMenuOrder(target.toMap());
            if(menuOrder==null){
                target.setMenuOrder("1");
            }else{
                target.setMenuOrder(String.valueOf(Integer.parseInt(menuOrder)+1));
            }
            //此时将父级节点更改为非叶子节点
            ReCaseDesignMenutree caseDesignMenutree = new ReCaseDesignMenutree();
            caseDesignMenutree.setId(father.getId());
            caseDesignMenutree.setIsLeaf("0");
            if(caseTreeService.updateByPrimaryKey(caseDesignMenutree)<1){
                return new ResultWrapper().fail("caseDesignDelete0002","父节点信息修改失败");
            }
        }
        //插入menuId
        String topMenuId = target.getTopMenuId();
        String menuId = topMenuId+"_"+tools.generateMenuId();
        target.setMenuId(menuId);
        if(caseTreeService.insert(target)<1){
            return new ResultWrapper().fail("caseDesignDelete0003","数据插入失败");
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
    @RequestMapping(value = "/deleteCaseTree", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteCaseTree(@RequestParam("id") String id){
        if(id==null){
            return new ResultWrapper().fail("caseTree0001","请确认参数是否完整");
        }
        ReCaseDesignMenutree target = caseTreeService.findByPrimaryKey(id);
        if("0".equals(target.getIsLeaf())){
            return new ResultWrapper().fail("caseTree0006","非叶子节点无法删除，请先删除子节点");
        }
        //删除前确认是否一存在用例
        MdCaseDesignInfo info = new MdCaseDesignInfo();
        info.setMenuId(target.getMenuId());
        info.setProjectId(target.getProjectId());
        int count = (int)mdCaseDesignInfoService.queryCount(info);
        if(count>0){
            return new ResultWrapper().fail("caseTree0006","节点已存在用例,无法删除。请先删除用例");
        }

        //删除所有的当前节点以及子节点
        String menuId = target.getMenuId()+"%";
        target.setMenuId(menuId);
        boolean check = caseTreeService.deleteMenuByMenuIdForLike(target);
        if(check){
            //删除成功，重新判断上层节点是否为叶子节点
            if(!"top".equals(target.getTopMenuId())){
                ReCaseDesignMenutree cdm = new ReCaseDesignMenutree();
                cdm.setMenuId(target.getTopMenuId());
                cdm.setProjectId(target.getProjectId());
                String menuOrder = caseTreeService.queryMaxMenuOrder(target.toMap());
                if(menuOrder == null || "".equals(menuOrder)){
                    //此时将父级节点更改为叶子节点
                    ReCaseDesignMenutree fa = caseTreeService.query(cdm).get(0);
                    cdm = new ReCaseDesignMenutree();
                    cdm.setId(fa.getId());
                    cdm.setIsLeaf("1");
                    caseTreeService.updateByPrimaryKey(cdm);
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
        @RequestMapping(value = "/selectCaseTreeFroLike", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<Map<String,Object>>> selectCaseTreeFroLike(@RequestBody ReCaseDesignMenutree target){
        CaseDesignTools tools = new CaseDesignTools();
        ResultWrapper<List<Map<String,Object>>> resultWrapper = new ResultWrapper<List<Map<String,Object>>>();
        //查询所有节点
        ReCaseDesignMenutree query = new ReCaseDesignMenutree();
        query.setProjectId(target.getProjectId());
        //查询所有相关节点
        String menuName = target.getMenuName();
        query.setMenuName("%" + menuName + "%");
        List<ReCaseDesignMenutree> likeNodeList = caseTreeService.queryMenuForList(query);
        List<Map<String,Object>> resultList = new ArrayList<Map<String, Object>>();
        if(likeNodeList == null){
        } else if(likeNodeList.size()<2){
            resultList.add(likeNodeList.get(0).toMap());
        }else{
            List<ReCaseDesignMenutree> allNodeList = caseTreeService.queryAllMenuForOrder(query);
            List<Map<String,Object>> toolList = caseTreeService.queryMenuForListCount(query);
            resultList = caseDesignService.generateListForQuery(allNodeList,likeNodeList,toolList);
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

    /**
     *查询当前节点模块名
     * @param target
     * @return
     */
    @Transactional
    @ApiOperation(value = "菜单节点查询_模块名", notes = "菜单节点查询_模块名")
    @RequestMapping(value = "/queryCaseTreeModuleName", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper queryCaseTreeModuleName(@RequestBody ReCaseDesignMenutree target){
        if(target.getMenuId()==null){
            return new ResultWrapper().fail("demandTree0001","请确认参数是否完整");
        }
        String menuName = target.getMenuName();
        String id = target.getId();
        //查询当前节点对应的第二级节点为模块名_top默认隐藏，第二级为第三层
        String menuId = target.getMenuId();
        String[] menuStr = menuId.split("_");
        if(menuStr.length>2){
            String menuNo = menuStr[0]+"_"+menuStr[1]+"_"+menuStr[2];
            ReCaseDesignMenutree tree = new ReCaseDesignMenutree();
            tree.setProjectId(target.getProjectId());
            tree.setMenuId(menuNo);
            List<ReCaseDesignMenutree> treeList = caseTreeService.query(tree);
            if(treeList!=null&&treeList.size()>0){
                menuName = treeList.get(0).getMenuName();
                id = treeList.get(0).getId();
            }
        }
        target.setId(id);
        target.setMenuName(menuName);
        return new ResultWrapper().success(target);
    }

    /**
     * 查询项目下所有模块
     * @param target
     * @return
     */
    @Transactional
    @ApiOperation(value = "全量模块查询", notes = "全量模块查询")
    @RequestMapping(value = "/queryAllModuleInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper queryAllModuleInfo(@RequestBody ReCaseDesignMenutree target){
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("demandTree0001","请确认参数是否完整");
        }
        List<ReCaseDesignMenutree> result = caseTreeService.queryAllModuleInfo(target);
        return new ResultWrapper().success(result);
    }
}
