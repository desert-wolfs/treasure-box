package com.douniu.box.callback.annotation;

// 回调类（用注解标记方法）
public class CallbackClass {
    @CallbackMethod
    public void onComplete(String result) {
        System.out.println("注解回调结果：" + result);
    }
}