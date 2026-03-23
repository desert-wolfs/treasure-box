package com.douniu.box.callback.normal;

// 2. 调用者类（发起回调的类）
public class Caller {
    // 接收回调接口实例
    public void doSomething(Callback callback) {
        // 执行耗时操作
        String result = "任务完成";
        // 完成后调用回调方法
        callback.onComplete(result);
    }



}