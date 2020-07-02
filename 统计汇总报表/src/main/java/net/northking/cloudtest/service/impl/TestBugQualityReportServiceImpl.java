package net.northking.cloudtest.service.impl;

import net.northking.cloudtest.assist.RedisUtil;
import net.northking.cloudtest.common.Page;
import net.northking.cloudtest.constants.ErrorConstants;
import net.northking.cloudtest.dao.analyse.TestCaseMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.report.CltStsProgressMapper;
import net.northking.cloudtest.dao.task.CltTestcaseExecuteMapper;
import net.northking.cloudtest.dao.testBug.CltTestBugMapper;
import net.northking.cloudtest.domain.analyse.TestCase;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectDto;
import net.northking.cloudtest.domain.project.CltProjectExample;
import net.northking.cloudtest.domain.project.CltProjectQuery;
import net.northking.cloudtest.domain.report.CltStsProgress;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.domain.testBug.CltBugRecord;
import net.northking.cloudtest.dto.report.TestBugGradeData;
import net.northking.cloudtest.dto.report.TestBugGradeDataByTime;
import net.northking.cloudtest.dto.report.TestBugQualityReportDTO;
import net.northking.cloudtest.dto.report.TestBugReportData;
import net.northking.cloudtest.dto.task.CltTestCaseExecuteDTO;
import net.northking.cloudtest.dto.testBug.TestBugDTO;
import net.northking.cloudtest.dto.user.CltUserDTO;
import net.northking.cloudtest.exception.GlobalException;
import net.northking.cloudtest.feign.analyse.TestCaseFeignClient;
import net.northking.cloudtest.feign.execute.ManualExecFeignClient;
import net.northking.cloudtest.feign.project.ProjectFeignClient;
import net.northking.cloudtest.query.analyse.TestCaseQuery;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.query.report.TestBugQualityReportQuery;
import net.northking.cloudtest.query.testBug.TestBugQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.TestBugQualityReportService;
import net.northking.cloudtest.utils.MapperUtils;
import net.northking.cloudtest.utils.PageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Title:
 * @Description: 缺陷质量报告逻辑实现层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-09 9:51
 * @UpdateUser:
 * @Version:0.1
 */

@Service
public class TestBugQualityReportServiceImpl implements TestBugQualityReportService {

    //日志
    private final static Logger logger = LoggerFactory.getLogger(TestBugQualityReportServiceImpl.class);

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Autowired
    private CltTestBugMapper cltTestBugMapper;

    @Autowired
    private CltStsProgressMapper cltStsProgressMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CltTestcaseExecuteMapper cltTestcaseExecuteMapper;

    @Autowired
    private CltProjectMapper cltProjectMapper;
    @Autowired
    private TestCaseFeignClient testCaseFeignClient;


