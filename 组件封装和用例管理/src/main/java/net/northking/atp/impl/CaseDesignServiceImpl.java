package net.northking.atp.impl;

import net.northking.atp.db.persistent.ReCaseDesignMenutree;
import net.northking.atp.service.CaseDesignService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/28 0028.
 */
@Service
public class CaseDesignServiceImpl implements CaseDesignService{
    /**
     * 集合补充所有模糊查询层级
     * @param allList 全量
     * @param list 模糊查询结果
     * @return
     */
    public List<Map<String,Object>> generateListForQuery(
            List<ReCaseDesignMenutree> allList,List<ReCaseDesignMenutree> list,List<Map<String,Object>> toolList){

        Map<String,Map<String,Object>> allMap = new HashMap<String,Map<String,Object>>();
        for (ReCaseDesignMenutree caseDesignMenutree : allList){
            allMap.put(caseDesignMenutree.getMenuId(),caseDesignMenutree.toMap());
        }
        List<Map<String,Object>> oneList = new ArrayList<Map<String, Object>>();
        int num = 0;
        int all = Integer.parseInt(toolList.get(0).get("count")+"");
        for(int i=0;i<all;i++){
            ReCaseDesignMenutree cdm = list.get(num);
            oneList.add(cdm.toMap());
            num++;
        }

        List<Map<String,Object>> result = new ArrayList<Map<String, Object>>();
        System.out.println("第1层:"+oneList);
        for(int i=0;i<toolList.size();i++){
            result = new ArrayList<Map<String, Object>>();
            result = recursiveList(oneList,i+1,num,list,toolList,allMap);
            oneList = new ArrayList<Map<String, Object>>();
            oneList=result;
            System.out.println("第"+(i+1)+"层返回:"+oneList);
        }

        return result;
    }
    //辅助
    private List<Map<String,Object>> recursiveList(List<Map<String,Object>> oneList,int ceng,int num,
        List<ReCaseDesignMenutree> list,List<Map<String,Object>> toolList,Map<String,Map<String,Object>> allMap){
        if(toolList.size()<=ceng){
            return oneList;
        }
        int cengCount = Integer.parseInt(toolList.get(ceng).get("count")+""); //当前层级节点数
        System.out.println("第"+ceng+1+"层节点数："+cengCount);
        int cengLevel = Integer.parseInt(toolList.get(ceng).get("menuLevel")+""); //当前层级

        int dataLevel = Integer.parseInt(oneList.get(0).get("menuLevel")+"");//现有数据层级
        List<Map<String,Object>> stopList = new ArrayList<Map<String,Object>>();
        for(int k = 0;k<dataLevel-cengLevel;k++){
            //循环补齐差距层级
            List<Map<String,Object>> toolCengList = new ArrayList<Map<String,Object>>();
            for(Map<String,Object> map : oneList){
                String menuIdFa = getMenuId(map.get("menuId")+"",1);
                Map<String,Object> fa = allMap.get(menuIdFa);//直属父级节点
                Map<String,Object> newFa = new HashMap<String,Object>();
                newFa.putAll(fa);
                List<Map<String,Object>> childList = new ArrayList<Map<String, Object>>();
                Map<String,Object> chMap = new HashMap<String,Object>();
                chMap.putAll(map);
                childList.add(chMap);
                newFa.put("childList",childList);
                toolCengList.add(newFa);
            }
            //循环父级节点合并相同节点
            if(k==(dataLevel-cengLevel-1)){
                //最后一次，现有层级加入合并
                for(int i=0;i<cengCount;i++){
                    ReCaseDesignMenutree cdm = list.get(num);
                    toolCengList.add(cdm.toMap());
                    num++;
                }

            }
            Map<String,Object> midMap = new HashMap<String,Object>();
            stopList = new ArrayList<Map<String,Object>>();
            for(Map<String,Object> map : toolCengList){
                map.put("actionFlag","1");
                List<Map<String,Object>> childList = new ArrayList<Map<String, Object>>();
                if(map.get("childList")!=null){
                    childList = (List<Map<String,Object>>)map.get("childList");
                }
                if(!midMap.containsKey(map.get("menuId"))){
                    for(Map<String,Object> map1 : toolCengList){
                        if(map1.containsKey("actionFlag")){
                            continue;
                        }
                        if(map.get("menuId").equals(map1.get("menuId"))){
                            List<Map<String,Object>> childListC = (List<Map<String,Object>>)map1.get("childList");
                            if(childListC==null ||childListC.size()<1){
                            }else{
                                childList.add(childListC.get(0));
                            }
                        }
                    }
                    midMap.put(map.get("menuId")+"","1");
                    stopList.add(map);
                }
            }
            oneList = new ArrayList<Map<String, Object>>();
            oneList = stopList;
        }
        return stopList;
    }

    //返回目标节点id
    private String getMenuId(String menuId,int num){
        String[] arr = menuId.split("_");
        String result = "top";
        System.out.println(menuId+"/"+num);
        for(int i=1;i<arr.length-num;i++){
            result += "_"+arr[i];
        }
        return result;
    }
}
