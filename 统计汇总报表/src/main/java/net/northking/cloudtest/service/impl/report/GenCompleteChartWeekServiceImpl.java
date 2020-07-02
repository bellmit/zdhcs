package net.northking.cloudtest.service.impl.report;

import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.testBug.CltTestBugMapper;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.testBug.CltBugLifeCount;
import net.northking.cloudtest.domain.testBug.CltTestBug;
import net.northking.cloudtest.dto.analyse.MapNodeNumDTO;
import net.northking.cloudtest.dto.report.QualityReportDTO;
import net.northking.cloudtest.feign.report.QualityReportFeignClient;
import net.northking.cloudtest.query.report.QualityReportQuery;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenCompleteChartWeekService;
import net.northking.cloudtest.utils.WordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by liujinghao on 2018/5/25.
 */
@Service
public class GenCompleteChartWeekServiceImpl implements GenCompleteChartWeekService{

    private final static Logger logger = LoggerFactory.getLogger(GenCompleteChartWeekServiceImpl.class);

    @Autowired
    private QualityReportFeignClient qualityReportFeignClient;

    @Autowired
    private CltProjectMapper cltProjectMapper;

    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;
    /**
     * 生成图表
     * @param proId
     * @return
     * @throws Exception
     */
    @Override
    public int genCompleteChartWeek(String proId) throws Exception {
        Runtime run = Runtime.getRuntime();

        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int num = 0;
        //查出项目
        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(proId);

        //一周前日期
     /*   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");*/
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, - 6);
        Date monday = c.getTime();

