package net.northking.atp.service;

import net.northking.atp.db.persistent.HisComponentInfo;
import net.northking.atp.entity.InterfaceComponentInfo;

import java.util.Map;

/**
 * 组件历史表服务
 * Created by Administrator on 2019/6/10 0010.
 */
public interface ComponentHisInfoService {
    boolean insertComponentHisInfo(Map<String,Object> infoMap);
    Map<String,Object> queryComponentVersion(HisComponentInfo target);
    InterfaceComponentInfo queryHisDetailComponentInfo(HisComponentInfo target);
    String getComponentNameByHis(String comId);
}
