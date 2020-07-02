package net.northking.cloudtest.controller;

import net.northking.cloudtest.assist.RedisUtil;
import net.northking.cloudtest.constants.SuccessConstants;
import net.northking.cloudtest.domain.attach.CltAttach;
import net.northking.cloudtest.domain.user.CltUserLogin;
import net.northking.cloudtest.enums.DefectCreateType;
import net.northking.cloudtest.exception.GlobalException;
import net.northking.cloudtest.query.attach.AttachQuery;
import net.northking.cloudtest.result.ResultCode;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.AttachService;
import net.northking.cloudtest.utils.CltUtils;
import net.northking.cloudtest.utils.ParamVerifyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @Title:
 * @Description: 附件表信息
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-03-16 11:08
 * @UpdateUser:
 * @Version:0.1
 */


@RestController
@RequestMapping(value ="/cltAttach")
public class AttachController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private AttachService attachService;

    //日志
    private final static Logger logger = LoggerFactory.getLogger(AttachController.class);


    //添加附件表
    @PostMapping("/addCltAttachInfo")
    public ResultInfo<Integer> addCltAttachInfo(@RequestBody AttachQuery attachQuery) throws Exception {

        logger.info(" addCltAttachInfo start paramData" + attachQuery.toString());

        //参数校验
        init(attachQuery,"addCltAttachInfo");

        setCreator(attachQuery);

        Integer result = attachService.addCltAttachInfo(attachQuery);


        logger.info(" addCltAttachInfo end paramData" + attachQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.ADD_CLT_ATTACH_INFO_SUCCESS, result);


    }

    private void setCreator(@RequestBody AttachQuery attachQuery) {
        if(attachQuery.getAccess_token()!=null) {
            CltUserLogin cltUserLogin = (CltUserLogin) redisUtil.get(attachQuery.getAccess_token());

            //设置缺陷发现人员(创建人)
            if (cltUserLogin != null) {

                attachQuery.setCreateUser(cltUserLogin.getUserId());
            }
        }else {
            attachQuery.setCreateUser("autotest");
        }
    }

    /**
     * 添加附件信息
     * （去掉校验）
     * @param query
     * @return
     */
    @PostMapping("/addAttach")
    public ResultInfo<CltAttach> addAttach(@RequestBody AttachQuery query) {
        logger.info(" addAttach start paramData" + query.toString());
        setCreator(query);
        CltAttach attach = attachService.addAttach(query);
        logger.info(" addAttach end");
        ResultInfo<CltAttach> resultInfo = new ResultInfo<>();
        resultInfo.setData(attach);
        resultInfo.setMessage("附件信息添加成功");
        resultInfo.setSuccess(true);
        return resultInfo;
    }

    @PostMapping("/deleteAttach")
    public ResultInfo<Integer> deleteAttach(@RequestBody AttachQuery query) {
        logger.info(" deleteAttach start paramData" + query.toString());
        Integer delNum = attachService.deleteAttach(query);
        logger.info(" deleteAttach end");
        ResultInfo<Integer> resultInfo = new ResultInfo<>();
        resultInfo.setData(delNum);
        resultInfo.setMessage("删除成功");
        resultInfo.setSuccess(true);
        return resultInfo;
    }


    //查询附件列表
    @PostMapping("/queryAllAttachList")
    public ResultInfo<List<CltAttach>> queryAllAttachList(@RequestBody AttachQuery attachQuery) throws Exception {

        logger.info(" queryAllAttachList start paramData" + attachQuery.toString());

        //参数校验
        init(attachQuery,"queryAllAttachList");

        List<CltAttach> result=null;

        String createType=attachQuery.getCreateType();
        // 手动提交或者自动化提交
        if(DefectCreateType.MANUAL.getCode().equals(createType) || DefectCreateType.AUTO_TEST.getCode().equals(createType)){

            result = attachService.queryAllCltAttachList(attachQuery);

        }else{

            if (StringUtils.isEmpty(attachQuery.getExecuteId())){
                throw new GlobalException(ResultCode.INVALID_PARAM.code(),"executeId为空");

            }
            if (StringUtils.isEmpty(attachQuery.getStepId())){
                throw new GlobalException(ResultCode.INVALID_PARAM.code(),"stepId为空");

            }

            attachQuery.setFuncId(attachQuery.getStepId());
            result=attachService.queryAllCltAttachListByExecuteIdAndStepIdAndBugId(attachQuery);

        }




        logger.info(" queryAllAttachList end paramData" + attachQuery.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.QUERY_ALL_CLT_ATTACH_LIST_INFO_SUCCESS, result);


    }

    //删除附件列表
    @PostMapping("/deleteCltAttachInfoByBugId")
    public ResultInfo<Integer> deleteCltAttachInfoByBugId(@RequestBody List<AttachQuery> attachQueries) throws Exception {

        logger.info(" deleteCltAttachInfoByBugId start paramData" + attachQueries.toString());

        //参数校验


        if(attachQueries.size()==0 ||attachQueries==null) {

            return null;
        }

            for (int i = 0; i < attachQueries.size(); i++) {
                AttachQuery attachQuery = attachQueries.get(i);

                String bugId = attachQuery.getBugId();
                String attachId = attachQuery.getAttachId();

                if (StringUtils.isEmpty(bugId)) {
                   throw new GlobalException(ResultCode.INVALID_PARAM.code(),"bugId为空！");

                }

                if(StringUtils.isEmpty(attachId)){

                    throw new GlobalException(ResultCode.INVALID_PARAM.code(),"attachId为空");
                }


            }


        Integer result = attachService.deleteCltAttachInfoByBugId(attachQueries);

        logger.info(" deleteCltAttachInfoByBugId end paramData" + attachQueries.toString());

        return new ResultInfo<>(ResultCode.SUCCESS, SuccessConstants.DELETE_CLT_ATTACH_LIST_INFO_SUCCESS, result);


    }


    //根据executeid查询附件列表
    @PostMapping("/queryAllCltAttachListByExecuteId")
    public ResultInfo<List<CltAttach>> queryAllCltAttachListByExecuteId(@RequestBody AttachQuery attachQuery) throws Exception {

        logger.info(" queryAllCltAttachListByExecuteId start paramData" + attachQuery.toString());
        //参数校验
        String executeId=attachQuery.getExecuteId();
        if(StringUtils.isEmpty(executeId)){
            throw new GlobalException(ResultCode.INVALID_PARAM.code(),"executeId不能为空");
        }
        List<CltAttach> result = attachService.queryAllCltAttachListByExecuteId(attachQuery);
        logger.info(" queryAllCltAttachListByExecuteId end paramData" + attachQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS, "查询附件列表成功", result);
    }

    //根据executeid和stepId查询附件列表
    @PostMapping("/queryAllCltAttachListByExecuteIdAndStepId")
    public ResultInfo<List<CltAttach>> queryAllCltAttachListByExecuteIdAndStepId(@RequestBody AttachQuery attachQuery) throws Exception {

        logger.info(" queryAllCltAttachListByExecuteIdAndStepId start paramData" + attachQuery.toString());
        //参数校验
        String executeId=attachQuery.getExecuteId();
        String stepId=attachQuery.getFuncId();
        if(StringUtils.isEmpty(stepId)){
            throw new GlobalException(ResultCode.INVALID_PARAM.code(),"stepId不能为空");
        }
        if(StringUtils.isEmpty(executeId)){
            throw new GlobalException(ResultCode.INVALID_PARAM.code(),"executeId不能为空");
        }
        List<CltAttach> result = attachService.queryAllCltAttachListByExecuteIdAndStepId(attachQuery);
        logger.info(" queryAllCltAttachListByExecuteIdAndStepId end paramData" + attachQuery.toString());
        return new ResultInfo<>(ResultCode.SUCCESS, "查询附件列表成功", result);
    }


    /**
     * 条件查询附件信息
     * @param query
     * @return
     */
    @PostMapping(value = "/queryAttachOnCondition")
    public ResultInfo<List<CltAttach>> queryAttachOnCondition(@RequestBody AttachQuery query) {
        logger.info(" queryAttachOnCondition start paramData -> 【{}】", query.toString());
        List<CltAttach> result = attachService.queryAttachOnCondition(query);
        logger.info(" queryAllCltAttachListByExecuteIdAndStepId end paramData");
        ResultInfo<List<CltAttach>> resultInfo = new ResultInfo<>();
        resultInfo.setData(result);
        resultInfo.setSuccess(true);
        resultInfo.setMessage("附件查询成功！");
        return resultInfo;

    }

    //参数校验
    private static void init(AttachQuery attachQuery, String funcCode) {

        ParamVerifyUtil paramVerifyUtil = new ParamVerifyUtil();

        Map<String, Object> dataMap = CltUtils.beanToMap(attachQuery);

        if ("addCltAttachInfo".equals(funcCode)) {
            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "bugId", "attachType", "attachName", "attachPath");
        } else if ("queryAllAttachList".equals(funcCode)) {

            paramVerifyUtil.checkNullOrEmpty(dataMap, logger,
                    "bugId","createType");


        }
    }
}