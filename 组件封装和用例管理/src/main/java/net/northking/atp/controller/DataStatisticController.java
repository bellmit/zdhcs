package net.northking.atp.controller;

import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.CloudtestWebClient;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseDesignMenutree;
import net.northking.atp.db.persistent.ReComponentInfo;
import net.northking.atp.db.persistent.ReExecPlan;
import net.northking.atp.db.service.ReCaseDesignInfoService;
import net.northking.atp.db.service.ReCaseDesignMenutreeService;
import net.northking.atp.db.service.ReComponentInfoService;
import net.northking.atp.db.service.ReExecPlanService;
import net.northking.atp.entity.CloudTest.CltUserAndLogin;
import net.northking.atp.entity.CloudTest.UserAndLoginQuery;
import net.northking.atp.util.CaseDesignTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/8/6 0006.
 */
@RestController
@Api(tags = {"数据统计"}, description = "数据库数据统计分析")
@RequestMapping(value = "/dataStatistic")
public class DataStatisticController {

    @Autowired
    private ReCaseDesignInfoService reCaseDesignInfoService;
    @Autowired
    private ReComponentInfoService reComponentInfoService;
    @Autowired
    private ReExecPlanService reExecPlanService;
    @Autowired
    private ReCaseDesignMenutreeService reCaseDesignMenutreeService;

