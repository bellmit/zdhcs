package net.northking.atp.impl;

import net.northking.atp.controller.ComponentLibraryController;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.element.core.Library;
import net.northking.atp.element.core.keyword.KeywordArgument;
import net.northking.atp.element.core.keyword.KeywordInfo;
import net.northking.atp.element.core.keyword.KeywordReturn;
import net.northking.atp.element.extension.helper.JarLibraryScanner;
import net.northking.atp.entity.InterfaceComponentPackage;
import net.northking.atp.service.ComponentLibraryService;
import net.northking.atp.util.CaseDesignTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 组件库扫描保存组件库，组件，参数等信息以及版本信息
 * Created by Administrator on 2019/4/26 0026.
 */
@Service
public class ComponentLibraryServiceImpl implements ComponentLibraryService{

    @Autowired
    ReComponentLibraryService reComponentLibraryService; //组件库表
    @Autowired
    ReComponentInfoService reComponentInfoService; //组件信息表
    @Autowired
    ReComponentParameterService reComponentParameterService;//组件参数表

    @Autowired
    HisComponentLibraryService hisComponentLibraryService; //组件库版本
    @Autowired
    HisComponentInfoService hisComponentInfoService; //组件信息版本
    @Autowired
    HisComponentParameterService hisComponentParameterService; //组件参数版本
    //日志
    private static final Logger logger = LoggerFactory.getLogger(ComponentLibraryServiceImpl.class);
    /**
     * 判断组件库是否存在并校验版本
     * @param libraryInfo
     * @param target
     * @return 0：版本更新
     * @return 1：版本过低
     * @return 2：版本相同
     * @return 3：判定为新组件库
     */
    @Override
    public int versionCheck(Library libraryInfo ,
                            ReComponentLibrary target, List<ReComponentLibrary> checkList) {
        //判断库是否已存在，若存在则判断版本信息
        int result = 2; //版本相同——默认
        if(checkList !=null && checkList.size()>0){
            //已存在有效组件库，根据版本号判断是否需要更新
            ReComponentLibrary reComponentLibrary = checkList.get(0);
            String versionNew = libraryInfo.getVersion();
            String versionOld = reComponentLibrary.getVersion();
            //全量比较
            String[] verNew = versionNew.split("\\.");
            String[] verOld = versionOld.split("\\.");
            String[] check = verNew;
            if(verNew.length>verOld.length){
                check=verOld;
            }
            int i = 0;
            for(String ver : check){
                //逐个校验
                int n = Integer.parseInt(verNew[i]);
                int o = Integer.parseInt(verOld[i]);
                i++;
                if(n==o){
                    continue;
                }else if(n>o){
                    result=0; //新版更新
                    break;
                }else{
                    result=1; //旧版不跟新
                    break;
                }
            }
            if(result == 2 && verNew.length>verOld.length){
                result=0;//新版更新
            }
            if(result == 2 && verNew.length<verOld.length){
                result=1;//旧版不跟新
            }
            //数字比较法，只只用于一个点
//            BigDecimal verNew = new BigDecimal(versionNew);
//            BigDecimal verOld = new BigDecimal(versionOld);
//            if (verNew.compareTo(verOld) == 0){
//                return 2; //版本相同
//            }else if(verNew.compareTo(verOld) >0){
//                return 0; //新版更新
//            }else{
//                return 1; //旧版不跟新
//            }
        }else{
            result = 3;//库表不存在，为新组件库
        }
        return result;
    }

