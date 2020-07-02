package net.northking.atp.impl;

import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceComponentInfo;
import net.northking.atp.entity.InterfaceComponentPacModify;
import net.northking.atp.service.CaseDesignVersionService;
import net.northking.atp.service.CaseStepComponentService;
import net.northking.atp.service.InterfaceInfoFormalService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 组件以及案例的版本管理服务
 * Created by Administrator on 2019/6/6 0006.
 */
@Service
public class CaseDesignVersionServiceImpl implements CaseDesignVersionService{
    @Autowired
    private ComponentInfoServiceImpl componentInfoService; //组件正式表我服
    @Autowired
    private ComponentHisInfoServiceImpl componentHisInfoService; //组件历史表
    @Autowired
    private ComponentModifyServiceImpl componentModifyService;//修改表操作服务
    @Autowired
    private ReComponentInfoService reComponentInfoService;
    @Autowired
    private MdComponentInfoService mdComponentInfoService;


    /**
     * 高级组件提交版本更新
     * @param target
     * @return
     */
    @Override
    public boolean commitComponentVersion(MdComponentInfo target) {
        //数据更新至正式表以及历史表
        CaseDesignTools tools = new CaseDesignTools();
        //查询组件信息，参数信息作为当前更新版本数据
        Map<String,Object> comMap = componentModifyService.queryComponentToMap(target);
        //更新版本号
        String newVersion = tools.getNextVersion(comMap.get("version")+"");
        comMap.put("version",newVersion);
        comMap.put("modifyStaff",target.getModifyStaff());
        //删除正式表数据
        ReComponentInfo reComponentInfo = new ReComponentInfo();
        reComponentInfo.setId(target.getId());
        reComponentInfo.setProjectId(target.getProjectId());
        componentInfoService.deleteComponentInfo(reComponentInfo);
        //插入正式表数据
        componentInfoService.insertFormalComponentInfo(comMap);
        //插入历史表数据
        componentHisInfoService.insertComponentHisInfo(comMap);
        //更新修改表版本号
        MdComponentInfo update = new MdComponentInfo();
        update.setId(target.getId());
        update.setVersion(newVersion);
        mdComponentInfoService.updateByPrimaryKey(update);
        return true;
    }

    /**
     * 高级组件版本回滚
     * @param target
     * @return
     */
    @Override
    public boolean rollbackComponentVersion(HisComponentInfo target) {
        //数据更新至修改表以及正式表
        CaseDesignTools tools = new CaseDesignTools();
        Map<String,Object> hisMap = componentHisInfoService.queryComponentVersion(target);
        //删除正式表数据
        ReComponentInfo reComponentInfo = new ReComponentInfo();
        reComponentInfo.setId(hisMap.get("id")+"");
        reComponentInfo.setProjectId(target.getProjectId());
        //查询正式表版本号，增1位新版本好
        String version = reComponentInfoService.findByPrimaryKey(reComponentInfo.getId()).getVersion();
        String newVersion = tools.getNextVersion(version);
        hisMap.put("version",newVersion);
        hisMap.put("modifyStaff",target.getModifyStaff());
        componentInfoService.deleteComponentInfo(reComponentInfo);
        //插入正式表数据
        componentInfoService.insertFormalComponentInfo(hisMap);
        //删除修改表数据
        componentModifyService.deleteComponentModifyInfo(hisMap.get("id")+"",target.getProjectId());
        //插入修改表数据
        componentModifyService.insertComponentModifyByMap(hisMap);
        //将版本号增加重新再插入历史表
        componentHisInfoService.insertComponentHisInfo(hisMap);
        return true;
    }

    @Autowired
    private CaseDesignModifyServiceImpl caseDesignModifyService; //修改表
    @Autowired
    private CaseStepComponentServiceImpl caseStepService; //正式表
    @Autowired
    private CaseDesignHisServiceImpl caseDesignHisService; //历史表
    @Autowired
    private ReCaseDesignInfoService reCaseDesignInfoService;
    @Autowired
    private MdCaseDesignInfoService mdCaseDesignInfoService;

