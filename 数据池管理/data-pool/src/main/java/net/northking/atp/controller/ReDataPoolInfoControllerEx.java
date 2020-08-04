package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.impl.ReDataPoolInfoServiceImpl;
import net.northking.atp.db.persistent.*;
import net.northking.atp.db.service.ReBusinessRulesService;
import net.northking.atp.db.service.ReDataPoolInfoService;
import net.northking.atp.db.service.ReDataPoolService;
import net.northking.atp.db.service.ReDataPoolValueService;

import net.northking.atp.entity.InterfaceDataPoolCopy;
import net.northking.atp.service.BusinessRulesService;
import net.northking.atp.service.DataPoolService;
import net.northking.atp.service.ReDataPoolInfoServiceEx;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Administrator on 2019/3/18 0018.
 */
@RestController
@Api(tags = {"测试数据池"}, description = "测试数据池以及查询接口")
@RequestMapping(value = "/extend")
public class ReDataPoolInfoControllerEx {
    @Autowired
    private DataPoolService dataPoolService;
    @Autowired
    private ReDataPoolService reDataPoolService;
    @Autowired
    private ReDataPoolInfoService reDataPoolInfoService;
    @Autowired
    private ReDataPoolInfoServiceEx reDataPoolInfoServiceEx;
    @Autowired
    private BusinessRulesService businessRulesService;
    private static final Logger logger = LoggerFactory.getLogger(ReDataPoolInfoControllerEx.class);
    /**
     *  数据库持久层服务r
     */
   // @Autowired
   // private ReDataPoolValueService reDataPoolValueService;

