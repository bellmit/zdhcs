package net.northking.atp.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.northking.atp.db.persistent.ReExecPlanPluginSetting;
import net.northking.atp.db.persistent.RePluginInfo;
import net.northking.atp.db.service.ReExecPlanPluginSettingService;
import net.northking.atp.db.service.RePluginInfoService;
import net.northking.atp.entity.ExecutePlan;
import net.northking.atp.enums.PluginSettingStatus;
import net.northking.atp.mq.RabbitMQEndpoint;
import net.northking.atp.utils.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@Component
public class PlanPluginProducer {
    // 日志
    private static final Logger logger = LoggerFactory.getLogger(PlanPluginProducer.class);

    // rabbitMQ服务
    @Autowired
    private RabbitMQEndpoint rabbitMQEndpoint;

    @Autowired
    private RePluginInfoService rePluginInfoService;

    @Autowired
    private ReExecPlanPluginSettingService reExecPlanPluginSettingService;

    public void pushPlanPluginToQueue(ReExecPlanPluginSetting setting, ExecutePlan planExecInfo)
            throws UnsupportedEncodingException, JsonProcessingException {
        // 查询插件信息
        RePluginInfo pluginInfo = rePluginInfoService.findByPrimaryKey(setting.getPluginId());
        // todo: 更改了判断条件，多了一个非：!
        if (setting.getId() != null && !"".equals(setting.getId())) {
            // 更新插件设置
            setting.setUpdateTime(new Date());
            reExecPlanPluginSettingService.updateByPrimaryKey(setting);
        } else {
            // 添加插件设置信息
            setting.setId(UUIDUtil.getUUIDWithoutDash());
            setting.setStatus(PluginSettingStatus.ENABLE.code());
            setting.setUpdateTime(new Date());
            setting.setPlanId(planExecInfo.getId());
            reExecPlanPluginSettingService.insert(setting);
        }
        rabbitMQEndpoint.sendByJson(pluginInfo.getPluginQueueName(), setting);
        // 将相关信息推送到mq
        logger.info("消息已发送到队列{}中", setting.getPluginId());
    }

}
