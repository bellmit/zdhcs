package net.northking.atp.controller;

import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.MdCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseSetLink;
import net.northking.atp.db.service.MdCaseDesignInfoService;
import net.northking.atp.db.service.ReCaseDesignInfoService;
import net.northking.atp.db.service.ReCaseSetLinkService;
import net.northking.atp.db.service.ReCaseStepService;
import net.northking.atp.entity.InterfaceCaseInfo;
import net.northking.atp.impl.CaseDesignModifyServiceImpl;
import net.northking.atp.impl.CaseDesignVersionServiceImpl;
import net.northking.atp.impl.CaseStepComponentServiceImpl;
import net.northking.atp.impl.FeignInterfaceServiceImpl;
import net.northking.atp.service.CaseDesignModifyService;
import net.northking.atp.service.FeignInterfaceService;
import net.northking.atp.util.RedisTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/12 0012.
 */
@RestController
@Api(tags = {"用例对外接口"}, description = "用例设计对其他微服务的feign接口")
@RequestMapping(value = "/caseFeign")
public class FeignInterfaceController {
    private static final Logger logger = LoggerFactory.getLogger(FeignInterfaceController.class);
    @Autowired
    private ReCaseDesignInfoService reCaseDesignInfoService;
    @Autowired
    private MdCaseDesignInfoService mdCaseDesignInfoService;
    @Autowired
    private CaseDesignModifyService caseDesignModifyService;
    @Autowired
    private FeignInterfaceServiceImpl feignInterfaceService;
    @Autowired
    private RedisTools redisTools;
    /**
     * 分页查询 案例信息表
     * @param target 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 案例信息表", notes = "分页查询 案例信息表")
    @RequestMapping(value = "/queryCaseByScript", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Integer> queryCaseByScript(@RequestBody MdCaseDesignInfo target)
    {
        int count = (int)mdCaseDesignInfoService.queryCount(target);
        return new ResultWrapper<Integer>().success(count);
    }


    /**
     * 用例脚本批量生成用例
     * @return 接口返回
     */
    @Transactional
    @RequestMapping(value = "/genCaseBatchByScript", method = {RequestMethod.POST})
    public ResultWrapper genCaseBatchByScript(@RequestBody InterfaceCaseInfo target)
    {
        if(target.getAddType() ==null || target.getProjectId()==null || target.getScriptId() ==null){
            //重名
            return new ResultWrapper().fail("caseInfo0000005","参数确实，请确认");
        }
        if (target.getDataDesignDataList()==null || target.getDataDesignDataList().size()<1){
            return new ResultWrapper().fail("caseInfo0000005","无脚本参数");
        }
        if (target.getComponentList()==null || target.getComponentList().size()<1){
            return new ResultWrapper().fail("caseInfo0000005","无用例步骤");
        }
        String staff = redisTools.getRedisUserInfo(target.getAccess_token());
        if(staff==null){
            return new ResultWrapper().fail("caseInfo0000006","用户信息查询失败");
        }
        target.setModifyStaff(staff);
        target.setCreateStaff(staff);
        String message =  feignInterfaceService.insertModifyCaseInfo(target);
        if("".equals(message)){
            return new ResultWrapper().success();
        }else {
            return new ResultWrapper().fail("caseInfo0000001",message);
        }
    }


    /**
     * 智能接口同步至组件
     * @return 接口返回
     */
    @Transactional
    @RequestMapping(value = "/smartPortToComponent", method = {RequestMethod.POST})
    public ResultWrapper smartPortToComponent(@RequestBody Map<String,Object> target)
    {
        boolean result = false;
        if (target.get("projectId")== null || target.get("id")== null ||
                target.get("token")== null || target.get("name")== null){
            result = false;
        }else{
            String modifyStaff = redisTools.getRedisUserInfo(target.get("token")+"");
            if(modifyStaff == null){
                result = false;
            }
            target.put("staff",modifyStaff);
            result = feignInterfaceService.genSmartPortComponent(target);
        }
        return new ResultWrapper().success(result);
    }
}
