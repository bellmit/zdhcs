package net.northking.cloudtest.service.impl;

import net.northking.cloudtest.dao.analyse.DemandMapper;
import net.northking.cloudtest.dao.analyse.TestCaseMapper;
import net.northking.cloudtest.domain.analyse.Demand;
import net.northking.cloudtest.domain.analyse.DemandExample;
import net.northking.cloudtest.domain.analyse.TestCase;
import net.northking.cloudtest.dto.report.CoverageReportDTO;
import net.northking.cloudtest.dto.report.CoverageReportData;
import net.northking.cloudtest.query.report.CoverageReportQuery;
import net.northking.cloudtest.service.CoverageReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description: 覆盖报告统计报表
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-05-08 15:16
 * @UpdateUser:
 * @Version:0.1
 */

@Service
public class CoverageReportServiceImpl implements CoverageReportService {


    //日志
    private final static Logger logger = LoggerFactory.getLogger(CoverageReportServiceImpl.class);

    @Autowired
    private DemandMapper demandMapper;


    @Autowired
    private TestCaseMapper testCaseMapper;


   //需求分析/用例覆盖报告-交易/模块
    @Override
    public CoverageReportDTO testcaseDemandCoverageReport(CoverageReportQuery coverageReportQuery){

        String type=coverageReportQuery.getType();


        try{

            if("T".equals(type)){

                List<Demand> demandList=demandMapper.queryDemandNumByTrade(coverageReportQuery.getProId());

                List<TestCase> testCaseList=testCaseMapper.queryTestCaseNumByTrade(coverageReportQuery.getProId());

                //组装数据
                CoverageReportDTO coverageReportDTOData = getCoverageReportDTOData(demandList, testCaseList,"T");

                 return coverageReportDTOData;

            }else{

                List<Demand> demandList = getDemandModule(coverageReportQuery.getProId());

                List<TestCase> testCaseList=testCaseMapper.queryTestCaseNumByModule(coverageReportQuery.getProId());

                //组装数据
                CoverageReportDTO coverageReportDTOData = getCoverageReportDTOData(demandList, testCaseList,"M");

                return coverageReportDTOData;


            }

        }catch (Exception e){

            logger.info("getTestCaseDemandCoverageData",e);

        }


      return  null;

    }


    //组装数据
    public CoverageReportDTO getCoverageReportDTOData(List<Demand> demandList, List<TestCase> testCaseList,String funcCode){

        List<CoverageReportData> coverageReportDatas=new ArrayList<>();//存储需求和测试对应的数量的集合

        List<Integer> demandData=new ArrayList<>();//存储需求数量

        List<Integer> testCaseData=new ArrayList<>();//存储测试数量

        List<String> xAxis=new ArrayList<>();//x轴数据

        Map<String,Integer>  map=new HashMap<>();//存储测试用例数据

        if("T".equals(funcCode)){
            for (int i = 0; i < demandList.size(); i++) {
                Demand demand =  demandList.get(i);
                xAxis.add(demand.getName());//x轴数据
                demandData.add(demand.getPointNum());//x轴对应的参数
            }

            for (int i = 0; i < testCaseList.size(); i++) {
                TestCase testCase = testCaseList.get(i);
                map.put(testCase.getTestItem(),testCase.getCaseNum());
            }

            for (int i = 0; i < xAxis.size(); i++) {
                String s =  xAxis.get(i);

                if(map.containsKey(s)){

                    testCaseData.add(map.get(s));

                }else{

                    testCaseData.add(1);
                }

            }

        }else{
            for (int i = 0; i < testCaseList.size(); i++) {
                TestCase testCase = testCaseList.get(i);
                xAxis.add(testCase.getModule());//x轴数据
                testCaseData.add(testCase.getCaseNum());//x轴对应的参数
            }

            for (int i = 0; i < demandList.size(); i++) {
                Demand demand = demandList.get(i);
                map.put(demand.getName(),demand.getPointNum());
            }

            for (int i = 0; i < xAxis.size(); i++) {
                String s =  xAxis.get(i);

                if(map.containsKey(s)){

                    demandData.add(map.get(s));

                }else{

                    demandData.add(1);
                }

            }

        }



        CoverageReportData test=new CoverageReportData();

        test.setName("测试用例数量");

        test.setData(testCaseData);

        CoverageReportData demand=new CoverageReportData();

        demand.setName("需求分析数量");
        demand.setData(demandData);

        coverageReportDatas.add(test);
        coverageReportDatas.add(demand);

        CoverageReportDTO coverageReportDTO=new CoverageReportDTO();

        coverageReportDTO.setxAxis(xAxis);

        coverageReportDTO.setyAxis(coverageReportDatas);


         return coverageReportDTO;


        }


    //需求分析每日进度统计(按模块)
    public List<Demand> getDemandModule(String proId){

        DemandExample example = new DemandExample();
        DemandExample.Criteria criteria = example.createCriteria();
        criteria.andLevelNoEqualTo(3);
        criteria.andLogicDelEqualTo("N");
        criteria.andProjectIdEqualTo(proId);
        example.setOrderByClause("CREATE_TIME ASC");
        List<Demand> demandList = demandMapper.selectByExample(example);

        //还需要列出LevelNo为2且是叶子节点的
        DemandExample leafExample = new DemandExample();
        leafExample.setOrderByClause("CREATE_TIME ASC");
        DemandExample.Criteria leafCriteria = leafExample.createCriteria();
        leafCriteria.andProjectIdEqualTo(proId);
        leafCriteria.andLogicDelEqualTo("N");
        leafCriteria.andLevelNoEqualTo(2);
        leafCriteria.andLeafEqualTo("Y");

        List<Demand> demandLeafList = demandMapper.selectByExample(leafExample);
        demandList.addAll(demandLeafList);

        for (int i = 0; i < demandList.size(); i++) {
            Demand demand =  demandList.get(i);

            Integer pointNum = demandMapper.queryDemandNumByModule(demand.getPath());

            demand.setPointNum(pointNum);

        }

        return demandList;

    }


}
