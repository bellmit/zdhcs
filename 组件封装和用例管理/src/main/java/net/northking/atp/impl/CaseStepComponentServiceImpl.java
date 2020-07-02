package net.northking.atp.impl;

import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceCaseInfo;
import net.northking.atp.entity.InterfaceCaseStep;
import net.northking.atp.entity.InterfaceStepComponent;
import net.northking.atp.service.CaseStepComponentService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 案例步骤组件操作
 * Created by Administrator on 2019/4/15 0015.
 */
@Service
public class CaseStepComponentServiceImpl implements CaseStepComponentService{
    @Autowired
    private ReCaseDesignInfoService reCaseDesignInfoService; //案例信息
    @Autowired
    private ReCaseStepService reCaseStepService; //案例步骤
    @Autowired
    private ReStepParameterService reStepParameterService; //步骤参数
    @Autowired
    private ReComponentParameterService reComponentParameterService; //组件参数
    @Autowired
    private ReComponentStepService reComponentStepService; //组件步骤关联
    @Autowired
    private ReComponentLibraryService reComponentLibraryService; //组件库
    @Autowired
    private ReComponentInfoService reComponentInfoService; //组件信息表

    /**
     * 查询所有案例对应的步骤以及参数
     * @param target
     * @return
     */
    @Override
    public List<InterfaceStepComponent> queryStepComponentList(InterfaceCaseStep target) {
        //查询步骤数据(包含组件)
        ReCaseStep reCaseStep = new ReCaseStep();
        reCaseStep.setCaseId(target.getId());
        reCaseStep.setProjectId(target.getProjectId());
        List<Map<String,Object>> caseStepList = reCaseStepService.queryStepListByOrder(reCaseStep.toMap());
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
        List<Map<String,Object>> paramList = reStepParameterService.queryParamForStepList(queryMap);
       // System.out.println("打印"+paramList);
        List<InterfaceStepComponent> result = new ArrayList<InterfaceStepComponent>();
        for(Map<String,Object> map : caseStepList){
            //循环查询结果，添加参数
            InterfaceStepComponent interStepComp = new InterfaceStepComponent();
            interStepComp.setProjectId(target.getProjectId());
            interStepComp.setComponentNo(map.get("componentNo")+"");
            interStepComp.setComponentName(map.get("componentName")+"");
            interStepComp.setComponentFlag(map.get("componentFlag")+"");
            interStepComp.setId(map.get("componentId")+"");
            interStepComp.setStepId(map.get("stepId")+"");
            interStepComp.setParamCheck("0");
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
            if(!"1".equals(map.get("isValid"))){
                //已经不存在的组件
                interStepComp.setParamCheck("3");
                ReComponentInfo comInfo = reComponentInfoService.findByPrimaryKey(interStepComp.getId());
                ReComponentLibrary libraryInfo = reComponentLibraryService.findByPrimaryKey(comInfo.getLibraryId());
                String libraryName = libraryInfo.getLibraryName();
                interStepComp.setCheckMessage("组件库"+libraryName+"已禁用或删除，组件以无法使用，请更换组件以免影响您使用");
            }else{
                if("0".equals(map.get("isDeprecated"))){
                    //组件为不推荐状态
                    interStepComp.setParamCheck("2");
                    interStepComp.setCheckMessage("组件已有更优化的版本，请查看组件描述使用优化版本组件");
                }
            }
            interStepComp.setParamList(paList);
            result.add(interStepComp);
        }

        //进行高级组件的参数同步校验
        String message = "此组件参数已进行更新，对比当前步骤:\n";
        for(InterfaceStepComponent checkCom : result){
            if("1".equals(checkCom.getComponentFlag())){
                //进行高级组件的参数与对应步骤组件参数进行对比
                ReComponentParameter queryParam = new ReComponentParameter();
                queryParam.setComponentId(checkCom.getId());
                queryParam.setProjectId(checkCom.getProjectId());
                queryParam.setInOrOut("1");
                List<ReComponentParameter> comParamList = reComponentParameterService.query(queryParam);
                List<ReComponentParameter> stepParamList = checkCom.getParamList();

                //增加的
                Map<String,ReComponentParameter> oneMap = new HashMap<String,ReComponentParameter>();
                for(ReComponentParameter step : stepParamList){
                    oneMap.put(step.getId(),step);
                }
                String add = "增加组件:";
                int addNum=0;
                for(ReComponentParameter com : comParamList){
                    if(!oneMap.containsKey(com.getId())){
                        String name = com.getParameterName();
                        add = add+name+".";
                        addNum++;
                    }
                }
                if(addNum>0){
                    message = message+add+"共"+addNum+"个";
                }
                //减少的
                ReStepParameter tableQuery = new ReStepParameter();
                tableQuery.setProjectId(checkCom.getProjectId());
                tableQuery.setStepId(checkCom.getStepId());
                List<ReStepParameter> tableList = reStepParameterService.query(tableQuery);
                Map<String,ReComponentParameter> twoMap = new HashMap<String,ReComponentParameter>();
                for(ReComponentParameter step : comParamList){
                    twoMap.put(step.getId(),step);
                }
                String reduce = "\n去掉组件:";
                int reduceNum=0;
                for(ReStepParameter com : tableList){
                    if(!twoMap.containsKey(com.getParameterId())){
                        reduceNum++;
                    }
                }
                if(reduceNum>0){
                    message = message+reduce+reduceNum+"个";
                }

                if(reduceNum+addNum>0){
                    checkCom.setParamCheck("1"); //校验参数更新
                    checkCom.setCheckMessage(message);
                }else{
                    checkCom.setParamCheck("0"); //校验参数未更新
                }
            }
        }
        return result;
    }

