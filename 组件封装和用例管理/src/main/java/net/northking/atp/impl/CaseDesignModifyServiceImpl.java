package net.northking.atp.impl;

import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceCaseInfo;
import net.northking.atp.entity.InterfaceCaseStep;
import net.northking.atp.entity.InterfaceComAndCaseCopy;
import net.northking.atp.entity.InterfaceStepComponent;
import net.northking.atp.service.CaseDesignModifyService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 测试案例修改表服务
 * Created by Administrator on 2019/6/10 0010.
 */
@Service
public class CaseDesignModifyServiceImpl implements CaseDesignModifyService{
    @Autowired
    private MdCaseDesignInfoService mdCaseDesignInfoService;
    @Autowired
    private MdCaseStepService mdCaseStepService;
    @Autowired
    private MdComponentStepService mdComponentStepService;
    @Autowired
    private MdStepParameterService mdStepParameterService;
    @Autowired
    private CaseDesignModifyServiceImpl caseDesignModifyService;
    @Autowired
    private ComponentInfoServiceImpl componentInfoService;
    @Autowired
    private MdComponentParameterService mdComponentParameterService;
    @Autowired
    private ReComponentParameterService reComponentParameterService;
    @Autowired
    private HisComponentInfoService hisComponentInfoService;
    @Autowired
    private ComponentHisInfoServiceImpl componentHisInfoService;
    @Autowired
    private ReCaseDesignInfoService reCaseDesignInfoService;
    @Autowired
    private ReCaseDesignMenutreeService reCaseDesignMenutreeService;
    @Autowired
    private ReComponentLibraryService reComponentLibraryService;

    //编号生成集合
    public static Map<String,String> caseNoMap = new HashMap<String, String>();

    /**
     * 新增案例信息_修改表
     * @param target
     */
    @Override
    public void insertModifyCaseInfo(InterfaceCaseInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        target.setId(UUID.randomUUID().toString().replace("-", ""));
        String caseNo = getNewCaseNo(target.getProjectId());

        target.setCaseNo(caseNo);
        target.setCreateTime(new Date());
        //target.setCreateStaff(Constant.staff);
        //新增直接将修改人写为创建人（为了统计数据_已修改人为准）
        target.setModifyTime(new Date());
        //target.setModifyStaff(Constant.staff);
        target.setVersion("");
        mdCaseDesignInfoService.insertInfoForMap(target.toMap());
        //步骤数据
        List<Map<String,Object>> caseStepList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> compStepList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> stepParamList = new ArrayList<Map<String,Object>>();
        target.setCaseId(target.getId());
        analysisCaseInfo(target,caseStepList,compStepList,stepParamList);
        mdCaseStepService.insertBatchForMap(caseStepList);
        mdComponentStepService.insertBatchForMap(compStepList);
        mdStepParameterService.insertBatchForMap(stepParamList);

    }

    /**
     * 修改案例信息表_修改表
     * @param target
     */
    @Override
    public void updateModifyCaseInfo(InterfaceCaseInfo target) {
        MdCaseDesignInfo old = mdCaseDesignInfoService.findByPrimaryKey(target.getId());

        //删除原数据
        caseDesignModifyService.deleteModifyCaseInfo(target.getId(),target.getProjectId());
        //生成新数据
        String version = old.getVersion();
        target.setModifyTime(new Date());
        target.setVersion(version);

        Map<String,Object> info = old.toMap();
        info.putAll(target.toMap());
        mdCaseDesignInfoService.insertInfoForMap(info);
        //插入数据
        List<Map<String,Object>> caseStepList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> compStepList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> stepParamList = new ArrayList<Map<String,Object>>();
        analysisCaseInfo(target,caseStepList,compStepList,stepParamList);
        mdCaseStepService.insertBatchForMap(caseStepList);
        mdComponentStepService.insertBatchForMap(compStepList);
        mdStepParameterService.insertBatchForMap(stepParamList);
    }

