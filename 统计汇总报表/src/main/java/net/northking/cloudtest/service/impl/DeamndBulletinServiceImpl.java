package net.northking.cloudtest.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.northking.cloudtest.dao.analyse.CltMapNodeMapper;
import net.northking.cloudtest.dao.analyse.DemandMapper;
import net.northking.cloudtest.dao.cust.CltCustomerMapper;
import net.northking.cloudtest.dao.project.CltProjectCountMapper;
import net.northking.cloudtest.dao.project.CltProjectMapper;
import net.northking.cloudtest.dao.report.CltReportMapper;
import net.northking.cloudtest.dao.testBug.CltBugLogMapper;
import net.northking.cloudtest.domain.analyse.Demand;
import net.northking.cloudtest.domain.analyse.DemandExample;
import net.northking.cloudtest.domain.cust.CltCustomer;
import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.project.CltProjectCount;
import net.northking.cloudtest.domain.project.CltProjectCountExample;
import net.northking.cloudtest.domain.report.CltReport;
import net.northking.cloudtest.domain.report.CltReportExample;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.domain.testBug.CltBugLogExample;
import net.northking.cloudtest.dto.DeamndBulletinService;
import net.northking.cloudtest.dto.DemandBulletin;
import net.northking.cloudtest.dto.report.DemandMapCountDto;
import net.northking.cloudtest.dto.report.DemandMapDto;
import net.northking.cloudtest.enums.CltBulletinCatalog;
import net.northking.cloudtest.query.analyse.DemandQuery;
import net.northking.cloudtest.utils.BeanUtil;
import net.northking.cloudtest.utils.CltUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by 老邓 on 2018/5/22.
 */
@Service
public class DeamndBulletinServiceImpl implements DeamndBulletinService {
    private final static Logger logger = LoggerFactory.getLogger(DeamndBulletinServiceImpl.class);

