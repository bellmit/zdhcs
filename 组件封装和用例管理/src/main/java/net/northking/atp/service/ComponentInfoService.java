package net.northking.atp.service;

import net.northking.atp.db.persistent.MdComponentInfo;
import net.northking.atp.db.persistent.ReComponentInfo;
import net.northking.atp.db.persistent.ReComponentParameter;
import net.northking.atp.entity.InterfaceComponentInfo;
import net.northking.atp.entity.InterfaceComponentPackage;

import java.util.List;
import java.util.Map;

/**
 * 组件维护
 * Created by Administrator on 2019/4/9 0009.
 */
public interface ComponentInfoService {

    boolean checkComponentExist(ReComponentInfo reComponentInfo);
    List<InterfaceComponentPackage> queryComponentAndParamList(InterfaceComponentInfo target);
    boolean insertFormalComponentInfo(Map<String,Object> infoMap);
    boolean updateComponentInfo(InterfaceComponentInfo target);
    boolean deleteComponentInfo(ReComponentInfo target);
    String queryLibraryNameByComId(String id);
}
