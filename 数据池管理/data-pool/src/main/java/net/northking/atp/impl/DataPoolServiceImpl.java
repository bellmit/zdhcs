package net.northking.atp.impl;

import net.northking.atp.db.persistent.ReDataPool;
import net.northking.atp.db.persistent.ReDataPoolInfo;
import net.northking.atp.db.persistent.ReDataPoolInfoParam;
import net.northking.atp.db.persistent.ReDataPoolValue;
import net.northking.atp.db.service.ReDataPoolInfoService;
import net.northking.atp.db.service.ReDataPoolService;
import net.northking.atp.db.service.ReDataPoolValueService;
import net.northking.atp.entity.InterfaceDataPoolCopy;
import net.northking.atp.service.DataPoolService;
import net.northking.atp.util.FunctionTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static javafx.scene.input.KeyCode.R;

/**
 * Created by Administrator on 2019/7/19 0019.
 */
@Service
public class DataPoolServiceImpl implements DataPoolService {
    @Autowired
    private ReDataPoolService reDataPoolService;
    @Autowired
    private ReDataPoolInfoService reDataPoolInfoService;
    @Autowired
    private ReDataPoolValueService reDataPoolValueService;
    @Autowired
    private DataPoolService dataPoolService;
    @Autowired
    private FunctionTools functionTools;
    /**
     * 在数据池总览表中插入数据记录
     * @return
     */
    @Override
    public void saveDataRecord(ReDataPool data) {
        FunctionTools tools = new FunctionTools();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        data.setId(tools.getUUID());
        if (data.getUseTimeStart() ==null || "".equals(data.getUseTimeStart())){
            try {
                data.setUseTimeStart(sdf.parse("2000-01-01 06:00:00"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (data.getUseTimeStop() ==null || "".equals(data.getUseTimeStop())){
            try {
                data.setUseTimeStop(sdf.parse("2037-12-31 06:00:00"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        data.setIsValid("1");
        reDataPoolService.insert(data);
    }

    /**
     * 更新数据池记录数据
     * @param data
     */
    @Override
    public void updateDataRecord(ReDataPool data) {
        ReDataPool query = new ReDataPool();
        query.setDataId(data.getDataId());
        query.setProjectId(data.getProjectId());
        query.setIsValid(data.getIsValid());
        List<ReDataPool> queryList = reDataPoolService.query(query);
        if(queryList != null && queryList.size()>0){
            data.setId(queryList.get(0).getId());
        }
        data.setDataValue("");
        reDataPoolService.updateByPrimaryKey(data);
    }

    /**
     * 校验是否存在
     * @param data
     * @return
     */
    @Override
    public boolean checkRecordExist(ReDataPool data) {
        //判定法则：有效期内是否存在其他同名动态或静态数据
        ReDataPool check = new ReDataPool();
        check.setIsValid("1");
        check.setDataName(data.getDataName());
        check.setProjectId(data.getProjectId());
        Date now = new Date();
        check.setUseTimeStartGe(now);
        check.setUseTimeStopLe(now);
        if("0".equals(data.getDataFalg())){
            check.setDataFalg("1");
        }else{
            check.setDataFalg("0");
        }
        List<ReDataPool> result = reDataPoolService.query(check);
        if(result != null && result.size()>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断变量名属于静态还是动态数据
     * @param data
     * @return
     */
    @Override
    public String queryFlagByDataRecord(ReDataPool data) {
        ReDataPool check = new ReDataPool();
        check.setIsValid("1");
        check.setDataName(data.getDataName());
        check.setProjectId(data.getProjectId());
        Date now = new Date();
        check.setUseTimeStartLe(now);
        check.setUseTimeStopGe(now);
        List<ReDataPool> result = reDataPoolService.query(check);
        if(result ==null || result.size()<1){
            return "2"; //无有效数据
        }else{
            return result.get(0).getDataFalg();
        }
    }

    /**
     * 复制静态数据
     * @param target
     */
    @Transactional
    @Override
    public void copyStaticData(InterfaceDataPoolCopy target) {
        for(String id : target.getIdList()){
            //查询数据
            ReDataPoolInfo dataInfo =  reDataPoolInfoService.findByPrimaryKey(id);//静态数据信息
            ReDataPoolValue values = new ReDataPoolValue();
            values.setDataPoolInfoId(dataInfo.getId());
            List<ReDataPoolValue> dataValues = reDataPoolValueService.query(values);//静态数据值

            //名称生成
            String newName = "";
            String name = dataInfo.getPropKey();
            ReDataPoolInfo nameQuery = new ReDataPoolInfo();
            nameQuery.setProjectId(target.getProjectId());
            nameQuery.setPropKey(name);
            if(target.getProfileId()!=null){
                nameQuery.setProfileId(target.getProfileId());
            }
            List<ReDataPoolInfo> nameList = reDataPoolInfoService.query(nameQuery);
            if(nameList!=null && nameList.size()>0){
                nameQuery.setPropKey(name+"_复制(%%)");
                String maxName = reDataPoolInfoService.queryMaxName(nameQuery);
                if(maxName==null || maxName.isEmpty()){
                    newName = name+"_复制(1)";
                }else{
                    newName= getNewMaxName(maxName);
                }
            }else {
                newName = name;
            }

            String dataInfoId = functionTools.getUUID();
            dataInfo.setId(dataInfoId);
            dataInfo.setProfileId(target.getProfileId());
            dataInfo.setPropKey(newName);
            dataInfo.setCreateTime(new Date());
            dataInfo.setUpdateTime(new Date());
            reDataPoolInfoService.insert(dataInfo);

//            ReDataPoolInfo exitsParam=new ReDataPoolInfo();
//            exitsParam.setProjectId(target.getProjectId());
//            exitsParam.setPropKey(newName);
//            if(target.getProfileId() != null){
//                exitsParam.setProfileId(target.getProfileId());
//            }
//            List<ReDataPoolInfo> reDataPoolInfoListExist = reDataPoolInfoService.queryIdForInsert(exitsParam);
//            if(reDataPoolInfoListExist!=null && reDataPoolInfoListExist.size()>0){
//                ReDataPoolInfo currentData =  reDataPoolInfoListExist.get(0);
//                dataInfo.setId(currentData.getId());
//            }

            for(ReDataPoolValue value : dataValues){
                value.setId(functionTools.getUUID());
                value.setDataPoolInfoId(dataInfo.getId());
                reDataPoolValueService.insert(value);
            }

            //zcy 测试——增加数据池记录
            ReDataPool dataPool = new ReDataPool();
            dataPool.setDataId(dataInfo.getId());
            dataPool.setDataName(dataInfo.getPropKey());
            dataPool.setProjectId(dataInfo.getProjectId());
            dataPool.setDataFalg("1");
            dataPoolService.saveDataRecord(dataPool);
        }
    }

    /**
     * 括号内数值加一返回
     * @param maxName
     * @return
     */
    private String getNewMaxName(String maxName){
        String result = "";
        String[] nameStr = maxName.split("_");
        for (int i = 0; i < nameStr.length; i++) {
            if(i < nameStr.length-1){
                result = result + nameStr[i]+"_";
            }else{
                String tool = nameStr[i];
                String num = tool.split("\\(")[1].split("\\)")[0];
                result = result + tool.split("\\(")[0]+"("+(Integer.parseInt(num)+1)+")";
            }
        }

        return result;
    }
}
