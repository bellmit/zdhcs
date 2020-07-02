package net.northking.cloudtest.dto;

import net.northking.cloudtest.domain.project.CltProject;
import net.northking.cloudtest.domain.testBug.CltBugLog;
import net.northking.cloudtest.dto.report.DemandMapCountDto;
import net.northking.cloudtest.dto.report.DemandMapDto;
import net.northking.cloudtest.query.analyse.DemandQuery;

import java.util.Date;
import java.util.List;

/**
 * Created by 老邓 on 2018/5/22.
 */
public class DemandBulletin {
    private String userChnName;
    /**
     * 客户名称
     */
    private String custName;
    /**
     * 项目名称
     */
    private CltProject project;

    /**
     * 版本
     */
    private String version;

    /**
     * 报告日期
     */
    private Date reportDate;

    private Integer unGenStatusTotalNum;//总未生成数量
    private Integer genStatusTotalNum;//总已生成数量
    private Integer waitUpdateStatusTotalNum;//总待修改数量
    private Integer waitReviewStatusTotalNum;//总评审通过数量

    private DemandMapCountDto demandMapCountDto;

    private List<DemandQuery> demandQueryList;

    private  List<DemandMapDto> demandMapDtoList;

    private List<CltBugLog> cltBugLogList;

    public String getUserChnName() {
        return userChnName;
    }

    public void setUserChnName(String userChnName) {
        this.userChnName = userChnName;
    }

    public List<CltBugLog> getCltBugLogList() {
        return cltBugLogList;
    }

    public void setCltBugLogList(List<CltBugLog> cltBugLogList) {
        this.cltBugLogList = cltBugLogList;
    }

    public Integer getUnGenStatusTotalNum() {
        return unGenStatusTotalNum;
    }

    public void setUnGenStatusTotalNum(Integer unGenStatusTotalNum) {
        this.unGenStatusTotalNum = unGenStatusTotalNum;
    }

    public Integer getGenStatusTotalNum() {
        return genStatusTotalNum;
    }

    public void setGenStatusTotalNum(Integer genStatusTotalNum) {
        this.genStatusTotalNum = genStatusTotalNum;
    }

    public Integer getWaitUpdateStatusTotalNum() {
        return waitUpdateStatusTotalNum;
    }

    public void setWaitUpdateStatusTotalNum(Integer waitUpdateStatusTotalNum) {
        this.waitUpdateStatusTotalNum = waitUpdateStatusTotalNum;
    }

    public Integer getWaitReviewStatusTotalNum() {
        return waitReviewStatusTotalNum;
    }

    public void setWaitReviewStatusTotalNum(Integer waitReviewStatusTotalNum) {
        this.waitReviewStatusTotalNum = waitReviewStatusTotalNum;
    }

    public List<DemandMapDto> getDemandMapDtoList() {
        return demandMapDtoList;
    }

    public void setDemandMapDtoList(List<DemandMapDto> demandMapDtoList) {
        this.demandMapDtoList = demandMapDtoList;
    }

    public List<DemandQuery> getDemandQueryList() {
        return demandQueryList;
    }

    public void setDemandQueryList(List<DemandQuery> demandQueryList) {
        this.demandQueryList = demandQueryList;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public CltProject getProject() {
        return project;
    }

    public void setProject(CltProject project) {
        this.project = project;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public DemandMapCountDto getDemandMapCountDto() {
        return demandMapCountDto;
    }

    public void setDemandMapCountDto(DemandMapCountDto demandMapCountDto) {
        this.demandMapCountDto = demandMapCountDto;
    }
}
