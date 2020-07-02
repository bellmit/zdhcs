package net.northking.atp.impl;

import com.google.gson.Gson;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.service.InterfaceInfoHisService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Administrator on 2019/6/26 0026.
 */
@Service
public class InterfaceInfoHisServiceImpl implements InterfaceInfoHisService{
    @Autowired
    private HisInterfaceInfoService hisInterfaceInfoService;
    @Autowired
    private HisInterfaceDataService hisInterfaceDataService;
    @Autowired
    private ReInterfaceColumnService reInterfaceColumnService;

    @Autowired
    private ReComponentParameterService reComponentParameterService;
    @Autowired
    private ReComponentInfoService reComponentInfoService;
    @Autowired
    private InterfaceInfoModifyServiceImpl interfaceInfoModifyService;

    /**
     * 查询某版本历史数据用于数据回滚
     * @param target
     * @return
     */
    @Override
    public Map<String, Object> queryInterfaceInfoToMap(HisInterfaceInfo target) {
        HisInterfaceInfo info = hisInterfaceInfoService.findByPrimaryKey(target.getId());
        HisInterfaceData query = new HisInterfaceData();
        query.setProjectId(info.getProjectId());
        query.setHisId(info.getId());
        List<HisInterfaceData> mdList = hisInterfaceDataService.query(query);
        List<Map<String,Object>> dataList = new ArrayList<Map<String, Object>>();
        for (HisInterfaceData one : mdList){
            one.setId(one.getReId());
            dataList.add(one.toMap());
        }
        info.setId(info.getReId());
        Map<String,Object> map = info.toMap();
        map.put("dataList",dataList);
        return map;
    }

    /**
     * 版本控制时插入数据
     * @param map
     */
    @Override
    public void insertInterfaceHisByVersion(Map<String, Object> map) {
        CaseDesignTools tools = new CaseDesignTools();
        String hisId = tools.getUUID(); //历史Id
        //参数信息
        Map<String,Object> insertMap = new HashMap<String,Object>();
        insertMap.putAll(map);
        insertMap.put("reId",map.get("id"));
        insertMap.put("id",hisId);
        insertMap.put("hisCommitStaff", map.get("createStaff"));
        insertMap.put("hisCommitTime",new Date());
        insertMap.put("hisCommitLog","版本更新日志");
        hisInterfaceInfoService.insertInfoForMap(insertMap);
        //插入参数数据
        List<Map<String,Object>> dataList = (List<Map<String,Object>>)insertMap.get("dataList");
        if(dataList != null){
            for(Map<String,Object> dataMap : dataList){
                dataMap.put("reId",dataMap.get("id"));
                dataMap.put("hisId",hisId);
                dataMap.put("id",tools.getUUID());
            }
        }
        hisInterfaceDataService.insertBatchForMap(dataList);
    }



    /**
     * 根据接口参数信息生成对应组件数据
     * -按指定格式导入到组件正式表
     * @param target
     * @return
     */
    @Override
    public Map<String, Object> genComInfoByHisInterface(HisInterfaceInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        //获取信息
        HisInterfaceInfo info = hisInterfaceInfoService.findByPrimaryKey(target.getId());
        //目标数据
        Map<String ,Object> dataMap = info.toMap();
        dataMap.putAll(queryDataListToCom(info));
        //构造插入数据
        Map<String ,Object> resultMap = new HashMap<String,Object>();
        //仅更改参数值即可
        List<Map<String,Object>> parList = new ArrayList<Map<String, Object>>();
        ReComponentParameter queryParam = new ReComponentParameter();
        queryParam.setComponentId(info.getReId());
        queryParam.setProjectId(info.getProjectId());
        List<ReComponentParameter> paramList = reComponentParameterService.query(queryParam);
        for(ReComponentParameter one : paramList){
            String name = one.getParameterName();
            if(dataMap.get(name) != null){
                one.setDefaultValue(dataMap.get(name)+"");
            }
            parList.add(one.toMap());
        }
        dataMap.put("id",dataMap.get("reId"));
        dataMap.put("paramList",parList);
        return dataMap;
    }