    /**
     * 数据统计_案例数量统计
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "案例数量统计_指定维度", notes = "案例数量统计_指定维度")
    @RequestMapping(value = "/caseNumberForStaff", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Map<String,Object>> caseNumberForStaff(@RequestBody Map<String,Object> info){
        //统计数量
        if("1".equals(info.get("dimensionality"))){
            //编写人员维度
            info.put("column","MODIFY_STAFF");
        }else if("2".equals(info.get("dimensionality"))){
            //模块维度
            info.put("column","MODULE_ID");
        }else{
            return new ResultWrapper().fail("dataStatistic0002","模式读取出错,请联系前端确认");
        }
        List<Map<String,Object>> numList = reCaseDesignInfoService.queryCaseStatistic(info);
        if(numList ==null|| numList.size()<1){
            return new ResultWrapper<Map<String,Object>>().success();
        }
        if("2".equals(info.get("dimensionality"))){
            Map<String,Object> menuMap = getMenuMap(info.get("projectId")+"");
            for(Map<String,Object> one : numList){
                one.put("column",menuMap.get(one.get("column")));
            }
        }
        Map<String,Object> result = analysisStatistic(numList,"column","num");
        return new ResultWrapper<Map<String,Object>>().success(result);
    }

    /**
     * 使用率统计_测试用例使用率统计百分比
     * 编写人员
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "案例使用率统计_指定维度", notes = "案例使用率统计_指定维度")
    @RequestMapping(value = "/caseStatisticPercentByDimen", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Map<String,Object>> caseStatisticPercentByDimen(@RequestBody Map<String,Object> info){
        //统计数量
        List<Map<String,Object>> numList = new ArrayList<Map<String, Object>>();
        if("1".equals(info.get("dimensionality"))){
            //编写人员维度
            numList = reCaseDesignInfoService.queryCaseStatisticPercentStaff(info);
        }else if("2".equals(info.get("dimensionality"))){
            //模块维度
            numList = reCaseDesignInfoService.queryCaseStatisticPercentModule(info);
        }
        if(numList ==null|| numList.size()<1){
            //return new ResultWrapper().fail("dataStatistic0001","无查询结果，请确认数据是否存在或者联系管理员");
        }
        Map<String,Object> result = new HashMap<String,Object>();
        if("1".equals(info.get("dimensionality"))){
            //模块维度
            result = analysisStatisticForPercent(numList,"modifyStaff","jobId");
        }else if("2".equals(info.get("dimensionality"))){
            //查询总数
            Map<String,Object> menuMap = getMenuMap(info.get("projectId")+"");
            for(Map<String,Object> one : numList){
                one.put("moduleName",menuMap.get(one.get("moduleId")));
            }
            ReCaseDesignInfo query = new ReCaseDesignInfo();
            query.setProjectId(info.get("projectId")+"");
            query.setCaseFlag("1");
            int count = (int)reCaseDesignInfoService.queryCount(query);
            result = analysisStatisticForPercentModule(numList,"moduleName","num",count);
        }
        return new ResultWrapper<Map<String,Object>>().success(result);
    }


    /**
     * 数据统计_组件数量统计
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "业务组件数量统计_指定维度", notes = "业务组件数量统计_指定维度")
    @RequestMapping(value = "/componentNumberForStaff", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Map<String,Object>> componentNumberForStaff(@RequestBody Map<String,Object> info){
        //统计数量
        List<Map<String,Object>> numList = new ArrayList<Map<String, Object>>();
        String str = "";
        if("1".equals(info.get("dimensionality"))){
            //编写人员维度
            numList = reComponentInfoService.queryComponentStatistic(info);
            str = "modifyStaff";
        }else if("2".equals(info.get("dimensionality"))){
            //模块维度
            numList = reComponentInfoService.queryComponentStatisticForModule(info);
            Map<String,Object> menuMap = getMenuMap(info.get("projectId")+"");
            for(Map<String,Object> one : numList){
                one.put("moduleName",menuMap.get(one.get("moduleId")));
            }
            str = "moduleName";
        }else{
            return new ResultWrapper().fail("dataStatistic0002","模式读取出错,请联系前端确认");
        }
        if(numList ==null|| numList.size()<1){
            //return new ResultWrapper().fail("dataStatistic0001","无查询结果，请确认数据是否存在或者联系管理员");
        }
        Map<String,Object> result = analysisStatistic(numList,str,"num");
        return new ResultWrapper<Map<String,Object>>().success(result);
    }

    /**
     * 数据统计_组件使用百分比
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "业务组件使用率统计_指定维度", notes = "业务组件使用率统计_指定维度")
    @RequestMapping(value = "/componentStatisticPercentByDimen", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Map<String,Object>> componentStatisticPercentStaff(@RequestBody Map<String,Object> info){
        List<Map<String,Object>> numList = new ArrayList<Map<String, Object>>();
        if("1".equals(info.get("dimensionality"))){
            //统计数量_ 编写人员维度
            numList =  reComponentInfoService.queryCompStaForStaff(info);
        }else if("2".equals(info.get("dimensionality"))){
            //模块维度
            numList = reComponentInfoService.queryCompStaForModule(info);
        }
        if(numList ==null|| numList.size()<1){
            return new ResultWrapper<Map<String,Object>>().success();
        }
        Map<String,Object> result = new HashMap<String,Object>();
        if("1".equals(info.get("dimensionality"))){
            //统计数量_ 编写人员维度
            result = analysisStatisticForPercent(numList,"modifyStaff","caseId");
        }else if("2".equals(info.get("dimensionality"))){
            //模块维度
            //查询总数
            Map<String,Object> menuMap = getMenuMap(info.get("projectId")+"");
            for(Map<String,Object> one : numList){
                one.put("moduleName",menuMap.get(one.get("moduleId")));
            }
            ReComponentInfo query = new ReComponentInfo();
            query.setProjectId(info.get("projectId")+"");
            query.setComponentFlag(info.get("componentFlag")+"");
            int count = (int)reComponentInfoService.queryCount(query);
            //计算总数
            result = analysisStatisticForPercentModule(numList,"moduleName","num",count);
        }

        return new ResultWrapper<Map<String,Object>>().success(result);
    }

    /**
     * 执行计划汇总
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "执行计划执行结果汇总", notes = "执行计划执行结果汇总")
    @RequestMapping(value = "/execPlanCollectStatistic", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Map<String,Object>> execPlanCollectStatistic(@RequestBody Map<String,Object> info){
        List<Map<String,Object>> numList = reExecPlanService.queryJobStatisticPercentByStaff(info);
        if(numList ==null|| numList.size()<1){
            //return new ResultWrapper().fail("dataStatistic0001","无查询结果，请确认数据是否存在或者联系管理员");
        }
        Map<String,Object> result = analysisPlanStatisticCollect(numList,"result","num");
        return new ResultWrapper<Map<String,Object>>().success(result);
    }

    /**
     * 数据统计_执行计划统计
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "执行记录任务统计", notes = "执行记录任务统计")
    @RequestMapping(value = "/execPlanStatistic", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<Map<String,Object>>> execPlanStatistic(@RequestBody QueryByPage<ReExecPlan> queryByPage){
        //统计数量
        CaseDesignTools tools = new CaseDesignTools();
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        ReExecPlan info = queryByPage.getQuery();
        info.setPlanType(1);
        Pagination<Map<String,Object>> result = reExecPlanService.selectExecPlanInfo(
                info, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());

        String idStr = "";
        for(Map<String,Object> one : result.getRecords()){
            if("".equals(idStr)){
                idStr = "'"+one.get("id")+"'";
            }else{
                idStr += ",'"+one.get("id")+"'";
            }
        }
        if(!"".equals(idStr)){
            Map<String,Object> query = new HashMap<String,Object>();
            query.put("idStr",idStr);
            query.put("execStatus","Finished");
            List<Map<String,Object>> jobList = reExecPlanService.queryJobStatistic(query);
            Map<String,Object> map = tools.getMapForAlikeKey(jobList,"planId");
            for(Map<String,Object> plan : result.getRecords()){
                //获取计划执行记录
                List<Map<String,Object>> staList = new ArrayList<Map<String, Object>>();
                if(map.get(plan.get("id")+"")==null){
                    plan.put("dataList",staList);
                    continue;
                }
                List<Map<String,Object>> list = (List<Map<String, Object>>) map.get(plan.get("id")+"");
                Map<String,Object> jobMap = tools.getMapForAlikeKey(list,"jobId");
                Map<String,Object> tool = new HashMap<String,Object>();
                for(Map<String,Object> job : list){
                    if (!tool.containsKey(job.get("jobId"))){
                        Map<String,Object> one = analysisPlanStatistic((List<Map<String, Object>>) jobMap.get(job.get("jobId")),"result","num");
                        staList.add(one);
                        tool.put(job.get("jobId")+"","1");
                    }
                }
                plan.put("dataList",staList);
            }
        }

        return new ResultWrapper<Pagination<Map<String,Object>>>().success(result);
    }

    /**
     * 执行用例通过率——指定维度
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "执行计划通过率统计", notes = "执行计划通过率统计")
    @RequestMapping(value = "/execPlanPassStatistic", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Map<String,Object>> execPlanPassStatistic(@RequestBody Map<String,Object> info){
        List<Map<String,Object>> numList = reExecPlanService.queryJobStatisticPercentByStaff(info);
//        if("1".equals(info.get("dimensionality"))){
//            //统计数量_ 编写人员维度
//            numList =  reExecPlanService.queryJobStatisticPercentByStaff(info);
//        }else if("2".equals(info.get("dimensionality"))){
//            //模块维度
//            numList =  reExecPlanService.queryJobStatisticPercentByStaff(info);
//        }
        if(numList ==null|| numList.size()<1){
            //return new ResultWrapper().fail("dataStatistic0001","无查询结果，请确认数据是否存在或者联系管理员");
        }
        Map<String,Object> result = new HashMap<String,Object>();
        if("1".equals(info.get("dimensionality"))){
            //统计数量_ 编写人员维度
            result = analysisPlanStatisticDimen(numList,"modifyStaff","num");
        }else if("2".equals(info.get("dimensionality"))){
            //模块维度
            Map<String,Object> menuMap = getMenuMap(info.get("projectId")+"");
            for(Map<String,Object> one : numList){
                one.put("moduleName",menuMap.get(one.get("moduleId")));
            }
            result = analysisPlanStatisticDimen(numList,"moduleName","num");
        }
        return new ResultWrapper<Map<String,Object>>().success(result);
    }

//======================================  数据处理私有方法区  =======================================================//
    /**
     * 分析查询结果，生成看板模板数据_更新人员
     * @param numList
     * @return
     */
    private Map<String,Object> analysisStatistic(List<Map<String,Object>> numList,String xKey,String yKey){
        Map<String,Object> result = new HashMap<String,Object>();
        List<String> xAxis = new ArrayList<String>();
        List<String> yAxis = new ArrayList<String>();
        for(Map<String,Object> one : numList){
            xAxis.add(one.get(xKey)==null?"无模块":(one.get(xKey)+""));
            yAxis.add(one.get(yKey)+"");
        }
        result.put("xAxis",xAxis);
        result.put("yAxis",yAxis);
        return result;
    }

