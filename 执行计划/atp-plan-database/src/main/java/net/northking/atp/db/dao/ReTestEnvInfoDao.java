/*
* Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
*
*/
package net.northking.atp.db.dao;

import net.northking.atp.db.persistent.ReTestEnvInfo;
import net.northking.atp.db.mapper.ReTestEnvInfoMapper;
import net.northking.atp.entity.TestEnvInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 测试环境信息表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-08-05 16:56:26  <br>
 * @author:  database-mybatis-maven-plugin  <br>
 * @since:  1.0 <br>
 */
public interface ReTestEnvInfoDao extends ReTestEnvInfoMapper
{
    // ----      The End by Generator     ----//

    List<ReTestEnvInfo> queryEnvInfoByOsInfo(@Param("env") TestEnvInfo testEnvInfo);

}
