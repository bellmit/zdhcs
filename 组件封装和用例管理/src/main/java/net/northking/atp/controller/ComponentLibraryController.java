package net.northking.atp.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.northking.atp.commons.http.QueryByPage;
import net.northking.atp.commons.http.QueryOrderBy;
import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.db.impl.ReComponentLibraryServiceImpl;
import net.northking.atp.db.persistent.HisComponentLibrary;
import net.northking.atp.db.persistent.ReComponentInfo;
import net.northking.atp.db.persistent.ReComponentLibrary;
import net.northking.atp.db.persistent.ReComponentParameter;
import net.northking.atp.db.service.HisComponentLibraryService;
import net.northking.atp.db.service.ReComponentInfoService;
import net.northking.atp.db.service.ReComponentLibraryService;
import net.northking.atp.db.service.ReComponentParameterService;
import net.northking.atp.element.core.Library;
import net.northking.atp.element.core.keyword.KeywordInfo;
import net.northking.atp.element.extension.helper.JarLibraryScanner;
import net.northking.atp.entity.InterfaceFileData;
import net.northking.atp.impl.ComponentLibraryServiceImpl;
import net.northking.atp.service.ComponentLibraryService;
import net.northking.atp.util.CaseDesignTools;
import net.northking.atp.util.RedisTools;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import net.northking.files.NKFilesClient;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * 组件库信息维护
 * Created by Administrator on 2019/4/2 0002.
 */
@RestController
@Api(tags = {"组件库"}, description = "组件库_组件库维护")
@RequestMapping(value = "/caseComponent")
public class ComponentLibraryController {
    @Autowired
    private ComponentLibraryService componentLibraryService;//组件库业务处理
    @Autowired
    private ReComponentInfoService reComponentInfoService; //组件信息表
    @Autowired
    private ReComponentParameterService reComponentParameterService;//组件参数表
    @Autowired
    private ReComponentLibraryService reComponentLibraryService;//组件库表
    @Autowired
    private HisComponentLibraryService hisComponentLibraryService;//组件库历史列表
    @Autowired
    private RedisTools redisTools;

    @Value("${atp.attach.url.ip}")
    private String ip;
    @Value("${atp.attach.url.port}")
    private int port;
    @Value("${atp.attach.url.path}")
    private String filePath;
    //private String filePath = "D:/nkTest";
    //日志
    private static final Logger logger = LoggerFactory.getLogger(ComponentLibraryController.class);


    @ApiOperation(value = "上传组件库", notes = "上传组件库")
    @RequestMapping(value = "/ReComponentLibrary/uploadLibrary", method = {RequestMethod.POST})
    public ResultWrapper<ReComponentLibrary> uploadLibrary(@RequestHeader(name = "Authorization") String authorization,
                                                           @RequestBody MultipartFile file,ReComponentLibrary target){

        String staff = redisTools.getRedisUserInfo(authorization.split(" ")[1]);
        if(staff==null){
            return new ResultWrapper().fail("userInfo00001","人员信息查询失败,请重新登录");
        }else{
            target.setUploadStaff(staff);
        }

        String path = filePath;
        logger.info("获取路径"+path);
        logger.info("file是否为空:"+file);
        String fileName = file.getOriginalFilename();
        logger.info("获取文件"+fileName);
        String suffixName = fileName.substring(fileName.lastIndexOf(".")+1);
        if(!"jar".equals(suffixName)){
            return new ResultWrapper().fail("Component010001",suffixName+"文件无法解析，请确认导入文件");
        }
        String classPath = System.getProperty("user.dir")+"/tmp/";
        String saveName = classPath+fileName;
        try {
            File fileCheck = new File(classPath);
            if(!fileCheck.exists()){
                fileCheck.mkdirs();//若路径不存在则进行创建
            }
            file.transferTo(new File(saveName));
            logger.info("======================文件保存成功:"+saveName);
        } catch (IOException e) {
            logger.info("文件上传失败"+e);
            return new ResultWrapper().fail("Component010001","文件上传失败");
        }

        target.setFilePath(saveName);
        target.setFileName(fileName);
        String message = componentLibraryService.scanLibraryAndSave(target);
        if(message !=null){
            return new ResultWrapper().fail("library0003",message);
        }
        //扫描完毕文件发送至服务器
        // 返回文件的唯一ID，此ID用于文件下载、查询等功能
        String fileId = null;
        try {
            fileId = NKFilesClient.uploadFile(ip, port, new File(saveName));
        } catch (IOException e) {
            logger.info("文件上传失败:"+e);
            return new ResultWrapper().fail("upload00001","文件上传失败");
        }

        logger.info("文件上传成功");
        //更新
        ReComponentLibrary lib = new ReComponentLibrary();
        lib.setId(target.getId());
        lib.setIpPort("http://"+ip+":"+port+filePath.substring(0,filePath.length()-1));
        lib.setFilePath(filePath);
        lib.setFileId(fileId);
        reComponentLibraryService.updateByPrimaryKey(lib);
        return new ResultWrapper<ReComponentLibrary>().success(target);
    }

