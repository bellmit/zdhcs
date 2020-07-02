package net.northking.atp.util;

import net.northking.atp.db.persistent.*;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.element.core.Library;
import net.northking.atp.element.core.keyword.KeywordArgument;
import net.northking.atp.element.core.keyword.KeywordInfo;
import net.northking.atp.element.core.keyword.KeywordReturn;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 工具类
 * Created by Administrator on 2019/3/6 0006.
 */
public class CaseDesignTools {
    /**
     * 根据目标查找对应的层级树
     * @param childList
     * @param menuId
     * @return
     */
    public Map<String,Object> getMenuList(List<Map<String,Object>> childList,String menuId){
        Map<String,Object> menuMap = new HashMap<String, Object>();

        for(Map<String,Object> child : childList){
            if(menuId.equals(child.get("menuId"))){
                //找到当前层级
                menuMap.putAll(child);
                break;
            }else{
                List<Map<String,Object>> childListT = (List<Map<String,Object>>) child.get("childList");
                if(childListT != null){
                    getMenuList(childListT,menuId);
                }
            }
        }
        return menuMap;
    }

    /**
     * list下所有menuId集合
     * @param map
     * @param menuList
     */
    public void getAllMenuId(Map<String,Object> map,List<String> menuList){
        List<Map<String,Object>> childList = (List<Map<String,Object>>) map.get("childList");
        menuList.add(map.get("menuId")+"");
        if(childList != null){
            for(Map<String,Object> mapOne : childList){
                getAllMenuId(mapOne,menuList);
            }
        }
    }

    /**
     * 为下一级菜单生成唯一编码
     * @return 1970年第一天早8点到当前系统时间的毫秒数
     */
    public String generateMenuId(){
        Long now = new Date().getTime();
        String id = String.valueOf(now);
        return id;
    }

    /**
     * 讲指定键值加上%，适应模糊查询
     * @param caseInfo
     */
    public void setLikeKey(ReCaseDesignInfo caseInfo){
        if(caseInfo.getCaseNo()!=null){
            String caseNo= "%"+caseInfo.getCaseNo()+"%";
            caseInfo.setCaseNo(caseNo);
        }
        if(caseInfo.getCaseName()!=null){
            String caseName= "%"+caseInfo.getCaseName()+"%";
            caseInfo.setCaseName(caseName);
        }
    }


    /**
     * 生成编号
     * @return 日期+当前0点到系统时间的毫秒数
     */
    public String generateBusinessNo() {
        try{
            Long now = new Date().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(new Date()) + " 00:00:00";
            Date start = sdf.parse(date);
            Long result = now-start.getTime();
            SimpleDateFormat sdfResult = new SimpleDateFormat("yyyyMMdd");
            String re = sdfResult.format(now)+""+result;
            return re;
        }catch (Exception e){
            return null;
        }
    }
    /**
     * 生成六位数唯一编号编号
     * @return 000001-999999
     */
    public String generateUniqueBusinessNo(Map<String,String> redis,String maxNo) {
        String result = "";
        if(maxNo == null || "".equals(maxNo)){
           //初始编号
            result =  "000001";
        }else{
            result = maxNo.split("-")[1];
            redis.put(result,"");
        }
        while ("".equals(redis.putIfAbsent(result,""))){
            int newNum = Integer.parseInt(result)+1;
            result = String.format("%06d",newNum);
//            String add = "000000000"+newNum;
//            result = add.substring(add.length()-6,add.length());
        }
        return result;
    }

    /**
     * 获取当前时间
     * @return yyyy-MM-dd HH:mm:ss
     */
    public String getNowTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * 获取顺序
     * @return 001-010-100
     */
    public String getOrder(int order){
        String newOrder = "000"+order;
        return newOrder.substring(newOrder.length()-3,newOrder.length());
    }

    /**
     * 获取新的版本号
     * @param version
     * @return
     * 输入空字符串返回初始版本1.0.0
     */
    public String getNextVersion(String version){
        if("".equals(version)){
            return "1.0.0";
        }
        String[] arr = version.split("\\.");
        String after = Integer.valueOf(arr[2])+1+"";
        String begin = arr[1];
        if("10".equals(after)){
            after = "0";
            begin = Integer.valueOf(begin)+1+"";
        }
        String result = arr[0]+"."+begin +"."+ after;
        return result;
    }

    /**
     * 返回唯一主键uuid
     * @return
     */
    public String getUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 分析list，聚拢相同目标字段的数据
     * @return map(key,list(map))
     */
    public Map<String,Object> getMapForAlikeKey(List<Map<String,Object>> list, String key){
        Map<String,Object> result = new HashMap<String,Object>();
        for(Map<String,Object> map : list){
            if(result.containsKey(map.get(key)+"")){
                List<Map<String,Object>> toolList = (List<Map<String, Object>>) result.get(map.get(key)+"");
                toolList.add(map);
            }else{
                List<Map<String,Object>> toolList = new ArrayList<Map<String, Object>>();
                toolList.add(map);
                result.put(map.get(key)+"",toolList);
            }
        }
        return result;
    }
}

