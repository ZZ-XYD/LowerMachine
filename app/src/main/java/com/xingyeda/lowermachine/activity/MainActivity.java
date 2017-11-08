package com.xingyeda.lowermachine.activity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hurray.plugins.serial;
import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.base.BaseActivity;
import com.xingyeda.lowermachine.business.MainBusiness;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static com.hurray.plugins.serial.byte2HexString;

public class MainActivity extends BaseActivity {

    private serial mSerial = new serial();
    private String arg = "/dev/ttyS1,9600,N,1,8";
    private int iRead = 0;
    private Thread mThread = null;
    private String cardId = "";
    private Button mButton;
    private TextView mTextView;
    private MyHandler myHandler = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initSerial();

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        mButton = findViewById(R.id.mainBtn1);
        mTextView = findViewById(R.id.mainTxt);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity(DoorActivity.class, null, false);
            }
        });
        MainBusiness.getSN(this, wifiInfo.getMacAddress());
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
    protected void onDestroy() {
        super.onDestroy();
    }


    /*
    初始化串口
     */
    private void initSerial() {
        int iret = mSerial.open(arg);
        if (iret > 0) {
            iRead = iret;
            showToast(String.format("打开串口成功 (port = %s,fd=%d)", arg, iret));
            runReadSerial(iRead);
        } else {
            showToast(String.format("打开串口失败 (fd=%d)", iret));
        }
    }

    /*
    读取串口数据线程
     */
    public void runReadSerial(final int fd) {
        Runnable run = new Runnable() {
            public void run() {
                while (true) {
                    int r = mSerial.select(fd, 1, 0);
                    if (r == 1) {
                        //测试 普通读串口数据
                        byte[] buf = new byte[50];
                        buf = mSerial.read(fd, 100);

                        if (buf == null) break;

                        if (buf.length <= 0) break;

                        String strData = byte2HexString(buf);

                        Message msgpwd = new Message();
                        msgpwd.what = 1;
                        Bundle data = new Bundle();
                        data.putString("data", strData);
                        msgpwd.setData(data);
                        myHandler.sendMessage(msgpwd);

                    }
                }
                onThreadEnd();
            }
        };
        mThread = new Thread(run);
        mThread.start();
    }

    /*
    byte[]转换成字符串
     */
//    public static String byte2HexString(byte[] b) {
//        StringBuffer sb = new StringBuffer();
//        int length = b.length;
//        for (int i = 0; i < b.length; i++) {
//            String stmp = Integer.toHexString(b[i] & 0xff);
//            if (stmp.length() == 1)
//                sb.append("0" + stmp);
//            else
//                sb.append(stmp);
//        }
//        return sb.toString();
//    }

    public void onThreadEnd() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                showToast(String.format("%s", "监听串口线程结束"));
                Log.v("MainActivity", String.format("%s", "监听串口线程结束"));
            }
        });
    }

    /*
    通过网络接口获取MAC地址
     */
    private static String getAdressMacByInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return null;
                    }
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }
                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String strData = msg.getData().getString("data");
//                    cardId += strData;
                    mTextView.setText(strData);
                    showToast(strData);
                    break;
            }
        }
    }
}