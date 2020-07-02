package net.northking.cloudtest.service.impl;

import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.cloudtest.assist.RedisUtil;
import net.northking.cloudtest.common.Direction;
import net.northking.cloudtest.common.Page;
import net.northking.cloudtest.common.SortOrder;
import net.northking.cloudtest.constants.ErrorConstants;
import net.northking.cloudtest.constants.NoticeAndToDoConstants;
import net.northking.cloudtest.dao.attach.CltAttachMapper;
import net.northking.cloudtest.dao.testBug.CltBugLogMapper;
import net.northking.cloudtest.dao.testBug.CltBugRecordMapper;
import net.northking.cloudtest.dao.testBug.CltTestBugMapper;
import net.northking.cloudtest.domain.attach.CltAttach;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectDto;
import net.northking.cloudtest.domain.project.CltProjectQuery;
import net.northking.cloudtest.domain.testBug.*;
import net.northking.cloudtest.domain.user.CltUserLogin;
import net.northking.cloudtest.dto.analyse.ProjectModule;
import net.northking.cloudtest.dto.project.ProjectIdDTO;
import net.northking.cloudtest.dto.testBug.TestBugDTO;
import net.northking.cloudtest.dto.testBug.TestBugListDTO;
import net.northking.cloudtest.enums.CltTestBugStatus;
import net.northking.cloudtest.enums.DefectCreateType;
import net.northking.cloudtest.exception.GlobalException;
import net.northking.cloudtest.feign.analyse.AppCaseTreeFeignClient;
import net.northking.cloudtest.feign.analyse.CaseDesignFeignClient;
import net.northking.cloudtest.feign.basicinfo.TodoFeignClient;
import net.northking.cloudtest.feign.project.ProjectFeignClient;
import net.northking.cloudtest.pojo.CltTodo;
import net.northking.cloudtest.query.attach.AttachQuery;
import net.northking.cloudtest.query.testBug.CltBugLogQuery;
import net.northking.cloudtest.query.testBug.TestBugQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.AttachService;
import net.northking.cloudtest.service.TestBugService;
import net.northking.cloudtest.utils.*;
import net.northking.cloudtest.utils.PageUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title:
 * @Description: 缺陷业务逻辑层实现类
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-03-06 15:23
 * @UpdateUser:
 * @Version:0.1
 */

@Service
public class TestBugServiceImpl implements TestBugService {

    //注入CltTestBugMapper
    @Autowired
    private CltTestBugMapper cltTestBugMapper;

    //注入cltBugLogMapper
    @Autowired
    private CltBugLogMapper cltBugLogMapper;

    @Autowired
    private AttachService attachService;

    @Autowired
    private CltBugRecordMapper cltBugRecordMapper;

    @Autowired
    private CltAttachMapper cltAttachMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired

    private CaseDesignFeignClient caseDesignFeignClient;

    @Autowired
    private ProjectFeignClient projectFeignClient;
    @Autowired

    private AppCaseTreeFeignClient appCaseTreeFeignClient;

    @Autowired

    private TodoFeignClient todoFeignClient;


    //日志
    private final static Logger logger = LoggerFactory.getLogger(TestBugServiceImpl.class);


    //根据executeIds查询缺陷列表
    @Override
    public Page<TestBugListDTO> queryTestBugByListExecuteIds(TestBugQuery testBugQuery) throws Exception {
        PageUtil.startPage(testBugQuery);
        List<String> executeIds = testBugQuery.getExecuteIds();
        List<TestBugListDTO> testBugListDTOS = null;
        try {
            testBugListDTOS = cltTestBugMapper.queryTestBugListByExecuteIds(executeIds);
            //组装返回前端的参数
//            testBugListDTOS = getCltTestBugList2(testBugListDTOS);
            for (TestBugListDTO dto : testBugListDTOS) {
                sendDataListResult(dto);
            }
        } catch (Exception e) {
            logger.error("queryTestBugByListExecuteIds", e);
            throw new GlobalException(ErrorConstants.QUERY_CLT_TESTBUG_LIST_ERROR_CODE, ErrorConstants.QUERY_CLT_TESTBUG_LIST_ERROR_MESSAGE);
        }
        return new Page<>(testBugListDTOS);
    }

    /**
     * @param createType
     * @param bugId
     * @param executeId
     * @param stepId
     * @return
     * @throws Exception
     */
    private List<CltAttach> queryCltBugAttaches(String createType, String bugId, String executeId, String stepId) {
        AttachQuery attachQuery = new AttachQuery();
        List<CltAttach> cltAttaches = new ArrayList<>();
        // 判断是否是手动提交的缺陷
        if (DefectCreateType.MANUAL.getCode().equals(createType)) {
            attachQuery.setBugId(attachQuery.getBugId());
            cltAttaches = attachService.queryAllCltAttachList(attachQuery);
        } else {
            try {
                attachQuery.setBugId(bugId);
                attachQuery.setExecuteId(executeId);
                attachQuery.setFuncId(stepId);
                cltAttaches = attachService.queryAllCltAttachListByExecuteIdAndStepIdAndBugId(attachQuery);
//                attachService.completeAttachPath(cltAttaches);
            } catch (Exception e) {
                logger.info("addCltAttachInfo", e);
//                throw new GlobalException(ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_CODE, ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_MESSAGE);
            }
        }
        return cltAttaches;
    }

    /**
     * 根据bugId查询缺陷操作记录表
     *
     * @param bugId 缺陷ID
     * @return
     */
    private List<CltBugLog> queryCltBugLogListByBugId(String bugId) {

//        String bugId = cltTestBug.getBugId();
        List<CltBugLog> cltBugLogs = null;
        try {
            cltBugLogs = cltBugLogMapper.queryCltBugLogListByBugId(bugId);
            for (int i = 0; i < cltBugLogs.size(); i++) {
                CltBugLog cltBugLog = cltBugLogs.get(i);
                String modifierName = getUserNameFromRedis(cltBugLog.getModifier());
                cltBugLog.setModifierName(modifierName);
            }
        } catch (Exception e) {
            logger.info("queryCltBugLogListByBugId", e);
            throw new GlobalException(ErrorConstants.UPDATE_CLT_TESTBUG_ERROR_CODE, ErrorConstants.UPFATE_CLT_TESTBUG_ERROR_MESSAGE);
        }
        return cltBugLogs;
    }

