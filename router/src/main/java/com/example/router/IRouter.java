package com.example.router;

import java.util.Map;

/**
 * @author xushibin
 * @date 2019-09-23
 * description：
 */
public interface IRouter {
    void loadRouterMap(Map<String, Class> routerMap);
}