    /**
     * 新增插入案例步骤信息_进入历史表
     * @param target
     * @return
     */
    @Override
    public boolean saverCaseStepInfo(InterfaceCaseInfo target, String hisId) {
        /*/此接口同时增加案例步骤关联，步骤组件管理，步骤参数_删除新增的方式
        CaseDesignTools tools = new CaseDesignTools();

        if(target.getComponentList() != null && target.getComponentList().size()>0){
            int order = 1;
            //新增
            List<ReCaseStep> caseStepList = new ArrayList<ReCaseStep>();
            List<ReComponentStep> compStepList = new ArrayList<ReComponentStep>();
            List<ReStepParameter> stepParamList = new ArrayList<ReStepParameter>();
            //历史表新增
            List<Map<String,Object>> hisCaseStepList = new ArrayList<Map<String,Object>>();
            List<Map<String,Object>> hisCompStepList = new ArrayList<Map<String,Object>>();
            List<Map<String,Object>> hisStepParList = new ArrayList<Map<String,Object>>();

            for(InterfaceStepComponent stepComponent : target.getComponentList()){
                //案例步骤关联
                stepComponent.setComponentId(stepComponent.getId());

                ReCaseStep insertCS = new ReCaseStep();
                insertCS.setProjectId(target.getProjectId());
                insertCS.setId(UUID.randomUUID().toString().replace("-", ""));
                String stepId = UUID.randomUUID().toString().replace("-", "");
                insertCS.setStepId(stepId);
                insertCS.setCaseId(target.getCaseId());
                insertCS.setStepOrder(tools.getOrder(order));
                insertCS.setModifyTime(new Date());
                order++;
                caseStepList.add(insertCS);
                Map<String,Object> caseStepMap = insertCS.toMap();
                caseStepMap.put("id",tools.getUUID());
                caseStepMap.put("reId",insertCS.getId());
                caseStepMap.put("hisId",hisId);
                hisCaseStepList.add(caseStepMap);//生成历史案例步骤数据

                //步骤组件关联
                ReComponentStep reComponentStep = new ReComponentStep();
                reComponentStep.setId(UUID.randomUUID().toString().replace("-", ""));
                reComponentStep.setStepId(stepId);
                reComponentStep.setComponentId(stepComponent.getId());
                reComponentStep.setProjectId(target.getProjectId());
                reComponentStep.setModifyTime(new Date());
                reComponentStep.setCaseId(target.getCaseId());
                compStepList.add(reComponentStep);
                Map<String,Object> comStepMap = reComponentStep.toMap();
                comStepMap.put("id",tools.getUUID());
                comStepMap.put("reId",reComponentStep.getId());
                comStepMap.put("hisId",hisId);
                hisCompStepList.add(comStepMap);//生成历史步骤组件数据

                //步骤参数
                int orderPar = 1;
                for(ReComponentParameter reCompParam :stepComponent.getParamList()){
                    ReStepParameter reStepParameter = new ReStepParameter();
                    reStepParameter.setId(UUID.randomUUID().toString().replace("-", ""));
                    reStepParameter.setProjectId(target.getProjectId());
                    reStepParameter.setStepId(stepId);
                    reStepParameter.setModifyTime(new Date());
                    reStepParameter.setParameterId(reCompParam.getId());
                    reStepParameter.setParameterOrder(tools.getOrder(orderPar));
                    reStepParameter.setParameterValue(reCompParam.getDefaultValue());
                    reStepParameter.setCaseId(target.getCaseId());
                    stepParamList.add(reStepParameter);
                    orderPar++;
                    Map<String,Object> stepParMap = reStepParameter.toMap();
                    stepParMap.put("id",tools.getUUID());
                    stepParMap.put("reId",reStepParameter.getId());
                    stepParMap.put("hisId",hisId);
                    hisStepParList.add(stepParMap);//生成历史步骤参数数据
                }
            }

            reCaseStepService.insertByBatch(caseStepList); //批量插入步骤数据
            hisCaseStepService.insertBatchForMap(hisCaseStepList); //批量插入步骤历史数据

            reComponentStepService.insertByBatch(compStepList); //批量插入步骤组件数据
            hisComponentStepService.insertBatchForMap(hisCompStepList);//批量插入步骤组件历史数据

            if(stepParamList.size()>0){
                reStepParameterService.insertByBatch(stepParamList); //批量插入步骤参数数据
                hisStepParameterService.insertBatchForMap(hisStepParList); //批量插入步骤参数历史数据
            }
        }*/
        return true;
    }

