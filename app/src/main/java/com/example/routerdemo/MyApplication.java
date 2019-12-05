package com.example.routerdemo;

import android.app.Application;

import com.example.router.Router;

/**
 * @author xushibin
 * @date 2019-09-23
 * description：
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化方法，构建routerMap
        Router.getInstance().init(this);
    }
}
