package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReExecPlan;
import net.northking.atp.db.service.ReExecPlanService;
import net.northking.atp.entity.ExecutePlan;
import net.northking.atp.enums.PlanClass;
import net.northking.atp.enums.PlanType;
import net.northking.atp.service.ExecPlanService;
import net.northking.atp.service.PlanExecuteInfoService;
import net.northking.db.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 *
 */

@RestController
@Api(tags = {"测试执行计划"}, description = "测试执行计划_执行计划信息的维护")
@RequestMapping(value = "/execplan")
public class ExecPlanController {

    private static final Logger logger = LoggerFactory.getLogger(ExecPlanController.class);

    @Autowired
    private ReExecPlanService reExecPlanService;

    @Autowired
    private ExecPlanService execPlanService;

    protected HttpServletRequest request;

    @ModelAttribute
    public void getRequest(HttpServletRequest request) {
        this.request = request;
        logger.info("请求的接口地址 --> {}", this.request.getRequestURL());
        logger.info("请求的用户 --> {}", this.request.getRemoteUser());
        logger.info("请求的客户机IP地址 --> {}", this.request.getRemoteAddr());
        logger.info("请求的web服务器的ip地址 --> {}", this.request.getLocalAddr());
    }
    
    /**
     * 执行计划jar包接口 测试
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "执行计划jar包接口 测试", notes = "执行计划jar包接口 测试")
    @RequestMapping(value = "/test/{id}.json", method = {RequestMethod.GET})
    public String insertExecPlan(@PathVariable String id) {
    	logger.info("执行计划jar包接口 测试 输入的id为--{}",id);
    	return id;
    }

    /**
     * 新增测试执行计划
     *
     * @param target
     * @return
     */
    @ApiOperation(value = "测试执行计划 新增", notes = "新增测试执行计划 新增")
    @RequestMapping(value = "/insertExecPlan", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insertExecPlan(@RequestBody ExecutePlan target) {
        logger.info("新增计划的测试参数：【planClass计划类别】->【{}】", target.getPlanClass());
        // todo: 参数检查
        if (0 ==  target.getPlanClass()) {
            // 测试类别如果没勾选，默认给定web/ui
            target.setPlanClass(PlanClass.WEB_UI.code());
        }
        try {
            target.setPlanType(PlanType.NORMAL.code());
            ExecutePlan result = execPlanService.addExecPlan(target);
            return new ResultWrapper().success(result);
        } catch (Exception e) {
            return new ResultWrapper().fail("ATP-ERROR-00001", e.getMessage(), e);
        }
    }

    /**
     * 测试执行计划 分页查询
     *
     * @param query 查询条件对象
     * @return
     */
    @ApiOperation(value = "测试执行计划 分页查询", notes = "测试执行计划 分页查询")
    @RequestMapping(value = "/queryByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<ExecutePlan>> queryByPage(@RequestBody QueryByPage<ExecutePlan> query) {
        Pagination<ExecutePlan> page = execPlanService.queryExecutePlan(query);
        return new ResultWrapper<Pagination<ExecutePlan>>().success(page);
    }

    /**
     * 测试执行计划 查看计划详情
     * @param query
     * @return
     */
    @ApiOperation(value = "测试执行计划 查看详情", notes = "测试执行计划 查看详情")
    @RequestMapping(value = "/queryPlanSettingById", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper queryPlanSettingById(@RequestBody ExecutePlan query) {
        ExecutePlan plan = execPlanService.queryPlanSettingById(query.getId());
        if (plan != null) {
            return new ResultWrapper<ExecutePlan>().success(plan);
        } else {
            return new ResultWrapper<>().fail("000001", "找不到计划" + query.getId() + "信息");
        }
    }

    /**
     * 测试执行计划 修改测试执行计划
     * @param plan
     * @return
     */
    @ApiOperation(value = "测试执行计划 修改详情", notes = "测试执行计划 修改详情")
    @RequestMapping(value = "/updateExcePlan", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper updateExecPlan(@RequestBody ExecutePlan plan) {
        execPlanService.updateExecPlan(plan);
        return new ResultWrapper().success();
    }

    /**
     * 测试执行计划 删除计划
     * @param plan
     * @return
     */
    @ApiOperation(value = "测试执行计划 删除计划", notes = "测试执行计划 删除计划")
    @RequestMapping(value = "/deleteExecPlan", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteExecPlan(@RequestBody ReExecPlan plan) {
        int count = 0;
        try {
            count = execPlanService.deleteExecPlan(plan.getId());
        } catch (Exception e) {
            return new ResultWrapper().fail("DELETE_EXEC_PLAN00001", "删除失败");
        }
        if (count == 0) {
            return new ResultWrapper<>().fail("DELETE_EXEC_PLAN00001", "没有可删除的计划");
        } else {
            return new ResultWrapper().success(count);
        }
    }


}