        //本周需求分析完成情况
        QualityReportQuery qualityReportQuery = new QualityReportQuery();
        qualityReportQuery.setProId(proId);
        qualityReportQuery.setStartDate(monday);
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
                Integer userTotal=0;
                for (int i = 0; i < xUserAxis.size(); i++) {
                    for (Integer xu:xUserAxis
                            ) {
                        userTotal+=xu;
                    }
                    dataMap.put("userTotal",userTotal);
                    System.out.println("userTotal="+userTotal);
                    Integer maxNum = xUserAxis.get(0);
                    if (xUserAxis.get(i) > maxNum) {
                        maxNum = xUserAxis.get(i);
                    }
                    UserAxis1.add(maxNum - xUserAxis.get(i));
                }
                dataMap.put("xUserAxis1", listToString(UserAxis1));
                System.out.println("xUserAxis1=" + dataMap.get("xUserAxis1"));
                System.out.println("xUserAxis=" + dataMap.get("xUserAxis"));
                dataMap.put("yUserAxis", listChangeToString(yUserAxis));
            } else {
                dataMap.put("xUserAxis1", "[]");
                System.out.println("xUserAxis1="+dataMap.get("xUserAxis1"));
                dataMap.put("xUserAxis", "[]");
                System.out.println("xUserAxis=" + dataMap.get("xUserAxis"));
                dataMap.put("yUserAxis", "[]");
            }
            List<Integer> xModuleAxis = qualityReportDTOResultInfo.getData().getxModuleAxis();
            List<String> yModuleAxis = qualityReportDTOResultInfo.getData().getyModuleAxis();
            List<Integer> xModuleAxis1 = new ArrayList<>();
            Integer moduleTotal=0;
            if (xModuleAxis != null && xModuleAxis.size() > 0) {

                for (Integer xu:xModuleAxis
                        ) {
                    moduleTotal+=xu;
                }
                dataMap.put("moduleTotal",moduleTotal);
                dataMap.put("xModuleAxis", listToString(xModuleAxis));
                System.out.println("xModuleAxis=" + dataMap.get("xModuleAxis"));
                for (int i = 0; i < xModuleAxis.size(); i++) {
                    Integer maxNum = xModuleAxis.get(0);
                    if (xModuleAxis.get(i) > maxNum) {
                        maxNum = xModuleAxis.get(i);
                    }
                    xModuleAxis1.add(maxNum - xModuleAxis.get(i));
                }
                dataMap.put("xModuleAxis1", listToString(xModuleAxis1));
                System.out.println("xModuleAxis1=" + dataMap.get("xModuleAxis1"));
                dataMap.put("yModuleAxis", listChangeToString(yModuleAxis));
                System.out.println("yModuleAxis=" + dataMap.get("yModuleAxis"));

            } else {
                dataMap.put("xModuleAxis1", "[]");
                dataMap.put("xModuleAxis", "[]");
                dataMap.put("yModuleAxis", "[]");
            }
            wordUtil.createWord("complete.json", preFilePath + qualityReportQuery.getProId() + "/json/demandCompleteByWeek.json", dataMap);
            String progressCmd ="node " + outputJsPath + " "+ preFilePath + qualityReportQuery.getProId() + "/json/demandCompleteByWeek.json " + preFilePath + qualityReportQuery.getProId() + "/images/demandCompleteByWeek.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        //项目整体的需求完成情况
        QualityReportQuery qualityReportQuery1 = new QualityReportQuery();
        qualityReportQuery1.setProId(proId);
        qualityReportQuery1.setStartDate(cltProject.getCreateDate());
        qualityReportQuery1.setEndDate(new Date());
        ResultInfo<QualityReportDTO> qualityReportDTOResult= qualityReportFeignClient.queryDemandQualityReport(qualityReportQuery1);
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
               /* System.out.println("需求分析模块moduleChart=" + dataMap.get("moduleChart"));*/
            } else {
                dataMap.put("moduleChart", "[]");
            }
            List<String> yUserAxis = qualityReportDTOResult.getData().getyUserAxis();
            List<Integer> xUserAxis = qualityReportDTOResult.getData().getxUserAxis();
            List<Integer> UserAxis1 = new ArrayList<>();
            if (yUserAxis != null && yUserAxis.size() > 0) {
                dataMap.put("xUserAxis", listToString(xUserAxis));
                Integer userTotal=0;
                for (int i = 0; i < xUserAxis.size(); i++) {
                    for (Integer xu:xUserAxis
                            ) {
                        userTotal+=xu;
                    }
                    dataMap.put("userTotal",userTotal);
                    System.out.println("userTotal="+userTotal);
                    Integer maxNum = xUserAxis.get(0);
                    if (xUserAxis.get(i) > maxNum) {
                        maxNum = xUserAxis.get(i);
                    }
                    UserAxis1.add(maxNum - xUserAxis.get(i));
                }
                dataMap.put("xUserAxis1", listToString(UserAxis1));
               /* System.out.println("xUserAxis1=" + dataMap.get("xUserAxis1"));
                System.out.println("xUserAxis=" + dataMap.get("xUserAxis"));*/
                dataMap.put("yUserAxis", listChangeToString(yUserAxis));
            } else {
                dataMap.put("xUserAxis1", "[]");
                dataMap.put("xUserAxis", "[]");
                dataMap.put("yUserAxis", "[]");
            }
            List<Integer> xModuleAxis = qualityReportDTOResult.getData().getxModuleAxis();
            List<String> yModuleAxis = qualityReportDTOResult.getData().getyModuleAxis();
            List<Integer> xModuleAxis1 = new ArrayList<>();
            Integer moduleTotal=0;
            if (xModuleAxis != null && xModuleAxis.size() > 0) {

                for (Integer xu:xModuleAxis
                        ) {
                    moduleTotal+=xu;
                }
                dataMap.put("moduleTotal",moduleTotal);
                dataMap.put("xModuleAxis", listToString(xModuleAxis));
                System.out.println("xModuleAxis=" + dataMap.get("xModuleAxis"));
                for (int i = 0; i < xModuleAxis.size(); i++) {
                    Integer maxNum = xModuleAxis.get(0);
                    if (xModuleAxis.get(i) > maxNum) {
                        maxNum = xModuleAxis.get(i);
                    }
                    xModuleAxis1.add(maxNum - xModuleAxis.get(i));
                }
                dataMap.put("xModuleAxis1", listToString(xModuleAxis1));
              /*  System.out.println("xModuleAxis1=" + dataMap.get("xModuleAxis1"));*/
                dataMap.put("yModuleAxis", listChangeToString(yModuleAxis));
              /*  System.out.println("yModuleAxis=" + dataMap.get("yModuleAxis"));
*/
            } else {
                dataMap.put("xModuleAxis1", "[]");
                dataMap.put("xModuleAxis", "[]");
                dataMap.put("yModuleAxis", "[]");
            }
            wordUtil.createWord("complete.json", preFilePath + qualityReportQuery.getProId() + "/json/demandCompleteByAllWeek.json", dataMap);
            String progressCmd ="node " + outputJsPath + " "+ preFilePath + qualityReportQuery.getProId() + "/json/demandCompleteByAllWeek.json " + preFilePath + qualityReportQuery.getProId() + "/images/demandCompleteByAllWeek.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        //测试用例设计完成情况(项目开始算起,模块)
        QualityReportQuery qualityQuery = new QualityReportQuery();
        qualityQuery.setProId(proId);
        qualityQuery.setStartDate(cltProject.getCreateDate());
        qualityQuery.setEndDate(new Date());
        ResultInfo<QualityReportDTO> qualityResultInfo = qualityReportFeignClient.queryTestCaseQualityReport(qualityQuery);
        if (qualityResultInfo != null && qualityResultInfo.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            List<MapNodeNumDTO> userChart = qualityResultInfo.getData().getUserChart();

            if (userChart != null && userChart.size() > 0) {
                dataMap.put("userChart", ojectToJsonString(userChart));
               /* System.out.println("用例设计人员userChart=" + dataMap.get("userChart"));*/
            } else {
                dataMap.put("userChart", "[]");
            }
            List<MapNodeNumDTO> moduleChart = qualityResultInfo.getData().getModuleChart();
            if (moduleChart != null && moduleChart.size() > 0) {
                dataMap.put("moduleChart", ojectToJsonString(moduleChart));
                /*System.out.println("用例设计模块moduleChart=" + dataMap.get("moduleChart"));*/
            } else {
                dataMap.put("moduleChart", "[]");
            }
            List<String> yUserAxis = qualityResultInfo.getData().getyUserAxis();
            List<Integer> xUserAxis = qualityResultInfo.getData().getxUserAxis();
            List<Integer> UserAxis1 = new ArrayList<>();
            if (yUserAxis != null && yUserAxis.size() > 0) {
                Integer userTotal=0;
                for (Integer xu:xUserAxis
                        ) {
                    userTotal+=xu;
                }
                dataMap.put("userTotal",userTotal);
                dataMap.put("xUserAxis", listToString(xUserAxis));
                for (int i = 0; i < xUserAxis.size(); i++) {
                    Integer maxNum = xUserAxis.get(0);
                    if (xUserAxis.get(i) > maxNum) {
                        maxNum = xUserAxis.get(i);
                    }
                    UserAxis1.add(maxNum - xUserAxis.get(i));
                }
                dataMap.put("xUserAxis1", listToString(UserAxis1));
               /* System.out.println("xUserAxis1=" + dataMap.get("xUserAxis1"));
                System.out.println("xUserAxis=" + dataMap.get("xUserAxis"));*/
                dataMap.put("yUserAxis", listChangeToString(yUserAxis));
            } else {
                dataMap.put("xUserAxis1", "[]");
                dataMap.put("xUserAxis", "[]");
                dataMap.put("userTotal","");
                System.out.println("xUserAxis=" + dataMap.get("xUserAxis"));
                dataMap.put("yUserAxis", "[]");
            }
            List<Integer> xModuleAxis = qualityResultInfo.getData().getxModuleAxis();
            List<String> yModuleAxis = qualityResultInfo.getData().getyModuleAxis();
            List<Integer> xModuleAxis1 = new ArrayList<>();
            if (xModuleAxis != null && xModuleAxis.size() > 0) {
                Integer moduleTotal=0;
                for (Integer xu:xModuleAxis
                        ) {
                    moduleTotal+=xu;
                }
                dataMap.put("moduleTotal",moduleTotal);
                dataMap.put("xModuleAxis", listToString(xModuleAxis));
                /*System.out.println("xModuleAxis=" + dataMap.get("xModuleAxis"));*/
                for (int i = 0; i < xModuleAxis.size(); i++) {
                    Integer maxNum = xModuleAxis.get(0);
                    if (xModuleAxis.get(i) > maxNum) {
                        maxNum = xModuleAxis.get(i);
                    }
                    xModuleAxis1.add(maxNum - xModuleAxis.get(i));
                }
                dataMap.put("xModuleAxis1", listToString(xModuleAxis1));
                dataMap.put("yModuleAxis", listChangeToString(yModuleAxis));
                /*System.out.println("yModuleAxis=" + dataMap.get("yModuleAxis"));*/

            } else {
                dataMap.put("moduleTotal","");
                dataMap.put("xModuleAxis", "[]");
                dataMap.put("yModuleAxis", "[]");
                dataMap.put("xModuleAxis1", "[]");
            }
            wordUtil.createWord("caseComplete.json", preFilePath + qualityReportQuery.getProId() + "/json/caseCompleteByModuleAndWeek.json", dataMap);
            String progressCmd ="node " + outputJsPath + " "+ preFilePath + qualityReportQuery.getProId() + "/json/caseCompleteByModuleAndWeek.json " + preFilePath + qualityReportQuery.getProId() + "/images/caseCompleteByModuleAndWeek.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }

        //本周测试用例设计完成情况(人员)
        QualityReportQuery quality = new QualityReportQuery();
        quality.setProId(proId);
        quality.setStartDate(monday);
        quality.setEndDate(new Date());
        ResultInfo<QualityReportDTO> qualityInfo= qualityReportFeignClient.queryTestCaseQualityReport(quality);
        if (qualityInfo != null && qualityInfo.getData() != null) {
            WordUtil wordUtil = new WordUtil();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            List<MapNodeNumDTO> userChart = qualityInfo.getData().getUserChart();

            if (userChart != null && userChart.size() > 0) {
                dataMap.put("userChart", ojectToJsonString(userChart));
            } else {
                dataMap.put("userChart", "[]");
            }
            List<MapNodeNumDTO> moduleChart = qualityInfo.getData().getModuleChart();
            if (moduleChart != null && moduleChart.size() > 0) {
                dataMap.put("moduleChart", ojectToJsonString(moduleChart));
            } else {
                dataMap.put("moduleChart", "[]");
            }
            List<String> yUserAxis = qualityInfo.getData().getyUserAxis();
            List<Integer> xUserAxis = qualityInfo.getData().getxUserAxis();
            List<Integer> UserAxis1 = new ArrayList<>();
            if (yUserAxis != null && yUserAxis.size() > 0) {
                Integer userTotal=0;
                for (Integer xu:xUserAxis
                        ) {
                    userTotal+=xu;
                }
                dataMap.put("userTotal",userTotal);
                System.out.println("userTotal="+userTotal);
                dataMap.put("xUserAxis", listToString(xUserAxis));
                for (int i = 0; i < xUserAxis.size(); i++) {
                    Integer maxNum = xUserAxis.get(0);
                    if (xUserAxis.get(i) > maxNum) {
                        maxNum = xUserAxis.get(i);
                    }
                    UserAxis1.add(maxNum - xUserAxis.get(i));
                }
                dataMap.put("xUserAxis1", listToString(UserAxis1));
               /* System.out.println("xUserAxis1=" + dataMap.get("xUserAxis1"));
                System.out.println("xUserAxis=" + dataMap.get("xUserAxis"));*/
                dataMap.put("yUserAxis", listChangeToString(yUserAxis));
            } else {
                dataMap.put("xUserAxis1", "[]");
                dataMap.put("xUserAxis", "[]");
                dataMap.put("userTotal","");
                dataMap.put("yUserAxis", "[]");
            }
            List<Integer> xModuleAxis = qualityInfo.getData().getxModuleAxis();
            List<String> yModuleAxis = qualityInfo.getData().getyModuleAxis();
            List<Integer> xModuleAxis1 = new ArrayList<>();
            if (xModuleAxis != null && xModuleAxis.size() > 0) {
                Integer moduleTotal=0;
                for (Integer xu:xModuleAxis
                        ) {
                    moduleTotal+=xu;
                }
                dataMap.put("moduleTotal",moduleTotal);
                dataMap.put("xModuleAxis", listToString(xModuleAxis));
                for (int i = 0; i < xModuleAxis.size(); i++) {
                    Integer maxNum = xModuleAxis.get(0);
                    if (xModuleAxis.get(i) > maxNum) {
                        maxNum = xModuleAxis.get(i);
                    }
                    xModuleAxis1.add(maxNum - xModuleAxis.get(i));
                }
                dataMap.put("xModuleAxis1", listToString(xModuleAxis1));
                dataMap.put("yModuleAxis", listChangeToString(yModuleAxis));

            } else {
                dataMap.put("moduleTotal","");
                dataMap.put("xModuleAxis", "[]");
                dataMap.put("yModuleAxis", "[]");
                dataMap.put("xModuleAxis1", "[]");
            }
            wordUtil.createWord("caseComplete.json", preFilePath + qualityReportQuery.getProId() + "/json/caseCompleteByUserAndWeek.json", dataMap);
            String progressCmd ="node " + outputJsPath + " "+ preFilePath + qualityReportQuery.getProId() + "/json/caseCompleteByUserAndWeek.json " + preFilePath + qualityReportQuery.getProId() + "/images/caseCompleteByUserAndWeek.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        return 0;
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
