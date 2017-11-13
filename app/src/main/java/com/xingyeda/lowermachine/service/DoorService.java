package com.xingyeda.lowermachine.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.hurray.plugins.rkctrl;
import com.hurray.plugins.serial;
import com.xingyeda.lowermachine.utils.SharedPreferencesUtils;

public class DoorService extends Service {

    private rkctrl mRkctrl = new rkctrl();
    private serial mSerial = new serial();
    private String arg = "/dev/ttyS1,9600,N,1,8";
    private Thread pthread = null;
    private int iRead = 0;
    private SharedPreferencesUtils preferencesUtils;

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
        int iret = mSerial.open(arg);
        if (iret > 0) {
            iRead = iret;
            runReadSerial(iRead);
        }
    }

    // 读取串口数据线程
    public void runReadSerial(final int fd) {
        Runnable run = new Runnable() {
            public void run() {
                String cardId = "";
                String idData = "";
                while (true) {
                    int r = mSerial.select(fd, 1, 0);
                    if (r == 1) {
                        byte[] buf = new byte[50];
                        buf = mSerial.read(fd, 100);
                        cardId += byte2HexString(buf);
                        if (cardId.length() >= 28) {
                            idData = cardId.substring(0, 28);
                            if (idData.equals("57434441000000002e320d160023")) {
                                mRkctrl.exec_io_cmd(6, 1);//开门
                                cardId = "";
                                idData = "";
                                try {
                                    pthread.sleep(1000 * 3);
                                    mRkctrl.exec_io_cmd(6, 0);//关门
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
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
}
