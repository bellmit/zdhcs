package net.northking.atp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(
        description = "分布式锁的请求对象和请求结果的封装实体对象"
)
public class LockRequestWrapper {

    @ApiModelProperty("请求对象")
    private RequestWrapper requestWrapper;

    @ApiModelProperty("结果对象")
    private ResultWrapper resultWrapper;

    public RequestWrapper getRequestWrapper() {
        return requestWrapper;
    }

    public void setRequestWrapper(RequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }

    public ResultWrapper getResultWrapper() {
        return resultWrapper;
    }

    public void setResultWrapper(ResultWrapper resultWrapper) {
        this.resultWrapper = resultWrapper;
    }

    @Override
    public String toString() {
        return "LockRequestWrapper{" +
                "requestWrapper=" + requestWrapper +
                ", resultWrapper=" + resultWrapper +
                '}';
    }
}
