package com.xingyeda.lowermachine.business;

import android.content.Context;

import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.bean.SNCode;
import com.xingyeda.lowermachine.utils.HttpUtils;
import com.xingyeda.lowermachine.utils.JsonUtils;
import com.xingyeda.lowermachine.utils.SharedPreferencesUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainBusiness {

    /*
    获取设备SN码
     */
    public static void getSN(final Context context, String macAddress) {
        Map map = new HashMap();
        map.put("mac", macAddress);
        HttpUtils.doPost(ConnectPath.ADDSHEBEIFORAPP, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SNCode snCode = JsonUtils.getGson().fromJson(response.body().string(), SNCode.class);
                SharedPreferencesUtils preferencesUtils = new SharedPreferencesUtils(context);
                preferencesUtils.put("sncode", snCode.getObj());
            }
        });
    }
}
