package net.northking.cloudtest.service;

import net.northking.cloudtest.common.Page;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.domain.testBug.CltTestBug;
import net.northking.cloudtest.dto.analyse.ProjectModule;
import net.northking.cloudtest.dto.testBug.TestBugDTO;
import net.northking.cloudtest.dto.testBug.TestBugListDTO;
import net.northking.cloudtest.query.testBug.CltBugLogQuery;
import net.northking.cloudtest.query.testBug.TestBugQuery;
import net.northking.cloudtest.query.user.UserAndLoginQuery;

import java.util.List;

/**
 * @Title:
 * @Description: 缺陷业务逻辑层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-03-06 15:21
 * @UpdateUser:
 * @Version:0.1
 */
public interface TestBugService {

    //根据ExecuteIds查询缺陷列表
    Page<TestBugListDTO> queryTestBugByListExecuteIds(TestBugQuery testBugQuery)throws Exception;

    //根据stepId查询缺陷列表
    Page<TestBugListDTO> queryTestBugByListStepId(TestBugQuery testBugQuery)throws Exception;

    //创建缺陷
    CltTestBug addCltTestBugInfo(TestBugQuery testBugQuery)throws Exception;

    //修改缺陷/修改缺陷状态
    Integer updateCltTestBugInfoByBugId(TestBugQuery testBugQuery)throws Exception;

    //查询缺陷列表/条件查询
    Page<TestBugDTO> queryAllCltTestBugList(TestBugQuery testBugQuery)throws Exception;


    //查询缺陷详情
    TestBugDTO queryCltTestBugByBugId(TestBugQuery testBugQuery)throws Exception;

    //查询项目下的所有的创建人员
      List<TestBugDTO> queryAllCreateUsersByProId(TestBugQuery testBugQuery)throws Exception;

    //添加信息记录表

    CltBugLog addCltBugLogInfo(TestBugQuery testBugQuery)throws Exception;

    //添加项目信息记录表

    CltBugLog addCltProjectLogInfo(CltBugLogQuery cltBugLogQuery)throws Exception;

    //查询项目信息记录
    List<CltBugLog> queryCltBugLogList(CltBugLogQuery cltBugLogQuery)throws Exception;

    //查询遗留A,B类缺陷
    int countBugAOrB(TestBugQuery testBugQuery)throws Exception;

    //查询遗留C,D,E类缺陷
    int countBugOthers(TestBugQuery testBugQuery)throws Exception;

    //删除项目信息记录表
    int deleteCltProjectLogInfo(CltBugLogQuery cltBugLogQuery)throws Exception;

    //查询所有项目下的所欲运营端同步过来的缺陷的分配人员
    List<TestBugDTO> queryAllReceiverUsersByProId(TestBugQuery testBugQuery)throws Exception;

    /**
     * 查询缺陷发现人 all
     * @return
     */
    Page<TestBugDTO> queryAllReceiverUsers(TestBugQuery testBugQuery);

    /**
     * 缺陷
     * @return
     */
    Page<TestBugDTO> queryAllCreateUsers(TestBugQuery testBugQuery);

    /**
     * 查询缺陷库的所有列表
     * @param testBugQuery
     * @return
     */
    Page<TestBugDTO> queryAllCltTestBugListForAllPro(TestBugQuery testBugQuery);

    /**
     * 查询所有模块
     * @param testBugQuery
     * @return
     */
    Page<ProjectModule> queryAllCaseList(TestBugQuery testBugQuery);

    /**
     * 删除项目下的缺陷
     * @param testBugQuery
     * @return
     */
    Integer deleteBugByProIds(TestBugQuery testBugQuery);
}
