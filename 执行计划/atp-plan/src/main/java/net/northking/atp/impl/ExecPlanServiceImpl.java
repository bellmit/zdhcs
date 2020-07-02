package net.northking.atp.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.northking.atp.CaseDesignFeignClient;
import net.northking.atp.DataPoolClient;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.ExecutePlan;
import net.northking.atp.entity.TestEnvInfo;
import net.northking.atp.enums.*;
import net.northking.atp.producer.PlanPluginProducer;
import net.northking.atp.service.ExecPlanService;
import net.northking.atp.service.PlanExecuteInfoService;
import net.northking.atp.service.TestEnvService;
import net.northking.atp.utils.BeanUtil;
import net.northking.atp.utils.UUIDUtil;
import net.northking.db.DefaultPagination;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author jieying.li
 */
@Service
public class ExecPlanServiceImpl implements ExecPlanService {

    private static final Logger logger = LoggerFactory.getLogger(ExecPlanServiceImpl.class);

    @Autowired
    private ReExecPlanService reExecPlanService;    // 测试计划信息

    @Autowired
    private RePlanExecInfoService rePlanExecInfoService;    // 测试计划执行记录

    @Autowired
    private ReExecPlanPluginSettingService pluginSettingService;

    @Autowired
    private ReExecPlanCaseSetRelService reExecPlanCaseSetRelService;    // 测试计划-用例集关联

    @Autowired
    private CaseDesignFeignClient caseDesignFeignClient;

    @Autowired
    private PlanPluginProducer planPluginProducer;

    @Autowired
    private PlanExecuteInfoService planExecuteInfoService;

    @Autowired
    private RePlanEnvRelService rePlanEnvRelService;

    @Autowired
    private ReTestEnvInfoService reTestEnvInfoService;

    @Autowired
    private TestEnvService testEnvService;

    @Autowired
    private DataPoolClient dataPoolClient;

