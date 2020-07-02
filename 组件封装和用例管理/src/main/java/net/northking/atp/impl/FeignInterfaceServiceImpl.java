package net.northking.atp.impl;


import io.swagger.models.auth.In;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceCaseInfo;
import net.northking.atp.entity.InterfaceComponentParameter;
import net.northking.atp.entity.InterfaceStepComponent;
import net.northking.atp.service.*;
import net.northking.atp.util.BeanUtil;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import net.northking.db.OrderBy;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by Administrator on 2020/3/12 0012.
 */
@Service
public class FeignInterfaceServiceImpl implements FeignInterfaceService {

    @Autowired
    private CaseDesignModifyService caseDesignModifyService;
    @Autowired
    private ComponentInfoServiceImpl componentInfoService;
    @Autowired
    private ComponentHisInfoService componentHisInfoService;
    @Autowired
    private CaseDesignVersionService caseDesignVersionService;
    @Autowired
    private CaseStepComponentService caseStepComponentService;

    @Autowired
    private MdCaseDesignInfoService mdCaseDesignInfoService;
    @Autowired
    private MdCaseStepService mdCaseStepService;
    @Autowired
    private MdComponentStepService mdComponentStepService;
    @Autowired
    private MdStepParameterService mdStepParameterService;
    @Autowired
    private MdComponentParameterService mdComponentParameterService;
    @Autowired
    private ReComponentParameterService reComponentParameterService;
    @Autowired
    private HisComponentInfoService hisComponentInfoService;
    @Autowired
    private ReCaseDesignInfoService reCaseDesignInfoService;
    @Autowired
    private ReCaseSetService reCaseSetService;
    @Autowired
    private ReComponentInfoService reComponentInfoService;
    @Autowired
    private ReComponentLibraryService reComponentLibraryService;

    /**
     * 批量生成用例
     * @param target
     * @return
     */
    @Transactional
    @Override
    public String insertModifyCaseInfo(InterfaceCaseInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        String caseName = target.getCaseName();
        String message = "";
        int num = 0;
        if("1".equals(target.getAddType())){
            //做覆盖更新
            //校验是否可以删除
            MdCaseDesignInfo mdInfo = new MdCaseDesignInfo();
            mdInfo.setProjectId(target.getProjectId());
            mdInfo.setScriptId(target.getScriptId());
            List<MdCaseDesignInfo> mdList = mdCaseDesignInfoService.query(mdInfo);
            if (mdList != null && mdList.size()>0){
                Map<String,Object> check = new HashMap<String,Object>();
                check.put("str",genQuery(mdList));
                check.put("projectId",target.getProjectId());
                List<ReCaseSet> linkList = reCaseSetService.queryCaseByScript(check);
                if(linkList != null && linkList.size()>0){
                    return  "案例已关联案例集,无法删除!";
                }else{
                    for (MdCaseDesignInfo info : mdList){
                        //修改表删除
                        caseDesignModifyService.deleteModifyCaseInfo(info.getId(),info.getProjectId());
                        //正式表删除
                        caseStepComponentService.deleteCaseInfo(info.getId(),info.getProjectId());
                    }
                }
            }
            num = 1;
        }else{
            MdCaseDesignInfo maxName = new MdCaseDesignInfo();
            maxName.setScriptId(target.getScriptId());
            maxName.setProjectId(target.getProjectId());
            String max = mdCaseDesignInfoService.queryMaxCaseName(maxName.toMap());
            if(max!=null && max.contains("_")){
                num = Integer.parseInt(max.split("_")[1])+1;
            }else {
                num = 1;
            }
        }

        List<String> idList = new ArrayList<String>();
        for(Map<String,String> map :target.getDataDesignDataList()){
            String id = tools.getUUID();
            target.setId(id);
            idList.add(id);
            String caseNo = getNewCaseNo(target.getProjectId());

            target.setCaseNo(caseNo);
            target.setCreateTime(new Date());
            target.setModifyTime(new Date());
            target.setVersion("1.0.0");
            target.setCaseName(caseName+"_"+getOrder(num));
            mdCaseDesignInfoService.insertInfoForMap(target.toMap());
            //步骤数据
            List<Map<String,Object>> caseStepList = new ArrayList<Map<String,Object>>();
            List<Map<String,Object>> compStepList = new ArrayList<Map<String,Object>>();
            List<Map<String,Object>> stepParamList = new ArrayList<Map<String,Object>>();
            target.setCaseId(target.getId());
            analysisCaseInfo(target,caseStepList,compStepList,stepParamList,map);
            mdCaseStepService.insertBatchForMap(caseStepList);
            mdComponentStepService.insertBatchForMap(compStepList);
            mdStepParameterService.insertBatchForMap(stepParamList);
            num++;
        }

        for(String id : idList){
            //插入完成开始版本提交
            MdCaseDesignInfo check = new MdCaseDesignInfo();
            check.setId(id);
            check.setProjectId(target.getProjectId());
            check.setModifyStaff(target.getModifyStaff());
            caseDesignVersionService.commitCaseDesignVersion(check);
        }
        return message;
    }

