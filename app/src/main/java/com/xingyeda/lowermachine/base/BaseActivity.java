package com.xingyeda.lowermachine.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class BaseActivity extends Activity {

    private Toast mToast;

    public static MainApplication mApplication;
    public Context mContext = this;
    public static int mScreenW, mScreenH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        MainApplication.getInstance().addActivity(this);

        //得到屏幕的长宽
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        mScreenW = outMetrics.widthPixels;
        mScreenH = outMetrics.heightPixels;

        mApplication = (MainApplication) getApplication();
        hideBottomUIMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainApplication.getInstance().finishActivity(this);
    }

    protected void showToast(CharSequence text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    protected void showToast(int res) {
        if (mToast == null) {
            mToast = Toast.makeText(this, res, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(res);
        }
        mToast.show();
    }

    protected void openActivity(Class<?> clazz, Bundle extras, boolean isFinish) {
        Intent intent = new Intent(this, clazz);
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
        if (isFinish)
            finish();
    }

    /*
    隐藏虚拟按键,并且全屏
     */
    protected void hideBottomUIMenu() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View view = this.getWindow().getDecorView();
            view.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View view = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            view.setSystemUiVisibility(uiOptions);
        }
    }
}
