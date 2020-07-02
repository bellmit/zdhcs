package net.northking.atp.impl;

import net.northking.atp.db.persistent.ReRulePluginInfo;
import net.northking.atp.db.persistent.ReRulePluginLibrary;
import net.northking.atp.db.persistent.ReRulePluginParameter;
import net.northking.atp.db.service.ReRulePluginInfoService;
import net.northking.atp.db.service.ReRulePluginLibraryService;
import net.northking.atp.db.service.ReRulePluginParameterService;
import net.northking.atp.element.core.Library;
import net.northking.atp.element.core.keyword.KeywordArgument;
import net.northking.atp.element.core.keyword.KeywordInfo;
import net.northking.atp.element.core.keyword.KeywordReturn;
import net.northking.atp.rule.core.RuleLibrary;
import net.northking.atp.rule.core.rule.RuleArgument;
import net.northking.atp.rule.core.rule.RuleInfo;
import net.northking.atp.rule.core.rule.RuleReturn;
import net.northking.atp.service.RulePluginLibraryService;
import net.northking.atp.util.FunctionTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2019/7/16 0016.
 */
@Service
public class RulePluginLibraryServiceImpl implements RulePluginLibraryService{

    @Autowired
    private ReRulePluginLibraryService reRulePluginLibraryService;
    @Autowired
    private ReRulePluginInfoService reRulePluginInfoService;
    @Autowired
    private ReRulePluginParameterService reRulePluginParameterService;

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
    public int versionCheck(RuleLibrary libraryInfo , ReRulePluginLibrary target, List<ReRulePluginLibrary> checkList) {
        //判断库是否已存在，若存在则判断版本信息
        if (checkList != null && checkList.size() > 0) {
            //已存在有效组件库，根据版本号判断是否需要更新
            ReRulePluginLibrary ruleLibrary = checkList.get(0);
            String versionNew = libraryInfo.getVersion();
            BigDecimal verNew = new BigDecimal(versionNew);
            String versionOld = ruleLibrary.getVersion();
            BigDecimal verOld = new BigDecimal(versionOld);
            if (verNew.compareTo(verOld) == 0) {
                return 2; //版本相同
            } else if (verNew.compareTo(verOld) > 0) {
                return 0; //新版更新
            } else {
                return 1; //旧版不跟新
            }
        } else {
            return 3;//库表不存在，为新组件库
        }
    }



    /**
     * 根据扫面结果生成注册表和参数表的插入批量list
     * @param comList //注册list
     * @param paramMap //参数map
     * @param target //请求报文-组件对象
     * @param keyList //扫面结果-关键字信息
     */
    public void  getListForRulePluginInsert(List<ReRulePluginInfo> comList, Map<String,List<ReRulePluginParameter>> paramMap,
               ReRulePluginLibrary target, List<RuleInfo> keyList, String version, String libraryId) {
        FunctionTools tools = new FunctionTools();
        for (RuleInfo keywordInfo : keyList) {
            //注册表
            ReRulePluginInfo ruleInfo = new ReRulePluginInfo();
            String ruleId = tools.getUUID();
            ruleInfo.setId(ruleId);
            ruleInfo.setRuleName(keywordInfo.getRuleName());
            ruleInfo.setDescription(keywordInfo.getRuleDocumentation());
            ruleInfo.setDataName(keywordInfo.getRuleAlias());
            ruleInfo.setLibraryId(libraryId);
            ruleInfo.setIsValid("1");//默认有效
            ruleInfo.setVersion(version);

            //参数表
            List<ReRulePluginParameter> paramList = new ArrayList<ReRulePluginParameter>();
            List<RuleArgument> kaList = keywordInfo.getRuleArguments();
            int listNumber = kaList.size();
            int order = 1;
            for (RuleArgument rule : kaList) {
                ReRulePluginParameter param = new ReRulePluginParameter();
                param.setId(tools.getUUID());
                param.setRuleId(ruleId);
                if(rule.getType() != null){
                    param.setParameterType(rule.getType().name());
                }
                param.setParameterFlag("0"); //输入参数
                param.setDefaultValue(rule.getDefaultValue());
                param.setParameterComment(rule.getComment());
                param.setRequired(String.valueOf(rule.isRequired()));
                param.setParameterName(rule.getName());
                param.setParameterOrder(tools.getOrder(order));
                order++;
                paramList.add(param);
            }
            //添加每个插件对应的输出
            RuleReturn ruleReturn = keywordInfo.getRuleReturn();
            if(ruleReturn != null){
                listNumber++;
                ReRulePluginParameter param = new ReRulePluginParameter();
                param.setId(tools.getUUID());
                if(ruleReturn.getType() != null){
                    param.setParameterType(ruleReturn.getType().name());
                }
                param.setRuleId(ruleId);
                param.setParameterFlag("1"); //输出参数
                param.setDefaultValue(ruleReturn.getDefaultValue());
                param.setParameterComment(ruleReturn.getComment());
                param.setRequired(String.valueOf(ruleReturn.isRequired()));
                param.setParameterName(ruleReturn.getName());
                param.setParameterOrder(tools.getOrder(order));
                paramList.add(param);
            }
            ruleInfo.setParameterNumber(listNumber+"");
            comList.add(ruleInfo);
            paramMap.put(ruleId,paramList);
        }
    }