    /**
     * 测试案例版本提交
     * @param target
     * @return
     */
    @Override
    public boolean commitCaseDesignVersion(MdCaseDesignInfo target) {
        //数据更新至正式表以及历史表
        CaseDesignTools tools = new CaseDesignTools();
        //查询组件信息，参数信息作为当前更新版本数据
        Map<String,Object> caseMap = caseDesignModifyService.queryCaseInfoToMap(target);
        //更新版本号
        String newVersion = tools.getNextVersion(caseMap.get("version")+"");
        caseMap.put("version",newVersion);
        caseMap.put("modifyStaff",target.getModifyStaff());
        //删除正式表数据
        caseStepService.deleteCaseInfo(target.getId(),target.getProjectId());
        //插入正式表数据
        caseStepService.insertFormalCaseInfo(caseMap);
        //插入历史表数据
        caseDesignHisService.insertCaseInfoHis(caseMap);
        //更新修改表版本号
        MdCaseDesignInfo update = new MdCaseDesignInfo();
        update.setId(target.getId());
        update.setVersion(newVersion);
        mdCaseDesignInfoService.updateByPrimaryKey(update);
        return true;
    }

    /**
     * 测试案例版本回滚
     * @param target
     * @return
     */
    @Override
    public boolean rollbackCaseDesignVersion(HisCaseDesignInfo target) {
        //数据更新至修改表，正式表，历史表
        CaseDesignTools tools = new CaseDesignTools();
        Map<String,Object> hisMap = caseDesignHisService.queryCaseInfoVersion(target);
        //更新版本号
        ReCaseDesignInfo reCaseDesignInfo = new ReCaseDesignInfo();
        reCaseDesignInfo.setId(hisMap.get("id")+"");
        reCaseDesignInfo.setProjectId(target.getProjectId());
        //查询正式表版本号，增1位新版本好
        String version = reCaseDesignInfoService.findByPrimaryKey(reCaseDesignInfo.getId()).getVersion();
        hisMap.put("version",tools.getNextVersion(version));
        hisMap.put("modifyStaff",target.getModifyStaff());
        //删除正式表数据
        caseStepService.deleteCaseInfo(hisMap.get("id")+"",target.getProjectId());
        //插入正式表数据
        caseStepService.insertFormalCaseInfo(hisMap);
        //删除修改表数据
        caseDesignModifyService.deleteModifyCaseInfo(hisMap.get("id")+"",target.getProjectId());
        //插入修改表数据
        caseDesignModifyService.insertModifyCaseByMap(hisMap);
        //插入历史表
        caseDesignHisService.insertCaseInfoHis(hisMap);
        return true;
    }

