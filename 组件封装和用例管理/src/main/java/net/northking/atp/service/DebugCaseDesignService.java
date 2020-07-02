package net.northking.atp.service;

import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.db.persistent.ReComponentParameter;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/4/23 0023.
 */
public interface DebugCaseDesignService {
    List<ReComponentParameter> analysisCaseComponentInfo(ReCaseDesignInfo target);
}
