package net.northking.atp.service;

import net.northking.atp.db.persistent.ReCaseDesignMenutree;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/28 0028.
 */
public interface CaseDesignService {
    List<Map<String,Object>> generateListForQuery(
            List<ReCaseDesignMenutree> allList, List<ReCaseDesignMenutree> list, List<Map<String,Object>> toolList);
}
