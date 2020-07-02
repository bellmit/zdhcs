package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.*;
import net.northking.atp.impl.CaseDesignModifyServiceImpl;
import net.northking.atp.impl.CaseDesignVersionServiceImpl;
import net.northking.atp.impl.CaseStepComponentServiceImpl;
import net.northking.atp.impl.DebugCaseDesignServiceImpl;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.RedisTools;
import net.northking.db.OrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 案例步骤组件相关
 * Created by Administrator on 2019/4/9 0009.
 */
@RestController
@Api(tags = {"步骤组件案例关联"}, description = "关联案例步骤以及组件之间的关系")
@RequestMapping(value = "/caseDesign")
public class CaseStepComponentController {
    //    @Autowired
//    private ReCaseDesignInfoService reCaseDesignInfoService; //案例表
//    @Autowired
//    private ReComponentStepService reComponentStepService; //步骤组件表
//    @Autowired
//    private ReCaseStepService reCaseStepService; //案例步骤表
//    @Autowired
//    private ReStepParameterService reStepParameterService; //步骤参数表
//    @Autowired
//    private CaseStepComponentServiceImpl caseStepComponentService;
//    @Autowired
//    private HisCaseDesignInfoService hisCaseDesignInfoService; //案例设计历史库
//    @Autowired
//    private HisComponentStepService hisComponentStepService; //步骤组件历史库
//    @Autowired
//    private HisCaseStepService hisCaseStepService; // 案例步骤历史库
//    @Autowired
//    private HisStepParameterService hisStepParameterService; //步骤参数历史库
    @Autowired
    private CaseDesignModifyServiceImpl caseDesignModifyService;
    @Autowired
    private CaseDesignVersionServiceImpl caseDesignVersionService;
    @Autowired
    private ReCaseStepService reCaseStepService;
    @Autowired
    private RedisTools redisTools;
    /**
     * 新增 案例步骤关联
     * @param target 组件信息
     * @return 接口返回
     * 统一接口于2019-05-20 废弃
     *
     @Transactional
     @ApiOperation(value = "新增 案例步骤组件关联", notes = "新增 案例步骤组件关联")
     @RequestMapping(value = "/CaseStepComponent/insertComponentInfo", method = {RequestMethod.POST},
     produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
     consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
     public ResultWrapper insertComponentInfo(@RequestBody InterfaceCaseStep target)
     {
     //此接口同时增加案例步骤关联，步骤组件管理，步骤参数_删除新增的方式
     target.setCaseId(target.getId());
     CaseDesignTools tools = new CaseDesignTools();
     if(target.getCaseId()==null || target.getProjectId()==null){
     return  new ResultWrapper().fail("caseStep0001","请确认参数是否完整");
     }
     ReCaseStep deleteCS = new ReCaseStep();
     deleteCS.setCaseId(target.getCaseId()); //案例编号
     deleteCS.setProjectId(target.getProjectId()); //系统编号
     reCaseStepService.deleteByExample(deleteCS);//删除原案例步骤关联

     ReComponentStep reCompStep = new ReComponentStep();
     reCompStep.setProjectId(target.getProjectId());
     reCompStep.setCaseId(target.getCaseId());
     reComponentStepService.deleteByExample(reCompStep); //删除原步骤组件参数

     ReStepParameter reStepPara = new ReStepParameter();
     reStepPara.setCaseId(target.getCaseId());
     reStepPara.setProjectId(target.getProjectId());
     reStepParameterService.deleteByExample(reStepPara); //删除原步骤参数


     if(target.getComponentList() != null && target.getComponentList().size()>0){
     int order = 1;
     //新增
     List<ReCaseStep> caseStepList = new ArrayList<ReCaseStep>();
     List<ReComponentStep> compStepList = new ArrayList<ReComponentStep>();
     List<ReStepParameter> stepParamList = new ArrayList<ReStepParameter>();
     for(InterfaceStepComponent stepComponent : target.getComponentList()){
     //案例步骤关联
     stepComponent.setComponentId(stepComponent.getId());

     ReCaseStep insertCS = new ReCaseStep();
     insertCS.setProjectId(target.getProjectId());
     insertCS.setId(UUID.randomUUID().toString().replace("-", ""));
     String stepId = UUID.randomUUID().toString().replace("-", "");
     insertCS.setStepId(stepId);
     insertCS.setCaseId(target.getCaseId());
     insertCS.setStepOrder(tools.getOrder(order));
     insertCS.setModifyTime(new Date());
     order++;
     caseStepList.add(insertCS);

     //步骤组件关联
     ReComponentStep reComponentStep = new ReComponentStep();
     reComponentStep.setId(UUID.randomUUID().toString().replace("-", ""));
     reComponentStep.setStepId(stepId);
     reComponentStep.setComponentId(stepComponent.getId());
     reComponentStep.setProjectId(target.getProjectId());
     reComponentStep.setModifyTime(new Date());
     reComponentStep.setCaseId(target.getCaseId());
     compStepList.add(reComponentStep);

     //步骤参数
     int orderPar = 1;
     for(ReComponentParameter reCompParam :stepComponent.getParamList()){
     ReStepParameter reStepParameter = new ReStepParameter();
     reStepParameter.setId(UUID.randomUUID().toString().replace("-", ""));
     reStepParameter.setProjectId(target.getProjectId());
     reStepParameter.setStepId(stepId);
     reStepParameter.setModifyTime(new Date());
     reStepParameter.setParameterId(reCompParam.getId());
     reStepParameter.setParameterOrder(tools.getOrder(orderPar));
     reStepParameter.setParameterValue(reCompParam.getDefaultValue());
     reStepParameter.setCaseId(target.getCaseId());
     stepParamList.add(reStepParameter);
     orderPar++;
     }
     }

     reCaseStepService.insertByBatch(caseStepList); //批量插入步骤数据
     reComponentStepService.insertByBatch(compStepList); //批量插入步骤组件数据
     if(stepParamList.size()>0){
     reStepParameterService.insertByBatch(stepParamList); //批量插入步骤参数数据
     }
     }
     return new ResultWrapper().success();
     }*/

