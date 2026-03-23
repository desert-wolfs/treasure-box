package com.douniu.box.callback.abstracts;

//5. 抽象类实现（带默认逻辑）
//如果需要为回调提供默认实现，可以使用抽象类代替接口（类似模板方法模式）。
// 定义抽象回调类（可包含默认实现）
public abstract class AbstractCallback {
    // 抽象回调方法（必须实现）
    public abstract void onComplete(String result);
    // 默认回调方法（可选覆盖）
    public void onError(Exception e) {
        System.out.println("默认错误处理：" + e.getMessage());
    }
}