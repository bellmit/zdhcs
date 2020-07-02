package net.northking.atp.impl;

import com.google.gson.Gson;
import com.sun.javafx.font.directwrite.RECT;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceInterfaceInfo;
import net.northking.atp.service.InterfaceInfoModifyService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 接口测试操作服务
 * Created by Administrator on 2019/6/24 0024.
 */
@Service
public class InterfaceInfoModifyServiceImpl implements InterfaceInfoModifyService {
    @Autowired
    private MdInterfaceInfoService mdInterfaceInfoService;
    @Autowired
    private ReInterfaceColumnService reInterfaceColumnService;
    @Autowired
    private MdInterfaceDataService mdInterfaceDataService;

    //编号生成集合
    private static Map<String,String> caseNoMap = new HashMap<String, String>();

    /**
     * 保存接口封装基础信息
     * @param target
     * @return
     */
    @Override
    public int insertInterfaceInfo(MdInterfaceInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        if(target.getId()==null){
            target.setId(tools.getUUID());
            target.setCreateTime(new Date());
            target.setVersion("");
        }
        return mdInterfaceInfoService.insert(target);
    }

    /**
     * 保存所有参数
     * @param dataMap
     * @return
     */
    @Override
    public boolean saveInterfaceData(Map<String, Object> dataMap,MdInterfaceInfo target) {
        List<MdInterfaceData> insert = new ArrayList<MdInterfaceData>();
        CaseDesignTools tools = new CaseDesignTools();
        Map<String ,String> toolsMap = getColumnMap();
        //params
        if(dataMap.get("Params") != null && !"".equals(dataMap.get("Params"))){
            Map<String,Object> paraMap = (Map<String, Object>) dataMap.get("Params");
            if(paraMap.get("list") != null && !"".equals(paraMap.get("list"))){
                //存在params数据
                List<Map<String,Object>> list = (List<Map<String, Object>>) paraMap.get("list");
                String columnNo = toolsMap.get("Params");
                analysisList(list,insert,columnNo,target);
            }
        }

        //Authorization_只保存一个
        if(dataMap.get("Authorization") != null && !"".equals(dataMap.get("Authorization"))){
            Map<String,Object> auMap = (Map<String, Object>) dataMap.get("Authorization");
            if(auMap.get("type") != null && !"".equals(auMap.get("type"))){
                //存在数据
                List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
                String type = auMap.get("type")+"";
                if(!"None".equals(type)){
                    //有认证信息
                    list = (List<Map<String, Object>>) auMap.get("list");
                }
                String columnNo = toolsMap.get("Authorization");
                //增加type数据
                Map<String,Object> typeMap = new HashMap<String,Object>();
                typeMap.put("type",auMap.get("type"));
                list.add(0,typeMap);
                analysisList(list,insert,columnNo,target);
            }
        }


        //Headers
        if(dataMap.get("Headers") != null && !"".equals(dataMap.get("Headers"))){
            Map<String,Object> headMap = (Map<String, Object>) dataMap.get("Headers");
            if(headMap.get("list") != null && !"".equals(headMap.get("list"))){
                //存在Headers数据
                List<Map<String,Object>> list = (List<Map<String, Object>>) headMap.get("list");
                String columnNo = toolsMap.get("Headers");
                analysisList(list,insert,columnNo,target);
            }
        }


        //Body_只保存其中一个
        if(dataMap.get("Body") != null && !"".equals(dataMap.get("Body"))){
            Map<String,Object> bodyMap = (Map<String, Object>) dataMap.get("Body");
            if(bodyMap.get("type") != null && !"".equals(bodyMap.get("type"))){
                List<Map<String,Object>> bodyList = new ArrayList<Map<String, Object>>();
                if(!"NONE".equals(bodyMap.get("type"))){
                    if (bodyMap.get("list") != null && !"".equals(bodyMap.get("list"))){
                        //表格数据
                        bodyList = (List<Map<String, Object>>) bodyMap.get("list");
                    }
                    if(bodyMap.get("content") != null && !"".equals(bodyMap.get("content"))){
                        //文本域数据
                        Map<String,Object> oneMap = new HashMap<String,Object>();
                        oneMap.putAll(bodyMap);
                        oneMap.remove("name");
                        oneMap.remove("type");
                        bodyList.add(oneMap);
                    }
                }
                String columnNo = toolsMap.get("Body");
                //增加type数据
                Map<String,Object> typeMap = new HashMap<String,Object>();
                typeMap.put("type",bodyMap.get("type"));
                bodyList.add(0,typeMap);
                analysisList(bodyList,insert,columnNo,target);
            }
        }


        //Pre-request Script
        if(dataMap.get("Pre-requestScript") != null && !"".equals(dataMap.get("Pre-requestScript"))){
            Map<String,Object> preMap = (Map<String, Object>) dataMap.get("Pre-requestScript");
            if(preMap.get("content") != null && !"".equals(preMap.get("content"))){
                //文本域数据
                String columnNo = toolsMap.get("Pre-requestScript");
                List<Map<String,Object>> bodyList = new ArrayList<Map<String, Object>>();
                Map<String,Object> oneMap = preMap;
                oneMap.remove("name");
                bodyList.add(oneMap);
                analysisList(bodyList,insert,columnNo,target);
            }
        }


        //Tests
        if(dataMap.get("Tests") != null && !"".equals(dataMap.get("Tests"))){
            Map<String,Object> testMap = (Map<String, Object>) dataMap.get("Tests");
            if(testMap.get("content") != null && !"".equals(testMap.get("content"))){
                //文本域数据
                String columnNo = toolsMap.get("Tests");
                List<Map<String,Object>> bodyList = new ArrayList<Map<String, Object>>();
                Map<String,Object> oneMap = testMap;
                oneMap.remove("name");
                bodyList.add(oneMap);
                analysisList(bodyList,insert,columnNo,target);
            }
        }

        //数据整理完毕进行插入
        mdInterfaceDataService.insertByBatch(insert);
        return true;
    }

