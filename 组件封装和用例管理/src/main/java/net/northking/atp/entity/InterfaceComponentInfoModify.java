package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.MdComponentInfo;
import net.northking.atp.db.persistent.MdComponentParameter;
import net.northking.atp.db.persistent.ReComponentParameter;

import java.util.List;

/**
 * Created by Administrator on 2019/6/10 0010.
 */
@ApiModel(
        description = "组件封装_修改"
)
public class InterfaceComponentInfoModify extends MdComponentInfo {
    public static final String KEY_staffName = "staffName";
    public static final String KEY_packageList = "packageList";
    public static final String KEY_paramList = "paramList";
    public static final String KEY_libraryName = "libraryName";

    @ApiModelProperty("组件list")
    private List<InterfaceComponentPacModify> packageList;
    @ApiModelProperty("高级组件参数list")
    private List<MdComponentParameter> paramList;
    @ApiModelProperty("更改人")
    private String staffName;
    @ApiModelProperty("组件所属库名")
    private String libraryName;

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public List<InterfaceComponentPacModify> getPackageList() {
        return packageList;
    }

    public void setPackageList(List<InterfaceComponentPacModify> packageList) {
        this.packageList = packageList;
    }

    public List<MdComponentParameter> getParamList() {
        return paramList;
    }

    public void setParamList(List<MdComponentParameter> paramList) {
        this.paramList = paramList;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }
}
