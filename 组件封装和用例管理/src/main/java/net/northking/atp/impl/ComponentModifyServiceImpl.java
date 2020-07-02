package net.northking.atp.impl;

import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceComAndCaseCopy;
import net.northking.atp.entity.InterfaceComponentInfo;
import net.northking.atp.entity.InterfaceComponentPacModify;
import net.northking.atp.entity.InterfaceComponentPackage;
import net.northking.atp.service.ComponentHisInfoService;
import net.northking.atp.service.ComponentModifyService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import net.northking.atp.util.RedisTools;
import net.northking.atp.util.TargetTransformTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 高级组件封装修改表操作服务
 * Created by Administrator on 2019/6/4 0004.
 */
@Service
public class ComponentModifyServiceImpl implements ComponentModifyService{
    @Autowired
    private MdComponentInfoService mdComponentInfoService;
    @Autowired
    private MdComponentPackageService mdComponentPackageService;
    @Autowired
    private MdComponentParameterService mdComponentParameterService;
    @Autowired
    private ReComponentLibraryService reComponentLibraryService;
    @Autowired
    private HisComponentInfoService hisComponentInfoService;
    @Autowired
    private ComponentHisInfoService componentHisInfoService;
    //日志
    private static final Logger logger = LoggerFactory.getLogger(ComponentModifyServiceImpl.class);

    /**
     * 插入组件信息__修改表
     * @param target
     * @return
     */
    @Override
    public boolean insertComponentModifyInfo(InterfaceComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        //基础信息插入
        String componentNo = "TSC-" + tools.generateBusinessNo();
        target.setId(tools.getUUID());
        target.setComponentNo(componentNo);
        target.setComponentFlag("1"); //高级组件
        target.setCreateTime(new Date());
        target.setModifyTime(new Date());
        target.setIsValid("1");//默认有效
        target.setVersion("");//新建数据无版本号，置为空字符串
        if(target.getParamList() ==null || target.getParamList().size()<1){
            target.setParameterNumber("0");
        }else{
            target.setParameterNumber(String.valueOf(target.getParamList().size()));
        }
        //插入外部自定义输入输出参数
        List<Map<String, Object>> mdParamList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> mdPackList = new ArrayList<Map<String, Object>>();
        //解析参数_计算复杂度
        int complexity = analysisComponentInfo(target,mdParamList,mdPackList);
        target.setComplexity(complexity+"");
        mdComponentInfoService.insertInfoForMap(target.toMap());
        mdComponentPackageService.insertBatchForMap(mdPackList);
        mdComponentParameterService.insertBatchForMap(mdParamList);
        return true;
    }

    /**
     * 修改组件信息__修改表
     * @param target
     * @return
     */
    @Override
    public boolean updateComponentModifyInfo(InterfaceComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        MdComponentInfo old = mdComponentInfoService.findByPrimaryKey(target.getId());
        String version = old.getVersion();
        target.setModifyTime(new Date());
        target.setVersion(version);
        if(target.getParamList() ==null || target.getParamList().size()<1){
            target.setParameterNumber("0");
        }else{
            target.setParameterNumber(String.valueOf(target.getParamList().size()));
        }
        //原始数据
        mdComponentInfoService.deleteByPrimaryKey(target.getId());

        MdComponentPackage cpDelete = new MdComponentPackage();
        cpDelete.setProjectId(target.getProjectId());
        cpDelete.setComponentId(target.getId());
        mdComponentPackageService.deleteByExample(cpDelete);
        //原始参数删除
        MdComponentParameter paramDelete = new MdComponentParameter();
        paramDelete.setProjectId(target.getProjectId());
        paramDelete.setComponentId(target.getId());
        mdComponentParameterService.deleteByExample(paramDelete);
        logger.info("+++++ComponentInfoController.updateComponentInfo+++++原始数据删除成功");

        //定义数据集合
        List<Map<String,Object>> mdPackList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> mdParamList = new ArrayList<Map<String, Object>>();
        //解析数据
        int complexity = analysisComponentInfo(target,mdParamList,mdPackList);
        Map<String,Object> info = old.toMap();
        target.setComplexity(complexity+"");
        info.putAll(target.toMap());
        mdComponentInfoService.insertInfoForMap(info);
        mdComponentPackageService.insertBatchForMap(mdPackList);
        mdComponentParameterService.insertBatchForMap(mdParamList);
        return true;
    }

