package net.northking.atp.impl;

import net.northking.atp.db.persistent.MdInterfaceInfo;
import net.northking.atp.db.persistent.ReInterfaceData;
import net.northking.atp.db.persistent.ReInterfaceInfo;
import net.northking.atp.db.service.MdInterfaceInfoService;
import net.northking.atp.db.service.ReInterfaceDataService;
import net.northking.atp.db.service.ReInterfaceInfoService;
import net.northking.atp.service.InterfaceInfoFormalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/6/25 0025.
 */
@Service
public class InterfaceInfoFormalServiceImpl implements InterfaceInfoFormalService{
    @Autowired
    private ReInterfaceInfoService reInterfaceInfoService;
    @Autowired
    private ReInterfaceDataService reInterfaceDataService;
    @Autowired
    private MdInterfaceInfoService mdInterfaceInfoService;

    /**
     * 删除正式表数据
     * @param id
     */
    @Override
    public void deleteInterfaceFormalData(String id,String projectId) {
        //基础信息的删除
        reInterfaceInfoService.deleteByPrimaryKey(id);
        //接口数据的删除
        ReInterfaceData delete = new ReInterfaceData();
        delete.setProjectId(projectId);
        delete.setInterfaceId(id);
        reInterfaceDataService.deleteByExample(delete);
    }

    /**
     * 版本控制时插入数据
     * @param map
     */
    @Override
    public void insertInterfaceByVersion(Map<String, Object> map) {
        //参数信息
        reInterfaceInfoService.insertInfoForMap(map);
        //插入参数数据
        List<Map<String,Object>> dataList = (List<Map<String,Object>>)map.get("dataList");
        reInterfaceDataService.insertBatchForMap(dataList);
    }

    /**
     * 校验接口名是否已经存在
     * @param info
     * @return
     */
    @Override
    public boolean checkInterfaceExist(MdInterfaceInfo info) {
        MdInterfaceInfo query = new MdInterfaceInfo();
        query.setProjectId(info.getProjectId());
        query.setInterfaceName(info.getInterfaceName());
        List<MdInterfaceInfo> reList = mdInterfaceInfoService.query(query);
        if (reList != null && reList.size() >0){
            //名称已存在
            if(reList.get(0).getId().equals(info.getId())){
                return false;
            }else{
                return true;
            }
        }else{
            //名称不存在
            return false;
        }
    }
}