    /**
     * 缺陷密度质量统计表(模块)
     *
     * @param testBugQualityReportQuery
     * @return
     * @throws Exception
     */
    @Override
    public TestBugQualityReportDTO testBugDensityReportByModule(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {

        List<TestCase> testCases = queryTestCaseModuleAndCaseNumByProId(testBugQualityReportQuery.getProId());

        List<TestBugDTO> bugDTOS = queryTestBugNumByModule(testBugQualityReportQuery.getProId());

        //组装数据
        TestBugQualityReportDTO testBugQualityReportDTO = getTestBugQualityReportDTO(testCases, bugDTOS);


        return testBugQualityReportDTO;
    }


    /**
     * 缺陷密度质量统计表(人员)
     *
     * @param testBugQualityReportQuery
     * @return
     * @throws Exception
     */
    @Override
    public TestBugQualityReportDTO testBugDensityReportByUser(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {

        //用户--用例数量
        List<CltTestCaseExecuteDTO> cltTestCaseExecuteDTOList = queryReceiverUserCaseNumByProId(testBugQualityReportQuery.getProId());

        //用户 -- 缺陷数量
        List<TestBugDTO> testBugDTOList = getReceiverUserTestBugNum(testBugQualityReportQuery.getProId());

        //组装数据
        TestBugQualityReportDTO testBugQualityReportDTO = getTestBugQualityReportDTOByUser(cltTestCaseExecuteDTOList, testBugDTOList);


        return testBugQualityReportDTO;
    }


    /**
     * 组装数据
     *
     * @param cltTestCaseExecuteDTOList
     * @param testBugDTOList
     * @return
     */
    private TestBugQualityReportDTO getTestBugQualityReportDTOByUser(List<CltTestCaseExecuteDTO> cltTestCaseExecuteDTOList, List<TestBugDTO> testBugDTOList) {


        List<String> testBugUserIds = new ArrayList<>();

        List<String> yAxis = new ArrayList<>();

        List<Double> testBugNums = new ArrayList<>();

        List<Double> caseNums = new ArrayList<>();

        Map<String, Integer> executeMap = new HashMap<>();

        for (int i = 0; i < testBugDTOList.size(); i++) {
            TestBugDTO testBugDTO = testBugDTOList.get(i);
            testBugUserIds.add(testBugDTO.getCreateUser());
            testBugNums.add(getTestBugResult(testBugDTO));
        }


        for (int i = 0; i < cltTestCaseExecuteDTOList.size(); i++) {

            CltTestCaseExecuteDTO cltTestCaseExecuteDTO = cltTestCaseExecuteDTOList.get(i);

            executeMap.put(cltTestCaseExecuteDTO.getReceiveUser(), cltTestCaseExecuteDTO.getCaseNum());

        }

        for (int i = 0; i < testBugUserIds.size(); i++) {
            String s = testBugUserIds.get(i);

            if (executeMap.containsKey(s)) {
                caseNums.add(executeMap.get(s).doubleValue());

            } else {
                caseNums.add(0.0);
            }

        }


        for (int i = 0; i < testBugUserIds.size(); i++) {


            String userId = testBugUserIds.get(i);
            String userChnName = (String) redisUtil.get("username:" + userId);
            yAxis.add(userChnName);
        }


        TestBugReportData testBug = new TestBugReportData();

        TestBugReportData caseNum = new TestBugReportData();

        testBug.setName("缺陷值");

        testBug.setData(testBugNums);

        caseNum.setName("测试用例数量");

        caseNum.setData(caseNums);

        List<TestBugReportData> xAxis = new ArrayList<>();

        xAxis.add(caseNum);
        xAxis.add(testBug);

        TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();

        testBugQualityReportDTO.setxAxis(xAxis);
        testBugQualityReportDTO.setyAxis(yAxis);

        return testBugQualityReportDTO;


    }


    //缺陷严重程度分布图(模块/人员)
    @Override
    public TestBugQualityReportDTO testBugGradeReportByModuleOrUser(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {

        //M:按模块  U:按人员
        String type = testBugQualityReportQuery.getType();

        String timeType = testBugQualityReportQuery.getTimeType();


        if ("M".equals(type) || "AM".equals(type)) {   //按模块

            TestBugQualityReportDTO bugGradeData = getTestBugGradeByModule(testBugQualityReportQuery, timeType);
            if (bugGradeData != null) return bugGradeData;


        } else {  //按人员

            TestBugQualityReportDTO bugGradeData = getTestBugGradeByUser(testBugQualityReportQuery, timeType);
            if (bugGradeData != null) return bugGradeData;


        }

        return null;

    }


    @Autowired
    ManualExecFeignClient manualExecFeignClient;

    //测试执行人员缺陷数量统计
    public List<CltTestCaseExecuteDTO> queryReceiverUserCaseNumByProId(String projectId) {


        List<CltTestCaseExecuteDTO> cltTestCaseExecuteDTOList = null;

        try {

            QualityReportQuery query = new QualityReportQuery();
            query.setProId(projectId);
            cltTestCaseExecuteDTOList = manualExecFeignClient.queryReceiverUserCaseNumByProId(query).getData();
//            cltTestCaseExecuteDTOList = cltTestcaseExecuteMapper.queryReceiverUserCaseNumByProId(projectId);

        } catch (Exception e) {

            logger.info("queryReceiverUserCaseNumByProId", e);
        }

        return cltTestCaseExecuteDTOList;

    }

    //测试执行人员缺陷数量统计
    public List<TestBugDTO> getReceiverUserTestBugNum(String proId) {

        List<TestBugDTO> testBugDTOList = null;


        try {

            testBugDTOList = cltTestBugMapper.getReceiverUserTestBugNum(proId);


        } catch (Exception e) {

            logger.info("getReceiverUserTestBugNum", e);

        }

        return testBugDTOList;


    }


    //缺陷严重程度(按人员)
    private TestBugQualityReportDTO getTestBugGradeByUser(TestBugQualityReportQuery testBugQualityReportQuery, String timeType) throws Exception {
        if ("D".equals(timeType)) {
            //默认先查询最近七天的数据
            List<CltStsProgress> cltStsProgresses = cltStsProgressMapper.queryTestBugNumByUserByDay(testBugQualityReportQuery);

            //组装数据
            TestBugQualityReportDTO bugGradeData = getBugGradeDataByDay(testBugQualityReportQuery, cltStsProgresses);

            return bugGradeData;
        }
        if ("W".equals(timeType)) {

            //查询前四周的数据
            //第一周的数据
            List<TestBugDTO> testBugDTOListFirst = cltTestBugMapper.queryTestBugNumByUserFirstWeek(testBugQualityReportQuery.getProId());

            for (int i = 0; i < testBugDTOListFirst.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListFirst.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }


            //查询第二周的数据
            List<TestBugDTO> testBugDTOListSecond = cltTestBugMapper.queryTestBugNumByUserSecondWeek(testBugQualityReportQuery.getProId());
            for (int i = 0; i < testBugDTOListSecond.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListSecond.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }

            //查询第三周的数据
            List<TestBugDTO> testBugDTOListThird = cltTestBugMapper.queryTestBugNumByUserThirdWeek(testBugQualityReportQuery.getProId());
            for (int i = 0; i < testBugDTOListThird.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListThird.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }


            //查询第四周的数据
            List<TestBugDTO> testBugDTOListForth = cltTestBugMapper.queryTestBugNumByUserForthWeek(testBugQualityReportQuery.getProId());

            for (int i = 0; i < testBugDTOListForth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListForth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }

            TestBugQualityReportDTO bugGradeDataByWeek = getBugGradeDataByWeek(testBugDTOListFirst, testBugDTOListSecond, testBugDTOListThird, testBugDTOListForth, "G", timeType, "U");

            return bugGradeDataByWeek;

        }

        if ("M".equals(timeType)) {
            //第一月的数据
            List<TestBugDTO> testBugDTOListFirstMonth = cltTestBugMapper.queryTestBugNumByUserFirstMonth(testBugQualityReportQuery.getProId());

            for (int i = 0; i < testBugDTOListFirstMonth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListFirstMonth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }
            //查询第二月的数据
            List<TestBugDTO> testBugDTOListSecondMonth = cltTestBugMapper.queryTestBugNumByUserSecondMonth(testBugQualityReportQuery.getProId());

            for (int i = 0; i < testBugDTOListSecondMonth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListSecondMonth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }
            //查询第三月的数据
            List<TestBugDTO> testBugDTOListThirdMonth = cltTestBugMapper.queryTestBugNumByUserThirdMonth(testBugQualityReportQuery.getProId());

            for (int i = 0; i < testBugDTOListThirdMonth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListThirdMonth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }
            //查询第四月的数据
            List<TestBugDTO> testBugDTOListForthMonth = cltTestBugMapper.queryTestBugNumByUserForthMonth(testBugQualityReportQuery.getProId());

            for (int i = 0; i < testBugDTOListForthMonth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListForthMonth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }

            TestBugQualityReportDTO bugGradeDataByMonth = getBugGradeDataByMonth(testBugDTOListFirstMonth, testBugDTOListSecondMonth, testBugDTOListThirdMonth, testBugDTOListForthMonth, "G", timeType, "U");

            return bugGradeDataByMonth;

        }
        return null;
    }


    //缺陷严重程度(按模块)
    private TestBugQualityReportDTO getTestBugGradeByModule(TestBugQualityReportQuery testBugQualityReportQuery, String timeType) throws Exception {
        if ("D".equals(timeType)) {
            //默认先查询最近七天的数据
            List<CltStsProgress> cltStsProgresses = cltStsProgressMapper.queryTestBugNumByModuleByDay(testBugQualityReportQuery);

            //组装数据
            TestBugQualityReportDTO bugGradeData = getBugGradeDataByDay(testBugQualityReportQuery, cltStsProgresses);

            return bugGradeData;
        }
        if ("W".equals(timeType)) {

            //查询前四周的数据
            //第一周的数据
            List<TestBugDTO> testBugDTOListFirst = cltTestBugMapper.queryTestBugNumFirstWeek(testBugQualityReportQuery.getProId());
            //查询第二周的数据
            List<TestBugDTO> testBugDTOListSecond = cltTestBugMapper.queryTestBugNumSecondWeek(testBugQualityReportQuery.getProId());
            //查询第三周的数据
            List<TestBugDTO> testBugDTOListThird = cltTestBugMapper.queryTestBugNumThirdWeek(testBugQualityReportQuery.getProId());
            //查询第四周的数据
            List<TestBugDTO> testBugDTOListForth = cltTestBugMapper.queryTestBugNumForthWeek(testBugQualityReportQuery.getProId());
            TestBugQualityReportDTO bugGradeDataByWeek = getBugGradeDataByWeek(testBugDTOListFirst, testBugDTOListSecond, testBugDTOListThird, testBugDTOListForth, "G", timeType, "M");

            return bugGradeDataByWeek;

        }

        if ("M".equals(timeType)) {
            //第一月的数据
            List<TestBugDTO> testBugDTOListFirstMonth = cltTestBugMapper.queryTestBugNumFirstMonth(testBugQualityReportQuery.getProId());
            //查询第二月的数据
            List<TestBugDTO> testBugDTOListSecondMonth = cltTestBugMapper.queryTestBugNumSecondMonth(testBugQualityReportQuery.getProId());
            //查询第三月的数据
            List<TestBugDTO> testBugDTOListThirdMonth = cltTestBugMapper.queryTestBugNumThirdMonth(testBugQualityReportQuery.getProId());
            //查询第四月的数据
            List<TestBugDTO> testBugDTOListForthMonth = cltTestBugMapper.queryTestBugNumForthMonth(testBugQualityReportQuery.getProId());

            TestBugQualityReportDTO bugGradeDataByMonth = getBugGradeDataByMonth(testBugDTOListFirstMonth, testBugDTOListSecondMonth, testBugDTOListThirdMonth, testBugDTOListForthMonth, "G", timeType, "M");

            return bugGradeDataByMonth;

        }
        return null;
    }


    //缺陷状态分布图(模块/人员)
    @Override
    public TestBugQualityReportDTO testBugStatusReportByModuleOrUser(TestBugQualityReportQuery testBugQualityReportQuery) throws Exception {


        //M:按模块  U:按人员
        String type = testBugQualityReportQuery.getType();

        String timeType = testBugQualityReportQuery.getTimeType();


        if ("M".equals(type) || "AM".equals(type)) { //按模块

            TestBugQualityReportDTO bugGradeData = getTestBugStatusByModule(testBugQualityReportQuery, timeType);
            if (bugGradeData != null) return bugGradeData;

        } else {  //按人员

            TestBugQualityReportDTO bugGradeData = getTestBugStatusByUser(testBugQualityReportQuery, timeType);
            if (bugGradeData != null) return bugGradeData;

        }

        return null;
    }


    /**
     * 总体缺陷状态分布图(项目/人员)
     *
     * @param query 传入proId  type（type为 “U”则横坐标为用户， type为“C”则横坐标为子项目）
     * @return
     * @throws Exception
     */
    @Override
    public TestBugQualityReportDTO allTestBugStatusReportBySonOrUser(TestBugQualityReportQuery query) throws Exception {

        TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();
        //M:按模块  U:按人员
        String type = query.getType();


        Calendar ca = Calendar.getInstance();//得到一个Calendar的实例
        ca.setTime(new Date()); //设置时间为当前时间
        ca.add(Calendar.DAY_OF_MONTH, -2); //日期减1

        query.setStartDate(ca.getTime());
        query.setEndDate(new Date());

        String timeType = "D";

        //查询父项目下的所有子项目
        CltProjectExample cltProjectExample = new CltProjectExample();
        CltProjectExample.Criteria criteria = cltProjectExample.createCriteria();
        criteria.andProParentIdEqualTo(query.getProId());

        List<CltProject> sonProjectList = cltProjectMapper.selectByExample(cltProjectExample);

        List<String> sonIdList = new ArrayList<>();

        for (CltProject son : sonProjectList) {
            sonIdList.add(son.getProId());
        }

        //按子项目
        if ("C".equals(type)) {

            //将map封装
            List<String> sonProjectNameList = new ArrayList<>();
            List<List<TestBugGradeData>> yAxisData = new ArrayList<>();

            for (CltProject son : sonProjectList) {
                TestBugDTO testBugDTO = cltTestBugMapper.testBugStatusReportByTotal(son.getProId());
                StringBuffer sb = new StringBuffer("{\"1\":\"" + testBugDTO.getCase1());
                sb.append("\",\"2\":\"" + testBugDTO.getCase2());
                sb.append("\",\"3\":\"" + testBugDTO.getCase3());
                sb.append("\",\"4\":\"" + testBugDTO.getCase4());
                sb.append("\",\"5\":\"" + testBugDTO.getCase5());
                sb.append("\",\"6\":\"" + testBugDTO.getCase6());
                sb.append("\",\"7\":\"" + testBugDTO.getCase7());
                sb.append("\",\"8\":\"" + testBugDTO.getCase8());
                sb.append("\",\"9\":\"" + testBugDTO.getCase9());
                sb.append("\",\"10\":\"" + testBugDTO.getCase10());
                sb.append("\",\"11\":\"" + testBugDTO.getCase11());
                sb.append("\",\"total\":\"" + testBugDTO.getCaseTotal() + "\"}");

                //封装TestBugGradeData
                TestBugGradeData testBugGradeData = new TestBugGradeData();
                testBugGradeData.setResult(sb.toString());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String strDate = format.format(new Date());
                testBugGradeData.setTime(strDate);

                //封装第一次 --对应天
                List<TestBugGradeData> yAxisDataItem = new ArrayList<>();
                yAxisDataItem.add(testBugGradeData);

                //封装第二层 --对应人员
                yAxisData.add(yAxisDataItem);
                sonProjectNameList.add(son.getProName());

            }
            testBugQualityReportDTO.setxAxisData(sonProjectNameList);
            testBugQualityReportDTO.setyAxisData(yAxisData);

        } else {  //按人员
            // key为人员，value为该人员的y坐标数据
            Map<String, String> mapDate = new HashMap<>();
            //遍历子项目
            for (String sonId : sonIdList) {
                query.setProId(sonId);
                TestBugQualityReportDTO testBugStatus = getTestBugStatusByUser(query, timeType);

                //查询到的X数据--用户
                List<String> userList = testBugStatus.getxAxisData();

                //查询到的Y数据(最外层对应用户  第二层对应时间  里面的result对象对应json字符串 )
                List<List<TestBugGradeData>> yAxisData = testBugStatus.getyAxisData();

                //遍历用户
                for (int i = 0; i < userList.size(); i++) {
                    String user = userList.get(i);
                    //map中的数据
                    String YMap = mapDate.get(userList.get(i));

                    //查询到的数据
                    List<TestBugGradeData> yAxisDatas = yAxisData.get(i);
                    String YData = yAxisDatas.get(yAxisDatas.size() - 1).getResult();
                    if (YMap == null) {

                        //放入数据，因为只有一天，所以数据只有一天，所以get(0)
                        mapDate.put(user, YData);
                    } else {

                        //数据相加后放入
                        String YAdd = yAxisAdd(YMap, YData);
                        mapDate.put(user, YAdd);
                    }
                }
            }

            //将map封装
            List<String> userList = new ArrayList<>();
            List<List<TestBugGradeData>> yAxisData = new ArrayList<>();

            for (Map.Entry<String, String> data : mapDate.entrySet()) {

                //封装用户
                userList.add(data.getKey());

                //封装TestBugGradeData
                TestBugGradeData testBugGradeData = new TestBugGradeData();
                testBugGradeData.setResult(data.getValue());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String strDate = format.format(new Date());
                testBugGradeData.setTime(strDate);

                //封装第一次 --对应天
                List<TestBugGradeData> yAxisDataItem = new ArrayList<>();
                yAxisDataItem.add(testBugGradeData);

                //封装第二层 --对应人员
                yAxisData.add(yAxisDataItem);
            }

            //封装数据返回
            testBugQualityReportDTO = new TestBugQualityReportDTO();
            testBugQualityReportDTO.setxAxisData(userList);
            testBugQualityReportDTO.setyAxisData(yAxisData);

        }

        return testBugQualityReportDTO;
    }


    /**
     * 两个Y轴item相加
     */
    private String yAxisAdd(String a, String b) throws Exception {
        Map<String, Object> aMap = MapperUtils.json2map(a);
        Map<String, Object> bMap = MapperUtils.json2map(b);
        StringBuffer sb = new StringBuffer("{");

        for (int i = 1; i <= 11; i++) {
            int c = Integer.parseInt((String) aMap.get(i + "")) + Integer.parseInt((String) bMap.get(i + ""));
            sb.append("\"" + i + "\":\"" + c + "\",");
        }
        int total = Integer.parseInt((String) aMap.get("total")) + Integer.parseInt((String) bMap.get("total"));

        sb.append("\"" + "total" + "\":\"" + total + "\"}");

        return sb.toString();

    }


    //缺陷状态分布(按人员)
    private TestBugQualityReportDTO getTestBugStatusByUser(TestBugQualityReportQuery query, String timeType) throws
            Exception {
        if ("D".equals(timeType)) {
            //默认先查询最近七天的数据
            List<CltStsProgress> cltStsProgresses = cltStsProgressMapper.queryTestBugNumStatusByUserByDay(query);

            //组装数据
            TestBugQualityReportDTO bugGradeData = getBugGradeDataByDay(query, cltStsProgresses);

            return bugGradeData;
        }
        if ("W".equals(timeType)) {

            //查询前四周的数据
            //第一周的数据
            List<TestBugDTO> testBugDTOListFirst = cltTestBugMapper.queryTestBugNumStatusByUserFirstWeek(query.getProId(), query.getType());

            for (int i = 0; i < testBugDTOListFirst.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListFirst.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }


            //查询第二周的数据
            List<TestBugDTO> testBugDTOListSecond = cltTestBugMapper.queryTestBugNumStatusByUserSecondWeek(query.getProId(), query.getType());
            for (int i = 0; i < testBugDTOListSecond.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListSecond.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }

            //查询第三周的数据
            List<TestBugDTO> testBugDTOListThird = cltTestBugMapper.queryTestBugNumStatusByUserThirdWeek(query.getProId(), query.getType());
            for (int i = 0; i < testBugDTOListThird.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListThird.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }


            //查询第四周的数据
            List<TestBugDTO> testBugDTOListForth = cltTestBugMapper.queryTestBugNumStatusByUserForthWeek(query.getProId(), query.getType());

            for (int i = 0; i < testBugDTOListForth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListForth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }

            TestBugQualityReportDTO bugGradeDataByWeek = getBugGradeDataByWeek(testBugDTOListFirst, testBugDTOListSecond, testBugDTOListThird, testBugDTOListForth, "S", timeType, "U");

            return bugGradeDataByWeek;

        }

        if ("M".equals(timeType)) {
            //第一月的数据
            List<TestBugDTO> testBugDTOListFirstMonth = cltTestBugMapper.queryTestBugNumStatusByUserFirstMonth(query.getProId(), query.getType());

            for (int i = 0; i < testBugDTOListFirstMonth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListFirstMonth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }
            //查询第二月的数据
            List<TestBugDTO> testBugDTOListSecondMonth = cltTestBugMapper.queryTestBugNumStatusByUserSecondMonth(query.getProId(), query.getType());

            for (int i = 0; i < testBugDTOListSecondMonth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListSecondMonth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }
            //查询第三月的数据
            List<TestBugDTO> testBugDTOListThirdMonth = cltTestBugMapper.queryTestBugNumStatusByUserThirdMonth(query.getProId(), query.getType());

            for (int i = 0; i < testBugDTOListThirdMonth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListThirdMonth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }
            //查询第四月的数据
            List<TestBugDTO> testBugDTOListForthMonth = cltTestBugMapper.queryTestBugNumStatusByUserForthMonth(query.getProId());

            for (int i = 0; i < testBugDTOListForthMonth.size(); i++) {
                TestBugDTO testBugDTO = testBugDTOListForthMonth.get(i);

                String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());

                testBugDTO.setCreateUserName(createUserName);

            }

            TestBugQualityReportDTO bugGradeDataByMonth = getBugGradeDataByMonth(testBugDTOListFirstMonth, testBugDTOListSecondMonth, testBugDTOListThirdMonth, testBugDTOListForthMonth, "S", timeType, "U");

            return bugGradeDataByMonth;

        }
        return null;
    }

    //缺陷状态分布(按模块)
    private TestBugQualityReportDTO getTestBugStatusByModule(TestBugQualityReportQuery query, String timeType) throws
            Exception {
        if ("D".equals(timeType)) {
            //默认先查询最近七天的数据
            List<CltStsProgress> cltStsProgresses = cltStsProgressMapper.queryTestBugNumStatusByModuleByDay(query);

            //组装数据
            TestBugQualityReportDTO bugGradeData = getBugGradeDataByDay(query, cltStsProgresses);

            return bugGradeData;
        }
        if ("W".equals(timeType)) {

            //查询前四周的数据
            //第一周的数据
            List<TestBugDTO> testBugDTOListFirst = cltTestBugMapper.queryTestBugNumStatusByModuleFirstWeek(query.getProId(), query.getType());
            //查询第二周的数据
            List<TestBugDTO> testBugDTOListSecond = cltTestBugMapper.queryTestBugNumStatusByModuleSecondWeek(query.getProId(), query.getType());
            //查询第三周的数据
            List<TestBugDTO> testBugDTOListThird = cltTestBugMapper.queryTestBugNumStatusByModuleThirdWeek(query.getProId(), query.getType());
            //查询第四周的数据
            List<TestBugDTO> testBugDTOListForth = cltTestBugMapper.queryTestBugNumStatusByModuleForthWeek(query.getProId(), query.getType());
            TestBugQualityReportDTO bugGradeDataByWeek = getBugGradeDataByWeek(testBugDTOListFirst, testBugDTOListSecond, testBugDTOListThird, testBugDTOListForth, "S", timeType, "M");

            return bugGradeDataByWeek;

        }

        if ("M".equals(timeType)) {
            //第一月的数据
            List<TestBugDTO> testBugDTOListFirstMonth = cltTestBugMapper.queryTestBugNumStatusByModuleFirstMonth(query.getProId(), query.getType());
            //查询第二月的数据
            List<TestBugDTO> testBugDTOListSecondMonth = cltTestBugMapper.queryTestBugNumStatusByModuleSecondMonth(query.getProId(), query.getType());
            //查询第三月的数据
            List<TestBugDTO> testBugDTOListThirdMonth = cltTestBugMapper.queryTestBugNumStatusByModuleThirdMonth(query.getProId(), query.getType());
            //查询第四月的数据
            List<TestBugDTO> testBugDTOListForthMonth = cltTestBugMapper.queryTestBugNumStatusByModuleForthMonth(query.getProId(), query.getType());

            TestBugQualityReportDTO bugGradeDataByMonth = getBugGradeDataByMonth(testBugDTOListFirstMonth, testBugDTOListSecondMonth, testBugDTOListThirdMonth, testBugDTOListForthMonth, "S", timeType, "M");

            return bugGradeDataByMonth;

        }
        return null;
    }


    //缺陷严重程度汇总分布图
    @Override
    public TestBugQualityReportDTO testBugGradeReportByTotal(TestBugQualityReportQuery query) throws Exception {

        TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();

        try {

            TestBugDTO testBugDTO = cltTestBugMapper.testBugGradeReportByTotal(query.getProId());

            testBugQualityReportDTO.setTestBugDTO(testBugDTO);

        } catch (Exception e) {

            logger.info("testBugGradeReportByTotal", e);
            throw new GlobalException(ErrorConstants.TEST_BUG_GRADE_REPORT_TOTAL_ERROR_CODE, ErrorConstants.TEST_BUG_GRADE_REPORT_TOTAL_ERROR_MESSAGE);

        }


        return testBugQualityReportDTO;
    }


    //缺陷状态分布汇总分布图
    @Override
    public TestBugQualityReportDTO testBugStatusReportByTotal(TestBugQualityReportQuery query) throws Exception {
        TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();

        try {

            TestBugDTO testBugDTO = cltTestBugMapper.testBugStatusReportByTotal(query.getProId());

            testBugQualityReportDTO.setTestBugDTO(testBugDTO);

        } catch (Exception e) {

            logger.info("testBugStatusReportByTotal", e);
            throw new GlobalException(ErrorConstants.TEST_BUG_STATUS_REPORT_TOTAL_ERROR_CODE, ErrorConstants.TEST_BUG_STATUS_REPORT_TOTAL_ERROR_MESSAGE);

        }


        return testBugQualityReportDTO;
    }


    //==================================封装数据===================================================================

    //组装查询缺陷严重程度的数据(按最近4月)
    public TestBugQualityReportDTO getBugGradeDataByMonth
    (List<TestBugDTO> testBugDTOListFirstMonth, List<TestBugDTO> testBugDTOListSecondMonth, List<TestBugDTO> testBugDTOListThirdMonth, List<TestBugDTO> testBugDTOListForthMonth, String
            funcCode, String timeType, String type) {


        TestBugGradeDataByTime month1 = new TestBugGradeDataByTime();
        TestBugGradeDataByTime month2 = new TestBugGradeDataByTime();
        TestBugGradeDataByTime month3 = new TestBugGradeDataByTime();
        TestBugGradeDataByTime month4 = new TestBugGradeDataByTime();
        List<TestBugGradeData> firstmonths = new ArrayList<>();
        List<TestBugGradeData> secondmonths = new ArrayList<>();
        List<TestBugGradeData> thirdmonths = new ArrayList<>();
        List<TestBugGradeData> forthmonths = new ArrayList<>();

        if (testBugDTOListFirstMonth != null && testBugDTOListFirstMonth.size() > 0) {

            for (int i = 0; i < testBugDTOListFirstMonth.size(); i++) {

                TestBugDTO testBugDTO = testBugDTOListFirstMonth.get(i);
                TestBugGradeData firstmonth = new TestBugGradeData();
                if ("G".equals(funcCode)) {
                    firstmonth.setResult(getBugResultByGrade(testBugDTO));

                } else {
                    firstmonth.setResult(getBugResultByStatus(testBugDTO));

                }

                if ("U".equals(type)) {
                    firstmonth.setUserChnName(testBugDTO.getCreateUserName());
                    firstmonth.setUserId(testBugDTO.getCreateUser());
                } else {

                    firstmonth.setModule(testBugDTO.getModule());
                }
                firstmonths.add(firstmonth);
            }
            month1.setTime(getTime(0));

            month1.setWeeks(firstmonths);
        } else {
            month1.setTime(getTime(0));

            month1.setWeeks(null);
        }

        if (testBugDTOListSecondMonth != null && testBugDTOListSecondMonth.size() > 0) {
            for (int i = 0; i < testBugDTOListSecondMonth.size(); i++) {

                TestBugDTO testBugDTO = testBugDTOListSecondMonth.get(i);

                TestBugGradeData secondmonth = new TestBugGradeData();
                if ("G".equals(funcCode)) {
                    secondmonth.setResult(getBugResultByGrade(testBugDTO));

                } else {

                    secondmonth.setResult(getBugResultByStatus(testBugDTO));
                }

                if ("U".equals(type)) {
                    secondmonth.setUserChnName(testBugDTO.getCreateUserName());
                    secondmonth.setUserId(testBugDTO.getCreateUser());
                } else {

                    secondmonth.setModule(testBugDTO.getModule());
                }

                secondmonths.add(secondmonth);
            }
            month2.setTime(getTime(1));

            month2.setWeeks(secondmonths);
        } else {
            month2.setTime(getTime(1));

            month2.setWeeks(null);

        }
        if (testBugDTOListThirdMonth != null && testBugDTOListThirdMonth.size() > 0) {
            for (int i = 0; i < testBugDTOListThirdMonth.size(); i++) {

                TestBugDTO testBugDTO = testBugDTOListThirdMonth.get(i);

                TestBugGradeData thirdmonth = new TestBugGradeData();

                if ("G".equals(funcCode)) {

                    thirdmonth.setResult(getBugResultByGrade(testBugDTO));
                } else {
                    thirdmonth.setResult(getBugResultByStatus(testBugDTO));
                }

                if ("U".equals(type)) {
                    thirdmonth.setUserChnName(testBugDTO.getCreateUserName());
                    thirdmonth.setUserId(testBugDTO.getCreateUser());
                } else {
                    thirdmonth.setModule(testBugDTO.getModule());
                }

                thirdmonths.add(thirdmonth);
            }
            month3.setTime(getTime(2));

            month3.setWeeks(thirdmonths);
        } else {
            month3.setTime(getTime(2));

            month3.setWeeks(null);
        }
        if (testBugDTOListForthMonth != null && testBugDTOListForthMonth.size() > 0) {
            for (int i = 0; i < testBugDTOListForthMonth.size(); i++) {

                TestBugDTO testBugDTO = testBugDTOListForthMonth.get(i);

                TestBugGradeData forthmonth = new TestBugGradeData();


                if ("G".equals(funcCode)) {
                    forthmonth.setResult(getBugResultByGrade(testBugDTO));

                } else {

                    forthmonth.setResult(getBugResultByStatus(testBugDTO));
                }

                if ("U".equals(type)) {
                    forthmonth.setUserChnName(testBugDTO.getCreateUserName());
                    forthmonth.setUserId(testBugDTO.getCreateUser());
                } else {
                    forthmonth.setModule(testBugDTO.getModule());
                }
                forthmonths.add(forthmonth);
            }
            month4.setTime(getTime(3));

            month4.setWeeks(forthmonths);
        } else {
            month4.setTime(getTime(3));
            month4.setWeeks(null);
        }


        List<TestBugGradeDataByTime> yAxisDataResult = new ArrayList<>();
        yAxisDataResult.add(month4);
        yAxisDataResult.add(month3);
        yAxisDataResult.add(month2);
        yAxisDataResult.add(month1);


        if ("M".equals(type)) {
            Set<String> moduleSet = new HashSet<>();

            List<TestBugGradeData> months1 = month1.getWeeks();
            if (months1 != null && months1.size() > 0) {
                for (int i = 0; i < months1.size(); i++) {
                    TestBugGradeData testBugGradeData = months1.get(i);
                    moduleSet.add(testBugGradeData.getModule());

                }
            }

            List<TestBugGradeData> months2 = month2.getWeeks();

            if (months2 != null && months2.size() > 0) {

                for (int i = 0; i < months2.size(); i++) {
                    TestBugGradeData testBugGradeData = months2.get(i);
                    moduleSet.add(testBugGradeData.getModule());
                }

            }

            List<TestBugGradeData> months3 = month3.getWeeks();

            if (months3 != null && months3.size() > 0) {

                for (int i = 0; i < months3.size(); i++) {
                    TestBugGradeData testBugGradeData = months3.get(i);
                    moduleSet.add(testBugGradeData.getModule());

                }
            }

            List<TestBugGradeData> months4 = month4.getWeeks();
            if (months4 != null && months4.size() > 0) {
                for (int i = 0; i < months4.size(); i++) {
                    TestBugGradeData testBugGradeData = months4.get(i);
                    moduleSet.add(testBugGradeData.getModule());
                }
            }
            List<String> xAsix = new ArrayList<>();

            xAsix.addAll(moduleSet);

            TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();

            testBugQualityReportDTO.setyAxisDataResult(yAxisDataResult);

            testBugQualityReportDTO.setxAxisData(xAsix);

            testBugQualityReportDTO.setTimeType(timeType);


            return testBugQualityReportDTO;


        } else {
            List<CltUserDTO> cltUserDTOS = new ArrayList<>();

            Set<String> userIdsSet = new HashSet<>();

            List<TestBugGradeData> months1 = month1.getWeeks();
            if (months1 != null && months1.size() > 0) {

                for (int i = 0; i < months1.size(); i++) {
                    TestBugGradeData testBugGradeData = months1.get(i);
                    userIdsSet.add(testBugGradeData.getUserId());

                }
            }
            List<TestBugGradeData> months2 = month2.getWeeks();
            if (months2 != null && months2.size() > 0) {

                for (int i = 0; i < months2.size(); i++) {
                    TestBugGradeData testBugGradeData = months2.get(i);
                    userIdsSet.add(testBugGradeData.getUserId());

                }
            }
            List<TestBugGradeData> months3 = month3.getWeeks();
            if (months3 != null && months3.size() > 0) {

                for (int i = 0; i < months3.size(); i++) {
                    TestBugGradeData testBugGradeData = months3.get(i);
                    userIdsSet.add(testBugGradeData.getUserId());
                }
            }
            List<TestBugGradeData> months4 = month4.getWeeks();
            if (months4 != null && months4.size() > 0) {

                for (int i = 0; i < months4.size(); i++) {
                    TestBugGradeData testBugGradeData = months4.get(i);
                    userIdsSet.add(testBugGradeData.getUserId());

                }
            }

            List<String> userIds = new ArrayList<>();

            userIds.addAll(userIdsSet);
            for (int i = 0; i < userIds.size(); i++) {
                CltUserDTO cltUserDTO = new CltUserDTO();
                String userId = userIds.get(i);
                String userChnName = (String) redisUtil.get("username:" + userId);
                cltUserDTO.setUserId(userId);
                cltUserDTO.setUserChnName(userChnName);
                cltUserDTOS.add(cltUserDTO);

            }

            TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();

            testBugQualityReportDTO.setyAxisDataResult(yAxisDataResult);

            testBugQualityReportDTO.setUserDTOList(cltUserDTOS);

            testBugQualityReportDTO.setTimeType(timeType);


            return testBugQualityReportDTO;


        }


    }


    //组装查询缺陷严重程度的数据(按最近4周)
    public TestBugQualityReportDTO getBugGradeDataByWeek
    (List<TestBugDTO> testBugDTOListFirst, List<TestBugDTO> testBugDTOListSecond, List<TestBugDTO> testBugDTOListThird, List<TestBugDTO> testBugDTOListForth, String
            funcCode, String timeType, String type) {


        TestBugGradeDataByTime week1 = new TestBugGradeDataByTime();
        TestBugGradeDataByTime week2 = new TestBugGradeDataByTime();
        TestBugGradeDataByTime week3 = new TestBugGradeDataByTime();
        TestBugGradeDataByTime week4 = new TestBugGradeDataByTime();
        List<TestBugGradeData> firstWeeks = new ArrayList<>();
        List<TestBugGradeData> secondWeeks = new ArrayList<>();
        List<TestBugGradeData> thirdWeeks = new ArrayList<>();
        List<TestBugGradeData> forthWeeks = new ArrayList<>();

        Date date = new Date();
        Calendar c = Calendar.getInstance();

        if (testBugDTOListFirst != null && testBugDTOListFirst.size() > 0) {

            c.setTime(date);
            int year = c.get(Calendar.YEAR);
            int week = c.get(Calendar.WEEK_OF_YEAR);
            for (int i = 0; i < testBugDTOListFirst.size(); i++) {

                TestBugDTO testBugDTO = testBugDTOListFirst.get(i);
                TestBugGradeData firstWeek = new TestBugGradeData();
                if ("G".equals(funcCode)) {
                    firstWeek.setResult(getBugResultByGrade(testBugDTO));

                } else {
                    firstWeek.setResult(getBugResultByStatus(testBugDTO));
                }

                if ("U".equals(type)) {
                    firstWeek.setUserChnName(testBugDTO.getCreateUserName());
                    firstWeek.setUserId(testBugDTO.getCreateUser());

                } else {

                    firstWeek.setModule(testBugDTO.getModule());
                }
                firstWeeks.add(firstWeek);
            }
            week1.setTime(year + "年" + "第" + week + "周");

            week1.setWeeks(firstWeeks);
        } else {
            c.setTime(date);
            int year = c.get(Calendar.YEAR);
            int week = c.get(Calendar.WEEK_OF_YEAR);

            week1.setTime(year + "年" + "第" + week + "周");

            week1.setWeeks(null);
        }

        if (testBugDTOListSecond != null && testBugDTOListSecond.size() > 0) {

            date.setDate(date.getDate() - 7);
            c.setTime(date);
            int year = c.get(Calendar.YEAR);
            int week = c.get(Calendar.WEEK_OF_YEAR);

            for (int i = 0; i < testBugDTOListSecond.size(); i++) {

                TestBugDTO testBugDTO = testBugDTOListSecond.get(i);

                TestBugGradeData secondWeek = new TestBugGradeData();

                if ("G".equals(funcCode)) {
                    secondWeek.setResult(getBugResultByGrade(testBugDTO));

                } else {
                    secondWeek.setResult(getBugResultByStatus(testBugDTO));
                }

                if ("U".equals(type)) {
                    secondWeek.setUserChnName(testBugDTO.getCreateUserName());
                    secondWeek.setUserId(testBugDTO.getCreateUser());
                } else {

                    secondWeek.setModule(testBugDTO.getModule());
                }

                secondWeeks.add(secondWeek);
            }
            week2.setTime(year + "年" + "第" + (week) + "周");

            week2.setWeeks(secondWeeks);
        } else {

            date.setDate(date.getDate() - 7);
            c.setTime(date);
            int year = c.get(Calendar.YEAR);
            int week = c.get(Calendar.WEEK_OF_YEAR);
            week2.setTime(year + "年" + "第" + (week) + "周");

            week2.setWeeks(null);

        }
        if (testBugDTOListThird != null && testBugDTOListThird.size() > 0) {
            date.setDate(date.getDate() - 7);
            c.setTime(date);
            int year = c.get(Calendar.YEAR);
            int week = c.get(Calendar.WEEK_OF_YEAR);

            for (int i = 0; i < testBugDTOListThird.size(); i++) {

                TestBugDTO testBugDTO = testBugDTOListThird.get(i);

                TestBugGradeData thirdWeek = new TestBugGradeData();

                if ("G".equals(funcCode)) {
                    thirdWeek.setResult(getBugResultByGrade(testBugDTO));

                } else {
                    thirdWeek.setResult(getBugResultByStatus(testBugDTO));
                }

                if ("U".equals(type)) {
                    thirdWeek.setUserChnName(testBugDTO.getCreateUserName());
                    thirdWeek.setUserId(testBugDTO.getCreateUser());
                } else {
                    thirdWeek.setModule(testBugDTO.getModule());
                }
                thirdWeeks.add(thirdWeek);
            }
            week3.setTime(year + "年" + "第" + (week) + "周");

            week3.setWeeks(thirdWeeks);
        } else {

            date.setDate(date.getDate() - 7);
            c.setTime(date);
            int year = c.get(Calendar.YEAR);
            int week = c.get(Calendar.WEEK_OF_YEAR);
            week3.setTime(year + "年" + "第" + (week) + "周");

            week3.setWeeks(null);
        }
        if (testBugDTOListForth != null && testBugDTOListForth.size() > 0) {
            date.setDate(date.getDate() - 7);
            c.setTime(date);
            int year = c.get(Calendar.YEAR);
            int week = c.get(Calendar.WEEK_OF_YEAR);

            for (int i = 0; i < testBugDTOListForth.size(); i++) {

                TestBugDTO testBugDTO = testBugDTOListForth.get(i);

                TestBugGradeData forthWeek = new TestBugGradeData();
                if ("G".equals(funcCode)) {
                    forthWeek.setResult(getBugResultByGrade(testBugDTO));
                } else {
                    forthWeek.setResult(getBugResultByStatus(testBugDTO));
                }
                if ("U".equals(type)) {
                    forthWeek.setUserChnName(testBugDTO.getCreateUserName());
                    forthWeek.setUserId(testBugDTO.getCreateUser());
                } else {
                    forthWeek.setModule(testBugDTO.getModule());
                }
                forthWeeks.add(forthWeek);
            }
            week4.setTime(year + "年" + "第" + (week) + "周");

            week4.setWeeks(forthWeeks);
        } else {

            date.setDate(date.getDate() - 7);
            c.setTime(date);
            int year = c.get(Calendar.YEAR);
            int week = c.get(Calendar.WEEK_OF_YEAR);

            week4.setTime(year + "年" + "第" + (week) + "周");

            week4.setWeeks(null);
        }


        List<TestBugGradeDataByTime> yAxisDataResult = new ArrayList<>();
        yAxisDataResult.add(week4);
        yAxisDataResult.add(week3);
        yAxisDataResult.add(week2);
        yAxisDataResult.add(week1);


        if ("M".equals(type)) {
            Set<String> moduleSet = new HashSet<>();
            List<TestBugGradeData> weeks1 = week1.getWeeks();

            if (weeks1 != null && weeks1.size() > 0) {
                for (int i = 0; i < weeks1.size(); i++) {
                    TestBugGradeData testBugGradeData = weeks1.get(i);
                    moduleSet.add(testBugGradeData.getModule());

                }
            }


            List<TestBugGradeData> weeks2 = week2.getWeeks();

            if (weeks2 != null && weeks2.size() > 0) {

                for (int i = 0; i < weeks2.size(); i++) {
                    TestBugGradeData testBugGradeData = weeks2.get(i);
                    moduleSet.add(testBugGradeData.getModule());
                }

            }

            List<TestBugGradeData> weeks3 = week3.getWeeks();

            if (weeks3 != null && weeks3.size() > 0) {

                for (int i = 0; i < weeks3.size(); i++) {
                    TestBugGradeData testBugGradeData = weeks3.get(i);
                    moduleSet.add(testBugGradeData.getModule());

                }
            }

            List<TestBugGradeData> weeks4 = week4.getWeeks();
            if (weeks4 != null && weeks4.size() > 0) {
                for (int i = 0; i < weeks4.size(); i++) {
                    TestBugGradeData testBugGradeData = weeks4.get(i);
                    moduleSet.add(testBugGradeData.getModule());
                }
            }

            List<String> xAsix = new ArrayList<>();

            xAsix.addAll(moduleSet);

            TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();

            testBugQualityReportDTO.setyAxisDataResult(yAxisDataResult);

            testBugQualityReportDTO.setTimeType(timeType);
            testBugQualityReportDTO.setxAxisData(xAsix);

            return testBugQualityReportDTO;

        } else {
            List<CltUserDTO> cltUserDTOS = new ArrayList<>();

            Set<String> userIdsSet = new HashSet<>();

            List<TestBugGradeData> weeks1 = week1.getWeeks();
            if (weeks1 != null && weeks1.size() > 0) {

                for (int i = 0; i < weeks1.size(); i++) {
                    TestBugGradeData testBugGradeData = weeks1.get(i);
                    userIdsSet.add(testBugGradeData.getUserId());

                }
            }

            List<TestBugGradeData> weeks2 = week2.getWeeks();
            if (weeks2 != null && weeks2.size() > 0) {

                for (int i = 0; i < weeks2.size(); i++) {
                    TestBugGradeData testBugGradeData = weeks2.get(i);
                    userIdsSet.add(testBugGradeData.getUserId());

                }
            }
            List<TestBugGradeData> weeks3 = week3.getWeeks();
            if (weeks3 != null && weeks3.size() > 0) {

                for (int i = 0; i < weeks3.size(); i++) {
                    TestBugGradeData testBugGradeData = weeks3.get(i);
                    userIdsSet.add(testBugGradeData.getUserId());


                }
            }
            List<TestBugGradeData> weeks4 = week4.getWeeks();
            if (weeks4 != null && weeks4.size() > 0) {

                for (int i = 0; i < weeks4.size(); i++) {
                    TestBugGradeData testBugGradeData = weeks4.get(i);
                    userIdsSet.add(testBugGradeData.getUserId());

                }
            }

            List<String> userIds = new ArrayList<>();

            userIds.addAll(userIdsSet);

            for (int i = 0; i < userIds.size(); i++) {

                CltUserDTO cltUserDTO = new CltUserDTO();

                String userId = userIds.get(i);
                String userChnName = (String) redisUtil.get("username:" + userId);
                cltUserDTO.setUserId(userId);
                cltUserDTO.setUserChnName(userChnName);
                cltUserDTOS.add(cltUserDTO);
            }


            TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();

            testBugQualityReportDTO.setyAxisDataResult(yAxisDataResult);

            testBugQualityReportDTO.setUserDTOList(cltUserDTOS);

            testBugQualityReportDTO.setTimeType(timeType);


            return testBugQualityReportDTO;

        }

    }


    //组装查询缺陷严重程度的数据(按最近7天)
    public TestBugQualityReportDTO getBugGradeDataByDay(TestBugQualityReportQuery
                                                                query, List<CltStsProgress> cltStsProgresses) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String start = sdf.format(query.getStartDate());

        String end = sdf.format(query.getEndDate());

        //遍历输入的开始时间和结束时间
        List<String> times = collectLocalDates(LocalDate.parse(start), LocalDate.parse(end));//输入的开始时间和结束时间

        List<List<TestBugGradeData>> yAxisData = new ArrayList<>();//用来存放y轴的所有的信息

        List<String> modules = new ArrayList<>();//存放模块的信息(x轴的信息)


        //key为TypeName，value为该key为TypeName对应的数据（会有多条重复，因为定时任务每天都会重复生成多条）
        Map<String, List<CltStsProgress>> map = new HashMap<>();

        //遍历数据
        for (int i = 0; i < cltStsProgresses.size(); i++) {

            CltStsProgress cltStsProgress = cltStsProgresses.get(i);

            if (map.containsKey(cltStsProgress.getTypeName())) {

                map.get(cltStsProgress.getTypeName()).add(cltStsProgress);

            } else {

                List<CltStsProgress> list = new ArrayList<>();

                list.add(cltStsProgress);

                map.put(cltStsProgress.getTypeName(), list);
            }

        }


        Iterator entries = map.entrySet().iterator();

        //遍历map，这个map的key为typeName
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();

            //存入map的key
            modules.add((String) entry.getKey());//存入模块的信息

            //这里的value包含所有日期和每次定时任务出现的重复内容
            List<CltStsProgress> cltStsProgressList = (List<CltStsProgress>) entry.getValue();

            //key为日期  value为result，也就是将value以日期来归类，但是这里的value还是有重复（因为定时任务多次执行）
            Map<String, String> cltStsProgressMap = new HashMap<>();

            //遍历并以日期来归类
            for (int i = 0; i < cltStsProgressList.size(); i++) {

                CltStsProgress cltStsProgress = cltStsProgressList.get(i);
                //这里会去掉因定时任务重复的数据
                cltStsProgressMap.put(cltStsProgress.getStsData(), cltStsProgress.getResult());

            }


            List<TestBugGradeData> datas = new ArrayList<>();

               /* for (int i = 0; i < times.size(); i++) {

                    String s = times.get(i);

                    TestBugGradeData testBugGradeData=new TestBugGradeData() ;

                    testBugGradeData.setTime(s);


                    if(cltStsProgressMap.containsKey(s)){

                        testBugGradeData.setResult(cltStsProgressMap.get(s));

                    }else{
                        testBugGradeData.setResult("");


                    }
                    datas.add(testBugGradeData);

                }*/

            List<TestBugGradeData> resultData = getResultData(times, cltStsProgressMap);
            yAxisData.add(resultData);

        }

        TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();

        testBugQualityReportDTO.setyAxisData(yAxisData);

        testBugQualityReportDTO.setxAxisData(modules);

        return testBugQualityReportDTO;


    }


//+++++++++++++++++++++++++++++++++++++++缺陷密度质量统计表(模块)start++++++++++++++++++++++++++++++++
    ///查询用例对应的所有的模块和用例数量

