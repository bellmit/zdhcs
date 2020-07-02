package net.northking.cloudtest.service.impl;

import net.northking.cloudtest.assist.RedisUtil;
import net.northking.cloudtest.dao.analyse.CltMapNodeMapper;
import net.northking.cloudtest.dao.analyse.DemandMapper;
import net.northking.cloudtest.dao.analyse.TestCaseMapper;
import net.northking.cloudtest.dao.task.CltTestcaseExecuteMapper;
import net.northking.cloudtest.domain.analyse.Demand;
import net.northking.cloudtest.domain.analyse.DemandExample;
import net.northking.cloudtest.dto.analyse.MapNodeNumDTO;
import net.northking.cloudtest.dto.report.DemandMapNodeDTO;
import net.northking.cloudtest.dto.report.QualityReportDTO;
import net.northking.cloudtest.dto.report.TestCaseExecuteReportDTO;
import net.northking.cloudtest.dto.report.TestCaseReportDTO;
import net.northking.cloudtest.dto.user.CltUserDTO;
import net.northking.cloudtest.feign.execute.ManualExecFeignClient;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.QualityReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @Title:
 * @Description: 质量报告逻辑层实现类
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-09 9:14
 * @UpdateUser:
 * @Version:0.1
 */

@Service
public class QualityReportServiceImpl implements QualityReportService {


    @Autowired
    private CltTestcaseExecuteMapper cltTestcaseExecuteMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Autowired
    private CltMapNodeMapper cltMapNodeMapper;


    @Autowired
    private DemandMapper demandMapper;

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Autowired
    private ManualExecFeignClient manualExecFeignClient;


    //日志
    private final static Logger logger = LoggerFactory.getLogger(QualityReportServiceImpl.class);

    //查询测试用例设计质量报告
    @Override
    public QualityReportDTO queryTestCaseQualityReport(QualityReportQuery query) throws Exception {

        //按人员

        List<TestCaseReportDTO> reportDTOUserList = getTestCaseReportData(query, "U");
        //按模块
        List<TestCaseReportDTO> reportDTOModuleList = getTestCaseReportData(query, "M");
        //总数
        List<TestCaseReportDTO> reportDTOTotalList = getTestCaseReportData(query, "G");

        QualityReportDTO qualityReportDTO = getTestCaseReport(query, reportDTOUserList, reportDTOModuleList, reportDTOTotalList);

        return qualityReportDTO;
    }

    /**
     * 需求分析完成情况
     *
     * @param query
     * @return
     * @throws Exception
     */
    @Override
    public QualityReportDTO queryDemandQualityReport(QualityReportQuery query) throws Exception {

        //按人员
        List<DemandMapNodeDTO> userList = getDemandAnalyseData(query, "U");
        //按模块
        List<DemandMapNodeDTO> moduleList = getDemandAnalyseData(query, "M");
        //总数
        DemandMapNodeDTO demandMapNodeDTO = cltMapNodeMapper.queryMapNodeOnAllByTime(query);

        QualityReportDTO detailList = getDetailList(query, userList, moduleList, demandMapNodeDTO);

        return detailList;
    }

    //用例执行完成情况
    @Override
    public QualityReportDTO queryTestExecuteQualityReport(QualityReportQuery query) throws Exception {

        if ("U".equals(query.getType())) {
            List<TestCaseExecuteReportDTO> userList = getTestExecuteData(query, "U");

            QualityReportDTO executeQualityReportDTO = getExecuteQualityReportDTO(userList, "U");

            executeQualityReportDTO.setStartDate(query.getStartDate());
            executeQualityReportDTO.setEndDate(query.getEndDate());
            return executeQualityReportDTO;

        } else {

            //查询按模块用例设计进度
            List<TestCaseExecuteReportDTO> userList = getTestExecuteData(query, "M");

            QualityReportDTO executeQualityReportDTO = getExecuteQualityReportDTO(userList, "M");

            executeQualityReportDTO.setStartDate(query.getStartDate());
            executeQualityReportDTO.setEndDate(query.getEndDate());
            return executeQualityReportDTO;
        }


    }

