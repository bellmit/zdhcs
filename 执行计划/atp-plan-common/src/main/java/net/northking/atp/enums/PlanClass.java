package net.northking.atp.enums;

public enum PlanClass {

    WEB_UI(1, "WebUI测试"),
    MOBILE(2, "移动测试");

    int code;
    String msg;

    PlanClass(int code, String msg) {
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