    public List<TestCase> queryTestCaseModuleAndCaseNumByProId(String proId) throws Exception {

        List<TestCase> testCases = null;

        try {

            QualityReportQuery query = new QualityReportQuery();
            query.setProId(proId);
            testCases = manualExecFeignClient.queryTestCaseNumByModule(query).getData();

        } catch (Exception e) {

            logger.info("queryTestCaseModuleAndCaseNumByProId", e);

            throw new GlobalException(ErrorConstants.QUERY_CLT_TEST_CASE_BY_PRO_ID_ERROR_CODE, ErrorConstants.QUERY_CLT_TEST_CASE_BY_PRO_ID_ERROR_MESSAGE);

        }

        return testCases;


    }


    //查询模块对应的缺陷值
    public List<TestBugDTO> queryTestBugNumByModule(String proId) {

        List<TestBugDTO> testBugs = null;

        try {

            testBugs = cltTestBugMapper.queryTestBugNumByModule(proId);

        } catch (Exception e) {

            logger.info("queryTestCaseModuleAndCaseNumByProId", e);

            throw new GlobalException(ErrorConstants.QUERY_CLT_TEST_BUG_NUM_BY_MODULE_ERROR_CODE, ErrorConstants.QUERY_CLT_TEST_BUG_NUM_BY_MODULE__ERROR_MESSAGE);

        }

        return testBugs;


    }


