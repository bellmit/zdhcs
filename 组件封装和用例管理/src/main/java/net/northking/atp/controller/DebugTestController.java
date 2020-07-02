package net.northking.atp.controller;

import com.sun.corba.se.spi.ior.ObjectKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.CaseDesignFeignClient;
import net.northking.atp.TestExecFeignClient;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.*;
import net.northking.atp.impl.*;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2019/7/29 0029.
 */
@RestController
@Api(tags = {"调试测试"}, description = "调试")
@RequestMapping(value = "/debugTest")
public class DebugTestController {

    private static final Logger logger = LoggerFactory.getLogger(DebugTestController.class);

    @Autowired
    private TestExecFeignClient testExecFeignClient;
    @Autowired
    private InterfaceInfoModifyServiceImpl interfaceInfoModifyService;
    @Autowired
    private CaseDesignVersionServiceImpl caseDesignVersionService;
    @Autowired
    private ComponentInfoServiceImpl componentInfoService;
    @Autowired
    private CaseDesignModifyServiceImpl caseDesignModifyService;
    @Autowired
    private CaseStepComponentServiceImpl caseStepService;
    @Autowired
    private ReCaseSetService reCaseSetService;
    @Autowired
    private ReCaseSetLinkService reCaseSetLinkService;
    @Autowired
    private ComponentModifyServiceImpl componentModifyService;
    @Autowired
    private ReExecPlanService reExecPlanService;