    /**
     * 查询 案例步骤信息
     *
     * @param target 案例信息
     * @return 接口返回
     */
    @ApiOperation(value = "查询 案例步骤信息", notes = "查询 案例步骤信息")
    @RequestMapping(value = "/CaseStepComponent/queryStepComponentList", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<InterfaceStepComponent>> queryStepComponentList(@RequestBody InterfaceCaseStep target) {
        //List<InterfaceStepComponent> result = caseStepComponentService.queryStepComponentList(target);
        List<InterfaceStepComponent> result = caseDesignModifyService.queryStepComponentList(target);
        return new ResultWrapper<List<InterfaceStepComponent>>().success(result);
    }

    /**
     * 提交修改到版本
     *
     * @param target 案例信息
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "提交案例版本更新", notes = "将改动提交到案例历史库")
    @RequestMapping(value = "/CaseStepComponent/commitCaseModify", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper commitCaseModify(@RequestHeader(name = "Authorization") String authorization, @RequestBody MdCaseDesignInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        if (target.getId() == null || target.getProjectId() == null) {
            return new ResultWrapper().fail("caseStep0001", "请确认参数是否完整");
        }
        //版本校验
        String result = caseDesignModifyService.checkCaseComStepVersion(target);
        if(!"none".equals(result)){
            return new ResultWrapper().fail("caseStep00010005",result);
        }
        String staff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        target.setModifyStaff(staff);
        caseDesignVersionService.commitCaseDesignVersion(target);
        return new ResultWrapper().success();
    }

    /**
     * 根据版本回滚历史信息
     *
     * @param target
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "回滚历史信息", notes = "回滚历史数据")
    @RequestMapping(value = "/CaseStepComponent/caseDesignInfoHisRollback", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper caseDesignInfoHisRollback(@RequestHeader(name = "Authorization") String authorization,@RequestBody HisCaseDesignInfo target) {
        CaseDesignTools tools = new CaseDesignTools();
        if (target.getId() == null || target.getProjectId() == null) {
            return new ResultWrapper().fail("caseSet0001", "请确认参数是否完整");
        }
        String staff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        target.setModifyStaff(staff);
        caseDesignVersionService.rollbackCaseDesignVersion(target);
        return new ResultWrapper().success();
    }

    /**
     * 修改保存同时提交版本
     *
     * @param target 案例信息
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "修改提交 案例步骤组件关联", notes = "修改提交 案例步骤组件关联")
    @RequestMapping(value = "/caseInfoSaveAndCommit", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper caseInfoSaveAndCommit(@RequestHeader(name = "Authorization") String authorization,@RequestBody InterfaceCaseInfo target)
    {
        //保存案例步骤信息，包含新增以及编辑
        if(target.getProjectId()==null){
            return new ResultWrapper().fail("caseStep0001","请确认参数是否完整");
        }
        String staff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        target.setModifyStaff(staff);
        if(caseDesignModifyService.checkCaseDesignExist(target)){
            //重名
            return new ResultWrapper().fail("caseInfo0000005","案例已存在,请搜索查看或者更换案例名称");
        }
        if(target.getId() == null || "".equals(target.getId())){
            //为新增
            target.setCreateStaff(staff);
            caseDesignModifyService.insertModifyCaseInfo(target);
        }else{
            //更新
            target.setCaseId(target.getId());
            caseDesignModifyService.updateModifyCaseInfo(target);
        }
        //插入完成开始版本校验
        MdCaseDesignInfo check = new MdCaseDesignInfo();
        check.setId(target.getId());
        check.setProjectId(target.getProjectId());
        String result = caseDesignModifyService.checkCaseComStepVersion(check);
        if(!"none".equals(result)){
            return new ResultWrapper().fail("caseStep00010005",result);
        }else{
            check.setModifyStaff(target.getModifyStaff());
            caseDesignVersionService.commitCaseDesignVersion(check);
        }
        return new ResultWrapper().success(target);
    }

    /**
     * 用例批量复制
     * @param target 复制对象
     * @return
     */
    @ApiOperation(value = "数据复制 用例信息复制", notes = "数据复制 用例信息复制")
    @RequestMapping(value = "/CopyCaseData", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper CopyCaseData(@RequestHeader(name = "Authorization") String authorization,@RequestBody InterfaceComAndCaseCopy target)
    {
        if(target.getProjectId() == null || target.getMenuId() ==null){
            //参数确实
            return new ResultWrapper().fail("componentInfo0000004","参数缺失，请确认交易内容");
        }
        String modifyStaff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        if(modifyStaff==null){
            return new ResultWrapper().fail("login0000001","用户获取信息失败!");
        }
        target.setModifyStaff(modifyStaff);
        caseDesignModifyService.CopyCaseToMenu(target);
        return new ResultWrapper().success();
    }


    @ApiOperation(value = "查询案例步骤信息", notes = "查询案例步骤信息")
    @RequestMapping(value = "/CaseStep/queryStepListByOrder", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Map<String, Object>> queryStepListByOrder(@RequestBody Map<String, Object> queryMap) {
        return reCaseStepService.queryStepListByOrder(queryMap);
    }
}
