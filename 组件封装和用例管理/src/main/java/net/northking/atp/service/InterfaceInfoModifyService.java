package net.northking.atp.service;

import net.northking.atp.db.persistent.MdInterfaceInfo;
import net.northking.atp.db.persistent.ReInterfaceInfo;
import net.northking.atp.entity.InterfaceInterfaceInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/6/24 0024.
 */
public interface InterfaceInfoModifyService {
    int insertInterfaceInfo(MdInterfaceInfo target);
    boolean saveInterfaceData(Map<String,Object> dataMap,MdInterfaceInfo target);
    void deleteInterfaceData(String id,String projectId);
    Map<String,Object> queryInterfaceInfoToMap(MdInterfaceInfo target);
    void insertModifyInfoByVersion(Map<String,Object> map);
    Map<String,Object> queryDataList(MdInterfaceInfo info);

    Map<String,Object> genComInfoByInterface(MdInterfaceInfo target);
    Map<String,String> getColumnMap();
}
