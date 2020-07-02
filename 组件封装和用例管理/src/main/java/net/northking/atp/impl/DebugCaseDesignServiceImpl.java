package net.northking.atp.impl;

import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseStep;
import net.northking.atp.db.persistent.ReComponentPackage;
import net.northking.atp.db.persistent.ReComponentParameter;
import net.northking.atp.db.service.ReCaseStepService;
import net.northking.atp.db.service.ReComponentInfoService;
import net.northking.atp.db.service.ReComponentPackageService;
import net.northking.atp.db.service.ReComponentParameterService;
import net.northking.atp.service.DebugCaseDesignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/4/23 0023.
 */
@Service
public class DebugCaseDesignServiceImpl implements DebugCaseDesignService{
    @Autowired
    ReComponentInfoService reComponentInfoService; //组件信息表
    @Autowired
    ReComponentPackageService reComponentPackageService; //组件封装表
    @Autowired
    ReComponentParameterService reComponentParameterService; //组件参数表
    @Autowired
    ComponentInfoServiceImpl componentInfoService;
    @Autowired
    ReCaseStepService reCaseStepService;

    private List<ReComponentParameter> resultList = new ArrayList<ReComponentParameter>();
    @Override
    public List<ReComponentParameter> analysisCaseComponentInfo(ReCaseDesignInfo target) {
        //根据案例编号查询案例所有步骤，组件，并解析调取引擎接口完成调试、
        //步骤-组件-组件详细信息
        Map<String,Object> query = new HashMap<String,Object>();
        query.put("caseId",target.getId());
        query.put("projectId",target.getProjectId());
        List<Map<String,Object>> compList = reCaseStepService.queryStepListByOrder(query);
        List<Map<String,Object>> newList = new ArrayList<Map<String, Object>>();
        for(Map<String,Object> comp : compList){
            if(comp.get("componentFlag")!=null && "1".equals(comp.get("componentFlag"))){
                //为高级组件
                analysisList(comp,target);
                //查高级组件的自定义参数.获取自定义参数
                ReComponentParameter paramQuery = new ReComponentParameter();
                paramQuery.setProjectId(target.getProjectId());
                paramQuery.setInOrOut("1");
                paramQuery.setComponentId(comp.get("componentId")+"");
                List<ReComponentParameter> gaoji = reComponentParameterService.query(paramQuery);
                resultList.addAll(gaoji);
            }else{
                //已经是基础组件。获取参数
                ReComponentParameter paramQuery = new ReComponentParameter();
                paramQuery.setProjectId(target.getProjectId());
                paramQuery.setInOrOut("0");
                paramQuery.setRunComponentId(comp.get("basisComponentId")+"");
                paramQuery.setComponentId(comp.get("componentId")+"");
                List<ReComponentParameter> basis = reComponentParameterService.query(paramQuery);
                resultList.addAll(basis);
            }
        }
        //基础组件-详细拆分所有子list按序排列组件
        for(Map<String,Object> comp : compList){

        }
        return resultList;
    }

    /**
     * 解析高级组件
     * @param compMap
     */
    private void analysisList(Map<String,Object> compMap,ReCaseDesignInfo target){
        //组件-判断每个组件，如果是高级组件，则进行组件解析
        //查询所有基础组件
        ReComponentPackage reComponentPackage = new ReComponentPackage();
        reComponentPackage.setProjectId(target.getProjectId());
        reComponentPackage.setComponentId(compMap.get("componentId")+"");
        List<Map<String,Object>> pacList =
                reComponentPackageService.queryComponentByOrder(reComponentPackage.toMap());
        //循环基础组件
        for (Map<String,Object> rePac: pacList){
            rePac.put("componentId",rePac.get("basisComponentId"));
            if(rePac.get("componentFlag")!=null && "1".equals(rePac.get("componentFlag"))){
                analysisList(rePac,target);
                //查高级组件的自定义参数.获取自定义参数
                ReComponentParameter paramQuery = new ReComponentParameter();
                paramQuery.setProjectId(target.getProjectId());
                paramQuery.setInOrOut("1");
                paramQuery.setComponentId(rePac.get("componentId")+"");
                List<ReComponentParameter> high = reComponentParameterService.query(paramQuery);
                resultList.addAll(high);
            }else{
                //已经是基础组件。获取参数
                ReComponentParameter paramQuery = new ReComponentParameter();
                paramQuery.setProjectId(target.getProjectId());
                paramQuery.setInOrOut("0");
                paramQuery.setComponentId(compMap.get("componentId")+"");
                paramQuery.setRunComponentId(rePac.get("basisComponentId")+"");
                List<ReComponentParameter> basis = reComponentParameterService.query(paramQuery);
                resultList.addAll(basis);
            }
        }
        compMap.put("childList",pacList);
    }
}