    //用例设计完成情况
    public List<TestCaseReportDTO> getTestCaseReportData(QualityReportQuery qualityReportQuery, String type) {


        try {

            if ("U".equals(type)) {
                List<TestCaseReportDTO> testCaseReportUserList = testCaseMapper.queryCaseReportByUserByTime(qualityReportQuery);

                return testCaseReportUserList;
            }

            if ("M".equals(type)) {
                List<TestCaseReportDTO> testCaseReportModuleList = testCaseMapper.queryCaseReportByModuleByTime(qualityReportQuery);

                return testCaseReportModuleList;
            }


            if ("G".equals(type)) {

                List<TestCaseReportDTO> testCaseReporTotalList = testCaseMapper.queryCaseReportByAllByTime(qualityReportQuery);

                return testCaseReporTotalList;

            }

        } catch (Exception e) {

            e.printStackTrace();
            logger.info("getTestCaseReportData", e);

        }

        return null;
    }


    //用例执行完成情况
    private List<TestCaseExecuteReportDTO> getTestExecuteData(QualityReportQuery qualityReportQuery, String type) {

        try {

            if ("U".equals(type)) {

                //  List<TestCaseExecuteReportDTO> testCaseExecuteReportDTOS = cltTestcaseExecuteMapper.queryCaseExecuteDataByUser(qualityReportQuery);

                ResultInfo<List<TestCaseExecuteReportDTO>> testCaseExecuteReportDTOS = manualExecFeignClient.queryCaseExecDataByUser(qualityReportQuery);

                return testCaseExecuteReportDTOS.getData();
            } else {

                //List<TestCaseExecuteReportDTO> testCaseExecuteReportDTOS = cltTestcaseExecuteMapper.queryCaseExecuteDataByModule(qualityReportQuery);

                ResultInfo<List<TestCaseExecuteReportDTO>> testCaseExecuteReportDTOS = manualExecFeignClient.queryCaseExecDataByModule(qualityReportQuery);

                return testCaseExecuteReportDTOS.getData();

            }

        } catch (Exception e) {

            e.printStackTrace();
            logger.info("getTestExecuteData", e);

        }


        return null;


    }


    //用例执行完成情况组装数据
    public QualityReportDTO getExecuteQualityReportDTO(List<TestCaseExecuteReportDTO> caseExecuteReportDTOList, String type) {

        if ("U".equals(type)) {

            List<String> userResults = new ArrayList<>();

            List<String> userIds = new ArrayList<>();
            List<String> userNames = new ArrayList<>();

            List<CltUserDTO> userDTOList = new ArrayList<>();

            if (caseExecuteReportDTOList != null && caseExecuteReportDTOList.size() > 0) {
                for (int i = 0; i < caseExecuteReportDTOList.size(); i++) {

                    TestCaseExecuteReportDTO testCaseExecuteReportDTO = caseExecuteReportDTOList.get(i);

                    String receiveUser = testCaseExecuteReportDTO.getReceiveUser();
                    if (StringUtils.isEmpty(receiveUser)) {
                        continue;
                    }
                    userIds.add(receiveUser);

                    userResults.add(toString(testCaseExecuteReportDTO));

                }

            }
            for (int i = 0; i < userIds.size(); i++) {

                String userId = userIds.get(i);
                String userChnName = (String) redisUtil.get("username:" + userId);
                userNames.add(userChnName);

            }

            QualityReportDTO qualityReportDTO = new QualityReportDTO();

            qualityReportDTO.setxAxis(userNames);

            qualityReportDTO.setyAxis(userResults);

            return qualityReportDTO;

        } else {

            List<String> moduleResults = new ArrayList<>();

            List<String> modules = new ArrayList<>();

            if (caseExecuteReportDTOList != null && caseExecuteReportDTOList.size() > 0) {
                for (int i = 0; i < caseExecuteReportDTOList.size(); i++) {

                    TestCaseExecuteReportDTO testCaseExecuteReportDTO = caseExecuteReportDTOList.get(i);

                    modules.add(testCaseExecuteReportDTO.getModule());

                    moduleResults.add(toString(testCaseExecuteReportDTO));

                }
            }
            QualityReportDTO qualityReportDTO = new QualityReportDTO();

            qualityReportDTO.setxAxis(modules);

            qualityReportDTO.setyAxis(moduleResults);

            return qualityReportDTO;


        }


    }


