/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.dao.ReDataPoolInfoDao;
import net.northking.atp.db.dao.ReDataPoolValueDao;
import net.northking.atp.db.persistent.ReDataPool;
import net.northking.atp.db.persistent.ReDataPoolInfo;
import net.northking.atp.db.persistent.ReDataPoolInfoParam;
import net.northking.atp.db.persistent.ReDataPoolValue;
import net.northking.atp.service.DataPoolService;
import net.northking.atp.service.ReDataPoolInfoServiceEx;
import net.northking.atp.util.FunctionTools;
import net.northking.db.DefaultPagination;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
   * 
 *
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
@Service
public class ReDataPoolInfoServiceExImpl  implements ReDataPoolInfoServiceEx
{
private static final Logger logger = LoggerFactory.getLogger(ReDataPoolInfoServiceExImpl.class);

  @Autowired
  private ReDataPoolInfoDao reDataPoolInfoDao;
  @Autowired
  private ReDataPoolValueDao reDataPoolValueDao;
  @Autowired
  private DataPoolService dataPoolService;
  @Autowired
  private FunctionTools functionTools;

// ----      The End by Generator     ----//

  /**
   * 查询非本ID对应数据除外的数据
   * @param var1
   * @return
   */
  public List<ReDataPoolInfo> queryForUpdate(ReDataPoolInfo var1) {
    return reDataPoolInfoDao.queryForUpdate(var1);
  }