    /**
     * 修改表接口信息及数据的删除
     * @param id
     * @param projectId
     */
    @Override
    public void deleteInterfaceData(String id,String projectId) {
        //基础信息的删除
        mdInterfaceInfoService.deleteByPrimaryKey(id);
        //接口数据的删除
        MdInterfaceData delete = new MdInterfaceData();
        delete.setProjectId(projectId);
        delete.setInterfaceId(id);
        mdInterfaceDataService.deleteByExample(delete);
    }

    /**
     * 查询接口信息数据
     * @param target
     * @return
     */
    @Override
    public Map<String, Object> queryInterfaceInfoToMap(MdInterfaceInfo target) {
        MdInterfaceInfo info = mdInterfaceInfoService.findByPrimaryKey(target.getId());
        Map<String,Object> map = info.toMap();
        MdInterfaceData query = new MdInterfaceData();
        query.setProjectId(target.getProjectId());
        query.setInterfaceId(info.getId());
        List<MdInterfaceData> mdList = mdInterfaceDataService.query(query);
        List<Map<String,Object>> dataList = new ArrayList<Map<String, Object>>();
        for (MdInterfaceData one : mdList){
            dataList.add(one.toMap());
        }
        map.put("dataList",dataList);
        return map;
    }

    /**
     * 版本控制回滚插入数据
     * @param map
     */
    @Override
    public void insertModifyInfoByVersion(Map<String, Object> map) {
        //插入基础数据
        mdInterfaceInfoService.insertInfoForMap(map);
        //插入参数数据
        List<Map<String,Object>> dataList = (List<Map<String,Object>>)map.get("dataList");
        mdInterfaceDataService.insertBatchForMap(dataList);
    }

    /**
     * 查询指定接口的详细参数数据
     * @param info
     * @return
     */
    @Override
    public Map<String, Object> queryDataList(MdInterfaceInfo info) {
        Map<String,Object> dataMap = new HashMap<String, Object>();
        Map<String ,String> toolsMap = getColumnMap();
        //params
        Map<String,Object> paMap = analysisData(info,"Params",1);
        //Authorization
        Map<String,Object> auMap = analysisData(info,"Authorization",2);
        //Headers
        Map<String,Object> headMap = analysisData(info,"Headers",1);
        //Body
        Map<String,Object> bodyMap = analysisData(info,"Body",2);
        //Pre-request Script
        Map<String,Object> preMap = analysisData(info,"Pre-request Script",3);
        //Tests
        Map<String,Object> testsMap = analysisData(info,"Tests",3);
        dataMap.put("Params",paMap);
        dataMap.put("Authorization",auMap);
        dataMap.put("Headers",headMap);
        dataMap.put("Body",bodyMap);
        dataMap.put("Pre-requestScript",preMap);
        dataMap.put("Tests",testsMap);
        return dataMap;
    }

