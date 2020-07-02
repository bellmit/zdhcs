package net.northking.atp.impl;

import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceStepComponent;
import net.northking.atp.service.CaseDesignHisService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 案例信息历史表服务
 * Created by Administrator on 2019/6/11 0011.
 */
@Service
public class CaseDesignHisServiceImpl implements CaseDesignHisService {
    @Autowired
    private HisCaseDesignInfoService hisCaseDesignInfoService;
    @Autowired
    private HisCaseStepService hisCaseStepService;
    @Autowired
    private HisComponentStepService hisComponentStepService;
    @Autowired
    private HisStepParameterService hisStepParameterService;
    @Autowired
    private HisComponentInfoService hisComponentInfoService;
    @Autowired
    private ComponentInfoServiceImpl componentInfoService;
    @Autowired
    private ComponentHisInfoServiceImpl componentHisInfoService;

    /**
     * 插入案例信息历史表
     * @param infoMap
     */
    @Override
    public void insertCaseInfoHis(Map<String, Object> infoMap) {
        CaseDesignTools tools = new CaseDesignTools();
        String hisId = tools.getUUID(); //历史Id
        //插入历史表基础信息
        infoMap.put("reId", infoMap.get("id"));
        infoMap.put("id", hisId);
        infoMap.put("hisCommitStaff", infoMap.get("modifyStaff"));
        infoMap.put("hisCommitTime", tools.getNowTime());
        infoMap.put("hisChangeLog", "更新版本"+tools.getNowTime());
        hisCaseDesignInfoService.insertInfoForMap(infoMap);
        //步骤信息
        List<Map<String,Object>> caseStepList = (List<Map<String,Object>>)infoMap.get("caseStepList");
        if(caseStepList != null){
            for(Map<String,Object> stepMap : caseStepList){
                stepMap.put("reId",stepMap.get("id"));
                stepMap.put("id",tools.getUUID());
                stepMap.put("hisId",hisId);
            }
        }
        //组件信息
        List<Map<String,Object>> compStepList = (List<Map<String,Object>>)infoMap.get("compStepList");
        if(compStepList != null){
            for(Map<String,Object> comMap : compStepList){
                comMap.put("reId",comMap.get("id"));
                comMap.put("id",tools.getUUID());
                comMap.put("hisId",hisId);
            }
        }
        //参数信息
        List<Map<String,Object>> stepParamList = (List<Map<String,Object>>)infoMap.get("stepParamList");
        if(stepParamList != null){
            for(Map<String,Object> paramMap : stepParamList){
                paramMap.put("reId",paramMap.get("id"));
                paramMap.put("id",tools.getUUID());
                paramMap.put("hisId",hisId);
            }
        }
        hisCaseStepService.insertBatchForMap(caseStepList);
        hisComponentStepService.insertBatchForMap(compStepList);
        hisStepParameterService.insertBatchForMap(stepParamList);
    }

    /**
     * 查询版本对应的案例信息
     * @param target
     * @return
     */
    @Override
    public Map<String, Object> queryCaseInfoVersion(HisCaseDesignInfo target) {
        HisCaseDesignInfo hisCaseInfo = hisCaseDesignInfoService.findByPrimaryKey(target.getId());
        //步骤信息
        List<Map<String,Object>> caseStepList = new ArrayList<Map<String, Object>>();
        HisCaseStep stepQuery = new HisCaseStep();
        stepQuery.setHisId(hisCaseInfo.getId());
        stepQuery.setProjectId(hisCaseInfo.getProjectId());
        List<HisCaseStep> stepList = hisCaseStepService.query(stepQuery);
        for(HisCaseStep one:stepList){
            one.setId(one.getReId());
            caseStepList.add(one.toMap());
        }
        //组件信息
        List<Map<String,Object>> compStepList = new ArrayList<Map<String, Object>>();
        HisComponentStep comQuery = new HisComponentStep();
        comQuery.setHisId(hisCaseInfo.getId());
        comQuery.setProjectId(hisCaseInfo.getProjectId());
        List<HisComponentStep> comList = hisComponentStepService.query(comQuery);
        for(HisComponentStep one:comList){
            one.setId(one.getReId());
            compStepList.add(one.toMap());
        }
        //参数信息
        List<Map<String,Object>> stepParamList = new ArrayList<Map<String, Object>>();
        HisStepParameter paramQuery = new HisStepParameter();
        paramQuery.setHisId(hisCaseInfo.getId());
        paramQuery.setProjectId(hisCaseInfo.getProjectId());
        List<HisStepParameter> paramList = hisStepParameterService.query(paramQuery);
        for(HisStepParameter one:paramList){
            one.setId(one.getReId());
            stepParamList.add(one.toMap());
        }
        hisCaseInfo.setId(hisCaseInfo.getReId());
        Map<String,Object> hisCaseMap = hisCaseInfo.toMap();
        hisCaseMap.put("caseStepList",caseStepList);
        hisCaseMap.put("compStepList",compStepList);
        hisCaseMap.put("stepParamList",stepParamList);
        return hisCaseMap;
    }

    /**
     * 查询某历史版本详情
     * @param target
     * @return
     */
    @Override
    public List<InterfaceStepComponent> queryDetailHisCaseInfo(HisCaseDesignInfo target) {
        //查询步骤数据(包含组件)
        HisCaseStep mdCaseStep = new HisCaseStep();
        mdCaseStep.setCaseId(target.getReId());
        mdCaseStep.setProjectId(target.getProjectId());
        mdCaseStep.setHisId(target.getId());
        List<Map<String,Object>> caseStepList = hisCaseStepService.queryHisStepListByOrder(mdCaseStep.toMap());
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
        queryMap.put("hisId",target.getId());
        List<Map<String,Object>> paramList = hisStepParameterService.queryHisParamForStepList(queryMap);
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
        return result;
    }
}
