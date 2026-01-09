package com.douniu.box.core;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    public enum CodeKey {
        /**
         * 成功
         */
        success("200", "ok"),
        fail("0", "fail"),
        timeout("408", "Request Timeout"),
        remoteInvoke("503", "Remote invoke failed"),
        paramErr("90000", "Parameter error"),
        loginErr("40003", "用户名或密码错误"),
        uploadErr("40004", "上传文件到COS失败"),
        token_invalid("40005", "token无效"),

        ;

        private String value;
        private String msg;

        CodeKey(String value, String msg) {
            this.value = value;
            this.msg = msg;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Integer valueInteget() {
            return Integer.parseInt(value);
        }

        public CodeKey changeMsg(String msg) {
            if (null != msg && msg.trim().length() > 0) {
                this.msg = msg;
            }
            return this;
        }

        public CodeKey fM(Object... ll) {
            this.msg = String.format(this.msg, ll);
            return this;
        }
    }

    private final String code;
    private final CodeKey enumT;
    private final Object data;

    public BusinessException() {
        super();
        this.code = null;
        this.data = null;
        this.enumT = null;
    }

    public BusinessException(String msg) {
        this(CodeKey.fail.getValue(), msg, null);
    }

    public BusinessException(String code, String msg) {
        this(code, msg, null);
    }

    public BusinessException(String code, String msg, Object data) {
        super(msg, null, false, false);
        this.code = code;
        this.data = data;
        this.enumT = null;
    }

    public BusinessException(CodeKey code) {
        this(code, null);
    }

    public BusinessException(CodeKey code, Object data) {
        super(code.getMsg(), null, false, false);
        this.code = code.getValue();
        this.data = data;
        this.enumT = code;
    }

    public String getCode() {
        return code;
    }

    public Integer getIntegerCode() {
        if (null != code) {
            return Integer.parseInt(code);
        }
        return null;
    }

    public String getMsg() {
        return getMessage();
    }

    public CodeKey getEnumT() {
        return enumT;
    }

    public Object getData() {
        return data;
    }

}