    @Autowired
    private ReComponentParameterService reComponentParameterService;
    @Autowired
    private ReComponentInfoService reComponentInfoService;
    @Autowired
    private InterfaceInfoModifyServiceImpl interfaceInfoModifyService;
    /**
     * 根据接口参数信息生成对应组件数据
     * -按指定格式导入到组件正式表
     * @param target
     * @return
     */
    @Override
    public Map<String, Object> genComInfoByInterface(MdInterfaceInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        //获取信息
        MdInterfaceInfo info = mdInterfaceInfoService.findByPrimaryKey(target.getId());
        //导入的目标数据
        Map<String ,Object> dataMap = info.toMap();
        dataMap.putAll(queryDataListToCom(info));
        //构造插入数据
        Map<String ,Object> resultMap = new HashMap<String,Object>();
        if(reComponentInfoService.findByPrimaryKey(info.getId()) == null){
            //基础信息
            ReComponentInfo reComInfo = new ReComponentInfo();
            reComInfo.setId(info.getId());
            reComInfo.setComponentName(info.getInterfaceName());
            reComInfo.setDataName(info.getInterfaceName());
            reComInfo.setVersion(target.getVersion());
            reComInfo.setProjectId(info.getProjectId());
            reComInfo.setComponentFlag("3"); //表示为接6口
            reComInfo.setIsValid("1");
            reComInfo.setDescription(info.getDescription());
            reComInfo.setCreateStaff(target.getCreateStaff());
            reComInfo.setCreateTime(new Date());
            reComInfo.setModifyStaff(target.getCreateStaff());
            reComInfo.setModifyTime(new Date());
            reComInfo.setParameterNumber("1"); //暂默认只有输出参数
            resultMap = reComInfo.toMap();
            //封装信息
            Map<String,Object> query = new HashMap<String,Object>();
            query.put("projectId",info.getProjectId());
            List<Map<String,Object>> comQueryList = reInterfaceColumnService.queryComponentByColumnConfig(query);
            List<Map<String,Object>> comList = new ArrayList<Map<String, Object>>();
            Map<String,Integer> toolMap = new HashMap<String,Integer>();
            int k = 0;
            for(Map<String,Object> com : comQueryList){
                String comId = com.get("componentId")+"";
                if(toolMap.containsKey(comId)){
                    List<Map<String,Object>> one = (List<Map<String, Object>>) comList.get(toolMap.get(comId)).get(comId);
                    one.add(com);
                }else{
                    toolMap.put(comId,k);
                    List<Map<String,Object>> one = new ArrayList<Map<String, Object>>();
                    one.add(com);
                    Map<String,Object> oneMap = new HashMap<String,Object>();
                    oneMap.put(comId,one);
                    comList.add(k,oneMap);
                    k++;
                }
            }
            //封装信息以及参数信息生成
            //List<Map<String,Object>> two = (List<Map<String, Object>>) comList.get(toolMap.get("11111111")).get(10000);
            int order= 1;
            List<Map<String,Object>> packList = new ArrayList<Map<String, Object>>();
            List<Map<String,Object>> parList = new ArrayList<Map<String, Object>>();
            List<Map<String,Object>> parToolList = new ArrayList<Map<String, Object>>();
            for(Map<String,Object> com : comList){
                //封装信息
                ReComponentPackage reComPack = new ReComponentPackage();
                reComPack.setId(tools.getUUID());
                reComPack.setComponentId(info.getId());
                reComPack.setProjectId(info.getProjectId());
                reComPack.setBasisComponentId(com.keySet().iterator().next());
                reComPack.setBasisComponentOrder(tools.getOrder(order));
                packList.add(reComPack.toMap());
                //对应参数信息
                ReComponentParameter queryParam = new ReComponentParameter();
                queryParam.setComponentId(reComPack.getBasisComponentId());
                queryParam.setProjectId("SYSTEM");
                List<ReComponentParameter> paramList = reComponentParameterService.query(queryParam);
                List<Map<String,Object>> toolList = new ArrayList<Map<String,Object>>();
                for(ReComponentParameter one : paramList){
                    one.setId(tools.getUUID());
                    one.setInOrOut("0");
                    one.setProjectId(target.getProjectId());
                    one.setComponentId(info.getId());
                    one.setRunComponentId(reComPack.getBasisComponentId());
                    one.setRunComponentOrder(reComPack.getBasisComponentOrder());
                    String name = one.getParameterName();
                    if(dataMap.get(name) != null){
                        String value = replaceBlank(dataMap.get(name)+"");
                        String newValue = "";
                        if(value.contains("\"")){
                            newValue = "'"+value+"'";
                        }else{
                            newValue = "\""+value+"\"";
                        }
                        one.setDefaultValue(newValue);
                    }else if(one.getDefaultValue() != null && !"".equals(one.getDefaultValue())){
                        one.setDefaultValue("\""+one.getDefaultValue()+"\"");
                    }
                    parToolList.add(one.toMap());
                    toolList.add(one.toMap());
                }
                for(Map<String,Object> one : parToolList){
                    one.put("defaultValue",one.get("parameterName")+"_out");
                    parList.add(one);
                }
                for(Map<String,Object> one : toolList){
                    one.put("id",tools.getUUID());
                    one.put("inOrOut","1");
                    one.put("parameterName",one.get("parameterName")+"_out");
                    one.remove("runComponentId");
                    one.remove("runComponentOrder");
                    parList.add(one);
                }
                //默认添加自定义输参数
//                outParam.put("id",tools.getUUID());
//                outParam.put("inOrOut","1");
//                outParam.put("componentId",info.getId());
//                outParam.put("runComponentId",reComPack.getBasisComponentId());
//                outParam.put("runComponentOrder",reComPack.getBasisComponentOrder());
//                outParam.put("projectId",target.getProjectId());

            }
            resultMap.put("complexity",comList.size());
            resultMap.put("packList",packList);
            resultMap.put("paramList",parList);
        }else{
            //仅更改参数值即可
            List<Map<String,Object>> parList = new ArrayList<Map<String, Object>>();
            ReComponentParameter queryParam = new ReComponentParameter();
            queryParam.setComponentId(info.getId());
            queryParam.setProjectId(info.getProjectId());
            List<ReComponentParameter> paramList = reComponentParameterService.query(queryParam);
            List<Map<String,Object>> toolList = new ArrayList<Map<String,Object>>();
            for(ReComponentParameter one : paramList){
                String name = one.getParameterName().split("_")[0];
                if(dataMap.get(name) != null){
                    String value = replaceBlank(dataMap.get(name)+"");
                    String newValue = "";
                    if(value.contains("\"")){
                        newValue = "'"+value+"'";
                    }else{
                        newValue = "\""+value+"\"";
                    }
                    one.setDefaultValue(newValue);
                }
                if("1".equals(one.getInOrOut())){//更改为只更新对外参数
                    //旧数据
                    one.setRunComponentId(null);
                    one.setRunComponentOrder(null);
                    if(!one.getParameterName().contains("_")){
                        one.setParameterName(one.getParameterName()+"_out");
                    }
                }else{
                    one.setDefaultValue(one.getParameterName()+"_out");
                }
                parList.add(one.toMap());
            }
            resultMap.put("paramList",parList);
        }
        return resultMap;
    }


