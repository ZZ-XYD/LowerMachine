package com.xingyeda.lowermachine.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetActivity extends BaseActivity {

    @BindView(R.id.ip_address)
    TextView ipAddress;
    @BindView(R.id.is_cell_gate)
    Switch isCellGate;
    @BindView(R.id.is_wait_time)
    Switch isWaitTime;
    @BindView(R.id.is_test)
    Switch isTest;
    @BindView(R.id.is_elevator)
    Switch isElevator;
    private boolean mIsCellGate;
    private boolean mIsWaitTime;
    private boolean mIsTest;
    private boolean mIsElevator;
    private String mHostIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        ButterKnife.bind(this);

        mHostIp = "http://192.168.10.250:8080/";

        mIsCellGate = SharedPreUtil.getBoolean(mContext,"isCellGate");
        mIsWaitTime = SharedPreUtil.getBoolean(mContext,"isWaitTime");
        mIsTest = SharedPreUtil.getBoolean(mContext,"isTest");
        mIsElevator = SharedPreUtil.getBoolean(mContext,"isElevator");

        isCellGate.setChecked(mIsCellGate);
        isWaitTime.setChecked(mIsWaitTime);
        isTest.setChecked(mIsTest);
        isElevator.setChecked(mIsElevator);

        isCellGate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsCellGate = b;
            }
        });
        isWaitTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsWaitTime = b;
            }
        });
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

    }


    @OnClick({R.id.ip_address, R.id.is_cell_gate, R.id.is_wait_time, R.id.is_test, R.id.is_elevator,R.id.set_text})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ip_address:
                break;
            case R.id.set_text:
                setSubmit();
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
    private void setSubmit(){
        Map<String,String> params = new HashMap<>();
        params.put("hostIp",mHostIp);
        params.put("isCellGate",mIsCellGate+"");
        params.put("isWaitTime",mIsWaitTime+"");
        params.put("isTest",mIsTest+"");
        params.put("isElevator",mIsElevator+"");
        String path = ConnectPath.USERSET_PATH(mContext)+"?"+getHrefByMap(params)+"&eid="+MainBusiness.getMacAddress(mContext)+"&type=add";
        OkHttp.get(path,new BaseStringCallback(mContext, new CallbackHandler<String>() {
            @Override
            public void onResponse(JSONObject response) {
                SharedPreUtil.put(mContext,"ip",mHostIp);
                SharedPreUtil.put(mContext,"isCellGate",mIsCellGate);
                SharedPreUtil.put(mContext,"isWaitTime",mIsWaitTime);
                SharedPreUtil.put(mContext,"isTest",mIsTest);
                SharedPreUtil.put(mContext,"isElevator",mIsElevator);
                BaseUtils.showShortToast(mContext,"设置成功");
            }

            @Override
            public void parameterError(JSONObject response) {

            }

            @Override
            public void onFailure() {

            }
        }));
    }
}