    /**
     * 根据bugId查询缺陷的历史的操作记录表
     *
     * @param bugId 缺陷ID
     * @return
     */
    private List<CltBugRecord> queryCltBugRecordList(String bugId) {
        List<CltBugRecord> cltBugRecords = null;
        try {
            cltBugRecords = cltBugRecordMapper.queryCltBugRecordList(bugId);
            for (int i = 0; i < cltBugRecords.size(); i++) {
                CltBugRecord cltBugRecord = cltBugRecords.get(i);
                String modifierName = getUserNameFromRedis(cltBugRecord.getModifier());
                cltBugRecord.setModifierName(modifierName);
            }
        } catch (Exception e) {
            logger.info("queryCltBugRecordList", e);
            throw new GlobalException(ErrorConstants.QUERY_CLT_TEST_BUG_RECORD_LIST_BY_BUG_ID_ERROR_CODE, ErrorConstants.QUERY_CLT_TEST_BUG_RECORD_LIST_BY_BUG_ID_ERROR_MESSAGE);
        }
        return cltBugRecords;
    }

    /**
     * 从redis获取用户名称
     *
     * @param userId 用户id
     * @return
     */
    private String getUserNameFromRedis(String userId) {
        return (String) redisUtil.get("username:" + userId);
    }

    /**
     * 根据stepId查询缺陷列表
     *
     * @param testBugQuery
     * @return
     */
    @Override
    public Page<TestBugListDTO> queryTestBugByListStepId(TestBugQuery testBugQuery) {

        PageUtil.startPage(testBugQuery);
        List<TestBugListDTO> testBugDTOS = null;
        try {
            testBugDTOS = cltTestBugMapper.queryTestBugListByStepId(testBugQuery.getStepId(), testBugQuery.getExecuteId());
            //组装返回前端的参数
            for (TestBugListDTO dto : testBugDTOS) {
                sendDataListResult(dto);
            }
        } catch (Exception e) {
            logger.error("queryTestBugByListStepId", e);
            throw new GlobalException(ErrorConstants.QUERY_CLT_TESTBUG_LIST_ERROR_CODE, ErrorConstants.QUERY_CLT_TESTBUG_LIST_ERROR_MESSAGE);
        }
        return new Page<>(testBugDTOS);
    }


    /**
     * 创建缺陷
     *
     * @param testBugQuery
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public CltTestBug addCltTestBugInfo(TestBugQuery testBugQuery) throws Exception {

        //添加缺陷表
        CltTestBug cltTestBug = addCltTestBug(testBugQuery);

        //缺陷表添加成功之后添加历史操作记录表
        if (cltTestBug != null) {

            //组装添加的参数
            CltBugRecord cltBugRecord = new CltBugRecord();
            //主键
            cltBugRecord.setRecordId(UUIDUtil.getUUID());
            //获取修改人
            cltBugRecord.setModifier(cltTestBug.getCreateUser());
            //获取修改时间
            cltBugRecord.setRecordTime(cltTestBug.getCreateDate());
            //缺陷Id
            cltBugRecord.setBugId(cltTestBug.getBugId());
            cltBugRecord.setRecordField("缺陷");
            cltBugRecord.setRecordValue("新建");
            //插入数据
            cltBugRecordMapper.insertSelective(cltBugRecord);
        }

        //查看注释是否为空,如果不为空,添加信息记录表
        String message = testBugQuery.getMessage();
        if (!StringUtils.isEmpty(message)) {
            testBugQuery.setLogTime(cltTestBug.getCreateDate());
            testBugQuery.setBugId(cltTestBug.getBugId());
            testBugQuery.setModifier(cltTestBug.getCreateUser());
            //添加缺陷操作记录表
            addCltBugLogInfo(testBugQuery);
        }
        return cltTestBug;
    }


    //修改缺陷/修改缺陷状态
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public Integer updateCltTestBugInfoByBugId(TestBugQuery testBugQuery) throws Exception {

        Integer updateNum = 1;
        // 查询当前数据库中的缺陷数据
        CltTestBug temp = cltTestBugMapper.selectByPrimaryKey(testBugQuery.getBugId());
        //添加历史操作记录表
        Integer addCltBugRecordNum = addCltBugRecord(testBugQuery);
        //更新缺陷信息表
        CltTestBug cltTestBug = updateCltTestBugInfo(testBugQuery);

        //添加信息记录表
        if (cltTestBug != null) {

            String message = testBugQuery.getMessage();
            //如果注释不为空就往信息表里面插入一条数据
            if (!StringUtils.isEmpty(message) && !message.equals(temp.getMessage())) {
                updateNum++;
                testBugQuery.setModifier(cltTestBug.getUpdateUser());
                testBugQuery.setLogTime(cltTestBug.getUpdateDate());
                testBugQuery.setBugId(cltTestBug.getBugId());
                addCltBugLogInfo(testBugQuery);
            }
        }
        sendTodoInfo(temp, testBugQuery);

        return updateNum + addCltBugRecordNum;


    }

    /**
     *
     * 发送待办信息
     *
     * @param source
     * @param query
     */
    private void sendTodoInfo(CltTestBug source, TestBugQuery query) {
        logger.info("sendTodoInfo start ------------>");
        if (query.getStatus() != null && !source.getStatus().equals(query.getStatus())) {
            // 缺陷状态改变
            CltTodo todo = new CltTodo();
            todo.setUserId(query.getReceiver());
            todo.setTodoType(new Integer(NoticeAndToDoConstants.ToDoTypeCode.BUG_MANAGE.code()).shortValue());
            todo.setTodoName("缺陷管理" + "-" + source.getBugOrder() + "-" + "缺陷状态已更改");
            todo.setStartTime(new Date());
            CltProject projectInfo = getProjectInfo(source.getProId());
            todo.setProjectName(projectInfo.getProName());
            todo.setProjectCode(projectInfo.getProNo());
            todo.setProjectId(source.getProId());
            todo.setEndTime(new Date());
            todo.setAccess_token(query.getAccess_token());
            String content = String.format("%s，【%s】成功修改缺陷【%s】（%s）的状态为【%s】，指派人为【您】，请及时处理，谢谢合作！",
                    DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"),
                    redisUtil.get("username:" + query.getUpdateUser()),
                    source.getBugOrder(),
                    query.getBugTitle(),
                    CltTestBugStatus.getStatus(query.getStatus()).getMsg());
            todo.setContent(content);
            List<CltTodo> list = new ArrayList<>();
            list.add(todo);
            todoFeignClient.addTodo(list);
            logger.info("sendTodoInfo successs ------------>");
        }
        logger.info("sendTodoInfo end ------------>");
    }


