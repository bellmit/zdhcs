package net.northking.atp.enums;

public enum ComponentFlag {

    BASIC("0", "基础组件"),
    SENIOR("1", "高级组件"),
    EXPRESSION("2", "内置组件"),
    INTERFACE("3", "接口组件"),
    SMART("4", "智能接口");

    String code;
    String msg;

    ComponentFlag(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ComponentFlag getFlagByCode(String code) {
        for (ComponentFlag value : ComponentFlag.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return ComponentFlag.BASIC;
    }

    public String code() {
        return code;
    }

    public String msg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ParameterFlag{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
