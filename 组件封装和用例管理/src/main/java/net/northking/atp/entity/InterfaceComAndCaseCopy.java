package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created by Administrator on 2019/4/18 0018.
 */
@ApiModel(
        description = "数据复制对象"
)
public class InterfaceComAndCaseCopy {
    public static final String KEY_projectId = "projectId";
    public static final String KEY_menuId = "menuId";
    public static final String KEY_idList = "idList";
    public static final String KEY_modifyStaff = "modifyStaff";
    @ApiModelProperty("项目ID")
    private String projectId;
    @ApiModelProperty("目标目录id")
    private String menuId;
    @ApiModelProperty("拷贝数据ID集合")
    private List<String> idList;
    @ApiModelProperty("更新人")
    private String modifyStaff;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public List<String> getIdList() {
        return idList;
    }

    public void setIdList(List<String> idList) {
        this.idList = idList;
    }

    public String getModifyStaff() {
        return modifyStaff;
    }

    public void setModifyStaff(String modifyStaff) {
        this.modifyStaff = modifyStaff;
    }
}
