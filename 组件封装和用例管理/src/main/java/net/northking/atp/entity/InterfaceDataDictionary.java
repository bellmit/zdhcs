package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.northking.atp.db.persistent.DataDictionaryEnum;

import java.util.List;

/**
 * 数据字典自定义实体类
 * Created by Administrator on 2019/5/16 0016.
 */
@ApiModel(
        description = "数据字典"
)
public class InterfaceDataDictionary {
    public static final String KEY_id = "id";
    public static final String KEY_field = "field";
    public static final String KEY_enumList = "enumList";
    @ApiModelProperty("字段主键")
    private String id;
    @ApiModelProperty("表字段")
    private String field;
    @ApiModelProperty("枚举list")
    private List<DataDictionaryEnum> enumList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<DataDictionaryEnum> getEnumList() {
        return enumList;
    }

    public void setEnumList(List<DataDictionaryEnum> enumList) {
        this.enumList = enumList;
    }
}
