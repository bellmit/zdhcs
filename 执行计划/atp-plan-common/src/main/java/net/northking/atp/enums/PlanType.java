package net.northking.atp.enums;

/**
 * 计划类型 1-正常 2-临时
 */
public enum  PlanType {
    NORMAL(1, "正常"),
    TEMPORARY(2, "临时");

    int code;
    String msg;

    PlanType(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String msg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "PlanType{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
