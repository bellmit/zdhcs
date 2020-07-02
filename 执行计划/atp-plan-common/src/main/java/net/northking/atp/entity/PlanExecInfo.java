package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.RePlanExecInfo;
import net.northking.atp.db.persistent.ReProfileInfo;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Map;

@ApiModel(description = "测试计划执行记录")
public class PlanExecInfo extends RePlanExecInfo {

    public static final String KEY_resultMap = "resultMap";

    @ApiModelProperty(value = "测试环境")
    private ReProfileInfo reProfileInfo;

    @ApiModelProperty(value = "执行开始时间")
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private Date execBeginTime;

    @ApiModelProperty(value = "执行结束时间")
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private Date execEndTime;

    @ApiModelProperty(value = "执行结果统计")
    private Map<String, Long> resultMap;

    public Map<String, Long> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, Long> resultMap) {
        this.resultMap = resultMap;
    }

    public ReProfileInfo getReProfileInfo() {
        return reProfileInfo;
    }

    public void setReProfileInfo(ReProfileInfo reProfileInfo) {
        this.reProfileInfo = reProfileInfo;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        if (resultMap != null) {
            map.put(KEY_resultMap, resultMap);
        }
        return map;
    }
}