    /**
     * 根据mdId删除组件信息以及对应的封装参数等信息
     * @param comId
     * @return
     */
    @Override
    public boolean deleteComponentModifyInfo(String comId,String projectId) {
        //删除信息表
        mdComponentInfoService.deleteByPrimaryKey(comId);
        //删除封装表
        MdComponentPackage deletePack = new MdComponentPackage();
        deletePack.setProjectId(projectId);
        deletePack.setComponentId(comId);
        mdComponentPackageService.deleteByExample(deletePack);
        //删除参数
        MdComponentParameter deleteParam = new MdComponentParameter();
        deleteParam.setProjectId(projectId);
        deleteParam.setComponentId(comId);
        mdComponentParameterService.deleteByExample(deleteParam);
        return true;
    }

    /**
     * 根据主键查询修改表组件信息以及对应自定义参数
     * @param id
     * @return
     */
    @Override
    public InterfaceComponentInfo queryComponentModifyInfo(String id) {
        TargetTransformTools tools = new TargetTransformTools();
        MdComponentInfo record = mdComponentInfoService.findByPrimaryKey(id);

        InterfaceComponentInfo interCompInfo = new InterfaceComponentInfo();
        //查询高级组件自定义参数
        MdComponentParameter reCompParam = new MdComponentParameter();
        reCompParam.setProjectId(record.getProjectId());
        reCompParam.setComponentId(record.getId());
        reCompParam.setInOrOut("1");//查询外部参数
        List<MdComponentParameter> paramList = mdComponentParameterService.queryCustomParamInfo(reCompParam);
        interCompInfo.setParamList(tools.transModifyToFormal(paramList));

        interCompInfo.setId(record.getId());
        interCompInfo.setComponentNo(record.getComponentNo());
        interCompInfo.setComponentName(record.getComponentName());
        interCompInfo.setCaseType(record.getCaseType());
        interCompInfo.setDescription(record.getDescription());
        interCompInfo.setProjectId(record.getProjectId());
        interCompInfo.setComponentFlag(record.getComponentFlag());
        interCompInfo.setDataName(record.getDataName());
        interCompInfo.setLibraryId(record.getLibraryId());
        return interCompInfo;
    }

