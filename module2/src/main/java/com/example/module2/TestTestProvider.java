package com.example.module2;

import com.example.annotation.Route;
import com.zdww.baselib.ITestProvider;

/**
 * @author xushibin
 * @date 2019-11-15
 * description：
 */

@Route("/module2/provider")
public class TestTestProvider implements ITestProvider {
    @Override
    public String test(String str) {


        str = str + " module2";


        return str;
    }
}
