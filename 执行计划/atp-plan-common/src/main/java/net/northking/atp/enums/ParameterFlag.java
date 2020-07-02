package net.northking.atp.enums;

public enum ParameterFlag {

    INPUT("0", "input"),
    OUTPUT("1", "output");

    String code;
    String msg;

    ParameterFlag(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ParameterFlag getFlagByCode(String code) {
        for (ParameterFlag value : ParameterFlag.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return ParameterFlag.INPUT;
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
