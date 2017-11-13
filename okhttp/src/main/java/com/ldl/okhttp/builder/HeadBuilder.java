package com.ldl.okhttp.builder;

import com.ldl.okhttp.OkHttpUtils;
import com.ldl.okhttp.request.OtherRequest;
import com.ldl.okhttp.request.RequestCall;

/**
 * Created by zhy on 16/3/2.
 */
public class HeadBuilder extends GetBuilder {
    @Override
    public RequestCall build() {
        return new OtherRequest(null, null, OkHttpUtils.METHOD.HEAD, url, tag, params, headers, id).build();
    }
}