    /**
     * 新增测试执行计划
     *
     * @param target 计划对象
     * @return
     */
    @Override
    @Transactional
    public ExecutePlan addExecPlan(ExecutePlan target) {
        try {
            if (target.getId() == null) {   // 对于临时计划，页面产生计划id
                // 测试执行计划情况
                target.setId(UUIDUtil.getUUIDWithoutDash());    // 计划主键
            }
            target.setCreateTime(new Date());   // 创建时间
            target.setPlanStatus(ExecPlanStatus.NEW.code());    // 默认初始状态
            // 关联插件
            addExecPlanPluginRel(target);
            // 关联用例集
            addPlanCaseSetRel(target);
            // 关联测试环境
            addTestEnvRel(target);
            // 新增执行计划
            reExecPlanService.insert(target);
            return queryPlanSettingById(target.getId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("测试执行计划添加失败", e);
        }
    }

    private void addTestEnvRel(ExecutePlan target) {
        List<TestEnvInfo> envInfos = target.getEnvInfos();
        if (envInfos != null && envInfos.size() > 0) {
            // 勾选了测试环境
            for (TestEnvInfo envInfo : envInfos) {
                ReTestEnvInfo resultEnvInfo = reTestEnvInfoService.findByPrimaryKey(envInfo.getId());
                if (resultEnvInfo != null) {
                    RePlanEnvRel rePlanEnvRel = new RePlanEnvRel();
                    createPlanEnvRel(target, rePlanEnvRel, envInfo);
                    rePlanEnvRelService.insert(rePlanEnvRel);
                }
            }
        } else {
            // 没有勾选测试环境，给定默认的测试环境，默认是web/ui测试环境windows 10 + IE 11
            RePlanEnvRel envRel = new RePlanEnvRel();
            // 查询当前计划默认测试环境
//            ReTestEnvInfo envQuery = new ReTestEnvInfo();
            ReTestEnvInfo defaultEnvInfo = testEnvService.checkAndAddDefaultEnv(target.getProjectId(),
                    "windows", "10", "Internet Explorer", "11", PlanClass.WEB_UI);
//            envQuery.setProjectId(target.getProjectId());
//            envQuery.setDefaultEnv(DefaultEnv.DEFAULT.code());
//            envQuery.setEnvType(target.getPlanClass());
//            List<ReTestEnvInfo> envResultList = reTestEnvInfoService.query(envQuery);
//            if (envResultList != null && envResultList.size() == 1) {
//                ReTestEnvInfo defaultEnvInfo = envResultList.get(0);
//            }
            createPlanEnvRel(target, envRel, defaultEnvInfo);
            rePlanEnvRelService.insert(envRel);
        }
    }

    private void createPlanEnvRel(ExecutePlan target, RePlanEnvRel envRel, ReTestEnvInfo envInfo) {
        envRel.setId(UUIDUtil.getUUIDWithoutDash());
        envRel.setProjectId(target.getProjectId());
        envRel.setPlanId(target.getId());
        envRel.setEnvId(envInfo.getId());
        envRel.setCraeteTime(new Date());
        envRel.setEnvArea(target.getEnvArea());
    }

    /**
     * 添加计划-用例集关联
     *
     * @param target
     */
    private void addPlanCaseSetRel(ExecutePlan target) {
        if (target.getCaseSets() != null && target.getCaseSets().length > 0) {
            List<ReExecPlanCaseSetRel> caseSetList = new ArrayList<>();
            String[] caseSets = target.getCaseSets();
            for (String caseSet : caseSets) {
                ReExecPlanCaseSetRel rel = new ReExecPlanCaseSetRel();
                rel.setId(UUIDUtil.getUUIDWithoutDash());       // 主键
                rel.setPlanId(target.getId());                  // 关联计划ID
                rel.setCaseSet(caseSet);                        // 关联用例集ID
                caseSetList.add(rel);
            }
            // 用例关联
            reExecPlanCaseSetRelService.insertByBatch(caseSetList);
        }
    }

    /**
     * 添加插件设置信息
     *
     * @param target 计划对象信息
     */
    private void addExecPlanPluginRel(ExecutePlan target) {
        List<ReExecPlanPluginSetting> pluginLists = target.getPluginLists();
        if (pluginLists != null && pluginLists.size() > 0) {
            // 遍历插件配置，逐个保存
            savePluginSetting(target, pluginLists);
        }
    }

    private void savePluginSetting(ExecutePlan target, List<ReExecPlanPluginSetting> pluginLists) {
        for (ReExecPlanPluginSetting setting : pluginLists) {
            // 利用队列，将相对应的插件设置信息放入到队列中，由相应的队列监听服务保存具体配置信息
            try {
                planPluginProducer.pushPlanPluginToQueue(setting, target);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 分页查询执行计划列表
     *
     * @param target 查询条件
     * @return
     */
    @Override
    public Pagination<ExecutePlan> queryExecutePlan(QueryByPage<ExecutePlan> target) {
        target.getQuery().setPlanType(PlanType.NORMAL.code());
        ExecutePlan query = target.getQuery();
        SqlOrderBy orderBy = new SqlOrderBy();
        // 按创建时间排序
        orderBy.addOrderBy(ReExecPlan.KEY_createTime, OrderBy.DESC);
        // 按条件获取
        //Pagination<ReExecPlan> page = reExecPlanService.query(query, orderBy, target.getPageNo(), target.getPageSize());
        Pagination<ReExecPlan> page = reExecPlanService.selectPlanListByPage(query, orderBy, target.getPageNo(), target.getPageSize());

        Pagination<ExecutePlan> resultPage = new DefaultPagination<>();
        List<ExecutePlan> plans = resultPage.getRecords();
        resultPage.setPageNo(target.getPageNo());     // 当前页
        resultPage.setPageSize(target.getPageSize()); // 页面记录
        resultPage.setRecordCount(reExecPlanService.queryCount(query)); // 总条目数

        List<ReExecPlan> records = page.getRecords();
        for (ReExecPlan record : records) {
            ExecutePlan plan = new ExecutePlan();
            BeanUtils.copyProperties(record, plan);
            // 查询最近执行情况
            RePlanExecInfo latelyExecInfo = rePlanExecInfoService.queryFirstById(plan.getId());
            // 添加最近执行情况
            plan.setRePlanExecInfo(latelyExecInfo);
            if (plans == null) {
                plans = new ArrayList<>();
            }
            plans.add(plan);
        }
        return resultPage;
    }

    /**
     * 根据计划id获取执行计划信息
     *
     * @param planId
     * @return
     */
    @Override
    public ExecutePlan queryPlanSettingById(String planId) {
        ExecutePlan executePlan = new ExecutePlan();
        ReExecPlan ep = reExecPlanService.findByPrimaryKey(planId);
        if (ep == null) {
            return null;
        }
        BeanUtils.copyProperties(ep, executePlan);
        // 插件使用情况（只查看已启用的）
        ReExecPlanPluginSetting pluginSettingRecord = new ReExecPlanPluginSetting();
        pluginSettingRecord.setPlanId(planId);
        pluginSettingRecord.setStatus(PluginSettingStatus.ENABLE.code());
        List<ReExecPlanPluginSetting> pluginList = pluginSettingService.query(pluginSettingRecord);
        executePlan.setPluginLists(pluginList);
        // 用例集
//        List<ReCaseSet> caseSets = reCaseSetService.queryAllCaseSetsByPlanId(planId);
        List<ReCaseSet> caseSets = (List<ReCaseSet>) caseDesignFeignClient.queryAllCaseSetsByPlanId(planId).getData();
        executePlan.setCaseSetsInfo(caseSets);
        // 获取数据环境
        ReProfileInfo profileQuery = new ReProfileInfo();
        profileQuery.setId(executePlan.getProfileId());

//        ResultWrapper<ReProfileInfo> profileWrapper = dataPoolClient.findProfileInfoById(executePlan.getProfileId().toString());
        ResultWrapper<ReProfileInfo> profileWrapper = dataPoolClient.queryByPrimaryKey(profileQuery);
        if (profileWrapper != null && profileWrapper.getData() != null) {
            executePlan.setReProfileInfo(profileWrapper.getData());
        }
        // 测试环境
        queryPlanTestEnv(planId, executePlan);
        return executePlan;
    }

    private void queryPlanTestEnv(String planId, ExecutePlan executePlan) {
        RePlanEnvRel relQuery = new RePlanEnvRel();
        relQuery.setPlanId(planId);
        List<RePlanEnvRel> envRels = rePlanEnvRelService.query(relQuery);
        if (envRels != null && envRels.size() > 0) {
            List<TestEnvInfo> envInfos = new ArrayList<>();
            for (RePlanEnvRel envRel : envRels) {
                ReTestEnvInfo envInfo = reTestEnvInfoService.findByPrimaryKey(envRel.getEnvId());
                TestEnvInfo testEnvInfo = reTestEnvInfoService.setTestEnvInfoDetails(envInfo);
                envInfos.add(testEnvInfo);
            }
            executePlan.setEnvInfos(envInfos);

            //获取执行引擎所属地区
            if(null != envRels.get(0)) {
                executePlan.setEnvArea(envRels.get(0).getEnvArea());
            }
        }
    }


    /**
     * 根据id删除
     *
     * @param planId
     */
    @Override
    public int deleteExecPlan(String planId) {
        logger.info("被删除的计划ID：" + planId);
        // 删除执行记录（任务、任务步骤、执行日志、附件）
        planExecuteInfoService.deleteExecRecord(planId);
        // 删除计划
        int count = reExecPlanService.deleteByPrimaryKey(planId);
        logger.info("计划删除数量 -------> " + count);
        return count;
    }

    /**
     * 修改执行计划信息
     *
     * @param plan
     */
    @Override
    @Transactional
    public void updateExecPlan(ExecutePlan plan) {
        logger.debug("计划更新信息：" + plan.toMap().toString());
        try {
            ReExecPlan target = new ReExecPlan();
            BeanUtils.copyProperties(plan, target);
            target.setUpdateTime(new Date());
            // 更新插件配置
            updateReExecPlanPluginSettings(plan);
            // 更新用例集关联情况
            updatePlanCaseRelInfo(plan);
            // 更新测试环境信息
            updatePlanTestEnvInfo(plan);
            // 更新计划信息
            reExecPlanService.updateByPrimaryKey(target);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("计划更新失败", e);
        }
    }

    /**
     * 更新计划的测试环境信息
     *
     * @param plan 执行计划信息
     */
    private void updatePlanTestEnvInfo(ExecutePlan plan) {
//        List<TestEnvInfo> newEnvInfos = plan.getEnvInfos();
        //  删除之前的关联
        RePlanEnvRel relQuery = new RePlanEnvRel();
        relQuery.setPlanId(plan.getId());
        rePlanEnvRelService.deleteByExample(relQuery);
        // 重新插入新的关联
        addTestEnvRel(plan);
    }

    /**
     * 修改计划-用例关联关系
     *
     * @param plan 执行计划对象
     */
    private void updatePlanCaseRelInfo(ExecutePlan plan) {
        ReExecPlanCaseSetRel relRecord = new ReExecPlanCaseSetRel();
        relRecord.setPlanId(plan.getId());
        // 获取旧的用例集关联关系
        List<ReExecPlanCaseSetRel> relList = reExecPlanCaseSetRelService.query(relRecord);
        List<String> oldRelList = new ArrayList<>();
        for (ReExecPlanCaseSetRel planCaseSetRel : relList) {
            oldRelList.add(planCaseSetRel.getCaseSet());
        }
        // 新的关联关系
        List<String> newRelList;
        if (plan.getCaseSets() == null) {
            newRelList = new ArrayList<>();
        } else {
            newRelList = Arrays.asList(plan.getCaseSets());
        }
        // 新添加的list
        List<ReExecPlanCaseSetRel> addRelList = new ArrayList<>();
        // 将要移除的list
        List<ReExecPlanCaseSetRel> removeList = new ArrayList<>();
        for (String newCaseSetId : newRelList) {
            if (!oldRelList.contains(newCaseSetId)) {
                ReExecPlanCaseSetRel add = new ReExecPlanCaseSetRel();
                add.setCaseSet(newCaseSetId);
                add.setId(UUIDUtil.getUUIDWithoutDash());
                add.setPlanId(plan.getId());
                addRelList.add(add);
            }
        }
        oldRelList.forEach(oldCaseSetId -> {
            if (!newRelList.contains(oldCaseSetId)) {
                ReExecPlanCaseSetRel record = new ReExecPlanCaseSetRel();
                record.setCaseSet(oldCaseSetId);
                record.setPlanId(plan.getId());
                removeList.add(record);
            }
        });
        // 移除关联
        if (removeList.size() > 0) {
            removeList.forEach(rel -> reExecPlanCaseSetRelService.deleteByExample(rel));
        }
        // 添加新的关联
        if (addRelList.size() > 0) {
            reExecPlanCaseSetRelService.insertByBatch(addRelList);
        }
    }

    /**
     * 修改计划-插件配置信息
     *
     * @param plan 计划对象
     * @return
     */
    private void updateReExecPlanPluginSettings(ExecutePlan plan) {
        // 新的插件配置
        List<ReExecPlanPluginSetting> newPluginLists = plan.getPluginLists();
        if (newPluginLists == null) {
            newPluginLists = new ArrayList<>();
        }
        // 条件查询对象
        ReExecPlanPluginSetting pluginSettingRecord = new ReExecPlanPluginSetting();
        pluginSettingRecord.setPlanId(plan.getId());
        pluginSettingRecord.setStatus(PluginSettingStatus.ENABLE.code());
        // 原来的插件配置
        List<ReExecPlanPluginSetting> oldPluginSettingList = pluginSettingService.query(pluginSettingRecord);
        List<ReExecPlanPluginSetting> addList = new ArrayList<>();      // 新增的用例集集合
        List<ReExecPlanPluginSetting> updateList = new ArrayList<>();   // 更新的用例集集合
        List<String> delIdList = new ArrayList<>();                     // 不再关联的用例集集合
        List<String> temp = new ArrayList<>();                          // 临时集合
        for (ReExecPlanPluginSetting newSetting : newPluginLists) {
            if (oldPluginSettingList.size() == 0) {
                // 新插件配置
                addList.add(newSetting);
            } else {
                for (ReExecPlanPluginSetting oldSetting : oldPluginSettingList) {
                    if (oldSetting.getId().equals(newSetting.getId())) {
                        // 是同一条记录
                        if (oldSetting.getPluginId().equals(newSetting.getPluginId())
                                && oldSetting.getPluginParamValue().equals(newSetting.getPluginParamValue())) {
                            // 不需要更新记录
                            logger.debug("计划{}下的插件{}没有不需要更新信息", plan.getId(), newSetting.getId());
                        } else {
                            // 需要更新记录
//                            newSetting.setUpdateTime(new Date());
                            updateList.add(newSetting);
                        }
                    } else {
                        // 新插件配置
                        addList.add(newSetting);
                    }
                }
            }
            temp.add(newSetting.getId());
        }
        // 需要取消关联关系
        for (ReExecPlanPluginSetting oldSetting : oldPluginSettingList) {
            if (!temp.contains(oldSetting.getId())) {
                delIdList.add(oldSetting.getId());
            }
        }
        if (delIdList.size() > 0) {
            for (String delSettingId : delIdList) {
                // 删除插件具体信息逻辑
                ReExecPlanPluginSetting setting = new ReExecPlanPluginSetting();
                setting.setId(delSettingId);
                setting.setStatus(PluginSettingStatus.DISABLE.code());
                pluginSettingService.updateByPrimaryKey(setting);
            }
        }
        if (updateList.size() > 0) {
            for (ReExecPlanPluginSetting setting : updateList) {
                // todo: 更新插件信息逻辑
                pluginSettingService.updateByPrimaryKey(setting);
                savePluginSetting(plan, updateList);
            }
        }
        if (addList.size() > 0) {
//            pluginSettingService.insertByBatch(addList);
            // 新增到各个对应的插件信息表
            savePluginSetting(plan, addList);
        }
    }

    /**
     * 检测当前插件是否可用
     */
    @Override
    public boolean pluginIsEnable(String planPluginId) {
        ReExecPlanPluginSetting setting = pluginSettingService.findByPrimaryKey(planPluginId);
        if (setting != null) {
            return setting.getStatus() == PluginSettingStatus.ENABLE.code();
        }
        return false;
    }

}
