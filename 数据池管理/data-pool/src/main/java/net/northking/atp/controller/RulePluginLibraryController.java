package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.persistent.ReRulePluginInfo;
import net.northking.atp.db.persistent.ReRulePluginLibrary;
import net.northking.atp.db.persistent.ReRulePluginParameter;
import net.northking.atp.db.service.ReRulePluginInfoService;
import net.northking.atp.db.service.ReRulePluginLibraryService;
import net.northking.atp.element.core.Library;
import net.northking.atp.element.core.keyword.KeywordInfo;
import net.northking.atp.element.extension.helper.JarLibraryScanner;
import net.northking.atp.entity.DataTest;
import net.northking.atp.impl.RulePluginLibraryServiceImpl;
import net.northking.atp.rule.core.RuleLibrary;
import net.northking.atp.rule.core.RuleLibraryManager;
import net.northking.atp.rule.core.rule.RuleInfo;
import net.northking.atp.rule.core.scanner.RuleLibraryScanner;
import net.northking.atp.service.RulePluginInfoService;
import net.northking.atp.service.RulePluginLibraryService;
import net.northking.atp.util.FunctionTools;
import net.northking.atp.util.RedisTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2019/7/15 0015.
 */
@RestController
@Api(tags = {"规则插件库"}, description = "插件_规则插件库的管理")
@RequestMapping(value = "/rulePluginLibrary")
public class RulePluginLibraryController {

    @Autowired
    private ReRulePluginLibraryService reRulePluginLibraryService;
    @Autowired
    private ReRulePluginInfoService reRulePluginInfoService;
    @Autowired
    private RulePluginLibraryService rulePluginLibraryService;
    @Autowired
    private RulePluginInfoService rulePluginInfoService;
    @Autowired
    private RedisTools redisTools;

