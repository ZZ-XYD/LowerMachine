package com.xingyeda.lowermachine.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.hurray.plugins.rkctrl;
import com.xingyeda.lowermachine.bean.Message;
import com.xingyeda.lowermachine.utils.JsonUtils;
import com.xingyeda.lowermachine.utils.SharedPreferencesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class HeartBeatService extends Service {

    private Socket mSocket = null;
    private InputStream in = null;
    private OutputStream out = null;
    private SharedPreferencesUtils utils;
    private rkctrl m_rkctrl = new rkctrl();

    public HeartBeatService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        utils = new SharedPreferencesUtils(HeartBeatService.this);

        /*
        发送消息线程
         */
        new Thread() {
            @Override
            public void run() {
                initSocket();
                while (true) {
                    sendMessage();
                    try {
                        sleep(1000 * 10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        /*
        读取消息线程
         */
        new Thread() {
            @Override
            public void run() {
                initSocket();
                while (true) {
                    getMessage();
                    try {
                        sleep(1000 * 3);
//                        m_rkctrl.exec_io_cmd(6, 0);//关闭
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

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

    private void initSocket() {
        if (mSocket == null) {
            try {
                mSocket = new Socket("192.168.10.200", 5888);
                if (out == null) {
                    out = mSocket.getOutputStream();
                }
                if (in == null) {
                    in = mSocket.getInputStream();
                }
            } catch (IOException e) {
                exitForReConnect();
                initSocket();
            }
        }
    }

    /*
    获取消息
     */
    private void getMessage() {
        try {
            byte[] buffer = new byte[in.available()];
            in.read(buffer);

            if (buffer.length == 0) {
                System.out.println(buffer.length);
                return;
            } else {
                System.out.println("-" + buffer.length + "-");
                String responseInfo = new String(buffer, "UTF-8");
                System.out.println("-" + responseInfo + "-");
                String info = responseInfo.substring(4);
                System.out.println("-" + info + "-");
                Message message = JsonUtils.getGson().fromJson(info, Message.class);
                if (message.getCommond() != null) {
                    if (message.getCommond().equals("M001")) {
//                        m_rkctrl.exec_io_cmd(6, 1);//打开
                    }
                }

            }
//            if (responseInfo.contains("M001")) {
//                m_rkctrl.exec_io_cmd(6, 1);//打开
//            }
//            Log.e("Message", aaa);
        } catch (Exception e) {
            exitForReConnect();
            initSocket();
        }
    }

    /*
    发送消息
     */
    private void sendMessage() {
        Message msg = new Message();
        msg.setConverType("Object");
        msg.setContent("KeepLive");
        msg.setCommond("M001");
        msg.setmId((String) utils.get("sncode", ""));

        String jsonObject = JsonUtils.getGson().toJson(msg);

        byte[] objByte = jsonObject.getBytes();
        byte[] intByte = intToBytes(objByte.length);
        byte[] bytes = addBytes(intByte, objByte);

        Log.e("HeartBeatService", String.valueOf(intByte.length));

        try {
            out.write(bytes);
            out.flush();
        } catch (Exception e) {
            exitForReConnect();
            initSocket();
        }
    }

    /*
    关闭流,为了断线重连
     */
    private void exitForReConnect() {
        //关闭流
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (Exception ex) {
        }
    }

    /*
    int转byte[]
     */
    private byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /*
    byte[]转int
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }

    /*
    合并byte[]数组
     */
    private byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i = begin; i < begin + count; i++) bs[i - begin] = src[i];
        return bs;
    }
}