    //组装数据
    public TestBugQualityReportDTO getTestBugQualityReportDTO
    (List<TestCase> testCases, List<TestBugDTO> bugDTOS) {


        List<Double> caseNumdata = new ArrayList<>();//用例的数量

        List<Double> testBugNumdata = new ArrayList<>();//缺陷的值

        List<String> yAxis = new ArrayList<>();//存储模块(用例对应)

        Map<String, Integer> testCaseMap = new HashMap<>();


        for (int i = 0; i < bugDTOS.size(); i++) {

            TestBugDTO testBugDTO = bugDTOS.get(i);

            yAxis.add(testBugDTO.getModule());//y轴对应的参数

            testBugNumdata.add(getTestBugResult(testBugDTO));//缺陷对应的缺陷值

        }

        for (int i = 0; i < testCases.size(); i++) {

            TestCase testCase = testCases.get(i);

            testCaseMap.put(testCase.getModule(), testCase.getCaseNum());


        }

        for (int i = 0; i < yAxis.size(); i++) {
            String s = yAxis.get(i);

            if (testCaseMap.containsKey(s)) {

                caseNumdata.add(testCaseMap.get(s).doubleValue());

            } else {
                caseNumdata.add(0.0);
            }

        }

        TestBugReportData testCase = new TestBugReportData();
        TestBugReportData testBug = new TestBugReportData();
        testCase.setName("测试用例数量");
        testCase.setData(caseNumdata);
        testBug.setName("缺陷值");
        testBug.setData(testBugNumdata);
        List<TestBugReportData> xAxis = new ArrayList<>();

        xAxis.add(testCase);
        xAxis.add(testBug);

        TestBugQualityReportDTO testBugQualityReportDTO = new TestBugQualityReportDTO();

        testBugQualityReportDTO.setxAxis(xAxis);

        testBugQualityReportDTO.setyAxis(yAxis);


        return testBugQualityReportDTO;
    }


