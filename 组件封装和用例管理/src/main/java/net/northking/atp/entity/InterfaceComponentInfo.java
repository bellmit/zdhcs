package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReComponentInfo;
import net.northking.atp.db.persistent.ReComponentParameter;
import net.northking.db.POJO;

import java.util.*;

/**
 * Created by Administrator on 2019/4/1 0001.
 */
@ApiModel(
        description = "组件封装"
)
public class InterfaceComponentInfo extends ReComponentInfo implements POJO{
    public static final String KEY_staffName = "staffName";
    public static final String KEY_packageList = "packageList";
    public static final String KEY_paramList = "paramList";
    public static final String KEY_libraryName = "libraryName";

    @ApiModelProperty("组件list")
    private List<InterfaceComponentPackage> packageList;
    @ApiModelProperty("高级组件参数list")
    private List<ReComponentParameter> paramList;
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

    public List<InterfaceComponentPackage> getPackageList() {
        return packageList;
    }

    public void setPackageList(List<InterfaceComponentPackage> packageList) {
        this.packageList = packageList;
    }

    public List<ReComponentParameter> getParamList() {
        return paramList;
    }

    public void setParamList(List<ReComponentParameter> paramList) {
        this.paramList = paramList;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }
}