    /**
     * 分析查询结果，生成看板模板数据_更新人员
     * @param numList
     * @return
     */
    private Map<String,Object> analysisPlanStatistic(List<Map<String,Object>> numList,String xKey,String yKey){
        Map<String,Object> toolMap = new HashMap<String,Object>();
        toolMap.put("success","成功");
        toolMap.put("fail","失败");

        Map<String,Object> map = new HashMap<String,Object>();
        for(Map<String,Object> one : numList){
            map.put(one.get(xKey)+"",one.get(yKey));
        }

        Map<String,Object> result = new HashMap<String,Object>();
        List<String> xAxis = new ArrayList<String>();
        List<String> yAxis = new ArrayList<String>();
        int all = 0;
        for(String key : toolMap.keySet()){
            xAxis.add(toolMap.get(key)+"");
            String num = map.get(key) == null ? "0" :map.get(key)+"";
            yAxis.add(num);
            all+=Integer.parseInt(num);
        }
        //增加总数
        xAxis.add("总任务数");
        yAxis.add(String.valueOf(all));
        result.put("xAxis",xAxis);
        result.put("yAxis",yAxis);
        return result;
    }

    /**
     * 计算组件使用率
     * @param numList
     * @return
     */
    private Map<String,Object> analysisStatisticForPercent(List<Map<String,Object>> numList,String xKey,String key){
        CaseDesignTools tools = new CaseDesignTools();
        Map<String,Object> result = new HashMap<String,Object>();
        Map<String,Object> dataMap = tools.getMapForAlikeKey(numList,xKey);
        List<String> xAxis = new ArrayList<String>();
        List<String> yAxis = new ArrayList<String>();
        DecimalFormat df = new DecimalFormat("0.00");
        for(String staff : dataMap.keySet()){
            List<Map<String,Object>> oneList = (List<Map<String, Object>>) dataMap.get(staff);
            int use = 0;
            for(Map<String,Object> one : oneList){
                if(one.get(key) != null && !"".equals(one.get(key))){
                    //已被使用
                    use++;
                }
            }
            xAxis.add("null".equals(staff)?"其他人员":staff);
            yAxis.add(df.format((float)use*100/oneList.size())+"%");
        }
        result.put("xAxis",xAxis);
        result.put("yAxis",yAxis);
        return result;
    }

