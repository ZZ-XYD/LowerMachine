package com.xingyeda.lowermachine.activity;

import android.os.Bundle;
import android.view.KeyEvent;

import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.base.BaseActivity;

public class CallActivity extends BaseActivity {

    private String phoneInfo = "";
    private String nameInfo = "";
    private String pwdInfo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_0) {//0
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_1) {//1
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_2) {//2
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_3) {//3
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_4) {//4
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_5) {//5
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_6) {//6
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_7) {//7
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_8) {//8
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_9) {//9
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_STAR) {//*
            finish();
        } else { //#
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