    /**
     * 查询组件的封装以及参数信息_修改表
     * @param target
     * @return
     */
    @Override
    public List<InterfaceComponentPackage> queryComponentAndParamList(ReComponentInfo target) {
        List<InterfaceComponentPackage> result = new ArrayList<InterfaceComponentPackage>();
        TargetTransformTools tools = new TargetTransformTools();
        //通过高级组件编号查询基础组件的顺序
        MdComponentPackage query = new MdComponentPackage();
        query.setProjectId(target.getProjectId());
        query.setComponentId(target.getId());
        List<Map<String,Object>> mdCpList = mdComponentPackageService.queryCompModifyByOrder(query.toMap());
        //查询高级组件对应的所有子组件参数列表
        MdComponentParameter paramQuery = new MdComponentParameter();
        paramQuery.setProjectId(target.getProjectId());
        paramQuery.setComponentId(target.getId());
        paramQuery.setInOrOut("0");//查询内部参数
        List<MdComponentParameter> paramList = mdComponentParameterService.queryCustomParamInfo(paramQuery);
        for(Map<String,Object> comPac : mdCpList){
            List<ReComponentParameter> reComParamList = new ArrayList<ReComponentParameter>();
            for (MdComponentParameter mdPack : paramList){
                if(comPac.get("basisComponentId").equals(mdPack.getRunComponentId())
                        && comPac.get("basisComponentOrder").equals(mdPack.getRunComponentOrder())){
                    ReComponentParameter reParam = tools.transMdParamToRe(mdPack);
                    reComParamList.add(reParam);
                }
            }
            InterfaceComponentPackage interPack = new InterfaceComponentPackage();
            interPack.setComponentId(comPac.get("basisComponentId")+"");
            interPack.setId(comPac.get("basisComponentId")+"");
            interPack.setComponentNo(comPac.get("componentNo")+"");
            interPack.setComponentName(comPac.get("componentName")+"");
            interPack.setBasisComponentParamNum(comPac.get("basisComponentParamNum") == null? null:comPac.get("basisComponentParamNum")+"");
            interPack.setParamCheck("0");//先默认为0
            interPack.setComplexity(comPac.get("complexity")+"");
            interPack.setComponentFlag(comPac.get("componentFlag")+"");
            interPack.setVersion(comPac.get("basisComponentVersion")+"");
            interPack.setLibraryName(comPac.get("libraryName")+"");
            if("0".equals(comPac.get("isValid"))){
                interPack.setParamCheck("3");
                MdComponentInfo comInfo = mdComponentInfoService.findByPrimaryKey(interPack.getId());
                ReComponentLibrary libraryInfo = reComponentLibraryService.findByPrimaryKey(comInfo.getLibraryId());
                String libraryName = libraryInfo.getLibraryName();
                interPack.setCheckMessage("组件库"+libraryName+"已禁用或删除，组件以无法使用，请更换组件以免影响您使用");
            }else{
                if(comPac.get("isValid") == null){
                    interPack.setParamCheck("3");
                    interPack.setCheckMessage("组件已删除,请更换其他组件使用");
                    //去历史库查询组件名称
                    String name = componentHisInfoService.getComponentNameByHis(interPack.getId());
                    interPack.setComponentName(name);
                }else{
                    if(interPack.getBasisComponentParamNum() != null &&
                            !interPack.getBasisComponentParamNum().equals(comPac.get("parameterNumber"))){
                        interPack.setParamCheck("3");
                        interPack.setCheckMessage("组件参数已进行更新，请使用新版本组件，详情请查看组件信息");
                    }
                }
                if("0".equals(comPac.get("isDeprecated"))){
                    //组件为不推荐状态
                    interPack.setParamCheck("2");
                    interPack.setCheckMessage("组件已有更优化的版本，请查看组件描述使用优化版本组件");
                }
            }
            interPack.setParamList(reComParamList);
            result.add(interPack);
        }
        return result;
        /*/进行高级组件的参数同步校验
        String message = "此组件参数已进行更新，对比当前步骤:\n";
        for(InterfaceComponentPacModify checkCom : result){
            if("1".equals(checkCom.getComponentFlag())){
                //进行高级组件的参数与对应步骤组件参数进行对比
                ReComponentParameter queryParam = new ReComponentParameter();
                queryParam.setComponentId(checkCom.getId());
                queryParam.setProjectId(checkCom.getProjectId());
                queryParam.setInOrOut("1");
                List<ReComponentParameter> comParamList = mdComponentParameterService.query(queryParam);
                List<ReComponentParameter> stepParamList = checkCom.getParamList();

                //增加的
                Map<String,ReComponentParameter> oneMap = new HashMap<String,ReComponentParameter>();
                for(ReComponentParameter step : stepParamList){
                    oneMap.put(step.getId(),step);
                }
                String add = "增加参数:";
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
                MdStepParameter tableQuery = new MdStepParameter();
                tableQuery.setProjectId(checkCom.getProjectId());
                tableQuery.setStepId(checkCom.getStepId());
                List<MdStepParameter> tableList = mdStepParameterService.query(tableQuery);
                Map<String,ReComponentParameter> twoMap = new HashMap<String,ReComponentParameter>();
                for(ReComponentParameter step : comParamList){
                    twoMap.put(step.getId(),step);
                }
                String reduce = "\n去掉参数:";
                int reduceNum=0;
                for(MdStepParameter com : tableList){
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
        }*/
    }

