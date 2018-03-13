package com.xingyeda.lowermachine.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.vvsip.ansip.IVvsipService;
import com.vvsip.ansip.IVvsipServiceListener;
import com.vvsip.ansip.VvsipCall;
import com.vvsip.ansip.VvsipService;
import com.vvsip.ansip.VvsipServiceBinder;
import com.vvsip.ansip.VvsipTask;
import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InitActivity extends BaseActivity implements IVvsipServiceListener {

    protected int _splashTime = 3000;
    protected Handler _exitHandler = null;
    protected Runnable _exitRunnable = null;
    protected Handler _startServiceHandler = null;
    protected Runnable _startServiceRunnable = null;
    @BindView(R.id.init_text)
    TextView initText;

    private ServiceConnection connection;

//    private int mInitTime = 0;
//    private boolean isSocket = false;
//    private boolean isSip = false;
//    private boolean isDoor = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("ActivitySplash", "生命周期 // onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        ButterKnife.bind(this);


        verifyStoragePermissions(this);




        // 可运行退出启动画面和启动菜单。
        _exitRunnable = new Runnable() {
            public void run() {
                exitSplash();
            }
        };
        // 在“splashtime ms”中运行exitRunnable。
        _exitHandler = new Handler();

        IVvsipService _service = VvsipService.getService();
        if (_service != null) {
            _exitHandler.postDelayed(_exitRunnable, 0);
            return;
        }

        _exitHandler.postDelayed(_exitRunnable, _splashTime);

        _startServiceHandler = new Handler();

        _startServiceRunnable = new Runnable() {
            public void run() {

                Intent intent = new Intent(InitActivity.this.getApplicationContext(), VvsipService.class);
                startService(intent);

                connection = new ServiceConnection() {
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Log.i("ActivitySplash", "连接!");
                        IVvsipService _service = ((VvsipServiceBinder) service).getService();
                        _service.addListener(InitActivity.this);
                    }

                    public void onServiceDisconnected(ComponentName name) {
                        Log.i("ActivitySplash", "断开连接!");
                    }
                };

                bindService(intent, connection, Context.BIND_AUTO_CREATE);
                Log.i("ActivitySplash", "bindService完成!");
            }
        };

        _startServiceHandler.postDelayed(_startServiceRunnable, 0);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onDestroy() {
        Log.i("ActivitySplash", "生命周期 // onDestroy");
        super.onDestroy();
//        unregisterReceiver(mBroadcastReceiver);
        IVvsipService _service = VvsipService.getService();
        if (_service != null) {
            _service.removeListener(this);
        }

        _exitHandler.removeCallbacks(_startServiceRunnable);
        _exitHandler.removeCallbacks(_exitRunnable);
        if (connection != null) {
            unbindService(connection);
            connection = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // 从处理程序队列中删除exitRunnable回调。
            _exitHandler.removeCallbacks(_exitRunnable);
            // 手动运行退出代码。
            exitSplash();
        }
        return true;
    }

    private void exitSplash() {
        Log.i("ActivitySplash", "生命周期 // exitSplash");
        VvsipTask vvsipTask = VvsipTask.getVvsipTask();
        if (vvsipTask != null && VvsipTask.global_failure != 0) {
            final AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setIcon(R.mipmap.ic_launcher);
            b.setTitle(getString(R.string.app_name));
            b.setMessage("global_installation_failure");

            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
            b.show();

            //失败重启服务
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClass(this.getApplicationContext(), VvsipService.class);
            stopService(intent);
        } else {
            //成功进入下一步
            initText.append("\nsip初始化成功");
            finish();

            Intent intent = new Intent();
            intent.setClass(InitActivity.this, MainActivity.class);
            startActivity(intent);
//            if (!isSip) {
//                isSip = true;
//                LogUtils.d("sip初始化成功");
//                initText.append("\nsip初始化成功");
//                initSucceed();
//            }

        }
    }

    @Override
    public void onNewVvsipCallEvent(VvsipCall call) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRemoveVvsipCallEvent(VvsipCall call) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusVvsipCallEvent(VvsipCall call) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRegistrationEvent(int rid, String remote_uri, final int code, String reason) {
        InitActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (code >= 200 && code < 300) {
                    // 从处理程序队列中删除exitRunnable回调。
                    _exitHandler.removeCallbacks(_exitRunnable);
                    // 手动运行退出代码。
                    exitSplash();
                }
            }
        });
    }
}