package com.douniu.box.callback.annotation;

import java.lang.reflect.Method;

// 调用者（通过反射调用注解方法）
public class AnnCaller {
    public void doSomething(Object callbackObj) {
        try {
            // 获取所有方法
            Method[] methods = callbackObj.getClass().getDeclaredMethods();
            for (Method method : methods) {
                // 查找带@CallbackMethod注解的方法
                if (method.isAnnotationPresent(CallbackMethod.class)) {
                    // 调用回调方法
                    method.invoke(callbackObj, "任务完成");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}