    /**
     * 更新案例步骤信息_不进入历史表
     * @param target
     * @return
     */
    @Override
    public boolean updateCaseStepInfo(InterfaceCaseInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        ReCaseStep deleteCS = new ReCaseStep();
        deleteCS.setCaseId(target.getCaseId()); //案例编号
        deleteCS.setProjectId(target.getProjectId()); //系统编号
        reCaseStepService.deleteByExample(deleteCS);//删除原案例步骤关联

        ReComponentStep reCompStep = new ReComponentStep();
        reCompStep.setProjectId(target.getProjectId());
        reCompStep.setCaseId(target.getCaseId());
        reComponentStepService.deleteByExample(reCompStep); //删除原步骤组件参数

        ReStepParameter reStepPara = new ReStepParameter();
        reStepPara.setCaseId(target.getCaseId());
        reStepPara.setProjectId(target.getProjectId());
        reStepParameterService.deleteByExample(reStepPara); //删除原步骤参数


        if(target.getComponentList() != null && target.getComponentList().size()>0){
            int order = 1;
            //新增
            List<ReCaseStep> caseStepList = new ArrayList<ReCaseStep>();
            List<ReComponentStep> compStepList = new ArrayList<ReComponentStep>();
            List<ReStepParameter> stepParamList = new ArrayList<ReStepParameter>();
            for(InterfaceStepComponent stepComponent : target.getComponentList()){
                //案例步骤关联
                stepComponent.setComponentId(stepComponent.getId());

                ReCaseStep insertCS = new ReCaseStep();
                insertCS.setProjectId(target.getProjectId());
                insertCS.setId(UUID.randomUUID().toString().replace("-", ""));
                String stepId = UUID.randomUUID().toString().replace("-", "");
                insertCS.setStepId(stepId);
                insertCS.setCaseId(target.getCaseId());
                insertCS.setStepOrder(tools.getOrder(order));
                insertCS.setModifyTime(new Date());
                order++;
                caseStepList.add(insertCS);

                //步骤组件关联
                ReComponentStep reComponentStep = new ReComponentStep();
                reComponentStep.setId(UUID.randomUUID().toString().replace("-", ""));
                reComponentStep.setStepId(stepId);
                reComponentStep.setComponentId(stepComponent.getId());
                reComponentStep.setProjectId(target.getProjectId());
                reComponentStep.setModifyTime(new Date());
                reComponentStep.setCaseId(target.getCaseId());
                compStepList.add(reComponentStep);

                //步骤参数
                int orderPar = 1;
                for(ReComponentParameter reCompParam :stepComponent.getParamList()){
                    ReStepParameter reStepParameter = new ReStepParameter();
                    reStepParameter.setId(UUID.randomUUID().toString().replace("-", ""));
                    reStepParameter.setProjectId(target.getProjectId());
                    reStepParameter.setStepId(stepId);
                    reStepParameter.setModifyTime(new Date());
                    reStepParameter.setParameterId(reCompParam.getId());
                    reStepParameter.setParameterOrder(tools.getOrder(orderPar));
                    reStepParameter.setParameterValue(reCompParam.getDefaultValue());
                    reStepParameter.setCaseId(target.getCaseId());
                    stepParamList.add(reStepParameter);
                    orderPar++;
                }
            }

            reCaseStepService.insertByBatch(caseStepList); //批量插入步骤数据
            reComponentStepService.insertByBatch(compStepList); //批量插入步骤组件数据
            if(stepParamList.size()>0){
                reStepParameterService.insertByBatch(stepParamList); //批量插入步骤参数数据
            }
        }
        return false;
    }