    /**
     * 接口调试
     * @param info
     * @return
     */
    @ApiOperation(value = "接口调试", notes = "接口调试")
    @RequestMapping(value = "/doInterfaceDebug/{planId}",method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper doInterfaceDebug(@PathVariable String planId,@RequestBody InterfaceInterfaceInfo info) {
        logger.info("----->临时计划{}正在准备进行调试");
        String runProjectId = info.getProjectId();
        InterfaceCaseSetLink link = saveInterfaceDebugData(info);
        link.setRunProjectId(runProjectId);
        return testExecFeignClient.doInterfaceDebug(planId, link);
    }

    /**
     * 组件调试
     * @param info
     * @return
     */
    @ApiOperation(value = "组件调试", notes = "组件调试")
    @RequestMapping(value = "/doComponentDebug/{planId}",method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper doComponentDebug(@PathVariable String planId,@RequestBody InterfaceComponentInfo info) {
        logger.info("----->临时计划{}正在准备进行调试");
        String runProjectId = info.getProjectId();
        InterfaceCaseSetLink link = saveComponentDebugData(info);
        link.setRunProjectId(runProjectId);
        return testExecFeignClient.doInterfaceDebug(planId, link);
    }

    /**
     * 案例调试
     * @param info
     * @return
     */
    @ApiOperation(value = "案例调试", notes = "案例调试")
    @RequestMapping(value = "/doCaseDesignDebug/{planId}",method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper doCaseDesignDebug(@PathVariable String planId,@RequestBody InterfaceCaseInfo info) {
        logger.info("----->临时计划{}正在准备进行调试");
        String runProjectId = info.getProjectId();
        InterfaceCaseSetLink link = saveCaseDebugData(info);
        link.setRunProjectId(runProjectId);
        return testExecFeignClient.doInterfaceDebug(planId, link);
    }

    /**
     * 过往调试数据清理_一日前
     * @param info
     * @return
     */
    @Transactional
    @ApiOperation(value = "数据清理", notes = "数据清理")
    @RequestMapping(value = "/cleanDebugDataByTime",method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper cleanDebugDataByTime(@RequestBody InterfaceCaseInfo info) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        logger.info("=================清理日期:"+sdf.format(new Date()));
        Map<String,Object> map = new HashMap<String,Object>();
        List<String> list = Constant.tableList;
        String projectId= Constant.interDebug;
        /** 清理接口  **/
        //接口MD表
        int num = deleteDebugDataByDynA(map,list.get(15),list.get(14),"a.INTERFACE_ID","b.ID",projectId,"b.CREATE_TIME"); //封装
        logger.info("=================MD表__接口封装__清理数据:"+num+"条！");

        num = deleteDebugDataByDynB(map,list.get(14),projectId,"CREATE_TIME"); //信息
        logger.info("=================MD表__接口信息__清理数据:"+num+"条！");

        /** 清理组件  **/
        //组件MD表
        num = deleteDebugDataByDynA(map,list.get(1),list.get(0),"a.COMPONENT_ID","b.ID",projectId,"b.CREATE_TIME"); //封装
        logger.info("=================MD表__组件封装__清理数据:"+num+"条！");

        num = deleteDebugDataByDynA(map,list.get(2),list.get(0),"a.COMPONENT_ID","b.ID",projectId,"b.CREATE_TIME"); //参数
        logger.info("=================MD表__组件参数__清理数据:"+num+"条！");

        num = deleteDebugDataByDynB(map,list.get(0),projectId,"CREATE_TIME"); //信息
        logger.info("=================MD表__组件基础信息__清理数据:"+num+"条！");
        //组件RE表
        num = deleteDebugDataByDynA(map,list.get(4),list.get(3),"a.COMPONENT_ID","b.ID",projectId,"b.CREATE_TIME"); //封装
        logger.info("=================RE表__组件封装__清理数据:"+num+"条！");

        num = deleteDebugDataByDynA(map,list.get(5),list.get(3),"a.COMPONENT_ID","b.ID",projectId,"b.CREATE_TIME"); //参数
        logger.info("=================RE表__组件参数__清理数据:"+num+"条！");

        num = deleteDebugDataByDynB(map,list.get(3),projectId,"CREATE_TIME"); //信息
        logger.info("=================RE表__组件基础信息__清理数据:"+num+"条！");

        /** 清理案例  **/
        //案例MD表
        num = deleteDebugDataByDynA(map,list.get(7),list.get(6),"a.CASE_ID","b.ID",projectId,"b.CREATE_TIME"); //步骤
        logger.info("=================MD表__案例步骤__清理数据:"+num+"条！");

        num = deleteDebugDataByDynA(map,list.get(8),list.get(6),"a.CASE_ID","b.ID",projectId,"b.CREATE_TIME"); //步骤组件
        logger.info("=================MD表__步骤组件__清理数据:"+num+"条！");

        num = deleteDebugDataByDynA(map,list.get(9),list.get(6),"a.CASE_ID","b.ID",projectId,"b.CREATE_TIME"); //步骤参数
        logger.info("=================MD表__步骤参数__清理数据:"+num+"条！");

        num = deleteDebugDataByDynB(map,list.get(6),projectId,"CREATE_TIME"); //信息
        logger.info("=================MD表__案例信息__清理数据:"+num+"条！");

        //案例RE表
        num = deleteDebugDataByDynA(map,list.get(11),list.get(10),"a.CASE_ID","b.ID",projectId,"b.CREATE_TIME"); //步骤
        logger.info("=================RE表__案例步骤__清理数据:"+num+"条！");

        num = deleteDebugDataByDynA(map,list.get(12),list.get(10),"a.CASE_ID","b.ID",projectId,"b.CREATE_TIME"); //步骤组件
        logger.info("=================RE表__步骤组件__清理数据:"+num+"条！");

        num = deleteDebugDataByDynA(map,list.get(13),list.get(10),"a.CASE_ID","b.ID",projectId,"b.CREATE_TIME"); //步骤参数
        logger.info("=================RE表__步骤参数__清理数据:"+num+"条！");

        num = deleteDebugDataByDynB(map,list.get(10),projectId,"CREATE_TIME"); //信息
        logger.info("=================RE表__案例信息__清理数据:"+num+"条！");

        /** 清理案例集  **/
        num = deleteDebugDataByDynA(map,list.get(17),list.get(16),"a.SET_ID","b.ID",projectId,"b.MODIFY_TIME"); //封装
        logger.info("=================RE表__案例集__清理数据:"+num+"条！");

        num = deleteDebugDataByDynB(map,list.get(16),projectId,"MODIFY_TIME"); //信息
        logger.info("=================RE表__案例集关联__清理数据:"+num+"条！");

        return new ResultWrapper().success();
    }

    /**
     * 动态生成删除参数
     * @param map
     * @param tableA
     * @param tableB
     * @param left
     * @param right
     * @param projectId
     */
    private int deleteDebugDataByDynA(Map<String,Object> map,String tableA,String tableB,String left,String right,String projectId,String timeName){
        map = new HashMap<String,Object>();
        map.put("tableA",tableA);
        map.put("tableB",tableB);
        map.put("left",left);
        map.put("right",right);
        map.put("projectId",projectId);
        map.put("timeName",timeName);
        map.put("time",new Date());
        return reExecPlanService.deleteDebugDataForDynA(map);
    }
    /**
     * 动态生成删除参数
     * @param map
     * @param table
     * @param projectId
     */
    private int deleteDebugDataByDynB(Map<String,Object> map,String table,String projectId,String timeName){
        map = new HashMap<String,Object>();
        map.put("table",table);
        map.put("projectId",projectId);
        map.put("timeName",timeName);
        map.put("time",new Date());
        return reExecPlanService.deleteDebugDataForDynB(map);
    }

    /**
     * 保存调试数据_接口
     * @param target
     * @return
     */
    @Transactional
    public InterfaceCaseSetLink saveInterfaceDebugData(InterfaceInterfaceInfo target){
        CaseDesignTools tools = new CaseDesignTools();
        target.setId(null);
        target.setProjectId(Constant.interDebug);
        //接口数据生成高级组件
        MdInterfaceInfo insertInfo = target;
        if(insertInfo.getUrl() != null){
            insertInfo.setUrl(insertInfo.getUrl().trim());
        }
        interfaceInfoModifyService.insertInterfaceInfo(insertInfo);
        Map<String,Object> dataMap = target.getDataMap();
        interfaceInfoModifyService.saveInterfaceData(dataMap,insertInfo);
        Map<String,Object> reComInfoMap = interfaceInfoModifyService.genComInfoByInterface(target);
        componentInfoService.insertFormalComponentInfo(reComInfoMap);

        //生成案例
        Map<String,Object> toolMap = insertInfo.toMap();
        toolMap.put("name",toolMap.get("interfaceName"));
        InterfaceCaseInfo caseInfo = createTempCaseInfo(toolMap); //插入md信息
        caseDesignModifyService.insertModifyCaseInfo(caseInfo);
        MdCaseDesignInfo reCase = new MdCaseDesignInfo();
        reCase.setId(caseInfo.getId());
        reCase.setProjectId(target.getProjectId());
        Map<String,Object> caseMap = caseDesignModifyService.queryCaseInfoToMap(reCase); //生成re信息
        caseStepService.insertFormalCaseInfo(caseMap);//插入正式表

        //生成案例集
        ReCaseSet caseSet = createTempCaseSet(toolMap);
        caseSet.setId(tools.getUUID());
        caseSet.setExecuteNumber("0"); //初始执行0次
        caseSet.setSetStatus("0"); //初始状态为未完成
        caseSet.setCaseNumber("0");
        caseSet.setModifyTime(new Date());
        reCaseSetService.insert(caseSet);

        //关联案例集
        List<ReCaseSetLink> linkList = createTempSetCaseLink(caseInfo,caseSet);
        reCaseSetLinkService.insertByBatch(linkList);

        InterfaceCaseSetLink link = new InterfaceCaseSetLink();
        link.setId(caseSet.getId());
        link.setProjectId(caseSet.getProjectId());
        link.setLinkList(linkList);
        return link;
    }

    /**
     * 保存调试数据_组件
     * @param target
     * @return
     */
    @Transactional
    public InterfaceCaseSetLink saveComponentDebugData(InterfaceComponentInfo target){
        CaseDesignTools tools = new CaseDesignTools();
        target.setId(null);
        target.setProjectId(Constant.interDebug);
        //生成高级组件
        removeParamId(target);
        componentModifyService.insertComponentModifyInfo(target);
        MdComponentInfo mdInfo = new MdComponentInfo();
        mdInfo.setId(target.getId());
        Map<String,Object> comMap = componentModifyService.queryComponentToMap(mdInfo);
        componentInfoService.insertFormalComponentInfo(comMap);

        //生成案例
        ReComponentInfo comInfo = target;
        Map<String,Object> toolMap = comInfo.toMap();
        toolMap.put("name",toolMap.get("componentName"));
        InterfaceCaseInfo caseInfo = createTempCaseInfo(toolMap); //插入md信息
        caseDesignModifyService.insertModifyCaseInfo(caseInfo);
        MdCaseDesignInfo reCase = new MdCaseDesignInfo();
        reCase.setId(caseInfo.getId());
        reCase.setProjectId(target.getProjectId());
        Map<String,Object> caseMap = caseDesignModifyService.queryCaseInfoToMap(reCase); //生成re信息
        caseStepService.insertFormalCaseInfo(caseMap);//插入正式表

        //生成案例集
        ReCaseSet caseSet = createTempCaseSet(toolMap);
        caseSet.setId(tools.getUUID());
        caseSet.setExecuteNumber("0"); //初始执行0次
        caseSet.setSetStatus("0"); //初始状态为未完成
        caseSet.setCaseNumber("0");
        caseSet.setModifyTime(new Date());
        reCaseSetService.insert(caseSet);

        //关联案例集
        List<ReCaseSetLink> linkList = createTempSetCaseLink(caseInfo,caseSet);
        reCaseSetLinkService.insertByBatch(linkList);

        InterfaceCaseSetLink link = new InterfaceCaseSetLink();
        link.setId(caseSet.getId());
        link.setProjectId(caseSet.getProjectId());
        link.setLinkList(linkList);
        return link;
    }

    /**
     * 保存调试数据_案例
     * @param target
     * @return
     */
    @Transactional
    public InterfaceCaseSetLink saveCaseDebugData(InterfaceCaseInfo target){
        CaseDesignTools tools = new CaseDesignTools();
        target.setId(null);
        target.setProjectId(Constant.interDebug);
        //生成案例
        caseDesignModifyService.insertModifyCaseInfo(target);
        MdCaseDesignInfo mdInfo = new MdCaseDesignInfo();
        mdInfo.setId(target.getId());
        mdInfo.setProjectId(target.getProjectId());
        Map<String,Object> caseMap = caseDesignModifyService.queryCaseInfoToMap(mdInfo);
        caseStepService.insertFormalCaseInfo(caseMap);

        //生成案例集
        ReCaseSet caseSet = createTempCaseSet(mdInfo.toMap());
        caseSet.setId(tools.getUUID());
        caseSet.setExecuteNumber("0"); //初始执行0次
        caseSet.setSetStatus("0"); //初始状态为未完成
        caseSet.setCaseNumber("0");
        caseSet.setModifyTime(new Date());
        reCaseSetService.insert(caseSet);

        //关联案例集
        List<ReCaseSetLink> linkList = createTempSetCaseLink(target,caseSet);
        reCaseSetLinkService.insertByBatch(linkList);

        InterfaceCaseSetLink link = new InterfaceCaseSetLink();
        link.setId(caseSet.getId());
        link.setProjectId(caseSet.getProjectId());
        link.setLinkList(linkList);
        return link;
    }



    /**
     * 创建临时用例
     * @param info
     * @return
     */
    private InterfaceCaseInfo createTempCaseInfo(Map<String,Object> info) {
        InterfaceCaseInfo interfaceCaseInfo = new InterfaceCaseInfo();
        interfaceCaseInfo.setCaseName("临时案例" + info.get("id"));
        interfaceCaseInfo.setProjectId(info.get("projectId")+"");
        List<InterfaceStepComponent> componentList = new ArrayList<>();
        InterfaceStepComponent stepComponent = new InterfaceStepComponent();
        stepComponent.setProjectId(info.get("projectId")+"");
        stepComponent.setComponentFlag("3");     // 3
        stepComponent.setComponentName(info.get("name")+"");
        stepComponent.setId(info.get("id")+"");                 // 组件id
        stepComponent.setVersion(info.get("version")+"");     // 版本
        stepComponent.setParamList(new ArrayList<ReComponentParameter>());  // 参数列表
        stepComponent.setStaffName(info.get("createStaff")+"");
        componentList.add(stepComponent);   // 加入到组件列表
        interfaceCaseInfo.setComponentList(componentList);
        return interfaceCaseInfo;
    }

    /**
     * 创建临时用例集
     *
     * @param map 用例信息
     * @return 返回信息
     */
    private ReCaseSet createTempCaseSet(Map<String,Object> map) {
        ReCaseSet caseSet = new ReCaseSet();
        caseSet.setProjectId(map.get("projectId")+"");
        caseSet.setSetName("临时用例集" + map.get("id"));
        caseSet.setDescription("临时用例集");
        return caseSet;
    }

    /**
     * 创建临时用例-用例关联关系
     *
     * @param interCaseInfo 临时用例信息
     * @param caseSet       临时用例集信息
     * @return 返回信息
     */
    private List<ReCaseSetLink> createTempSetCaseLink(InterfaceCaseInfo interCaseInfo, ReCaseSet caseSet) {
        List<ReCaseSetLink> linkList = new ArrayList<ReCaseSetLink>();
        ReCaseSetLink reCaseSetLink = new ReCaseSetLink();
        reCaseSetLink.setId(UUID.randomUUID().toString().replace("-", ""));
        reCaseSetLink.setSetId(caseSet.getId());
        reCaseSetLink.setCaseId(interCaseInfo.getId());
        reCaseSetLink.setLinkOrder("001");
        reCaseSetLink.setProjectId(interCaseInfo.getProjectId());
        linkList.add(reCaseSetLink);
        return linkList;
    }

    /**
     * 去掉参数id
     * @param target
     */
    private void removeParamId(InterfaceComponentInfo target){
        CaseDesignTools tools = new CaseDesignTools();
        List<InterfaceComponentPackage> packList = target.getPackageList();
        if(packList != null && packList.size()>0){
            for (InterfaceComponentPackage pack: packList){
                List<ReComponentParameter> paramList = pack.getParamList();
                if(paramList != null && paramList.size()>0){
                    for(ReComponentParameter param : paramList){
                        param.setId(tools.getUUID());
                    }
                }
            }
        }
        List<ReComponentParameter> paramList = target.getParamList();
        if(paramList != null && paramList.size()>0){
            for(ReComponentParameter param : paramList){
                param.setId(tools.getUUID());
            }
        }
    }

//
//
//
//    @RequestMapping(value = "/doInterfaceDebug_01/{planId}",method = {RequestMethod.POST},
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
//            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public ResultWrapper doInterfaceDebug_01(@PathVariable String planId, @RequestBody InterfaceInterfaceInfo target) {
//        logger.info("----->临时计划{}正在准备进行调试", planId);
//        // todo: 创建临时高级组件
//        ResultWrapper<MdInterfaceInfo> interInfoWrapper = caseDesignFeignClient.interfaceInfoSaveAndCommitForDebug(target);
//        if (!interInfoWrapper.isSuccess()) {
//            return interInfoWrapper;
//        }
//        // todo: 创建临时用例和版本
//        ResultWrapper caseWrapper = createTempCaseInfo(target);
//        if (!caseWrapper.isSuccess()) {
//            return caseWrapper;
//        }
////        InterfaceCaseInfo interCaseInfo = new InterfaceCaseInfo();
////        BeanUtil.mapToBean((Map) caseWrapper.getData(), interCaseInfo);
//        InterfaceCaseInfo interCaseInfo = (InterfaceCaseInfo) caseWrapper.getData();
//
//        // todo: 创建临时用例集
//        ResultWrapper caseSetWrapper = createTempCaseSet(target);
//        if (!caseSetWrapper.isSuccess()) {
//            return caseSetWrapper;
//        }
////        ReCaseSet caseSet = new ReCaseSet();
////        BeanUtil.mapToBean((Map) caseSetWrapper.getData(), caseSet);
//        ReCaseSet caseSet = (ReCaseSet) caseSetWrapper.getData();
//
//        // todo: 创建用例-用例集临时关联
//        ResultWrapper setCaseRelWrapper = createTempSetCaseLink(interCaseInfo, caseSet);
//        if (!setCaseRelWrapper.isSuccess()) {
//            return setCaseRelWrapper;
//        }
////        InterfaceCaseSetLink caseSetLink = new InterfaceCaseSetLink();
////        BeanUtil.mapToBean((Map) setCaseRelWrapper.getData(), caseSetLink);
//        InterfaceCaseSetLink caseSetLink = (InterfaceCaseSetLink) setCaseRelWrapper.getData();
//        caseSetLink.setProjectId(target.getProjectId());
//        // todo: 调用测试执行的调试接口
//        return testExecFeignClient.doInterfaceDebug(planId, caseSetLink);
//    }
//
}
