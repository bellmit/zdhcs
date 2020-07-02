package net.northking.atp.impl;

import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceComponentInfo;
import net.northking.atp.entity.InterfaceComponentPackage;
import net.northking.atp.service.ComponentHisInfoService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import net.northking.atp.util.TargetTransformTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 组件历史表服务
 * Created by Administrator on 2019/6/10 0010.
 */
@Service
public class ComponentHisInfoServiceImpl implements ComponentHisInfoService {
    @Autowired
    private HisComponentInfoService hisComponentInfoService;
    @Autowired
    private HisComponentPackageService hisComponentPackageService;
    @Autowired
    private HisComponentParameterService hisComponentParameterService;
    @Autowired
    private ReComponentInfoService reComponentInfoService;
    @Autowired
    private ReComponentLibraryService reComponentLibraryService;
    @Autowired
    private HisInterfaceInfoService hisInterfaceInfoService;
    @Autowired
    private ComponentHisInfoService componentHisInfoService;

    /**
     * 插入组件历史表相关数据
     * @return
     更改留存记录_整体完成提交后删除
    @Override
    public boolean insertComponentHisInfo(InterfaceComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        String hisId = tools.getUUID(); //历史Id
        //插入初始版本的历史数据
        Map<String, Object> hisMap = target.toMap();
        hisMap.put("reId", hisMap.get("id"));
        hisMap.put("id", hisId);
        hisMap.put("hisCommitStaff", "admin");
        hisMap.put("hisCommitTime", tools.getNowTime());
        hisMap.put("hisChangeLog", "新建——初始版本");
        hisComponentInfoService.insertInfoForMap(hisMap);


        List<Map<String, Object>> hisParamList = new ArrayList<Map<String, Object>>();
        List<ReComponentParameter> customList = target.getParamList();
        for (ReComponentParameter one : customList) {
            Map<String, Object> hisParamMap = one.toMap();
            hisParamMap.put("id", tools.getUUID());
            hisParamMap.put("reId", one.getId());
            hisParamMap.put("hisId", hisId);
            hisParamList.add(hisParamMap);
        }

        List<InterfaceComponentPackage> compList = target.getPackageList();
        if(compList != null && compList.size()>0){
            //批量
            List<ReComponentPackage> cpList = new ArrayList<ReComponentPackage>();
            List<Map<String,Object>> hisPackList = new ArrayList<Map<String, Object>>();
            for(InterfaceComponentPackage comMap : compList){
                Map<String,Object> pack = comMap.toMap();
                pack.put("reId",comMap.getId());
                pack.put("id",tools.getUUID());
                pack.put("hisId",hisId);
                hisPackList.add(pack);

                int paramOrder = 1;
                for(ReComponentParameter one : comMap.getParamList()){
                    //生成历史版本数据
                    Map<String,Object> hisParMap = one.toMap();
                    hisMap.put("reId",one.getId());
                    hisMap.put("id",tools.getUUID());
                    hisMap.put("hisId",hisId);
                    hisParamList.add(hisParMap);
                    paramOrder++;
                }
            }
            hisComponentPackageService.insertBatchForMap(hisPackList);
        }

        //新增更改过后所有组件的参数
        if (customList.size() > 0) {
            hisComponentParameterService.insertBatchForMap(hisParamList);
        }
        return true;
    }*/

    /**
     * 组件历史表插入
     * @param infoMap
     * @return
     */
    public boolean insertComponentHisInfo(Map<String,Object> infoMap) {
        CaseDesignTools tools = new CaseDesignTools();
        String hisId = tools.getUUID(); //历史Id
        //插入初始版本的历史数据
        infoMap.put("reId", infoMap.get("id"));
        infoMap.put("id", hisId);
        infoMap.put("hisCommitStaff", infoMap.get("modifyStaff"));
        infoMap.put("hisCommitTime", tools.getNowTime());
        infoMap.put("hisChangeLog", "更新版本"+tools.getNowTime());
        hisComponentInfoService.insertInfoForMap(infoMap);

        List<Map<String,Object>> packList = (List<Map<String,Object>>)infoMap.get("packList");
        if(packList != null){
            for(Map<String,Object> packMap : packList){
                packMap.put("reId",packMap.get("id"));
                packMap.put("id",tools.getUUID());
                packMap.put("hisId",hisId);
            }
        }
        List<Map<String,Object>> paramList = (List<Map<String,Object>>)infoMap.get("paramList");
        if(paramList != null){
            for(Map<String,Object> parMap : paramList){
                parMap.put("reId",parMap.get("id"));
                parMap.put("hisId",hisId);
                parMap.put("id",tools.getUUID());
            }
        }
        hisComponentPackageService.insertBatchForMap(packList);
        hisComponentParameterService.insertBatchForMap(paramList);
        return true;
    }

