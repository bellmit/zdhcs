package net.northking.atp.entity;

import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.ReBrowserInfo;
import net.northking.atp.db.persistent.ReOsInfo;
import net.northking.atp.db.persistent.ReTestEnvInfo;

import java.util.Map;

/**
 *
 */
public class TestEnvInfo extends ReTestEnvInfo {

    public static final String KEY_osInfo = "osInfo";

    public static final String KEY_bwInfo = "browserInfo";

    @ApiModelProperty("操作系统信息")
    private ReOsInfo reOsInfo;

    @ApiModelProperty("浏览器信息")
    private ReBrowserInfo reBrowserInfo;

    public ReOsInfo getReOsInfo() {
        return reOsInfo;
    }

    public void setReOsInfo(ReOsInfo reOsInfo) {
        this.reOsInfo = reOsInfo;
    }

    public ReBrowserInfo getReBrowserInfo() {
        return reBrowserInfo;
    }

    public void setReBrowserInfo(ReBrowserInfo reBrowserInfo) {
        this.reBrowserInfo = reBrowserInfo;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        if (this.reOsInfo != null) {
            map.put(KEY_osInfo, this.reOsInfo);
        }
        if (this.reBrowserInfo != null) {
            map.put(KEY_bwInfo, this.reBrowserInfo);
        }
        return map;
    }

}
