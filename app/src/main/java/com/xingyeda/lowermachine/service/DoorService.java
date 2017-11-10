package com.xingyeda.lowermachine.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.hurray.plugins.rkctrl;
import com.hurray.plugins.serial;
import com.xingyeda.lowermachine.utils.SharedPreferencesUtils;

public class DoorService extends Service {

    private rkctrl mRkctrl = new rkctrl();
    private serial pSerialport = new serial();
    private String arg = "/dev/ttyS1,9600,N,1,8";
    private Thread pthread = null;
    private int iRead = 0;
    private String cardId = "";
    private SharedPreferencesUtils preferencesUtils;

    private MyHandler myHandler = new MyHandler();

    public DoorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferencesUtils = new SharedPreferencesUtils(this);
        initSerial();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void initSerial() {
        int iret = pSerialport.open(arg);
        if (iret > 0) {
            iRead = iret;
            log(String.format("打开串口成功 (port = %s,fd=%d)", arg, iret));

            runReadSerial(iRead);
        } else {
            log(String.format("打开串口失败 (fd=%d)", iret));
        }
    }

    // 读取串口数据线程
    public void runReadSerial(final int fd) {
        Runnable run = new Runnable() {
            public void run() {
                while (true) {
                    int r = pSerialport.select(fd, 1, 0);
                    if (r == 1) {
                        //测试 普通读串口数据
                        byte[] buf = new byte[50];
                        buf = pSerialport.read(fd, 100);
                        String str = "";

                        if (buf == null) break;

                        if (buf.length <= 0) break;

                        str = byte2HexString(buf);

                        Message msgpwd = new Message();
                        msgpwd.what = 1;
                        Bundle data = new Bundle();
                        data.putString("data", str);
                        msgpwd.setData(data);
                        myHandler.sendMessage(msgpwd);

                    }
                }
//                onThreadEnd();
            }
        };
        pthread = new Thread(run);
        pthread.start();
    }

    /**
     * byte[]转换成字符串
     *
     * @param b
     * @return
     */
    public static String byte2HexString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        int length = b.length;
        for (int i = 0; i < b.length; i++) {
            String stmp = Integer.toHexString(b[i] & 0xff);
            if (stmp.length() == 1)
                sb.append("0" + stmp);
            else
                sb.append(stmp);
        }
        return sb.toString();
    }

//    public void onThreadEnd(){
//        this.runOnUiThread(new Runnable() {
//            public void run() {
//                log(String.format("%s", "监听串口线程结束"));
//            }
//        });
//    }

    public void log(String str) {
        System.out.println("[output] " + str);
        Log.v("info", str);
    }

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String strData = "";
            switch (msg.what) {
                case 1:
                    strData = msg.getData().getString("data");

                    cardId += strData;
                    if (cardId.length() == 28) {
                        String string = (String) preferencesUtils.get("code", "");
                        if (cardId.equals(string)){
                            mRkctrl.exec_io_cmd(6, 1);//打开
                        }else {
                            mRkctrl.exec_io_cmd(6, 0);//打开
                        }
                    }
                    break;
            }
        }
    }
}
