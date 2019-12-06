package com.example.router;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;

/**
 * @author xushibin
 * @date 2019-09-23
 * description：单例
 */
public class Router {

    /**
     * 需要扫描的包名，编译时注解生成的类在这个包下面
     */
    private static final String GENERATE_PACKAGE = "com.example.genarate";

    private static final Router instance = new Router();

    private Application application;

    /**
     * routerMap就是一个缓存，把path和class对应缓存下来
     */
    private Map<String, Class> routerMap;

    public Map<String, Class> getRouterMap() {
        return routerMap;
    }

    private Router() {
        routerMap = new HashMap<>();
    }

    /**
     * 单例
     *
     * @return
     */
    public static Router getInstance() {
        return instance;
    }

    /**
     * 初始化方法
     *
     * @param application
     */
    public void init(Application application) {
        this.application = application;
        //查找指定包名下的所有类名
        List<String> classNameList = getClassName(GENERATE_PACKAGE);
        for (String s : classNameList) {
            try {
                //通过类名反射创建对象，调用对象的方法，装载routerMap
                Class<?> aClass = Class.forName(s);
                IRouter iRouter = (IRouter) aClass.getConstructor().newInstance();
                iRouter.loadRouterMap(routerMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 通过包名获取类名
     *
     * @param packageName
     * @return
     */
    private List<String> getClassName(String packageName) {
        List<String> list = new ArrayList<>();
        try {
            String sourceDir = application.getPackageManager().getApplicationInfo(application.getPackageName(), 0).sourceDir;
            DexFile dexFile = new DexFile(sourceDir);
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement();
                if (name.contains(packageName)) {
                    list.add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * @param postCard
     * @return
     */
    Object navigation(PostCard postCard) {
        String path = postCard.getPath();
        if (TextUtils.isEmpty(path)) {
            throw new IllegalStateException("找不到 path");
        }
        Class aClass = routerMap.get(path);
        if (aClass != null) {
            try {
                Object instance = aClass.newInstance();
                Bundle bundle = postCard.getBundle();
                if (instance instanceof Fragment) {   //跳转Fragment
                    if (bundle != null && bundle.size() > 0) {
                        ((Fragment) instance).setArguments(bundle);
                    }
                    return instance;
                } else if (instance instanceof Activity) {  //跳转activity
                    Intent intent = new Intent(application, aClass);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (bundle != null && bundle.size() > 0) {
                        intent.putExtras(bundle);
                    }
                    application.startActivity(intent);
                } else if (instance instanceof IProvider) { //获取接口
                    return instance;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public PostCard build(String path) {
        return new PostCard(path);
    }

    /**
     * 注入方法，如果要使用Autowired注解，必须在程序开始进行注入
     *
     * @param object
     */
    public void inject(Object object) {
        String name = object.getClass().getName();
        try {
            Class<?> aClass = Class.forName(name + "$$Autowired");
            ISyringe iSyringe = (ISyringe) aClass.getConstructor().newInstance();
            iSyringe.inject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
