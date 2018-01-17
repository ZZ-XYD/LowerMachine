package com.xingyeda.lowermachine.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.hurray.plugins.rkctrl;
import com.hurray.plugins.serial;
import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.bean.CardResult;
import com.xingyeda.lowermachine.utils.BaseUtils;
import com.xingyeda.lowermachine.utils.HttpUtils;
import com.xingyeda.lowermachine.utils.JsonUtils;
import com.xingyeda.lowermachine.utils.SharedPreUtil;
import com.xingyeda.lowermachine.utils.SharedPreferencesUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DoorService extends Service {

    private rkctrl mRkctrl = null;
    private serial mSerial = null;
    private String arg = "/dev/ttyS1,9600,N,1,8";
    private Thread pthread = null;
    private int iRead = 0;
    private Handler mHandler = null;
    private SoundPool mSoundPool;

    public DoorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRkctrl = new rkctrl();
        mSerial = new serial();
        mHandler = new Handler();
        initSP();
        if (android.os.Build.MODEL.equals("rk3168")) {
            initSerial();
            runReadLockStatus();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent();
        intent.setAction("DoorService.onDestroy");
        DoorService.this.sendBroadcast(intent);
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
                String idData28 = "";
                String idData8 = "";
                while (true) {
//                    int r = mSerial.select(fd, 1, 0);
//                    if (r == 1) {
                    byte[] buf = new byte[50];
                    buf = mSerial.read(fd, 100);
                    if (buf != null && buf.length > 0) {
                        cardId += byte2HexString(buf);
                        if (cardId.length() >= 28) {
                            idData28 = cardId.substring(0, 28);
                            idData8 = idData28.substring(16, 24);

                            Map map = new HashMap();
                            map.put("searchType", "getByCode");
                            map.put("snCode", idData8);
                            map.put("dongshu", SharedPreUtil.getString(DoorService.this, "DongShuId"));

                            HttpUtils.doPost(ConnectPath.getPath(DoorService.this, ConnectPath.CARD), map, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    final CardResult cardResult = JsonUtils.getGson().fromJson(response.body().string(), CardResult.class);
                                    if (cardResult.getStatus().equals("200")) {
                                        mRkctrl.exec_io_cmd(6, 1);//开门
                                        mSoundPool.play(1, 1, 1, 0, 0, 1);
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                BaseUtils.showShortToast(getApplicationContext(), cardResult.getMsg());
                                            }
                                        });
                                        try {
                                            pthread.sleep(1000 * 3);
                                            mRkctrl.exec_io_cmd(6, 0);//关门
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                BaseUtils.showShortToast(getApplicationContext(), cardResult.getMsg());
                                            }
                                        });
                                    }
                                }
                            });
                            cardId = "";
                            idData28 = "";
                            idData8 = "";
                        }
                    }
                }
            }
        };
        pthread = new Thread(run);
        pthread.start();
    }

    public void runReadLockStatus() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    int gpioid = 9;
                    int r = mRkctrl.get_io_status(gpioid);
                    if (r == 1) {
                        mRkctrl.exec_io_cmd(6, 1);//开门
                        mSoundPool.play(1, 1, 1, 0, 0, 1);
                        try {
                            sleep(1000 * 3);
                            mRkctrl.exec_io_cmd(6, 0);//关门
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
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

    private void initSP() {
        if (mSoundPool == null) {
            mSoundPool = new SoundPool(3, AudioManager.STREAM_SYSTEM, 5);
            mSoundPool.load(DoorService.this, R.raw.opendoor, 1);
            mSoundPool.load(DoorService.this, R.raw.bujie, 1);
            mSoundPool.load(DoorService.this, R.raw.busy, 1);
        }
    }
}
