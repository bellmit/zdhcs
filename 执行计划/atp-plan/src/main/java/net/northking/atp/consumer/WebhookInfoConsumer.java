package net.northking.atp.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.northking.atp.db.persistent.ReExecPlanPluginSetting;
import net.northking.atp.db.persistent.ReWebhookInfo;
import net.northking.atp.db.service.ReWebhookInfoService;
import net.northking.atp.utils.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class WebhookInfoConsumer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(WebhookInfoConsumer.class);

    @Autowired
    private ReWebhookInfoService reWebhookInfoService;

    @RabbitListener(queues = {"${atp.mq.queue.webhook:Q.pi.webhook}"})
    public void addWebhookInfo(@Payload byte[] body) {
        try {
            ObjectMapper om = new ObjectMapper();
            ReExecPlanPluginSetting setting = om.readValue(body, ReExecPlanPluginSetting.class);
            String paramValue = setting.getPluginParamValue();
            // 新增记录
            ReWebhookInfo whInfo = new ReWebhookInfo();
            whInfo.setId(UUIDUtil.getUUIDWithoutDash());
            whInfo.setCreateTime(new Date());
            whInfo.setUpdateTime(new Date());
            whInfo.setPlanId(setting.getPlanId());
            whInfo.setPlanPluginId(setting.getPluginId());
            // 从插件值获取信息
            JSONObject jsonObject = JSON.parseObject(paramValue);
            whInfo.setToken(jsonObject.getString(ReWebhookInfo.KEY_token));
            reWebhookInfoService.insert(whInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