    @Autowired
    private DemandMapper demandMapper;
    @Autowired
    private CltMapNodeMapper cltMapNodeMapper;
    @Autowired
    private CltProjectMapper cltProjectMapper;
    @Autowired
    private CltCustomerMapper cltCustomerMapper;
    @Autowired
    private CltReportMapper cltReportMapper;
    @Autowired
    private CltBugLogMapper cltBugLogMapper;
    @Autowired
    private CltProjectCountMapper cltProjectCountMapper;
    @Override
    public void dealWithReport(String projectId, Map dataMap) {
        DemandBulletin demandBulletin = new DemandBulletin();
        DemandMapCountDto demandMapCountDto = getDemandMapCountDto(projectId);
        demandBulletin.setDemandMapCountDto(demandMapCountDto);
        //查出项目
        CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);
        //查询需求开始结束时间
        if(cltProject!=null){

            queryDeamdTime(projectId,cltProject);
        }
        if (cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
            //查出客户
            CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
            demandBulletin.setCustName(cltCustomer.getCustName());
            demandBulletin.setProject(cltProject);

            //获取上一版本号
            CltReportExample reportExample = new CltReportExample();
            CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
            reportCriteria.andProjectIdEqualTo(projectId);
            reportCriteria.andCatalogEqualTo(CltBulletinCatalog.RA.getCode());
            reportExample.setOrderByClause("CREATE_DATE DESC");
            List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
            if (cltReportList.size() == 0) {
                demandBulletin.setVersion("1.0");
            } else {
                demandBulletin.setVersion("1." + (cltReportList.get(0).getVersion() + 1));
            }

            //报告日期
            demandBulletin.setReportDate(new Date());

            //获取需求树
            List<DemandQuery> treeList = new ArrayList<>();
            try{
                List<Demand> list = demandMapper.selectByCondition(null,null, projectId,"N");
                if(list.size()>0){
                    List<DemandQuery> lst = new ArrayList<DemandQuery>();
                    for(Demand d : list){
                        DemandQuery dq = new DemandQuery();
                        BeanUtil.copyProperties(d,dq);
                        lst.add(dq);
                    }
                    treeList = getDemandTree(lst);
                }
            }catch (Exception e){
                logger.error("queryForGridLike",e);
            }
            demandBulletin.setDemandQueryList(treeList);
            int unGenStatusTotalNum=0;//总未生成数量
            int genStatusTotalNum=0;//总已生成数量
            int waitUpdateStatusTotalNum=0;//总待修改数量
            int waitReviewStatusTotalNum=0;//总评审通过数量
            //读取需求分析情况图表数据
            List<DemandMapDto> demandMapDtoList = getDemandMapDtos(projectId);
            for(DemandMapDto demandMapDto:demandMapDtoList){
                Integer unGenStatusNum = demandMapDto.getUnGenStatusNum();
                Integer genStatusNum = demandMapDto.getGenStatusNum();
                Integer waitUpdateStatusNum = demandMapDto.getWaitUpdateStatusNum();
                Integer waitReviewStatusNum = demandMapDto.getWaitReviewStatusNum();
                unGenStatusTotalNum+=unGenStatusNum;
                genStatusTotalNum+=genStatusNum;
                waitUpdateStatusTotalNum+=waitUpdateStatusNum;
                waitReviewStatusTotalNum+=waitReviewStatusNum;
            }
            demandBulletin.setUnGenStatusTotalNum(unGenStatusTotalNum);
            demandBulletin.setGenStatusTotalNum(genStatusTotalNum);
            demandBulletin.setWaitUpdateStatusTotalNum(waitUpdateStatusTotalNum);
            demandBulletin.setWaitReviewStatusTotalNum(waitReviewStatusTotalNum);
            demandBulletin.setDemandMapDtoList(demandMapDtoList);
            //存在的风险
            CltBugLogExample cltBugLogExample = new CltBugLogExample();
            CltBugLogExample.Criteria cltBugLogCriteria = cltBugLogExample.createCriteria();
            cltBugLogCriteria.andBugIdEqualTo(projectId);
            cltBugLogExample.setOrderByClause("LOG_TIME DESC");
            List<CltBugLog> cltBugLogList = cltBugLogMapper.selectByExample(cltBugLogExample);
            demandBulletin.setCltBugLogList(cltBugLogList);

            dataMap.put("demandBulletin",demandBulletin);
        }



    }


    public Integer insertOrUpdateData(String projectId, String operatorUserId, String docPath, String swfPath) {
        int result = 0;
        if(operatorUserId == null) operatorUserId = "system";
        CltReportExample reportExample = new CltReportExample();
        CltReportExample.Criteria reportCriteria = reportExample.createCriteria();
        reportCriteria.andProjectIdEqualTo(projectId);
        reportCriteria.andCatalogEqualTo(CltBulletinCatalog.RA.getCode());
        reportExample.setOrderByClause("CREATE_DATE DESC");
        List<CltReport> cltReportList = cltReportMapper.selectByExample(reportExample);
        if(cltReportList.size()>0){ //修改
            CltReport cltReport = cltReportList.get(0);
            cltReport.setUpdateDate(new Date());
            cltReport.setUpdateUser(operatorUserId);
            cltReport.setDocPath(docPath);
            cltReport.setSwfPath(swfPath);
            cltReport.setVersion(cltReport.getVersion() + 1);
            result = cltReportMapper.updateByPrimaryKeySelective(cltReport);
        }else{ //新增
            //查出项目
            CltProject cltProject = cltProjectMapper.selectByPrimaryKey(projectId);
            if(cltProject != null && StringUtils.isNotEmpty(cltProject.getCustId())) {
                //查出客户
                CltCustomer cltCustomer = cltCustomerMapper.selectByPrimaryKey(cltProject.getCustId());
                CltReport cltReport = new CltReport();
                cltReport.setCustomId(cltCustomer.getCustId());
                cltReport.setProjectId(projectId);
                cltReport.setReportName(cltProject.getProName() + "需求分析阶段测试报告");
                cltReport.setCatalog(CltBulletinCatalog.RA.getCode());
                cltReport.setReportDate(new Date());
                cltReport.setCreateUser(operatorUserId);
                cltReport.setCreateDate(new Date());
                cltReport.setDocPath(docPath);
                cltReport.setSwfPath(swfPath);
                cltReport.setVersion(0);
                result = cltReportMapper.insertSelective(cltReport);
            }
        }
        return result;
    }
    //根据项目ID查询需求阶段开始结束时间
    public void queryDeamdTime(String proId,CltProject cltProject){
        CltProjectCount cltProjectCount = cltProjectCountMapper.selectByPrimaryKey(proId);
        if(cltProjectCount!=null){
            Date demandStartDate = cltProjectCount.getDemandStartDate();
            Date demandEndDate = cltProjectCount.getDemandEndDate();
            cltProject.setTestPlanStartTime(demandStartDate);
            cltProject.setTestPlanEndTime(demandEndDate);
        }
    }

    //t统计数量
    public DemandMapCountDto getDemandMapCountDto(String projectId){

        DemandMapCountDto demandMapCountDto  = demandMapper.queryCountByproId(projectId);
        Integer deLeafNum = demandMapCountDto.getDeLeafNum();
        Integer leveNoNum = demandMapCountDto.getLeveNoNum();

        demandMapCountDto = cltMapNodeMapper.queryCountStatus(projectId);
        demandMapCountDto.setDeLeafNum(deLeafNum);
        demandMapCountDto.setLeveNoNum(leveNoNum);
        return demandMapCountDto;

    }
  //获取需求树
    public List<DemandQuery> getDemandTree(String projectId){
        List<DemandQuery> treeList = new ArrayList<>();
        try{
            List<Demand> list = demandMapper.queryAllDemandByProId(projectId);
            if(list.size()>0){
                List<DemandQuery> lst = new ArrayList<DemandQuery>();
                for(Demand d : list){
                    DemandQuery dq = new DemandQuery();
                    BeanUtil.copyProperties(d,dq);
                    lst.add(dq);
                }
                treeList = getDemandTree(lst);
            }
        }catch (Exception e){
            logger.error("queryForGridLike",e);
        }

        return treeList;
    }


    //组装数据（树状数据）
    public static List<DemandQuery> getDemandTree(List<DemandQuery> list){
        List<DemandQuery> treeList = new ArrayList<>();
        for (int i=0;i<list.size();i++) {
            DemandQuery dem = (DemandQuery)list.get(i);
            //获取最上级节点
            if("TOP".equalsIgnoreCase(dem.getParentId())){
                Map map = CltUtils.beanToMap(dem);
                Map jsonMap = new HashMap();
                jsonMap.putAll(map);
                //包装下级
                getSonTree(jsonMap,list);
                CltUtils.mapToBean(jsonMap,dem);
                treeList.add(dem);
            }

        }
        return treeList;
    }
    //递归方法
    private static Map<String,Object> getSonTree(Map<String,Object> parentMap,List<DemandQuery> itemList){

        List<DemandQuery> sonList = new ArrayList<>();

        Map<String, Object> treeMap;
        for (DemandQuery item : itemList){
            if(org.springframework.util.StringUtils.hasText(item.getParentId())&&(parentMap.get("id").toString().equals(item.getParentId().toString()))){

                treeMap = CltUtils.beanToMap(item);

                getSonTree(treeMap,itemList);
                CltUtils.mapToBean(treeMap,item);
                sonList.add(item);

            }
        }

        parentMap.put("children", sonList);

        return parentMap;
    }

    //读取需求分析情况图表数据
    public List<DemandMapDto> getDemandMapDtos(String projectId){
       //查出所有模块节点
        DemandExample demandExample = new DemandExample();
        DemandExample.Criteria criteria = demandExample.createCriteria();
        criteria.andLevelNoEqualTo(3);
        criteria.andLogicDelEqualTo("N");
        criteria.andProjectIdEqualTo(projectId);
        demandExample.setOrderByClause("CREATE_TIME ASC");
        List<DemandMapDto> list = new ArrayList<>();


        List<Demand> demands = demandMapper.selectByExample(demandExample);

        //还需要列出LevelNo为2且是叶子节点的
        DemandExample leafExample = new DemandExample();
        leafExample.setOrderByClause("CREATE_TIME ASC");
        DemandExample.Criteria leafCriteria = leafExample.createCriteria();
        leafCriteria.andProjectIdEqualTo(projectId);
        leafCriteria.andLogicDelEqualTo("N");
        leafCriteria.andLevelNoEqualTo(2);
        leafCriteria.andLeafEqualTo("Y");

        List<Demand> demandLeafList = demandMapper.selectByExample(leafExample);
        demands.addAll(demandLeafList);

        for(Demand demand:demands){

            DemandMapDto demandMapDto = demandMapper.selectCoutSataus(demand.getPath());
            demandMapDto.setName(demand.getName());
            list.add(demandMapDto);
        }
        //查出是目录叶子节点的节点
        DemandExample demandExample2 = new DemandExample();
        DemandExample.Criteria criteria2 = demandExample2.createCriteria();
        criteria2.andLevelNoEqualTo(2);
        criteria2.andLogicDelEqualTo("N");
        criteria2.andProjectIdEqualTo(projectId);
        //List<DemandMapDto> list2 = new ArrayList<>();


        List<Demand> demands2 = demandMapper.selectByExample(demandExample2);

        for(Demand demand:demands2){
           // if("Y".equalsIgnoreCase(demand.getLeaf())){
                String mindId = demand.getMindId();
                DemandMapDto demandMapDto = demandMapper.selectCoutSatausByMindId(mindId);
                demandMapDto.setName(demand.getName());
                list.add(demandMapDto);
           // }

        }
        return list;
    }

}
