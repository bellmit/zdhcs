package net.northking.atp.enums;

public enum PluginSettingStatus {

    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    int code;
    String msg;

    PluginSettingStatus(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ExecPlanStatus{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }

}
