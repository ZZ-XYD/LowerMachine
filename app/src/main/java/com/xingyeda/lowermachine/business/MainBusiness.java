package com.xingyeda.lowermachine.business;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.xingyeda.lowermachine.base.ConnectPath;
import com.xingyeda.lowermachine.bean.SNCode;
import com.xingyeda.lowermachine.bean.SipResult;
import com.xingyeda.lowermachine.utils.AppUtils;
import com.xingyeda.lowermachine.utils.BaseUtils;
import com.xingyeda.lowermachine.utils.HttpUtils;
import com.xingyeda.lowermachine.utils.Installation;
import com.xingyeda.lowermachine.utils.JsonUtils;
import com.xingyeda.lowermachine.utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainBusiness {

    private static ProgressDialog mProgressDialog;
    private static int progress = 0;
    private static Handler mHandler = new Handler();

    /*
    获取设备SN码
     */
    public static void getSN(final Context context) {
        Map map = new HashMap();
        map.put("mac", getMacAddress(context));
        map.put("version", AppUtils.getVersionName(context));
        LogUtils.d(ConnectPath.getPath(context, ConnectPath.ADDSHEBEIFORAPP) + map);
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
//        return Installation.id(context).replaceAll("-", "");
            try {
                return loadFileAsString("/sys/class/net/wlan0/address").toUpperCase().substring(0, 17).replaceAll(":", "");
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

    private static String loadFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setTitle("正在下载");
                mProgressDialog.setMessage("请稍候...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置进度条对话框//样式（水平，旋转）
                mProgressDialog.show();
            }
        });

        final String apkPath = BaseUtils.initFile(context) + "/" + "LowerMachine.apk";
        com.lidroid.xutils.HttpUtils mHttpUtils = new com.lidroid.xutils.HttpUtils();

        mHttpUtils.download(downPath, apkPath, true, true, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                mProgressDialog.dismiss();
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
            public void onLoading(long total, long current, boolean isUploading) {
                progress = (int) (((float) current / total) * 100);
                progress = (progress > 0) ? progress : 0;
                mProgressDialog.setProgress(progress);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                mProgressDialog.dismiss();
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
