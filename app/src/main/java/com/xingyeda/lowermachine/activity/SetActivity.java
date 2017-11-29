package com.xingyeda.lowermachine.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.xingyeda.lowermachine.R;
import com.xingyeda.lowermachine.base.BaseActivity;
import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.business.MainBusiness;
import com.xingyeda.lowermachine.http.BaseStringCallback;
import com.xingyeda.lowermachine.http.CallbackHandler;
import com.xingyeda.lowermachine.http.OkHttp;
import com.xingyeda.lowermachine.utils.BaseUtils;
import com.xingyeda.lowermachine.utils.SharedPreUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetActivity extends BaseActivity {

    @BindView(R.id.is_cell_gate)
    Switch isCellGate;
    @BindView(R.id.is_test)
    Switch isTest;
    @BindView(R.id.is_elevator)
    Switch isElevator;
    @BindView(R.id.mac_address)
    TextView macAddress;
    @BindView(R.id.ip_address)
    TextView setIp;
    @BindView(R.id.set_time_text)
    TextView setTime;
    @BindView(R.id.is_portrait)
    Switch isPortrait;
    private boolean mIsCellGate;
    private boolean mIsTest;
    private boolean mIsElevator;
    private boolean mIsPortrait;
    private String mHostIp = "";
    private int mTimerTime;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        ButterKnife.bind(this);

        //初始化音频管理器
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        macAddress.setText(MainBusiness.getMacAddress(mContext));

//        mHostIp = "http://192.168.10.250:8080/";

        setIp.setText(ConnectPath.getHost(mContext));
        mIsCellGate = SharedPreUtil.getBoolean(mContext, "isCellGate");
        mIsTest = SharedPreUtil.getBoolean(mContext, "isTest");
        mIsElevator = SharedPreUtil.getBoolean(mContext, "isElevator");
        mIsPortrait = SharedPreUtil.getBoolean(mContext, "isPortrait");
        mTimerTime = SharedPreUtil.getInt(mContext, "timerTime");
        setTime.setText(mTimerTime + "s");

        isCellGate.setChecked(mIsCellGate);
        isTest.setChecked(mIsTest);
        isElevator.setChecked(mIsElevator);
        isPortrait.setChecked(mIsPortrait);

        isCellGate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsCellGate = b;
            }
        });
        ;
        isTest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsTest = b;
            }
        });
        isElevator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsElevator = b;
            }
        });
        isPortrait.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsPortrait = b;
            }
        });

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_0) {//0
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_1) {//1
                if (isCellGate.isChecked()) {
                    mIsCellGate = false;
                }else{
                    mIsCellGate = true;
                }
                isCellGate.setChecked(mIsCellGate);
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_2) {//2
                if (isTest.isChecked()) {
                    mIsTest = false;
                }else{
                    mIsTest = true;
                }
                isTest.setChecked(mIsTest);
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_3) {//3
                if (isElevator.isChecked()) {
                    mIsElevator = false;
                }else{
                    mIsElevator = true;
                }
                isElevator.setChecked(mIsElevator);
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_4) {//4
                if (isPortrait.isChecked()) {
                    mIsPortrait = false;
                }else{
                    mIsPortrait = true;
                }
                isPortrait.setChecked(mIsPortrait);
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_5) {//5
                //减少音量
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FX_FOCUS_NAVIGATION_UP);
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_6) {//6
                //增加音量
                mAudioManager.adjustStreamVolume (AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,AudioManager.FX_FOCUS_NAVIGATION_UP);
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_7) {//7
                if (mTimerTime >= 5) {
                    if (setTime!=null) {
                        mTimerTime-=5;
                        setTime.setText(mTimerTime + "s");
                    }
                }
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_8) {//8
                if (mTimerTime < 120) {
                    if (setTime!=null) {
                        mTimerTime+=5;
                        setTime.setText(mTimerTime + "s");
                    }
                }

                return false;
            } else if (keyCode == KeyEvent.KEYCODE_9) {//9
                return false;
            } else if (keyCode == KeyEvent.KEYCODE_STAR) {//*
                finish();
                return false;
            } else { //#
                setSubmit();
            }
        return super.onKeyDown(keyCode, event);
    }


    @OnClick({R.id.is_cell_gate, R.id.is_test, R.id.is_elevator, R.id.set_save, R.id.set_ip, R.id.set_time})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.set_save:
                setSubmit();
                break;
            case R.id.set_ip:
                getDialog(1);
                break;
            case R.id.set_time:
                getDialog(2);
                break;
        }
    }


    public String getHrefByMap(Map<String, String> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        String href = "";
        for (Object str : map.keySet()) {
            href += "key=" + str + "&value=" + map.get(str) + "&";
        }
        if (href.length() > 0) {
            href = href.substring(0, href.length() - 1);
        }
        return href;
    }

    private void setSubmit() {
        Map<String, String> params = new HashMap<>();
        params.put("hostIp", mHostIp);
        params.put("isWaitTime", mTimerTime+"");
        params.put("isCellGate", mIsCellGate + "");
        params.put("isTest", mIsTest + "");
        params.put("isElevator", mIsElevator + "");
        params.put("isPortrait", mIsPortrait + "");
        String path = ConnectPath.getPath(mContext, ConnectPath.USERSET_PATH) + "?" + getHrefByMap(params) + "&eid=" + MainBusiness.getMacAddress(mContext) + "&type=add";
        OkHttp.get(path, new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {
                SharedPreUtil.put(mContext, "ip", mHostIp);
                SharedPreUtil.put(mContext, "timerTime", mTimerTime);
                SharedPreUtil.put(mContext, "isCellGate", mIsCellGate);
                SharedPreUtil.put(mContext, "isTest", mIsTest);
                SharedPreUtil.put(mContext, "isElevator", mIsElevator);
                SharedPreUtil.put(mContext, "isPortrait", mIsPortrait);
                BaseUtils.showShortToast(mContext, "设置成功");
                finish();
            }

            @Override
            public void parameterError(JSONObject response) {

            }

            @Override
            public void onFailure() {

            }
        }));
    }


    private void getDialog(final int type) {
        final EditText et = new EditText(this);
        String title = "";
        switch (type) {
            case 1:
                title = "设置新的ip地址";
                break;
            case 2:
                title = "设置等待呼叫电话时间";
                break;
        }
        new AlertDialog.Builder(this).setTitle(title)
//                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            BaseUtils.showShortToast(mContext, "设置内容不能为空！");
                        } else {
                            switch (type) {
                                case 1:
                                    if (isIp(input)) {
                                        mHostIp = input;
                                        if (setIp != null) {
                                            setIp.setText(mHostIp);
                                        }
                                    } else {
                                        BaseUtils.showShortToast(mContext, "设置内容不是ip地址，请重新输入");
                                    }
                                    break;
                                case 2:
                                    if (isNumeric(input)) {
                                        mTimerTime = Integer.valueOf(input);
                                        if (setTime != null) {
                                            setTime.setText(mTimerTime + "s");
                                        }
                                    } else {
                                        BaseUtils.showShortToast(mContext, "输入的时间不真确，请输入时间（秒）");
                                    }
                                    break;
                            }
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();


    }


    public static boolean isIp(String ip) {//判断是否是一个IP
        boolean b = false;
//        ip = trimSpaces(ip);//去掉空格
        if (ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = ip.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }
        return b;
    }

    public static String trimSpaces(String ip) {//去掉IP字符串前后所有的空格
        while (ip.startsWith(" ")) {
            ip = ip.substring(1, ip.length()).trim();
        }
        while (ip.endsWith(" ")) {
            ip = ip.substring(0, ip.length() - 1).trim();
        }
        return ip;
    }

    //数字的判断
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


}
