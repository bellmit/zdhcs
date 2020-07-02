package net.northking.atp.enums;

/**
 *  执行计划状态枚举
 * @author 李杰应
 */
public enum ExecPlanStatus {

    NEW(1, "新建"),
    EXECUTING(2, "执行中"),
    DELETED(3, "已删除");

    int code;
    String msg;

    ExecPlanStatus(int code, String msg) {
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
