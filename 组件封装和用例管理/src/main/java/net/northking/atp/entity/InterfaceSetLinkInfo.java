package net.northking.atp.entity;

import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReCaseSetLink;

/**
 * @Author: chaoyi.zhang
 * @Date: 2020/5/12 0012
 */
public class InterfaceSetLinkInfo extends ReCaseSetLink{

    public static final String KEY_version = "version";
    public static final String KEY_createStaff = "createStaff";
    public static final String KEY_createTime = "createTime";
    @ApiModelProperty("版本")
    private String version;
    @ApiModelProperty("创建人")
    private String createStaff;
    @ApiModelProperty("创建时间")
    private String createTime;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreateStaff() {
        return createStaff;
    }

    public void setCreateStaff(String createStaff) {
        this.createStaff = createStaff;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
