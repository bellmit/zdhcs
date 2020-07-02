package net.northking.atp.consumer;

import com.github.pagehelper.PageHelper;
import net.northking.atp.db.dao.RePlanExecInfoDao;
import net.northking.atp.db.dao.RuEngineJobDao;
import net.northking.atp.db.dao.RuJobProDao;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.ReExecPlanPluginSettingService;
import net.northking.atp.db.service.RePlanTriggerRelService;
import net.northking.atp.db.service.RuEngineJobService;
import net.northking.atp.entity.DoubleBarEchartEntity;
import net.northking.atp.entity.EngineJob;
import net.northking.atp.entity.JobPro;
import net.northking.atp.enums.ExecuteResult;
import net.northking.atp.enums.PluginSettingStatus;
import net.northking.atp.producer.TestReportProducer;
import net.northking.atp.service.PlanExecuteInfoService;
import net.northking.atp.utils.VelocityUtil;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportAckConsumer {

    // 日志
    private static final Logger logger = LoggerFactory.getLogger(ReportAckConsumer.class);

    @Autowired
    private RuEngineJobDao ruEngineJobDao;
    @Autowired
    private RePlanExecInfoDao rePlanExecInfoDao;
    @Autowired
    private RuEngineJobService ruEngineJobService;
    @Autowired
    private RuJobProDao ruJobProDao;
    @Autowired
    private PlanExecuteInfoService planExecuteInfoService;
    @Autowired
    private TestReportProducer testReportProducer;
    @Autowired
    private RePlanTriggerRelService rePlanTriggerRelService;
    @Autowired
    private ReExecPlanPluginSettingService reExecPlanPluginSettingService;

    private final static String separator = File.separator;

//    private final static String testReportPath = separator + "home" +
//            separator + "data" +separator + "html" + separator + "test" + separator + "report";

//    private final static String testReportPath = "C:\\Users\\darkm\\Desktop\\report\\";
    @Value("${atp.report.path:/var/nk_atp/report}")
    private String testReportPath;

    @Value("${atp.report.url:http://192.168.0.130:8082}")
    private String testReportUrl;

    /**
     * 生成测试报告
     *
     * @param planExecId 执行记录id
     */
    @RabbitListener(queues = {"${atp.mq.queue.plan-report:Q.plan.report}"}, containerFactory = "mqContainerFactory")
    public void generateTestReport(String planExecId) {
        // 查询当前执行记录
        RePlanExecInfo planExecInfo = rePlanExecInfoDao.findByPrimaryKey(planExecId);
        // 执行任务的执行结果情况
        Map<String, Long> execResultMap = ruEngineJobDao.countExecResultByPlanExecId(planExecId);
        // 计划执行成功，调用接口，触发其他计划的执行
//        planGetTriggered(planExecInfo);
        try {
            planExecuteInfoService.planGetTriggeredByPlanId(planExecInfo.getPlanId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 处理模板文件
        handleVMFile(planExecInfo, execResultMap);

    }

    private void planGetTriggered(RePlanExecInfo planExecInfo) {
        RePlanTriggerRel query = new RePlanTriggerRel();
        query.setTriggerPlanId(planExecInfo.getPlanId());
        List<RePlanTriggerRel> resultList = rePlanTriggerRelService.query(query);
        if (resultList == null || resultList.size() == 0) return;
        for (RePlanTriggerRel rel : resultList) {
            ReExecPlanPluginSetting setting = reExecPlanPluginSettingService.findByPrimaryKey(rel.getPlanPluginId());
            if (PluginSettingStatus.ENABLE.code() == setting.getStatus()) {
                planExecuteInfoService.doPlanExecute(rel.getPlanId());
            }
        }
    }

    /**
     * 根据模板文件生成文件
     *
     * @param planExecInfo      执行记录信息
     * @param execResultMap     执行结果集合
     */
    private void handleVMFile(RePlanExecInfo planExecInfo, Map<String, Long> execResultMap) {
        VelocityContext context = new VelocityContext();
        context.put("planExecInfo", planExecInfo);
        context.put("execResultMap", execResultMap);
        // 执行结果
        context.put("executeResult", ExecuteResult.getMsgByCode(planExecInfo.getExecResult()).msg());
        // 加入工具类
        context.put("veloUtil", VelocityUtil.class);
        // 查询当前执行记录的所有执行任务，任务下的执行情况
        reportToFile(context, planExecInfo);
    }

    /**
     *
     * @param context       模板引擎上下文
     * @param planExecInfo  执行记录信息
     */
    private void reportToFile(VelocityContext context, RePlanExecInfo planExecInfo) {
        HashMap<String, Object> jobQueryMap = new HashMap<>();
        jobQueryMap.put(RePlanExecInfo.KEY_id, planExecInfo.getId());
        List<RuEngineJob> jobs = ruEngineJobService.queryEngineJobsByPage(jobQueryMap);
        List<EngineJob> engineJobs = planExecuteInfoService.getEngineJobs(jobs);
        context.put("engineJobs", engineJobs);

        for (EngineJob engineJob : engineJobs) {
            Map<String, Object> proQueryMap = new HashMap<>();
            proQueryMap.put(RuJobPro.KEY_jobId, engineJob.getId());
            OrderBy orderBy = new SqlOrderBy();
            orderBy.addOrderBy(RuJobPro.KEY_idx, OrderBy.ASC);
            orderBy.addOrderBy(RuJobPro.KEY_startTme, OrderBy.ASC);
            Pagination<JobPro> jobProPage = planExecuteInfoService.queryJobProsByPage(proQueryMap, orderBy, 0, 2000);
            engineJob.setJobProList(jobProPage.getRecords());
        }

        try {
            // 生成html文件
            String outputFileName = planExecInfo.getPlanName() + "_" + planExecInfo.getExecIdx() + ".html";
            VelocityUtil.generateFileByVM("template/report/default.vm", testReportPath + separator +outputFileName, context);
            // 将报告地址更新到执行机记录
            RePlanExecInfo planExecRecord = new RePlanExecInfo();
            planExecRecord.setId(planExecInfo.getId());
            planExecRecord.setReportUrl(testReportUrl + "/report/" + outputFileName);
            rePlanExecInfoDao.updateByPrimaryKey(planExecRecord);
            logger.info("执行记录 {} 生成测试报告：{}", planExecInfo.getId(), outputFileName);
        } catch (IOException e) {
            logger.error("" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取图表的数据信息
     * @param planId    执行记录id
     * @return          图标的数据信息
     */
    private String getEchartOption(String planId) {
        PageHelper.startPage(0, 10);
        List<Map<String, Object>> resultList = ruEngineJobDao.queryResultByPlanId(planId);
        // 双柱状图的数据集合
        ArrayList<DoubleBarEchartEntity> list = new ArrayList<>();
        for (Map<String, Object> map : resultList) {
            DoubleBarEchartEntity entity = new DoubleBarEchartEntity();
            entity.setName(String.valueOf((Integer) map.get(RePlanExecInfo.KEY_execIdx)));
            entity.setLeftBar((Long) map.get(ExecuteResult.FAILURE.code()));
            entity.setRightBar((Long) map.get(ExecuteResult.SUCCESS.code()));
            list.add(entity);
        }
        VelocityContext context = new VelocityContext();
        context.put("list", list);
        String option = VelocityUtil.generateDataString("template/data/doublebar.vm", context);
        return option;
    }
}
