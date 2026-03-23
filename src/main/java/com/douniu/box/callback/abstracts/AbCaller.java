package com.douniu.box.callback.abstracts;

public class AbCaller {
    public void doSomething(AbstractCallback callback) {
        try {
            String result = "任务完成";
            callback.onComplete(result);
        } catch (Exception e) {
            callback.onError(e);
        }
    }
}