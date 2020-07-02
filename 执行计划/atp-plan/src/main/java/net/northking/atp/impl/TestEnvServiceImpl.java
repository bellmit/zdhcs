package net.northking.atp.impl;

import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.db.dao.ReTestEnvInfoDao;
import net.northking.atp.db.persistent.ReBrowserInfo;
import net.northking.atp.db.persistent.ReOsInfo;
import net.northking.atp.db.persistent.ReTestEnvInfo;
import net.northking.atp.db.service.ReBrowserInfoService;
import net.northking.atp.db.service.ReOsInfoService;
import net.northking.atp.db.service.ReTestEnvInfoService;
import net.northking.atp.entity.TestEnvInfo;
import net.northking.atp.enums.DefaultEnv;
import net.northking.atp.enums.PlanClass;
import net.northking.atp.service.TestEnvService;
import net.northking.atp.utils.UUIDUtil;
import net.northking.db.DefaultPagination;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TestEnvServiceImpl implements TestEnvService {

    private static final Logger logger = LoggerFactory.getLogger(TestEnvServiceImpl.class);

    @Autowired
    private ReTestEnvInfoService reTestEnvInfoService;

    @Autowired
    private ReBrowserInfoService reBrowserInfoService;

    @Autowired
    private ReOsInfoService reOsInfoService;

    @Autowired
    private ReTestEnvInfoDao reTestEnvInfoDao;

    @Override
    public Pagination<TestEnvInfo> queryTestEnvList(QueryByPage<ReTestEnvInfo> query) {
        OrderBy orderBy = new SqlOrderBy();
        if (query.getOrderByList() != null && query.getOrderByList().size() > 0) {
            for (QueryOrderBy qob : query.getOrderByList()) {
                orderBy.addOrderBy(qob.getColumn(), qob.getDir());
            }
        }
        Pagination<ReTestEnvInfo> resultPage = reTestEnvInfoService.query(query.getQuery(), orderBy, query.getPageNo(), query.getPageSize());
        // 创建返回对象Pagination<TestEnvInfo>
        Pagination<TestEnvInfo> page = new DefaultPagination<>();
        if (resultPage != null && resultPage.getRecords() != null) {
            page.setPageNo(resultPage.getPageNo());
            page.setPageSize(resultPage.getPageSize());
            page.setRecordCount(resultPage.getRecordCount());
            List<ReTestEnvInfo> records = resultPage.getRecords();
            ArrayList<TestEnvInfo> infos = new ArrayList<>();
            for (ReTestEnvInfo record : records) {
                TestEnvInfo testEnvInfo = new TestEnvInfo();
                // 赋值属性值
                BeanUtils.copyProperties(record, testEnvInfo);
                // 查询关联的操作系统信息
                ReOsInfo osInfo = reOsInfoService.findByPrimaryKey(record.getOsInfoId());
                testEnvInfo.setReOsInfo(osInfo);
                // 查询关联的浏览器信息
                ReBrowserInfo bwInfo = reBrowserInfoService.findByPrimaryKey(record.getBwInfoId());
                testEnvInfo.setReBrowserInfo(bwInfo);
                infos.add(testEnvInfo);
            }
            page.setRecords(infos);
        }
        return page;
    }

    @Override
    public TestEnvInfo getTestEnvInfo(ReTestEnvInfo envInfo) {
        TestEnvInfo testEnvInfo = new TestEnvInfo();
        BeanUtils.copyProperties(envInfo, testEnvInfo);
        ReOsInfo osInfo = reOsInfoService.findByPrimaryKey(envInfo.getOsInfoId());
        ReBrowserInfo bwInfo = reBrowserInfoService.findByPrimaryKey(envInfo.getBwInfoId());
        if (osInfo != null && bwInfo != null) {
            testEnvInfo.setReOsInfo(osInfo);
            testEnvInfo.setReBrowserInfo(bwInfo);
        }
        return testEnvInfo;
    }

    /**
     * 检查并添加默认的测试环境
     * @param projectId         接口请求参数对象
     * @param osType        操作系统类型
     * @param osVersion     操作系统版本
     * @param bwType        浏览器类型
     * @param bwVersion     浏览器版本
     * @param envType       环境类型
     */
    @Override
    @Transactional
    public ReTestEnvInfo checkAndAddDefaultEnv(String projectId,
                                      String osType, String osVersion,
                                      String bwType, String bwVersion, PlanClass envType) {
        ReTestEnvInfo envInfoQuery = new ReTestEnvInfo();
        envInfoQuery.setEnvType(envType.code());
        envInfoQuery.setDefaultEnv(DefaultEnv.DEFAULT.code());
        envInfoQuery.setProjectId(projectId);
        List<ReTestEnvInfo> defaultEnvList = reTestEnvInfoService.query(envInfoQuery);
        ReTestEnvInfo defaultEnvInfo = new ReTestEnvInfo();
        if (defaultEnvList.size() == 0) {
            ReOsInfo osQuery = new ReOsInfo();
            osQuery.setOsType(osType);
            osQuery.setOsVersion(osVersion);
            List<ReOsInfo> osList = reOsInfoService.query(osQuery); // 查询系统
            ReBrowserInfo bwQuery = new ReBrowserInfo();
            bwQuery.setBwType(bwType);
            if (bwVersion != null) {
                bwQuery.setBwVersion(bwVersion);
            }
            List<ReBrowserInfo> bwList = reBrowserInfoService.query(bwQuery);   // 查询浏览器
            if (osList.size() > 0 && bwList.size() > 0) {
                ReOsInfo osInfo = osList.get(0);    // 获取第一个系统
                ReBrowserInfo bwInfo = bwList.get(0);   // 获取第一个浏览器
                defaultEnvInfo.setOsInfoId(osInfo.getId());
                defaultEnvInfo.setBwInfoId(bwInfo.getId());
                if (envType.code() == PlanClass.WEB_UI.code()) {
                    defaultEnvInfo.setTestEnvName(osInfo.getOsName() + "-" + bwInfo.getBwName() + "(系统默认)");
                } else {
                    defaultEnvInfo.setTestEnvName(osInfo.getOsName() + "(系统默认)");
                }
                defaultEnvInfo.setEnvType(envType.code());
                defaultEnvInfo.setDefaultEnv(DefaultEnv.DEFAULT.code());
                defaultEnvInfo.setProjectId(projectId);
                defaultEnvInfo.setCreateTime(new Date());
                defaultEnvInfo.setUpdateTime(new Date());
                defaultEnvInfo.setEnvDesc("系统自动生成默认的测试" + envType.msg() + "环境");
                defaultEnvInfo.setId(UUIDUtil.getUUIDWithoutDash());
                reTestEnvInfoService.insert(defaultEnvInfo);
            }
        } else {
            defaultEnvInfo = defaultEnvList.get(0);
        }
        return defaultEnvInfo;
    }

    @Override
    public List<TestEnvInfo> queryEnvInfoByOsInfo(TestEnvInfo testEnvInfo) {
        List<ReTestEnvInfo> resultList = reTestEnvInfoDao.queryEnvInfoByOsInfo(testEnvInfo);
        ArrayList<TestEnvInfo> envInfos = new ArrayList<>();
        if (resultList != null && resultList.size() > 0) {
            for (ReTestEnvInfo reTestEnvInfo : resultList) {
                envInfos.add(getTestEnvInfo(reTestEnvInfo));
            }
        }
        return envInfos;
    }
}
