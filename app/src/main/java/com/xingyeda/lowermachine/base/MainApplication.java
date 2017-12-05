package com.xingyeda.lowermachine.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;


import com.ldl.okhttp.OkHttpUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.xingyeda.lowermachine.service.DoorService;
import com.xingyeda.lowermachine.service.HeartBeatService;
import com.youth.banner.loader.ImageLoader;

import org.linphone.LinphoneService;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.ACTION_MAIN;

public class MainApplication extends Application {

    private static Stack<Activity> activityStack;
    private static MainApplication singleton;
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        activityStack = new Stack<Activity>();
        mContext = getApplicationContext();
        CrashReport.initCrashReport(getApplicationContext(), "6e8ad93526", true);
        OkHttpUtils.getInstance().setConnectTimeout(100000, TimeUnit.MILLISECONDS);

        initDoorService();
        initHeartBeatService();
        if (!LinphoneService.isReady()) {
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
        }
    }

    public static Context getmContext() {
        return mContext;
    }

    public static Stack<Activity> getActivityStack() {
        return activityStack;
    }

    public static void setActivityStack(Stack<Activity> activityStack) {
        MainApplication.activityStack = activityStack;
    }

    // 返回application 实例
    public static MainApplication getInstance() {
        return singleton;
    }

    /**
     * add Activity 添加Activity到栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * get current Activity 获取当前Activity（栈中最后一个压入的）
     */
    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    /**
     * 结束当前Activity（栈中最后一个压入的）
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

    private void initDoorService() {
        Intent intent = new Intent();
        intent.setClass(this, DoorService.class);
        startService(intent);
    }

    private void initHeartBeatService() {
        Intent intent = new Intent();
        intent.setClass(this, HeartBeatService.class);
        startService(intent);
    }

}