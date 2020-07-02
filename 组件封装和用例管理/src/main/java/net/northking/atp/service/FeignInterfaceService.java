package net.northking.atp.service;


import net.northking.atp.db.persistent.MdCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.entity.InterfaceCaseInfo;
import net.northking.atp.entity.InterfaceCaseStep;
import net.northking.atp.entity.InterfaceStepComponent;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/6/10 0010.
 */
public interface FeignInterfaceService {
    String insertModifyCaseInfo(InterfaceCaseInfo target);

    boolean genSmartPortComponent(Map<String, Object> target);
}
