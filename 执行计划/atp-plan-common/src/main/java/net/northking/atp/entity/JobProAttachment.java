package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.RuJobProAttachment;

@ApiModel(description = "测试任务步骤附件信息表")
public class JobProAttachment extends RuJobProAttachment {

    @ApiModelProperty(value = "附件链接")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