    //计算缺陷值得方法
    public double getTestBugResult(TestBugDTO bugDTO) {

        //系数
        BigDecimal levelA = new BigDecimal(5);
        BigDecimal levelB = new BigDecimal(3);
        BigDecimal levelC = new BigDecimal(1);
        BigDecimal levelD = new BigDecimal(0.5);
        BigDecimal levelE = new BigDecimal(0.2);
        //缺陷数量
        int testBugA = bugDTO.getCaseA();
        int testBugB = bugDTO.getCaseB();
        int testBugC = bugDTO.getCaseC();
        int testBugD = bugDTO.getCaseD();
        int testBugE = bugDTO.getCaseE();

        BigDecimal caseA = new BigDecimal(testBugA);
        BigDecimal caseB = new BigDecimal(testBugB);
        BigDecimal caseC = new BigDecimal(testBugC);
        BigDecimal caseD = new BigDecimal(testBugD);
        BigDecimal caseE = new BigDecimal(testBugE);

        double bugNumResult = levelA.multiply(caseA).add(levelB.multiply(caseB)).add(levelC.multiply(caseC)).add(levelD.multiply(caseD)).add(levelE.multiply(caseE)).doubleValue();

        return bugNumResult;


    }
//+++++++++++++++++++++++++++++++++++++++缺陷密度质量统计表(模块)end++++++++++++++++++++++++++++++++