    /**
     * 上传组件库并扫面保存组件
     * @param file
     * @param target
     * @return

    @ApiOperation(value = "上传组件库", notes = "上传组件库")
    @RequestMapping(value = "/ReComponentLibrary/uploadLibrary", method = {RequestMethod.POST})
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
//            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<ReComponentLibrary> uploadLibrary(@RequestBody MultipartFile file,ReComponentLibrary target){
        String path = filePath;
        logger.info("获取路径"+path);
        logger.info("file是否为空:"+file);
        String fileName = file.getOriginalFilename();
        logger.info("获取文件"+fileName);
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        String saveName = path+fileName;
        try {
            File fileCheck = new File(path);
            if(!fileCheck.exists()){
                fileCheck.mkdirs();//若路径不存在则进行创建
            }
            file.transferTo(new File(saveName));
        } catch (IOException e) {
            logger.info("文件上传失败"+e);
            return new ResultWrapper().fail("Component010001","文件上传失败");
        }
        logger.info("文件上传成功");
        target.setFilePath(path);
        target.setFileName(fileName);
        return new ResultWrapper<ReComponentLibrary>().success(target);
    }*/

    /**
     * 保存组件库信息并扫描保存组件
     * @param target
     * @return

    @Transactional
    @ApiOperation(value = "保存组件库", notes = "保存组件库")
    @RequestMapping(value = "/ReComponentLibrary/saveLibrary", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper saveLibrary(@RequestHeader(name = "Authorization") String authorization,@RequestBody ReComponentLibrary target){
        target.setProjectId("SYSTEM");
        if(target.getProjectId() == null){
            return new ResultWrapper().fail("component0002","参数不正确请确认");
        }
        //文件扫描
        CaseDesignTools tools = new CaseDesignTools();
        String path = target.getFilePath();
        Library libraryInfo = null;
        List<KeywordInfo> keyList = new ArrayList<KeywordInfo>();
        try {
            libraryInfo = JarLibraryScanner.scan(path+target.getFileName());
            keyList = libraryInfo.getAllKeywords();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(libraryInfo == null){
            return new ResultWrapper().fail("library0003","上传组件库扫描失败，请确认上传文件");
        }

        //存储库信息
        String version = libraryInfo.getVersion();
        String id = "";
        ReComponentLibrary comLibrary = new ReComponentLibrary();
        comLibrary.setFileName(target.getFileName());
        comLibrary.setFilePath(path);
        comLibrary.setUploadTime(new Date());
        comLibrary.setLibraryName(libraryInfo.getName());
        comLibrary.setStatus("1"); //启用
        comLibrary.setIsValid("1"); //逻辑存在
        comLibrary.setProjectId(target.getProjectId());
        comLibrary.setRemark(target.getRemark());
        comLibrary.setDataName(target.getDataName());
        comLibrary.setVersion(version);
        comLibrary.setChangeLog(libraryInfo.getChangeLog());
        comLibrary.setIntro(libraryInfo.getIntro());
        comLibrary.setUploadStaff(redisTools.getRedisUserInfo(authorization.split(" ")[1]));
        comLibrary.setIpPort("http://"+ip+":"+port);

        String libraryName = libraryInfo.getName();
        ReComponentLibrary checkLib = new ReComponentLibrary();
        checkLib.setLibraryName(libraryName);
        checkLib.setProjectId(target.getProjectId());
        checkLib.setIsValid("1"); //逻辑存在
        List<ReComponentLibrary> checkList = reComponentLibraryService.query(checkLib);
        int checkResult = componentLibraryService.versionCheck(libraryInfo,target,checkList);
        if(checkResult == 1 || checkResult == 2){
            return new ResultWrapper().fail("component0001","上传版本过低或与当前版本相同，请确认组件库文件");
        }else if(checkResult == 0){
            //更新
            ReComponentLibrary old = checkList.get(0);
            id = old.getId();
            comLibrary.setId(id);
            String libraryNo = old.getLibraryNo();
            comLibrary.setLibraryNo(libraryNo);
            reComponentLibraryService.updateByPrimaryKey(comLibrary);
            logger.info("库信息更新");
        }else{
            //新插入
            id = UUID.randomUUID().toString().replace("-", "");
            comLibrary.setId(id);
            String libraryNo = "LIB"+"-"+tools.generateBusinessNo();
            comLibrary.setLibraryNo(libraryNo);
            reComponentLibraryService.insert(comLibrary);
            logger.info("库信息保存");
        }

        //循环关键字获取信息
        List<ReComponentInfo> comList = new ArrayList<ReComponentInfo>();
        List<ReComponentParameter> paramList = new ArrayList<ReComponentParameter>();
        Map<String,List<ReComponentParameter>> paramMap = new HashMap<String,List<ReComponentParameter>>();
        componentLibraryService.getListForComponentInsert(comList,paramMap,target,libraryInfo,keyList,version,id);

        //插入两张表的数据
        Map<String,Object> map = new HashMap<String,Object>();
        componentLibraryService.insertCompInfoAndHis(comList,paramMap,checkResult,id,target,map);
        for(String key : paramMap.keySet()){
            List<ReComponentParameter> paList = paramMap.get(key);
            for(ReComponentParameter one : paList){
                one.setComponentId(key);
                paramList.add(one);
            }
        }
        componentLibraryService.insertCompParamInfoAndHis(paramList,checkResult,id,target,map);
        componentLibraryService.versionInfoSave(comLibrary,comList,paramList);
        return new ResultWrapper().success();
    }*/

