package net.northking.atp.enums;

public enum DefaultEnv {

    DEFAULT(1, "默认"),
    NOT_DEFAULT(2, "非默认");

    int code;
    String msg;

    DefaultEnv(int code, String msg) {
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
        return "PlanClass{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