    public List<String> collectLocalDates(LocalDate start, LocalDate end) {
        // 用起始时间作为流的源头，按照每次加一天的方式创建一个无限流
        return Stream.iterate(start, localDate -> localDate.plusDays(1))
                // 截断无限流，长度为起始时间和结束时间的差+1个
                .limit(ChronoUnit.DAYS.between(start, end) + 1)
                // 由于最后要的是字符串，所以map转换一下
                .map(LocalDate::toString)
                // 把流收集为List
                .collect(Collectors.toList());
    }


    //转化成String
    public String getBugResultByGrade(TestBugDTO testBugDTO) {

        Integer caseA = testBugDTO.getCaseA();
        Integer caseB = testBugDTO.getCaseB();
        Integer caseC = testBugDTO.getCaseC();
        Integer caseD = testBugDTO.getCaseD();
        Integer caseE = testBugDTO.getCaseE();
        Integer total = testBugDTO.getCaseTotal();

        String result = "{\"A\":\"" + caseA +
                "\",\"B\":\"" + caseB +
                "\",\"C\":\"" + caseC +
                "\",\"D\":\"" + caseD +
                "\",\"E\":\"" + caseE +
                "\",\"total\":\"" + total + "\"}";

        return result;

    }

    //转化成String
    public String getBugResultByStatus(TestBugDTO testBugDTO) {

        Integer case1 = testBugDTO.getCase1();
        Integer case2 = testBugDTO.getCase2();
        Integer case3 = testBugDTO.getCase3();
        Integer case4 = testBugDTO.getCase4();
        Integer case5 = testBugDTO.getCase5();
        Integer case6 = testBugDTO.getCase6();
        Integer case7 = testBugDTO.getCase7();
        Integer case8 = testBugDTO.getCase8();
        Integer case9 = testBugDTO.getCase9();
        Integer case10 = testBugDTO.getCase10();
        Integer case11 = testBugDTO.getCase11();
        Integer total = testBugDTO.getCaseTotal();
        String result = "{\"1\":\"" + case1 +
                "\",\"2\":\"" + case2 +
                "\",\"3\":\"" + case3 +
                "\",\"4\":\"" + case4 +
                "\",\"5\":\"" + case5 +
                "\",\"6\":\"" + case6 +
                "\",\"7\":\"" + case7 +
                "\",\"8\":\"" + case8 +
                "\",\"9\":\"" + case9 +
                "\",\"10\":\"" + case10 +
                "\",\"11\":\"" + case11 +
                "\",\"total\":\"" + total + "\"}";

        return result;

    }