    /**
     *  根据扫面结果生成注册表和参数表的插入批量list
     * @param comList //注册list
     * @param paramMap //参数map
     * @param target //请求报文-组件对象
     * @param libraryInfo //扫描结果-库信息
     * @param keyList //扫面结果-关键字信息
     */
    public void  getListForComponentInsert(List<ReComponentInfo> comList, Map<String,List<ReComponentParameter>> paramMap,
            ReComponentLibrary target, Library libraryInfo, List<KeywordInfo> keyList,
            String version,String libraryId) {
        CaseDesignTools tools = new CaseDesignTools();
        for (KeywordInfo keywordinfo : keyList) {
            //注册表
            ReComponentInfo reComponentInfo = new ReComponentInfo();
            String componentId = UUID.randomUUID().toString().replace("-", "");
            reComponentInfo.setId(componentId);
            String componentNo = "TSC-" + tools.generateBusinessNo();
            reComponentInfo.setComponentNo(componentNo);
            reComponentInfo.setComponentName(keywordinfo.getKeywordName());
            reComponentInfo.setDescription(keywordinfo.getKeywordDocumentation());
            reComponentInfo.setDataName(keywordinfo.getKeywordAlias());
            reComponentInfo.setProjectId(target.getProjectId());
            reComponentInfo.setComponentFlag("0");//基础组件
            reComponentInfo.setLibraryId(libraryId);
            reComponentInfo.setIsValid("1");//默认有效
            reComponentInfo.setVersion(version);
            reComponentInfo.setCreateTime(new Date());
            reComponentInfo.setComplexity("1"); //复杂度默认1
            if(keywordinfo.isDeprecated()){
                reComponentInfo.setIsDeprecated("0");
            }else{
                reComponentInfo.setIsDeprecated("1");
            }
            //参数表
            List<ReComponentParameter> paramList = new ArrayList<ReComponentParameter>();
            List<KeywordArgument> kaList = keywordinfo.getKeywordArguments();
            int listNumber = kaList.size();
            int order = 1;
            for (KeywordArgument ka : kaList) {
                ReComponentParameter param = new ReComponentParameter();
                param.setId(UUID.randomUUID().toString().replace("-", ""));
                param.setComponentId(componentId);
                if(ka.getType() != null){
                    param.setParameterType(ka.getType().name());
                }
                param.setParameterFlag("0"); //输入参数
                param.setInOrOut("0"); //基础内部参数
                param.setProjectId(target.getProjectId());
                param.setDefaultValue(ka.getDefaultValue());
                param.setParameterComment(ka.getComment());
                param.setRequired(String.valueOf(ka.isRequired()));
                param.setParameterName(ka.getName());
                param.setParameterOrder(tools.getOrder(order));
                order++;
                paramList.add(param);
            }
            //添加每个组件对应的输出
            KeywordReturn keywordReturn = keywordinfo.getKeywordReturn();
            if(keywordReturn != null){
                listNumber++;
                ReComponentParameter param = new ReComponentParameter();
                param.setId(UUID.randomUUID().toString().replace("-", ""));
                if(keywordReturn.getType() != null){
                    param.setParameterType(keywordReturn.getType().name());
                }
                param.setComponentId(componentId);
                param.setParameterFlag("1"); //输入参数
                param.setInOrOut("0"); //基础内部参数
                param.setProjectId(target.getProjectId());
                param.setDefaultValue(keywordReturn.getDefaultValue());
                param.setParameterComment(keywordReturn.getComment());
                param.setRequired(String.valueOf(keywordReturn.isRequired()));
                param.setParameterName(keywordReturn.getName());
                param.setParameterOrder(tools.getOrder(order));
                paramList.add(param);
            }
            reComponentInfo.setParameterNumber(listNumber+"");
            comList.add(reComponentInfo);
            paramMap.put(componentId,paramList);
            //延迟1ms,避免组件编号重复
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 组件库版本信息插入
     * @param reComponentLibrary
     * @return
     */
    @Override
    public boolean versionInfoSave(ReComponentLibrary reComponentLibrary,
                List<ReComponentInfo> comList,List<ReComponentParameter> paramList) {
        //组件库版本信息插入
        String hisId = UUID.randomUUID().toString().replace("-", "");
        Map<String,Object> hisComLibrary = reComponentLibrary.toMap();
        hisComLibrary.put("reId",reComponentLibrary.getId());
        hisComLibrary.put("id",hisId);
        hisComponentLibraryService.insertInfoForMap(hisComLibrary);

        List<Map<String,Object>> hisInfoList = new ArrayList<Map<String,Object>>();
        for(ReComponentInfo reComponentInfo : comList){
            Map<String,Object> hisComponentInfo = reComponentInfo.toMap();
            hisComponentInfo.put("reId",reComponentInfo.getId());
            hisComponentInfo.put("id",UUID.randomUUID().toString().replace("-", ""));
            hisComponentInfo.put("hisId",hisId);
            hisInfoList.add(hisComponentInfo);
        }
        hisComponentInfoService.insertBatchForMap(hisInfoList);

        List<Map<String,Object>> hisParamList = new ArrayList<Map<String,Object>>();
        for(ReComponentParameter reComponentParameter : paramList){
            Map<String,Object> hisComponentParameter = reComponentParameter.toMap();
            hisComponentParameter.put("reId",reComponentParameter.getId());
            hisComponentParameter.put("id",UUID.randomUUID().toString().replace("-", ""));
            hisComponentParameter.put("hisId",hisId);
            hisParamList.add(hisComponentParameter);
        }
        hisComponentParameterService.insertBatchForMap(hisParamList);
        return true;
    }

    /**
     * 更新组件信息
     * @param comList
     * @param paramMap
     * @param checkResult
     * @param id
     * @param target
     * @param toolMap
     * @return
     */
    @Override
    public boolean insertCompInfoAndHis(List<ReComponentInfo> comList,Map<String,List<ReComponentParameter>> paramMap,
            int checkResult,String id,ReComponentLibrary target,Map<String,Object> toolMap) {
        CaseDesignTools tools = new CaseDesignTools();
        if(checkResult == 3){
            //新组件库，全部插入
            reComponentInfoService.insertByBatch(comList);
        }
        if (checkResult == 0){
            //组件更新-部分插入
            ReComponentInfo query = new ReComponentInfo();
            query.setLibraryId(id);
            query.setProjectId(target.getProjectId());
            List<ReComponentInfo> comInfoList = reComponentInfoService.query(query);
            //TODO-是否需要更换循环方式，循环套循环次数会根据list大小成倍增加。
            //改变方式-循环被查找list，根据组件名+参数个数为主键，组件信息为值的方式进行直接获取判定
            for(ReComponentInfo scanCom : comList){
                for(ReComponentInfo tableCom : comInfoList){
                    //分辨扫描信息中，已存在的和新增的（暂不会存在删除）
                    if(scanCom.getComponentName().equals(tableCom.getComponentName()) &&
                            scanCom.getParameterNumber().equals(tableCom.getParameterNumber())){
                        //已存在进行更新
                        String comId = scanCom.getId();
                        scanCom.setId(tableCom.getId());
                        scanCom.setComponentNo(tableCom.getComponentNo());
                        scanCom.setCreateTime(tableCom.getCreateTime());
                        scanCom.setModifyTime(new Date());
                        scanCom.setVersion(tableCom.getVersion());
                        toolMap.put(tableCom.getId()+"","1");
                        //更新参数map
                        List<ReComponentParameter> paList = paramMap.get(comId);
                        for(ReComponentParameter one : paList){
                            one.setComponentId(comId);
                        }
                        paramMap.put(scanCom.getId(),paList);
                        paramMap.remove(comId);
                    }
                }
            }
            reComponentInfoService.deleteByExample(query);
            reComponentInfoService.insertByBatch(comList);
        }

        return true;
    }

    /**
     * 更新组件参数信息
     * @param paramList
     * @param checkResult
     * @param id
     * @param target
     * @param toolMap
     * @return
     */
    @Override
    public boolean insertCompParamInfoAndHis(List<ReComponentParameter> paramList,
             int checkResult,String id,ReComponentLibrary target,Map<String,Object> toolMap) {
        if(checkResult == 3){
            //新组件库，全部插入
            reComponentParameterService.insertByBatch(paramList);
        }if (checkResult == 0){
            //组件更新-部分插入
            List<String> idList = new ArrayList<String>();
            Map<String,List<ReComponentParameter>> allParamMap = queryParamList(toolMap,target.getProjectId());
            for(ReComponentParameter scanParam : paramList){
                String compId = scanParam.getComponentId();
                if(allParamMap.containsKey(compId)){
                    //组件存在，更新
                    List<ReComponentParameter> comParList = allParamMap.get(compId);
                    for(ReComponentParameter checkParam : comParList){
                        if(checkParam.getParameterName().equals(scanParam.getParameterName())
                                && checkParam.getParameterFlag().equals(scanParam.getParameterFlag())){
                            scanParam.setId(checkParam.getId());
                            idList.add(scanParam.getId());
//                            ids[i] = scanParam.getId();
//                            i++;
                        }
                    }
                }
            }
            if(idList.size()>0){
                String[] ids = new String[idList.size()];
                int i = 0;
                for(String str: idList){
                    ids[i] = str;
                    i++;
                }
                reComponentParameterService.deleteByPrimaryKeys(ids);
            }
            reComponentParameterService.insertByBatch(paramList);
        }
        return true;
    }

    /**
     * 扫描组件库
     * @param target
     * @return
     */
    @Transactional
    @Override
    public String scanLibraryAndSave(ReComponentLibrary target) {
        target.setProjectId("SYSTEM");
        //文件扫描
        CaseDesignTools tools = new CaseDesignTools();
        Library libraryInfo = null;
        List<KeywordInfo> keyList = new ArrayList<KeywordInfo>();
        try {
            libraryInfo = JarLibraryScanner.scan(target.getFilePath());
            keyList = libraryInfo.getAllKeywords();
        } catch (Exception e) {
            logger.info("扫描失败:"+e);
            return "上传组件库扫描失败，请确认上传文件";
        }
        if(libraryInfo == null){
            return "上传组件库扫描失败，请确认上传文件";
        }

        //存储库信息
        String version = libraryInfo.getVersion();
        String id = "";
        ReComponentLibrary comLibrary = new ReComponentLibrary();
        comLibrary.setFileName(target.getFileName());
        comLibrary.setUploadTime(new Date());
        comLibrary.setLibraryName(libraryInfo.getName());
        comLibrary.setStatus("1"); //启用
        comLibrary.setIsValid("1"); //逻辑存在
        comLibrary.setProjectId(target.getProjectId());
        comLibrary.setRemark(target.getRemark());
        comLibrary.setDataName(target.getDataName());
        comLibrary.setVersion(version);
        comLibrary.setChangeLog(libraryInfo.getChangeLog());
        comLibrary.setIntro(libraryInfo.getIntro());
        comLibrary.setUploadStaff(target.getUploadStaff());

        String libraryName = libraryInfo.getName();
        ReComponentLibrary checkLib = new ReComponentLibrary();
        checkLib.setLibraryName(libraryName);
        checkLib.setProjectId(target.getProjectId());
        checkLib.setIsValid("1"); //逻辑存在
        List<ReComponentLibrary> checkList = reComponentLibraryService.query(checkLib);
        int checkResult = versionCheck(libraryInfo,target,checkList);
        if(checkResult == 1 || checkResult == 2){
            return "上传版本过低或与当前版本相同，请确认组件库文件";
        }else if(checkResult == 0){
            //更新
            ReComponentLibrary old = checkList.get(0);
            id = old.getId();
            comLibrary.setId(id);
            String libraryNo = old.getLibraryNo();
            comLibrary.setLibraryNo(libraryNo);
            reComponentLibraryService.updateByPrimaryKey(comLibrary);
            logger.info("库信息更新");
        }else{
            //新插入
            id = UUID.randomUUID().toString().replace("-", "");
            comLibrary.setId(id);
            String libraryNo = "LIB"+"-"+tools.generateBusinessNo();
            comLibrary.setLibraryNo(libraryNo);
            reComponentLibraryService.insert(comLibrary);
            logger.info("库信息保存");
        }
        target.setId(comLibrary.getId());
        //循环关键字获取信息
        List<ReComponentInfo> comList = new ArrayList<ReComponentInfo>();
        List<ReComponentParameter> paramList = new ArrayList<ReComponentParameter>();
        Map<String,List<ReComponentParameter>> paramMap = new HashMap<String,List<ReComponentParameter>>();
        getListForComponentInsert(comList,paramMap,target,libraryInfo,keyList,version,id);

        //插入两张表的数据
        Map<String,Object> map = new HashMap<String,Object>();
        insertCompInfoAndHis(comList,paramMap,checkResult,id,target,map);
        for(String key : paramMap.keySet()){
            List<ReComponentParameter> paList = paramMap.get(key);
            for(ReComponentParameter one : paList){
                one.setComponentId(key);
                paramList.add(one);
            }
        }
        insertCompParamInfoAndHis(paramList,checkResult,id,target,map);
        versionInfoSave(comLibrary,comList,paramList);
        return null;
    }

    /**
     * 查询组件对应的参数
     * @param toolMap
     * @param projectId
     * @return
     */
    private Map<String,List<ReComponentParameter>> queryParamList(Map<String,Object> toolMap,String projectId){
        String queryCom = "";
        for(String key : toolMap.keySet()){
            if("".equals(queryCom)){
                queryCom = queryCom+"'"+key+"'";
            }else{
                queryCom = queryCom+",'"+key+"'";
            }
        }
        Map<String,Object> query = new HashMap<String,Object>();
        query.put("queryCom",queryCom);
        query.put("projectId",projectId);
        List<ReComponentParameter> allParList = new ArrayList<>();
        if(!"".equals(queryCom)){
            allParList = reComponentParameterService.queryParamForComList(query);
        }
        Map<String,List<ReComponentParameter>> result = new HashMap<String,List<ReComponentParameter>>();
        for(ReComponentParameter param : allParList){
            if(result.containsKey(param.getComponentId())){
                List<ReComponentParameter> list = result.get(param.getComponentId());
                list.add(param);
            }else{
                List<ReComponentParameter> list = new ArrayList<ReComponentParameter>();
                list.add(param);
                result.put(param.getComponentId()+"",list);
            }
        }

        return result;
    }
}
