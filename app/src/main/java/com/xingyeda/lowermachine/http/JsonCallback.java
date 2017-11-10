package com.xingyeda.lowermachine.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.ldl.okhttp.callback.Callback;
import com.xingyeda.lowermachine.utils.BaseUtils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;


public class JsonCallback extends Callback<JSONObject> {

    private static final int SUCCEED = 1;
    private static final int PARAMETER_FAIL = 2;
    private CallbackHandler<JSONObject> mCallbackHandler;
    private Message mMessage;
    private Context mContext;
    
    
    public JsonCallback(Context context,CallbackHandler<JSONObject> handler) {
	mContext = context;
	mCallbackHandler = handler;
	mMessage = new Message();
    }
    @Override
    public void onError(Call call, Exception e,int id) {
        BaseUtils.showShortToast(mContext, "连接超时");
    }
    @Override
    public void onResponse(JSONObject response,int id) {
	try {
	    if (!response.get("status").equals("200")) {
		if (response.has("msg")) {
			BaseUtils.showShortToast(mContext,response.getString("msg"));
		}
		mMessage.what = PARAMETER_FAIL;
		mMessage.obj = response;
		mHandler.sendMessage(mMessage);
		return;
	    }
	    mMessage.what = SUCCEED;
	    mMessage.obj = response;
	    mHandler.sendMessage(mMessage);	 

	} catch (JSONException e) {
	    e.printStackTrace();
	}
    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
	public void handleMessage(Message msg) {
	    switch (msg.what) {
	    case SUCCEED:
		mCallbackHandler.onResponse((JSONObject) msg.obj);
		break;
	    case PARAMETER_FAIL:
		mCallbackHandler.parameterError((JSONObject) msg.obj);
		break;
	    }
	}
    };




	@Override
	public JSONObject parseNetworkResponse(Response response, int id) throws Exception {
		return null;
	}


}
