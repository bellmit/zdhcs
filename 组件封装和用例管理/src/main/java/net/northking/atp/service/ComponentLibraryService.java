package net.northking.atp.service;

import net.northking.atp.db.persistent.ReComponentInfo;
import net.northking.atp.db.persistent.ReComponentLibrary;
import net.northking.atp.db.persistent.ReComponentParameter;
import net.northking.atp.element.core.Library;
import net.northking.atp.element.core.keyword.KeywordInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/4/26 0026.
 */
public interface ComponentLibraryService {
    int versionCheck(Library libraryInfo , ReComponentLibrary target,
                     List<ReComponentLibrary> checkList);

    boolean versionInfoSave(ReComponentLibrary reComponentLibrary,
                            List<ReComponentInfo> comList,List<ReComponentParameter> paramList);

    boolean insertCompInfoAndHis(List<ReComponentInfo> comList,Map<String,List<ReComponentParameter>> paramMap,
                    int checkResult,String id,ReComponentLibrary target,Map<String,Object> toolMap);

    boolean insertCompParamInfoAndHis(List<ReComponentParameter> paramList,int checkResult,
                         String id,ReComponentLibrary target,Map<String,Object> toolMap);

    String scanLibraryAndSave(ReComponentLibrary target);

    void getListForComponentInsert(List<ReComponentInfo> comList, Map<String, List<ReComponentParameter>> paramMap,
                                   ReComponentLibrary target, Library libraryInfo, List<KeywordInfo> keyList, String version, String id);
}
