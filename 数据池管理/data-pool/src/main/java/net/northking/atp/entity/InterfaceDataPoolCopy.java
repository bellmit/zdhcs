package net.northking.atp.entity;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by Administrator on 2020/3/27 0027.
 */
public class InterfaceDataPoolCopy {
    public static final String KEY_projectId = "projectId";
    public static final String KEY_profileId = "profileId";
    public static final String KEY_idList = "idList";
    @ApiModelProperty("项目ID")
    private String projectId;
    @ApiModelProperty("环境ID")
    private String profileId;
    @ApiModelProperty("拷贝数据ID集合")
    private List<String> idList;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public List<String> getIdList() {
        return idList;
    }

    public void setIdList(List<String> idList) {
        this.idList = idList;
    }
}