    /**
     * 删除案例相关表
     * @param caseId
     * @param projectId
     */
    @Override
    public void deleteModifyCaseInfo(String caseId, String projectId) {
        //案例基础信息删除
        mdCaseDesignInfoService.deleteByPrimaryKey(caseId);
        //案例步骤删除
        MdCaseStep stepDelete = new MdCaseStep();
        stepDelete.setCaseId(caseId);
        stepDelete.setProjectId(projectId);
        mdCaseStepService.deleteByExample(stepDelete);
        //步骤组件删除
        MdComponentStep comDelete = new MdComponentStep();
        comDelete.setCaseId(caseId);
        comDelete.setProjectId(projectId);
        mdComponentStepService.deleteByExample(comDelete);
        //步骤参数删除
        MdStepParameter paramDelete = new MdStepParameter();
        paramDelete.setCaseId(caseId);
        paramDelete.setProjectId(projectId);
        mdStepParameterService.deleteByExample(paramDelete);
    }

    /**
     * 查询步骤关联数据
     * @param target
     * @return
     */
    @Override
    public List<InterfaceStepComponent> queryStepComponentList(InterfaceCaseStep target) {
        //查询步骤数据(包含组件)
        MdCaseStep mdCaseStep = new MdCaseStep();
        mdCaseStep.setCaseId(target.getId());
        mdCaseStep.setProjectId(target.getProjectId());
        List<Map<String,Object>> caseStepList = mdCaseStepService.queryStepListByOrder(mdCaseStep.toMap());
        String compQuery = "";
        for(Map<String,Object> stepMap : caseStepList){
            if("".equals(compQuery)){
                compQuery = "'"+stepMap.get("stepId")+"'";
            }else{
                compQuery = compQuery+",'"+stepMap.get("stepId")+"'";
            }
        }
        Map<String,Object> queryMap = new HashMap<String,Object>();
        queryMap.put("compQuery",compQuery);
        queryMap.put("projectId",target.getProjectId());
        List<Map<String,Object>> paramList = mdStepParameterService.queryParamForStepList(queryMap);
        // System.out.println("打印"+paramList);
        List<InterfaceStepComponent> result = new ArrayList<InterfaceStepComponent>();
        for(Map<String,Object> map : caseStepList){
            //循环查询结果，添加参数
            InterfaceStepComponent interStepComp = new InterfaceStepComponent();
            interStepComp.setProjectId(target.getProjectId());
            interStepComp.setComponentNo(map.get("componentNo")+"");
            interStepComp.setComponentName(map.get("componentName")+"");
            interStepComp.setComponentFlag(map.get("componentFlag")+"");
            if("0".equals(interStepComp.getComponentFlag())){
                //基础组件查询库信息
                ReComponentLibrary libInfo = reComponentLibraryService.findByPrimaryKey(map.get("libraryId")+"");
                interStepComp.setLibraryName(libInfo==null?null:libInfo.getLibraryName());
            }

            interStepComp.setId(map.get("componentId")+"");
            interStepComp.setStepId(map.get("stepId")+"");
            interStepComp.setParamCheck("0");
            interStepComp.setVersion(map.get("componentVersion")+"");
            interStepComp.setParameterNumber(map.get("componentParamNum") == null? null:map.get("componentParamNum")+"");
            List<ReComponentParameter> paList = new ArrayList<ReComponentParameter>();
            for(Map<String,Object> stepParam : paramList){
                if(stepParam.get("stepId").equals(map.get("stepId"))){
                    ReComponentParameter parameter = new ReComponentParameter();
                    parameter.setId(stepParam.get("id")+"");
                    parameter.setProjectId(stepParam.get("projectId")+"");
                    parameter.setComponentId(stepParam.get("componentId")+"");
                    parameter.setParameterType(stepParam.get("parameterType")+"");
                    parameter.setParameterName(stepParam.get("parameterName")+"");
                    parameter.setParameterFlag(stepParam.get("parameterFlag")+"");
                    parameter.setDefaultValue(stepParam.get("parameterValue")+"");
                    parameter.setParameterComment(stepParam.get("parameterComment")+"");
                    parameter.setRequired(stepParam.get("required")+"");
                    paList.add(parameter);
                }
            }
            if("0".equals(map.get("isValid"))){
                //已经不存在的组件
                interStepComp.setParamCheck("3");
                String libraryName = componentInfoService.queryLibraryNameByComId(interStepComp.getId());
                interStepComp.setCheckMessage("组件库"+libraryName+"已禁用或删除，组件以无法使用，请更换组件以免影响您使用");
            }else{
                if(map.get("isValid") == null){
                    interStepComp.setParamCheck("3");
                    interStepComp.setCheckMessage("组件已删除,请更换其他组件使用");
                    //去历史库查询组件名称
                    String name = componentHisInfoService.getComponentNameByHis(interStepComp.getId());
                    interStepComp.setComponentName(name);
                }else {
                    if(interStepComp.getParameterNumber() != null &&
                            !interStepComp.getParameterNumber().equals(map.get("parameterNumber"))){
                        interStepComp.setParamCheck("3");
                        interStepComp.setCheckMessage("组件参数已进行更新，请使用新版本组件，详情请查看组件信息");
                    }
                }
                if("0".equals(map.get("isDeprecated"))){
                    //组件为不推荐状态
                    interStepComp.setParamCheck("2");
                    interStepComp.setCheckMessage("组件已有更优化的版本，请查看组件描述使用优化版本组件");
                }
            }
            interStepComp.setParamList(paList);
            result.add(interStepComp);
        }

//        //进行高级组件的参数同步校验
//        String message = "此组件参数已进行更新，对比当前步骤:\n";
//        for(InterfaceStepComponent checkCom : result){
//            if("1".equals(checkCom.getComponentFlag())){
//                //进行高级组件的参数与对应步骤组件参数进行对比
//                ReComponentParameter queryParam = new ReComponentParameter();
//                queryParam.setComponentId(checkCom.getId());
//                queryParam.setProjectId(checkCom.getProjectId());
//                queryParam.setInOrOut("1");
//                List<ReComponentParameter> comParamList = reComponentParameterService.query(queryParam);
//                List<ReComponentParameter> stepParamList = checkCom.getParamList();
//
//                //增加的
//                Map<String,ReComponentParameter> oneMap = new HashMap<String,ReComponentParameter>();
//                for(ReComponentParameter step : stepParamList){
//                    oneMap.put(step.getId(),step);
//                }
//                String add = "增加参数:";
//                int addNum=0;
//                for(ReComponentParameter com : comParamList){
//                    if(!oneMap.containsKey(com.getId())){
//                        String name = com.getParameterName();
//                        add = add+name+".";
//                        addNum++;
//                    }
//                }
//                if(addNum>0){
//                    message = message+add+"共"+addNum+"个";
//                }
//                //减少的
//                MdStepParameter tableQuery = new MdStepParameter();
//                tableQuery.setProjectId(checkCom.getProjectId());
//                tableQuery.setStepId(checkCom.getStepId());
//                List<MdStepParameter> tableList = mdStepParameterService.query(tableQuery);
//                Map<String,ReComponentParameter> twoMap = new HashMap<String,ReComponentParameter>();
//                for(ReComponentParameter step : comParamList){
//                    twoMap.put(step.getId(),step);
//                }
//                String reduce = "\n去掉参数:";
//                int reduceNum=0;
//                for(MdStepParameter com : tableList){
//                    if(!twoMap.containsKey(com.getParameterId())){
//                        reduceNum++;
//                    }
//                }
//                if(reduceNum>0){
//                    message = message+reduce+reduceNum+"个";
//                }
//
//                if(reduceNum+addNum>0){
//                    checkCom.setParamCheck("1"); //校验参数更新
//                    checkCom.setCheckMessage(message);
//                }else{
//                    checkCom.setParamCheck("0"); //校验参数未更新
//                }
//            }
//        }
        return result;
    }