  public List<ReDataPoolInfo> queryForDistinct(ReDataPoolInfo var1) {
    return reDataPoolInfoDao.queryForDistinct(var1);
  }
  public int deleteReDataPoolByDataPoolInfoId(Object id) {
    return reDataPoolInfoDao.deleteReDataPoolByDataPoolInfoId(id);
  }
  public ReDataPoolInfoParam findDataPoolValueByPrimaryKey(Object id){
    return reDataPoolInfoDao.findDataPoolValueByPrimaryKey(id);
  }
  @Transactional
  public void insert(ReDataPoolInfoParam target){
    String id = functionTools.getUUID();
    target.setId(id);
    if(target.getGlobalName()!=null && !"".equals(target.getGlobalName())){
      reDataPoolInfoDao.insert(target);
//      //取到刚插入数据的ID
//      ReDataPoolInfo exitsParam=new ReDataPoolInfo();
//      exitsParam.setGlobalName(target.getGlobalName());
//      exitsParam.setPropKey(target.getPropKey());
//      List<ReDataPoolInfo> reDataPoolInfoListExist = reDataPoolInfoDao.query(exitsParam);
//      if(reDataPoolInfoListExist!=null && reDataPoolInfoListExist.size()>0){
//        ReDataPoolInfo currentData=  reDataPoolInfoListExist.get(0);
//        target.setId(currentData.getId());
//      }
    }else if(target.getProjectId()!=null && !"".equals(target.getProjectId()) && (target.getProfileId()==null ||"".equals(target.getProfileId()))){


      reDataPoolInfoDao.insert(target);
//      //取到刚插入数据的ID
//      ReDataPoolInfo exitsParam=new ReDataPoolInfo();
//      exitsParam.setProjectId(target.getProjectId());
//
//      exitsParam.setPropKey(target.getPropKey());
//      List<ReDataPoolInfo> reDataPoolInfoListExist = queryForDistinct(exitsParam);
//      if(reDataPoolInfoListExist!=null && reDataPoolInfoListExist.size()>0){
//        ReDataPoolInfo currentData=  reDataPoolInfoListExist.get(0);
//        target.setId(currentData.getId());
//      }
    }else{
      //本项目、环境；相同使用范围的测试参数名称不可重复


      reDataPoolInfoDao.insert(target);
      //取到刚插入数据的ID
//      ReDataPoolInfo exitsParam=new ReDataPoolInfo();
//      exitsParam.setProjectId(target.getProjectId());
//      exitsParam.setProfileId(target.getProfileId());
//      exitsParam.setPropKey(target.getPropKey());
//      List<ReDataPoolInfo> reDataPoolInfoListExist = reDataPoolInfoDao.query(exitsParam);
//      if(reDataPoolInfoListExist!=null && reDataPoolInfoListExist.size()>0){
//        ReDataPoolInfo currentData=  reDataPoolInfoListExist.get(0);
//        target.setId(currentData.getId());
//      }
    }

    ReDataPoolValue reDataPoolValueParam=new ReDataPoolValue();
    reDataPoolValueParam.setDataPoolInfoId(target.getId());
    reDataPoolValueDao.deleteByExample(reDataPoolValueParam);
    //新值
    ReDataPoolValue reDataPoolValueNew=new ReDataPoolValue();
    // 2020.05.13 去掉了id自增，增加设置id，jieying.li
    reDataPoolValueNew.setId(functionTools.getUUID());
    reDataPoolValueNew.setPropValue(target.getPropValue());
    reDataPoolValueNew.setDataPoolInfoId(target.getId());
    reDataPoolValueDao.insert(reDataPoolValueNew);

    //zcy 测试——增加数据池记录
    ReDataPool dataPool = new ReDataPool();
    // 2020.05.13 去掉了id自增，增加设置id，jieying.li
    dataPool.setId(functionTools.getUUID());
    dataPool.setDataId(target.getId());
    dataPool.setDataName(target.getPropKey());
    dataPool.setProjectId(target.getProjectId());
    dataPool.setDataFalg("1");
    dataPoolService.saveDataRecord(dataPool);
  }
  @Transactional
  public void delete( String lid){
    reDataPoolInfoDao.deleteByPrimaryKey(lid);
    deleteReDataPoolByDataPoolInfoId(lid);
  }
  @Transactional
  public void update(ReDataPoolInfoParam target){
    reDataPoolInfoDao.updateByPrimaryKey(target);

    ReDataPoolValue reDataPoolValueParam=new ReDataPoolValue();
    reDataPoolValueParam.setDataPoolInfoId(target.getId());
    reDataPoolValueDao.deleteByExample(reDataPoolValueParam);
    //新值
    ReDataPoolValue reDataPoolValueNew=new ReDataPoolValue();
    reDataPoolValueNew.setId(functionTools.getUUID());
    reDataPoolValueNew.setPropValue(target.getPropValue());
    reDataPoolValueNew.setDataPoolInfoId(target.getId());
    reDataPoolValueDao.insert(reDataPoolValueNew);

    //zcy 测试——修改数据池记录
    ReDataPool dataPool = new ReDataPool();
    dataPool.setDataId(String.valueOf(target.getId()));
    dataPool.setDataName(target.getPropKey());
    dataPool.setProjectId(target.getProjectId());
    dataPool.setDataFalg("1");
    dataPoolService.updateDataRecord(dataPool);
  }
  /**
   * 查询带返回参数值的数据
   * @param var1
   * @return
   */
  public List<ReDataPoolInfoParam> queryDataPoolValueByDataPoolInfo(ReDataPoolInfo var1){
   return  reDataPoolInfoDao.queryDataPoolValueBy(var1);
  }
  public Pagination<ReDataPoolInfo> query(ReDataPoolInfo record, OrderBy orderBy, int pageNo, int pageSize) {
    if (pageNo <= 0) {
      pageNo = 1;
    }

    if (pageSize <= 0 || pageSize > 1000) {
      pageSize = 1000;
    }

    DefaultPagination<ReDataPoolInfo> pagination = new DefaultPagination();
    pagination.setPageSize(pageSize);
    pagination.setPageNo(pageNo);
    if (orderBy != null && orderBy.size() > 0) {
      PageHelper.orderBy(orderBy.toString());
    }

    Page<ReDataPoolInfo> page = PageHelper.startPage(pageNo, pageSize);
    List<ReDataPoolInfo> result = reDataPoolInfoDao.queryForPage(record);
    pagination.setRecords(result);
    pagination.setRecordCount(page.getTotal());
    return pagination;
  }
}

