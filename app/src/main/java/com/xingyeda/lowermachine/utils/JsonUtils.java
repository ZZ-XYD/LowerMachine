package com.xingyeda.lowermachine.utils;

import com.google.gson.Gson;

public class JsonUtils {

    private static Gson gson = new Gson();

    public static Gson getGson() {
        return gson;
    }
}