    /**
     * 查询案例信息并生成Map
     * @param target
     * @return
     */
    @Override
    public Map<String, Object> queryCaseInfoToMap(MdCaseDesignInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        //基础信息
        MdCaseDesignInfo caseInfo = mdCaseDesignInfoService.findByPrimaryKey(target.getId());
        Map<String, Object> caseMap = caseInfo.toMap();
        List<Map<String,Object>> caseStepList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> compStepList = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> stepParamList = new ArrayList<Map<String,Object>>();
        //案例步骤
        MdCaseStep stepQuery = new MdCaseStep();
        stepQuery.setCaseId(target.getId());
        stepQuery.setProjectId(target.getProjectId());
        List<MdCaseStep> stepQueryList = mdCaseStepService.query(stepQuery);
        for (MdCaseStep one : stepQueryList){
            caseStepList.add(one.toMap());
        }
        //步骤组件
        MdComponentStep comQuery = new MdComponentStep();
        comQuery.setCaseId(target.getId());
        comQuery.setProjectId(target.getProjectId());
        List<MdComponentStep> comQueryList = mdComponentStepService.query(comQuery);
        for(MdComponentStep one : comQueryList){
            compStepList.add(one.toMap());
        }
        //步骤参数
        MdStepParameter paramQuery = new MdStepParameter();
        paramQuery.setCaseId(target.getId());
        paramQuery.setProjectId(target.getProjectId());
        List<MdStepParameter> paramQueryList = mdStepParameterService.query(paramQuery);
        for(MdStepParameter one: paramQueryList){
            stepParamList.add(one.toMap());
        }
        caseMap.put("caseStepList",caseStepList);
        caseMap.put("compStepList",compStepList);
        caseMap.put("stepParamList",stepParamList);
        return caseMap;
    }

