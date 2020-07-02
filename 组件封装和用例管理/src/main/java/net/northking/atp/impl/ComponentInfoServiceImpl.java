package net.northking.atp.impl;

import net.northking.atp.controller.ComponentLibraryController;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceComponentInfo;
import net.northking.atp.entity.InterfaceComponentPacModify;
import net.northking.atp.entity.InterfaceComponentPackage;
import net.northking.atp.service.ComponentInfoService;
import net.northking.atp.util.CaseDesignTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.rmi.transport.ObjectTable;

import java.util.*;

/**
 * 组件维护处理
 * Created by Administrator on 2019/4/9 0009.
 */
@Service
public class ComponentInfoServiceImpl implements ComponentInfoService{
    @Autowired
    ReComponentLibraryService reComponentLibraryService; //组件库
    @Autowired
    ReComponentInfoService reComponentInfoService; //组件信息表
    @Autowired
    ReComponentPackageService reComponentPackageService; //组件封装表
    @Autowired
    ReComponentParameterService reComponentParameterService; //组件参数表
    @Autowired
    HisComponentInfoService hisComponentInfoService; //组件信息版本表
    @Autowired
    HisComponentParameterService hisComponentParameterService; //组件参数版本表
    @Autowired
    HisComponentPackageService hisComponentPackageService; //组件封住表版本表

    //日志
    private static final Logger logger = LoggerFactory.getLogger(ComponentInfoServiceImpl.class);

