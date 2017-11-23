package com.xingyeda.lowermachine.business;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

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
    public static void getSN(final Context context) {
        Map map = new HashMap();
        map.put("mac", getMacAddress(context));
        HttpUtils.doPost(ConnectPath.getPath(context,ConnectPath.ADDSHEBEIFORAPP), map, new Callback() {
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

    /*
    *获取mac地址
    */
    public static String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress().replaceAll(":","");
    }


}