    /**
     * 回滚时插入案例数据到修改表
     * @param caseMap
     */
    @Override
    public void insertModifyCaseByMap(Map<String, Object> caseMap) {
        caseMap.put("modifyTime",new Date());
        mdCaseDesignInfoService.insertInfoForMap(caseMap);
        //步骤数据
        List<Map<String,Object>> caseStepList = (List<Map<String,Object>>)caseMap.get("caseStepList");
        //组件表
        List<Map<String,Object>> compStepList = (List<Map<String,Object>>)caseMap.get("compStepList");
        //参数表
        List<Map<String,Object>> stepParamList = (List<Map<String,Object>>)caseMap.get("stepParamList");
        mdCaseStepService.insertBatchForMap(caseStepList);
        mdComponentStepService.insertBatchForMap(compStepList);
        mdStepParameterService.insertBatchForMap(stepParamList);
    }

    /**
     * 校验案例内组件版本
     * @param caseInfo
     * @return
     */
    @Override
    public String checkCaseComStepVersion(MdCaseDesignInfo caseInfo) {
        //查询所有封装数据以及版本
        String message = "none";
        MdCaseStep mdCaseStep = new MdCaseStep();
        mdCaseStep.setCaseId(caseInfo.getId());
        mdCaseStep.setProjectId(caseInfo.getProjectId());
        List<Map<String,Object>> caseStepList = mdCaseStepService.queryStepListByOrder(mdCaseStep.toMap());
        for(Map<String,Object> comPac : caseStepList){
            if("0".equals(comPac.get("isValid"))){
                message="组件库"+comPac.get("componentName")+"已禁用或删除，请刷新数据查看";
            }else{
                if(comPac.get("isValid") == null){
                    //去历史库查询组件名称
                    String name = componentHisInfoService.getComponentNameByHis(comPac.get("componentId")+"");
                    message= "组件"+name+"已删除,请更换其他组件使用";
                }else{
                    if(comPac.get("componentParamNum") != null &&
                            !comPac.get("componentParamNum").equals(comPac.get("parameterNumber"))){
                        message = "组件" + comPac.get("componentName") + "参数已进行更新，请使用新版本组件，详情请查看组件信息";
                    }
                }
                if("0".equals(comPac.get("isDeprecated"))){
                    //组件为不推荐状态
                    message="组件"+comPac.get("componentName")+"已有更优化的版本，请查看组件描述使用优化版本组件";
                }
            }
        }
        return message;
    }

