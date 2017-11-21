package com.xingyeda.lowermachine.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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

import com.hurray.plugins.rkctrl;
import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.adapter.GlideImageLoader;
import com.xingyeda.lowermachine.base.BaseActivity;
import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.business.MainBusiness;
import com.xingyeda.lowermachine.http.BaseStringCallback;
import com.xingyeda.lowermachine.http.CallbackHandler;
import com.xingyeda.lowermachine.http.OkHttp;
import com.xingyeda.lowermachine.utils.BaseUtils;
import com.xingyeda.lowermachine.utils.SharedPreUtil;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;


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

    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.main_time)
    TextView time;
    @BindView(R.id.equipment_id)
    TextView equipmentId;
    @BindView(R.id.sn_text)
    TextView snText;
    @BindView(R.id.no_network)
    ImageView noNetwork;
    @BindView(R.id.notification)
    TextView notificationText;
    @BindView(R.id.weather_text)
    TextView weatherText;
    private List<String> mList = new ArrayList<>();
    private BroadcastReceiver networkReceiver;
    private rkctrl mRkctrl = new rkctrl();
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        MainBusiness.getSN(mContext);//获取sn

        updateTime();//时间更新

        ininImage();//图片获取

        getBindMsg();//绑定数据获取

        getInform();//获取通告

        getWeather(1000);//获取天气

        registerBoradcastReceiver();//返回监控

        setEquipmentName();

        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
                NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//                NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if (mNetworkInfo.isConnected() || wifiNetworkInfo.isConnected()) {
                    flag = true;
                } else {
                    flag = false;
                }
            }
        };
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initAgoraEngineAndJoinChannel();
        }
    }


    public void getBindMsg() {
        Map<String, String> params = new HashMap<>();
        params.put("mac", MainBusiness.getMacAddress(mContext));
        OkHttp.get(ConnectPath.BINDMSG_PATH(mContext), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.has("obj")) {
                        JSONObject jobj = (JSONObject) response.get("obj");

                        if (jobj.has("rid"))
                            SharedPreUtil.put(mContext, "XiaoQuId", jobj.getString("rid"));

                        if (jobj.has("rname"))
                            SharedPreUtil.put(mContext, "XiaoQu", jobj.getString("rname"));

                        if (jobj.has("nid"))
                            SharedPreUtil.put(mContext, "QiShuId", jobj.getString("nid"));

                        if (jobj.has("nname"))
                            SharedPreUtil.put(mContext, "QiShu", jobj.getString("nname"));

                        if (jobj.has("tid"))
                            SharedPreUtil.put(mContext, "DongShuId", jobj.getString("tid"));

                        if (jobj.has("tname"))
                            SharedPreUtil.put(mContext, "DongShu", jobj.getString("tname"));

                        if (jobj.has("sn"))
                            SharedPreUtil.put(mContext, "sncode", jobj.getString("sn"));

                        if (jobj.has("isxiaoqu"))
                            SharedPreUtil.put(mContext, "isxiaoqu", jobj.getString("isxiaoqu"));

                        setEquipmentName();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void parameterError(JSONObject response) {

            }

            @Override
            public void onFailure() {

            }
        }));
    }

    private void setEquipmentName() {
        if (!SharedPreUtil.getString(mContext, "sncode").equals("")) {
            if (snText != null) {
                snText.setText("SN : " + SharedPreUtil.getString(mContext, "sncode"));
            }
        }

        String xiaoQu = SharedPreUtil.getString(mContext, "XiaoQu");
        String qiShu = SharedPreUtil.getString(mContext, "QiShu");
        String dongShu = SharedPreUtil.getString(mContext, "DongShu");
        if (!xiaoQu.equals("") && !qiShu.equals("") && !dongShu.equals("")) {
            if (equipmentId != null) {
                equipmentId.setText(xiaoQu + qiShu + dongShu);
            }

        }

    }

    private void ininImage() {
        Map<String, String> params = new HashMap<>();
        params.put("xiaoId", MainBusiness.getMacAddress(mContext));
        OkHttp.get(ConnectPath.IMAGE_PATH(mContext), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("obj")) {
                    try {
                        JSONObject jobj = (JSONObject) response.get("obj");
                        if (banner != null) {
                            banner.setDelayTime(jobj.has("duration") ? (Integer.valueOf(jobj.getString("duration")) * 1000) : 10000);
                        }
                        if (jobj.has("files")) {
                            JSONArray jan = (JSONArray) jobj.get("files");
                            if (jan != null && jan.length() != 0) {
                                for (int i = 0; i < jan.length(); i++) {
                                    JSONObject jobjBean = jan.getJSONObject(i);
                                    if (jobjBean.has("path")) {
                                        mList.add(jobjBean.getString("path"));
                                    }
                                }
                            }
                            if (banner != null) {
                                banner.setImages(mList).setImageLoader(new GlideImageLoader()).setBannerAnimation(Transformer.ZoomOutSlide).setBannerStyle(BannerConfig.NOT_INDICATOR).start();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void parameterError(JSONObject response) {
            }

            @Override
            public void onFailure() {

            }
        }));

    }

    private List<String> notificationList = new ArrayList<>();
    private int notification = 0;

    private void getInform() {
        Map<String, String> params = new HashMap<>();
        params.put("eid", MainBusiness.getMacAddress(mContext));
        OkHttp.get(ConnectPath.INFORM_PATH(mContext), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("obj")) {
                    try {
                        JSONArray jobj = (JSONArray) response.get("obj");
                        if (jobj != null && jobj.length() != 0) {
                            for (int i = 0; i < jobj.length(); i++) {
                                JSONObject jobjBean = jobj.getJSONObject(i);
                                if (jobjBean.has("content")) {
                                    notificationList.add(jobjBean.getString("content"));
                                }
                            }
                            if (notificationList != null && !notificationList.isEmpty()) {
                                carouselMsg(notificationList);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void parameterError(JSONObject response) {
            }

            @Override
            public void onFailure() {

            }
        }));
    }

    private void carouselMsg(final List<String> list) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (list != null && !list.isEmpty()) {
                    if (notification + 1 <= list.size()) {
                        if (notificationText != null) {
                            notificationText.setText(list.get(notification));
                        }
                        if (notification + 1 == list.size()) {
                            notification = 0;
                        } else {
                            notification++;
                        }
                    }
                    if (notificationList != null && !notificationList.isEmpty()) {
                        carouselMsg(notificationList);
                    }
                }

            }
        };
        new Handler().postDelayed(runnable, 1000);
    }

    public void registerBoradcastReceiver() {
        IntentFilter intent = new IntentFilter();
        intent.addAction("HeartBeatService.RELOADIMG");//更新广告
        // 注册广播
        mContext.registerReceiver(mBroadcastReceiver, intent);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("HeartBeatService.RELOADIMG")) {//更新广告
                ininImage();//图片更新
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void updateTime() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd \n  HH:mm:ss");
                String str = sdf.format(new Date());
                if (time != null) {
                    time.setText(str);
                }
                updateTime();

            }
        };
        new Handler().postDelayed(runnable, 1000);
    }

    private void getWeather(int time) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
                Calendar calendar = Calendar.getInstance();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd \n  HH:mm:ss");