    private String filePath;
    //private String filePath = "D:/nkTest";
    private String ip;
    private String port;
    /**
     * 查询 规则插件库列表
     *
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "列表查询 规则插件库", notes = "列表查询 规则插件库")
    @RequestMapping(value = "/queryPluginLibraryByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<Map<String,Object>>> queryPluginLibraryByPage(@RequestBody QueryByPage<ReRulePluginLibrary> queryByPage) {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList()) {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }

        ReRulePluginLibrary reRulePlugLibrary = queryByPage.getQuery();
        reRulePlugLibrary.setIsValid("1");
        Pagination<Map<String,Object>> result = reRulePluginLibraryService.selectRulePlugins(
                reRulePlugLibrary, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<Map<String,Object>>>().success(result);
    }

    /**
     * 上传插件库并扫面保存插件
     * @param file
     * @param target
     * @return
     */
    @ApiOperation(value = "上传插件库", notes = "上传插件库")
    @RequestMapping(value = "/uploadRulePluginLibrary", method = {RequestMethod.POST})
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
//            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<ReRulePluginLibrary> uploadRulePluginLibrary(@RequestBody MultipartFile file, ReRulePluginLibrary target){
        String path = filePath;
        String fileName = file.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        String saveName = path+fileName;
        try {
            File fileCheck = new File(path);
            if(!fileCheck.exists()){
                fileCheck.mkdirs();//若路径不存在则进行创建
            }
            file.transferTo(new File(saveName));
        } catch (IOException e) {
            return new ResultWrapper().fail("File0001","文件上传失败");
        }
        target.setFilePath(path);
        target.setFileName(fileName);
        return new ResultWrapper<ReRulePluginLibrary>().success(target);
    }

    /**
     * 保存插件库信息并扫描保存规则插件
     * @param target
     * @return
     */
    @Transactional
    @ApiOperation(value = "保存插件库", notes = "保存插件库")
    @RequestMapping(value = "/saveRulePluginLibrary", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper saveRulePluginLibrary(@RequestHeader(name = "Authorization") String authorization, @RequestBody ReRulePluginLibrary target){
        //文件扫描
        FunctionTools tools = new FunctionTools();
        String path = target.getFilePath();
        RuleLibrary libraryInfo = null;
        List<RuleInfo> keyList = new ArrayList<RuleInfo>();
        try {
           libraryInfo = RuleLibraryScanner.scan(path+target.getFileName());
            keyList = libraryInfo.getAllRules();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(libraryInfo == null){
            return new ResultWrapper().fail("library0001","上传插件库扫描失败，请确认上传文件");
        }

        //存储库信息
        String version = libraryInfo.getVersion();
        String id = "";
        ReRulePluginLibrary ruleLibrary = new ReRulePluginLibrary();
        ruleLibrary.setFileName(target.getFileName());
        ruleLibrary.setFilePath(path);
        ruleLibrary.setUploadStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        ruleLibrary.setUploadTime(new Date());
        ruleLibrary.setLibraryName(libraryInfo.getName());
        ruleLibrary.setStatus("1"); //启用
        ruleLibrary.setIsValid("1"); //逻辑存在
        ruleLibrary.setRemark(target.getRemark());
        ruleLibrary.setDataName(target.getDataName());
        ruleLibrary.setVersion(version);
        ruleLibrary.setChangeLog(libraryInfo.getChangeLog());
        //ruleLibrary.setIntro(libraryInfo.getIntro());
        ruleLibrary.setIpPort("http://"+ip+":"+port);

        String libraryName = libraryInfo.getName();
        ReRulePluginLibrary checkLib = new ReRulePluginLibrary();
        checkLib.setLibraryName(libraryName);
        checkLib.setIsValid("1"); //逻辑存在
        List<ReRulePluginLibrary> checkList = reRulePluginLibraryService.query(checkLib);
        int checkResult = rulePluginLibraryService.versionCheck(libraryInfo,target,checkList);
        if(checkResult == 1 || checkResult == 2){
            return new ResultWrapper().fail("rulePluginLibrary0002","上传版本过低或与当前版本相同，请确认插件库文件");
        }else if(checkResult == 0){
            //更新
            ReRulePluginLibrary old = checkList.get(0);
            id = old.getId();
            ruleLibrary.setId(id);
            reRulePluginLibraryService.updateByPrimaryKey(ruleLibrary);
        }else{
            //新插入
            id=tools.getUUID();
            ruleLibrary.setId(id);
            reRulePluginLibraryService.insert(ruleLibrary);
        }

        //循环关键字获取信息
        List<ReRulePluginInfo> comList = new ArrayList<ReRulePluginInfo>();
        List<ReRulePluginParameter> paramList = new ArrayList<ReRulePluginParameter>();
        Map<String,List<ReRulePluginParameter>> paramMap = new HashMap<String,List<ReRulePluginParameter>>();
        rulePluginLibraryService.getListForRulePluginInsert(comList,paramMap,target,keyList,version,id);

        //插入两张表的数据
        Map<String,Object> map = new HashMap<String,Object>();
        rulePluginLibraryService.insertRulePluginInfo(comList,paramMap,checkResult,id,target,map);
        for(String key : paramMap.keySet()){
            List<ReRulePluginParameter> paList = paramMap.get(key);
            for(ReRulePluginParameter one : paList){
                one.setRuleId(key);
                paramList.add(one);
            }
        }
        rulePluginLibraryService.insertRulePluginParam(paramList,checkResult,id,target,map);
        return new ResultWrapper().success();
    }

    /**
     * 启用-禁用插件库-更改状态
     * @param target
     * @return
     */
    @Transactional
    @ApiOperation(value = "启用/禁用插件库", notes = "启用/禁用插件库")
    @RequestMapping(value = "/onOffPluginLibrary", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper onOffPluginLibrary(@RequestBody ReRulePluginLibrary target){
        if(target.getId()==null){
            return new ResultWrapper().fail("rulePluginLibrary0003","请确认参数是否完整");
        }
        String status = target.getStatus();
        //逻辑删除-组件库以及库编号对应的组件信息
        ReRulePluginLibrary library = new ReRulePluginLibrary();
        library.setId(target.getId());
        library.setStatus(status);//状态置为1-启用0-禁用
        reRulePluginLibraryService.updateByPrimaryKey(library);
        //设置所有库编号对应的插件信息为无效
        ReRulePluginInfo info = new ReRulePluginInfo();
        info.setIsValid(status);
        info.setLibraryId(target.getId());
        reRulePluginInfoService.updateByLibraryId(info);
        return new ResultWrapper().success();
    }

    /**
     * 卸载插件库
     * @param target
     * @return
     */
    @Transactional
    @ApiOperation(value = "卸载插件库", notes = "启用/卸载插件库")
    @RequestMapping(value = "/deletePluginLibrary", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deletePluginLibrary(@RequestBody ReRulePluginLibrary target){
        if(target.getId()==null){
            return new ResultWrapper().fail("rulePluginLibrary0003","请确认参数是否完整");
        }
        String status = target.getStatus();
        //逻辑删除-组件库以及库编号对应的组件信息
        ReRulePluginLibrary library = new ReRulePluginLibrary();
        library.setId(target.getId());
        library.setIsValid("0");
        reRulePluginLibraryService.updateByPrimaryKey(library);
        //设置所有库编号对应的插件信息为无效
        ReRulePluginInfo info = new ReRulePluginInfo();
        info.setIsValid(status);
        info.setLibraryId(target.getId());
        reRulePluginInfoService.updateByLibraryId(info);
        return new ResultWrapper().success();
    }
}