    /**
     * 分析查询结果，生成汇总图表数据
     * @param numList
     * @return
     */
    private Map<String,Object> analysisStatisticForPercentModule(List<Map<String,Object>> numList,String xKey,String yKey,int count){
        CaseDesignTools tools = new CaseDesignTools();
        Map<String,Object> result = new HashMap<String,Object>();
        List<String> xAxis = new ArrayList<String>();
        List<String> yAxis = new ArrayList<String>();
        DecimalFormat df = new DecimalFormat("0.00");
        int all = 0;
        for(Map<String,Object> one : numList){
            if(one.get(xKey) == null || "".equals(one.get(xKey))){
                xAxis.add("无模块");
                all = Integer.parseInt(one.get(yKey)+"");
            }else{
                xAxis.add(one.get(xKey)+"");
            }
            int use = Integer.parseInt(one.get(yKey)+"");
            yAxis.add(df.format((float)use*100/count)+"%");
        }
        //增加总体使用率
//        xAxis.add("项目总计");
//        yAxis.add(df.format(100-(float)all*100/count)+"%");
        result.put("xAxis",xAxis);
        result.put("yAxis",yAxis);
        return result;
    }

    /**
     * 分析执行结果数据_通过率
     * @param numList
     * @return
     */
    private Map<String,Object> analysisPlanStatisticDimen(List<Map<String,Object>> numList,String xKey,String yKey){
        CaseDesignTools tools = new CaseDesignTools();
        Map<String,Object> result = new HashMap<String,Object>();
        Map<String,Object> dataMap = getMapForAlikeKeyByExecPlan(numList,xKey);
        List<String> xAxis = new ArrayList<String>();
        List<String> yAxis = new ArrayList<String>();
        DecimalFormat df = new DecimalFormat("0.00");
        for(String staff : dataMap.keySet()){
            List<Map<String,Object>> oneList = (List<Map<String, Object>>) dataMap.get(staff);
            int use = 0;
            int all = oneList.size();
            for(Map<String,Object> one : oneList){
                if("success".equals(one.get("result"))){
                    use++;
                }
            }
            if("modifyStaff".equals(xKey)){
                xAxis.add("null".equals(staff)?"无人员":staff);
            }else{
                xAxis.add("null".equals(staff)?"无模块":staff);
            }
            yAxis.add(df.format((float)use*100/all)+"%");
        }
        result.put("xAxis",xAxis);
        result.put("yAxis",yAxis);
        return result;
    }

