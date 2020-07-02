package net.northking.cloudtest.controller;

import io.swagger.annotations.ApiOperation;
import net.northking.cloudtest.assist.RedisUtil;
import net.northking.cloudtest.common.Page;
import net.northking.cloudtest.constants.SuccessConstants;
import net.northking.cloudtest.domain.project.CltProjectDto;
import net.northking.cloudtest.domain.project.CltProjectQuery;
import net.northking.cloudtest.domain.project.CltProjectUserInfo;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.domain.testBug.CltTestBug;
import net.northking.cloudtest.domain.user.CltUserAndLogin;
import net.northking.cloudtest.domain.user.CltUserLogin;
import net.northking.cloudtest.dto.analyse.ProjectModule;
import net.northking.cloudtest.dto.testBug.TestBugDTO;
import net.northking.cloudtest.dto.testBug.TestBugListDTO;
import net.northking.cloudtest.enums.DefectCreateType;
import net.northking.cloudtest.exception.GlobalException;
import net.northking.cloudtest.feign.analyse.AppCaseTreeFeignClient;
import net.northking.cloudtest.feign.project.ProjectFeignClient;
import net.northking.cloudtest.feign.project.ProjectTeamFeignClient;
import net.northking.cloudtest.feign.testBug.TestBugFeignClient;
import net.northking.cloudtest.query.project.CltProjectTeamQuery;
import net.northking.cloudtest.query.testBug.CltBugLogQuery;
import net.northking.cloudtest.query.testBug.TestBugQuery;
import net.northking.cloudtest.query.user.UserAndLoginQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.TestBugService;
import net.northking.cloudtest.utils.AssertUtil;
import net.northking.cloudtest.utils.CltUtils;
import net.northking.cloudtest.utils.FormatStringUtil;
import net.northking.cloudtest.utils.ParamVerifyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description: 缺陷控制层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-03-06 15:20
 * @UpdateUser:
 * @Version:0.1
 */

