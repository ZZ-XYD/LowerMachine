package com.xingyeda.lowermachine.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hurray.plugins.rkctrl;
import com.hurray.plugins.serial;
import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.base.BaseActivity;

public class DoorActivity extends BaseActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private Button openBtn_camera, closeBtn_camera, openBtn_lock_1, closeBtn_lock_1, openBtn_lock_2, closeBtn_lock_2,
            openBtn_backlight, closeBtn_backlight, openBtn_4G, closeBtn_4G, openBtn_externalLight, closeBtn_externalLight,
            openBtn_lockStatus, closeBtn_lockStatus, openBtn_safeStatus, closeBtn_safeStatus;
    private EditText msgOutPut, rfidOutPut, cd4051OutPut, keyOutPut;
    private int iRead;
    private String arg = "/dev/ttyS1,9600,N,1,8";
    private serial mSerial = new serial();
    private rkctrl mRkctrl = new rkctrl();
    private MyHandler myHandler = new MyHandler();
    private Thread mThread;
    private TextToSpeech mTextToSpeech;
    private boolean bFlag_light = true;
    private boolean bFlag_lock = true;
    private boolean bFlag_safe = true;

    private String strMsgOutput;
    private String strRfid;
    private String strGpiostatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);

        openBtn_camera = findViewById(R.id.openBtn_camera);
        closeBtn_camera = findViewById(R.id.closeBtn_camera);
        openBtn_lock_1 = findViewById(R.id.openBtn_lock_1);
        closeBtn_lock_1 = findViewById(R.id.closeBtn_lock_1);
        openBtn_lock_2 = findViewById(R.id.openBtn_lock_2);
        closeBtn_lock_2 = findViewById(R.id.closeBtn_lock_2);
        openBtn_backlight = findViewById(R.id.openBtn_backlight);
        closeBtn_backlight = findViewById(R.id.closeBtn_backlight);
        openBtn_4G = findViewById(R.id.openBtn_4G);
        closeBtn_4G = findViewById(R.id.closeBtn_4G);
        openBtn_externalLight = findViewById(R.id.openBtn_externalLight);
        closeBtn_externalLight = findViewById(R.id.closeBtn_externalLight);
        openBtn_lockStatus = findViewById(R.id.openBtn_lockStatus);
        closeBtn_lockStatus = findViewById(R.id.closeBtn_lockStatus);
        openBtn_safeStatus = findViewById(R.id.openBtn_safeStatus);
        closeBtn_safeStatus = findViewById(R.id.closeBtn_safeStatus);
        msgOutPut = findViewById(R.id.msgOutPut);
        rfidOutPut = findViewById(R.id.rfidOutPut);
        cd4051OutPut = findViewById(R.id.cd4051OutPut);
        keyOutPut = findViewById(R.id.keyOutPut);

        openBtn_camera.setOnClickListener(this);
        closeBtn_camera.setOnClickListener(this);
        openBtn_lock_1.setOnClickListener(this);
        closeBtn_lock_1.setOnClickListener(this);
        openBtn_lock_2.setOnClickListener(this);
        closeBtn_lock_2.setOnClickListener(this);
        openBtn_backlight.setOnClickListener(this);
        closeBtn_backlight.setOnClickListener(this);
        openBtn_4G.setOnClickListener(this);
        closeBtn_4G.setOnClickListener(this);
        openBtn_externalLight.setOnClickListener(this);
        closeBtn_externalLight.setOnClickListener(this);
        openBtn_lockStatus.setOnClickListener(this);
        closeBtn_lockStatus.setOnClickListener(this);
        openBtn_safeStatus.setOnClickListener(this);
        closeBtn_safeStatus.setOnClickListener(this);

        initSerial();
        initSystemEvent();

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.openBtn_camera:
                mRkctrl.exec_io_cmd(1, 1);
                log("打开摄像头补光灯");
                showToast("打开摄像头补光灯");
                break;
            case R.id.closeBtn_camera:
                mRkctrl.exec_io_cmd(1, 0);
                log("关闭摄像头补光灯");
                showToast("关闭摄像头补光灯");
                break;
            case R.id.openBtn_lock_1:
                mRkctrl.exec_io_cmd(4, 1);
                log("打开电压控制电磁锁");
                showToast("打开电压控制电磁锁");
                break;
            case R.id.closeBtn_lock_1:
                mRkctrl.exec_io_cmd(4, 0);
                log("关闭电压控制电磁锁");
                showToast("关闭电压控制电磁锁");
                break;
            case R.id.openBtn_lock_2:
                mRkctrl.exec_io_cmd(6, 1);
                log("打开继电器控制电磁所");
                showToast("打开继电器控制电磁所");
                break;
            case R.id.closeBtn_lock_2:
                mRkctrl.exec_io_cmd(6, 0);
                log("关闭继电器控制电磁所");
                showToast("关闭继电器控制电磁所");
                break;
            case R.id.openBtn_backlight:
                mRkctrl.exec_io_cmd(13, 1);
                log("打开按键板背光灯");
                showToast("打开按键板背光灯");
                break;
            case R.id.closeBtn_backlight:
                mRkctrl.exec_io_cmd(13, 0);
                log("关闭按键板背光灯");
                showToast("关闭按键板背光灯");
                break;
            case R.id.openBtn_4G:
                mRkctrl.exec_io_cmd(15, 1);
                log("打开4G模块供电");
                showToast("打开4G模块供电");
                break;
            case R.id.closeBtn_4G:
                mRkctrl.exec_io_cmd(15, 0);
                log("关闭4G模块供电");
                showToast("关闭4G模块供电");
                break;
            case R.id.openBtn_externalLight:
                bFlag_light = true;
                runReadlight();
                log("打开检测外部光线");
                showToast("打开检测外部光线");
                break;
            case R.id.closeBtn_externalLight:
                bFlag_light = false;
                log("关闭检测外部光线");
                showToast("关闭检测外部光线");
                break;
            case R.id.openBtn_lockStatus:
                bFlag_lock = true;
                runReadLockStatus();
                log("打开检测电磁锁状态");
                showToast("打开检测电磁锁状态");
                break;
            case R.id.closeBtn_lockStatus:
                bFlag_lock = false;
                log("关闭检测电磁锁状态");
                showToast("关闭检测电磁锁状态");
                break;
            case R.id.openBtn_safeStatus:
                bFlag_safe = true;
                runReadAlarm();
                log("打开检测拆机报警");
                showToast("打开检测拆机报警");
                break;
            case R.id.closeBtn_safeStatus:
                bFlag_safe = false;
                log("关闭检测拆机报警");
                showToast("关闭检测拆机报警");
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_0) {
            keyOutPut.setText("0");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_1) {
            keyOutPut.setText("1");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_2) {
            keyOutPut.setText("2");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_3) {
            keyOutPut.setText("3");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_4) {
            keyOutPut.setText("4");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_5) {
            keyOutPut.setText("5");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_6) {
            keyOutPut.setText("6");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_7) {
            keyOutPut.setText("7");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_8) {
            keyOutPut.setText("8");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_9) {
            keyOutPut.setText("9");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_STAR) {
            keyOutPut.setText("*");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_POUND) {
            keyOutPut.setText("");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_F4) {
            keyOutPut.setText("️➡️");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_F3) {
            keyOutPut.setText("⬅️️");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_F2) {
            keyOutPut.setText("管理处");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_F1) {
            keyOutPut.setText("帮助");
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
        讯飞语音状态反馈
         */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            showToast("讯飞语音成功");
        } else if (status == TextToSpeech.ERROR) {
            showToast("讯飞语音失败");
        }
    }

    private void initSerial() {
        int iRet = mSerial.open(arg);
        if (iRet > 0) {
            iRead = iRet;
            log(String.format("打开串口成功 (port = %s,fd=%d)", arg, iRet));
            showToast(String.format("打开串口成功 (port = %s,fd=%d)", arg, iRet));
            runReadSerial(iRead);
        } else {
            log(String.format("打开串口失败 (fd=%d)", iRet));
        }
    }

    private void initSystemEvent() {
        //读取8路安防
        runReadCd4051();
        //初始化TTS
        mTextToSpeech = new TextToSpeech(this, this);
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
                        String str = byte2HexString(buf);

                        Message msg = new Message();
                        msg.what = 1;
                        Bundle data = new Bundle();
                        data.putString("data", str);
                        msg.setData(data);
                        myHandler.sendMessage(msg);
                    }
                }
                onThreadEnd();
            }
        };
        mThread = new Thread(run);
        mThread.start();
    }

    /*
    读取8路安防状态线程
     */
    public void runReadCd4051() {
        Runnable run = new Runnable() {
            public void run() {
                while (true) {

                    //延迟读取
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < 8; i++) {
                        int r = mRkctrl.get_adc_status(i);
                        if (r == 1) {
                            String msg = String.format("GPIO输入口%d ,获取值为%d", i, r);
                            Log.v("info", msg);
                            showToast(msg);

                            Message msgpwd = new Message();
                            msgpwd.what = 2;
                            Bundle data = new Bundle();
                            data.putString("data", msg);
                            msgpwd.setData(data);
                            myHandler.sendMessage(msgpwd);
                        }
                    }
                }
            }
        };
        mThread = new Thread(run);
        mThread.start();
    }

    /*
    检查外部光线状态线程
     */
    public void runReadlight() {
        Runnable run = new Runnable() {
            public void run() {
                while (bFlag_light) {
                    //延迟读取
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    int gpioid = 2;
                    int r = mRkctrl.get_io_status(gpioid);
                    String msg = null;
                    if (r == 0) {
                        msg = String.format("检测当前为白天");
                    } else if (r == 1) {
                        msg = String.format("检测当前为黑夜");
                    }

                    Message msgpwd = new Message();
                    msgpwd.what = 3;
                    Bundle data = new Bundle();
                    data.putString("data", msg);
                    msgpwd.setData(data);
                    myHandler.sendMessage(msgpwd);

                }
            }
        };
        mThread = new Thread(run);
        mThread.start();
    }

    /*
    检查电磁锁状态线程
     */
    public void runReadLockStatus() {
        Runnable run = new Runnable() {
            public void run() {
                while (bFlag_lock) {
                    //延迟读取
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    int gpioid = 9;
                    int r = mRkctrl.get_io_status(gpioid);
                    if (r == 0) {
                        String msg = String.format("无电磁锁开门信号");

                        Message msgpwd = new Message();
                        msgpwd.what = 4;
                        Bundle data = new Bundle();
                        data.putString("data", msg);
                        msgpwd.setData(data);
                        myHandler.sendMessage(msgpwd);
                    } else if (r == 1) {
                        String msg = String.format("接收电磁锁开门信号");

                        Message msgpwd = new Message();
                        msgpwd.what = 4;
                        Bundle data = new Bundle();
                        data.putString("data", msg);
                        msgpwd.setData(data);
                        myHandler.sendMessage(msgpwd);
                    }

                }
            }
        };
        mThread = new Thread(run);
        mThread.start();
    }

    /*
    检查拆机报警状态线程
     */
    public void runReadAlarm() {
        Runnable run = new Runnable() {
            public void run() {
                while (bFlag_safe) {
                    //延迟读取
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    int gpioid = 11;
                    int r = mRkctrl.get_io_status(gpioid);

                    if (r == 0) {
                        String msg = String.format("无拆机报警信号");
                        Message msgpwd = new Message();
                        msgpwd.what = 5;
                        Bundle data = new Bundle();
                        data.putString("data", msg);
                        msgpwd.setData(data);
                        myHandler.sendMessage(msgpwd);

                    } else {
                        String msg = String.format("接收拆机报警信号");
                        Message msgpwd = new Message();
                        msgpwd.what = 5;
                        Bundle data = new Bundle();
                        data.putString("data", msg);
                        msgpwd.setData(data);
                        myHandler.sendMessage(msgpwd);
                    }


                }
            }
        };
        mThread = new Thread(run);
        mThread.start();
    }

    public void onThreadEnd() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                log(String.format("%s", "监听串口线程结束"));
                showToast(String.format("%s", "监听串口线程结束"));
            }
        });
    }

    /*
    byte[]转换成字符串
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

    public void msg_output(String msg) {
        strMsgOutput += msg;
        strMsgOutput += "\r\n";
        msgOutPut.setText(strMsgOutput);

        //方便测试用 如果超过显示区域就清空
        if (msgOutPut.getLineCount() > 10) {
            strMsgOutput = "";
            msgOutPut.setText("");
        }
    }

    /*
    讯飞语音播放
     */
    public void OnSpeak(String speakStr) {
        mTextToSpeech.speak(speakStr, TextToSpeech.QUEUE_ADD, null);
    }

    public void log(String str) {
        System.out.println("[output] " + str);
        Log.v("info", str);
    }

    public class MyHandler extends Handler {
        public MyHandler() {
        }

        public MyHandler(Looper L) {
            super(L);
        }

        // 子类必须重写此方法,接受数据
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String strData = "";
            // 此处可以更新UI
            switch (msg.what) {
                case 1:
                    strData = msg.getData().getString("data");

                    strRfid += strData;
                    rfidOutPut.setText(strRfid);
                    log(strRfid);
                    Log.v("test", strRfid);
                    showToast(strRfid);

                    //方便测试用 如果超过显示区域就清空
                    if (rfidOutPut.getLineCount() > 10) {
                        strRfid = "";
                        rfidOutPut.setText("");
                    }

                    break;
                case 2:
                    strData = msg.getData().getString("data");

                    strGpiostatus += strData;
                    strGpiostatus += "\r\n";
                    cd4051OutPut.setText(strGpiostatus);
                    cd4051OutPut.setSelection(cd4051OutPut.getText().length() - 1);

                    //方便测试用 如果超过显示区域就清空
                    if (cd4051OutPut.getLineCount() > 15) {
                        strGpiostatus = "";
                        cd4051OutPut.setText("");
                    }
                    break;
                case 3:
                    strData = msg.getData().getString("data");
                    msg_output(strData);
                    break;

                case 4:
                    strData = msg.getData().getString("data");
                    msg_output(strData);
                    OnSpeak(strData);
                    break;

                case 5:
                    strData = msg.getData().getString("data");
                    msg_output(strData);
                    OnSpeak(strData);
                    break;
            }
        }
    }
}