    /**
     * 校验案例名称是否存在
     * @param info
     * @return
     */
    @Override
    public boolean checkCaseDesignExist(ReCaseDesignInfo info) {
        ReCaseDesignInfo check = new ReCaseDesignInfo();
        check.setProjectId(info.getProjectId());
        check.setCaseName(info.getCaseName());
        check.setCaseFlag("1");
        List<ReCaseDesignInfo> checkList = reCaseDesignInfoService.query(check);
        if(checkList != null && checkList.size()>0){
            if(checkList.get(0).getId().equals(info.getId())){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    /**
     * 用例数据复制
     * @param target
     */
    @Transactional
    @Override
    public void CopyCaseToMenu(InterfaceComAndCaseCopy target) {
        CaseDesignTools tools = new CaseDesignTools();
        ReCaseDesignMenutree tree = new ReCaseDesignMenutree();
        tree.setProjectId(target.getProjectId());
        tree.setMenuId(target.getMenuId());
        ReCaseDesignMenutree caseTree = getNewModule(tree);
        for(String id : target.getIdList()){
            //循环list取每个id做数据查询并作为数据源插入
            MdCaseDesignInfo mdInfo = mdCaseDesignInfoService.findByPrimaryKey(id);
            if(mdInfo==null){
                continue;
            }
            //步骤
            MdCaseStep stepQuery = new MdCaseStep();
            stepQuery.setCaseId(id);
            stepQuery.setProjectId(target.getProjectId());
            List<MdCaseStep> mdStepList = mdCaseStepService.query(stepQuery);
            //步骤组件管关联
            MdComponentStep comStepQuery = new MdComponentStep();
            comStepQuery.setCaseId(id);
            comStepQuery.setProjectId(target.getProjectId());
            List<MdComponentStep> comStepList = mdComponentStepService.query(comStepQuery);
            //步骤数据
            MdStepParameter paramQuery = new MdStepParameter();
            paramQuery.setCaseId(id);
            paramQuery.setProjectId(target.getProjectId());
            List<MdStepParameter> paramList = mdStepParameterService.query(paramQuery);

            //处理数据
            String mdName = mdInfo.getCaseName();
            MdCaseDesignInfo mdQuery =  new MdCaseDesignInfo();
            mdQuery.setCaseName(mdName+"_复制(%%)"); //取复制后括号类数值最大的一个
            mdQuery.setProjectId(target.getProjectId());
            mdQuery.setMenuId(target.getMenuId());
            String maxName = mdCaseDesignInfoService.queryMaxNameByName(mdQuery);
            String newName = "";
            if(maxName==null || maxName.isEmpty()){
                newName = mdName+"_复制(1)";
            }else{
                newName= getNewMaxName(maxName);
            }

            String newId = tools.getUUID();
            mdInfo.setId(newId);
            mdInfo.setCreateStaff(target.getModifyStaff());
            mdInfo.setCreateTime(new Date());
            mdInfo.setModifyStaff(target.getModifyStaff());
            mdInfo.setModifyTime(new Date());
            mdInfo.setVersion("");
            mdInfo.setCaseNo(getNewCaseNo(target.getProjectId()));
            mdInfo.setMenuId(target.getMenuId());
            mdInfo.setCaseName(newName);
            mdInfo.setModuleId(caseTree.getId());
            mdInfo.setModuleName(caseTree.getMenuName());
            Map<String,String> toolMap = new HashMap<String,String>();
            for(MdCaseStep step : mdStepList){
                String stepId = tools.getUUID();
                toolMap.put(step.getStepId(),stepId);
                step.setId(tools.getUUID());
                step.setVersion("");
                step.setCaseId(newId);
                step.setStepId(stepId);
            }
            for(MdComponentStep com : comStepList){
                com.setId(tools.getUUID());
                com.setCaseId(newId);
                com.setStepId(toolMap.get(com.getStepId()));
            }
            for(MdStepParameter param : paramList){
                param.setId(tools.getUUID());
                param.setVersion("");
                param.setCaseId(newId);
                param.setStepId(toolMap.get(param.getStepId()));
            }

            //插入数据
            mdCaseDesignInfoService.insert(mdInfo);
            if(mdStepList.size()>0){
                mdCaseStepService.insertByBatch(mdStepList);
            }
            if(comStepList.size()>0){
                mdComponentStepService.insertByBatch(comStepList);
            }
            if(paramList.size()>0){
                mdStepParameterService.insertByBatch(paramList);
            }
        }
    }


    /**
     * 解析请求参数，生成关联表批量插入数据
     * @param caseStepList
     * @param compStepList
     * @param stepParamList
     */
    private void analysisCaseInfo(InterfaceCaseInfo target,List<Map<String,Object>> caseStepList,
                                  List<Map<String,Object>> compStepList,List<Map<String,Object>> stepParamList){
        CaseDesignTools tools = new CaseDesignTools();

        if(target.getComponentList() != null && target.getComponentList().size()>0){
            int order = 1;

            for(InterfaceStepComponent stepComponent : target.getComponentList()){
                //案例步骤关联
                stepComponent.setComponentId(stepComponent.getId());
                String stepId = "";
                if(target.getSetId() == null){
                    stepId=tools.getUUID();
                }else{
                    stepId=target.getSetId();
                }

                ReCaseStep insertCS = new ReCaseStep();
                insertCS.setProjectId(target.getProjectId());
                insertCS.setId(tools.getUUID());
                insertCS.setStepId(stepId);
                insertCS.setCaseId(target.getCaseId());
                insertCS.setStepOrder(tools.getOrder(order));
                insertCS.setModifyStaff(target.getModifyStaff());
                insertCS.setModifyTime(new Date());
                order++;
                caseStepList.add(insertCS.toMap());

                //步骤组件关联
                ReComponentStep reComponentStep = new ReComponentStep();
                reComponentStep.setId(UUID.randomUUID().toString().replace("-", ""));
                reComponentStep.setStepId(stepId);
                reComponentStep.setComponentId(stepComponent.getId());
                reComponentStep.setProjectId(target.getProjectId());
                reComponentStep.setModifyStaff(target.getModifyStaff());
                reComponentStep.setModifyTime(new Date());
                reComponentStep.setCaseId(target.getCaseId());
                reComponentStep.setComponentVersion(stepComponent.getVersion());

                //步骤参数
                int orderPar = 1;
                for(ReComponentParameter reCompParam :stepComponent.getParamList()){
                    ReStepParameter reStepParameter = new ReStepParameter();
                    reStepParameter.setId(UUID.randomUUID().toString().replace("-", ""));
                    reStepParameter.setProjectId(target.getProjectId());
                    reStepParameter.setStepId(stepId);
                    reStepParameter.setModifyStaff(target.getModifyStaff());
                    reStepParameter.setModifyTime(new Date());
                    reStepParameter.setParameterId(reCompParam.getId());
                    reStepParameter.setParameterOrder(tools.getOrder(orderPar));
                    reStepParameter.setParameterValue(reCompParam.getDefaultValue());
                    reStepParameter.setCaseId(target.getCaseId());
                    stepParamList.add(reStepParameter.toMap());
                    orderPar++;
                }
                reComponentStep.setComponentParamNum(stepComponent.getParameterNumber()==null?((orderPar-1)+""):stepComponent.getParameterNumber());
                compStepList.add(reComponentStep.toMap());
            }
        }
    }


    /**
     * 根据项目编号生成新的案例编号
     * @param projectId
     * @return caseNo
     */
    private String getNewCaseNo(String projectId) {
        CaseDesignTools tools = new CaseDesignTools();
        Map<String,Object> query = new HashMap<String,Object>();
        query.put("projectId",projectId);
        String maxNo = mdCaseDesignInfoService.queryMaxCaseNo(query);
        caseNoMap.put(maxNo,"");
        //String caseNo = "TC-"+projectId+"-"+tools.generateUniqueBusinessNo(caseNoMap,maxNo);
        String caseNo = "TC-"+tools.generateUniqueBusinessNo(caseNoMap,maxNo);
        return caseNo;
    }

    /**
     * 括号内数值加一返回
     * @param maxName
     * @return
     */
    private String getNewMaxName(String maxName){
        String result = "";
        String[] nameStr = maxName.split("_");
        for (int i = 0; i < nameStr.length; i++) {
            if(i < nameStr.length-1){
                result = result + nameStr[i]+"_";
            }else{
                String tool = nameStr[i];
                String num = tool.split("\\(")[1].split("\\)")[0];
                result = result + tool.split("\\(")[0]+"("+(Integer.parseInt(num)+1)+")";
            }
        }

        return result;
    }

    private ReCaseDesignMenutree getNewModule(ReCaseDesignMenutree target){
        List<ReCaseDesignMenutree> list = reCaseDesignMenutreeService.query(target);
        if(list !=null && list.size()>0){
            target.setMenuName(list.get(0).getMenuName());
            target.setId(list.get(0).getId());
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
            List<ReCaseDesignMenutree> treeList = reCaseDesignMenutreeService.query(tree);
            if(treeList!=null&&treeList.size()>0){
                menuName = treeList.get(0).getMenuName();
                id = treeList.get(0).getId();
            }
        }
        target.setId(id);
        target.setMenuName(menuName);
        return target;
    }
}
