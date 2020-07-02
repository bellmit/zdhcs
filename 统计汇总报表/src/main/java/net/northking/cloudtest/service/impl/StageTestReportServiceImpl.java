package net.northking.cloudtest.service.impl;

import net.northking.cloudtest.assist.RedisUtil;
import net.northking.cloudtest.dao.analyse.CltRoundMapper;
import net.northking.cloudtest.dao.report.CltReportMapper;
import net.northking.cloudtest.domain.analyse.CltRound;
import net.northking.cloudtest.domain.report.CltReport;
import net.northking.cloudtest.domain.report.CltReportExample;
import net.northking.cloudtest.domain.user.CltUserLogin;
import net.northking.cloudtest.dto.DeamndBulletinService;
import net.northking.cloudtest.enums.CltBulletinCatalog;
import net.northking.cloudtest.exception.GlobalException;
import net.northking.cloudtest.libreoffice.DocConverter;
import net.northking.cloudtest.query.report.CltReportQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.service.*;
import net.northking.cloudtest.utils.*;
import net.northking.cloudtest.utils.PageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import net.northking.cloudtest.common.Page;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * Created by liujinghao on 2018/5/11.
 */
@Service
public class StageTestReportServiceImpl implements StageTestReportService {

    @Autowired
    private CltReportMapper cltReportMapper;

    private final static Logger logger = LoggerFactory.getLogger(StageTestReportServiceImpl.class);

    @Autowired
    private GenChartService genChartService;

    @Autowired
    private SummaryBulletinService summaryBulletinService;

    @Autowired
    private GenTestBugChartService genTestBugChartService;

    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Autowired
    private CltRoundMapper cltRoundMapper;

    @Autowired
    private GenDemandReportChartService genDemandReportChartService;
    @Autowired
    private DeamndBulletinService deamndBulletinService;
    @Autowired
    private DesignBulletinService designBulletinService;


    @Autowired
    private GenCompleteChartService genCompleteChartService;

    @Autowired
    private GenChartExecuteService genChartExecuteService;


    @Autowired
    private DayReportService dayReportService;

    @Autowired
    private GenExecuteReportChartService genExecuteReportChartService;
    @Autowired
    private ExecuteBulletinService executeBulletinService;

    @Autowired
    private GenCompleteChartWeekService genCompleteChartWeekService;

    @Autowired
    private  WeekReportService weekReportService;

    @Autowired
    private GenExecuteChartWeekService genExecuteChartWeekService;