    /**
     * 查询缺陷列表/条件查询
     *
     * @param testBugQuery
     * @return
     * @throws Exception
     */
    @Override
    public Page<TestBugDTO> queryAllCltTestBugList(TestBugQuery testBugQuery) throws Exception {

        testBugQuery.validate();

        PageUtil.startPage(testBugQuery);//获取分页信息


        CltTestBugExample cltTestBugExample = assemblyExample(testBugQuery); //组装请求参数


        return getTestBugPage(cltTestBugExample);
    }

    private Page<TestBugDTO> getTestBugPage(CltTestBugExample cltTestBugExample) {
        List<TestBugDTO> cltTestBugs = null;
        try {

            cltTestBugs = cltTestBugMapper.selectByDTOExample(cltTestBugExample);

            //组装返回前端的参数
//            cltTestBugs = getCltTestBugList(cltTestBugs);
            for (TestBugDTO dto : cltTestBugs) {
                sendDataResult(dto);
            }

        } catch (Exception e) {

            logger.error("queryTestBugList", e);

            throw new GlobalException(ErrorConstants.QUERY_CLT_TESTBUG_LIST_ERROR_CODE, ErrorConstants.QUERY_CLT_TESTBUG_LIST_ERROR_MESSAGE);
        }

        return new Page<>(cltTestBugs);
    }


    /**
     * 查询缺陷详情
     *
     * @param testBugQuery
     * @return
     * @throws Exception
     */
    @Override
    public TestBugDTO queryCltTestBugByBugId(TestBugQuery testBugQuery) throws Exception {

        TestBugDTO testBugDTO = null;

        try {
            //根据bugId查询
            testBugDTO = cltTestBugMapper.queryCltTestBugByBugId(testBugQuery.getBugId());
            // 填充信息
            sendDataResult(testBugDTO);

            //根据bugId查询缺陷记录表
//            List<CltBugLog> cltBugLogs = queryCltBugLogListByBugId(testBugDTO);
//
//            testBugDTO.setCltBugLogs(cltBugLogs);


            //获取缺陷对应的附件表
//            AttachQuery attachQuery = new AttachQuery();
//
//            attachQuery.setBugId(testBugQuery.getBugId());
//
//            List<CltAttach> cltAttaches = attachService.queryAllCltAttachList(attachQuery);
//
//            testBugDTO.setAttaches(cltAttaches);

        } catch (Exception e) {

            logger.info("queryCltTestBugByBugId", e);
            throw new GlobalException(ErrorConstants.QUERY_CLT_TEST_BUG_BY_BUG_ID__ERROR_CODE, ErrorConstants.QUERY_CLT_TEST_BUG_BY_BUG_ID_ERROR_MESSAGE);

        }

        return testBugDTO;
    }


    //查询项目下的所有的创建人员
    @Override
    public List<TestBugDTO> queryAllCreateUsersByProId(TestBugQuery testBugQuery) throws Exception {


        List<TestBugDTO> testBugList = null;


        try {

            testBugList = cltTestBugMapper.queryAllCreateUsersByProId(testBugQuery.getProId());


        } catch (Exception e) {

            logger.error("queryAllCreateUsersByProId", e);

            throw new GlobalException(ErrorConstants.QUERY_ALL_CREATE_USERS_BY_PRO_ID__ERROR_CODE, ErrorConstants.QUERY_ALL_CREATE_USERS_BY_PROID_ERROR_MESSAGE);
        }

        return testBugList;
    }


    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    /**
     * 添加缺陷表
     *
     * @param testBugQuery
     * @return
     * @throws Exception
     */
    public CltTestBug addCltTestBug(TestBugQuery testBugQuery) throws Exception {

        //组装缺陷参数
        CltTestBug cltTestBugInfo = getCltTestBugInfo(testBugQuery);

        try {

            //调用添加缺陷方法
            cltTestBugMapper.insertSelective(cltTestBugInfo);

        } catch (Exception e) {

            logger.error("addCltTestBug", e);

            throw new GlobalException(ErrorConstants.ADD_CLT_TESTBUG_ERROR_CODE, ErrorConstants.ADD_CLT_TESTBUG_ERROR_MESSAGE);
        }


        return cltTestBugInfo;
    }


    //组装添加缺陷参数
    public CltTestBug getCltTestBugInfo(TestBugQuery testBugQuery) throws Exception {

        CltTestBug cltTestBug = new CltTestBug();

        try {

            BeanUtil.copyProperties(testBugQuery, cltTestBug);

            //如果BugId为空.后台生成
            if (StringUtils.isEmpty(testBugQuery.getBugId())) {
                //缺陷Id
                cltTestBug.setBugId(UUIDUtil.getUUID());
            }
            //缺陷序号

            Integer orderNum = getTestBugOrderNum(testBugQuery.getProId());
            cltTestBug.setOrderNum(orderNum);
            String testBugOrderNum = getTestBugOrderNum(orderNum);
//            cltTestBug.setBugOrder("Defect-C-" + testBugOrderNum);
            StringBuffer sb = new StringBuffer();
            sb.append("Defect")
                .append("-")
                .append(testBugQuery.getCreateType())
                .append("-")
                .append(testBugOrderNum);
            cltTestBug.setBugOrder(sb.toString());

            //缺陷状态(默认为新建的状态:1)
            cltTestBug.setStatus("1");
            //缺陷发现日期
            cltTestBug.setCreateDate(new Date());

        } catch (Exception e) {

            logger.info("getCltTestBugInfo", e);
        }

        return cltTestBug;

    }