    /**
     * 根据组件历史Id查询对应的组件信息以及封装参数信息
     * @param target
     * @return Map
     */
    @Override
    public Map<String, Object> queryComponentVersion(HisComponentInfo target) {
        //基础信息
        HisComponentInfo hisComInfo = hisComponentInfoService.findByPrimaryKey(target.getId());
        //封装信息
        List<Map<String,Object>> packList = new ArrayList<Map<String, Object>>();
        HisComponentPackage queryPack = new HisComponentPackage();
        queryPack.setHisId(hisComInfo.getId());
        queryPack.setProjectId(target.getProjectId());
        List<HisComponentPackage> hisPacList = hisComponentPackageService.query(queryPack);
        for(HisComponentPackage one : hisPacList){
            one.setId(one.getReId());
            packList.add(one.toMap());
        }
        //参数信息
        List<Map<String,Object>> paramList = new ArrayList<Map<String, Object>>();
        HisComponentParameter queryParam = new HisComponentParameter();
        queryParam.setHisId(hisComInfo.getId());
        queryParam.setProjectId(target.getProjectId());
        List<HisComponentParameter> hisParamList = hisComponentParameterService.query(queryParam);
        for(HisComponentParameter one : hisParamList){
            one.setId(one.getReId());
            paramList.add(one.toMap());
        }
        hisComInfo.setId(hisComInfo.getReId());
        Map<String,Object> hisMap = hisComInfo.toMap();
        hisMap.put("packList",packList);
        hisMap.put("paramList",paramList);
        return hisMap;
    }

    /**
     * 查询历史某版本详情
     * @param target
     * @return
     */
    @Override
    public InterfaceComponentInfo queryHisDetailComponentInfo(HisComponentInfo target) {
        TargetTransformTools tools = new TargetTransformTools();
        HisComponentInfo hisInfo = hisComponentInfoService.findByPrimaryKey(target.getId());
        InterfaceComponentInfo result = new InterfaceComponentInfo();
        result.setComponentName(hisInfo.getComponentName());
        result.setDataName(hisInfo.getDataName());
        result.setDescription(hisInfo.getDescription());
        result.setVersion(hisInfo.getVersion());
        result.setComplexity(hisInfo.getComplexity());
        result.setCreateStaff(hisInfo.getCreateStaff());
        result.setCreateTime(hisInfo.getCreateTime());

        HisComponentPackage query = new HisComponentPackage();
        query.setHisId(hisInfo.getId());
        query.setComponentId(hisInfo.getReId());
        List<Map<String,Object>> hisCpList = hisComponentPackageService.queryHisComPackByOrder(query);
        HisComponentParameter paramQuery = new HisComponentParameter();
        paramQuery.setHisId(hisInfo.getId());
        paramQuery.setComponentId(hisInfo.getReId());
        paramQuery.setInOrOut("0");//查询内部参数
        List<HisComponentParameter> paramList = hisComponentParameterService.queryHisCustomParamInfo(paramQuery);

        List<InterfaceComponentPackage> packList = new ArrayList<InterfaceComponentPackage>();
        for(Map<String,Object> comPac : hisCpList){
            List<ReComponentParameter> hisComParamList = new ArrayList<ReComponentParameter>();
            for (HisComponentParameter hisParam : paramList){
                if(comPac.get("basisComponentId").equals(hisParam.getRunComponentId())
                        && comPac.get("basisComponentOrder").equals(hisParam.getRunComponentOrder())){
                    ReComponentParameter reParam = tools.transHisParamToRe(hisParam);
                    hisComParamList.add(reParam);
                }
            }
            InterfaceComponentPackage interPack = new InterfaceComponentPackage();
            interPack.setComponentId(comPac.get("basisComponentId")+"");
            interPack.setId(comPac.get("basisComponentId")+"");
            interPack.setComponentNo(comPac.get("componentNo")+"");
            interPack.setComponentName(comPac.get("componentName")+"");
            interPack.setParamCheck("0");//先默认为0
            interPack.setComplexity(comPac.get("complexity")+"");
            interPack.setVersion(comPac.get("basisComponentVersion")+"");
            interPack.setBasisComponentParamNum(comPac.get("basisComponentParamNum") == null? null:comPac.get("basisComponentParamNum")+"");
            if("0".equals(comPac.get("isValid"))){
                //已经不存在的组件
                interPack.setParamCheck("3");
                ReComponentInfo comInfo = reComponentInfoService.findByPrimaryKey(interPack.getId());
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
                }else {
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
            interPack.setParamList(hisComParamList);
            packList.add(interPack);
        }
        result.setPackageList(packList);


        //查询高级组件自定义参数
        HisComponentParameter reCompParam = new HisComponentParameter();
        reCompParam.setHisId(hisInfo.getId());
        reCompParam.setComponentId(hisInfo.getReId());
        reCompParam.setInOrOut("1");//查询内部参数
        List<HisComponentParameter> outList = hisComponentParameterService.queryHisCustomParamInfo(reCompParam);
        List<ReComponentParameter> myselfList = new ArrayList<ReComponentParameter>();
        for(HisComponentParameter one : outList){
            ReComponentParameter pa = tools.transHisParamToRe(one);
            myselfList.add(pa);
        }
        result.setParamList(myselfList);
        return result;
    }

    /**
     * 从历史数据中查询被删除组件名称
     * @param comId
     * @return
     */
    @Override
    public String getComponentNameByHis(String comId) {
        String name = "已删除";
        //先查组件库
        HisComponentInfo hisQuery = new HisComponentInfo();
        hisQuery.setReId(comId);
        List<HisComponentInfo> queryList = hisComponentInfoService.query(hisQuery);
        if(queryList!=null && queryList.size()>0){
            name = queryList.get(0).getComponentName();
        }else{
            //去接口组件库查找
            HisInterfaceInfo query = new HisInterfaceInfo();
            query.setReId(comId);
            List<HisInterfaceInfo> list = hisInterfaceInfoService.query(query);
            if(list!=null && list.size()>0){
                name = list.get(0).getInterfaceName();
            }
        }
        return name;
    }
}
