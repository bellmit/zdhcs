package net.northking.atp.service;

import net.northking.atp.entity.InterfaceCaseInfo;
import net.northking.atp.entity.InterfaceCaseStep;
import net.northking.atp.entity.InterfaceStepComponent;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/4/15 0015.
 */
public interface CaseStepComponentService {
    List<InterfaceStepComponent> queryStepComponentList(InterfaceCaseStep target);
    boolean saverCaseStepInfo(InterfaceCaseInfo target, String hisId);
    boolean updateCaseStepInfo(InterfaceCaseInfo target);

    void deleteCaseInfo(String caseId,String projectId);
    void insertFormalCaseInfo(Map<String,Object> caseMap);
}
