package com.douniu.box.callback.normal;

// 3. 实现回调的类
public class CallbackImpl implements Callback {
    @Override
    public void onComplete(String result) {
        System.out.println("回调结果：" + result);
    }
}