package net.northking.cloudtest.service.impl.report;

import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.dto.analyse.MapNodeNumDTO;
import net.northking.cloudtest.dto.report.QualityReportDTO;
import net.northking.cloudtest.feign.report.QualityReportFeignClient;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenCompleteChartService;
import net.northking.cloudtest.utils.WordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * Created by liujinghao on 2018/5/20.
 */
@Service
public class GenCompleteChartServiceImpl implements GenCompleteChartService {

    private final static Logger logger = LoggerFactory.getLogger(GenCompleteChartServiceImpl.class);

    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;


    @Autowired
    private QualityReportFeignClient qualityReportFeignClient;

    @Autowired
    private CltProjectMapper cltProjectMapper;

    /**
     * 生成完成情况图表
     *
     * @param proId
     * @return
     * @throws Exception
     */
    @Override
    public int GenCompleteChart(String proId) throws Exception {
        Runtime run = Runtime.getRuntime();

        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int num = 0;
        //查出项目
        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(proId);
        //需求分析完成情况(整体)
        QualityReportQuery qualityReportQuery = new QualityReportQuery();
        qualityReportQuery.setProId(proId);
        qualityReportQuery.setStartDate(cltProject.getCreateDate());
        qualityReportQuery.setEndDate(new Date());
        ResultInfo<QualityReportDTO> qualityReportDTOResultInfo = qualityReportFeignClient.queryDemandQualityReport(qualityReportQuery);
        if (qualityReportDTOResultInfo != null && qualityReportDTOResultInfo.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            List<MapNodeNumDTO> userChart = qualityReportDTOResultInfo.getData().getUserChart();
            if (userChart != null && userChart.size() > 0) {
                dataMap.put("userChart", ojectToJsonString(userChart));
            } else {
                dataMap.put("userChart", "[]");
            }
            List<MapNodeNumDTO> moduleChart = qualityReportDTOResultInfo.getData().getModuleChart();
            if (moduleChart != null && moduleChart.size() > 0) {
                dataMap.put("moduleChart", ojectToJsonString(moduleChart));
            } else {
                dataMap.put("moduleChart", "[]");
            }
            List<String> yUserAxis = qualityReportDTOResultInfo.getData().getyUserAxis();
            List<Integer> xUserAxis = qualityReportDTOResultInfo.getData().getxUserAxis();
            List<Integer> UserAxis1 = new ArrayList<>();
            if (yUserAxis != null && yUserAxis.size() > 0) {
                dataMap.put("xUserAxis", listToString(xUserAxis));
                Integer userTotal = 0;
                Integer maxNum = 0;
                for (int i = 0; i < xUserAxis.size(); i++) {

                    if (xUserAxis.get(i) > maxNum) {
                        maxNum = xUserAxis.get(i);
                    }
                }

                for (int i = 0; i < xUserAxis.size(); i++) {
                    UserAxis1.add(maxNum - xUserAxis.get(i));
                }
                for (Integer xu : xUserAxis
                        ) {
                    userTotal += xu;
                }
                dataMap.put("userTotal", userTotal);
                dataMap.put("xUserAxis1", listToString(UserAxis1));
                dataMap.put("yUserAxis", listChangeToString(yUserAxis));
            } else {
                dataMap.put("xUserAxis1", "[]");
                dataMap.put("xUserAxis", "[]");
                dataMap.put("yUserAxis", "[]");
            }
            List<Integer> xModuleAxis = qualityReportDTOResultInfo.getData().getxModuleAxis();
            List<String> yModuleAxis = qualityReportDTOResultInfo.getData().getyModuleAxis();
            List<Integer> xModuleAxis1 = new ArrayList<>();
            if (xModuleAxis != null && xModuleAxis.size() > 0) {
                Integer moduleTotal = 0;
                for (Integer xu : xModuleAxis
                        ) {
                    moduleTotal += xu;
                }
                dataMap.put("xModuleAxis", listToString(xModuleAxis));
               /* System.out.println("xModuleAxis=" + dataMap.get("xModuleAxis"));*/
                Integer maxNum = 0;
                for (int i = 0; i < xModuleAxis.size(); i++) {
                    if (xModuleAxis.get(i) > maxNum) {
                        maxNum = xModuleAxis.get(i);
                    }
                }
                for (int i = 0; i < xModuleAxis.size(); i++) {
                    xModuleAxis1.add(maxNum - xModuleAxis.get(i));
                }
                dataMap.put("moduleTotal", moduleTotal);
                dataMap.put("xModuleAxis1", listToString(xModuleAxis1));
                dataMap.put("yModuleAxis", listChangeToString(yModuleAxis));
            } else {
                dataMap.put("xModuleAxis1", "[]");
                dataMap.put("xModuleAxis", "[]");
                dataMap.put("yModuleAxis", "[]");
            }
            wordUtil.createWord("complete.json", preFilePath + qualityReportQuery.getProId() + "/json/demandComplete.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + qualityReportQuery.getProId() + "/json/demandComplete.json " + preFilePath + qualityReportQuery.getProId() + "/images/demandComplete.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        //当日需求分析完成情况
        QualityReportQuery qualityReportQuery1 = new QualityReportQuery();
        qualityReportQuery1.setProId(proId);
        qualityReportQuery1.setStartDate(new Date());
        qualityReportQuery1.setEndDate(new Date());
        ResultInfo<QualityReportDTO> qualityReportDTOResult = qualityReportFeignClient.queryDemandQualityReport(qualityReportQuery1);
        if (qualityReportDTOResult != null && qualityReportDTOResult.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            List<MapNodeNumDTO> userChart = qualityReportDTOResult.getData().getUserChart();
            if (userChart != null && userChart.size() > 0) {
                dataMap.put("userChart", ojectToJsonString(userChart));
            } else {
                dataMap.put("userChart", "[]");
            }
            List<MapNodeNumDTO> moduleChart = qualityReportDTOResult.getData().getModuleChart();
            if (moduleChart != null && moduleChart.size() > 0) {
                dataMap.put("moduleChart", ojectToJsonString(moduleChart));
            } else {
                dataMap.put("moduleChart", "[]");
            }
            List<String> yUserAxis = qualityReportDTOResult.getData().getyUserAxis();
            List<Integer> xUserAxis = qualityReportDTOResult.getData().getxUserAxis();
            List<Integer> UserAxis1 = new ArrayList<>();
            if (yUserAxis != null && yUserAxis.size() > 0) {
                dataMap.put("xUserAxis", listToString(xUserAxis));
                Integer userTotal = 0;
                Integer maxNum = 0;
                for (int i = 0; i < xUserAxis.size(); i++) {
                    for (Integer xu : xUserAxis
                            ) {
                        userTotal += xu;
                    }
                    if (xUserAxis.get(i) > maxNum) {
                        maxNum = xUserAxis.get(i);
                    }
                    UserAxis1.add(maxNum - xUserAxis.get(i));
                }
                for (int i = 0; i < xUserAxis.size(); i++) {
                    UserAxis1.add(maxNum - xUserAxis.get(i));
                }
                dataMap.put("userTotal", userTotal);
                dataMap.put("xUserAxis1", listToString(UserAxis1));
                dataMap.put("yUserAxis", listChangeToString(yUserAxis));
            } else {
                dataMap.put("xUserAxis1", "[]");
                /*System.out.println("xUserAxis1=" + dataMap.get("xUserAxis1"));*/
                dataMap.put("xUserAxis", "[]");
                dataMap.put("yUserAxis", "[]");
            }
            List<Integer> xModuleAxis = qualityReportDTOResult.getData().getxModuleAxis();
            List<String> yModuleAxis = qualityReportDTOResult.getData().getyModuleAxis();
            List<Integer> xModuleAxis1 = new ArrayList<>();
            Integer moduleTotal = 0;
            Integer maxNum = 0;
            if (xModuleAxis != null && xModuleAxis.size() > 0) {

                for (Integer xu : xModuleAxis
                        ) {
                    moduleTotal += xu;
                }

                for (int i = 0; i < xModuleAxis.size(); i++) {

                    if (xModuleAxis.get(i) > maxNum) {
                        maxNum = xModuleAxis.get(i);
                    }
                }
                for (int i = 0; i < xModuleAxis.size(); i++) {
                    xModuleAxis1.add(maxNum - xModuleAxis.get(i));
                }
                dataMap.put("moduleTotal", moduleTotal);
                dataMap.put("xModuleAxis", listToString(xModuleAxis));
                dataMap.put("xModuleAxis1", listToString(xModuleAxis1));
                dataMap.put("yModuleAxis", listChangeToString(yModuleAxis));
            } else {
                dataMap.put("xModuleAxis1", "[]");
                dataMap.put("xModuleAxis", "[]");
                dataMap.put("yModuleAxis", "[]");
            }
            wordUtil.createWord("complete.json", preFilePath + proId + "/json/demandCompleteByDay.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + proId + "/json/demandCompleteByDay.json " + preFilePath + proId + "/images/demandCompleteByDay.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        //当日用例设计完成情况(人员)
        QualityReportQuery qualityQuery = new QualityReportQuery();
        qualityQuery.setProId(proId);
        qualityQuery.setStartDate(new Date());
        qualityQuery.setEndDate(new Date());
        ResultInfo<QualityReportDTO> qualityResultInfo = qualityReportFeignClient.queryTestCaseQualityReport(qualityQuery);
        if (qualityResultInfo != null && qualityResultInfo.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            List<MapNodeNumDTO> userChart = qualityResultInfo.getData().getUserChart();

            if (userChart != null && userChart.size() > 0) {
                dataMap.put("userChart", ojectToJsonString(userChart));
            } else {
                dataMap.put("userChart", "[]");
            }
            List<MapNodeNumDTO> moduleChart = qualityResultInfo.getData().getModuleChart();
            if (moduleChart != null && moduleChart.size() > 0) {
                dataMap.put("moduleChart", ojectToJsonString(moduleChart));
            } else {
                dataMap.put("moduleChart", "[]");
            }
            List<String> yUserAxis = qualityResultInfo.getData().getyUserAxis();
            List<Integer> xUserAxis = qualityResultInfo.getData().getxUserAxis();
            List<Integer> UserAxis1 = new ArrayList<>();
            if (yUserAxis != null && yUserAxis.size() > 0) {
                Integer userTotal = 0;
                Integer maxNum = 0;
                for (Integer xu : xUserAxis
                        ) {
                    userTotal += xu;
                }
                dataMap.put("userTotal", userTotal);
                dataMap.put("xUserAxis", listToString(xUserAxis));
                for (int i = 0; i < xUserAxis.size(); i++) {

                    if (xUserAxis.get(i) > maxNum) {
                        maxNum = xUserAxis.get(i);
                    }
                }
                for (int i = 0; i < xUserAxis.size(); i++) {
                    UserAxis1.add(maxNum - xUserAxis.get(i));
                }
                dataMap.put("xUserAxis1", listToString(UserAxis1));
                dataMap.put("yUserAxis", listChangeToString(yUserAxis));
            } else {
                dataMap.put("xUserAxis1", "[]");
                dataMap.put("xUserAxis", "[]");
                dataMap.put("userTotal", "");
                dataMap.put("yUserAxis", "[]");
            }
            List<Integer> xModuleAxis = qualityResultInfo.getData().getxModuleAxis();
            List<String> yModuleAxis = qualityResultInfo.getData().getyModuleAxis();
            List<Integer> xModuleAxis1 = new ArrayList<>();
            if (xModuleAxis != null && xModuleAxis.size() > 0) {
                Integer moduleTotal = 0;
                for (Integer xu : xModuleAxis
                        ) {
                    moduleTotal += xu;
                }
                dataMap.put("moduleTotal", moduleTotal);
                dataMap.put("xModuleAxis", listToString(xModuleAxis));
                Integer maxNum = 0;
                for (int i = 0; i < xModuleAxis.size(); i++) {

                    if (xModuleAxis.get(i) > maxNum) {
                        maxNum = xModuleAxis.get(i);
                    }
                }
                for (int i = 0; i < xModuleAxis.size(); i++) {
                    xModuleAxis1.add(maxNum - xModuleAxis.get(i));
                }
                dataMap.put("xModuleAxis1", listToString(xModuleAxis1));
                dataMap.put("yModuleAxis", listChangeToString(yModuleAxis));

            } else {
              /*  dataMap.put("moduleTotal","");*/
                dataMap.put("xModuleAxis", "[]");
                dataMap.put("yModuleAxis", "[]");
                dataMap.put("xModuleAxis1", "[]");
            }
            wordUtil.createWord("caseComplete.json", preFilePath + proId + "/json/caseCompleteByDay.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + proId + "/json/caseCompleteByDay.json " + preFilePath + proId + "/images/caseCompleteByDay.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        //用例设计完成情况(从项目开始算起,模块)
        QualityReportQuery qualityQuery2 = new QualityReportQuery();
        qualityQuery2.setProId(proId);
        qualityQuery2.setStartDate(cltProject.getCreateDate());
        qualityQuery2.setEndDate(new Date());
        ResultInfo<QualityReportDTO> qualityResult = qualityReportFeignClient.queryTestCaseQualityReport(qualityQuery2);
        if (qualityResult != null && qualityResult.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            List<MapNodeNumDTO> userChart = qualityResult.getData().getUserChart();

            if (userChart != null && userChart.size() > 0) {
                dataMap.put("userChart", ojectToJsonString(userChart));
            } else {
                dataMap.put("userChart", "[]");
            }
            List<MapNodeNumDTO> moduleChart = qualityResult.getData().getModuleChart();
            if (moduleChart != null && moduleChart.size() > 0) {
                dataMap.put("moduleChart", ojectToJsonString(moduleChart));
            } else {
                dataMap.put("moduleChart", "[]");
            }
            List<String> yUserAxis = qualityResult.getData().getyUserAxis();
            List<Integer> xUserAxis = qualityResult.getData().getxUserAxis();
            List<Integer> UserAxis1 = new ArrayList<>();
            if (yUserAxis != null && yUserAxis.size() > 0) {
                Integer userTotal = 0;
                for (Integer xu : xUserAxis
                        ) {
                    userTotal += xu;
                }
                dataMap.put("userTotal", userTotal);
                dataMap.put("xUserAxis", listToString(xUserAxis));
                Integer maxNum=0;
                for (int i = 0; i < xUserAxis.size(); i++) {

                    if (xUserAxis.get(i) > maxNum) {
                        maxNum = xUserAxis.get(i);
                    }
                }
                for (int i = 0; i < xUserAxis.size(); i++) {
                    UserAxis1.add(maxNum - xUserAxis.get(i));
                }
                dataMap.put("xUserAxis1", listToString(UserAxis1));
                dataMap.put("yUserAxis", listChangeToString(yUserAxis));
            } else {
                dataMap.put("xUserAxis1", "[]");
                dataMap.put("xUserAxis", "[]");
                dataMap.put("yUserAxis", "[]");
            }
            List<Integer> xModuleAxis = qualityResult.getData().getxModuleAxis();
            List<String> yModuleAxis = qualityResult.getData().getyModuleAxis();
            List<Integer> xModuleAxis1 = new ArrayList<>();
            if (xModuleAxis != null && xModuleAxis.size() > 0) {
                Integer moduleTotal = 0;
                for (Integer xu : xModuleAxis
                        ) {
                    moduleTotal += xu;
                }
                dataMap.put("moduleTotal", moduleTotal);
                dataMap.put("xModuleAxis", listToString(xModuleAxis));
                Integer maxNum=0;
                for (int i = 0; i < xModuleAxis.size(); i++) {

                    if (xModuleAxis.get(i) > maxNum) {
                        maxNum = xModuleAxis.get(i);
                    }
                }
                for (int i = 0; i < xModuleAxis.size(); i++) {
                    xModuleAxis1.add(maxNum - xModuleAxis.get(i));
                }
                dataMap.put("xModuleAxis1", listToString(xModuleAxis1));
                dataMap.put("yModuleAxis", listChangeToString(yModuleAxis));

            } else {
              /*  dataMap.put("moduleTotal","");*/
                dataMap.put("xModuleAxis", "[]");
                dataMap.put("yModuleAxis", "[]");
                dataMap.put("xModuleAxis1", "[]");
            }
            wordUtil.createWord("caseComplete.json", preFilePath + proId + "/json/caseComplete.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + proId + "/json/caseComplete.json " + preFilePath + qualityReportQuery.getProId() + "/images/caseComplete.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        return num;
    }

    /**
     * inter类型list转换成string
     *
     * @param list
     * @return
     */

    private String ObjectToString(List<MapNodeNumDTO> list) {
        String rtnStr = "[";
        for (int i = 0; i < list.size(); i++) {
            rtnStr += "\"" + list.get(i) + "\",";
        }
        if (rtnStr.indexOf(",") > 0) {
            rtnStr = rtnStr.substring(0, rtnStr.length() - 1);
        }
        return rtnStr + "]";
    }

    /**
     * 将对象转换成string
     *
     * @param list
     * @return
     */
    public String ojectToJsonString(List<MapNodeNumDTO> list) {
        String map = "[";

        for (MapNodeNumDTO mapNodeNumDTO :
                list) {
            map += "{\"name" + "\":" + "\"" + mapNodeNumDTO.getStatus() + "\"," + "\"value" + "\":" + mapNodeNumDTO.getNodeNum() + "}" + ",";

        }
        if (map.indexOf(",") > 0) {
            map = map.substring(0, map.length() - 1);
        }
        return map + "]";
    }

    /**
     * inter类型list转换成string
     *
     * @param list
     * @return
     */
    private String listToString(List<Integer> list) {
        String rtnStr = "[";
        for (int i = 0; i < list.size(); i++) {
            rtnStr += "\"" + list.get(i) + "\",";
        }
        if (rtnStr.indexOf(",") > 0) {
            rtnStr = rtnStr.substring(0, rtnStr.length() - 1);
        }
        return rtnStr + "]";
    }

    /**
     * list转换成string
     *
     * @param list
     * @return
     */
    private String listChangeToString(List<String> list) {
        String rtnStr = "[";
        for (int i = 0; i < list.size(); i++) {
            rtnStr += "\"" + list.get(i) + "\",";
        }
        if (rtnStr.indexOf(",") > 0) {
            rtnStr = rtnStr.substring(0, rtnStr.length() - 1);
        }
        return rtnStr + "]";
    }
}