@RestController
@RequestMapping(value ="/cltTestBug")
public class TestBugController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private TestBugService testBugService;

    @Resource
    private ProjectFeignClient projectFeignClient;
    @Resource
    private ProjectTeamFeignClient projectTeamFeignClient;
    @Resource
    private AppCaseTreeFeignClient appCaseTreeFeignClient;

    //日志
    private final static Logger logger = LoggerFactory.getLogger(TestBugController.class);

    //根据executeIds查询缺陷列表
    @PostMapping("/queryTestBugByListExecuteIds")
    public ResultInfo<Page<TestBugListDTO>> queryTestBugByListExecuteIds(@RequestBody TestBugQuery testBugQuery) throws Exception {

        logger.info(" queryTestBugByListExecuteIds start paramData" + testBugQuery.toString());
        //参数校验
        List<String> executeIds = testBugQuery.getExecuteIds();
        if(executeIds==null || executeIds.size()<0){
            throw new GlobalException(ResultCode.EXCEPTION.code(),"executeIds为空！");
        }
        Page<TestBugListDTO> result = null;
        result = testBugService.queryTestBugByListExecuteIds(testBugQuery);
        logger.info(" queryTestBugByListExecuteIds end paramData" + testBugQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS,"查询成功",result);
    }

    //根据stepId查询缺陷列表
    @PostMapping("/queryTestBugByListStepId")
    public ResultInfo<Page<TestBugListDTO>> queryTestBugByListStepId(@RequestBody TestBugQuery testBugQuery) throws Exception {

        logger.info(" queryTestBugByListStepId start paramData" + testBugQuery.toString());

        init(testBugQuery,"queryTestBugByListStepId");

        Page<TestBugListDTO> result = null;

        result = testBugService.queryTestBugByListStepId(testBugQuery);

        logger.info(" queryTestBugByListStepId end paramData" + testBugQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS,"查询成功",result);


    }

    //添加缺陷信息
    @PostMapping("/addCltTestBug")
    public ResultInfo<CltTestBug> addCltTestBug(@RequestBody @Validated TestBugQuery testBugQuery) throws Exception {

        logger.info(" addCltTestBug start paramData" + testBugQuery.toString());

        //参数校验
        init(testBugQuery,"addCltTestBug");

       CltUserLogin cltUserLogin = (CltUserLogin) redisUtil.get(testBugQuery.getAccess_token());

        //设置缺陷发现人员(创建人)
       if(cltUserLogin!=null){

            testBugQuery.setCreateUser(cltUserLogin.getUserId());
        }

        CltTestBug result = testBugService.addCltTestBugInfo(testBugQuery);


        logger.info(" addCltTestBug end paramData");

        return new ResultInfo<>(ResultCode.SUCCESS.code(), SuccessConstants.ADD_CLT_TESTBUG_SUCCESS, result);


    }

   //修改缺陷信息
    @PostMapping("/updateCltTestBugByBugId")
    public ResultInfo<Integer> updateCltTestBugByBugId(@RequestBody TestBugQuery testBugQuery) throws Exception {

        logger.info(" updateCltTestBugByBugId start paramData" + testBugQuery.toString());
        //参数校验
        init(testBugQuery,"updateCltTestBugByBugId");
        //设置修改人
        CltUserLogin cltUserLogin = (CltUserLogin) redisUtil.get(testBugQuery.getAccess_token());
        //设置缺陷修改人员
        if(cltUserLogin!=null){
            testBugQuery.setUpdateUser(cltUserLogin.getUserId());
        }
        logger.info(" queryCltUserListByOrgId end paramData");
        Integer result = testBugService.updateCltTestBugInfoByBugId(testBugQuery);
        return new ResultInfo<>(ResultCode.SUCCESS.code(),SuccessConstants.UPDATE_CLT_TESTBUG_SUCCESS,result);
    }

    //查询所有的缺陷的列表/条件查询缺陷列表
    @PostMapping("/queryAllCltTestBugList")
    public ResultInfo<Page<TestBugDTO>>queryAllCltTestBugList(@RequestBody TestBugQuery testBugQuery)throws Exception{

        logger.info(" queryAllCltTestBugList start paramData" + testBugQuery.toString());
        Page<TestBugDTO> result=null;
        //参数校验
        init(testBugQuery,"queryAllCltTestBugList");
        result = testBugService.queryAllCltTestBugList(testBugQuery);
        logger.info(" queryAllCltTestBugList end paramData" + testBugQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS,SuccessConstants.QUERY_CLT_TESTBUG_LIST_SUCCESS,result);
    }

    //根据bugId查询缺陷详细信息
    @PostMapping("/queryCltTestBugByBugId")
    public ResultInfo<TestBugDTO>queryCltTestBugByBugId(@RequestBody TestBugQuery testBugQuery)throws Exception{

        logger.info(" queryCltTestBugByBugId start paramData" + testBugQuery.toString());
        TestBugDTO result=null;
        //参数校验
        init(testBugQuery,"queryCltTestBugByBugId");
        result = testBugService.queryCltTestBugByBugId(testBugQuery);
        logger.info(" queryCltTestBugByBugId end paramData" + testBugQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS,SuccessConstants.QUERY_CLT_TESTBUG_BY_BUG_ID_SUCCESS,result);
    }

    //查询项目下的缺陷发现人员列表
    @PostMapping("/queryAllCreateUsersByProId")
    public ResultInfo<List<TestBugDTO>> queryAllCreateUsersByProId(@RequestBody TestBugQuery testBugQuery)throws Exception{

        logger.info(" queryAllCreateUsersByProId start paramData" + testBugQuery.toString());
        //参数校验
        init(testBugQuery,"queryAllCreateUsersByProId");
        List<TestBugDTO> testBugDTOS = testBugService.queryAllCreateUsersByProId(testBugQuery);
        logger.info(" queryAllCreateUsersByProId end paramData" + testBugQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS,SuccessConstants.QUERY_ALL_CREATE_USER_LIST_BY_PRO_ID_SUCCESS,testBugDTOS);
    }




    //查询项目下的缺陷分配人员列表
    @PostMapping("/queryAllReceiverUsersByProId")
    @ApiOperation(value = "查询缺陷发现人", notes = "查询缺陷发现人", response = TestBugDTO.class)
    public ResultInfo<List<TestBugDTO>> queryAllReceiverUsersByProId(@RequestBody TestBugQuery testBugQuery)throws Exception{

        logger.info(" queryAllReceiverUsersByProId start paramData" + testBugQuery.toString());
        //参数校验
        init(testBugQuery,"queryAllReceiverUsersByProId");
        List<TestBugDTO> testBugDTOS = testBugService.queryAllReceiverUsersByProId(testBugQuery);
        logger.info(" queryAllReceiverUsersByProId end paramData" + testBugQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS,SuccessConstants.QUERY_ALL_RECEIVER_USER_LIST_BY_PRO_ID_SUCCESS,testBugDTOS);
    }

    //添加信息记录表
    @PostMapping("/addCltBugLogInfo")
    public ResultInfo<CltBugLog> addCltBugLogInfo(@RequestBody TestBugQuery testBugQuery)throws Exception{

        logger.info(" addCltBugLogInfo start paramData" + testBugQuery.toString());
        //参数校验
        init(testBugQuery,"addCltBugLogInfo");
        //设置修改人
        CltUserLogin cltUserLogin = (CltUserLogin) redisUtil.get(testBugQuery.getAccess_token());
        //设置缺陷修改人员
        if(cltUserLogin!=null){
            testBugQuery.setModifier(cltUserLogin.getUserId());
        }
        //设置修改时间
        testBugQuery.setLogTime(new Date());
        CltBugLog cltBugLog = testBugService.addCltBugLogInfo(testBugQuery);
        logger.info(" addCltBugLogInfo end paramData" + testBugQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS,SuccessConstants.ADD_CLT_BUG_LOG_INFO_SUCCESS,cltBugLog);
    }

    //添加信息记录表
    @PostMapping("/addCltProjectLogInfo")
    public ResultInfo<CltBugLog> addCltProjectLogInfo(@RequestBody CltBugLogQuery cltBugLogQuery)throws Exception{

        logger.info(" addCltProjectLogInfo start paramData" + cltBugLogQuery.toString());

        //设置修改人
        CltUserLogin cltUserLogin = (CltUserLogin) redisUtil.get(cltBugLogQuery.getAccess_token());

        //设置缺陷修改人员
        if(cltUserLogin!=null){

            cltBugLogQuery.setModifier(cltUserLogin.getUserId());
        }

        CltBugLog cltBugLog = testBugService.addCltProjectLogInfo(cltBugLogQuery);

        logger.info(" addCltProjectLogInfo end " );

        return new ResultInfo<>(ResultCode.SUCCESS,cltBugLog);
    }

    //查询信息记录
    @PostMapping("/queryCltProjectLog")
    public ResultInfo<List<CltBugLog>> queryCltProjectLog(@RequestBody CltBugLogQuery cltBugLogQuery)throws Exception{

        logger.info(" queryCltProjectLog start paramData" + cltBugLogQuery.toString());

        List<CltBugLog> cltBugLog = testBugService.queryCltBugLogList(cltBugLogQuery);

        logger.info(" queryCltProjectLog end " );

        return new ResultInfo<>(ResultCode.SUCCESS,cltBugLog);


    }

    //查询遗留bugA或bugB总数
    @PostMapping("/countCltBugAOrBugB")
    public ResultInfo<Integer> countCltBugAOrBugB(@RequestBody TestBugQuery testBugQuery)throws Exception{

        logger.info(" countCltBugAOrBugB start paramData" + testBugQuery.toString());

        int cltBugLog = testBugService.countBugAOrB(testBugQuery);

        logger.info(" countCltBugAOrBugB end " );

        return new ResultInfo<>(ResultCode.SUCCESS,cltBugLog);

    }

    //查询遗留其它类型缺陷总数
    @PostMapping("/countOtherCltBug")
    public ResultInfo<Integer> countOtherCltBug(@RequestBody TestBugQuery testBugQuery)throws Exception{

        logger.info(" countOtherCltBug start paramData" + testBugQuery.toString());

        int cltBugLog = testBugService.countBugOthers(testBugQuery);

        logger.info(" countOtherCltBug end " );

        return new ResultInfo<>(ResultCode.SUCCESS,cltBugLog);

    }

    //删除项目风险信息
    @PostMapping("/deleteCltProjectLog")
    public ResultInfo<Integer> deleteCltProjectLog(@RequestBody CltBugLogQuery cltBugLogQuery)throws Exception{

        logger.info(" deleteCltProjectLog start paramData" + cltBugLogQuery.toString());

        int cltBugLog = testBugService.deleteCltProjectLogInfo(cltBugLogQuery);

        logger.info(" deleteCltProjectLog end " );

        return new ResultInfo<>(ResultCode.SUCCESS,cltBugLog);

    }





    @PostMapping("/findProjectList")
    @ApiOperation(value = "获取项目列表", notes = "获取项目列表", response = CltProjectDto.class)
    public ResultInfo<List<CltProjectDto>> findProjectList(@RequestBody CltProjectQuery query) throws Exception {
        logger.info("findProjectList parameter"+query.toString());
        return projectFeignClient.findProjectList(query);
    }



    /**
     * 缺陷库的创建人
     * @return
     */
    @PostMapping(value = "/queryAllCreateUsers")
    @ApiOperation(value = "查询缺陷创建人", notes = "查询缺陷库创建人", response = CltUserAndLogin.class)
    public ResultInfo<Page<TestBugDTO>> queryAllCreateUsers(@RequestBody TestBugQuery testBugQuery){
        Page<TestBugDTO> data =  testBugService.queryAllCreateUsers( testBugQuery);
        return new ResultInfo<>(ResultCode.SUCCESS.code(),"查询成功",data);
    }
    @PostMapping(value = "/queryAllCltTestCaseList")
    @ApiOperation(value = "查询模块列表", notes = "查询模块列表", response = ProjectModule.class)
    public ResultInfo<Page<ProjectModule>> queryTestCaseNameList(@RequestBody TestBugQuery testBugQuery){
        Page<ProjectModule> list = testBugService.queryAllCaseList(testBugQuery);
        return new ResultInfo<>(ResultCode.SUCCESS.code(),"查询成功",list);
    }

    /**
     * 缺陷库的分配人员列表
     * @return
     */
    @PostMapping(value = "/queryAllReceiverUsers")
    @ApiOperation(value = "查询缺陷库发现人", notes = "查询缺陷发现人", response = CltUserAndLogin.class)
    public ResultInfo<Page<TestBugDTO>> queryAllReceiverUsers(@RequestBody TestBugQuery testBugQuery){
        Page<TestBugDTO> data =  testBugService.queryAllReceiverUsers(testBugQuery);
        return new ResultInfo<>(ResultCode.SUCCESS.code(),"查询成功",data);
    }
    //查询缺陷库所有的缺陷的列表/条件查询缺陷列表
    @PostMapping("/queryAllCltTestBugListForAllPro")
    public ResultInfo<Page<TestBugDTO>>queryAllCltTestBugListForAllPro(@RequestBody TestBugQuery testBugQuery)throws Exception{

        logger.info(" queryAllCltTestBugList start paramData" + testBugQuery.toString());
        Page<TestBugDTO> result=null;
        //参数校验
        result = testBugService.queryAllCltTestBugListForAllPro(testBugQuery);
        logger.info(" queryAllCltTestBugList end paramData" + testBugQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS,SuccessConstants.QUERY_CLT_TESTBUG_LIST_SUCCESS,result);
    }
    //删除项目下的缺陷
    @PostMapping("/deleteBugByProIds")
    public ResultInfo<Integer>deleteBugByProIds(@RequestBody TestBugQuery testBugQuery)throws Exception{

        logger.info(" deleteBugByProIds start paramData" + testBugQuery.toString());

        //参数校验
        Integer result = testBugService.deleteBugByProIds(testBugQuery);
        logger.info(" deleteBugByProIds end paramData" + testBugQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS,SuccessConstants.QUERY_CLT_TESTBUG_LIST_SUCCESS,result);
    }

    


    //参数校验的方法
    public static void init(TestBugQuery testBugQuery, String funcCode) throws Exception {

        ParamVerifyUtil paramVerifyUtil = new ParamVerifyUtil();

        Map<String,Object> dataMap = CltUtils.beanToMap(testBugQuery);

        if ("addCltTestBug".equals(funcCode)) {

            //获取缺陷创建类型
            String createType=testBugQuery.getCreateType();

            //如果创建类型为A:以下为必输项
            if(createType.equals(DefectCreateType.CLIENT.getCode())){
                paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                        "batchId","caseId","stepId");
            }

        }else if ("updateCltTestBugByBugId".equals(funcCode)){


            String strFuncCode=testBugQuery.getFuncCode();

            //0：页面更新 1：状态更新/注释更新
            if(StringUtils.isEmpty(strFuncCode)){
                throw new GlobalException(ResultCode.INVALID_PARAM.code(),"funcCode不能为空！");
            }
            if(strFuncCode.equals("0")){
                paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                        "bugId");
            }else if(strFuncCode.equals("1")){
                paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                        "bugId","status");
            }

        }else if("queryCltTestBugByBugId".equals(funcCode)){
            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "bugId");


        }else if("queryAllCltTestBugList".equals(funcCode)){
            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");

        }else if("queryAllCreateUsersByProId".equals(funcCode)){
            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "proId");
        }else if("uploadCltTestBug".equals(funcCode)){
            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "bugId");
        }else if("downloadCltTestBug".equals(funcCode)){
            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "localFilePath");
        }else if("addCltBugLogInfo".equals(funcCode)){
            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "bugId");
        }

        }

}
