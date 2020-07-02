package net.northking.atp.service;

import net.northking.atp.db.persistent.ReCaseDemandTree;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/14 0014.
 */
public interface CaseDemandService {
    List<Map<String,Object>> generateListForQuery(
            List<ReCaseDemandTree> allList, List<ReCaseDemandTree> list, List<Map<String,Object>> toolList);
}