    //获取缺陷序号(用于缺陷编码)
    public Integer getTestBugOrderNum(String proId) throws Exception {

        Integer testBugNum = 0;

        try {
            //查询存在多少条缺陷
            testBugNum = cltTestBugMapper.countCltTestBugNum(proId) + 1;

        } catch (Exception e) {

            logger.info("getTestBugOrderNum", e);
        }

        return testBugNum;

    }

    //添加缺陷信息记录表
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public CltBugLog addCltBugLogInfo(TestBugQuery testBugQuery) throws Exception {

        //组装添加参数
        CltBugLog cltBugLogInfo = getCltBugLogInfo(testBugQuery);

        try {
            cltBugLogMapper.insertSelective(cltBugLogInfo);

        } catch (Exception e) {
            logger.info("addCltBugLogInfo", e);
            throw new GlobalException(ErrorConstants.ADD_CLT_BUG_LOG_ERROR_CODE, ErrorConstants.ADD_CLT_BUG_LOG_ERROR_MESSAGE);
        }

        return cltBugLogInfo;


    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public CltBugLog addCltProjectLogInfo(CltBugLogQuery cltBugLogQuery) throws Exception {
        if (StringUtils.isEmpty(cltBugLogQuery.getBugId())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "项目id不能为空");
        }
        cltBugLogQuery.setBugLogId(UUIDUtil.getUUID());
        cltBugLogQuery.setLogTime(new Date());
        CltBugLog log = new CltBugLog();
        BeanUtil.copyProperties(cltBugLogQuery, log);
        try {
            cltBugLogMapper.insertSelective(log);

        } catch (Exception e) {
            logger.info("addCltProjectLogInfo", e);
            throw new GlobalException(ErrorConstants.ADD_CLT_BUG_LOG_ERROR_CODE, "添加项目信息失败！！");
        }
        return log;
    }

