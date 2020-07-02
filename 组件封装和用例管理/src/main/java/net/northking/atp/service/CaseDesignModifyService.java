package net.northking.atp.service;


import net.northking.atp.db.persistent.MdCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.entity.InterfaceCaseInfo;
import net.northking.atp.entity.InterfaceCaseStep;
import net.northking.atp.entity.InterfaceComAndCaseCopy;
import net.northking.atp.entity.InterfaceStepComponent;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/6/10 0010.
 */
public interface CaseDesignModifyService {
    //String getNewCaseNo(String projectId);
    void insertModifyCaseInfo(InterfaceCaseInfo target);
    void updateModifyCaseInfo(InterfaceCaseInfo target);
    void deleteModifyCaseInfo(String caseId,String projectId);
    List<InterfaceStepComponent> queryStepComponentList(InterfaceCaseStep target);
    Map<String,Object> queryCaseInfoToMap(MdCaseDesignInfo target);
    void insertModifyCaseByMap(Map<String,Object> caseMap);
    String checkCaseComStepVersion(MdCaseDesignInfo caseInfo);
    boolean checkCaseDesignExist(ReCaseDesignInfo info);

    void CopyCaseToMenu(InterfaceComAndCaseCopy target);
}
