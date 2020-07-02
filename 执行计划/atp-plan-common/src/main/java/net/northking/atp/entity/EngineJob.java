package net.northking.atp.entity;

import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReCaseDesignInfo;
import net.northking.atp.db.persistent.ReCaseSet;
import net.northking.atp.db.persistent.ReProfileInfo;
import net.northking.atp.db.persistent.RuEngineJob;

import java.util.List;
import java.util.Map;

public class EngineJob extends RuEngineJob {

    public final static String KEY_reCaseDesignInfo = "reCaseDesignInfo";
    public final static String KEY_reCaseSet = "reCaseSet";
    public final static String KEY_jobProList = "jobProList";

    @ApiModelProperty(value = "测试环境")
    private ReProfileInfo reProfileInfo;

    @ApiModelProperty(value = "用例名称")
    private ReCaseDesignInfo reCaseDesignInfo;

    @ApiModelProperty(value = "用例集名称")
    private ReCaseSet reCaseSet;

    @ApiModelProperty(value = "任务步骤")
    private List<JobPro> jobProList;

    @ApiModelProperty(value = "任务耗时")
    private Long elapsedTime;

    public Long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public ReCaseDesignInfo getReCaseDesignInfo() {
        return reCaseDesignInfo;
    }

    public void setReCaseDesignInfo(ReCaseDesignInfo reCaseDesignInfo) {
        this.reCaseDesignInfo = reCaseDesignInfo;
    }

    public ReCaseSet getReCaseSet() {
        return reCaseSet;
    }

    public void setReCaseSet(ReCaseSet reCaseSet) {
        this.reCaseSet = reCaseSet;
    }

    public List<JobPro> getJobProList() {
        return jobProList;
    }

    public void setJobProList(List<JobPro> jobProList) {
        this.jobProList = jobProList;
    }

    public ReProfileInfo getReProfileInfo() {
        return reProfileInfo;
    }

    public void setReProfileInfo(ReProfileInfo reProfileInfo) {
        this.reProfileInfo = reProfileInfo;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        if (reCaseDesignInfo != null) {
            map.put(KEY_reCaseDesignInfo, reCaseDesignInfo);
        }
        if (reCaseSet != null) {
            map.put(KEY_reCaseSet, reCaseSet);
        }
        if (jobProList != null) {
            map.put(KEY_jobProList, jobProList);
        }
        return map;
    }
}
