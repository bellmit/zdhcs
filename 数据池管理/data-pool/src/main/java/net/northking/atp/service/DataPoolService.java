package net.northking.atp.service;

import net.northking.atp.db.persistent.ReDataPool;
import net.northking.atp.entity.InterfaceDataPoolCopy;

/**
 * Created by Administrator on 2019/7/19 0019.
 */
public interface DataPoolService {
    void saveDataRecord(ReDataPool data);
    void updateDataRecord(ReDataPool data);
    boolean checkRecordExist(ReDataPool data);
    String queryFlagByDataRecord(ReDataPool data);

    void copyStaticData(InterfaceDataPoolCopy target);
}