    /**
     * 获取栏位map
     * @return
     */
    @Override
    public Map<String, String> getColumnMap() {
        Map<String,String> map = new HashMap<String,String>();
        if(caseNoMap.size()<1){
            ReInterfaceColumn query = new ReInterfaceColumn();
            List<ReInterfaceColumn> list = reInterfaceColumnService.query(query);
            for(ReInterfaceColumn one : list){
                caseNoMap.put(one.getColumnNo(),one.getColumnModule());
                caseNoMap.put(one.getColumnModule(),one.getColumnNo());
            }
        }
        return caseNoMap;
    }
/******************************************************************************************/
/***********************************私有方法***********************************************/
/******************************************************************************************/
    /**
     * 查询指定模块数据并进行处理
     * @param info
     * @param module
     * @return
     */
    private List<List<MdInterfaceData>> queryMdData(MdInterfaceInfo info,String module){
        List<List<MdInterfaceData>> result = new ArrayList<List<MdInterfaceData>>();
        Map<String ,String> toolsMap = getColumnMap();
        MdInterfaceData params = new MdInterfaceData();
        params.setProjectId(info.getProjectId());
        params.setInterfaceId(info.getId());
        params.setColumnNo(toolsMap.get(module));
        List<MdInterfaceData> paramList = mdInterfaceDataService.queryDataByColumn(params);
        String name = "000";
        if(paramList!=null && paramList.size()>0){
            //增加特殊对象，用于最后一条数据识别
            MdInterfaceData stop = new MdInterfaceData();
            stop.setDataOrder("stop");
            paramList.add(stop);
        }
        List<MdInterfaceData> tooList = new ArrayList<MdInterfaceData>();
        for (MdInterfaceData param :paramList){
            if(name.equals(param.getDataOrder())){
                tooList.add(param);
            }else{
                name = param.getDataOrder();
                result.add(tooList);
                tooList = new ArrayList<MdInterfaceData>();
                tooList.add(param);
            }
        }
        return result;
    }