    //获取前几个月信息
    public String getTime(Integer monthNum) {

        Calendar calendar = Calendar.getInstance();

        Date date = calendar.getTime();

        calendar.add(Calendar.MONTH, -monthNum);

        return calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月";

    }


    @Autowired
    ProjectFeignClient projectFeignClient;

    /**
     * 查询该父项目下所有子项目的所有高级缺陷总和
     *
     * @param query
     * @return
     */
    @Override
    public Page<TestBugDTO> selectHighTestBugForParentProj(TestBugQuery query) {
        //调用feign查询子项目集合
        CltProjectQuery projectQuery = new CltProjectQuery();
        projectQuery.setProId(query.getProId());
        List<CltProjectDto> sonList = projectFeignClient.findSonProjectList(projectQuery).getData();

        //提取子项目id集合
        List<String> sonIdList = new ArrayList<>();
        sonList.forEach(son -> sonIdList.add(son.getProId()));

        //封装参数
        query.setProIds(sonIdList);

        return selectHighTestBugByProIds(query);

    }


    //获取缺陷序号
    public String getTestBugOrderNum(Integer orderNum) {

        String code = orderNum + "";
        int leng = (code.trim()).length();  //定义长度

        if (leng == 1) {
            code = "0000" + orderNum;
        } else if (leng == 2) {

            code = "000" + orderNum;
        } else if (leng == 3) {

            code = "00" + orderNum;
        } else if (leng == 4) {

            code = "0" + orderNum;
        }

        return code;


    }