    //需求分析完成情况
    public List<DemandMapNodeDTO> getDemandAnalyseData(QualityReportQuery qualityReportQuery, String type) {


        try {

            if ("U".equals(type)) {
                List<DemandMapNodeDTO> demandMapNodeDTOS = cltMapNodeMapper.queryMapNodeOnUserByTime(qualityReportQuery);

                return demandMapNodeDTOS;
            }

            if ("M".equals(type)) {

                List<DemandMapNodeDTO> demandMapNodeDTOS = new ArrayList<>();
                //查询所有的模块
                DemandExample example = new DemandExample();
                DemandExample.Criteria criteria = example.createCriteria();
                criteria.andLevelNoEqualTo(3);
                criteria.andLogicDelEqualTo("N");
                criteria.andProjectIdEqualTo(qualityReportQuery.getProId());
                example.setOrderByClause("CREATE_TIME ASC");
                List<Demand> demandList = demandMapper.selectByExample(example);

                //还需要列出LevelNo为2且是叶子节点的
                DemandExample leafExample = new DemandExample();
                leafExample.setOrderByClause("CREATE_TIME ASC");
                DemandExample.Criteria leafCriteria = leafExample.createCriteria();
                leafCriteria.andProjectIdEqualTo(qualityReportQuery.getProId());
                leafCriteria.andLogicDelEqualTo("N");
                leafCriteria.andLevelNoEqualTo(2);
                leafCriteria.andLeafEqualTo("Y");

                List<Demand> demandLeafList = demandMapper.selectByExample(leafExample);
                demandList.addAll(demandLeafList);

                if (demandList != null && demandList.size() > 0) {
                    for (int i = 0; i < demandList.size(); i++) {
                        Demand demand = demandList.get(i);
                        qualityReportQuery.setPath(demand.getPath());
                        DemandMapNodeDTO demandMapNodeDTO = cltMapNodeMapper.queryMapNodeOnModuleByTime(qualityReportQuery);
                        demandMapNodeDTO.setModule(demand.getName());
                        demandMapNodeDTOS.add(demandMapNodeDTO);
                    }

                }

                return demandMapNodeDTOS;
            }


        } catch (Exception e) {

            e.printStackTrace();

            logger.info("getdemandAnalyseData", e);
        }
        return null;


    }


    //组装数据
    private QualityReportDTO getDetailList(QualityReportQuery qualityReportQuery, List<DemandMapNodeDTO> userList, List<DemandMapNodeDTO> moduleList, DemandMapNodeDTO mapNodeDTO) {

        QualityReportDTO quality = new QualityReportDTO();
        //用例完成柱状图按人员
        List<Integer> xUserAxis = new ArrayList<>();
        List<String> yUserAxis = new ArrayList<>();
        //用例完成饼图按人员
        List<MapNodeNumDTO> userChart = new ArrayList<>();


        if (userList != null && userList.size() > 0) {
            for (int i = 0; i < userList.size(); i++) {
                DemandMapNodeDTO demandMapNodeDTO = userList.get(i);
                xUserAxis.add(demandMapNodeDTO.getDone());
                String userName = (String) redisUtil.get("username:" + demandMapNodeDTO.getLastModifier());
                yUserAxis.add(userName);
                MapNodeNumDTO dto = new MapNodeNumDTO();
                dto.setStatus(userName);
                dto.setNodeNum(demandMapNodeDTO.getDone());
                userChart.add(dto);
            }
        }
        //用例完成柱状图按模块
        List<Integer> xModuleAxis = new ArrayList<>();
        List<String> yModuleAxis = new ArrayList<>();
        //用例完成饼图按模块
        List<MapNodeNumDTO> moduleChart = new ArrayList<>();

        if (moduleList != null && moduleList.size() > 0) {
            for (int i = 0; i < moduleList.size(); i++) {
                DemandMapNodeDTO demandMapNodeDTO = moduleList.get(i);
                yModuleAxis.add(demandMapNodeDTO.getModule());
                xModuleAxis.add(demandMapNodeDTO.getDone());
                MapNodeNumDTO dto = new MapNodeNumDTO();
                dto.setStatus(demandMapNodeDTO.getModule());
                dto.setNodeNum(demandMapNodeDTO.getDone());
                moduleChart.add(dto);
            }
        }

        Integer totalAll = 0;
        if (mapNodeDTO != null) {

            totalAll = mapNodeDTO.getDone();
        }
        quality.setTotal(totalAll);
        quality.setxUserAxis(xUserAxis);
        quality.setyUserAxis(yUserAxis);
        quality.setUserChart(userChart);
        quality.setxModuleAxis(xModuleAxis);
        quality.setyModuleAxis(yModuleAxis);
        quality.setModuleChart(moduleChart);
        quality.setStartDate(qualityReportQuery.getStartDate());
        quality.setEndDate(qualityReportQuery.getEndDate());
        return quality;

    }


