package com.xingyeda.lowermachine.http;

import org.json.JSONObject;

public abstract class ConciseCallbackHandler<T> {
	public abstract void onResponse(JSONObject response);
}