    /**
     * 组装返回数据
     *
     * @param testBugDTO
     * @return
     */
    public TestBugDTO sendDataResult(TestBugDTO testBugDTO) {

        if (!StringUtils.isEmpty(testBugDTO.getProId())) {

            //缺陷ID Defect+缺陷序号

            String testBugOrderNum = getTestBugOrderNum(testBugDTO.getOrderNum());

            String bugForm = testBugDTO.getBugSycType();
            if (bugForm.equals("1")) {//判断是运营端缺陷还是客户端缺陷
                testBugDTO.setTestBugId("Defect-ZX-" + testBugOrderNum);
            } else {
                testBugDTO.setTestBugId("Defect-C-" + testBugOrderNum);
            }

        }
        if (!StringUtils.isEmpty(testBugDTO.getReceiver())) {

            //调用查询分配给
            String receive = (String) redisUtil.get("username:" + testBugDTO.getReceiver());

            testBugDTO.setReceiverName(receive);

        }
        /*if(StringUtils.isNotEmpty(testBugDTO.getBugTitle())){
            //缺陷标题:缺陷模块名称-轮次名称-缺陷概述缺陷
           //调用根据伦次Id查询伦次详情查询伦次详情
            CltRoundQuery cltRoundQuery=new CltRoundQuery();

            cltRoundQuery.setRoundId(testBugDTO.getRoundId());

            ResultInfo<CltRound> round = cltRoundFeignClient.round(cltRoundQuery);

            String roundName=null;

            CltRound  cltRound= round.getData();
            if(cltRound==null){
                roundName="轮次";
            }else {
                roundName=cltRound.getRoundName();
            }


            //组装缺陷标题返回的参数
             String  titleName=testBugDTO.getModule()+"-"+roundName+"-"+testBugDTO.getBugTitle();

            testBugDTO.setBugTitleName(titleName);

            testBugDTO.setRoundName(roundName);

        }
*/
        if (!StringUtils.isEmpty(testBugDTO.getCreateUser())) {
            //调用查询缺陷发现人

            String createUserName = (String) redisUtil.get("username:" + testBugDTO.getCreateUser());
            if (StringUtils.isEmpty(createUserName)) {
                testBugDTO.setCreateUserName(testBugDTO.getCreateUser());
            } else {
                testBugDTO.setCreateUserName(createUserName);
            }

        }

        if (!StringUtils.isEmpty(testBugDTO.getCaseId())) {
            //调用查询轮次详情的方法
            TestCaseQuery testCaseQuery = new TestCaseQuery();
            testCaseQuery.setId(testBugDTO.getCaseId());
            try {

                ResultInfo<TestCaseQuery> testCaseById = testCaseFeignClient.findTestCaseById(testCaseQuery);
                if (testCaseById != null && testCaseById.getData() != null) {
                    testBugDTO.setCaseName(testCaseById.getData().getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new GlobalException("", "调用测试用例微服务失败！");
            }

        }
        //信息记录表
        if (testBugDTO.getCltBugLogs() != null && testBugDTO.getCltBugLogs().size() > 0) {
            List<CltBugLog> cltBugLogs = testBugDTO.getCltBugLogs();
            for (int i = 0; i < cltBugLogs.size(); i++) {
                CltBugLog cltBugLog = cltBugLogs.get(i);

                String modifierName = (String) redisUtil.get("username:" + cltBugLog.getModifier());

                cltBugLog.setModifierName(modifierName);
            }

        }
        //操作记录表
        if (testBugDTO.getCltBugRecords() != null && testBugDTO.getCltBugRecords().size() > 0) {

            List<CltBugRecord> cltBugRecords = testBugDTO.getCltBugRecords();
            for (int i = 0; i < cltBugRecords.size(); i++) {

                CltBugRecord cltBugRecord = cltBugRecords.get(i);

                String modifierName = (String) redisUtil.get("username:" + cltBugRecord.getModifier());

                cltBugRecord.setModifierName(modifierName);

            }

        }


        return testBugDTO;
    }


    /**
     * 查询高级缺陷列表
     *
     * @param query
     * @return
     */
    @Override
    public Page<TestBugDTO> selectHighTestBugByProIds(TestBugQuery query) {
        //封装条件，查询高级缺陷列表
        //缺陷等级
        List<String> bugGrades = new ArrayList<>();
        bugGrades.add("A");
        bugGrades.add("B");
        query.setBugGrades(bugGrades);

        //缺陷状态：未解决
        String[] statuses = {"1", "2", "3", "4", "5", "6", "7", "8", "11"};
        List<String> statusList = Arrays.asList(statuses);
        query.setStatuses(statusList);


        PageUtil.startPage(query);
        List<TestBugDTO> testBugDTOS = cltTestBugMapper.queryTestBugByProIds(query);

        testBugDTOS.forEach(this::sendDataResult);

        return new Page<>(testBugDTOS);

    }


    //获取日期对应的result
    private List<TestBugGradeData> getResultData(List<String> timeList, Map<String, String> map) throws
            Exception {

        List<TestBugGradeData> datas = new ArrayList<>();

        String s = "";
        for (int i = 0; i < timeList.size(); i++) {
            String time = timeList.get(i);

            TestBugGradeData testBugGradeData = new TestBugGradeData();

            testBugGradeData.setTime(time);
            if (i == 0) {
                if (map.containsKey(time)) {
                    testBugGradeData.setResult(map.get(time));
                    s = map.get(time);
                } else {
                    testBugGradeData.setResult(s);
                }
            } else {
                if (map.containsKey(time)) {
                    testBugGradeData.setResult(map.get(time));
                    s = map.get(time);
                } else {
                    testBugGradeData.setResult(s);
                }

            }
            datas.add(testBugGradeData);
        }

        return datas;
    }


}