    /**
     * 校验正式表中是否存在重名数据
     * @param target
     * @return
     */
    @Override
    public boolean checkComponentExist(ReComponentInfo target) {
        ReComponentInfo checkQuery = new ReComponentInfo();
        checkQuery.setComponentFlag("1");
        checkQuery.setComponentName(target.getComponentName());
        checkQuery.setProjectId(target.getProjectId());
        List<ReComponentInfo> checkList = reComponentInfoService.query(checkQuery);
        if(checkList != null && checkList.size()>0){
            if(checkList.get(0).getId().equals(target.getId())){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    /**
     * 查询高级组件封装的基础组件以及组件的参数
     * @param target
     * @return
     */
    @Override
    public List<InterfaceComponentPackage> queryComponentAndParamList(InterfaceComponentInfo target) {
        List<InterfaceComponentPackage> result = new ArrayList<InterfaceComponentPackage>();
        //通过高级组件编号查询基础组件的顺序
        ReComponentPackage query = new ReComponentPackage();
        query.setProjectId(target.getProjectId());
        query.setComponentId(target.getId());
        List<Map<String,Object>> recpList = reComponentPackageService.queryComponentByOrder(query.toMap());
        //查询高级组件对应的所有子组件参数列表
        ReComponentParameter paramQuery = new ReComponentParameter();
        paramQuery.setProjectId(target.getProjectId());
        paramQuery.setComponentId(target.getId());
        paramQuery.setInOrOut("0");//查询内部参数
        List<ReComponentParameter> paramList = reComponentParameterService.queryCustomParamInfo(paramQuery);
        for(Map<String,Object> comPac : recpList){
            List<ReComponentParameter> reComParamList = new ArrayList<ReComponentParameter>();
            for (ReComponentParameter rePack : paramList){
                if(comPac.get("basisComponentId").equals(rePack.getRunComponentId())
                        && comPac.get("basisComponentOrder").equals(rePack.getRunComponentOrder())){
                    reComParamList.add(rePack);
                }
            }
            InterfaceComponentPackage interPack = new InterfaceComponentPackage();
            interPack.setComponentId(comPac.get("basisComponentId")+"");
            interPack.setId(comPac.get("basisComponentId")+"");
            interPack.setComponentNo(comPac.get("componentNo")+"");
            interPack.setComponentName(comPac.get("componentName")+"");
            interPack.setParamCheck("0");//先默认为0
            if(!"1".equals(comPac.get("isValid"))){
                //已经不存在的组件
                interPack.setParamCheck("3");
                ReComponentInfo comInfo = reComponentInfoService.findByPrimaryKey(interPack.getId());
                ReComponentLibrary libraryInfo = reComponentLibraryService.findByPrimaryKey(comInfo.getLibraryId());
                String libraryName = libraryInfo.getLibraryName();
                interPack.setCheckMessage("组件库"+libraryName+"已禁用或删除，组件以无法使用，请更换组件以免影响您使用");
            }else{
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
    }

    /**
     * 高级组件信息及封装数据更新_正式表
     * @param infoMap
     * @return
     */
    @Override
    public boolean insertFormalComponentInfo(Map<String,Object> infoMap) {
        //组件信息
        reComponentInfoService.insertInfoForMap(infoMap);
        //封装信息
        List<Map<String,Object>> packList = (List<Map<String,Object>>)infoMap.get("packList");
        reComponentPackageService.insertBatchForMap(packList);
        //参数信息
        List<Map<String,Object>> paramList = (List<Map<String,Object>>)infoMap.get("paramList");
        reComponentParameterService.insertBatchForMap(paramList);
        return true;
    }

    /**
     * 修改高级组件信息及封装数据——不生成历史版本
     * @param target
     * @return
     */
    @Override
    public boolean updateComponentInfo(InterfaceComponentInfo target) {
        CaseDesignTools tools = new CaseDesignTools();

        //附加信息处理处理
        //原始封装删除
        ReComponentPackage cpDelete = new ReComponentPackage();
        cpDelete.setProjectId(target.getProjectId());
        cpDelete.setComponentId(target.getId());
        reComponentPackageService.deleteByExample(cpDelete);
        //原始参数删除
        ReComponentParameter paramDelete = new ReComponentParameter();
        paramDelete.setProjectId(target.getProjectId());
        paramDelete.setComponentId(target.getId());
        reComponentParameterService.deleteByExample(paramDelete);
        logger.info("+++++ComponentInfoController.updateComponentInfo+++++原始数据删除成功");

        //处理外部自定义输入输出参数
        List<ReComponentParameter> customList = target.getParamList();
        int customOrder = 1;
        for (ReComponentParameter one : customList) {
            if (one.getId() == null) {
                one.setId(UUID.randomUUID().toString().replace("-", ""));
            }
            one.setParameterOrder(tools.getOrder(customOrder));
            one.setProjectId(target.getProjectId());
            one.setComponentId(target.getId());
            one.setInOrOut("1");//外部定义参数
            one.setModifyStaff(target.getStaffName());
            one.setModifyTime(new Date());
            one.setParameterName(one.getParameterName().trim());
            customOrder++;
        }

        //删除完成在整体新增
        int order = 1;
        List<InterfaceComponentPackage> compList = target.getPackageList();
        if(compList != null && compList.size()>0){
            //批量
            List<ReComponentPackage> cpList = new ArrayList<ReComponentPackage>();
            for(InterfaceComponentPackage comMap : compList){
                ReComponentPackage reCP = new ReComponentPackage();
                reCP.setId(UUID.randomUUID().toString().replace("-", ""));
                reCP.setComponentId(target.getId());
                reCP.setProjectId(target.getProjectId());
                reCP.setBasisComponentId(comMap.getId());
                reCP.setBasisComponentOrder(tools.getOrder(order));
                reCP.setProjectId(target.getProjectId());
                reCP.setModifyStaff(target.getStaffName());
                reCP.setModifyTime(new Date());
                cpList.add(reCP);

                int paramOrder = 1;
                for(ReComponentParameter one : comMap.getParamList()){
                    if(one.getComponentId().equals(reCP.getBasisComponentId())){
                        one.setRunComponentId(reCP.getBasisComponentId());
                        one.setId(UUID.randomUUID().toString().replace("-", ""));
                    }
                    one.setParameterOrder(tools.getOrder(paramOrder));
                    one.setRunComponentOrder(tools.getOrder(order));
                    one.setComponentId(reCP.getComponentId());
                    one.setInOrOut("0");//内部运行参数
                    customList.add(one);
                    paramOrder++;
                }
                order++;
            }
            if(cpList.size()>0){
                reComponentPackageService.insertByBatch(cpList);
                logger.info("+++++ComponentInfoController.updateComponentInfo+++++封装数据插入成功");
            }
        }

        if(customList.size()>0){
            reComponentParameterService.insertByBatch(customList);
            logger.info("+++++ComponentInfoController.updateComponentInfo+++++封装参数插入成功");
        }
        return true;
    }

    /**
     * 删除正式表组件信息以及分装参数信息
     * @param target
     * @return
     */
    @Override
    public boolean deleteComponentInfo(ReComponentInfo target) {
        //组件删除
        reComponentInfoService.deleteByPrimaryKey(target.getId());
        //组件封装删除
        ReComponentPackage packDelete = new ReComponentPackage();
        packDelete.setComponentId(target.getId());
        packDelete.setProjectId(target.getProjectId());
        reComponentPackageService.deleteByExample(packDelete);
        //组件参数删除
        ReComponentParameter paramDelete = new ReComponentParameter();
        paramDelete.setComponentId(target.getId());
        paramDelete.setProjectId(target.getProjectId());
        reComponentParameterService.deleteByExample(paramDelete);
        return true;
    }

    /**
     * 根据组件Id查询所属组件库
     * @param id
     * @return
     */
    @Override
    public String queryLibraryNameByComId(String id) {
        ReComponentInfo comInfo = reComponentInfoService.findByPrimaryKey(id);
        ReComponentLibrary libraryInfo = reComponentLibraryService.findByPrimaryKey(comInfo.getLibraryId());
        String libraryName = libraryInfo.getLibraryName();
        return libraryName;
    }


}
