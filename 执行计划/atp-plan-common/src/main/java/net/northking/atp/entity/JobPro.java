package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import net.northking.atp.db.persistent.RuJobPro;
import net.northking.atp.db.persistent.RuJobProAttachment;
import net.northking.atp.db.persistent.RuJobProLog;
import net.northking.atp.db.persistent.RuJobProParam;

import java.util.List;

@ApiModel(description = "任务步骤信息")
public class JobPro extends RuJobPro {

    private List<RuJobProLog> proLogs;

    private List<RuJobProParam> proParams;

    private List<JobProAttachment> proAttachments;

    private Long elapsedTime;

    public Long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public List<RuJobProLog> getProLogs() {
        return proLogs;
    }

    public void setProLogs(List<RuJobProLog> proLogs) {
        this.proLogs = proLogs;
    }

    public List<RuJobProParam> getProParams() {
        return proParams;
    }

    public void setProParams(List<RuJobProParam> proParams) {
        this.proParams = proParams;
    }

    public List<JobProAttachment> getProAttachments() {
        return proAttachments;
    }

    public void setProAttachments(List<JobProAttachment> proAttachments) {
        this.proAttachments = proAttachments;
    }
}
