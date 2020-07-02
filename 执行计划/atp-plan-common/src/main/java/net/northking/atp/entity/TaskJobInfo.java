package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import net.northking.atp.db.persistent.RuEngineJob;
import net.northking.atp.db.persistent.RuJobPro;
import net.northking.atp.db.persistent.RuJobProParam;

import java.util.List;

@ApiModel(description = "任务生成时的所有信息")
public class TaskJobInfo {

    private RuEngineJob ruEngineJob;

    private List<RuJobPro> ruJobPros;

    private List<RuJobProParam> ruJobProParams;

    public RuEngineJob getRuEngineJob() {
        return ruEngineJob;
    }

    public void setRuEngineJob(RuEngineJob ruEngineJob) {
        this.ruEngineJob = ruEngineJob;
    }

    public List<RuJobPro> getRuJobPros() {
        return ruJobPros;
    }

    public void setRuJobPros(List<RuJobPro> ruJobPros) {
        this.ruJobPros = ruJobPros;
    }

    public List<RuJobProParam> getRuJobProParams() {
        return ruJobProParams;
    }

    public void setRuJobProParams(List<RuJobProParam> ruJobProParams) {
        this.ruJobProParams = ruJobProParams;
    }

    @Override
    public String toString() {
        return "TaskJobInfo{" +
                "ruEngineJob=" + ruEngineJob +
                ", ruJobPros=" + ruJobPros +
                ", ruJobProParams=" + ruJobProParams +
                '}';
    }
}