//                String str = sdf.format(new Date());
                if (weatherText != null) {
                    weatherText.setText(weekDays[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
                }
                OkHttp.get(ConnectPath.WEATHER_PATH(mContext), new BaseStringCallback(mContext, new CallbackHandler<String>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.has("obj")) {
                            try {
                                JSONObject jobj = (JSONObject) response.get("obj");
                                if (jobj.has("temp")) {
                                    if (weatherText != null) {
                                        weatherText.append(" / " + jobj.getString("temp") + "℃ ");
                                    }
                                }
                                String weatherCode = "";
                                if (jobj.has("img")) {
                                    weatherCode = "w_" + jobj.getString("img");
                                }
                                int resId = getResources().getIdentifier(weatherCode, "mipmap", mContext.getPackageName());
                                if (weatherText != null) {
                                    if (resId != 0) {
                                        weatherText.append("/ ");
                                        Drawable drawable = getResources().getDrawable(resId);
                                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                                        weatherText.setCompoundDrawables(null, null, drawable, null);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void parameterError(JSONObject response) {
                        if (weatherText != null) {
                            weatherText.append("/暂无天气");
                        }
                    }

                    @Override
                    public void onFailure() {
                        if (weatherText != null) {
                            weatherText.append("/暂无天气");
                        }
                    }
                }));
                getWeather(60 * 60 * 1000);
            }
        };
        new Handler().postDelayed(runnable, time);
    }

    @OnClick({R.id.equipment_id, R.id.main_time})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.equipment_id:
                BaseUtils.startActivity(mContext, SetActivity.class);
                break;
            case R.id.main_time:
                BaseUtils.startActivity(mContext, CallActivity.class);
                break;
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
        unregisterReceiver(networkReceiver);
        leaveChannel();
        RtcEngine.destroy();//销毁引擎实例
        mRtcEngine = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Bundle bundle = new Bundle();
        if (keyCode == KeyEvent.KEYCODE_0) {
            bundle.putString("stringValue", "0");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_1) {
            bundle.putString("stringValue", "1");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_2) {
            bundle.putString("stringValue", "2");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_3) {
            bundle.putString("stringValue", "3");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_4) {
            bundle.putString("stringValue", "4");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_5) {
            bundle.putString("stringValue", "5");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_6) {
            bundle.putString("stringValue", "6");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_7) {
            bundle.putString("stringValue", "7");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_8) {
            bundle.putString("stringValue", "8");
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_9) {
            bundle.putString("stringValue", "9");
            return false;
        }
        BaseUtils.startActivities(mContext, CallActivity.class, bundle);
        return super.onKeyDown(keyCode, event);
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
        FrameLayout container = (FrameLayout) findViewById(R.id.main_local_video_view_container);
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
}