    /**
     * 更新插件信息
     * @param infoList
     * @param paramMap
     * @param checkResult
     * @param id
     * @param target
     * @param toolMap
     * @return
     */
    @Override
    public void insertRulePluginInfo(List<ReRulePluginInfo> infoList,Map<String,List<ReRulePluginParameter>> paramMap,
                                        int checkResult,String id,ReRulePluginLibrary target,Map<String,Object> toolMap) {
        FunctionTools tools = new FunctionTools();
        if(checkResult == 3){
            //新插件库，全部插入
            reRulePluginInfoService.insertByBatch(infoList);
        }
        if (checkResult == 0){
            //插件更新-部分插入
            ReRulePluginInfo query = new ReRulePluginInfo();
            query.setLibraryId(id);
            List<ReRulePluginInfo> queryInfoList = reRulePluginInfoService.query(query);
            //TODO-是否需要更换循环方式，循环套循环次数会根据list大小成倍增加。
            //改变方式-循环被查找list，根据插件名+参数个数为主键，插件信息为值的方式进行直接获取判定
            for(ReRulePluginInfo scanRule : infoList){
                for(ReRulePluginInfo tableRule : queryInfoList){
                    //分辨扫描信息中，已存在的和新增的（暂不会存在删除）
                    if(scanRule.getRuleName().equals(tableRule.getRuleName()) &&
                            scanRule.getParameterNumber().equals(tableRule.getParameterNumber())){
                        //已存在进行更新
                        String ruleId = scanRule.getId();
                        scanRule.setId(tableRule.getId());
                        scanRule.setVersion(tableRule.getVersion());
                        toolMap.put(tableRule.getId()+"","1");
                        //更新参数map
                        List<ReRulePluginParameter> paList = paramMap.get(ruleId);
                        for(ReRulePluginParameter one : paList){
                            one.setRuleId(ruleId);
                        }
                        paramMap.put(scanRule.getId(),paList);
                        paramMap.remove(ruleId);
                    }
                }
            }
            reRulePluginInfoService.deleteByExample(query);
            reRulePluginInfoService.insertByBatch(infoList);
        }
    }

    /**
     *
     * @param paramList
     * @param checkResult
     * @param id
     * @param target
     * @param toolMap
     * @return
     */
    @Override
    public void insertRulePluginParam(List<ReRulePluginParameter> paramList, int checkResult, String id,
                                      ReRulePluginLibrary target, Map<String, Object> toolMap) {
        if(checkResult == 3){
            //新插件库，全部插入
            reRulePluginParameterService.insertByBatch(paramList);
        }if (checkResult == 0){
            //组件更新-部分插入
            List<String> idList = new ArrayList<String>();
            Map<String,List<ReRulePluginParameter>> allParamMap = queryParamList(toolMap);
            for(ReRulePluginParameter scanParam : paramList){
                String ruleId = scanParam.getRuleId();
                if(allParamMap.containsKey(ruleId)){
                    //组件存在，更新
                    List<ReRulePluginParameter> comParList = allParamMap.get(ruleId);
                    for(ReRulePluginParameter checkParam : comParList){
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
            String[] ids = new String[idList.size()];
            int i = 0;
            for(String str: idList){
                ids[i] = str;
                i++;
            }
            reRulePluginParameterService.deleteByPrimaryKeys(ids);
            reRulePluginParameterService.insertByBatch(paramList);
        }
    }

    /**
     * 查询插件件对应的参数
     * @param toolMap
     * @return
     */
    private Map<String,List<ReRulePluginParameter>> queryParamList(Map<String,Object> toolMap){
        String queryRule = "";
        for(String key : toolMap.keySet()){
            if("".equals(queryRule)){
                queryRule = queryRule+"'"+key+"'";
            }else{
                queryRule = queryRule+",'"+key+"'";
            }
        }
        Map<String,Object> query = new HashMap<String,Object>();
        query.put("queryRule",queryRule);
        List<ReRulePluginParameter> allParList = reRulePluginParameterService.queryParamForRuleList(query);
        Map<String,List<ReRulePluginParameter>> result = new HashMap<String,List<ReRulePluginParameter>>();
        for(ReRulePluginParameter param : allParList){
            if(result.containsKey(param.getRuleId())){
                List<ReRulePluginParameter> list = result.get(param.getRuleId());
                list.add(param);
            }else{
                List<ReRulePluginParameter> list = new ArrayList<ReRulePluginParameter>();
                list.add(param);
                result.put(param.getRuleId()+"",list);
            }
        }
        return result;
    }
}
