package net.ds.screenshot;

import android.app.Application;

public class App extends Application {

    private static App sInstance;
    
    public void onCreate() {
        sInstance = this;
    };
    
    public static App getApp() {
        return sInstance;
    }
}