    //用例设计完成情况组装数据
    public QualityReportDTO getTestCaseReport(QualityReportQuery qualityReportQuery, List<TestCaseReportDTO> reportDTOUserList, List<TestCaseReportDTO> reportDTOModuleList, List<TestCaseReportDTO> reportDTOTotalList) {

        QualityReportDTO quality = new QualityReportDTO();
        //用例完成柱状图按人员
        List<Integer> xUserAxis = new ArrayList<>();
        List<String> yUserAxis = new ArrayList<>();
        //用例完成饼图按人员
        List<MapNodeNumDTO> userChart = new ArrayList<>();


        if (reportDTOUserList != null && reportDTOUserList.size() > 0) {
            for (int i = 0; i < reportDTOUserList.size(); i++) {
                TestCaseReportDTO testCaseReportDTO = reportDTOUserList.get(i);
                xUserAxis.add(testCaseReportDTO.getSs());
                String userName = (String) redisUtil.get("username:" + testCaseReportDTO.getLastModifier());
                yUserAxis.add(userName);
                MapNodeNumDTO dto = new MapNodeNumDTO();
                dto.setStatus(userName);
                dto.setNodeNum(testCaseReportDTO.getSs());
                userChart.add(dto);
            }
        }
        //用例完成柱状图按模块
        List<Integer> xModuleAxis = new ArrayList<>();
        List<String> yModuleAxis = new ArrayList<>();
        //用例完成饼图按模块
        List<MapNodeNumDTO> moduleChart = new ArrayList<>();

        if (reportDTOModuleList != null && reportDTOModuleList.size() > 0) {
            for (int i = 0; i < reportDTOModuleList.size(); i++) {
                TestCaseReportDTO testCaseReportDTO = reportDTOModuleList.get(i);
                yModuleAxis.add(testCaseReportDTO.getModule());
                xModuleAxis.add(testCaseReportDTO.getSs());
                MapNodeNumDTO dto = new MapNodeNumDTO();
                dto.setStatus(testCaseReportDTO.getModule());
                dto.setNodeNum(testCaseReportDTO.getSs());
                moduleChart.add(dto);
            }
        }

        Integer totalAll = 0;
        if (reportDTOTotalList != null && reportDTOTotalList.size() > 0) {

            totalAll = reportDTOTotalList.get(0).getSs();
        }
        quality.setTotal(totalAll);
        quality.setxUserAxis(xUserAxis);
        quality.setyUserAxis(yUserAxis);
        quality.setUserChart(userChart);
        quality.setxModuleAxis(xModuleAxis);
        quality.setyModuleAxis(yModuleAxis);
        quality.setModuleChart(moduleChart);
        quality.setStartDate(qualityReportQuery.getStartDate());
        quality.setEndDate(qualityReportQuery.getEndDate());
        return quality;


    }


    //转成json字符串
    public String toString(TestCaseExecuteReportDTO testCaseExecuteReportDTO) {

        Integer execute0 = testCaseExecuteReportDTO.getExecute0();
        Integer execute1 = testCaseExecuteReportDTO.getExecute1();
        Integer execute2 = testCaseExecuteReportDTO.getExecute2();
        Integer execute3 = testCaseExecuteReportDTO.getExecute3();
        Integer execute4 = testCaseExecuteReportDTO.getExecute4();
        Integer execute5 = testCaseExecuteReportDTO.getExecute5();
        Integer execute6 = testCaseExecuteReportDTO.getExecute6();
        Integer exeTotal = testCaseExecuteReportDTO.getExeTotal();

        return "{\"0\":\"" + execute0 +
                "\",\"1\":\"" + execute1 +
                "\",\"2\":\"" + execute2 +
                "\",\"3\":\"" + execute3 +
                "\",\"4\":\"" + execute4 +
                "\",\"5\":\"" + execute5 +
                "\",\"6\":\"" + execute6 +
                "\",\"total\":\"" + exeTotal + "\"}";
    }


}
