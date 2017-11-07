package com.xingyeda.lowermachine.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    private static ToastUtils mToastUtils;
    private static Toast mToast;

    private ToastUtils() {
    }

    public static ToastUtils getInstance() {
        if (mToastUtils == null) {
            //同步锁
            synchronized (ToastUtils.class) {
                mToastUtils = new ToastUtils();
            }
        }
        return mToastUtils;
    }

    public static void showToast(Context context, CharSequence text) {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    public static void showToast(Context context, int res) {
        if (mToast == null) {
            mToast = Toast.makeText(context, res, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(res);
        }
        mToast.show();
    }
}
