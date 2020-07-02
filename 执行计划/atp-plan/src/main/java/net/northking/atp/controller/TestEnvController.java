package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReBrowserInfo;
import net.northking.atp.db.persistent.ReOsInfo;
import net.northking.atp.db.persistent.ReTestEnvInfo;
import net.northking.atp.db.service.ReBrowserInfoService;
import net.northking.atp.db.service.ReOsInfoService;
import net.northking.atp.db.service.ReTestEnvInfoService;
import net.northking.atp.entity.TestEnvInfo;
import net.northking.atp.enums.PlanClass;
import net.northking.atp.service.TestEnvService;
import net.northking.atp.utils.UUIDUtil;
import net.northking.db.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@Api(tags = {"测试执行计划"}, description = "测试执行计划_测试环境信息维护")
@RequestMapping(value = "/testenv")
public class TestEnvController {

    private static final Logger logger = LoggerFactory.getLogger(TestEnvController.class);

    @Autowired
    private ReTestEnvInfoService reTestEnvInfoService;

    @Autowired
    private TestEnvService testEnvService;

    @Autowired
    private ReOsInfoService reOsInfoService;

    @Autowired
    private ReBrowserInfoService reBrowserInfoService;

    @ApiOperation(value = "测试环境 添加系统信息", notes = "测试环境 添加系统信息")
    @RequestMapping(value = "/addOsInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional
    public ResultWrapper addOsInfo(@RequestBody ReOsInfo target) {
        // 校验
        if (target.getOsName() == null || "".equals(target.getOsName())) {
            return new ResultWrapper().fail("OS_INFO_00001", "请填写系统名称");
        }
        ReOsInfo osQuery = new ReOsInfo();
        osQuery.setOsName(target.getOsName());  // 系统名称
        osQuery.setOsVersion(target.getOsVersion());    // 系统版本
        List<ReOsInfo> resultList = reOsInfoService.query(osQuery);
        if (resultList != null && resultList.size() > 0) {
            return new ResultWrapper().fail("OS_INFO_00002", "系统名称信息重复，请修改");
        }
        target.setId(UUIDUtil.getUUIDWithoutDash());
        target.setUpdateTime(new Date());
        target.setCreateTime(new Date());
        int insert = reOsInfoService.insert(target);
        if (insert > 0) {
            return new ResultWrapper<ReOsInfo>().success(reOsInfoService.findByPrimaryKey(target.getId()));
        }
        return new ResultWrapper().fail("OS_INFO_00003", "添加系统信息失败");
    }

    @ApiOperation(value = "测试环境 添加浏览器信息", notes = "测试环境 添加浏览器信息")
    @RequestMapping(value = "/addBwInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional
    public ResultWrapper addBwInfo(@RequestBody ReBrowserInfo target) {
        // 校验
        if (target.getBwName() == null || "".equals(target.getBwName())) {
            return new ResultWrapper().fail("BW_INFO_00001", "请填写浏览器名称");
        }
        ReBrowserInfo bwQuery = new ReBrowserInfo();
        bwQuery.setBwName(target.getBwName());
        bwQuery.setBwVersion(target.getBwVersion());
        List<ReBrowserInfo> resultList = reBrowserInfoService.query(bwQuery);
        if (resultList != null && resultList.size() > 0) {
            return new ResultWrapper().fail("BW_INFO_00002", "浏览器信息重复，请修改");
        }
        target.setId(UUIDUtil.getUUIDWithoutDash());
        target.setCreateTime(new Date());
        int insert = reBrowserInfoService.insert(target);
        if (insert > 0) {
            return new ResultWrapper<ReBrowserInfo>().success(reBrowserInfoService.findByPrimaryKey(target.getId()));
        }
        return new ResultWrapper().fail("OS_INFO_00003", "添加系统信息失败");
    }

    /**
     * 新增测试环境
     *
     * @param target
     * @return
     */
    @ApiOperation(value = "测试环境 新增", notes = "测试环境 新增")
    @RequestMapping(value = "/addTestEnv", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional
    public ResultWrapper addTestEnv(@RequestBody ReTestEnvInfo target) {
        logger.info("新增测试环境的参数：{}", target);
        if (target.getTestEnvName() == null || "".equals(target.getTestEnvName())) {
            return new ResultWrapper().fail("TEST_ENV_00001", "缺少测试环境名称，请修改");
        }
        ReTestEnvInfo envQuery = new ReTestEnvInfo();
        envQuery.setTestEnvName(target.getTestEnvName());
        List<ReTestEnvInfo> nameList = reTestEnvInfoService.query(envQuery);
        if (nameList != null && nameList.size() > 0) {
            return new ResultWrapper().fail("TEST_ENV_00002", "发现重复的测试环境名称，请修改");
        }
        // 校验
        if (target.getBwInfoId() == null || "".equals(target.getBwInfoId())
                || target.getOsInfoId() == null || "".equals(target.getOsInfoId())) {
            return new ResultWrapper().fail("TEST_ENV_00001", "缺少操作系统或者浏览器信息，请修改");
        }
        envQuery = new ReTestEnvInfo();
        envQuery.setOsInfoId(target.getOsInfoId());
        envQuery.setBwInfoId(target.getBwInfoId());
        List<ReTestEnvInfo> resultList = reTestEnvInfoService.query(envQuery);
        if (resultList != null && resultList.size() > 0) {
            return new ResultWrapper().fail("TEST_ENV_00002", "当前操作系统和浏览器信息的测试环境已存在");
        }
        target.setId(UUIDUtil.getUUIDWithoutDash());
        target.setCreateTime(new Date());
        target.setUpdateTime(new Date());
        int addCount = reTestEnvInfoService.insert(target);
        if (addCount > 0) {
            ReTestEnvInfo envInfo = reTestEnvInfoService.findByPrimaryKey(target.getId());
            TestEnvInfo testEnvInfo = testEnvService.getTestEnvInfo(envInfo);
            return new ResultWrapper<ReTestEnvInfo>().success(testEnvInfo);
        } else {
            return new ResultWrapper<>().fail("TEST_ENV_00001", "添加失败，请检查参数是否正确");
        }
    }


    @ApiOperation(value = "测试环境 查询环境详情", notes = "测试环境 查询环境详情")
    @RequestMapping(value = "/queryTestEnvById/{envId}", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper queryTestEnvById(@PathVariable("envId") String envId) {
        logger.info("需要进行查询测试环境的ID ======> {}", envId);
        if (envId == null || "".equals(envId)) {
            return new ResultWrapper().fail("TEST_ENV_00002", "参数不能为空，请检查参数是否正确");
        }
        ReTestEnvInfo envInfo = reTestEnvInfoService.findByPrimaryKey(envId);
        if (envInfo == null) {
            return new ResultWrapper().fail("TEST_EVN_00003", "找不到相关记录，当前查询的测试环境可能已被删除");
        }
        TestEnvInfo testEnvInfo = testEnvService.getTestEnvInfo(envInfo);
        return new ResultWrapper<TestEnvInfo>().success(testEnvInfo);
    }

    @ApiOperation(value = "测试环境 分页查询环境信息", notes = "测试环境 分页查询环境信息")
    @RequestMapping(value = "/queryTestEnvByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper queryTestEnvList(@RequestBody QueryByPage<ReTestEnvInfo> query) {

        // 检查参数
        if (query.getPageNo() == -1) {
            return new ResultWrapper().fail("TEST_EVN_00005", "缺少pageNo参数");
        }
        if (query.getPageSize() == -1) {
            return new ResultWrapper().fail("TEST_EVN_00005", "缺少pageSize参数");
        }
        if (query.getQuery() == null || query.getQuery().getProjectId() == null || "".equals(query.getQuery().getProjectId())) {
            return new ResultWrapper().fail("TEST_EVN_00005", "缺少项目编号id参数");
        }
        // 环境检查---------------start
        envCheck(query);
        // 环境检查---------------end

        Pagination<TestEnvInfo> page = testEnvService.queryTestEnvList(query);
        if (page == null) {
            return new ResultWrapper().fail("TEST_ENV_00004", "没有可查询的环境信息列表");
        }
        return new ResultWrapper<Pagination<TestEnvInfo>>().success(page);
    }

    /**
     * 环境检查
     *
     * @param query
     */
    private void envCheck(QueryByPage<ReTestEnvInfo> query) {
//        ReTestEnvInfo envInfoQuery = new ReTestEnvInfo();
//        envInfoQuery.setProjectId(query.getQuery().getProjectId());
        // 首先检查当前项目有否测试环境
//        List<ReTestEnvInfo> envInfoList = reTestEnvInfoService.query(envInfoQuery);
        // 检查当前环境是否有默认测试环境：web/ui 和 移动测试
        // 1. web/ui
        testEnvService.checkAndAddDefaultEnv(query.getQuery().getProjectId(), "windows", "10", "Internet Explorer", "11", PlanClass.WEB_UI);
        // 2.检查默认移动测试环境
        testEnvService.checkAndAddDefaultEnv(query.getQuery().getProjectId(), "android", "9.0", "Mobile Application", null, PlanClass.MOBILE);
    }


    @ApiModelProperty(value = "测试环境 查询移动端操作系统", notes = "测试环境 查询移动端操作系统")
    @RequestMapping(value = "/queryMobileOS", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper queryMobileOS(@RequestBody TestEnvInfo testEnvInfo) {
        if (testEnvInfo.getProjectId() == null || "".equals(testEnvInfo.getProjectId())) {
            return new ResultWrapper().fail("TEC00001", "缺少项目ID【projectId】");
        }
        if (testEnvInfo.getReOsInfo() == null || testEnvInfo.getReOsInfo().getOsType() == null || "".equals(testEnvInfo.getReOsInfo().getOsType())) {
            return new ResultWrapper().fail("TEC00001", "缺少移动操作系统类型【osType】");
        }
        List<TestEnvInfo> envInfos = testEnvService.queryEnvInfoByOsInfo(testEnvInfo);
        return new ResultWrapper<List<TestEnvInfo>>().success(envInfos);
    }

}
