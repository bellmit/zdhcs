/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.dao;

import net.northking.atp.db.persistent.ReDataPoolInfo;
import net.northking.atp.db.persistent.ReProfileInfo;
import net.northking.atp.db.mapper.ReProfileInfoMapper;

import java.util.List;


/**
 * 描述环境的基础信息
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-05-10 10:20:33  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
public interface ReProfileInfoDao extends ReProfileInfoMapper
{
  // ----      The End by Generator     ----//
    /**
     * 查询非本ID对应数据除外的数据
     * @param var1
     * @return
     */
    public List<ReProfileInfo> queryForUpdate(ReProfileInfo var1);
}