    /**
     * 将查询的数据解析成返回报文
     * @param info
     * @param module
     * @param check
     * @return
     */
    private Map<String,Object> analysisData(MdInterfaceInfo info,String module,int check){
        List<List<MdInterfaceData>> allList = queryMdData(info,module);
        Map<String,Object> map = new HashMap<String,Object>();
        List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
        for(List<MdInterfaceData> oneList : allList){
            Map<String,Object> oneMap = new HashMap<String,Object>();
            for(MdInterfaceData one : oneList){
                oneMap.put(one.getDataKey(),one.getDataValue());
            }
            result.add(oneMap);
        }
        //map.put("name",module);
        if(result.size()>0){
            if(check == 1){
                map.put("list",result);
            }else if(check == 2){
                map.put("type",result.get(0).get("type"));
                result.remove(0);
                if(result.size()>0 && "RAW".equals(map.get("type"))){
                    map.putAll(result.get(0));
                }else{
                    map.put("list",result);
                }
            }else{
                map.put("content",result.get(0).get("content"));
            }
        }
        return map;
    }

    /**
     * 将返回报文解析成插入数据
     * @param list
     * @param insert
     * @param columnNo
     * @param target
     */
    private void analysisList(List<Map<String,Object>> list, List<MdInterfaceData> insert,String columnNo,MdInterfaceInfo target){
        CaseDesignTools tools = new CaseDesignTools();
        int order = 0;
        for(Map<String,Object> one : list){
            for(String key : one.keySet()){
                MdInterfaceData data = new MdInterfaceData();
                data.setId(tools.getUUID());
                data.setDataKey(key);
                data.setDataValue(one.get(key)+"");
                data.setDataOrder(tools.getOrder(order));
                data.setColumnNo(columnNo);
                data.setProjectId(target.getProjectId());
                data.setUpdateTime(new Date());
                data.setUpdateStaff(target.getCreateStaff());
                data.setInterfaceId(target.getId());
                insert.add(data);
            }
            order++;
        }
    }

    /**
     * 查询指定接口的详细参数数据
     * @param info
     * @return
     */
    private Map<String, Object> queryDataListToCom(MdInterfaceInfo info) {
        List<Map<String,Object>> dataList = new ArrayList<Map<String, Object>>();
        Map<String ,String> toolsMap = getColumnMap();
        Map<String ,Object> dataMap = new HashMap<String,Object>();
        //params
        analysisDataToCom(info,"Params","params",1,dataMap);
        //Authorization
        analysisDataToCom(info,"Authorization","authorization",4,dataMap);
        //Headers
        analysisDataToCom(info,"Headers","headers",1,dataMap);
        //Body
        analysisDataToCom(info,"Body","body",2,dataMap);
        //Pre-request Script
        analysisDataToCom(info,"Pre-requestScript","preRequestScript",3,dataMap);
        //Tests
        analysisDataToCom(info,"Tests","tests",3,dataMap);

        return dataMap;
    }
    /**
     * 将查询的数据解析成组件报文
     * @param info
     * @param module
     * @param check
     * @return
     */
    private Map<String,Object> analysisDataToCom(MdInterfaceInfo info,String module,String com,int check, Map<String,Object> map){
        List<List<MdInterfaceData>> allList = queryMdData(info,module);
        List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
        for(List<MdInterfaceData> oneList : allList){
            Map<String,Object> oneMap = new HashMap<String,Object>();
            for(MdInterfaceData one : oneList){
                oneMap.put(one.getDataKey(),one.getDataValue());
            }
            result.add(oneMap);
        }
        Map<String,Object> comMap = new HashMap<String,Object>();
        if(result.size()>0){
            if(check == 1){
                for(Map<String,Object> one : result){
                    comMap.put(one.get("key")+"",one.get("value"));
                }
                Gson gson = new Gson();
                String json = gson.toJson(comMap);
                map.put(com,json);
            }else if(check == 2){
                comMap.put("type",result.get(0).get("type"));
                if(!"RAW".equals(comMap.get("type"))){
                    result.remove(0);
                    comMap.put("content",result);
                }else{
                    if(result.size()>1){
                        comMap.put("content",result.get(1).get("content"));
                    }
                }
                Gson gson = new Gson();
                String json = gson.toJson(comMap);
                map.put(com,json);
            }else if(check == 3){
                map.put(com,result.get(0).get("content"));
            }else{
                comMap.put("scheme",result.get(0).get("type"));
                if(result.size()>1){
                    comMap.put("schemeParam",result.get(1));
                }
                Gson gson = new Gson();
                String json = gson.toJson(comMap);
                map.put(com,json);
            }
        }
        return map;
    }

    public static String replaceBlank(String str) {
        String dest = StringUtils.replace(str,"\\n","");
        return dest;
    }
}
