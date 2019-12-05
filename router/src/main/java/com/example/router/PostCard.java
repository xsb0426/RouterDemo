package com.example.router;

import android.os.Bundle;

/**
 * @author xushibin
 * @date 2019-09-23
 * description：
 */
public class PostCard {

    private Bundle bundle;
    private String path;

    public PostCard withBundle(Bundle bundle) {
        this.bundle = bundle;
        return this;
    }

    Bundle getBundle() {
        return bundle;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    PostCard(String path) {
        setPath(path);
    }

    public Object navigation() {
        return Router.getInstance().navigation(this);
    }

}
