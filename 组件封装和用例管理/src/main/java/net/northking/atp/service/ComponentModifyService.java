package net.northking.atp.service;

import net.northking.atp.db.persistent.MdComponentInfo;
import net.northking.atp.db.persistent.ReComponentInfo;
import net.northking.atp.entity.InterfaceComAndCaseCopy;
import net.northking.atp.entity.InterfaceComponentInfo;
import net.northking.atp.entity.InterfaceComponentPacModify;
import net.northking.atp.entity.InterfaceComponentPackage;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/6/4 0004.
 */
public interface ComponentModifyService {
    boolean insertComponentModifyInfo(InterfaceComponentInfo target);
    boolean updateComponentModifyInfo(InterfaceComponentInfo target);
    boolean deleteComponentModifyInfo(String caseId,String projectId);
    InterfaceComponentInfo queryComponentModifyInfo(String id);
    List<InterfaceComponentPackage> queryComponentAndParamList(ReComponentInfo target);
    Map<String,Object> queryComponentToMap(MdComponentInfo target);
    boolean insertComponentModifyByMap(Map<String,Object> infoMap);
    String checkComPackageVersion(MdComponentInfo comInfo);

    void CopyComponentDataToMenu(InterfaceComAndCaseCopy target);
}