    /**
     * 查询组件信息以及全部的封装参数信息，生成Map
     * @param target
     * @return
     */
    @Override
    public Map<String, Object> queryComponentToMap(MdComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        MdComponentInfo comInfo = mdComponentInfoService.findByPrimaryKey(target.getId());
        Map<String,Object> comMap = comInfo.toMap();
        //查询组件封装
        MdComponentPackage reCPQuery = new MdComponentPackage();
        reCPQuery.setComponentId(comInfo.getId());
        reCPQuery.setProjectId(comInfo.getProjectId());
        List<MdComponentPackage> packList = mdComponentPackageService.query(reCPQuery);
        List<Map<String,Object>> rePackList = new ArrayList<Map<String, Object>>();
        for(MdComponentPackage one : packList){
            rePackList.add(one.toMap());
        }

        //查询组件参数
        MdComponentParameter comParamQuery = new MdComponentParameter();
        comParamQuery.setProjectId(comInfo.getProjectId());
        comParamQuery.setComponentId(comInfo.getId());
        List<MdComponentParameter> paramList = mdComponentParameterService.query(comParamQuery);
        List<Map<String,Object>> reParamList = new ArrayList<Map<String, Object>>();
        for(MdComponentParameter one : paramList){
            reParamList.add(one.toMap());
        }
        comMap.put("packList",rePackList);
        comMap.put("paramList",reParamList);
        return comMap;
    }

    /**
     * 现有数据直接插入修改表
     * @param infoMap
     * @return
     */
    @Override
    public boolean insertComponentModifyByMap(Map<String, Object> infoMap) {
        mdComponentInfoService.insertInfoForMap(infoMap);
        //封装信息
        List<Map<String,Object>> packList = (List<Map<String,Object>>)infoMap.get("packList");
        mdComponentPackageService.insertBatchForMap(packList);
        //参数信息
        List<Map<String,Object>> paramList = (List<Map<String,Object>>)infoMap.get("paramList");
        mdComponentParameterService.insertBatchForMap(paramList);
        return false;
    }

