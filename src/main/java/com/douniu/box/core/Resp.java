package com.douniu.box.core;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * 响应对象
 * @param <T>
 */
public class Resp<T> implements Serializable {

    /**
     * 响应编码 200表示成功，其他值表示失败
     */
    private int code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    public static <E> Resp<E> ok() {
        return ok(null);
    }

    public static <E> Resp<E> ok(E result) {
        return ok("ok", result);
    }

    public static <E> Resp<E> ok(String msg, E result) {
        Resp<E> r = new Resp<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(result);
        return r;
    }

    public static <E> Resp<E> error(int code, String msg) {
        Resp<E> r = new Resp<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public static <E> Resp<E> error(String msg) {
        return error(0, msg);
    }

    public static <E> Resp<E> error(BusinessException.CodeKey codeKey) {
        return error(codeKey.valueInteget(), codeKey.getMsg());
    }

    public static <E> Resp<E> init(Integer code, String msg, E data) {
        Resp<E> r = new Resp<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @JsonIgnore
    public boolean isOk() {
        return code == 200;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
