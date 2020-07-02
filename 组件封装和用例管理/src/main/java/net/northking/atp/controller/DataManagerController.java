package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.DataDictionary;
import net.northking.atp.db.persistent.DataDictionaryEnum;
import net.northking.atp.db.service.DataDictionaryEnumService;
import net.northking.atp.db.service.DataDictionaryService;
import net.northking.atp.entity.InterfaceDataDictionary;
import net.northking.atp.impl.CaseDesignHisServiceImpl;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.Constant;
import net.northking.atp.util.RedisTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by Administrator on 2019/3/18 0018.
 */
@RestController
@Api(tags = {"数据字典"}, description = "数据字典的维护以及查询接口")
@RequestMapping(value = "/dataDictionary")
public class DataManagerController {
    @Autowired
    private DataDictionaryService dataService;
    @Autowired
    private DataDictionaryEnumService dataEnumService;
    @Autowired
    private CaseDesignHisServiceImpl caseDesignHisService;
    @Autowired
    private RedisTools redisTools;
    /**
     * 数据字典全量查询
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "数据字典_全量", notes = "数据字典全量查询")
    @RequestMapping(value = "/queryDataDictionaryAll", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Map<String,Object>> queryDataDictionaryAll(){
        //查询全部数据
        List<Map<String,Object>> dataList = dataService.queryDataDictionaryInfo();
        Map<String,Object> dataMap = new HashMap<String,Object>();
        for(Map<String,Object> data : dataList){
            if(dataMap.containsKey(data.get("field"))){
                List<Map<String,Object>> fieldList =
                        (List<Map<String,Object>>) dataMap.get(data.get("field"));
                fieldList.add(data);
            }else {
                List<Map<String,Object>> fieldList = new ArrayList<Map<String,Object>>();
                fieldList.add(data);
                dataMap.put(data.get("field")+"",fieldList);
            }
        }
        return new ResultWrapper<Map<String,Object>>().success(dataMap);
    }

    /**
     * 数据字典数据插入
     * @param
     * @return 接口返回
     */
    @ApiOperation(value = "数据字典_数据插入", notes = "数据字典_数据插入")
    @RequestMapping(value = "/insertDataInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insertDataInfo(@RequestHeader(name = "Authorization") String authorization, @RequestBody DataDictionary target){
        CaseDesignTools tools = new CaseDesignTools();
        DataDictionary query = new DataDictionary();
        query.setField(target.getField());
        List<DataDictionary> dataList = dataService.query(query);
        if(dataList.size()>0){
            return new ResultWrapper().fail("dataDictionary0001","此字段已存在，请无重复添加");
        }
        target.setId(tools.getUUID());
        target.setIsValid("1");
        target.setModifyStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        target.setModifyTime(new Date());
        dataService.insert(target);
        return new ResultWrapper().success(target);
    }

    /**
     * 数据字典 字段数据枚举 插入
     * @param
     * @return 接口返回
     */
    @Transactional
    @ApiOperation(value = "数据字典_数据枚举插入", notes = "数据字典_数据枚举插入")
    @RequestMapping(value = "/insertEnumDataInfo", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper insertEnumDataInfo(@RequestBody InterfaceDataDictionary target){
        if(target.getId()==null){
            return new ResultWrapper().fail("caseSet0001","请确认参数是否完整");
        }
        CaseDesignTools tools = new CaseDesignTools();
        List<DataDictionaryEnum> enumList = target.getEnumList();
        int order = 0;
        for(DataDictionaryEnum one : enumList){
            if(one.getId() == null){
                one.setId(tools.getUUID());
            }
            one.setField(target.getField());
            one.setFieldOrder(tools.getOrder(order));
            one.setFieldId(target.getId());
            one.setIsValid("1");
            order++;
        }
        DataDictionaryEnum delete = new DataDictionaryEnum();
        delete.setField(target.getField());
        dataEnumService.insertByBatch(enumList);
        return new ResultWrapper().success();
    }

    /**
     * 分页查询 数据字典——定义
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 数据字典表", notes = "分页查询 数据字典表")
    @RequestMapping(value = "/queryByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<DataDictionary>> queryByPage(@RequestBody QueryByPage<DataDictionary> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        DataDictionary dataDictionary = queryByPage.getQuery();
        CaseDesignTools tools = new CaseDesignTools();
        if(dataDictionary.getField() != null){
            String field= "%"+dataDictionary.getField()+"%";
            dataDictionary.setField(field);
        }
        Pagination<DataDictionary> result = dataService.selectDataDictionary(
                dataDictionary, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<DataDictionary>>().success(result);
    }
}
