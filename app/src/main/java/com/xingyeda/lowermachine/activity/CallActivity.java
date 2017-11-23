package com.xingyeda.lowermachine.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.hurray.plugins.rkctrl;
import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.base.BaseActivity;
import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.business.MainBusiness;
import com.xingyeda.lowermachine.http.BaseStringCallback;
import com.xingyeda.lowermachine.http.CallbackHandler;
import com.xingyeda.lowermachine.http.ConciseCallbackHandler;
import com.xingyeda.lowermachine.http.ConciseStringCallback;
import com.xingyeda.lowermachine.http.OkHttp;
import com.xingyeda.lowermachine.utils.BaseUtils;
import com.xingyeda.lowermachine.utils.SharedPreUtil;
import com.xingyeda.lowermachine.view.layout.PercentLinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class CallActivity extends BaseActivity {
    private static final String LOG_TAG = CallActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    @BindView(R.id.test)
    PercentLinearLayout test;
    @BindView(R.id.door_number)
    TextView doorNumber;
    @BindView(R.id.call_timer)
    TextView callTimer;

    private rkctrl m_rkctrl = new rkctrl();
    private String mDoorNumber = new String();
    private MediaPlayer mMediaPlayer;

    private String mUserId = "";
    private String mCallId = "";
    private String mHousenum = "";
    private String mPhone = "";
    private Timer mTimer = new Timer();
    private Timer mCallTimer = new Timer();
    private rkctrl mRkctrl = new rkctrl();
    private String mCallNumber;

    private boolean mIsCall = false;

    private RtcEngine mRtcEngine;//  教程步骤 1
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // 教程步骤1  回调
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { //  教程步骤 5  远端视频接收解码回调
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) { //  教程步骤 7 其他用户离开当前频道回调
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft();
                }
            });
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { //  教程步骤 10  其他用户已停发/已重发视频流回调
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    onRemoteUserVideoMuted(uid, muted);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        ButterKnife.bind(this);

        mCallNumber = getIntent().getExtras().getString("stringValue");
        if (!mCallNumber.equals("")) {
            mDoorNumber += mCallNumber;
            if (doorNumber!=null) {
                doorNumber.append(mCallNumber);
            }
        }

        registerBoradcastReceiver();

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initAgoraEngineAndJoinChannel();
        }
    }

    //初始化声网引擎和加入频道
    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     //  教程步骤 1
        setupVideoProfile();         //  教程步骤 2
        setupLocalVideo();           //  教程步骤 3
        joinChannel();               //  教程步骤 4
    }

    //自我检查权限
    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "自我检查权限 " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "请求权限的结果 " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("没有许可 " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("没有许可 " + Manifest.permission.CAMERA);
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mContext.unregisterReceiver(mBroadcastReceiver);
        ReleasePlayer();
        clearAll();

        leaveChannel();
        RtcEngine.destroy();//销毁引擎实例
        mRtcEngine = null;
    }

    // 教程步骤 10   本地视频开关
    public void onLocalVideoMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalVideoStream(iv.isSelected());

        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
        surfaceView.setZOrderMediaOverlay(!iv.isSelected());
        surfaceView.setVisibility(iv.isSelected() ? View.GONE : View.VISIBLE);
    }

    // 教程步骤 9  本地音频静音（声控制）
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    //  教程步骤 8 摄像头切换
    public void onSwitchCameraClicked(View view) {
        mRtcEngine.switchCamera();
    }

    //  教程步骤 6  挂断
    public void onEncCallClicked(View view) {
        finish();
    }

    //  教程步骤 1
    private void initializeAgoraEngine() {
        try {
            /**
             * 创建 RtcEngine 对象。
             * 上下文
             * appid
             * 加入回调
             */
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("需要检查rtc sdk init致命错误\n" + Log.getStackTraceString(e));
        }
    }

    //  教程步骤 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();//打开视频模式
        /**
         * 设置本地视频属性
         * 视频属性
         * 是否交换宽高
         */
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
    }

    //  教程步骤 3
    private void setupLocalVideo() {
        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());//创建渲染视图 ----上下文
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);//将渲染视图添加进去
        /**
         * 设置本地视频显示属性
         * VideoCanvas
         *        视频显示视窗
         *        视频显示模式  RENDER_MODE_HIDDEN (1): 如果视频尺寸与显示视窗尺寸不一致，则视频流会按照显示视窗的比例进行周边裁剪或图像拉伸后填满视窗。
         RENDER_MODE_FIT(2): 如果视频尺寸与显示视窗尺寸不一致，在保持长宽比的前提下，将视频进行缩放后填满视窗。
         *        本地用户 ID，与 joinChannel 方法中的 uid 保持一致
         */
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
    }

    //  教程步骤 4
    private void joinChannel() {
        /**
         * 房间钥匙：可以为空
         * 房间名称：也就是通道
         * 房间信息：也就是通道信息
         * 用户id ：会在onJoinChannelSuccess返回
         */
        mRtcEngine.joinChannel(null, MainBusiness.getMacAddress(mContext), "Extra Optional Data", 0); // 如果你不指定uid,我们会为您生成的uid

    }

    //    //  教程步骤 5  设置远端视频显示属性 （连接）
    private void setupRemoteVideo(int uid) {
//        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
//
//        if (container.getChildCount() >= 1) {
//            return;
//        }
//
//        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
//        container.addView(surfaceView);
//        /**
//         * 设置本地视频显示属性
//         * VideoCanvas
//         *        视频显示视窗
//         *        视频显示模式
//         *        本地用户 ID，与 joinChannel 方法中的 uid 保持一致
//         */
//        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
//
//        surfaceView.setTag(uid); // 马克的目的
//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // 可选的界面
//        tipMsg.setVisibility(View.GONE);
    }

    //  教程步骤 6  离开频道
    private void leaveChannel() {
        mRtcEngine.leaveChannel();//离开频道
    }

    //  教程步骤 7  远程用户离开  （挂断）
    private void onRemoteUserLeft() {
//        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
//        container.removeAllViews();
//
//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // 可选的界面
//        tipMsg.setVisibility(View.VISIBLE);
    }

    //  教程步骤 10   远程用户已停发/已重发视频流
    private void onRemoteUserVideoMuted(int uid, boolean muted) {
//        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
//
//        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
//
//        Object tag = surfaceView.getTag();
//        if (tag != null && (Integer) tag == uid) {
//            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
//        }
    }


    private void callOut(String callinfo) {
        if (callTimer!=null) {
            callTimer.setText("呼叫中");
        }
        promptTone(R.raw.ringback, true);
        callTime(10000);
        mIsCall = true;
        mDoorNumber = "";
        mHousenum = callinfo;
        Map<String, String> params = new HashMap<>();
        params.put("randomCode", getRandom());
        params.put("callinfo", callinfo);
        params.put("eid", MainBusiness.getMacAddress(mContext));
        params.put("block", "00");
        params.put("isxiaoqu", SharedPreUtil.getString(mContext, "isxiaoqu"));
        params.put("paizhao", "false");
//        BaseUtils.showLongToast(mContext,ConnectPath.CALLUSER_PATH(mContext)+params);
        OkHttp.get(ConnectPath.getPath(mContext,ConnectPath.CALLUSER_PATH), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {//成功
                try {
                    mUserId = response.has("userId") ? response.getString("userId") : "";
                    mPhone = response.has("phone") ? response.getString("phone") : "";
                    mCallId = response.has("callId") ? response.getString("callId ") : "";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void parameterError(JSONObject response) {//失败
                mIsCall = false;
                ReleasePlayer();
                promptTone(R.raw.calltips, false);
//                if (mCallTimer != null) {
//                    mCallTimer.cancel();
//                }
                if (callTimer!=null) {
                    callTimer.setText("呼叫失败");
                }
            }

            @Override
            public void onFailure() {//接口问题
                mIsCall = false;
                ReleasePlayer();
                promptTone(R.raw.calltips, false);
//                if (mCallTimer != null) {
//                    mCallTimer.cancel();
//                }
                if (callTimer!=null) {
                    callTimer.setText("服务器异常，请联系管理员");
                }
            }
        }));


    }

    //挂断
    private void cancel() {
        if (callTimer!=null) {
            callTimer.setText("挂断中");
        }
        Map<String, String> params = new HashMap<>();
        params.put("eid", MainBusiness.getMacAddress(mContext));
        params.put("uid", mUserId);
        params.put("housenum", mHousenum);
        params.put("dongshu", SharedPreUtil.getString(mContext, "DongShuId"));
        OkHttp.get(ConnectPath.getPath(mContext,ConnectPath.CANCEL_PATH), params, new ConciseStringCallback(mContext, new ConciseCallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {
                finish();
            }
        }));
    }

    //本地电话查询
    private void checkPhone(String phone) {
        Map<String, String> params = new HashMap<>();
        params.put("tel", phone);
        OkHttp.get(ConnectPath.getPath(mContext,ConnectPath.CHECKPHONE_PATH), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {//本地

            }

            @Override
            public void parameterError(JSONObject response) {//外地

            }

            @Override
            public void onFailure() {//出错

            }
        }));
    }

    public void registerBoradcastReceiver() {
        IntentFilter intent = new IntentFilter();
        intent.addAction("HeartBeatService.HANG_UP");//手机直接挂断
        intent.addAction("HeartBeatService.REMOTE_RELEASE");//手机接通后挂断
        intent.addAction("HeartBeatService.MOBILE_ANSWER");//手机接通视频通话
        intent.addAction("HeartBeatService.MOBILE_RECEIVE");//手机收到呼入
        // 注册广播
        mContext.registerReceiver(mBroadcastReceiver, intent);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("HeartBeatService.HANG_UP")) {//手机直接挂断
                BaseUtils.showShortToast(mContext,"对方已挂断，通话结束中");
                finish();
            } else if (action.equals("HeartBeatService.REMOTE_RELEASE")) {//手机接通后挂断
                BaseUtils.showShortToast(mContext,"对方已挂断，通话结束中");
                finish();
            } else if (action.equals("HeartBeatService.MOBILE_ANSWER")) {//手机接通视频通话
                updateTime();
//                mIsCall = false;
                ReleasePlayer();
                connectTime(60000);
            } else if (action.equals("HeartBeatService.MOBILE_RECEIVE")) {//手机收到呼入
                if (mCallTimer != null) {
                    mCallTimer.cancel();
                }
            }
        }

    };

    private int mCount = 0;
    private void updateTime() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mCount += 1;
                if (callTimer!=null) {
                    callTimer.setText(getStandardTime(mCount));
                    updateTime();
                }
            }
        };
        new Handler().postDelayed(runnable, 1000);
    }


    @OnClick(R.id.test)
    public void onViewClicked() {
        callOut("8888");
    }

    private void clearAll() {
        mUserId = "";
        mCallId = "";
        mHousenum = "";
        mPhone = "";
        mIsCall = false;
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mCallTimer != null) {
            mCallTimer.cancel();
        }

    }

    //接通计时  默认60秒----收到接通信息时调用
    private void connectTime(int time) {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, time);
    }

    //电话呼叫计时  默认60秒
    private void callTime(int time) {
        mCallTimer.schedule(new TimerTask() {
            @Override
            public void run() {//呼叫电话
                ReleasePlayer();
                phoneCall(1,"start");
                promptTone(R.raw.record, false);
            }
        }, time);
    }
    private void phoneCall(int type,String flag){
        Map<String, String> params = new HashMap<>();
        params.put("flag", flag);//flag = start(开始调用)，fail(失败)
        params.put("uid", mUserId);
        switch (type){
            case 1:
                params.put("callId", mCallId);
                params.put("eid", MainBusiness.getMacAddress(mContext));
                break;
            case 2:
                params.put("id", mCallId);
                break;
        }
        OkHttp.get(ConnectPath.getPath(mContext,ConnectPath.PHONECALL_PATH),new ConciseStringCallback(mContext, new ConciseCallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }));
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsCall) {
            if (keyCode == KeyEvent.KEYCODE_STAR) {//*
                cancel();
                return false;
            }
        } else {
            if (keyCode == KeyEvent.KEYCODE_0) {//0
                mDoorNumber += "0";
                doorNumber.append("0");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_1) {//1
                mDoorNumber += "1";
                doorNumber.append("1");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_2) {//2
                mDoorNumber += "2";
                doorNumber.append("2");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_3) {//3
                mDoorNumber += "3";
                doorNumber.append("3");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_4) {//4
                mDoorNumber += "4";
                doorNumber.append("4");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_5) {//5
                mDoorNumber += "5";
                doorNumber.append("5");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_6) {//6
                mDoorNumber += "6";
                doorNumber.append("6");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_7) {//7
                mDoorNumber += "7";
                doorNumber.append("7");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_8) {//8
                mDoorNumber += "8";
                doorNumber.append("8");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_9) {//9
                mDoorNumber += "9";
                doorNumber.append("9");
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_STAR) {//*
                if (!mDoorNumber.equals("")) {
                    mDoorNumber = "";
                    doorNumber.setText("");
                } else {
                    finish();
                }
//                mDoorNumber += "*";
                return false;
            } else { //#
                if (mDoorNumber != null) {
                    if (mDoorNumber.equals("9999")) {//跳转设置
                        BaseUtils.startActivity(mContext, SetActivity.class);
                        finish();
                    } else if (mDoorNumber.equals("3818")) {//密码开门
                        mDoorNumber = "";
                        doorNumber.setText("");
                        new Thread() {
                            @Override
                            public void run() {
                                mRkctrl.exec_io_cmd(6, 1);//开门
                                try {
                                    sleep(1000 * 3);
                                    mRkctrl.exec_io_cmd(6, 0);//关门
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    } else if (mDoorNumber.equals("3819")) {//重启设备
                        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        powerManager.reboot("");
                    } else if (mDoorNumber.equals("3820")) {//关闭设备
                    } else if (mDoorNumber.equals("3821")) {//设备更新
                    } else {
                        callOut(mDoorNumber);
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private void promptTone(int resId, boolean isCirculation) {
        // 开始播放音乐
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
        mMediaPlayer = MediaPlayer.create(mContext, resId);
        mMediaPlayer.setLooping(isCirculation);
        mMediaPlayer.start();
        if (!isCirculation) {
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    ReleasePlayer();
                }
            });
        }

    }

    //释放提示音播放资源
    private void ReleasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }

    //随机呼叫数
    private String getRandom() {
        String strRand = "";
        for (int i = 0; i < 8; i++) {
            strRand += String.valueOf((int) (Math.random() * 10));
        }
        return strRand;
    }

    //时间换算
    public String getStandardTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss",
                Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        Date date = new Date(timestamp * 1000);
        sdf.format(date);
        return sdf.format(date);
    }

    public int getTimerTime(Context context) {
        if (SharedPreUtil.getString(context, "timerTime").equals("")) {
            return 30*1000;
        }
        return Integer.valueOf(SharedPreUtil.getString(context, "timerTime"))*1000;
    }
}
