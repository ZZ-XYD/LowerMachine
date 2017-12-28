package com.xingyeda.lowermachine.activity;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.KeyEvent;

import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipManager;
import com.csipsimple.api.SipProfile;
import com.hurray.plugins.rkctrl;
import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.base.BaseActivity;
import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.business.MainBusiness;
import com.xingyeda.lowermachine.utils.BaseUtils;

import java.util.Timer;
import java.util.TimerTask;

public class CallActivity extends BaseActivity {

    private rkctrl mRkctrl = new rkctrl();

    private String phoneInfo = "";
    private String nameInfo = "";
    private String pwdInfo = "";

    private long existingProfileId = SipProfile.INVALID_ID;
    private ISipService service;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = ISipService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };

//    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        Bundle bundle = getIntent().getExtras();
        phoneInfo = bundle.getString("mPhone");
        nameInfo = bundle.getString("userName");
        pwdInfo = bundle.getString("userPwd");

        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        // Optional, but here we bundle so just ensure we are using csipsimple package
        serviceIntent.setPackage(this.getPackageName());
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        registerSIP();

//        timer.schedule(timerTask, 1000 * 10);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        timer.cancel();
        mContext.unbindService(connection);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_0) {//0
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_1) {//1
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_2) {//2
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_3) {//3
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_4) {//4
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_5) {//5
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_6) {//6
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_7) {//7
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_8) {//8
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_9) {//9
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else if (keyCode == KeyEvent.KEYCODE_STAR) {//*
            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        } else { //#
            placeCallWithOption();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void registerSIP() {
        String fullUser = nameInfo + "@" + ConnectPath.SIP_HOST;
        String[] splitUser = fullUser.split("@");

        SipProfile builtProfile = new SipProfile();
        builtProfile.display_name = "兴业达科技";
        builtProfile.id = SipProfile.INVALID_ID;
        builtProfile.acc_id = "<sip:" + fullUser + ">";
        builtProfile.reg_uri = "sip:" + splitUser[1];
        builtProfile.realm = "*";
        builtProfile.username = splitUser[0];
        builtProfile.data = pwdInfo;
        builtProfile.proxies = new String[]{"sip:" + splitUser[1]};

        ContentValues builtValues = builtProfile.getDbContentValues();

        if (existingProfileId != SipProfile.INVALID_ID) {
            getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, existingProfileId), builtValues, null, null);
        } else {
            Uri savedUri = getContentResolver().insert(SipProfile.ACCOUNT_URI, builtValues);
            if (savedUri != null) {
                existingProfileId = ContentUris.parseId(savedUri);
            }
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000 * 3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                placeCallWithOption();
            }
        }.start();
        MainBusiness.releaseAccount(mContext, nameInfo);
    }

    private void placeCallWithOption() {
        if (service == null) {
            return;
        }
        try {
            service.makeCallWithOptions(phoneInfo, 1, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        BaseUtils.startActivity(mContext, MainActivity.class);
        finish();
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
//            BaseUtils.startActivity(mContext, MainActivity.class);
            finish();
        }
    };
}
