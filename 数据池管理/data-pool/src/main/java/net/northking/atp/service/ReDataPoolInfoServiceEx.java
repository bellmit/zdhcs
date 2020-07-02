/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.service;

import net.northking.atp.db.persistent.ReDataPoolInfo;
import net.northking.atp.db.persistent.ReDataPoolInfoParam;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;

import java.util.List;

public interface ReDataPoolInfoServiceEx
{
// ----      The End by Generator     ----//
    /**
     * 查询非本ID对应数据除外的数据
     * @param var1
     * @return
     */
   public  List<ReDataPoolInfo> queryForUpdate(ReDataPoolInfo var1);
    public List<ReDataPoolInfo> queryForDistinct(ReDataPoolInfo var1);
    public int deleteReDataPoolByDataPoolInfoId(Object id) ;
    public void delete( Long lid);
    public void update(ReDataPoolInfoParam target);
    public void insert(ReDataPoolInfoParam target);
    public ReDataPoolInfoParam findDataPoolValueByPrimaryKey(Object var1);
    /**
     * 查询带返回参数值的数据
     * @param var1
     * @return
     */
    public List<ReDataPoolInfoParam> queryDataPoolValueByDataPoolInfo(ReDataPoolInfo var1);
    /**
     * 分页查询，支持模糊查询
     * @param record
     * @param orderBy
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Pagination<ReDataPoolInfo> query(ReDataPoolInfo record, OrderBy orderBy, int pageNo, int pageSize);
}