    /**
     * 校验组件版本
     * @param comInfo
     * @return
     */
    @Override
    public String checkComPackageVersion(MdComponentInfo comInfo) {
        //查询所有封装数据以及版本
        String message = "none";
        List<Map<String,Object>> mdCpList = mdComponentPackageService.queryCompModifyByOrder(comInfo.toMap());
        for(Map<String,Object> comPac : mdCpList){
            if("0".equals(comPac.get("isValid"))){
                message="组件库"+comPac.get("componentName")+"已禁用或删除，请刷新数据查看";
            }else{
                if(comPac.get("isValid") == null){
                    //去历史库查询组件名称
                    String name = componentHisInfoService.getComponentNameByHis(comInfo.getId());
                    message= "组件"+name+"已删除,请更换其他组件使用";
                }else {
                    if(comPac.get("basisComponentParamNum") != null &&
                            !comPac.get("basisComponentParamNum").equals(comPac.get("parameterNumber"))){
                        message="组件"+comPac.get("componentName")+"已进行更新，请使用新版本组件，详情请查看组件信息";
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
     * 复制组件数据到指定目录下
     * @param target
     */
    @Transactional
    @Override
    public void CopyComponentDataToMenu(InterfaceComAndCaseCopy target) {
        CaseDesignTools tools = new CaseDesignTools();
        for (String id  : target.getIdList()){ //循环组件信息
            //查询组件信息
            logger.info("复制目标主键:"+id);
            MdComponentInfo mdInfo = mdComponentInfoService.findByPrimaryKey(id);
            if(mdInfo==null){
                continue;
            }
            String mdName = mdInfo.getComponentName();
            MdComponentInfo mdQuery =  new MdComponentInfo();
            mdQuery.setComponentName(mdName+"_复制(%%)"); //取复制后括号类数值最大的一个
            mdQuery.setProjectId(target.getProjectId());
            String maxName = mdComponentInfoService.queryMaxNameByName(mdQuery);
            String newName = "";
            if(maxName==null || maxName.isEmpty()){
                newName = mdName+"_复制(1)";
            }else{
                newName= getNewMaxName(maxName);
            }

            //查询组件封装以及参数信息
            MdComponentPackage packQuery = new MdComponentPackage();
            packQuery.setProjectId(target.getProjectId());
            packQuery.setComponentId(mdInfo.getId());
            List<MdComponentPackage> packList = mdComponentPackageService.query(packQuery);

            MdComponentParameter paramQuery = new MdComponentParameter();
            paramQuery.setProjectId(mdInfo.getProjectId());
            paramQuery.setComponentId(mdInfo.getId());
            List<MdComponentParameter> paramList = mdComponentParameterService.query(paramQuery);
            //处理数据
            String newId = tools.getUUID();
            mdInfo.setId(newId);
            String componentNo = "TSC-" + tools.generateBusinessNo();
            mdInfo.setComponentNo(componentNo);
            mdInfo.setComponentFlag("1"); //高级组件
            mdInfo.setComponentName(newName);
            mdInfo.setCreateTime(new Date());
            mdInfo.setModifyTime(new Date());
            mdInfo.setIsValid("1");//默认有效
            mdInfo.setVersion("");//新建数据无版本号，置为空字符串
            mdInfo.setCreateStaff(target.getModifyStaff());
            mdInfo.setModifyStaff(target.getModifyStaff());
            for (MdComponentPackage pack : packList){
                pack.setId(tools.getUUID());
                pack.setComponentId(newId);
            }
            for(MdComponentParameter param : paramList){
                param.setId(tools.getUUID());
                param.setComponentId(newId);
            }

            //md数据插入
            mdComponentInfoService.insert(mdInfo);
            if(packList!=null&&packList.size()>0){
                mdComponentPackageService.insertByBatch(packList);
            }
            if(paramList!=null&&paramList.size()>0){
                mdComponentParameterService.insertByBatch(paramList);
            }
            logger.info("复制组件成功:"+mdInfo.getId());
        }
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

    /**
     * 解析请求参数按固定模式生成插入数据
     * @param target
     */
    private int analysisComponentInfo(InterfaceComponentInfo target,List<Map<String, Object>> mdParamList,
                         List<Map<String, Object>> mdPackList){
        int complexity = 0;
        CaseDesignTools tools = new CaseDesignTools();
        List<ReComponentParameter> customList = target.getParamList();
        int customOrder = 1;
        for (ReComponentParameter one : customList) {
            if(one.getId() == null){
                one.setId(tools.getUUID());
            }
            one.setParameterOrder(tools.getOrder(customOrder));
            one.setProjectId(target.getProjectId());
            one.setComponentId(target.getId());
            one.setParameterName(one.getParameterName().trim());
            one.setInOrOut("1");
            mdParamList.add(one.toMap());
            customOrder++;

        }
        //分析新增参数list
        int order = 1;
        List<InterfaceComponentPackage> compList = target.getPackageList();
        if(compList != null && compList.size()>0){
            //批量
            for(InterfaceComponentPackage comMap : compList){
                complexity = complexity + Integer.parseInt(comMap.getComplexity());
                ReComponentPackage reCP = new ReComponentPackage();
                if(reCP.getId() == null){
                    reCP.setId(tools.getUUID());
                }
                reCP.setComponentId(target.getId());
                reCP.setProjectId(target.getProjectId());
                reCP.setBasisComponentId(comMap.getId());
                reCP.setBasisComponentOrder(tools.getOrder(order));
                reCP.setBasisComponentVersion(comMap.getVersion());
                reCP.setBasisComponentParamNum(comMap.getParameterNumber());
                reCP.setProjectId(target.getProjectId());
                reCP.setModifyStaff(target.getStaffName());
                reCP.setModifyTime(new Date());
                mdPackList.add(reCP.toMap());

                int paramOrder = 1;
                for(ReComponentParameter one : comMap.getParamList()){
                    if(one.getComponentId().equals(reCP.getBasisComponentId())){
                        one.setRunComponentId(reCP.getBasisComponentId());
                        one.setId(tools.getUUID());
                    }
                    one.setParameterOrder(tools.getOrder(paramOrder));
                    one.setRunComponentOrder(tools.getOrder(order));
                    one.setComponentId(reCP.getComponentId());
                    one.setInOrOut("0");//内部运行参数
                    one.setProjectId(target.getProjectId());
                    mdParamList.add(one.toMap());
                    paramOrder++;
                }
                order++;
            }
        }
        return complexity;
    }


}
