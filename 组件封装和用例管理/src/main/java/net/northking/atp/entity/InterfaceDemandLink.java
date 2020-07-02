package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReCaseDemandLink;
import net.northking.db.POJO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/15 0015.
 */
@ApiModel(
        description = "需求案例关联表"
)
public class InterfaceDemandLink implements POJO {
    public static final String KEY_demandId = "demandId";
    public static final String KEY_projectId = "projectId";
    public static final String KEY_linkList = "linkList";
    @ApiModelProperty("需求编号")
    private String demandId;
    @ApiModelProperty("项目编号")
    private String projectId;
    @ApiModelProperty("关联集合")
    private List<Map<String,Object>> linkList;

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<Map<String,Object>> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<Map<String,Object>> linkList) {
        this.linkList = linkList;
    }

    public InterfaceDemandLink() {
    }

    @Override
    public Map<String, Object> toMap() {
        HashMap valueMap = new HashMap();
        if(this.demandId != null) {
            valueMap.put("demandId", this.demandId);
        }
        if(this.projectId != null) {
            valueMap.put("projectId", this.projectId);
        }
        if(this.linkList != null) {
            valueMap.put("linkList", this.linkList);
        }
        return valueMap;
    }
}
