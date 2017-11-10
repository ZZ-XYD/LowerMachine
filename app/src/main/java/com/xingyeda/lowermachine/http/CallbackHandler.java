package com.xingyeda.lowermachine.http;

import org.json.JSONObject;

public abstract class CallbackHandler<T> {
    
//    public abstract T parseNetworkResponse(Response response) throws Exception;
//
//    public abstract void onError(Call call, Exception e);

	public abstract void onResponse(JSONObject response);
	
	public abstract void parameterError(JSONObject response);
    public abstract void onFailure();

    

}
