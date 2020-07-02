package net.northking.atp.impl;

import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceBusinessRule;
import net.northking.atp.rule.core.RuleLibraryManager;
import net.northking.atp.rule.core.rule.RuleInfo;
import net.northking.atp.service.BusinessRulesService;
import net.northking.atp.util.FunctionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by Administrator on 2019/7/16 0016.
 */
@Service
public class BusinessRulesServiceImpl implements BusinessRulesService {
    @Autowired
    private ReBusinessRulesService reBusinessRulesService;
    @Autowired
    private ReBusinessRulesParameterService reBusinessRulesParameterService;
    @Autowired
    private ReDataPoolService reDataPoolService;
    @Autowired
    private ReRulePluginInfoService reRulePluginInfoService;
    @Autowired
    private ReRulePluginLibraryService reRulePluginLibraryService;
    private static final Logger logger = LoggerFactory.getLogger(BusinessRulesServiceImpl.class);
    /**
     * 校验业务规则是否存在
     * @param info
     * @return
     */
    @Override
    public boolean checkBusinessRulesExist(ReBusinessRules info) {
        ReBusinessRules check = new ReBusinessRules();
        check.setProjectId(info.getProjectId());
        check.setRuleName(info.getRuleName());
        check.setRuleType(info.getRuleType());
        List<ReBusinessRules> checkList = reBusinessRulesService.query(check);
        if(checkList != null && checkList.size()>0){
            if(checkList.get(0).getId().equals(info.getId())){
                //业务规则校验通过
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    /**
     * 插入业务规则数据
     * @param info
     * @return
     */
    @Override
    public int insertRulesInfo(ReBusinessRules info) {
        FunctionTools tools = new FunctionTools();
        //新增
        //info.setId(tools.getUUID());
        info.setCreateTime(new Date());
        info.setRuleStatus("1");//新建
        return reBusinessRulesService.insert(info);
    }

    /**
     * 修改业务规则数据
     * @param info
     */
    @Override
    public void updateRulesInfo(ReBusinessRules info) {
        reBusinessRulesService.updateByPrimaryKey(info);
    }

    /**
     * 删除业务规则
     * @param info
     */
    @Override
    public void deleteRulesInfo(ReBusinessRules info) {
        reBusinessRulesService.deleteByPrimaryKey(info.getId());
        //删除规则参数
        ReBusinessRulesParameter delete = new ReBusinessRulesParameter();
        delete.setRuleId(info.getId());
        reBusinessRulesParameterService.deleteByExample(delete);
    }

    /**
     * 插入参数
     * @param info
     */
    @Override
    public void saveRuleParam(InterfaceBusinessRule info) {
        FunctionTools tools = new FunctionTools();
        for(String id : info.getIdList()){
            //删除重新插入
            ReBusinessRulesParameter delete = new ReBusinessRulesParameter();
            delete.setRuleId(id);
            reBusinessRulesParameterService.deleteByExample(delete);
            if(info.getParamList() != null && info.getParamList().size()>0){
                List<ReBusinessRulesParameter> paList = info.getParamList();
                int num = 0;
                if(paList != null){
                    for(ReBusinessRulesParameter one : paList){
                        one.setId(tools.getUUID());
                        one.setRuleId(id);
                        one.setParameterOrder(tools.getOrder(num));
                        num++;
                    }
                }
                reBusinessRulesParameterService.insertByBatch(paList);
            }
        }
    }

    /**
     * 查询动态数据的生存结果
     * @param info
     * @return
     */
    @Override
    public String queryBusinessRuleValue(ReBusinessRules info) {
        //判断是否可复用
        String result = "";
        ReDataPool query = new ReDataPool();
        query.setIsValid("1");
        query.setDataName(info.getRuleName());
        query.setProjectId(info.getProjectId());
        Date now = new Date();
        query.setUseTimeStartLe(now);
        query.setUseTimeStopGe(now);
        ReDataPool dataPool = reDataPoolService.query(query).get(0);
        if("1".equals(dataPool.getIsReusable())){
            //可复用
            if(dataPool.getDataValue() != null && !"".equals(dataPool.getDataValue())){
                return dataPool.getDataValue();
            }else{
                //调取方法获取
                result = runRuleLibraryToGetData(dataPool.getDataId());
                ReDataPool up = new ReDataPool();
                up.setId(dataPool.getId());
                up.setDataValue(result);
                reDataPoolService.updateByPrimaryKey(up);
            }
        }else{
            //不可复用 调取方法获取
            result = runRuleLibraryToGetData(dataPool.getDataId());
        }
        return result;
    }

    /**
     * 根据生成规则批量生成数据
     * @param target
     * @return
     */
    @Override
    public List<String> genDataListByRuleId(InterfaceBusinessRule target) {
        List<String> resultList = new ArrayList<String>();
        try{
            ReBusinessRules rule = reBusinessRulesService.findByPrimaryKey(target.getId());
            ReRulePluginInfo pluginInfo = reRulePluginInfoService.findByPrimaryKey(rule.getPluginId());
            ReRulePluginLibrary library = reRulePluginLibraryService.findByPrimaryKey(pluginInfo.getLibraryId());
            ReBusinessRulesParameter query = new ReBusinessRulesParameter();
            query.setRuleId(rule.getId());
            List<ReBusinessRulesParameter> paList = reBusinessRulesParameterService.query(query);
            Object result = new Object();
            if(paList != null && paList.size()>0){
                Map<String,Object> map = new HashMap<String,Object>();
                for(ReBusinessRulesParameter one : paList){
                    map.put(one.getParameterName(),one.getDefaultValue());
                }
                try {
                    URL url = new URL(library.getIpPort()+"/"+library.getFileName());
                    for(int i=0;i<target.getNum();i++){
                        result = RuleLibraryManager.getRuleLibrary(library.getLibraryName(),url).runRule(pluginInfo.getRuleName(),map);
                        resultList.add(result.toString());
                    }
                } catch (Exception e) {
                    logger.info(target.getId()+"获取生成数据失败:"+e);
                    return new ArrayList<String>();
                }
            }
        }catch (Exception e){
            logger.info(target.getId()+"数据处理失败:"+e);
            return null;
        }
        return resultList;
    }

    /**
     * 调取插件方法获取规则数据
     * @param dataId
     * @return
     */
    private String runRuleLibraryToGetData(String dataId){
        ReBusinessRules rule = reBusinessRulesService.findByPrimaryKey(dataId);
        ReRulePluginInfo pluginInfo = reRulePluginInfoService.findByPrimaryKey(rule.getPluginId());
        ReRulePluginLibrary library = reRulePluginLibraryService.findByPrimaryKey(pluginInfo.getLibraryId());
        ReBusinessRulesParameter query = new ReBusinessRulesParameter();
        query.setRuleId(rule.getId());
        List<ReBusinessRulesParameter> paList = reBusinessRulesParameterService.query(query);
        Object result = new Object();
        if(paList != null && paList.size()>0){
            Map<String,Object> map = new HashMap<String,Object>();
            for(ReBusinessRulesParameter one : paList){
                map.put(one.getParameterName(),one.getDefaultValue());
            }
            try {
                //URL url = new URL("file:"+(library.getFilePath()+"/"+library.getFileName()));本地
                //URL url = new URL(library.getIpPort()+"/"+library.getFilePath()+"/"+library.getFileName());
                URL url = new URL(library.getIpPort()+"/"+library.getFileName());
                System.out.println("url+++++++++++++++++++++:"+url);
                System.out.println("name+++++++++++++++++++++:"+pluginInfo.getRuleName());
                System.out.println("map+++++++++++++++++++++:"+map);
                result = RuleLibraryManager.getRuleLibrary(library.getLibraryName(),url).runRule(pluginInfo.getRuleName(),map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }
}