    /**
     * 分析执行结果数据_汇总
     * @param numList
     * @return
     */
    private Map<String,Object> analysisPlanStatisticCollect(List<Map<String,Object>> numList,String xKey,String yKey){
        CaseDesignTools tools = new CaseDesignTools();
        Map<String,Object> result = new HashMap<String,Object>();
        List<Map<String,Object>> dataList = extractNewestByExecPlan(numList);
        List<String> xAxis = new ArrayList<String>();
        List<Integer> yAxis = new ArrayList<Integer>();
        DecimalFormat df = new DecimalFormat("0.00");
        int noRun = 0;
        int running = 0;
        int success = 0;
        int fail = 0;
        for(Map<String,Object> map : dataList){
            if("NoRun".equals(map.get("result"))){
                noRun++;
            }
            if("running".equals(map.get("result"))){
                running++;
            }
            if("success".equals(map.get("result"))){
                success++;
            }
            if("fail".equals(map.get("result"))){
                fail++;
            }
        }
        //x
        xAxis.add("未执行");
        xAxis.add("执行中");
        xAxis.add("成功");
        xAxis.add("失败");
        //y
        yAxis.add(noRun);
        yAxis.add(running);
        yAxis.add(success);
        yAxis.add(fail);
//        yAxis.add(df.format((float)noRun*100/dataList.size())+"%");
//        yAxis.add(df.format((float)running*100/dataList.size())+"%");
//        yAxis.add(df.format((float)success*100/dataList.size())+"%");
//        yAxis.add(df.format((float)fail*100/dataList.size())+"%");

        result.put("xAxis",xAxis);
        result.put("yAxis",yAxis);
        return result;
    }

    /**
     * 分析list，聚拢相同目标字段的数据_执行统计专用
     * @return map(key,list(map))
     */
    private Map<String,Object> getMapForAlikeKeyByExecPlan(List<Map<String,Object>> list, String key){
        Map<String,Object> result = new HashMap<String,Object>();
        Map<String,Object> check = new HashMap<String,Object>();
        for(Map<String,Object> map : list){
            if(result.containsKey(map.get(key)+"")){
                if(!check.containsKey(map.get("caseName")+"")){
                    List<Map<String,Object>> toolList = (List<Map<String, Object>>) result.get(map.get(key)+"");
                    toolList.add(map);
                }
            }else{
                List<Map<String,Object>> toolList = new ArrayList<Map<String, Object>>();
                toolList.add(map);
                result.put(map.get(key)+"",toolList);
                check.put(map.get("caseName")+"","1");
            }
        }
        return result;
    }

    /**
     * 提取案例执行最新结果
     * @param list
     * @return
     */
    private List<Map<String,Object>> extractNewestByExecPlan(List<Map<String,Object>> list){
        List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
        Map<String,Object> check = new HashMap<String,Object>();
        for(Map<String,Object> one : list){
            if (!check.containsKey(one.get("id"))){
                result.add(one);
                check.put(one.get("id")+"","1");
            }
        }
        return result;
    }

    /**
     * 查询最新的节点信息
     * @param projectId
     * @return
     */
    private Map<String,Object> getMenuMap(String projectId){
        Map<String,Object> result = new HashMap<String,Object>();
        ReCaseDesignMenutree menu = new ReCaseDesignMenutree();
        menu.setProjectId(projectId);
        List<ReCaseDesignMenutree> menuList = reCaseDesignMenutreeService.query(menu);
        for (ReCaseDesignMenutree one : menuList){
            result.put(one.getId(),one.getMenuName());
        }
        return result;
    }
}