    @Override
    public List<CltBugLog> queryCltBugLogList(CltBugLogQuery cltBugLogQuery) throws Exception {
        if (StringUtils.isEmpty(cltBugLogQuery.getBugId())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "项目id不能为空");
        }
        cltBugLogQuery.addSortOrderDesc("logTime");
        CltBugLogExample example = assemblyExampleLog(cltBugLogQuery);
        List<CltBugLog> list = null;
        try {
            list = cltBugLogMapper.selectByExample(example);

        } catch (Exception e) {
            logger.info("queryCltBugLogList", e);
            throw new GlobalException(ErrorConstants.ADD_CLT_BUG_LOG_ERROR_CODE, "查询项目信息失败！！");
        }
        return list;
    }

    @Override
    public int countBugAOrB(TestBugQuery testBugQuery) throws Exception {
        if (StringUtils.isEmpty(testBugQuery.getProId())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "项目id不能为空");
        }
        List<String> status = new ArrayList<>();
        status.add("9");
        status.add("10");
        CltTestBugExample example = new CltTestBugExample();
        CltTestBugExample.Criteria criteria = example.createCriteria();
        if (StringUtils.hasText(testBugQuery.getBugGrade())) {
            criteria.andBugGradeEqualTo(testBugQuery.getBugGrade());
        }
        criteria.andStatusNotIn(status);
        criteria.andProIdEqualTo(testBugQuery.getProId());
        int index = cltTestBugMapper.countByExample(example);
        return index;
    }

    @Override
    public int countBugOthers(TestBugQuery testBugQuery) throws Exception {
        if (StringUtils.isEmpty(testBugQuery.getProId())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "项目id不能为空");
        }
        List<String> status = new ArrayList<>();
        status.add("9");
        status.add("10");
        List<String> grade = new ArrayList<>();
        grade.add("A");
        grade.add("B");
        CltTestBugExample example = new CltTestBugExample();
        CltTestBugExample.Criteria criteria = example.createCriteria();
        criteria.andBugGradeNotIn(grade);
        criteria.andStatusNotIn(status);
        criteria.andProIdEqualTo(testBugQuery.getProId());
        int index = cltTestBugMapper.countByExample(example);
        return index;
    }

    /**
     * 删除记录信息
     *
     * @param cltBugLogQuery
     * @return
     * @throws Exception
     */
    @Override
    public int deleteCltProjectLogInfo(CltBugLogQuery cltBugLogQuery) throws Exception {
        int index = 0;
        if (StringUtils.isEmpty(cltBugLogQuery.getBugLogId())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "风险信息id不能为空");
        }
        try {
            index = cltBugLogMapper.deleteByPrimaryKey(cltBugLogQuery.getBugLogId());
        } catch (Exception e) {
            logger.info("deleteCltProjectLogInfo", e);
            throw new GlobalException(ErrorConstants.ADD_CLT_BUG_LOG_ERROR_CODE, "删除项目风险信息失败！！");
        }
        return index;
    }


    //查询所有项目下的所欲运营端同步过来的缺陷的分配人员
    @Override
    public List<TestBugDTO> queryAllReceiverUsersByProId(TestBugQuery testBugQuery) throws Exception {

        List<TestBugDTO> testBugList = null;

        try {

            testBugList = cltTestBugMapper.queryAllReceiverUsersByProId(testBugQuery.getProId());


        } catch (Exception e) {

            logger.error("queryAllReceiverUsersByProId", e);

            throw new GlobalException(ErrorConstants.QUERY_ALL_RECEIVER_USERS_BY_PRO_ID__ERROR_CODE, ErrorConstants.QUERY_ALL_RECEIVER_USERS_BY_PROID_ERROR_MESSAGE);
        }

        return testBugList;
    }

    @Override
    public Page<TestBugDTO> queryAllReceiverUsers(TestBugQuery testBugQuery) {
        PageUtil.startPage(testBugQuery);
        //项目id转换
        setProIds(testBugQuery);
        List<TestBugDTO> testBugDTOS = cltTestBugMapper.queryAllReceiverUsersInProIds(testBugQuery);
        return new Page<>(testBugDTOS);
    }

    private void setProIds(TestBugQuery testBugQuery) {
        if(testBugQuery.getQuery()!=null && testBugQuery.getQuery().getProId() !=null){
            List<String> ids = FormatStringUtil.formatStringToList(testBugQuery.getQuery().getProId());
            testBugQuery.getQuery().setProIds(ids);
        }
    }

    @Override
    public Page<TestBugDTO> queryAllCreateUsers(TestBugQuery testBugQuery) {
        PageUtil.startPage(testBugQuery);
        //项目id转换
        setProIds(testBugQuery);
        List<TestBugDTO> testBugDTOS = cltTestBugMapper.queryAllCreateUsersInProIds(testBugQuery);
        return new Page<>(testBugDTOS);
    }

    @Override
    public Page<TestBugDTO> queryAllCltTestBugListForAllPro(TestBugQuery testBugQuery) {
        testBugQuery.validate();

        PageUtil.startPage(testBugQuery);
        if(testBugQuery.getProId()!=null){
            List<String> proIds = FormatStringUtil.formatStringToList(testBugQuery.getProId());
            List<String> parentIdList = changeStringToList(testBugQuery.getProId());
            if(CollectionUtils.isNotEmpty(parentIdList)){
                //查询父项目的子项目
                ProjectIdDTO dto = new ProjectIdDTO();
                dto.setProIds(parentIdList);
                ResultInfo<ProjectIdDTO> info = projectFeignClient.queryProjectIdList(dto);
                if(info != null && info.getData()!= null){
                    proIds.addAll(info.getData().getProIds());
                }
            }
            testBugQuery.setProIds(proIds);
        }
        //查询对应项目下跟模块 的搜索条件
        if(CollectionUtils.isNotEmpty(testBugQuery.getModuleAndPro())){
            List<String> modules = new ArrayList<>();
            List<String> proIds = new ArrayList<>();
            testBugQuery.getModuleAndPro().forEach(query->{
                proIds.add(query.getProId());
                modules.addAll(query.getModules());
            });
            testBugQuery.setModules(modules);
            if(testBugQuery.getProIds() == null){
                testBugQuery.setProIds(proIds);
            }
        }
        CltTestBugExample cltTestBugExample = assemblyExampleForType(testBugQuery,false);

        return getTestBugPage(cltTestBugExample);
    }

    private List<String> changeStringToList(String proId) {
        ArrayList<String> parentId = new ArrayList<>();
        String str = FormatStringUtil.replaceAll(proId, "", "[");
        String[] split = str.split("]");
        for (String s : split) {
            if(!StringUtils.isEmpty(s.trim())){
                String[] split1 = s.split(",");
                if(split1.length == 1){
                    parentId.add(split1[0]);
                }

            }
        }
        return parentId;

    }

    @Override
    public Page<ProjectModule> queryAllCaseList(TestBugQuery testBugQuery) {
        List<String> ids = testBugQuery.getQuery() != null && testBugQuery.getQuery().getProId() !=null ? FormatStringUtil.formatStringToList(testBugQuery.getQuery().getProId())
                : cltTestBugMapper.queryAllProIds() ;
        testBugQuery.setProIds(ids);
        if(testBugQuery.getQuery() !=null && testBugQuery.getQuery().getModule()!= null){
            testBugQuery.setModule(testBugQuery.getQuery().getModule());
        }
        ResultInfo<Page<ProjectModule>> listResultInfo = appCaseTreeFeignClient.queryModuleOnCondition(testBugQuery);

        return listResultInfo.getData();
    }

    @Override
    @Transactional(rollbackFor = Exception.class,timeout = 36000)
    public Integer deleteBugByProIds(TestBugQuery testBugQuery) {
        AssertUtil.getInstance().isNotNull(testBugQuery.getProIds(),"项目id集合不能为空");
        CltTestBugExample example = new CltTestBugExample();
        CltTestBugExample.Criteria criteria = example.createCriteria();
        criteria.andProIdIn(testBugQuery.getProIds());
        int i = cltTestBugMapper.deleteByExample(example);
        return i;
    }


    //组装缺陷信息记录表数据

    public CltBugLog getCltBugLogInfo(TestBugQuery testBugQuery) throws Exception {

        CltBugLog cltBugLog = new CltBugLog();

        try {
            //操作记录表Id
            cltBugLog.setBugLogId(UUIDUtil.getUUID());
            //缺陷Id
            cltBugLog.setBugId(testBugQuery.getBugId());
            //修改人
            cltBugLog.setModifier(testBugQuery.getModifier());

            //修改时间
            cltBugLog.setLogTime(testBugQuery.getLogTime());

            //留言
            String message = testBugQuery.getMessage();

            if (!StringUtils.isEmpty(message)) {

                cltBugLog.setMessage(message);

            }
        } catch (Exception e) {
            logger.info("getCltBugLogInfo", e);

        }

        return cltBugLog;
    }


    //修改缺陷/状态
    public CltTestBug updateCltTestBugInfo(TestBugQuery testBugQuery) throws Exception {


        CltTestBug cltTestBug = new CltTestBug();

        BeanUtil.copyProperties(testBugQuery, cltTestBug);

        //修改时间
        Date updateDate = new Date();

        cltTestBug.setUpdateDate(updateDate);

        //修复时间

        String status = cltTestBug.getStatus();


        if (!StringUtils.isEmpty(status)) {

            if ("8".equals(status)) {
                cltTestBug.setHandleDate(updateDate);

                CltUserLogin cltUserLogin = (CltUserLogin) redisUtil.get(testBugQuery.getAccess_token());

                //设置缺陷修复人员人员
                if (cltUserLogin != null) {

                    cltTestBug.setHandleUser(cltUserLogin.getUserId());
                }


            }

            //关闭时间

            if ("9".equals(status)) {
                cltTestBug.setCloseDate(updateDate);

            }

        }


        try {
            cltTestBugMapper.updateByPrimaryKeySelective(cltTestBug);

        } catch (Exception e) {
            logger.info("updateCltTestBugInfo", e);

            throw new GlobalException(ErrorConstants.UPDATE_CLT_TESTBUG_ERROR_CODE, ErrorConstants.UPFATE_CLT_TESTBUG_ERROR_MESSAGE);
        }

        return cltTestBug;

    }


    //根据bugId查询缺陷操作记录表
    private List<CltBugLog> queryCltBugLogListByBugId(TestBugDTO cltTestBug) {

        String bugId = cltTestBug.getBugId();

        List<CltBugLog> cltBugLogs = null;

        try {
            cltBugLogs = cltBugLogMapper.queryCltBugLogListByBugId(bugId);

        } catch (Exception e) {
            logger.info("queryCltBugLogListByBugId", e);

            throw new GlobalException(ErrorConstants.UPDATE_CLT_TESTBUG_ERROR_CODE, ErrorConstants.UPFATE_CLT_TESTBUG_ERROR_MESSAGE);
        }

        return cltBugLogs;

    }

    //组装返回数据
    public TestBugDTO sendDataResult(TestBugDTO testBugDTO) {

        //缺陷ID Defect-createType-缺陷序号
        String bugCode = getBugCode(testBugDTO.getOrderNum(), testBugDTO.getCreateType());
        testBugDTO.setTestBugId(bugCode);

        // 调用查询分配给
        if (!StringUtils.isEmpty(testBugDTO.getReceiver())) {
            String receive = (String) redisUtil.get("username:" + testBugDTO.getReceiver());
            testBugDTO.setReceiverName(receive);
        }
        // 设置缺陷创建人的名称
        testBugDTO.setCreateUserName(setUserChnName(testBugDTO.getCreateUser()));
        testBugDTO.setUserChnName(setUserChnName(testBugDTO.getCreateUser()));

        // 设置用例名称
        if (!StringUtils.isEmpty(testBugDTO.getCaseId())) {
            String caseName = getTestCaseById(testBugDTO.getCaseId());
            testBugDTO.setCaseName(caseName);
        }
        // 设置计划名称
        String projectName = getProjecName(testBugDTO.getProId());
        testBugDTO.setProjectName(projectName);
        // 信息记录表
        testBugDTO.setCltBugLogs(queryCltBugLogListByBugId(testBugDTO.getBugId()));
        // 操作记录表
        testBugDTO.setCltBugRecords(queryCltBugRecordList(testBugDTO.getBugId()));
        // 获取缺陷附件
//        List<CltAttach> cltAttaches = queryCltBugAttaches(testBugDTO.getCreateType(),
//                testBugDTO.getBugId(), testBugDTO.getExecuteId(), testBugDTO.getStepId());
        AttachQuery attQuery = new AttachQuery();
        attQuery.setBugId(testBugDTO.getBugId());
        List<CltAttach> cltAttaches = attachService.queryAttachOnCondition(attQuery);
        testBugDTO.setAttaches(cltAttaches);
        return testBugDTO;
    }

    /**
     * 组装返回数据List
     *
     * @param testBugListDTO
     * @return
     * @throws Exception
     */
    private TestBugListDTO sendDataListResult(TestBugListDTO testBugListDTO) throws Exception {

        //获取缺陷编码 Defect-createType-缺陷序号
        String bugCode = getBugCode(testBugListDTO.getOrderNum(), testBugListDTO.getCreateType());
        testBugListDTO.setTestBugId(bugCode);
        //调用查询分配给
        if (!StringUtils.isEmpty(testBugListDTO.getReceiver())) {
            String receive = getUserNameFromRedis(testBugListDTO.getReceiver());
            testBugListDTO.setReceiverName(receive);
        }
        //调用查询缺陷发现人
        testBugListDTO.setCreateUserName(setUserChnName(testBugListDTO.getCreateUser()));
        //设置用例名称
        if (!StringUtils.isEmpty(testBugListDTO.getCaseId())) {
            String caseName = getTestCaseById(testBugListDTO.getCaseId());
            testBugListDTO.setCaseName(caseName);
        }
        // 获取项目名称
        String projectName = getProjecName(testBugListDTO.getProId());
        testBugListDTO.setProjectName(projectName);
        // 信息记录表
        testBugListDTO.setCltBugLogs(queryCltBugLogListByBugId(testBugListDTO.getBugId()));
        // 操作记录表
        testBugListDTO.setCltBugRecords(queryCltBugRecordList(testBugListDTO.getBugId()));
        // 附件信息
        List<CltAttach> attaches = queryCltBugAttaches(testBugListDTO.getCreateType(),
                testBugListDTO.getBugId(), testBugListDTO.getExecuteId(), testBugListDTO.getStepId());
        testBugListDTO.setAttaches(attaches);
        return testBugListDTO;
    }

    /**
     * 组装缺陷编号
     *
     * @param orderNum
     * @param createType
     * @return
     */
    private String getBugCode(Integer orderNum, String createType) {
        StringBuffer buffer = new StringBuffer();
        if (!StringUtils.isEmpty(orderNum)) {
            String testBugOrderNum = getTestBugOrderNum(orderNum);
            buffer.append("Defect")
                    .append("-")
                    .append(createType)
                    .append("-")
                    .append(testBugOrderNum);
        }
        return buffer.toString();
    }

    /**
     * 设置缺陷创建人名称
     *
     * @param userId
     */
    private String setUserChnName(String userId) {
        if (!StringUtils.isEmpty(userId)) {
            return (String) redisUtil.get("username:" + userId);
        }
        return userId;
    }

    /**
     * 服务远程调用获取用例信息
     *
     * @param caseId 用例ID
     * @return
     */
    private String getTestCaseById(String caseId) {
        ResultInfo<ReCaseDesignInfo> resulInfo = null;
        try {
            resulInfo = caseDesignFeignClient.getCaseInfo(caseId);
            if (resulInfo.isSuccess() && resulInfo.getData() != null) {
                ReCaseDesignInfo caseInfo = resulInfo.getData();
    //            TestCaseQuery query = new TestCaseQuery();
    //            BeanUtils.copyProperties(caseInfo, query);
                if (caseInfo != null) {
                    return caseInfo.getCaseName();
                }
                throw new GlobalException("CLT00000001", "无法查询到当前缺陷的关联案例");
            }
        } catch (GlobalException e) {
            throw new GlobalException(resulInfo.getCode(), resulInfo.getMessage());
        }
        return caseId;
    }

    /**
     * 获取项目名称
     *
     * @param projectId 项目ID
     * @return
     */
    private String getProjecName(String projectId) {
        return getProjectInfo(projectId).getProName();
    }

    private CltProject getProjectInfo(String projectId) {
        if (!StringUtils.isEmpty(projectId)) {
            CltProjectQuery proQuery = new CltProjectQuery();
            proQuery.setProId(projectId);
            ResultInfo<CltProject> resultInfo = projectFeignClient.queryProjectSimpleInfoById(proQuery);
            if (resultInfo.isSuccess() && resultInfo.getData() != null) {
                return resultInfo.getData();
            }
        }
        throw new GlobalException("CLT00000002", "无法获取缺陷的项目");
    }

    //组装返回前端的参数
    public List<TestBugDTO> getCltTestBugList(List<TestBugDTO> cltTestBugs) {

/*
         for (int i = 0; i < cltTestBugs.size(); i++) {

             TestBugDTO cltTestBug =  cltTestBugs.get(i);
            //根据bugId查询缺陷信息记录列表
            List<CltBugLog> cltBugLogs = queryCltBugLogListByBugId(cltTestBug);

            //根据bugId查询缺陷历史记录列表
             List<CltBugRecord> cltBugRecords = queryCltBugRecordList(cltTestBug.getBugId());

             cltTestBug.setCltBugLogs(cltBugLogs);

             cltTestBug.setCltBugRecords(cltBugRecords);
        }*/

        for (TestBugDTO dto : cltTestBugs) {
            sendDataResult(dto);
        }

        return cltTestBugs;
    }

    //组装返回前端的参数
    public List<TestBugListDTO> getCltTestBugList2(List<TestBugListDTO> cltTestBugs) throws Exception {

        for (int i = 0; i < cltTestBugs.size(); i++) {
            TestBugListDTO cltTestBug = cltTestBugs.get(i);
            String bugId = cltTestBug.getBugId();
            //根据bugId查询缺陷信息记录列表
            List<CltBugLog> cltBugLogs = queryCltBugLogListByBugId(bugId);
            //根据bugId查询缺陷历史记录列表
            List<CltBugRecord> cltBugRecords = queryCltBugRecordList(bugId);
            cltTestBug.setCltBugLogs(cltBugLogs);
            cltTestBug.setCltBugRecords(cltBugRecords);

            List<CltAttach> cltAttaches = queryCltBugAttaches(cltTestBug.getCreateType(),
                    cltTestBug.getBugId(), cltTestBug.getExecuteId(), cltTestBug.getStepId());
            cltTestBug.setAttaches(cltAttaches);

        }
        return cltTestBugs;
    }

    /**
     * 添加历史操作记录表
     *
     * @param testBugQuery
     * @return
     * @throws Exception
     */
    private Integer addCltBugRecord(TestBugQuery testBugQuery) throws Exception {

        Integer addRecordNum = 0;

        //后台的数据
        CltTestBug testBugDB = cltTestBugMapper.selectByPrimaryKey(testBugQuery.getBugId());

        //UI传过来的数据
        CltTestBug testBugUI = new CltTestBug();

        BeanUtil.copyProperties(testBugQuery, testBugUI);
        //定义不需要添加的属性
        // String ignoreArr[] = new String[]{"updateUser", "bugId", "bugTitle","bugTitleName","message", "bugType", "operStep", "perResult", "actualResult", "envMessage", "isReappear","attach","caseId","roundId"};
        String ignoreArr[] = new String[]{"createType", "updateUser", "bugId", "bugTitleName", "message", "attach"};
        //比较两个实体的属性的不同
        Map<String, List<Object>> compareResult = CompareFieldsUtil.compareFields(testBugUI, testBugDB, ignoreArr);
        //不为空就添加历史操作记录表
        if (compareResult != null && compareResult.size() > 0) {

            Set<String> keySet = compareResult.keySet();

            for (String key : keySet) {

                List<Object> list = compareResult.get(key);


                //添加历史操作记录表
                if (!StringUtils.isEmpty(key) && list != null && list.size() > 0) {

                    //组装添加的参数
                    CltBugRecord cltBugRecord = new CltBugRecord();
                    //主键
                    cltBugRecord.setRecordId(UUIDUtil.getUUID());
                    //获取修改人
                    cltBugRecord.setModifier(testBugQuery.getUpdateUser());
                    //获取修改时间
                    cltBugRecord.setRecordTime(new Date());
                    //缺陷Id
                    cltBugRecord.setBugId(testBugQuery.getBugId());
                    //更改字段
                    cltBugRecord.setRecordField(key);

                    if (key.equals("receiver")) {

                        cltBugRecord.setRecordValue(testBugQuery.getReceiverName());
                    }

                    if (key.equals("roundId")) {
                        cltBugRecord.setRecordValue(testBugQuery.getRoundName());

                    }
                    if (key.equals("caseId") || key.equals("bugTitle") || key.equals("operStep") || key.equals("perResult") || key.equals("actualResult") || key.equals("envMessage") || key.equals("isReappear")) {
                        cltBugRecord.setRecordValue("已变更");

                    }
                    if (key.equals("emergency") || key.equals("bugGrade") || key.equals("bugType") || key.equals("isReappear") || key.equals("module") || key.equals("status")) {

                        //更改值
                        cltBugRecord.setRecordValue(list.get(0).toString());


                    }


                    try {
                        //插入数据库
                        int addNum = cltBugRecordMapper.insertSelective(cltBugRecord);

                        if (addNum > 0) {
                            addRecordNum++;
                        }

                    } catch (Exception e) {

                        logger.info("addCltBugRecordInfo", e);

                        throw new GlobalException(ErrorConstants.ADD_CLT_TEST_BUG_RECORD_INFO_ERROR_CODE, ErrorConstants.ADD_CLT_TEST_BUG_RECORD_INFO_ERROR_MESSAGE);

                    }

                }

            }
        }

        return addRecordNum;
    }

    private CltTestBugExample assemblyExample(TestBugQuery testBugQuery) {
        return assemblyExampleForType(testBugQuery,true);
    }
    /**
     * 装配缺陷查询参数
     *
     * @param testBugQuery
     * @param type 搜索类型
     * @return
     */
    private CltTestBugExample assemblyExampleForType(TestBugQuery testBugQuery,boolean type) {

        if (testBugQuery.getSorters() == null || testBugQuery.getSorters().size() == 0) {
            List<SortOrder> sorters = new ArrayList<>();
            SortOrder sortOrder = new SortOrder("orderNum", Direction.DESC);
            sorters.add(sortOrder);
            testBugQuery.setSorters(sorters);
        }

        CltTestBugExample example = new CltTestBugExample();
        example.setOrderByClause(testBugQuery.getOrderByClause());//设置排序条件
        CltTestBugExample.Criteria criteria = example.createCriteria();
        //缺陷等级
        List<String> bugGrades = testBugQuery.getBugGrades();

        if (bugGrades != null && bugGrades.size() > 0) {
            criteria.andBugGradeIn(bugGrades);
        }
        //所属项目
        if(type){
            if (StringUtils.hasText(testBugQuery.getProId())) {
                criteria.andProIdEqualTo(testBugQuery.getProId());
            }
        }else {
            if(!CollectionUtils.isEmpty(testBugQuery.getProIds())){
                criteria.andProIdIn(testBugQuery.getProIds());
            }
        }


        //标题

        String testBugTitleName = testBugQuery.getBugTitle();
        if (StringUtils.hasText(testBugTitleName)) {
           //criteria.andBugTitleOrOrderLike("%" + testBugTitleName + "%");
            criteria.andBugTitleOrBugOrderLike("%" + testBugTitleName + "%");
        }

        //缺陷创建类型
        if (StringUtils.hasText(testBugQuery.getCreateType())) {
            criteria.andCreateTypeEqualTo(testBugQuery.getCreateType());
        }


        //所属模块
        List<String> modules = testBugQuery.getModules();

        if (modules != null && modules.size() > 0) {

            criteria.andModuleIn(modules);
        }

        //缺陷类型
        List<String> bugTypes = testBugQuery.getBugTypes();

        if (bugTypes != null && bugTypes.size() > 0) {
            criteria.andBugTypeIn(testBugQuery.getBugTypes());
        }
        //缺陷紧急程度
        List<String> emergencies = testBugQuery.getEmergencies();

        if (emergencies != null && emergencies.size() > 0) {
            criteria.andEmergencyIn(testBugQuery.getEmergencies());
        }

        //缺陷状态
        List<String> statuses = testBugQuery.getStatuses();
        if (statuses != null && statuses.size() > 0) {
            criteria.andStatusIn(testBugQuery.getStatuses());
        }

        //缺陷发现人员
        List<String> createUsers = testBugQuery.getCreateUsers();
        if (createUsers != null && createUsers.size() > 0) {
            criteria.andCreateUserIn(testBugQuery.getCreateUsers());
        }

        //分配给

        List<String> receivers = testBugQuery.getReceivers();
        if (receivers != null && receivers.size() > 0) {
            criteria.andReceiverIn(testBugQuery.getReceivers());
        }


        //修复时间
        if (testBugQuery.getHandleDateStart() != null && testBugQuery.getHandleDateEnd() != null) {
            criteria.andHandleDateBetween(testBugQuery.getHandleDateStart(), testBugQuery.getHandleDateEnd());
        }

        //创建时间
        if (testBugQuery.getCreateDateStart() != null && testBugQuery.getCreateDateEnd() != null) {
            criteria.andCreateDateBetween(testBugQuery.getCreateDateStart(), testBugQuery.getCreateDateEnd());
        }

        //关闭时间
        if (testBugQuery.getCloseDateStart() != null && testBugQuery.getCloseDateEnd() != null) {
            criteria.andCloseDateBetween(testBugQuery.getCloseDateStart(), testBugQuery.getCloseDateEnd());
        }
        return example;
    }

    /**
     * 装配日志查询参数
     *
     * @param query
     * @return
     */
    private CltBugLogExample assemblyExampleLog(CltBugLogQuery query) {
        CltBugLogExample example = new CltBugLogExample();
        example.setOrderByClause(query.getOrderByClause());
        CltBugLogExample.Criteria criteria = example.createCriteria();
        criteria.andBugIdEqualTo(query.getBugId());
        return example;
    }


    //获取缺陷序号
    private String getTestBugOrderNum(Integer orderNum) {

//        String code = orderNum + "";
//        int leng = (code.trim()).length();  //定义长度

//        if(leng==1){
//            code="0000"+orderNum;
//        }else if(leng==2){
//
//            code="000"+orderNum;
//        }else if(leng==3){
//
//            code="00"+orderNum;
//        }else if(leng==4){
//
//            code="0"+orderNum;
//        }

        return String.format("%05d", orderNum);


    }


}









