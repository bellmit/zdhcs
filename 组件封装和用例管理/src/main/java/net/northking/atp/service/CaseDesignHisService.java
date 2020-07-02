package net.northking.atp.service;


import net.northking.atp.db.persistent.HisCaseDesignInfo;
import net.northking.atp.entity.InterfaceComponentInfo;
import net.northking.atp.entity.InterfaceStepComponent;

import java.util.List;
import java.util.Map;

/**
 * 案例信息历史操作服务
 * Created by Administrator on 2019/6/11 0011.
 */
public interface CaseDesignHisService {
    void insertCaseInfoHis(Map<String,Object> infoMap);
    Map<String,Object> queryCaseInfoVersion(HisCaseDesignInfo target);
    List<InterfaceStepComponent> queryDetailHisCaseInfo(HisCaseDesignInfo target);
}
