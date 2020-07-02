package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Administrator on 2019/4/18 0018.
 */
@ApiModel(
        description = "文件相关信息"
)
public class InterfaceFileData {
    public static final String KEY_filePath = "filePath";
    public static final String KEY_id = "id";
    @ApiModelProperty("文件路径")
    private String filePath;
    @ApiModelProperty("主键")
    private String id;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
