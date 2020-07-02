package net.northking.atp.enums;

public enum  LockStatus {

    FAILURE("1", "失败"),
    SUCCESS("0", "成功");

    String code;
    String msg;

    LockStatus(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String code() {
        return code;
    }

    public void setCode(String code) {
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
        return "LockStatus{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