    /**
     * 删除案例信息及关联表
     * @param caseId
     * @param projectId
     */
    @Override
    public void deleteCaseInfo(String caseId, String projectId) {
        //案例删除
        reCaseDesignInfoService.deleteByPrimaryKey(caseId);
        //案例步骤删除
        ReCaseStep stepDelete = new ReCaseStep();
        stepDelete.setCaseId(caseId);
        stepDelete.setProjectId(projectId);
        reCaseStepService.deleteByExample(stepDelete);
        //步骤组件删除
        ReComponentStep comDelete = new ReComponentStep();
        comDelete.setCaseId(caseId);
        comDelete.setProjectId(projectId);
        reComponentStepService.deleteByExample(comDelete);
        //步骤参数删除
        ReStepParameter paramDelete = new ReStepParameter();
        paramDelete.setCaseId(caseId);
        paramDelete.setProjectId(projectId);
        reStepParameterService.deleteByExample(paramDelete);
    }

    /**
     * 插入正式表数据
     * @param caseMap
     */
    @Override
    public void insertFormalCaseInfo(Map<String, Object> caseMap) {
        caseMap.put("modifyTime",new Date());
        //基础表
        reCaseDesignInfoService.insertInfoForMap(caseMap);
        //步骤表
        List<Map<String,Object>> caseStepList = (List<Map<String,Object>>)caseMap.get("caseStepList");
        reCaseStepService.insertBatchForMap(caseStepList);
        //组件表
        List<Map<String,Object>> compStepList = (List<Map<String,Object>>)caseMap.get("compStepList");
        reComponentStepService.insertBatchForMap(compStepList);
        //参数表
        List<Map<String,Object>> stepParamList = (List<Map<String,Object>>)caseMap.get("stepParamList");
        reStepParameterService.insertBatchForMap(stepParamList);
    }


}
