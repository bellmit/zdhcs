package net.northking.atp.util;

import com.google.gson.Gson;
import net.northking.atp.rule.core.RuleLibraryManager;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 工具类
 * Created by Administrator on 2019/7/16 0016.
 */
public class FunctionTools {
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
//
//    public static void main(String[] args) {
//        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
//        Map<String,Object> map = new HashMap<String,Object>();
//        map.put("parameterFlag","1");
//        map.put("parameterName","data");
//        map.put("parameterType","STRING");
//        map.put("parameterComment","规则数据");
//        map.put("defaultValue","");
//        list.add(map);
//
//        Map<String,Object> map1 = new HashMap<String,Object>();
//        map1.put("parameterFlag","0");
//        map1.put("parameterName","regex");
//        map1.put("parameterType","STRING");
//        map1.put("parameterComment","正则表达式");
//        map1.put("defaultValue","[1-3][4-6][7-9]");
//        list.add(map1);
//
//        Gson gson = new Gson();
//        String json = gson.toJson(list);
//        System.out.println(json);
//    }
}
