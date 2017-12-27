package com.xingyeda.lowermachine.business;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.bean.SNCode;
import com.xingyeda.lowermachine.bean.SipResult;
import com.xingyeda.lowermachine.utils.BaseUtils;
import com.xingyeda.lowermachine.utils.HttpUtils;
import com.xingyeda.lowermachine.utils.JsonUtils;
import com.xingyeda.lowermachine.utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
        LogUtils.d(ConnectPath.getPath(context, ConnectPath.ADDSHEBEIFORAPP)+map);
        HttpUtils.doPost(ConnectPath.getPath(context, ConnectPath.ADDSHEBEIFORAPP), map, new Callback() {
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
        return wifiInfo.getMacAddress().replaceAll(":", "");
    }

    /*
    获取版本
     */

    public static void getVersion(final Context context) {
        HttpUtils.doGet(ConnectPath.getPath(context, ConnectPath.GETSERVERVERSION), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String serverURL = "";
                String fileName = "";
                String versionNumber = "";
                String downPath = "";
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if (jsonObject.has("obj")) {
                        String obj = jsonObject.getString("obj");
                        JSONObject jsonObj = new JSONObject(obj);
                        if (jsonObj.has("ServerURL")) {
                            serverURL = jsonObj.getString("ServerURL");
                        }
                        if (jsonObj.has("files")) {
                            JSONArray jsonArray = (JSONArray) jsonObj.get("files");
                            JSONObject filesArray = (JSONObject) jsonArray.get(0);
                            if (filesArray.has("fileName")) {
                                fileName = filesArray.getString("fileName");
                            }
                        }
                        downPath = serverURL + fileName;
                        if (jsonObj.has("versionNumber")) {
                            versionNumber = jsonObj.getString("versionNumber");
                        }
                        if (Integer.valueOf(BaseUtils.getVersionCode(context)) < Integer.valueOf(versionNumber)) {
                            downloadUpdate(downPath, context);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    从服务器获取APK
    */
    public static void downloadUpdate(String downPath, final Context context) {
        final String apkPath = BaseUtils.initFile(context) + "/" + "LowerMachine.apk";
        com.lidroid.xutils.HttpUtils mHttpUtils = new com.lidroid.xutils.HttpUtils();
        mHttpUtils.download(downPath, apkPath, true, true, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                String[] command = {"chmod", "777", apkPath};
                ProcessBuilder builder = new ProcessBuilder(command);
                try {
                    builder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final File file_apk = (File) responseInfo.result;

                try {
                    Thread.sleep(1000 * 3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                BaseUtils.installApk(file_apk, context);
            }

            @Override
            public void onFailure(HttpException e, String s) {

            }
        });
    }

    /*
    释放SIP账号
     */
    public static void releaseAccount(Context context, String id) {
        Map map = new HashMap();
        map.put("user_name", id);
        HttpUtils.doPost(ConnectPath.getPath(context, ConnectPath.RELEASEACCOUNT), map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
}
