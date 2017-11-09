package com.xingyeda.lowermachine.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class SharedPreferencesUtils {
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;
    public static final String FILENAME = "lowermachine_data";

    public SharedPreferencesUtils(Context context) {
        mSharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    /*
    存储数据
     */
    public void put(String key, Object object) {
        if (object instanceof String) {
            mEditor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            mEditor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            mEditor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            mEditor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            mEditor.putLong(key, (Long) object);
        } else {
            mEditor.putString(key, object.toString());
        }
        mEditor.commit();
    }


    /*
    获取数据
     */
    public Object get(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return mSharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return mSharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return mSharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return mSharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return mSharedPreferences.getLong(key, (Long) defaultObject);
        } else {
            return mSharedPreferences.getString(key, null);
        }
    }

    /*
    移除数据
     */
    public void remove(String key) {
        mEditor.remove(key);
        mEditor.commit();
    }

    /*
    清除所有数据
     */
    public void clear() {
        mEditor.clear();
        mEditor.commit();
    }

    /*
    查询数据
     */
    public Boolean contain(String key) {
        return mSharedPreferences.contains(key);
    }

    /*
     返回所有数据
     */
    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }
}
