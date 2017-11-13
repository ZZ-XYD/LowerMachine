package com.xingyeda.lowermachine.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

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

public class MainActivity extends BaseActivity {

    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.main_time)
    TextView time;
    @BindView(R.id.equipment_id)
    TextView equipmentId;
    @BindView(R.id.sn_text)
    TextView snText;
    private List<String> mList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        MainBusiness.getSN(mContext);

        updateTime();//时间更新

        ininImage();//图片获取

        getBindMsg();//绑定数据获取

        setEquipmentName();
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

    private void updateTime() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd \n  HH:mm:ss");
                String str = sdf.format(new Date());
                if (time != null) {
                    time.setText(str + "\n" + weekDays[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
                }
                updateTime();

            }
        };
        new Handler().postDelayed(runnable, 1000);
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
}