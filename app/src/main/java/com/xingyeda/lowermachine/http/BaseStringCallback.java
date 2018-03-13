package com.xingyeda.lowermachine.http;

import android.content.Context;

import com.ldl.okhttp.callback.StringCallback;
import com.xingyeda.lowermachine.utils.BaseUtils;
import com.xingyeda.lowermachine.utils.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;


public class BaseStringCallback extends StringCallback {

    private CallbackHandler<String> mCallbackHandler;
    private Context mContext;
    
    
    public BaseStringCallback(Context context, CallbackHandler<String> handler) {
	mContext = context;
	mCallbackHandler = handler;
    }
    @Override
    public void onError(Call call, Exception e,int id) {
//        BaseUtils.showShortToast(mContext, "连接超时");
		MyLog.i("连接超时");
		mCallbackHandler.onFailure();
//        mHandler.sendEmptyMessage(TIMEOUT);
    }
    @Override
    public void onResponse(String response,int id) {
		MyLog.i("返回数据："+response);
		if (response!=null) {

	try {
	    JSONObject jobj= new JSONObject(response);
	    if (!jobj.get("status").equals("200")) {
		if (jobj.has("msg")) {
//			BaseUtils.showShortToast(mContext, jobj.getString("msg"));
		}
			mCallbackHandler.parameterError(jobj);
		return;
	    }
		mCallbackHandler.onResponse(jobj);

	} catch (JSONException e) {
	    e.printStackTrace();
	}

		}
    }
}
