package com.xingyeda.lowermachine.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hurray.plugins.rkctrl;
import com.lidroid.xutils.util.LogUtils;
import com.vvsip.ansip.IVvsipService;
import com.vvsip.ansip.IVvsipServiceListener;
import com.vvsip.ansip.VvsipCall;
import com.vvsip.ansip.VvsipDTMF;
import com.vvsip.ansip.VvsipService;
import com.vvsip.ansip.VvsipTask;
import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.adapter.GlideImageLoader;
import com.xingyeda.lowermachine.base.BaseActivity;
import com.xingyeda.lowermachine.base.Commond;
import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.bean.NotificationBean;
import com.xingyeda.lowermachine.bean.SipResult;
import com.xingyeda.lowermachine.business.MainBusiness;
import com.xingyeda.lowermachine.http.BaseStringCallback;
import com.xingyeda.lowermachine.http.CallbackHandler;
import com.xingyeda.lowermachine.http.ConciseCallbackHandler;
import com.xingyeda.lowermachine.http.ConciseStringCallback;
import com.xingyeda.lowermachine.http.OkHttp;
import com.xingyeda.lowermachine.utils.AppUtils;
import com.xingyeda.lowermachine.utils.BaseUtils;
import com.xingyeda.lowermachine.utils.HttpUtils;
import com.xingyeda.lowermachine.utils.JsonUtils;
import com.xingyeda.lowermachine.utils.MyLog;
import com.xingyeda.lowermachine.utils.SharedPreUtil;
import com.xingyeda.lowermachine.view.layout.PercentLinearLayout;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends BaseActivity implements IVvsipServiceListener {

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
    @BindView(R.id.city_text)
    TextView cityText;
    @BindView(R.id.msg_show)
    PercentLinearLayout msgShow;
    @BindView(R.id.notification_time)
    TextView notificationTime;
    @BindView(R.id.banner_layout)
    LinearLayout bannerLayout;

    private List<String> mList = new ArrayList<>();

    private rkctrl mRkctrl = null;
    private boolean flag = true;
    private boolean mIsSocket = false;
    private SipResult sipResult = null;
    public static String userName = "";
    public static String userPwd = "";
    private boolean mIsCarousel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mRkctrl = new rkctrl();
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        MainBusiness.getSN(mContext);//获取sn
        updateTime();//时间更新

        ininImage();//图片获取

        getBindMsg();//绑定数据获取

        getInform();//获取通告

        getWeather(1000);//获取天气

        registerBoradcastReceiver();//返回监控

        setEquipmentName();

//        if (flag) {
//            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
//                initAgoraEngineAndJoinChannel();
//            }
//        }

    }

    public void getBindMsg() {
        Map<String, String> params = new HashMap<>();
        params.put("mac", MainBusiness.getMacAddress(mContext));
        OkHttp.get(ConnectPath.getPath(mContext, ConnectPath.BINDMSG_PATH), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.has("obj")) {
                        JSONObject jobj = (JSONObject) response.get("obj");

                        SharedPreUtil.put(mContext, "XiaoQuId", jobj.has("rid") ? jobj.getString("rid") : "");

                        SharedPreUtil.put(mContext, "XiaoQu", jobj.has("rname") ? jobj.getString("rname") : "");

                        SharedPreUtil.put(mContext, "QiShuId", jobj.has("nid") ? jobj.getString("nid") : "");

                        SharedPreUtil.put(mContext, "QiShu", jobj.has("nname") ? jobj.getString("nname") : "");

                        SharedPreUtil.put(mContext, "DongShuId", jobj.has("tid") ? jobj.getString("tid") : "");

                        SharedPreUtil.put(mContext, "DongShu", jobj.has("tname") ? jobj.getString("tname") : "");

                        SharedPreUtil.put(mContext, "sncode", jobj.has("sn") ? jobj.getString("sn") : "");

                        SharedPreUtil.put(mContext, "isxiaoqu", jobj.has("isxiaoqu") ? jobj.getString("isxiaoqu") : "");

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
        } else {
            if (equipmentId != null) {
                equipmentId.setText("设备未绑定");
            }
        }

    }

    private void ininImage() {
        Map<String, String> params = new HashMap<>();
        params.put("xiaoId", MainBusiness.getMacAddress(mContext));
        params.put("type", "1");
        OkHttp.get(ConnectPath.getPath(mContext, ConnectPath.IMAGE_PATH), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
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
                                mList.clear();
                                for (int i = 0; i < jan.length(); i++) {
                                    JSONObject jobjBean = jan.getJSONObject(i);
                                    if (jobjBean.has("path")) {
                                        mList.add(jobjBean.getString("path"));
                                    }
                                }
                            }
                            if (banner != null) {
                                banner.setImages(mList).setImageLoader(new GlideImageLoader()).setBannerAnimation(Transformer.Stack).setBannerStyle(BannerConfig.NOT_INDICATOR).start();
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

    private List<NotificationBean> notificationList = new ArrayList<>();
    private int notification = 1;

    private void getInform() {//通告
        final Map<String, String> params = new HashMap<>();
        params.put("eid", MainBusiness.getMacAddress(mContext));
        OkHttp.get(ConnectPath.getPath(mContext, ConnectPath.INFORM_PATH), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response.has("obj")) {
                    try {
                        JSONArray jobj = (JSONArray) response.get("obj");
                        notificationList.clear();
                        if (jobj != null && jobj.length() != 0) {
                            for (int i = 0; i < jobj.length(); i++) {
                                JSONObject jobjBean = jobj.getJSONObject(i);
                                NotificationBean bean = new NotificationBean();
                                bean.setmTime(jobjBean.has("title") ? jobjBean.getString("title") : "");
                                bean.setmContent(jobjBean.has("content") ? jobjBean.getString("content") : "");
                                bean.setmTime(jobjBean.has("sendTime") ? jobjBean.getString("sendTime") : "");
                                bean.setmDuration(jobjBean.has("duration") ? jobjBean.getString("durationtime") : "10");
                                notificationList.add(bean);
                            }
                        }
                        if (!mIsCarousel) {
                            mIsCarousel = true;
                            carouselMsg(10);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void parameterError(JSONObject response) {
                MyLog.d(response.toString());
            }

            @Override
            public void onFailure() {
                MyLog.d("超时");

            }
        }));
    }

    private void carouselMsg(int time) {//通告转动
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (notificationText != null) {
                    if (notificationList != null && !notificationList.isEmpty()) {
                        MyLog.d("list:" + notificationList.toString());
                        if (notification <= notificationList.size()) {
                            notificationText.setVisibility(View.VISIBLE);
                            notificationTime.setVisibility(View.VISIBLE);
                            notificationText.setText(notificationList.get(notification - 1).getmContent());
                            notificationTime.setText(notificationList.get(notification - 1).getmTime());
                            if (notification == notificationList.size()) {
                                notification = 1;
                            } else {
                                notification++;
                            }
                        }
                        carouselMsg(Integer.valueOf(notificationList.get(notification - 1).getmDuration()));

                    } else {
                        notificationText.setText("暂无通告");
                        notificationTime.setText("");
                        notificationText.setVisibility(View.GONE);
                        notificationTime.setVisibility(View.GONE);
                        carouselMsg(10);
                    }
                }

            }
        };
        mHandler.postDelayed(runnable, time * 1000);
    }

    public void registerBoradcastReceiver() {//广播注册
        IntentFilter intent = new IntentFilter();
        intent.addAction("HeartBeatService.RELOADIMG");//更新广告
        intent.addAction("HeartBeatService.SocketConnected");//socket连接成功
        intent.addAction("HeartBeatService.SocketIsNotConnected");//socket断开连接

        intent.addAction("HeartBeatService.HANG_UP");//手机直接挂断
        intent.addAction("HeartBeatService.REMOTE_RELEASE");//手机接通后挂断
        intent.addAction("HeartBeatService.MOBILE_ANSWER");//手机接通视频通话
        intent.addAction("HeartBeatService.MOBILE_RECEIVE");//手机收到呼入
        intent.addAction("HeartBeatService.UPDATE_DEVICE");//更新设备
        intent.addAction("HeartBeatService.REMOTE_LINSTEN_OPEN");//远程监控开启
        intent.addAction("HeartBeatService.REMOTE_LINSTEN_CLOSE");//远程监控关闭
        intent.addAction("VvsipDTMF.DTMF");//开门

        // 注册广播
        mContext.registerReceiver(mBroadcastReceiver, intent);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {//Socket广播获取
            String action = intent.getAction();
            String msg = intent.getStringExtra("msg");
            if (action.equals("HeartBeatService.RELOADIMG")) {//更新广告
                ininImage();//图片更新
                getBindMsg();
                MyLog.d("更新广告");
                getInform();//获取通告
            } else if (action.equals("HeartBeatService.SocketConnected")) {//socket连接成功
                MyLog.d("socket连接成功");
                MyLog.getInstance(mContext).delFile();
                if (!mIsSocket) {
                    mIsSocket = true;
                    if (snText != null) {
                        snText.setBackgroundResource(R.drawable.green_circle);
//                        banner.setBackgroundResource(R.mipmap.not_image);
                        bannerLayout.setBackgroundResource(R.mipmap.not_image);
                        banner.setVisibility(View.VISIBLE);
                        ininImage();//图片更新
                        getWeather(1000);//获取天气
                    }
                }
            } else if (action.equals("HeartBeatService.SocketIsNotConnected")) {//socket连接失败
                MyLog.d("socket连接失败");
                if (mIsSocket) {
                    mIsSocket = false;
                    if (snText != null) {
                        mList.clear();
                        snText.setBackgroundResource(R.drawable.red_circle);
//                        banner.setBackgroundResource(R.mipmap.network_anomaly);
                        bannerLayout.setBackgroundResource(R.mipmap.network_anomaly);
                        banner.setVisibility(View.GONE);
                    }
                }
            } else if (action.equals("HeartBeatService.HANG_UP")) {//手机直接挂断
                MyLog.d("手机直接挂断");
                ReleasePlayer();
                promptTone(R.raw.wurenjieting, false);//正忙
                clearAll();
                leaveChannel();
            } else if (action.equals("HeartBeatService.REMOTE_RELEASE")) {//手机接通后挂断
                MyLog.d("手机接通后挂断");
                ReleasePlayer();
                clearAll();
                leaveChannel();
            } else if (action.equals("HeartBeatService.MOBILE_ANSWER")) {//手机接通视频通话
                MyLog.d("手机接通视频通话");
                i++;
                if (!mIsTime) {
                    mIsTime = true;
                    setTime();
                }
//                mIsCall = false;
                ReleasePlayer();
                if (mOvertimeTimer != null) {
                    mOvertimeTimer.cancel();
                }
                connectTime(60);
            } else if (action.equals("HeartBeatService.MOBILE_RECEIVE")) {//手机收到呼入
                MyLog.d("手机收到呼入");
//                BaseUtils.showShortToast(mContext,"手机收到呼入");
                   i1++;
                if (mCallTimer != null) {
                    mCallTimer.cancel();
                }
                overtimeTimer(30);
            } else if (action.equals("HeartBeatService.UPDATE_DEVICE")) {
                MainBusiness.getVersion(mContext);
                notificationBack();
            }else if (action.equals("HeartBeatService.REMOTE_LINSTEN_OPEN")) {//监控开启
                MyLog.d("监控开启");
//                BaseUtils.showShortToast(mContext,"监控开启");
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                    initAgoraEngineAndJoinChannel();
                }
            }else if (action.equals("HeartBeatService.REMOTE_LINSTEN_CLOSE")) {//监控关闭
                if (!mIsCall) {
                    MyLog.d("监控关闭");
//                    BaseUtils.showShortToast(mContext,"监控关闭");
                    leaveChannel();
                }
            }else if (action.equals("VvsipDTMF.DTMF")) {//开门
                BaseUtils.showShortToast(mContext,"开门成功");
                MyLog.d("开门");
                 new Thread() {
                    @Override
                    public void run() {
                        mRkctrl.exec_io_cmd(6, 1);//开门
//                        promptTone(R.raw.opendoor, false);
                        try {
                            sleep(1000 * 3);
                            mRkctrl.exec_io_cmd(6, 0);//关门
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }

    };
    private void notificationBack(){
        Map<String,String> params = new HashMap<>();
        params.put("eid",MainBusiness.getMacAddress(mContext));
        params.put("parameter", AppUtils.getVersionName(mContext));
        params.put("commond", Commond.UPDATE_DEVICE);
        OkHttp.get(ConnectPath.getPath(mContext,ConnectPath.ACCEPTCOMMOND),params,new ConciseStringCallback(mContext, new ConciseCallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }));
    }

    private int i = 0;
    private int i1 = 0;

    @Override
    protected void onResume() {
        super.onResume();
        if (snText != null) {
            if (mIsSocket) {
                snText.setBackgroundResource(R.drawable.green_circle);
                bannerLayout.setBackgroundResource(R.mipmap.not_image);
                banner.setVisibility(View.VISIBLE);
            } else {
                snText.setBackgroundResource(R.drawable.red_circle);
                bannerLayout.setBackgroundResource(R.mipmap.network_anomaly);
                banner.setVisibility(View.GONE);
            }
        }
        if (msgShow != null) {
            if (SharedPreUtil.getBoolean(this, "isPortrait")) {
                msgShow.setVisibility(View.GONE);//竖屏
            } else {
                msgShow.setVisibility(View.VISIBLE);//横屏
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void updateTime() {//时间管理器
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
        mHandler.postDelayed(runnable, 1000);
    }

    private void getWeather(int time) {//星期管理和天气管理
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
                //天气管理
                OkHttp.get(ConnectPath.getPath(mContext, ConnectPath.WEATHER_PATH), new BaseStringCallback(mContext, new CallbackHandler<String>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.has("obj")) {
                            try {
                                JSONObject jobj = (JSONObject) response.get("obj");
                                getWeather(12 * 60 * 60 * 1000);
                                if (cityText != null) {
                                    cityText.setText(jobj.has("city") ? jobj.getString("city") : "");
                                    LogUtils.d(jobj.has("city") ? jobj.getString("city") : "111");
                                }
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
//                        getWeather(5000);
                    }

                    @Override
                    public void onFailure() {
                        if (weatherText != null) {
                            weatherText.append("/暂无天气");
                        }
//                        getWeather(5000);
                    }
                }));

            }
        };
        mHandler.postDelayed(runnable, time);
    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {//网络监视器
            ConnectivityManager cwjManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cwjManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                if (!flag) {
                    flag = true;
                    if (noNetwork != null) {
                        noNetwork.setVisibility(View.GONE);
                    }
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                        initAgoraEngineAndJoinChannel();
                    }
                    ininImage();
                    getBindMsg();
                    getInform();//获取通告
                    getWeather(1000);//获取天气
                }
            } else {
                if (flag) {
                    flag = false;
                    if (mRtcEngine != null) {
                        leaveChannel();
                        RtcEngine.destroy();//销毁引擎实例
                    }
                }
            }
        }
    };

    @OnClick({R.id.equipment_id, R.id.main_time, R.id.qr_code, R.id.door_number})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.qr_code:
//                callOut("8888");
                sipInit(ConnectPath.SIP_HOST);
                break;
            case R.id.door_number:
//                BaseUtils.startActivity(mContext, SetActivity.class);
                sipHangup();
                break;
            case R.id.equipment_id:
//                BaseUtils.showLongToast(mContext, "接通视频通话 : " + i + "  收到呼叫 ： " + i1);
                sipCall("18711018824");
                break;
            case R.id.main_time:
//                i = 0;
//                i1 = 0;
                callOut("1111");
                break;
        }
    }

    @BindView(R.id.door_number)
    TextView doorNumber;
    @BindView(R.id.call_timer)
    TextView callTimer;

    private String mUserId = "";
    private String mCallId = "";
    private String mHousenum = "";
    private String mPhone = "";
    private Timer mTimer;
    private Timer mFreeTimer;
    private Timer mCallTimer;
    private Timer mOvertimeTimer;
    private boolean mIsCall = false;
    private String mDoorNumber = new String();
    private MediaPlayer mMediaPlayer;


    private void callOut(String callinfo) {
        mIsCall = true;
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initAgoraEngineAndJoinChannel();
        }
        while (callinfo.length() < 4) {
            callinfo = "0" + callinfo;
        }
        if (callTimer != null) {
            callTimer.setText("呼叫中");
        }
        promptTone(R.raw.ringback, true);
        mDoorNumber = "";
        mHousenum = callinfo;
        Map<String, String> params = new HashMap<>();
        params.put("randomCode", getRandom());
        params.put("callinfo", callinfo);
        params.put("eid", MainBusiness.getMacAddress(mContext));
        params.put("block", "00");
        params.put("isxiaoqu", SharedPreUtil.getString(mContext, "isxiaoqu"));
        params.put("paizhao", "false");
        OkHttp.get(ConnectPath.getPath(mContext, ConnectPath.CALLUSER_PATH), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {//成功
                try {
                    callTime(getTimerTime(mContext));
                    mUserId = response.has("userId") ? response.getString("userId") : "";
                    mPhone = response.has("phone") ? response.getString("phone") : "";
                    mCallId = response.has("callId") ? response.getString("callId") : "";
                   mHandler.sendEmptyMessage(5);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void parameterError(JSONObject response) {//失败
                promptTone(R.raw.calltips, false);
                callTime(3);
                mIsCall = false;
                mDoorNumber = "";
                if (doorNumber != null) {
                    doorNumber.setText("");
                }
                clearAll();
                if (callTimer != null) {
                    callTimer.setText("呼叫失败");
                    clearText(5);
                }
            }

            @Override
            public void onFailure() {//接口问题
                promptTone(R.raw.calltips, false);
                callTime(3);
                mIsCall = false;
                mDoorNumber = "";
                if (doorNumber != null) {
                    doorNumber.setText("");
                }
                clearAll();
                if (callTimer != null) {
                    callTimer.setText("服务器异常，请联系管理员");
                    clearText(5);
                }
            }
        }));


    }

    private void clearText(int time) {//清理文本
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (callTimer != null) {
                    callTimer.setText("");
                }
            }
        };
        mHandler.postDelayed(runnable, time * 1000);
    }

    //挂断
    private void cancels() {
        if (callTimer != null) {
            callTimer.setText("挂断中");
        }
        Map<String, String> params = new HashMap<>();
        params.put("eid", MainBusiness.getMacAddress(mContext));
        params.put("uid", mUserId);
        params.put("housenum", mHousenum);
        params.put("dongshu", SharedPreUtil.getString(mContext, "DongShuId"));
        OkHttp.get(ConnectPath.getPath(mContext, ConnectPath.CANCEL_PATH), params, new ConciseStringCallback(mContext, new ConciseCallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {
                clearAll();
            }
        }));
    }

    //本地电话查询
    private void checkPhone(String phone) {
        Map<String, String> params = new HashMap<>();
        params.put("tel", phone);
        OkHttp.get(ConnectPath.getPath(mContext, ConnectPath.CHECKPHONE_PATH), params, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {//本地

            }

            @Override
            public void parameterError(JSONObject response) {//外地
                mPhone = "0"+mPhone;
            }

            @Override
            public void onFailure() {//出错

            }
        }));
    }


    private int mCount = 0;
    private boolean mIsTime = false;

    private void setTime() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (callTimer != null) {
                    if (mIsCall) {
                        mIsTime = true;
                        mCount += 1;
                        callTimer.setText(getStandardTime(mCount));
                        setTime();
                    } else {
                        mIsTime = false;
                        mCount = 0;
                        if (callTimer != null) {
                            callTimer.setText("");
                        }
                    }
                }
            }
        };
        mHandler.postDelayed(runnable, 1000);
    }

    private void clearAll() {
        mUserId = "";
        mCallId = "";
        mHousenum = "";
//        mPhone = "";
        mIsCall = false;
        mDoorNumber = "";
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mFreeTimer != null) {
            mFreeTimer.cancel();
        }
        if (mOvertimeTimer != null) {
            mOvertimeTimer.cancel();
        }

            if (mCallTimer != null) {
                mCallTimer.cancel();
            }

        if (!mAudioCall) {
            if (doorNumber != null) {
                doorNumber.setText("");
            }
            if (callTimer != null) {
                callTimer.setText("");
            }
        }
     }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    cancels();
                    ReleasePlayer();
                    break;
                case 1:
                    clearAll();
                    break;
                case 2:
                    sipHangup();
                    break;
                case 3:
                    if (callTimer != null) {
                        callTimer.setText("电话呼叫中");
                    }
                    break;
                case 4:
                    leaveChannel();
                    ReleasePlayer();
                    getAccount(mContext);
                    cancels();
                    phoneCall(1, "start");
                    break;
                case 5:
                    if (mPhone!=null&&!"".equals(mPhone)) {
                        checkPhone(mPhone);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //闲置多少时间处理
    private void freeTime(int time) {
        if (mFreeTimer != null) {
            mFreeTimer.cancel();
        }
        mFreeTimer = new Timer();
        mFreeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(1);
            }
        }, time * 1000);
    }
    //呼叫超时未接通
    private void overtimeTimer(int time) {
        if (mOvertimeTimer != null) {
            mOvertimeTimer.cancel();
        }
        mOvertimeTimer = new Timer();
        mOvertimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0);
            }
        }, time * 1000);
    }

    //接通计时  默认60秒----收到接通信息时调用
    private void connectTime(int time) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ReleasePlayer();
                mHandler.sendEmptyMessage(0);
            }
        }, time * 1000);
    }

    //电话呼叫计时  默认60秒
    private void callTime(int time) {
        if (mCallTimer != null) {
            mCallTimer.cancel();
        }
        mCallTimer = new Timer();
        mCallTimer.schedule(new TimerTask() {
            @Override
            public void run() {//呼叫电话
                mHandler.sendEmptyMessage(4);
            }
        }, time * 1000);
    }

    private void phoneCall(int type, String flag) {
        Map<String, String> params = new HashMap<>();
        params.put("flag", flag);//flag = start(开始调用)，fail(失败)
        params.put("uid", mUserId);
        switch (type) {
            case 1:
                params.put("callId", mCallId);
                params.put("eid", MainBusiness.getMacAddress(mContext));
                break;
            case 2:
                params.put("id", mCallId);
                break;
        }
        OkHttp.get(ConnectPath.getPath(mContext, ConnectPath.PHONECALL_PATH),params, new ConciseStringCallback(mContext, new ConciseCallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }));
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsCall) {
            if (keyCode == KeyEvent.KEYCODE_STAR) {//*
                ReleasePlayer();
                cancels();
                leaveChannel();
                return false;
            }
        } else if(mAudioCall){
            sipHangup();
        }else {
            if (mDoorNumber.length() >= 11) {
//                return false;
            } else {
                if (keyCode == KeyEvent.KEYCODE_0) {//0
                    promptTone(R.raw.free, false);
                    mDoorNumber += "0";
                    doorNumber.append("0");
                    freeTime(10);
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_1) {//1
                    promptTone(R.raw.free, false);
                    mDoorNumber += "1";
                    doorNumber.append("1");
                    freeTime(10);
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_2) {//2
                    promptTone(R.raw.free, false);
                    mDoorNumber += "2";
                    doorNumber.append("2");
                    freeTime(10);
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_3) {//3
                    promptTone(R.raw.free, false);
                    mDoorNumber += "3";
                    doorNumber.append("3");
                    freeTime(10);
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_4) {//4
                    promptTone(R.raw.free, false);
                    mDoorNumber += "4";
                    doorNumber.append("4");
                    freeTime(10);
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_5) {//5
                    promptTone(R.raw.free, false);
                    mDoorNumber += "5";
                    doorNumber.append("5");
                    freeTime(10);
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_6) {//6
                    promptTone(R.raw.free, false);
                    mDoorNumber += "6";
                    doorNumber.append("6");
                    freeTime(10);
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_7) {//7
                    promptTone(R.raw.free, false);
                    mDoorNumber += "7";
                    doorNumber.append("7");
                    freeTime(10);
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_8) {//8
                    promptTone(R.raw.free, false);
                    mDoorNumber += "8";
                    doorNumber.append("8");
                    freeTime(10);
                    return false;
                } else if (keyCode == KeyEvent.KEYCODE_9) {//9
                    promptTone(R.raw.free, false);
                    mDoorNumber += "9";
                    doorNumber.append("9");
                    freeTime(10);
                    return false;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_0) {//0
            } else if (keyCode == KeyEvent.KEYCODE_1) {//1
            } else if (keyCode == KeyEvent.KEYCODE_2) {//2
            } else if (keyCode == KeyEvent.KEYCODE_3) {//3
            } else if (keyCode == KeyEvent.KEYCODE_4) {//4
            } else if (keyCode == KeyEvent.KEYCODE_5) {//5
            } else if (keyCode == KeyEvent.KEYCODE_6) {//6
            } else if (keyCode == KeyEvent.KEYCODE_7) {//7
            } else if (keyCode == KeyEvent.KEYCODE_8) {//8
            } else if (keyCode == KeyEvent.KEYCODE_9) {//9
            } else if (keyCode == KeyEvent.KEYCODE_STAR) {//*
                promptTone(R.raw.free, false);
                if (mFreeTimer != null) {
                    mFreeTimer.cancel();
                }
                ReleasePlayer();
                clearAll();
                return false;
            } else { //#
                if (mFreeTimer != null) {
                    mFreeTimer.cancel();
                }
                if (mDoorNumber != null) {
                    if (mDoorNumber.equals("9999")) {//跳转设置
                        BaseUtils.startActivity(mContext, SetActivity.class);
                        clearAll();
                    } else if (mDoorNumber.equals("3818")) {//密码开门
                        clearAll();
                        if (!mIsSocket || !flag) {
                            new Thread() {
                                @Override
                                public void run() {
                                    mRkctrl.exec_io_cmd(6, 1);//开门
                                    promptTone(R.raw.opendoor, false);
                                    try {
                                        sleep(1000 * 3);
                                        mRkctrl.exec_io_cmd(6, 0);//关门
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    } else if (mDoorNumber.equals("3819")) {//重启设备
                        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        powerManager.reboot("");
                    } else if (mDoorNumber.equals("3820")) {//关闭设备
//                        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
//                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
                    } else if (mDoorNumber.equals("3821")) {//设备更新
                        Intent intent = new Intent();
                        intent.setAction("HeartBeatService.UPDATE_DEVICE");
                        MainActivity.this.sendBroadcast(intent);
                    } else {
                        if (flag && !mIsCall) {
                            if (!mDoorNumber.equals("")) {
                                callOut(mDoorNumber);
                            }
                        }
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private void promptTone(final int resId, boolean isCirculation) {
        // 开始播放音乐
        if (mMediaPlayer != null) {
//            mMediaPlayer.start();
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
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

    //等待呼叫电话时间
    public int getTimerTime(Context context) {
        if (SharedPreUtil.getInt(context, "timerTime") == 0) {
            return 15;
        }
        return SharedPreUtil.getInt(context, "timerTime");
    }

    private boolean mIsAgora = false;

    //初始化声网引擎和加入频道
    private void initAgoraEngineAndJoinChannel() {
        if (!mIsAgora) {
            mIsAgora = true;
            initializeAgoraEngine();     //  教程步骤 1
            setupVideoProfile();         //  教程步骤 2
            setupLocalVideo();           //  教程步骤 3
            joinChannel();               //  教程步骤 4
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("没有许可 " + Manifest.permission.RECORD_AUDIO);
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("没有许可 " + Manifest.permission.CAMERA);
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
        mContext.unregisterReceiver(networkReceiver);

        ReleasePlayer();


        if (mRtcEngine!=null) {
            leaveChannel();
            RtcEngine.destroy();//销毁引擎实例
            mRtcEngine = null;
        }
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

//        FrameLayout container = (FrameLayout) findViewById(R.id.local_video_view_container);
//        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
//        surfaceView.setZOrderMediaOverlay(!iv.isSelected());
//        surfaceView.setVisibility(iv.isSelected() ? View.GONE : View.VISIBLE);
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
//        finish();
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
        if (mRtcEngine!=null) {
            if (mIsAgora) {
                mIsAgora = false;
                mRtcEngine.leaveChannel();//离开频道
            }
        }
    }

    //  教程步骤 7  远程用户离开  （挂断）
    private void onRemoteUserLeft() {
//        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
//        container.removeAllViews();
//
//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // 可选的界面
//        tipMsg.setVisibility(View.VISIBLE);
//        leaveChannel();
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

    /*
获取SIP账号
 */
    private void getAccount(Context context) {
        mAudioCall = true;
        HttpUtils.doGet(ConnectPath.getPath(context, ConnectPath.GETACCOUNT), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                sipResult = JsonUtils.getGson().fromJson(response.body().string(), SipResult.class);
                if (sipResult.getStatus().equals("200") && sipResult.getSipValue() != null) {
                    userName = sipResult.getSipValue().getUser_name();
                    userPwd = sipResult.getSipValue().getUser_pwd();
                } else {
                    userName = ConnectPath.SIP_NAME;
                    userPwd = ConnectPath.SIP_PWD;
                }
//                Bundle bundle = new Bundle();
//                bundle.putString("mPhone", mPhone);
//                bundle.putString("userName", userName);
//                bundle.putString("userPwd", userPwd);
//                BaseUtils.startActivities(mContext, CallActivity.class, bundle);
//                finish();
                sipInit(ConnectPath.SIP_HOST);

            }
        });
    }

    private List<VvsipCall> mVvsipCalls = null;
    private boolean mAudioCall = false;
    private Timer mCalloutTimer;//电话呼叫计时器
    private Timer mTalkbackTimer;//通话计时器

    private void talkbackTimer(int time) {//通话计时
        if (mTalkbackTimer != null) {
            mTalkbackTimer.cancel();
        }
        mTalkbackTimer = new Timer();
        mTalkbackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(2);
            }
        }, time * 1000);
    }
    private void calloutTimer(int time) {//电话呼叫计时
        if (mCalloutTimer != null) {
            mCalloutTimer.cancel();
        }
        mCalloutTimer = new Timer();
        mCalloutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(2);
            }
        }, time * 1000);
    }
    private void closeCalloutTimer(){
        if (mCalloutTimer != null) {
            mCalloutTimer.cancel();
        }
        if (mTalkbackTimer != null) {
            mTalkbackTimer.cancel();
        }
    }

    /**
     * sip初始化
     */
    private void sipInit(final String sipSite){
        mHandler.sendEmptyMessage(3);
        mAudioCall = true;
        IVvsipService _service = VvsipService.getService();
        if (_service != null) {
            _service.addListener(this);
            _service.setMessageHandler(messageHandler);
            VvsipDTMF.getVvsipDTMF().setHandler(messageHandler);
        } else {
        }

        if (mVvsipCalls == null) {
            mVvsipCalls = new ArrayList<VvsipCall>();
        }


        //注册
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                IVvsipService _service = VvsipService.getService();
                if (_service != null) {
                    _service.register(sipSite, userName, userPwd);
//                    _service.register("393818.2d09f8.sip.newrocktech.com:5090", "215", "467464");
                }
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sipCall(mPhone);
            }
        }, 3 * 1000);

    }

    /**
     * sip呼叫
     * @param number 电话号码
     */
    private void sipCall(final String number){
        MyLog.d("呼叫号码 ： "+number);
        calloutTimer(10);
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                IVvsipService _service = VvsipService.getService();
                if (_service == null)
                    return;
                _service.initiateOutgoingCall(number);
            }
        });
    }

    /**
     * sip挂断
     */
    private void sipHangup(){
        if (mAudioCall) {
            mAudioCall = false;
        closeCalloutTimer();
        if (doorNumber != null) {
            doorNumber.setText("");
        }
            if (callTimer != null) {
                callTimer.setText("");
            }
        VvsipCall pCall = null;
        for (VvsipCall _pCall : mVvsipCalls) {
            if(_pCall.cid > 0)
                if (_pCall.cid > 0 && _pCall.mState <= 2) {
                    pCall = _pCall;
                    break;
                }
        }
        if (pCall == null)
            return;
        IVvsipService _service = VvsipService.getService();
        if (_service == null)
            return;
        VvsipTask _vvsipTask = _service.getVvsipTask();
        if (_vvsipTask == null)
            return;
        pCall.stop();
        _service.setSpeakerModeOff();
        _service.stopPlayer();  //主叫挂断，声音停止;理论上在Closed，Released状态中会调用这个方法的。
        //但是HDL那边没有效果，这里主动调用一次 ,By 20160822
        _service.setAudioNormalMode();
        }
    }

    @Override
    public void onNewVvsipCallEvent(final VvsipCall call) {

        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (call == null) {
                        return;
                    }

                    if (mVvsipCalls == null)
                        return;
                    mVvsipCalls.add(call);

                    if (Build.VERSION.SDK_INT >= 5) {
                        getWindow()
                                .addFlags( // WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                        // |
                                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }

                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    public void onStatusVvsipCallEvent(VvsipCall call) {
        Log.d("SIPTES","call :"+call.mState);
    }

    @Override
    public void onRemoveVvsipCallEvent(final VvsipCall call) {

        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    if (call == null) {
                        return;
                    }

                    // 使用mvvsipcall =NULL，在这里检测到4.0.9的崩溃。
                    if (mVvsipCalls == null)
                        return;
                    mVvsipCalls.remove(call);

                    if (mVvsipCalls.size() == 0) {
                        if (Build.VERSION.SDK_INT >= 5) {
                            getWindow()
                                    .clearFlags( // WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                            // |
                                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    public void onRegistrationEvent(final int rid, final String remoteUri,
                                    final int code, String reason) {

    }

    @SuppressLint("HandlerLeak")
    private Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            MyLog.d("监听值 ："+msg.obj);
            if (!msg.obj.toString().contains("CALL")) {
                if (msg.obj.toString().contains("200 OK")) {//注册成功
                    MyLog.d("监听返回 ：注册成功");
                }
            }else if(msg.obj.toString().contains("Timeout")){
                MyLog.d("监听返回 ：sip请求超时");
                BaseUtils.showShortToast(mContext,"sip请求超时");
                sipInit(ConnectPath.SIP_REMARK);//sip备用服务器的注册
            }else{
                if (msg.obj.toString().contains("200 OK")) {//呼叫接通
                    MyLog.d("监听返回 ：呼叫接通");
                    if (mCalloutTimer!=null) {
                        mCalloutTimer.cancel();
                    }
                    talkbackTimer(60);
                }else if(msg.obj.toString().contains("CLOSED")||msg.obj.toString().contains("RELEASED")){ //结束呼叫
                    mAudioCall = false;
                    closeCalloutTimer();
                    MyLog.d("监听返回 ：结束呼叫");
                    if (doorNumber != null) {
                        doorNumber.setText("");
                    }
                    if (callTimer != null) {
                        callTimer.setText("");
                    }
                    VvsipCall pCall = null;
                    for (VvsipCall _pCall : mVvsipCalls) {
                        if(_pCall.cid > 0)
                            if (_pCall.cid > 0 && _pCall.mState <= 2) {
                                pCall = _pCall;
                                break;
                            }
                    }
                    if (pCall == null)
                        return;
                    IVvsipService _service = VvsipService.getService();
                    if (_service == null)
                        return;
                    VvsipTask _vvsipTask = _service.getVvsipTask();
                    if (_vvsipTask == null)
                        return;
                    pCall.stop();
                    _service.setSpeakerModeOff();
                }
            }
        }
    };
}