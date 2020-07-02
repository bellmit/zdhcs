/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.service;


import net.northking.atp.db.persistent.ReProfileInfo;

import java.util.List;

public interface ReProfileInfoServiceEx
{
// ----      The End by Generator     ----//
    /**
     * 查询非本ID对应数据除外的数据
     * @param var1
     * @return
     */
   public  List<ReProfileInfo> queryForUpdate(ReProfileInfo var1);
}
