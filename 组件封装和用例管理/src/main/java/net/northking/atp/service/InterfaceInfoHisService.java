package net.northking.atp.service;

import net.northking.atp.db.persistent.HisInterfaceInfo;

import java.util.Map;

/**
 * Created by Administrator on 2019/6/26 0026.
 */
public interface InterfaceInfoHisService {
    Map<String,Object> queryInterfaceInfoToMap(HisInterfaceInfo target);
    void insertInterfaceHisByVersion(Map<String,Object> map);
    Map<String,Object> genComInfoByHisInterface(HisInterfaceInfo target);
    Map<String,Object> queryHisDataList(HisInterfaceInfo target);
}
