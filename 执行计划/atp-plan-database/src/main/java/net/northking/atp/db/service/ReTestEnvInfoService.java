/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.service;

import net.northking.atp.entity.TestEnvInfo;
import net.northking.db.BasicService;
import net.northking.atp.db.persistent.ReTestEnvInfo;

public interface ReTestEnvInfoService extends BasicService<ReTestEnvInfo>
{
// ----      The End by Generator     ----//

    TestEnvInfo setTestEnvInfoDetails(ReTestEnvInfo envInfo);

    TestEnvInfo findTestEnvInfoById(String envInfoId);
}
