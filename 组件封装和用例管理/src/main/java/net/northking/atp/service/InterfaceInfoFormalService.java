package net.northking.atp.service;

import net.northking.atp.db.persistent.MdInterfaceInfo;

import java.util.Map;

/**
 * Created by Administrator on 2019/6/25 0025.
 */
public interface InterfaceInfoFormalService {
    void deleteInterfaceFormalData(String id,String projectId);
    void insertInterfaceByVersion(Map<String,Object> map);
    boolean checkInterfaceExist(MdInterfaceInfo info);
}
