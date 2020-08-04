package net.northking.atp.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.northking.atp.db.persistent.ReExecPlanPluginSetting;
import net.northking.atp.db.persistent.RePlanTriggerRel;
import net.northking.atp.db.service.RePlanTriggerRelService;
import net.northking.atp.utils.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class PlanTriggerInfoConsumer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(PlanTriggerInfoConsumer.class);

    @Autowired
    private RePlanTriggerRelService rePlanTriggerRelService;

    @RabbitListener(queues = {"${atp.mq.queue.plan-job:Q.pi.planTri}"})
    public void addPlanTriggerInfo(@Payload byte[] body) {
        try {
            ObjectMapper om = new ObjectMapper();
            ReExecPlanPluginSetting setting = om.readValue(body, ReExecPlanPluginSetting.class);

            // 从paramValue插件值的获取信息
            String paramValue = setting.getPluginParamValue();
            JSONObject jsonObject = JSON.parseObject(paramValue);
            String ids = jsonObject.getString(RePlanTriggerRel.KEY_triggerPlanId);
            List<String> triIds = JSON.parseArray(ids, String.class);
            List<RePlanTriggerRel> newList = new ArrayList<>();
            if (triIds != null && triIds.size() > 0) {
                // 获取原来的关联信息
                List<String> oldIds = getOldRelIds(setting);
                // 更新或者添加新的
                for (String triId : triIds) {
                    // 检测是否记录已经存在
                    List<RePlanTriggerRel> resultList = getRePlanTriggerRelsByTriId(setting, triId);
                    if (resultList.size() > 0) {
                        // 更新记录
                        RePlanTriggerRel updateRel = resultList.get(0);
                        if (oldIds.contains(updateRel.getId())) {
                            oldIds.remove(updateRel.getId());
                        } else {
                            updateRel.setUpdateTime(new Date());
                            rePlanTriggerRelService.updateByPrimaryKey(updateRel);
                        }
                    } else {
                        // 新增记录
                        if (triId.equals(setting.getPlanId())) {
                            // 计划不能自我触发
                            continue;
                        }
                        RePlanTriggerRel triInfo = new RePlanTriggerRel();
                        triInfo.setId(UUIDUtil.getUUIDWithoutDash());
                        triInfo.setCreateTime(new Date());
                        triInfo.setUpdateTime(new Date());
                        triInfo.setPlanId(setting.getPlanId());
                        triInfo.setPlanPluginId(setting.getId());
                        triInfo.setTriggerPlanId(triId);
                        newList.add(triInfo);
                    }
                }
                if (oldIds.size() > 0) {
                    // 取消关联
                    rePlanTriggerRelService.deleteByPrimaryKeys(oldIds.toArray());
                }
            }
            if (newList.size() > 0 ) {
                rePlanTriggerRelService.insertByBatch(newList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<RePlanTriggerRel> getRePlanTriggerRelsByTriId(ReExecPlanPluginSetting setting, String triId) {
        RePlanTriggerRel triRelQuery = new RePlanTriggerRel();
        triRelQuery.setPlanId(setting.getPlanId());
        triRelQuery.setTriggerPlanId(triId);
        triRelQuery.setPlanPluginId(setting.getId());
        return rePlanTriggerRelService.query(triRelQuery);
    }

    private List<String> getOldRelIds(ReExecPlanPluginSetting setting) {
        RePlanTriggerRel query = new RePlanTriggerRel();
        query.setPlanPluginId(setting.getId());
        List<RePlanTriggerRel> oldList = rePlanTriggerRelService.query(query);
        List<String> oldIds = new ArrayList<>();
        for (RePlanTriggerRel rel : oldList) {
            oldIds.add(rel.getId());
        }
        return oldIds;
    }
}