    @Autowired
    private GenTestBugChartWeekService genTestBugChartWeekService;
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 报告预览
     *
     * @param cltReport
     * @return
     * @throws Exception
     */
    @Override
    public CltReport previewReport(CltReport cltReport) throws Exception {
        if (StringUtils.isEmpty(cltReport.getProjectId())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "项目id不能为空");
        }
        if (StringUtils.isEmpty(cltReport.getCatalog())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "报告类型不能为空");
        }
        CltReport clt = cltReportMapper.selectByProjectIdAndCataLog(cltReport);

        return clt;
    }

    /**
     * 重新生成报告
     * @param cltReport
     * @return
     * @throws Exception
     */
    @Override
    public CltReport reGenReport(CltReport cltReport) throws Exception {
        if (StringUtils.isEmpty(cltReport.getProjectId())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "项目id不能为空");
        }
        if (StringUtils.isEmpty(cltReport.getCatalog())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "报告类型不能为空");
        }
       /* CltReport clt = cltReportMapper.selectByProjectIdAndCataLog(cltReport);*/

        Long reportId=cltReport.getId();


        CltReportExample example = new CltReportExample();
        CltReportExample.Criteria criteria = example.createCriteria();
        criteria.andProjectIdEqualTo(cltReport.getProjectId());
        String access_token = cltReport.getAccess_token();
        CltUserLogin cltUserLogin = (CltUserLogin)redisUtil.get(access_token);
        String userChnName = cltUserLogin.getUserChnName();
        //重新生成方法
        if(cltReport.getCatalog().equals(CltBulletinCatalog.SM.getCode())){ //总体报告
            genSummaryBulletin(cltReport.getProjectId(),userChnName);
        }else if(cltReport.getCatalog().equals(CltBulletinCatalog.RA.getCode())){ //需求阶段报告
            genDemandBulletin(cltReport.getProjectId(),userChnName );
        }else if(cltReport.getCatalog().equals(CltBulletinCatalog.CD.getCode())){ //设计阶段报告
            genDesignBulletin(cltReport.getProjectId(),userChnName);
        }else if(cltReport.getCatalog().equals(CltBulletinCatalog.TD.getCode())){ //日报
            genDayBulletin(cltReport.getProjectId(),userChnName);
        }else if(cltReport.getCatalog().equals(CltBulletinCatalog.CE.getCode())){ //轮次执行报告
            genExecuteBulletin(cltReport.getProjectId(), cltReport.getRoundId(),userChnName);
            criteria.andRoundIdEqualTo(cltReport.getRoundId());
        }else if(cltReport.getCatalog().equals(CltBulletinCatalog.TW.getCode())){ //周报
            genWeekBulletin(cltReport.getProjectId(),userChnName);
        }

        CltReport cltr=cltReportMapper.selectByPrimaryKey(reportId);
        if(cltr == null) {
            example.setOrderByClause("REPORT_DATE DESC");
            List<CltReport> cltReportList = cltReportMapper.selectByExample(example);
            if (cltReportList.size() > 0) {
                cltr = cltReportList.get(0);
            }
        }

        return cltr;
    }

    /**
     * 测试报告列表
     *
     * @param query
     * @return
     * @throws Exception
     */
    @Override
    public Page<CltReport> previewTestReports(CltReportQuery query) throws Exception {

        query.validate();
        PageUtil.startPage(query);//获取分页信息
        List<CltReport> cltReports = null;
        if (StringUtils.isEmpty(query.getEndDate())&&StringUtils.isEmpty(query.getStartDate())){
            if(CltBulletinCatalog.CE.getCode().equals(query.getCatalog())){
                cltReports = cltReportMapper.selectRoundReports(query.getProjectId());
            }else {
                CltReportExample CltReportExample = assemblyExample(query); //组装请求参数
                cltReports = cltReportMapper.selectByExample(CltReportExample);
            }
        }else{
            cltReports=cltReportMapper.selectByProjectIdAndCataLogAndDate(query);
        }
        return new Page<>(cltReports);
    }

    /**
     * 查询报告信息
     * @param cltReport
     * @return
     * @throws Exception
     */
    @Override
    public CltReport queryReport(CltReport cltReport) throws Exception {
        if (StringUtils.isEmpty(cltReport.getId())) {
            throw new GlobalException(ResultCode.INVALID_PARAM.code(), "报告id不能为空");
        }
        CltReport clt = cltReportMapper.selectByPrimaryKey(cltReport.getId());

        return clt;
    }

    /**
     * 装配查询参数
     *
     * @param
     * @return
     */
    private CltReportExample assemblyExample(CltReportQuery query) {
        CltReportExample example = new CltReportExample();

        example.setOrderByClause(query.getOrderByClause());

        CltReportExample.Criteria criteria = example.createCriteria();

        if (StringUtils.hasText(query.getCatalog())) {
            criteria.andCatalogEqualTo( query.getCatalog() );
        }
        if (StringUtils.hasText(query.getProjectId())) {
            criteria.andProjectIdEqualTo(query.getProjectId() );
        }

        return example;
    }

    /**
     * 生成总体测试报告
     * @param projectId
     * @throws Exception
     */
    private void genSummaryBulletin(String projectId,String userChnName) throws Exception{
        WordUtil wordUtil = new WordUtil();

        File dirFile = new File(preFilePath + projectId);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //生成图表
        genChartService.genChart(projectId);
        //生成缺陷趋势图表
        genTestBugChartService.genTestBugChart(projectId);

        //获取数据
        Map dataMap = new HashMap();
        try {
            summaryBulletinService.dealWithReport(projectId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!StringUtils.isEmpty(userChnName)){
            dataMap.put("userChnName",userChnName);
        }
        wordUtil.createWord("summeryModel.ftl", preFilePath + projectId + "/summeryReport.xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/summeryReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/summeryReport.docx")));
            String[] itemname = {"word/document.xml", "word/media/image1.png", "word/media/image2.png", "word/media/image3.png", "word/media/image4.png", "word/media/image5.png", "word/media/image6.png"};
            String[] itemInputFile = {preFilePath + projectId + "/summeryReport.xml", preFilePath + projectId + "/images/totalProgress.png", preFilePath + projectId + "/images/demandProgress.png", preFilePath + projectId + "/images/caseDesignProgress.png", preFilePath + projectId + "/images/caseExecuteProgress.png", preFilePath + projectId + "/images/testTeam.png", preFilePath + projectId + "/images/testBugTrend.png"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}

            DocConverter d = new DocConverter(preFilePath + projectId + "/summeryReport.docx");
            d.conver();

            //往数据库插入数据
            summaryBulletinService.insertOrUpdateData(projectId, null, "/bulletin/" + projectId + "/summeryReport.docx?t="+ UUIDUtil.getUUID(), "/bulletin/" + projectId + "/summeryReport.swf?t="+ UUIDUtil.getUUID());

        } catch (Exception e) {
            logger.error("error：", e);
        }
    }

    /**
     * 生成需求报告
     * @param projectId
     * @throws Exception
     */
    private void genDemandBulletin(String projectId,String userChnName) throws Exception{
        WordUtil wordUtil = new WordUtil();

        File dirFile = new File(preFilePath + projectId);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //生成图表
        genDemandReportChartService.genChart(projectId);

        //获取数据
        Map dataMap = new HashMap();

        deamndBulletinService.dealWithReport(projectId, dataMap);
        if(!StringUtils.isEmpty(userChnName)){

            dataMap.put("userChnName",userChnName);
        }
        wordUtil.createWord("demandModel.ftl", preFilePath + projectId + "/demandReport.xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/demandReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/demandReport.docx")));
            String[] itemname = {"word/document.xml","word/media/image1.png", "word/media/image2.png", "word/media/image3.png","word/media/image4.png"};
            String[] itemInputFile = {preFilePath + projectId + "/demandReport.xml", preFilePath + projectId + "/images/totalProgress.png",  preFilePath + projectId + "/images/demandProgress.png", preFilePath + projectId + "/images/testTeam.png",preFilePath + projectId + "/images/coverageReport.png"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}

            DocConverter d = new DocConverter(preFilePath + projectId + "/demandReport.docx");
            d.conver();

            //往数据库插入数据
            deamndBulletinService.insertOrUpdateData(projectId, null, "/bulletin/" + projectId + "/demandReport.docx?t="+ UUIDUtil.getUUID(), "/bulletin/" + projectId + "/demandReport.swf?t="+ UUIDUtil.getUUID());


        } catch (Exception e) {
            logger.error("error：", e);
        }
    }

    /**
     * 生成设计阶段报告
     * @param projectId
     * @throws Exception
     */
    private void genDesignBulletin(String projectId,String userChnName) throws Exception{
        WordUtil wordUtil = new WordUtil();

        File dirFile = new File(preFilePath + projectId);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //生成图表
        designBulletinService.genChart(projectId);

        //获取数据
        Map dataMap = new HashMap();
        try {
            designBulletinService.dealWithReport(projectId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(StringUtils.isEmpty(userChnName)){
            dataMap.put("userChnName",userChnName);
        }
        wordUtil.createWord("designModel.ftl", preFilePath + projectId + "/designReport.xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/designReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/designReport.docx")));
            String[] itemname = {"word/document.xml", "word/media/image1.png" , "word/media/image2.png", "word/media/image3.png", "word/media/image4.png"};
            String[] itemInputFile = {preFilePath + projectId + "/designReport.xml", preFilePath + projectId + "/images/totalProgress.png", preFilePath + projectId + "/images/caseDesignProgress.png", preFilePath + projectId + "/images/testTeam.png", preFilePath + projectId + "/images/coverageReport.png"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}

            DocConverter d = new DocConverter(preFilePath + projectId + "/designReport.docx");
            d.conver();

            //往数据库插入数据
            designBulletinService.insertOrUpdateData(projectId, null, "/bulletin/" + projectId + "/designReport.docx?t="+ UUIDUtil.getUUID(), "/bulletin/" + projectId + "/designReport.swf?t="+ UUIDUtil.getUUID());

        } catch (Exception e) {
            logger.error("error：", e);
        }
    }

    /**
     * 生成日报
     * @param proId
     * @throws Exception
     */
    private void genDayBulletin(String proId,String userChnName) throws Exception{
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String preDate = sdf.format(new Date());

        WordUtil wordUtil = new WordUtil();
        File dirFile = new File(preFilePath + proId);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        //生成缺陷图表
        genTestBugChartService.genTestBugChart(proId);
        //生成完成情况图表
        genCompleteChartService.GenCompleteChart(proId);
        //生成用例执行完成情况
        genChartExecuteService.GenChartExecute(proId);
        //生成进度表
        genChartService.genChart(proId);
        //获取数据
        Map dataMap = new HashMap();
        try {
            dayReportService.dealWithDayReport(proId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(StringUtils.isEmpty(userChnName)){
            dataMap.put("userChnName",userChnName);
        }
        wordUtil.createWord("dayModel.ftl", preFilePath + proId + "/dayReport_"  + preDate +".xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/dayReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + proId + "/dayReport_"  + preDate +".docx")));

            List<CltRound> list = cltRoundMapper.selectRoundInfoByDate(proId, new Date());
            List<String> round = new ArrayList<>();
            for (CltRound cltRound : list
                    ) {
                round.add(cltRound.getRoundId());
            }
            int length = 12 + round.size() * 2;
            String[] itemname1 = new String[length];

            String[] itemname = {"word/document.xml", "word/media/image1.png","word/media/image2.png","word/media/image3.png","word/media/image4.png","word/media/image5.png",
                    "word/media/image6.png","word/media/image7.png", "word/media/image8.png",
                    "word/media/image9.png","word/media/image10.png", "word/media/image21.png"};
            for (int i = 0; i < itemname1.length; i++) {
                if (i <= 10) {
                    itemname1[i] = itemname[i];
                }
                if (i > 10 && i < itemname1.length - 1) {

                    itemname1[i] = "word/media/image" + i + ".png";

                }
                if (i == (itemname1.length - 1)) {
                    itemname1[length - 1] = "word/media/image21.png";
                }
            }
            String[] itemInputFile1 = new String[length];
            String[] itemInputFile = {preFilePath + proId + "/dayReport_"  + preDate +".xml", preFilePath + proId + "/images/totalProgress.png",
                    preFilePath + proId + "/images/demandProgress.png", preFilePath + proId + "/images/caseDesignProgress.png",
                    preFilePath + proId + "/images/caseExecuteProgress.png", preFilePath + proId + "/images/demandComplete.png",
                    preFilePath + proId + "/images/demandCompleteByDay.png", preFilePath + proId + "/images/caseComplete.png",
                    preFilePath + proId + "/images/caseCompleteByDay.png", preFilePath + proId + "/images/executeByUser.png",
                    preFilePath + proId + "/images/executeByModule.png", preFilePath + proId + "/images/testBugTrend.png"
            };
            int j = 1;
            int k = 1;
            for (int i = 0; i < itemInputFile1.length; i++) {
                if (i <= 10) {
                    itemInputFile1[i] = itemInputFile[i];
                }
                if (i > 10 && i < itemname1.length - 1) {
                    if (i % 2 != 0) {
                        itemInputFile1[i] = preFilePath + proId + "/images/executeAllByUser" + j + ".png";
                        j++;
                    } else {
                        itemInputFile1[i] = preFilePath + proId + "/images/executeAllByModule" + k + ".png";
                        k++;
                    }

                }
                if (i == (itemInputFile1.length - 1)) {
                    itemInputFile1[length - 1] = preFilePath + proId + "/images/testBugTrend.png";
                }
            }

            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname1, itemInputFile1);
            //}
            DocConverter d = new DocConverter(preFilePath + proId + "/dayReport_"  + preDate +".docx");
            d.conver();

            //往数据库插入数据
            dayReportService.insertOrUpdateDayReportData(proId, null, "/bulletin/" + proId + "/dayReport_"  + preDate +".docx?t="+ UUIDUtil.getUUID(), "/bulletin/" + proId + "/dayReport_"  + preDate +".swf?t="+ UUIDUtil.getUUID());

        } catch (Exception e) {
            logger.error("error：", e);
        }
    }

    /**
     * 生成周报
     * @param proId
     * @throws Exception
     */
    private void genWeekBulletin(String proId,String userChnName) throws Exception{
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String preDate = sdf.format(WeekUtil.getLastWeekMonday(new Date())); //上周一

        WordUtil wordUtil = new WordUtil();
        File dirFile = new File(preFilePath + proId+"/images");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //生成项目进度图表
        genChartService.genChart(proId);

        //生成完成情况图表
        genCompleteChartWeekService.genCompleteChartWeek(proId);

        //生成执行完成情况图表
        genExecuteChartWeekService.GenExecuteWeekChart(proId);

        //生成缺陷图
        genTestBugChartWeekService.genTestBugChart(proId);
        //获取数据
        Map dataMap = new HashMap();
        try {
            weekReportService.dealWithWeekReport(proId, dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(StringUtils.isEmpty(userChnName)){
            dataMap.put("userChnName",userChnName);
        }
        wordUtil.createWord("weekModel.ftl", preFilePath + proId + "/weekReport_"  + preDate +".xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/weekReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + proId + "/weekReport_"  + preDate +".docx")));
            List<CltRound> list = cltRoundMapper.selectRoundInfoByDate(proId, new Date());
            List<String> round = new ArrayList<>();
            for (CltRound cltRound : list
                    ) {
                round.add(cltRound.getRoundId());
            }
            int length = 15 + round.size() * 2;
            String[] itemname1 = new String[length];
            String[] itemname = {"word/document.xml","word/media/image1.png","word/media/image2.png","word/media/image3.png","word/media/image4.png","word/media/image5.png",
                    "word/media/image6.png","word/media/image7.png","word/media/image8.png", "word/media/image9.png","word/media/image10.png",
                    "word/media/image21.wmf","word/media/image22.emf","word/media/image23.png","word/media/image24.png"};
            for (int i = 0; i < itemname1.length; i++) {
                if (i <= 10) {
                    itemname1[i] = itemname[i];
                }
                if (i > 10 && i < itemname1.length - 4) {

                    itemname1[i] = "word/media/image" + i + ".png";

                }
                if (i == (itemname1.length - 4)) {
                    itemname1[length - 4] = "word/media/image21.wmf";
                }
                if (i == (itemname1.length - 3)) {
                    itemname1[length - 3] = "word/media/image22.emf";
                }
                if (i == (itemname1.length - 2)) {
                    itemname1[length - 2] = "word/media/image23.png";
                }
                if (i == (itemname1.length - 1)) {
                    itemname1[length - 1] = "word/media/image24.png";
                }
            }
            String[] itemInputFile1 = new String[length];
            String[] itemInputFile = {preFilePath + proId + "/weekReport_"  + preDate +".xml",  preFilePath + proId + "/images/totalProgress.png",
                    preFilePath + proId + "/images/demandProgress.png",
                    preFilePath + proId + "/images/caseDesignProgress.png",preFilePath + proId + "/images/caseExecuteProgress.png",
                    preFilePath + proId + "/images/demandCompleteByAllWeek.png", preFilePath + proId + "/images/demandCompleteByWeek.png",
                    preFilePath + proId + "/images/caseCompleteByModuleAndWeek.png", preFilePath + proId + "/images/caseCompleteByUserAndWeek.png",
                    preFilePath + proId + "/images/executeByUserAndWeek.png", preFilePath + proId + "/images/executeByModuleAndWeek.png",
                    preFilePath + proId + "/images/testBugByWeek.png", preFilePath + proId + "/images/testBugModuleByWeek.png"
                    ,preFilePath + proId + "/images/testBugTrendByWeek.png",preFilePath + proId + "/images/testBugLife.png"};

            int j = 1;
            int k = 1;
            for (int i = 0; i < itemInputFile1.length; i++) {
                if (i <= 10) {
                    itemInputFile1[i] = itemInputFile[i];
                }
                if (i > 10 && i < itemInputFile1.length - 4) {
                    if (i % 2 != 0) {
                        itemInputFile1[i] = preFilePath + proId + "/images/executeAllByUserAndRound" + j + ".png";
                        j++;
                    } else {
                        itemInputFile1[i] = preFilePath + proId + "/images/executeAllByModuleAndRound" + k + ".png";
                        k++;
                    }

                }
                if (i == (itemInputFile1.length - 4)) {
                    itemInputFile1[length - 4] =preFilePath + proId + "/images/testBugByWeek.png";
                }
                if (i == (itemInputFile1.length - 3)) {
                    itemInputFile1[length - 3] =preFilePath + proId + "/images/testBugModuleByWeek.png";
                }
                if (i == (itemInputFile1.length - 2)) {
                    itemInputFile1[length - 2] =preFilePath + proId + "/images/testBugTrendByWeek.png";
                }
                if (i == (itemInputFile1.length - 1)) {
                    itemInputFile1[length - 1] = preFilePath + proId + "/images/testBugLife.png";
                }
            }
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname1, itemInputFile1);
            //}
            DocConverter d = new DocConverter(preFilePath + proId + "/weekReport_"  + preDate +".docx");
            d.conver();

            //往数据库插入数据
            weekReportService.insertOrUpdateWeekReportData(proId, null, "/bulletin/" + proId + "/weekReport_"  + preDate +".docx?t="+ UUIDUtil.getUUID(), "/bulletin/" + proId + "/weekReport_"  + preDate +".swf?t="+ UUIDUtil.getUUID());

        } catch (Exception e) {
            logger.error("error：", e);
        }
    }

    /**
     * 生成执行报告
     * @param
     * @throws Exception
     */
    private void genExecuteBulletin(String projectId, String roundId,String userChnName) throws Exception{
        WordUtil wordUtil = new WordUtil();

        File dirFile = new File(preFilePath + projectId+"/images");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        //生成图表
        genExecuteReportChartService.genChart(projectId, roundId);

        //获取数据
        Map dataMap = new HashMap();


        executeBulletinService.dealWithReport(projectId, roundId, dataMap);
        if(!StringUtils.isEmpty(userChnName)){

            dataMap.put("userChnName",userChnName);
        }
        wordUtil.createWord("executeModel.ftl", preFilePath + projectId + "/executeReport_" + roundId + ".xml", dataMap);

        try {
            //File cfgFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "zip/summeryReport.zip");
            InputStream inputStream = this.getClass().getResourceAsStream("/zip/executeReport.zip");
            //if(cfgFile.exists()) {
            ZipInputStream zipInputStream = ZipUtils.wrapZipInputStream(inputStream);
            ZipOutputStream zipOutputStream = ZipUtils.wrapZipOutputStream(new FileOutputStream(new File(preFilePath + projectId + "/executeReport_" + roundId + ".docx")));
            String[] itemname = {"word/document.xml","word/media/image1.png", "word/media/image2.png", "word/media/image3.png","word/media/image4.png","word/media/image5.png","word/media/image6.png","word/media/image7.png"};
            String[] itemInputFile = {preFilePath + projectId + "/executeReport_" + roundId + ".xml", preFilePath + projectId + "/images/totalProgress.png",  preFilePath + projectId + "/images/caseDesignProgressSingle.png", preFilePath + projectId + "/images/testTeam.png",preFilePath + projectId + "/images/testCaseTrend.png", preFilePath + projectId + "/images/executeByUserWeek.png", preFilePath + projectId + "/images/testBugByWeek.png", preFilePath + projectId + "/images/testBugTrend.png"};
            ZipUtils.replaceItems(zipInputStream, zipOutputStream, itemname, itemInputFile);
            //}

            DocConverter d = new DocConverter(preFilePath + projectId + "/executeReport_" + roundId + ".docx");
            d.conver();

            //往数据库插入数据
            executeBulletinService.insertOrUpdateData(projectId, roundId, null, "/bulletin/" + projectId + "/executeReport_" + roundId + ".docx?t="+ UUIDUtil.getUUID(), "/bulletin/" + projectId + "/executeReport_" + roundId + ".swf?t="+ UUIDUtil.getUUID());


        } catch (Exception e) {
            logger.error("error：", e);
        }
    }
}