    /**
     * 查询历史版本详情
     * @param target
     * @return
     */
    @Override
    public Map<String, Object> queryHisDataList(HisInterfaceInfo target) {
        Map<String,Object> dataMap = new HashMap<String, Object>();
        Map<String ,String> toolsMap = interfaceInfoModifyService.getColumnMap();
        //params
        Map<String,Object> paMap = analysisHisData(target,"Params",1);
        //Authorization
        Map<String,Object> auMap = analysisHisData(target,"Authorization",2);
        //Headers
        Map<String,Object> headMap = analysisHisData(target,"Headers",1);
        //Body
        Map<String,Object> bodyMap = analysisHisData(target,"Body",2);
        //Pre-request Script
        Map<String,Object> preMap = analysisHisData(target,"Pre-request Script",3);
        //Tests
        Map<String,Object> testsMap = analysisHisData(target,"Tests",3);
        dataMap.put("Params",paMap);
        dataMap.put("Authorization",auMap);
        dataMap.put("Headers",headMap);
        dataMap.put("Body",bodyMap);
        dataMap.put("Pre-requestScript",preMap);
        dataMap.put("Tests",testsMap);
        return dataMap;
    }

//*********************************************************************************************/
//************************            私有方法区          **************************************/
//*********************************************************************************************/
    /**
     * 将查询的数据解析成返回报文
     * @param info
     * @param module
     * @param check
     * @return
     */
    private Map<String,Object> analysisHisData(HisInterfaceInfo info,String module,int check){
        List<List<HisInterfaceData>> allList = queryHisData(info,module);
        Map<String,Object> map = new HashMap<String,Object>();
        List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
        for(List<HisInterfaceData> oneList : allList){
            Map<String,Object> oneMap = new HashMap<String,Object>();
            for(HisInterfaceData one : oneList){
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
     * 查询指定模块数据并进行处理
     * @param info
     * @param module
     * @return
     */
    private List<List<HisInterfaceData>> queryHisData(HisInterfaceInfo info,String module){
        List<List<HisInterfaceData>> result = new ArrayList<List<HisInterfaceData>>();
        Map<String ,String> toolsMap = interfaceInfoModifyService.getColumnMap();
        HisInterfaceData params = new HisInterfaceData();
        params.setProjectId(info.getProjectId());
        params.setInterfaceId(info.getReId());
        params.setColumnNo(toolsMap.get(module));
        params.setHisId(info.getId());
        List<HisInterfaceData> paramList = hisInterfaceDataService.queryDataByColumn(params);
        String name = "000";
        if(paramList!=null && paramList.size()>0){
            //增加特殊对象，用于最后一条数据识别
            HisInterfaceData stop = new HisInterfaceData();
            stop.setDataOrder("stop");
            paramList.add(stop);
        }
        List<HisInterfaceData> tooList = new ArrayList<HisInterfaceData>();
        for (HisInterfaceData param : paramList){
            if(name.equals(param.getDataOrder())){
                tooList.add(param);
            }else{
                name = param.getDataOrder();
                result.add(tooList);
                tooList = new ArrayList<HisInterfaceData>();
                tooList.add(param);
            }
        }
        return result;
    }


    /**
     * 查询指定接口的详细参数数据
     * @param info
     * @return
     */
    private Map<String, Object> queryDataListToCom(HisInterfaceInfo info) {
        List<Map<String,Object>> dataList = new ArrayList<Map<String, Object>>();
        Map<String ,String> toolsMap = interfaceInfoModifyService.getColumnMap();
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
        analysisDataToCom(info,"Pre-request Script","preRequestScript",3,dataMap);
        //Tests
        analysisDataToCom(info,"Tests","tests",3,dataMap);

        return dataMap;
    }

    /**
     * 将查询的数据解析成返回报文
     * @param info
     * @param module
     * @param check
     * @return
     */
    private Map<String,Object> analysisDataToCom(HisInterfaceInfo info,String module,String com,int check, Map<String,Object> map){
        List<List<HisInterfaceData>> allList = queryMdData(info,module);
        List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
        for(List<HisInterfaceData> oneList : allList){
            Map<String,Object> oneMap = new HashMap<String,Object>();
            for(HisInterfaceData one : oneList){
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
                result.remove(0);
                comMap.put("content",result);
                Gson gson = new Gson();
                String json = gson.toJson(comMap);
                map.put(com,json);
            }else if(check == 3){
                map.put(com,result.get(0).get("content"));
            }else{
                comMap.put("scheme",result.get(0).get("type"));
                if(result.size()>1){
                    comMap.put("schemeParam",result.get(1));
                }else{
                    comMap.put("schemeParam",new HashMap<String,Object>());
                }
                Gson gson = new Gson();
                String json = gson.toJson(comMap);
                map.put(com,json);
            }
        }
        return map;
    }
    /**
     * 查询指定模块数据并进行处理
     * @param info
     * @param module
     * @return
     */
    private List<List<HisInterfaceData>> queryMdData(HisInterfaceInfo info,String module){
        List<List<HisInterfaceData>> result = new ArrayList<List<HisInterfaceData>>();
        Map<String ,String> toolsMap = interfaceInfoModifyService.getColumnMap();
        HisInterfaceData params = new HisInterfaceData();
        params.setHisId(info.getId());
        params.setColumnNo(toolsMap.get(module));
        List<HisInterfaceData> paramList = hisInterfaceDataService.queryDataByColumn(params);
        String name = "000";
        //增加特殊对象，用于最后一条数据识别
        HisInterfaceData stop = new HisInterfaceData();
        stop.setDataOrder("stop");
        paramList.add(stop);
        List<HisInterfaceData> tooList = new ArrayList<HisInterfaceData>();
        for (HisInterfaceData param :paramList){
            if(name.equals(param.getDataOrder())){
                tooList.add(param);
            }else{
                name = param.getDataOrder();
                result.add(tooList);
                tooList = new ArrayList<HisInterfaceData>();
                tooList.add(param);
            }
        }
        return result;
    }
}
