package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.MdComponentInfo;
import net.northking.atp.db.persistent.MdComponentParameter;
import net.northking.atp.db.persistent.ReComponentInfo;
import net.northking.atp.db.persistent.ReComponentParameter;
import net.northking.atp.db.service.*;
import net.northking.atp.entity.InterfaceComponentInfo;
import net.northking.atp.entity.InterfaceComponentPacModify;
import net.northking.atp.entity.InterfaceComponentPackage;
import net.northking.atp.impl.ComponentInfoServiceImpl;
import net.northking.atp.impl.ComponentModifyServiceImpl;
import net.northking.atp.util.TargetTransformTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 高级组件封装
 * Created by Administrator on 2019/4/1 0001.
 */
@RestController
@Api(tags = {"案例组件"}, description = "案例组件_高级组件维护")
@RequestMapping(value = "/caseComponent")
public class ComponentPackageController {
    @Autowired
    private ReComponentInfoService reComponentInfoService; //组件信息表
    @Autowired
    private ReComponentPackageService reComponentPackageService; //组件封装表
    @Autowired
    private ReComponentParameterService reComponentParameterService; //组件参数表
    @Autowired
    private ComponentInfoServiceImpl componentInfoService;
    @Autowired
    private ComponentModifyServiceImpl componentModifyService;
    @Autowired
    private MdComponentInfoService mdComponentInfoService;
    @Autowired
    private ReComponentPackageService reComponentPackageServicel;
    @Autowired
    private MdComponentParameterService mdComponentParameterService;
    /**
     * 新增 组件封装表-整体删增
     * @param target 组件封装表
     * @return 接口返回
     *
     * 统一接口于2019-05-20 废弃

    @Transactional
    @ApiOperation(value = "新增 组件封装表", notes = "新增 组件封装表")
    @RequestMapping(value = "/ReComponentPackage/saveComponentPackage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper saveComponentPackage(@RequestBody InterfaceComponentInfo target)
    {
        if(target.getProjectId() == null || target.getId() == null){
            return new ResultWrapper().fail("001","错误,请确认报文正确");
        }
        CaseDesignTools tools = new CaseDesignTools();
        //先完成原始封装删除
        ReComponentPackage cpDelete = new ReComponentPackage();
        cpDelete.setProjectId(target.getProjectId());
        cpDelete.setComponentId(target.getId());
        reComponentPackageService.deleteByExample(cpDelete);
        //删除完成在整体新增
        int order = 1;
        List<InterfaceComponentPackage> compList = target.getPackageList();
        List<ReComponentParameter>  comParamList = new ArrayList<ReComponentParameter>();
        if(compList != null && compList.size()>0){
            //批量
            List<ReComponentPackage> cpList = new ArrayList<ReComponentPackage>();
            for(InterfaceComponentPackage comMap : compList){
                ReComponentPackage reCP = new ReComponentPackage();
                reCP.setId(UUID.randomUUID().toString().replace("-", ""));
                reCP.setComponentId(target.getId());
                reCP.setProjectId(target.getProjectId());
                reCP.setBasisComponentId(comMap.getId());
                reCP.setBasisComponentOrder(tools.getOrder(order));
                reCP.setProjectId(target.getProjectId());
                reCP.setModifyStaff(target.getStaffName());
                reCP.setModifyTime(new Date());
                cpList.add(reCP);

                int paramOrder = 1;
                for(ReComponentParameter one : comMap.getParamList()){
                    if(one.getComponentId().equals(reCP.getBasisComponentId())){
                        one.setRunComponentId(reCP.getBasisComponentId());
                        one.setId(UUID.randomUUID().toString().replace("-", ""));
                    }
                    one.setParameterOrder(tools.getOrder(paramOrder));
                    one.setRunComponentOrder(tools.getOrder(order));
                    one.setComponentId(reCP.getComponentId());
                    one.setInOrOut("0");//内部运行参数
                    comParamList.add(one);
                    paramOrder++;
                }
                order++;
            }
            if(cpList.size()>0){
                reComponentPackageService.insertByBatch(cpList);
            }
        }
        //整合封装组件的所有参数-整体增删
        ReComponentParameter reComponentParameter = new ReComponentParameter();
        reComponentParameter.setProjectId(target.getProjectId());
        reComponentParameter.setComponentId(target.getId());
        reComponentParameter.setInOrOut("0");
        //删除原组件对应的全部参数
        reComponentParameterService.deleteByExample(reComponentParameter);

        //新增更改过后所有组件的参数
        if(comParamList.size()>0){
            componentInfoService.insertComponentParam(comParamList,target);
        }
        return new ResultWrapper().success();
    }
     */


