/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.dao;

import net.northking.atp.db.persistent.ReDataPoolInfo;
import net.northking.atp.db.mapper.ReDataPoolInfoMapper;
import net.northking.atp.db.persistent.ReDataPoolInfoParam;

import java.util.List;


/**
 * 
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-05-07 17:10:00  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
public interface ReDataPoolInfoDao extends ReDataPoolInfoMapper
{
  // ----      The End by Generator     ----//
    /**
     * 查询非本ID对应数据除外的数据
     * @param var1
     * @return
     */
    public List<ReDataPoolInfo> queryForUpdate(ReDataPoolInfo var1);
    public int deleteReDataPoolByDataPoolInfoId(Object id) ;

    public ReDataPoolInfoParam findDataPoolValueByPrimaryKey(Object var1);
    //去重专用
    public List<ReDataPoolInfo> queryForDistinct(ReDataPoolInfo var1);
    /**
     * 查询带返回参数值的数据
     * @param var1
     * @return
     */
    public List<ReDataPoolInfoParam> queryDataPoolValueBy(ReDataPoolInfo var1);
    List<ReDataPoolInfo> queryForPage(ReDataPoolInfo var1);

    String queryMaxName(ReDataPoolInfo nameQuery);
    List<ReDataPoolInfo> queryIdForInsert(ReDataPoolInfo query);
}