    /**
     * 智能接口数据生成智能接口组件
     * @param target
     * @return
     */
    @Transactional
    @Override
    public boolean genSmartPortComponent(Map<String, Object> target) {
        CaseDesignTools tools = new CaseDesignTools();
        List<Map<String,Object>> paramList = (List<Map<String, Object>>) target.get("paramList");

        ReComponentInfo info = reComponentInfoService.findByPrimaryKey(target.get("id")+"");
        ReComponentInfo reComInfo = new ReComponentInfo();
        reComInfo.setId(target.get("id")+""); //智能接口信息ID直接作为组件ID
        reComInfo.setModifyStaff(target.get("staff")+"");
        reComInfo.setModifyTime(new Date());
        reComInfo.setDescription(target.get("description")+"");
        reComInfo.setComponentName(target.get("name")+""); //接口名
        reComInfo.setDataName(reComInfo.getComponentName()); //同接口名
        reComInfo.setVersion(target.get("version")+"");
        reComInfo.setProjectId(target.get("projectId")+"");
        reComInfo.setParameterNumber(paramList==null?"0":paramList.size()+"");
        if(info == null){
            //新增
            reComInfo.setIsValid("1");
            reComInfo.setComponentFlag(Constant.comFlag_smartPort);//默认4为智能接口标识
            reComInfo.setComplexity("1");
            reComInfo.setCreateStaff(target.get("staff")+"");
            reComInfo.setCreateTime(new Date());
            reComInfo.setIsDeprecated("1"); //都为推荐使用
            //查询默认的接口执行组件
            ReComponentLibrary lib = new ReComponentLibrary();
            lib.setLibraryName(Constant.smartPortLib);
            List<ReComponentLibrary> smart = reComponentLibraryService.query(lib);
            if(smart==null && smart.size()<1){
                return false;
            }
            reComInfo.setLibraryId(smart.get(0).getId()); //此组件作为组件库的基础组件处理
            reComponentInfoService.insert(reComInfo);
        }else{
            //更新
            reComponentInfoService.updateByPrimaryKey(reComInfo);
        }

        //查询旧有参数
        ReComponentParameter paramOld = new ReComponentParameter();
        paramOld.setProjectId(reComInfo.getProjectId());
        paramOld.setComponentId(reComInfo.getId());
        List<ReComponentParameter> paramOldList = reComponentParameterService.query(paramOld);
        Map<String,String> oldMap = new HashMap<String,String>();
        for(ReComponentParameter one : paramOldList){
            oldMap.put(one.getParameterName(),one.getId());
        }
        //参数则统一处理_删除新增
        List<ReComponentParameter> insertList = new ArrayList<ReComponentParameter>();
        int order = 1;
        for(Map<String,Object> param : paramList){
            //解析参数
            ReComponentParameter comParam = new ReComponentParameter();
            BeanUtil.mapToBean(param,comParam);
            comParam.setId(oldMap.containsKey(comParam.getParameterName())?oldMap.get(comParam.getParameterName()):tools.getUUID());
            comParam.setParameterOrder(tools.getOrder(order++));
            comParam.setComponentId(reComInfo.getId());
            comParam.setInOrOut("0"); //无对外参数
            comParam.setProjectId(reComInfo.getProjectId());
            insertList.add(comParam);
        }
        ReComponentParameter paramDel = new ReComponentParameter();
        paramDel.setProjectId(reComInfo.getProjectId());
        paramDel.setComponentId(reComInfo.getId());
        reComponentParameterService.deleteByExample(paramDel);
        if(insertList.size()>0){
            reComponentParameterService.insertByBatch(insertList);
        }
        return true;
    }


    /**
     * 解析请求参数，生成关联表批量插入数据
     * @param caseStepList
     * @param compStepList
     * @param stepParamList
     * @param map
     */
    private void analysisCaseInfo(InterfaceCaseInfo target,List<Map<String,Object>> caseStepList,
                                  List<Map<String,Object>> compStepList,List<Map<String,Object>> stepParamList,
                                    Map<String,String> map){
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
                reComponentStep.setId(tools.getUUID());
                reComponentStep.setStepId(stepId);
                reComponentStep.setComponentId(stepComponent.getId());
                reComponentStep.setProjectId(target.getProjectId());
                reComponentStep.setModifyStaff(target.getModifyStaff());
                reComponentStep.setModifyTime(new Date());
                reComponentStep.setCaseId(target.getCaseId());
                reComponentStep.setComponentVersion(stepComponent.getVersion());
                reComponentStep.setComponentParamNum(stepComponent.getParameterNumber());
                compStepList.add(reComponentStep.toMap());

                //步骤参数
                int orderPar = 1;
                for(InterfaceComponentParameter reCompParam :stepComponent.getParamScriptList()){
                    ReStepParameter reStepParameter = new ReStepParameter();

                    reStepParameter.setId(tools.getUUID());
                    reStepParameter.setProjectId(target.getProjectId());
                    reStepParameter.setStepId(stepId);
                    reStepParameter.setModifyStaff(target.getModifyStaff());
                    reStepParameter.setModifyTime(new Date());
                    reStepParameter.setParameterId(reCompParam.getId());
                    reStepParameter.setParameterOrder(tools.getOrder(orderPar));
                    reStepParameter.setParameterValue(reCompParam.getDefaultValue());
                    reStepParameter.setCaseId(target.getCaseId());
                    //替换变量数据
                    if(reCompParam.getParamCode()!=null && map.containsKey(reCompParam.getParamCode())){
                        reStepParameter.setParameterValue(map.get(reCompParam.getParamCode()));
                    }
                    stepParamList.add(reStepParameter.toMap());
                    orderPar++;
                }
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
        CaseDesignModifyServiceImpl.caseNoMap.put(maxNo,"");
        String caseNo = "TC-"+tools.generateUniqueBusinessNo(CaseDesignModifyServiceImpl.caseNoMap,maxNo);
        return caseNo;
    }

    private String genQuery(List<MdCaseDesignInfo> list){
        String str = "";
        for (MdCaseDesignInfo info : list){
            if("".equals(str)){
                str=str + "'"+info.getId()+"'";
            }else {
                str=str + ",'"+info.getId()+"'";
            }
        }
        return str;
    }

    public static String getOrder(int order){
        String newOrder = "000000"+order;
        return newOrder.substring(newOrder.length()-5,newOrder.length());
    }
}
