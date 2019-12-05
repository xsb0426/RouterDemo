package com.zdww.baselib;

import com.example.router.IProvider;

/**
 * 所有自定义的接口必须继承 Router中的IProvider接口
 *
 * @author xushibin
 * @date 2019-11-15
 * description：
 */
public interface ITestProvider extends IProvider {

    String test(String str);

}