    /**
     * 查询 组件封装信息_正式表
     * @param target 组件注册表
     * @return 接口返回

    @ApiOperation(value = "查询 组件封装信息", notes = "查询组件封装信息")
    @RequestMapping(value = "/ReComponentPackage/queryBasicComponentList", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<InterfaceComponentPackage>> queryBasicComponentList(@RequestBody InterfaceComponentInfo target)
    {
        List<InterfaceComponentPackage> result = componentInfoService.queryComponentAndParamList(target);
        return new ResultWrapper<List<InterfaceComponentPackage>>().success(result);
    }
     */

    /**
     * 查询 组件封装信息_修改表
     * @param target 组件注册表
     * @return 接口返回
     */
    @ApiOperation(value = "查询 组件封装信息", notes = "查询组件封装信息")
    @RequestMapping(value = "/ReComponentPackage/queryComponentInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<InterfaceComponentInfo> queryComponentInfo(@RequestBody InterfaceComponentInfo target)
    {
        TargetTransformTools tools = new TargetTransformTools();
        MdComponentInfo mdComponentInfo = mdComponentInfoService.findByPrimaryKey(target.getId());
        InterfaceComponentInfo result = tools.tranMdComInfoToInter(mdComponentInfo);
        List<InterfaceComponentPackage> list = componentModifyService.queryComponentAndParamList(target);
        result.setPackageList(list);
        //查询高级组件自定义参数
        MdComponentParameter reCompParam = new MdComponentParameter();
        reCompParam.setProjectId(target.getProjectId());
        reCompParam.setComponentId(target.getId());
        reCompParam.setInOrOut("1");//查询外部参数
        List<MdComponentParameter> paramList = mdComponentParameterService.queryCustomParamInfo(reCompParam);
        result.setParamList(tools.transModifyToFormal(paramList));
        return new ResultWrapper<InterfaceComponentInfo>().success(result);
    }


    /**
     * 查询全部 组件参数表
     * 用于选择组件后
     * @param target 查询条件
     * @return 接口返回
     */
    @ApiOperation(value = "查询全部 组件参数表", notes = "查询全部 组件参数表")
    @RequestMapping(value = "/ReComponentParameter/queryParamList", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<ReComponentParameter>> queryParamList(@RequestBody ReComponentParameter target)
    {
        ReComponentInfo reComponentInfo = reComponentInfoService.findByPrimaryKey(target.getComponentId());
        int i = 0;
        if("0".equals(reComponentInfo.getComponentFlag())){
            //基础组件
            target.setProjectId("SYSTEM");
        }else if (!"4".equals(reComponentInfo.getComponentFlag())){
            //高级组件
            target.setInOrOut("1");
            i=1;
        }
        List<ReComponentParameter> result = reComponentParameterService.queryCustomParamInfo(target);
        if(i==0){
            //基础组件增加双引号
            for(ReComponentParameter param : result){
                if(param.getDefaultValue() !=null && !"".equals(param.getDefaultValue()) && "0".equals(param.getParameterFlag())){
                    //有默认值
                    String newValue = "";
                    if(param.getDefaultValue().contains("\"")){
                        newValue = "'"+param.getDefaultValue()+"'";
                    }else{
                        newValue = "\""+param.getDefaultValue()+"\"";
                    }
                    param.setDefaultValue(newValue);
                }
            }
        }
        return new ResultWrapper<List<ReComponentParameter>>().success(result);
    }

    @ApiOperation(value = "条件查询")
    @RequestMapping(value = "/ComponentPackage/queryComponentByOrder",
            method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Map<String, Object>> queryComponentByOrder(@RequestBody Map<String, Object> query) {
        return reComponentPackageServicel.queryComponentByOrder(query);
    }

}
