/*
 * Copyright (c) 京北方信息技术股份有限公司 Corporation 2019 . All rights reserved.
 *
 */
package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReTestEnvInfoSrvAdapter;
import net.northking.atp.db.dao.ReTestEnvInfoDao;
import net.northking.atp.db.persistent.ReBrowserInfo;
import net.northking.atp.db.persistent.ReOsInfo;
import net.northking.atp.db.service.ReBrowserInfoService;
import net.northking.atp.db.service.ReOsInfoService;
import net.northking.atp.db.service.ReTestEnvInfoService;
import net.northking.atp.db.persistent.ReTestEnvInfo;

import net.northking.atp.entity.TestEnvInfo;
import net.northking.db.BasicDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 测试环境信息表
 *
 * <p>文件内容由代码生成器产生，请不要手动修改！ <br>
 * createdate:  2019-08-05 16:56:26  <br>
 *
 * @author: database-mybatis-maven-plugin  <br>
 * @since: 1.0 <br>
 */
@Service
public class ReTestEnvInfoServiceImpl extends ReTestEnvInfoSrvAdapter implements ReTestEnvInfoService {
    private static final Logger logger = LoggerFactory.getLogger(ReTestEnvInfoServiceImpl.class);

    @Autowired
    private ReTestEnvInfoDao reTestEnvInfoDao;

    protected BasicDao<ReTestEnvInfo> getDao() {
        return reTestEnvInfoDao;
    }
// ----      The End by Generator     ----//

    @Autowired
    private ReOsInfoService reOsInfoService;

    @Autowired
    private ReBrowserInfoService reBrowserInfoService;

    @Override
    public TestEnvInfo setTestEnvInfoDetails(ReTestEnvInfo envInfo) {
        ReOsInfo osInfo = reOsInfoService.findByPrimaryKey(envInfo.getOsInfoId());
        ReBrowserInfo bwInfo = reBrowserInfoService.findByPrimaryKey(envInfo.getBwInfoId());
        TestEnvInfo testEnvInfo = new TestEnvInfo();
        BeanUtils.copyProperties(envInfo, testEnvInfo);
        testEnvInfo.setReOsInfo(osInfo);
        testEnvInfo.setReBrowserInfo(bwInfo);
        return testEnvInfo;
    }

    /**
     * 根据环境id查询环境的 详细信息
     * @param envInfoId
     * @return
     */
    @Override
    public TestEnvInfo findTestEnvInfoById(String envInfoId) {
        ReTestEnvInfo resultEnv = reTestEnvInfoDao.findByPrimaryKey(envInfoId);
        TestEnvInfo testEnvInfo = setTestEnvInfoDetails(resultEnv);
        return testEnvInfo;
    }


}