    @Autowired
    private InterfaceInfoFormalServiceImpl interfaceInfoFormalService;
    @Autowired
    private InterfaceInfoModifyServiceImpl interfaceInfoModifyService;
    @Autowired
    private InterfaceInfoHisServiceImpl interfaceInfoHisService;
    @Autowired
    private ReInterfaceInfoService reInterfaceInfoService;
    @Autowired
    private MdInterfaceInfoService mdInterfaceInfoService;
    @Autowired
    private ReComponentParameterService reComponentParameterService;
    /**
     * 接口测试版本提交
     * @param target
     * @return
     */
    @Override
    public boolean commitInterfaceVersion(MdInterfaceInfo target) {
        //查询修改表数据
        CaseDesignTools tools = new CaseDesignTools();
        Map<String,Object> dataMap = interfaceInfoModifyService.queryInterfaceInfoToMap(target);
        //更新版本号
        String newVersion = tools.getNextVersion(dataMap.get("version")+"");
        dataMap.put("version",newVersion);
        //删除正式表数据
        interfaceInfoFormalService.deleteInterfaceFormalData(target.getId(),target.getProjectId());
        //插入正式表数据
        interfaceInfoFormalService.insertInterfaceByVersion(dataMap);
        //插入历史表数据
        interfaceInfoHisService.insertInterfaceHisByVersion(dataMap);
        //更新版本号
        MdInterfaceInfo modify = new MdInterfaceInfo();
        modify.setId(target.getId());
        modify.setVersion(newVersion);
        mdInterfaceInfoService.updateByPrimaryKey(modify);
        //提交到组件信息库
        target.setVersion(newVersion);
        Map<String,Object> reComInfoMap = interfaceInfoModifyService.genComInfoByInterface(target);
        if(reComponentInfoService.findByPrimaryKey(target.getId()) == null){
            //插入正式表数据
            componentInfoService.insertFormalComponentInfo(reComInfoMap);
        }else{
            //组件参数删除
            ReComponentParameter paramDelete = new ReComponentParameter();
            paramDelete.setComponentId(target.getId());
            paramDelete.setProjectId(target.getProjectId());
            reComponentParameterService.deleteByExample(paramDelete);
            List<Map<String,Object>> paramList = (List<Map<String,Object>>)reComInfoMap.get("paramList");
            reComponentParameterService.insertBatchForMap(paramList);
            //更新版本
            ReComponentInfo verUp = new ReComponentInfo();
            verUp.setId(target.getId());
            verUp.setVersion(newVersion);
            verUp.setComponentName(dataMap.get("interfaceName")+"");
            verUp.setDataName(dataMap.get("interfaceName")+"");
            verUp.setDescription(dataMap.get("description")+"");
            verUp.setModifyStaff(target.getCreateStaff());
            verUp.setModifyTime(new Date());
            reComponentInfoService.updateByPrimaryKey(verUp);
        }
        return true;
    }

    /**
     * 接口测试版本回滚
     * @param target
     * @return
     */
    @Override
    public boolean rollbackInterfaceVersion(HisInterfaceInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        //查找历史表数据
        Map<String,Object> hisMap = interfaceInfoHisService.queryInterfaceInfoToMap(target);
        ReInterfaceInfo info = reInterfaceInfoService.findByPrimaryKey(hisMap.get("id")+"");
        String newVersion = tools.getNextVersion(info.getVersion());
        hisMap.put("version",newVersion);
        hisMap.put("createStaff",target.getCreateStaff());
        //删除修改表数据
        interfaceInfoModifyService.deleteInterfaceData(hisMap.get("id")+"",target.getProjectId());
        //插入修改表数据
        interfaceInfoModifyService.insertModifyInfoByVersion(hisMap);
        //删除正式表数据
        interfaceInfoFormalService.deleteInterfaceFormalData(hisMap.get("id")+"",target.getProjectId());
        //插入正式表数据
        interfaceInfoFormalService.insertInterfaceByVersion(hisMap);
        //插入历史表数据
        interfaceInfoHisService.insertInterfaceHisByVersion(hisMap);

        //提交到组件信息库_既有历史记录必提交过版本仅更新参数即可
        Map<String,Object> reComInfoMap = interfaceInfoHisService.genComInfoByHisInterface(target);
        //组件参数删除
        ReComponentParameter paramDelete = new ReComponentParameter();
        paramDelete.setComponentId(hisMap.get("id")+"");
        paramDelete.setProjectId(target.getProjectId());
        reComponentParameterService.deleteByExample(paramDelete);
        List<Map<String,Object>> paramList = (List<Map<String,Object>>)reComInfoMap.get("paramList");
        reComponentParameterService.insertBatchForMap(paramList);
        //更新版本
        ReComponentInfo verUp = new ReComponentInfo();
        verUp.setId(hisMap.get("id")+"");
        verUp.setVersion(newVersion);
        verUp.setComponentName(hisMap.get("interfaceName")+"");
        verUp.setDataName(hisMap.get("interfaceName")+"");
        verUp.setDescription(hisMap.get("description")+"");
        verUp.setModifyStaff(target.getCreateStaff());
        verUp.setModifyTime(new Date());
        reComponentInfoService.updateByPrimaryKey(verUp);
        return true;
    }
}
