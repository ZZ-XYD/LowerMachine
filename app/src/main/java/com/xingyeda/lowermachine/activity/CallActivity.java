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
import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.base.BaseActivity;
import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.business.MainBusiness;
import com.xingyeda.lowermachine.utils.BaseUtils;

import java.util.TimerTask;

public class CallActivity extends BaseActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        Bundle bundle = getIntent().getExtras();
        phoneInfo = bundle.getString("mPhone");
        nameInfo = bundle.getString("userName");
        pwdInfo = bundle.getString("userPwd");

        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        serviceIntent.setPackage(this.getPackageName());
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        registerSIP();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext.unbindService(connection);
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

}
