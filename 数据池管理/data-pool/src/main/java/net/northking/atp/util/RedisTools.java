package net.northking.atp.util;

import net.northking.cloudtest.domain.user.CltUserLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2019/8/19 0019.
 */
@Component
public class RedisTools {
    @Autowired
    RedisUtil redisUtil;

    /**
     * 读取reids缓存获取user信息
     * @param key
     * @return
     */
    public String getRedisUserInfo(String key){
        CltUserLogin user = (CltUserLogin)redisUtil.get(key);
        return user.getUserChnName();
    }
}
