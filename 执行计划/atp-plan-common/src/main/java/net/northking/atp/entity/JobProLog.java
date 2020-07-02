package net.northking.atp.entity;

import net.northking.atp.db.persistent.RuJobPro;
import net.northking.atp.db.persistent.RuJobProAttachment;
import net.northking.atp.db.persistent.RuJobProLog;

import java.util.List;

public class JobProLog extends RuJobProLog {

    private RuJobPro ruJobPro;

    private List<JobProAttachment> proAttachments;

    public RuJobPro getRuJobPro() {
        return ruJobPro;
    }

    public void setRuJobPro(RuJobPro ruJobPro) {
        this.ruJobPro = ruJobPro;
    }

    public List<JobProAttachment> getProAttachments() {
        return proAttachments;
    }

    public void setProAttachments(List<JobProAttachment> proAttachments) {
        this.proAttachments = proAttachments;
    }
}
