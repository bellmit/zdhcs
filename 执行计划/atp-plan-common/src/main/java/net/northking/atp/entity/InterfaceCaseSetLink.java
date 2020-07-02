package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReCaseSetLink;

import java.util.List;

/**
 * 用例集关联
 * Created by Administrator on 2019/4/19 0019.
 */
@ApiModel(
        description = "用例集关联用例"
)
public class InterfaceCaseSetLink {
    public static final String KEY_id = "id";
    public static final String KEY_caseNo = "setId";
    public static final String KEY_projectId = "projectId";
    public static final String KEY_runProjectId = "runProjectId";
    public static final String KEY_linkList = "linkList";
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("用例集Id")
    private String setNo;
    @ApiModelProperty("项目编号")
    private String projectId;
    @ApiModelProperty("运行projectId")
    private String runProjectId;
    @ApiModelProperty("关联集合")
    private List<ReCaseSetLink> linkList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSetNo() {
        return setNo;
    }

    public void setSetNo(String setNo) {
        this.setNo = setNo;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getRunProjectId() {
        return runProjectId;
    }

    public void setRunProjectId(String runProjectId) {
        this.runProjectId = runProjectId;
    }

    public List<ReCaseSetLink> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<ReCaseSetLink> linkList) {
        this.linkList = linkList;
    }

    @Override
    public String toString() {
        return "InterfaceCaseSetLink{" +
                "id='" + id + '\'' +
                ", setNo='" + setNo + '\'' +
                ", projectId='" + projectId + '\'' +
                ", runProjectId='" + runProjectId + '\'' +
                ", linkList=" + linkList +
                '}';
    }
}