    /**
     * 删除组件库-逻辑删除
     * @param target
     * @return
     */
    @Transactional
    @ApiOperation(value = "删除组件库", notes = "删除组件库")
    @RequestMapping(value = "/ReComponentLibrary/deleteLibrary", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper deleteLibrary(@RequestBody ReComponentLibrary target){
        if(target.getId()==null){
            return new ResultWrapper().fail("caseSet0001","请确认参数是否完整");
        }
        //逻辑删除-组件库以及库编号对应的组件信息
        ReComponentLibrary reComponentLibrary = new ReComponentLibrary();
        reComponentLibrary.setId(target.getId());
        reComponentLibrary.setIsValid("0");//逻辑删除置为无效
        reComponentLibraryService.updateByPrimaryKey(reComponentLibrary);
        //设置所有库主键对应的组件信息为无效
        ReComponentInfo reComponentInfo = new ReComponentInfo();
        reComponentInfo.setIsValid("0");
        reComponentInfo.setLibraryId(target.getId());
        //reComponentInfo.setProjectId(target.getProjectId());
        reComponentInfoService.updateByLibraryId(reComponentInfo);
        return new ResultWrapper().success();
    }

    /**
     * 启用-禁用组件库-更改状态
     * @param target
     * @return
     */
    @Transactional
    @ApiOperation(value = "启用/禁用组件库", notes = "启用/禁用组件库")
    @RequestMapping(value = "/ReComponentLibrary/onOffLibrary", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper onOffLibrary(@RequestBody ReComponentLibrary target){
        if(target.getId()==null || target.getProjectId()==null){
            return new ResultWrapper().fail("caseSet0001","请确认参数是否完整");
        }
        String status = target.getStatus();
        //逻辑删除-组件库以及库编号对应的组件信息
        ReComponentLibrary reComponentLibrary = new ReComponentLibrary();
        reComponentLibrary.setId(target.getId());
        reComponentLibrary.setStatus(status);//状态置为1-启用0-禁用
        reComponentLibraryService.updateByPrimaryKey(reComponentLibrary);
        //设置所有库编号对应的组件信息为无效
        ReComponentInfo reComponentInfo = new ReComponentInfo();
        reComponentInfo.setIsValid(status);
        reComponentInfo.setLibraryId(target.getId());
        //reComponentInfo.setProjectId(target.getProjectId());
        reComponentInfoService.updateByLibraryId(reComponentInfo);
        return new ResultWrapper().success();
    }

    /**
     * 分页查询 组件库表
     * @param queryByPage 分页查询对象
     * @return 接口返回
     */
    @ApiOperation(value = "分页查询 组件库表", notes = "分页查询 组件库表")
    @RequestMapping(value = "/ReComponentLibrary/queryLibraryByPage", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<Pagination<ReComponentLibrary>> queryLibraryByPage(@RequestBody QueryByPage<ReComponentLibrary> queryByPage)
    {
        OrderBy orderBy = new SqlOrderBy();
        for (QueryOrderBy queryOrderBy : queryByPage.getOrderByList())
        {
            orderBy.addOrderBy(queryOrderBy.getColumn(), queryOrderBy.getDir());
        }
        ReComponentLibrary reComponentLibrary = queryByPage.getQuery();
        if(reComponentLibrary.getDataName() != null){
            reComponentLibrary.setDataName("%"+reComponentLibrary.getDataName()+"%");
        }
        if(reComponentLibrary.getLibraryName() != null){
            reComponentLibrary.setLibraryName("%"+reComponentLibrary.getLibraryName()+"%");
        }
        Pagination<ReComponentLibrary> result = reComponentLibraryService.selectComponentLibraryInfo(
                reComponentLibrary, orderBy, queryByPage.getPageNo(), queryByPage.getPageSize());
        return new ResultWrapper<Pagination<ReComponentLibrary>>().success(result);
    }

    /**
     * 下载组件库文件
     * @param target
     * @return
     */
    @ApiOperation(value = "下载组件库", notes = "下载组件库")
    @RequestMapping(value = "/ReComponentLibrary/downloadLibrary", method = {RequestMethod.GET})
    public ResultWrapper downloadLibrary(InterfaceFileData target,HttpServletResponse response){
        //文件下载
        String id = target.getId();
        ReComponentLibrary library = reComponentLibraryService.findByPrimaryKey(id);
        String libPath = library.getFilePath();
        String libName = library.getFileName();
        System.out.println(libPath+libName);
        File file = new File(libPath+libName);
        if(!file.exists()){
            logger.info("文件不存在");
            return new ResultWrapper().fail("caseComponent002","文件不存在，请联系管理员查看");
        }
        response.setContentType("application/force-download");// 设置强制下载不打开            
        response.addHeader("Content-Disposition", "attachment;fileName=" + libName);
        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        File download = new File(target.getFilePath());
        OutputStream os = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
            logger.info("下载成功");
            return new ResultWrapper().success();
        }
        catch (Exception e) {
            logger.info("下载失败:",e);
        }
        finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ResultWrapper().success();
    }


    /**
     * 历史版本查询
     * @param target
     * @return
     */
    @Transactional
    @ApiOperation(value = "查询组件库历史版本", notes = "查询组件库历史版本")
    @RequestMapping(value = "/ReComponentLibrary/queryAllLibraryHis", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResultWrapper<List<HisComponentLibrary>> queryAllLibraryHis(@RequestBody HisComponentLibrary target)throws Exception
    {
        target.setProjectId(null);
        return new ResultWrapper<List<HisComponentLibrary>>().success(hisComponentLibraryService.queryAllLibraryHis(target));
    }
}
