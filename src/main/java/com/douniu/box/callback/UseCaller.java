package com.douniu.box.callback;

import com.douniu.box.callback.abstracts.AbCaller;
import com.douniu.box.callback.abstracts.AbstractCallback;
import com.douniu.box.callback.annotation.AnnCaller;
import com.douniu.box.callback.annotation.CallbackClass;
import com.douniu.box.callback.normal.Caller;

public class UseCaller {

    public static void main(String[] args) {

        // 1. 调用者类（发起回调的类）
//        Caller caller = new Caller();
//        caller.doSomething(new CallbackImpl()); // 传递实现类


        // 2. 匿名内部类实现回调接口 不用callbackimpl实现接口
        /*Caller caller = new Caller();
        caller.doSomething(new Callback() {
            @Override
            public void onComplete(String result) {
                System.out.println("匿名内部类回调结果：" + result);
            }
        });*/

        // 3、可简化成lambda表达式
//        caller.doSomething(result -> System.out.println("匿名内部类回调结果：" + result));


        // 4、方法引用（Java 8+）
        //如果已有方法的签名与回调接口的抽象方法一致，可以直接引用该方法作为回调。
        Caller caller = new Caller();
        caller.doSomething(UseCaller::handleCallback); // 引用静态方法
        // 或引用实例方法：caller.doSomething(new Main()::handleCallback);

        // 5、抽象回调类
        AbCaller abCaller = new AbCaller();
        abCaller.doSomething(new AbstractCallback() {
            @Override
            public void onComplete(String result) {
                System.out.println("抽象回调类回调结果：" + result);
                // 模拟异常
                int i = 1/0;
            }
            // 可重新实现异常处理
            @Override
            public void onError(Exception e) {
                System.out.println("抽象回调类异常处理：" + e.getMessage());
            }
        });

        // 6、注解回调类
        AnnCaller annCaller = new AnnCaller();
        annCaller.doSomething(new CallbackClass());
    }

    // 已有的方法（签名需与Callback接口匹配）
    public static void handleCallback(String result) {
        System.out.println("方法引用回调结果：" + result);
    }
}
