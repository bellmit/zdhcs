package net.northking.atp.enums;

public enum ExecuteResult {
    // 执行结果
    NO_RUN("NoRun", "未执行"),
    RUNNING("running", "正在执行"),
    SUCCESS("success", "执行成功"),
    FAILURE("fail", "执行失败");

    String code;
    String msg;

    ExecuteResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public static ExecuteResult getMsgByCode(String code) {
        for (ExecuteResult value : ExecuteResult.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return ExecuteResult.NO_RUN;
    }

    public String code() {
        return code;
    }

    public String msg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ExecuteResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
