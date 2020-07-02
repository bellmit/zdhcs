package net.northking.atp.service;

import net.northking.atp.db.persistent.*;

/**
 * Created by Administrator on 2019/6/6 0006.
 */
public interface CaseDesignVersionService {
    boolean commitComponentVersion(MdComponentInfo target);
    boolean rollbackComponentVersion(HisComponentInfo target);

    boolean commitCaseDesignVersion(MdCaseDesignInfo target);
    boolean rollbackCaseDesignVersion(HisCaseDesignInfo target);

    boolean commitInterfaceVersion(MdInterfaceInfo target);
    boolean rollbackInterfaceVersion(HisInterfaceInfo target);
}