    /**
     * 测试数据池全局参数查询
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "数据池全局参数_全量", notes = "数据池全局参数全量查询")
    @RequestMapping(value = "/queryGlobalDataPoolAll", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<ReDataPoolInfo>> queryGlobalDataPoolAll(){
        ReDataPoolInfo param=new ReDataPoolInfo();
        param.setGlobalName("GLOBAL");

        List<ReDataPoolInfo> reDataPoolInfoList =reDataPoolInfoService.query(param);


        ResultWrapper<List<ReDataPoolInfo>> result= new ResultWrapper<List<ReDataPoolInfo>>();
        return result.success(reDataPoolInfoList);
    }

    /**
     * 分页查询
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 ", notes = "分页查询 支持 别名 键模糊查询")
    @RequestMapping(value = "/ReDataPoolInfo/queryByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<ReDataPoolInfo>> queryByPage(@RequestBody QueryByPage<ReDataPoolInfo> queryByPage)
    {
        logger.info("queryByPage输入参数:"+queryByPage.toString());
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        orderBy.addOrderBy("createTime","desc");
        Pagination<ReDataPoolInfo> result = reDataPoolInfoServiceEx.query(queryByPage.getQuery(), orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<ReDataPoolInfo>>().success(result);
    }
    /**
     * 测试数据池全量查询
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "测试数据池_全量", notes = "测试数据池全量查询")
    @RequestMapping(value = "/queryDataPoolAll", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<ReDataPoolInfo>> queryDataPoolAll(){
        ReDataPoolInfo param=new ReDataPoolInfo();
        param.setGlobalName("GLOBAL");

        List<ReDataPoolInfo> reDataPoolInfoList =reDataPoolInfoService.query(param);


        ResultWrapper<List<ReDataPoolInfo>> result= new ResultWrapper<List<ReDataPoolInfo>>();
        return result.success(reDataPoolInfoList);
    }

    /**
     * 查询带参数值返回数据
     *
     * @param target 查询条件
     * @return 接口返回
     */
    @ApiOperation(value = "查询带参数值返回数据 ", notes = "查询带参数值返回数据 ")
    @RequestMapping(value = "/ReDataPoolInfo/queryDataPoolValueByDataPoolInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<ReDataPoolInfoParam>> queryDataPoolValueByDataPoolInfo(@RequestBody ReDataPoolInfo target)
    {
        logger.info("queryDataPoolValueByDataPoolInfo 输入参数:"+target.toMap());

        List<ReDataPoolInfoParam> reDataPoolInfoParamList=reDataPoolInfoServiceEx.queryDataPoolValueByDataPoolInfo(target);
        return new ResultWrapper<List<ReDataPoolInfoParam>>().success(reDataPoolInfoParamList);
    }

    /**
     * 查询全局域值
     * @param propKey
     * @return
     */
    public ReDataPoolInfoParam getGlobalValue(String propKey){
        ReDataPoolInfo param=new ReDataPoolInfo();
        param.setPropKey(propKey);
        param.setGlobalName("GLOBAL");

        List<ReDataPoolInfoParam> reDataPoolInfoParamList=reDataPoolInfoServiceEx.queryDataPoolValueByDataPoolInfo(param);
        ReDataPoolInfoParam result=null;
        if(reDataPoolInfoParamList!=null && reDataPoolInfoParamList.size()>0){
            result=reDataPoolInfoParamList.get(0);

        }
        return result;
    }
    /**
     * 查询带参数值返回数据   同名key的值  复盖顺序 环境->项目->全局
     *
     * @param target 查询条件
     * @return 接口返回
     */
    @ApiOperation(value = "查询带参数值返回数据 ", notes = "同名key的值  复盖顺序 环境->项目->全局")
    @RequestMapping(value = "/ReDataPoolInfo/findDataPoolValueByDataPoolInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<ReDataPoolInfoParam> findDataPoolValueByDataPoolInfo(@RequestBody ReDataPoolInfo target)
    {
        logger.info("findDataPoolValueByDataPoolInfo global:"+target.getGlobalName()+" projectId:"+target.getProjectId()+" profileId:"+target.getProfileId()+" propKey:"+target.getPropKey());
        //zcy-判断是动态还是静态数据
        ReDataPool query = new ReDataPool();
        query.setProjectId(target.getProjectId());
        query.setDataName(target.getPropKey());
        String str = dataPoolService.queryFlagByDataRecord(query);
        if("2".equals(str)){
            return new ResultWrapper().fail("1",target.getPropKey()+"有效数据不存在" );
        }else if("0".equals(str)){
            ReBusinessRules rule = new ReBusinessRules();
            rule.setRuleName(target.getPropKey());
            rule.setProjectId(target.getProjectId());
            String ruleValue = businessRulesService.queryBusinessRuleValue(rule);
            ReDataPoolInfoParam result = new ReDataPoolInfoParam();
            result.setPropValue(ruleValue);
            return new ResultWrapper<ReDataPoolInfoParam>().success(result);
        }

        if( target.getProjectId()==null && target.getProfileId()==null){

            ReDataPoolInfoParam result=getGlobalValue(target.getPropKey());
            if(result!=null  ){
                return new ResultWrapper<ReDataPoolInfoParam>().success(result);
            }
            return new ResultWrapper<ReDataPoolInfoParam>().fail("1",target.getPropKey()+"数据不存在" );
        }

        if(target.getGlobalName()==null && target.getProjectId()==null && target.getProfileId()!=null){
            ReDataPoolInfo param=new ReDataPoolInfo();
            param.setPropKey(target.getPropKey());
            param.setProfileId(target.getProfileId());

            List<ReDataPoolInfoParam> reDataPoolInfoParamList=reDataPoolInfoServiceEx.queryDataPoolValueByDataPoolInfo(param);
            ReDataPoolInfoParam result=null;
            if(reDataPoolInfoParamList!=null && reDataPoolInfoParamList.size()>0){
                result=reDataPoolInfoParamList.get(0);
                return new ResultWrapper<ReDataPoolInfoParam>().success(result);
            }else{
                 result=getGlobalValue(target.getPropKey());
                if(result!=null  ){
                    return new ResultWrapper<ReDataPoolInfoParam>().success(result);
                }
            }

            return new ResultWrapper<ReDataPoolInfoParam>().fail("1",target.getPropKey()+"数据不存在" );
        }

        if(target.getGlobalName()==null && target.getProjectId()!=null && target.getProfileId()==null){
          ReDataPoolInfo param=new ReDataPoolInfo();
          param.setPropKey(target.getPropKey());
          param.setProjectId(target.getProjectId());

          List<ReDataPoolInfoParam> reDataPoolInfoParamList=reDataPoolInfoServiceEx.queryDataPoolValueByDataPoolInfo(param);

          if(reDataPoolInfoParamList!=null && reDataPoolInfoParamList.size()>0){
                ReDataPoolInfoParam findResult=null;

                for(ReDataPoolInfoParam data:reDataPoolInfoParamList){
                    if(data.getProfileId()!=null){
                        findResult=data;
                    }
                }
                if(findResult!=null){
                    return new ResultWrapper<ReDataPoolInfoParam>().success(findResult);
                }

           }else{
              ReDataPoolInfoParam result=getGlobalValue(target.getPropKey());
              if(result!=null  ){
                  return new ResultWrapper<ReDataPoolInfoParam>().success(result);
              }
          }

        }

        if(target.getGlobalName()==null && target.getProjectId()!=null && target.getProfileId()!=null){
            ReDataPoolInfo param=new ReDataPoolInfo();
            param.setPropKey(target.getPropKey());
            param.setProjectId(target.getProjectId());
            param.setProfileId(target.getProfileId());
            List<ReDataPoolInfoParam> reDataPoolInfoParamList=reDataPoolInfoServiceEx.queryDataPoolValueByDataPoolInfo(param);

            if(reDataPoolInfoParamList!=null && reDataPoolInfoParamList.size()>0){
                ReDataPoolInfoParam findResult=null;

                for(ReDataPoolInfoParam data:reDataPoolInfoParamList){
                    if(data.getProfileId()!=null){
                        findResult=data;
                    }
                }
                if(findResult!=null){
                    return new ResultWrapper<ReDataPoolInfoParam>().success(findResult);
                }

            }else{//只查项目一级是否存在对应KEY
                ReDataPoolInfo param2=new ReDataPoolInfo();
                param2.setPropKey(target.getPropKey());
                param2.setProjectId(target.getProjectId());
                reDataPoolInfoParamList=reDataPoolInfoServiceEx.queryDataPoolValueByDataPoolInfo(param2);

                if(reDataPoolInfoParamList!=null && reDataPoolInfoParamList.size()>0){
                    ReDataPoolInfoParam findResult=null;

                    for(ReDataPoolInfoParam data:reDataPoolInfoParamList){
                        if(data.getProjectId()!=null){
                            findResult=data;
                        }
                    }
                    if(findResult!=null){
                        return new ResultWrapper<ReDataPoolInfoParam>().success(findResult);
                    }

                }else{
                  ReDataPoolInfoParam result=getGlobalValue(target.getPropKey());
                  if(result!=null  ){
                     return new ResultWrapper<ReDataPoolInfoParam>().success(result);
                   }
                }
            }

        }
        return new ResultWrapper<ReDataPoolInfoParam>().fail("1",target.getPropKey()+"数据不存在" );
    }

    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 接口返回
     */
    @ApiOperation(value = "查询 ", notes = "根据主键查询测试数据池值的返回数据 ",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/ReDataPoolInfo/findDataPoolValueByPrimaryKey", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<ReDataPoolInfoParam> findDataPoolValueByPrimaryKey(@RequestParam("id") String id)
    {
        logger.info("findDataPoolValueByPrimaryKey输入参数:"+id);
        ResultWrapper<ReDataPoolInfoParam> resultWrapper = new ResultWrapper<ReDataPoolInfoParam>();

        if(id!=null && !"".equals(id)){
            ReDataPoolInfoParam record = reDataPoolInfoServiceEx.findDataPoolValueByPrimaryKey(id);
            if (record == null)
            {
                resultWrapper.fail("0000001", "不存在[主键=" + id + "]的记录！");
            } else
            {
                resultWrapper.success(record);
            }
        }else{
            ResultWrapper<ReDataPoolInfoParam> result=new ResultWrapper<ReDataPoolInfoParam>();
            result.fail("1", "请传入参数ID ");
            return result;
        }
        return resultWrapper;
    }
    /**
     * 根据主键删除
     *
     * @param id 主键
     * @return 接口返回
     */
    @ApiOperation(value = "删除 ", notes = "根据主键删除 ",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @RequestMapping(value = "/ReDataPoolInfo/deleteByPrimaryKey", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteByPrimaryKey(@RequestParam("id") String id)
    {
        logger.info("deleteByPrimaryKey输入参数:"+id);
        if(id==null && "".equals(id)){
            ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
            result.fail("1", "请传入参数ID ");
            return result;
        }

        reDataPoolInfoServiceEx.delete(id);
      //  reDataPoolInfoService.deleteByPrimaryKey(lid);
      //  reDataPoolInfoServiceEx.deleteReDataPoolByDataPoolInfoId(lid);
        //zcy-删除数据记录
        ReDataPool delete = new ReDataPool();
        delete.setDataId(id);
        reDataPoolService.deleteByExample(delete);
        return new ResultWrapper().success();
    }
    /**
     * 新增
     *
     * @param target
     * @return 接口返回
     */
    @ApiOperation(value = "新增 ", notes = "新增 ")
    @RequestMapping(value = "/ReDataPoolInfo/insert", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<ReDataPoolInfo> insert(@RequestBody ReDataPoolInfoParam target)
    {
        logger.info("insert输入参数:"+target.toMap());

        if(target.getPropValue()==null ){
            ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
            result.fail("1", "请输入"+target.getPropKey()+"值!");
            return result;
        }
        if(target.getPropValueType()==null ){
            ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
            result.fail("1", "请输入"+target.getPropKey()+"类型!");
            return result;
        }

        if(target.getGlobalName()!=null && (target.getProjectId()!=null && target.getProfileId()!=null) ){
            ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
            result.fail("1", "添加全局参数"+target.getPropKey()+"时，不用指定项目或环境!");
            return result;
        }

        if(target.getGlobalName()!=null && !"".equals(target.getGlobalName())){

            //新增全局测试参数，参数名称不可重复。
            ReDataPoolInfo param=new ReDataPoolInfo();
            param.setGlobalName(target.getGlobalName());
            param.setPropKey(target.getPropKey());
            List<ReDataPoolInfo> reDataPoolInfoList = reDataPoolInfoService.query(param);

            if(reDataPoolInfoList!=null && reDataPoolInfoList.size()>0){
                ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
                result.fail("1", "全局参数"+target.getPropKey()+"已经存在!");
                return result;
            }

//            reDataPoolInfoService.insert(target);
//            //取到刚插入数据的ID
//            ReDataPoolInfo exitsParam=new ReDataPoolInfo();
//            exitsParam.setGlobalName(target.getGlobalName());
//            exitsParam.setPropKey(target.getPropKey());
//            List<ReDataPoolInfo> reDataPoolInfoListExist = reDataPoolInfoService.query(exitsParam);
//            if(reDataPoolInfoListExist!=null && reDataPoolInfoListExist.size()>0){
//                ReDataPoolInfo currentData=  reDataPoolInfoListExist.get(0);
//                target.setId(currentData.getId());
//            }
        }else if(target.getProjectId()!=null && !"".equals(target.getProjectId()) && (target.getProfileId()==null ||"".equals(target.getProfileId()))){
            //本项目、；相同使用范围的测试参数名称不可重复
            ReDataPoolInfo param=new ReDataPoolInfo();
            param.setProjectId(target.getProjectId());
            param.setPropKey(target.getPropKey());
            List<ReDataPoolInfo> reDataPoolInfoList = reDataPoolInfoServiceEx.queryForDistinct(param);

            if(reDataPoolInfoList!=null && reDataPoolInfoList.size()>0){
                ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
                result.fail("1", "相同项目、环境参数"+target.getPropKey()+"已经存在!");
                return result;
            }

//            reDataPoolInfoService.insert(target);
//            //取到刚插入数据的ID
//            ReDataPoolInfo exitsParam=new ReDataPoolInfo();
//            exitsParam.setProjectId(target.getProjectId());
//
//            exitsParam.setPropKey(target.getPropKey());
//            List<ReDataPoolInfo> reDataPoolInfoListExist = reDataPoolInfoServiceEx.queryForDistinct(exitsParam);
//            if(reDataPoolInfoListExist!=null && reDataPoolInfoListExist.size()>0){
//                ReDataPoolInfo currentData=  reDataPoolInfoListExist.get(0);
//                target.setId(currentData.getId());
//            }
        }else{
            //本项目、环境；相同使用范围的测试参数名称不可重复
            ReDataPoolInfo param=new ReDataPoolInfo();
            param.setProjectId(target.getProjectId());
            param.setProfileId(target.getProfileId());
            param.setPropKey(target.getPropKey());
            List<ReDataPoolInfo> reDataPoolInfoList = reDataPoolInfoService.query(param);

            if(reDataPoolInfoList!=null && reDataPoolInfoList.size()>0){
                ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
                result.fail("1", "相同项目、环境参数"+target.getPropKey()+"已经存在!");
                return result;
            }

//            reDataPoolInfoService.insert(target);
//            //取到刚插入数据的ID
//            ReDataPoolInfo exitsParam=new ReDataPoolInfo();
//            exitsParam.setProjectId(target.getProjectId());
//            exitsParam.setProfileId(target.getProfileId());
//            exitsParam.setPropKey(target.getPropKey());
//            List<ReDataPoolInfo> reDataPoolInfoListExist = reDataPoolInfoService.query(exitsParam);
//            if(reDataPoolInfoListExist!=null && reDataPoolInfoListExist.size()>0){
//                ReDataPoolInfo currentData=  reDataPoolInfoListExist.get(0);
//                target.setId(currentData.getId());
//            }
        }

//        ReDataPoolValue reDataPoolValueParam=new ReDataPoolValue();
//        reDataPoolValueParam.setDataPoolInfoId(target.getId());
//        reDataPoolValueService.deleteByExample(reDataPoolValueParam);
//        //新值
//        ReDataPoolValue reDataPoolValueNew=new ReDataPoolValue();
//        reDataPoolValueNew.setPropValue(target.getPropValue());
//        reDataPoolValueNew.setDataPoolInfoId(target.getId());
//        reDataPoolValueService.insert(reDataPoolValueNew);
        //ZCY 校验数据池总览记录中动态数据是否存在
        ReDataPool check = new ReDataPool();
        check.setDataName(target.getPropKey());
        check.setDataFalg("1");
        check.setProjectId(target.getProjectId());
        if(dataPoolService.checkRecordExist(check)){
            return new ResultWrapper().fail("dataPool","有效数据在动态数据中已存在同名");
        }


        reDataPoolInfoServiceEx.insert(target);
        return new ResultWrapper<ReDataPoolInfo>().success(target);
    }

    /**
     * 根据主键修改
     *
     * @param target
     * @return 接口返回
     */
    @ApiOperation(value = "修改 ", notes = "根据主键修改 ")
    @RequestMapping(value = "/ReDataPoolInfo/updateByPrimaryKey", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<ReDataPoolInfo> updateByPrimaryKey(@RequestBody ReDataPoolInfoParam target)
    {
        logger.info("updateByPrimaryKey 输入参数:"+target.toMap());
        if(target.getPropValue()==null ){
            ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
            result.fail("1", "请输入"+target.getPropKey()+"值!");
            return result;
        }
        if(target.getPropValueType()==null ){
            ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
            result.fail("1", "请输入"+target.getPropKey()+"类型!");
            return result;
        }

        if(target.getGlobalName()!=null && (target.getProjectId()!=null && target.getProfileId()!=null) ){
            ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
            result.fail("1", "添加全局参数"+target.getPropKey()+"时，不用指定项目或环境!");
            return result;
        }

        if(target.getGlobalName()!=null && !"".equals(target.getGlobalName())){

            //新增全局测试参数，参数名称不可重复。
            ReDataPoolInfo param=new ReDataPoolInfo();
            param.setGlobalName(target.getGlobalName());
            param.setPropKey(target.getPropKey());
            List<ReDataPoolInfo> reDataPoolInfoList = reDataPoolInfoServiceEx.queryForUpdate(param);

            if(reDataPoolInfoList!=null && reDataPoolInfoList.size()>0){
                ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
                result.fail("1", "全局参数"+target.getPropKey()+"已经存在!");
                return result;
            }
          //  reDataPoolInfoService.updateByPrimaryKey(target);
        }else if(target.getProjectId()!=null && !"".equals(target.getProjectId()) && (target.getProfileId()==null ||"".equals(target.getProfileId()))){
            //本项目；相同使用范围的测试参数名称不可重复
            ReDataPoolInfo param=new ReDataPoolInfo();
            param.setId(target.getId());
            param.setProjectId(target.getProjectId());
            param.setPropKey(target.getPropKey());
            List<ReDataPoolInfo> reDataPoolInfoList = reDataPoolInfoServiceEx.queryForUpdate(param);

            if(reDataPoolInfoList!=null && reDataPoolInfoList.size()>0){
                ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
                result.fail("1", "相同项目参数"+target.getPropKey()+"已经存在!");
                return result;
            }
           // reDataPoolInfoService.updateByPrimaryKey(target);
        }else{
            //本项目、环境；相同使用范围的测试参数名称不可重复
            ReDataPoolInfo param=new ReDataPoolInfo();
            param.setId(target.getId());
            param.setProjectId(target.getProjectId());
            param.setProfileId(target.getProfileId());
            param.setPropKey(target.getPropKey());
            List<ReDataPoolInfo> reDataPoolInfoList = reDataPoolInfoServiceEx.queryForUpdate(param);

            if(reDataPoolInfoList!=null && reDataPoolInfoList.size()>0){
                ResultWrapper<ReDataPoolInfo> result=new ResultWrapper<ReDataPoolInfo>();
                result.fail("1", "相同项目、环境参数"+target.getPropKey()+"已经存在!");
                return result;
            }
          //  reDataPoolInfoService.updateByPrimaryKey(target);
        }

//        ReDataPoolValue reDataPoolValueParam=new ReDataPoolValue();
//        reDataPoolValueParam.setDataPoolInfoId(target.getId());
//        reDataPoolValueService.deleteByExample(reDataPoolValueParam);
//        //新值
//        ReDataPoolValue reDataPoolValueNew=new ReDataPoolValue();
//        reDataPoolValueNew.setPropValue(target.getPropValue());
//        reDataPoolValueNew.setDataPoolInfoId(target.getId());
//        reDataPoolValueService.insert(reDataPoolValueNew);
        //ZCY 校验数据池总览记录中动态数据是否存在
        ReDataPool check = new ReDataPool();
        check.setDataName(target.getPropKey());
        check.setDataFalg("1");
        check.setProjectId(target.getProjectId());
        if(dataPoolService.checkRecordExist(check)){
            return new ResultWrapper().fail("dataPool","有效数据在动态数据中已存在同名");
        }


        reDataPoolInfoServiceEx.update(target);
        ReDataPoolInfo newOne = reDataPoolInfoService.findByPrimaryKey(target);
        return new ResultWrapper<ReDataPoolInfo>().success(newOne);
    }


    /**
     * 复制静态数据
     * @param target
     * @return 接口返回
     */
    @ApiOperation(value = "复制粘贴 ", notes = "静态数据的跨环境复制 ")
    @RequestMapping(value = "/ReDataPoolInfo/copyStaticDataPool", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper copyStaticDataPool(@RequestBody InterfaceDataPoolCopy target)
    {
        if(target.getProjectId()==null || target.getIdList()==null){
            return new ResultWrapper().fail("dataPool000002","缺少参数,请完善参数");
        }
        dataPoolService.copyStaticData(target);
        return new ResultWrapper().success();
    }
}
