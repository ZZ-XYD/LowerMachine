package com.xingyeda.lowermachine.http;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ldl.okhttp.OkHttpUtils;
import com.ldl.okhttp.callback.Callback;
import com.ldl.okhttp.callback.StringCallback;
import com.xingyeda.lowermachine.utils.BaseUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;


@SuppressWarnings({ "rawtypes", "unchecked" })
public class OkHttp {

	/**
	 * 参数get请求
	 *
	 * @param url
	 * @param callback
	 */
	public static void get(String url, Callback callback) {
			OkHttpUtils.post().url(url).build().execute(callback);
	}
	/**
	 * 参数get请求
	 *
	 * @param url
	 * @param
	 */
	public static void get(String url,Map params) {
		OkHttpUtils.post().url(url).params(params).build().execute(new StringCallback() {
			@Override
			public void onError(Call call, Exception e, int id) {

			}

			@Override
			public void onResponse(String response, int id) {

			}
		});
	}

	/**
	 * 带参数get请求
	 *
	 * @param url
	 * @param params
	 */
	public static void get(String url, Map params, Callback callback) {
			OkHttpUtils.post().url(url).params(params).build().execute(callback);
	}

	/**
	 * 不带参数get请求string
	 *
	 * @param url
	 * @param callback
	 */
	public static void get(String url, BaseStringCallback callback) {
			OkHttpUtils.post().url(url).build().execute(callback);
	}

	/**
	 * 带参数get请求string
	 *
	 * @param url
	 * @param params
	 */
	public static void get(String url, Map params, BaseStringCallback callback) {
			OkHttpUtils.post().url(url).params(params).build().execute(callback);
	}

	/**
	 * 不带参数get请求Json
	 *
	 * @param url
	 * @param callback
	 */
	public static void getJson(String url, JsonCallback callback) {
			OkHttpUtils.post().url(url).build().execute(callback);
	}

	/**
	 * 带参数get请求Json
	 *
	 * @param url
	 * @param params
	 * @param callback
	 */
	public static void getJson(String url, Map params, JsonCallback callback) {
			OkHttpUtils.post().url(url).params(params).build().execute(callback);
	}

	/**
	 * 不带参数get请求集合Object
	 *
	 * @param url
	 * @param callback
	 */
	public static void getObjects(String url, Callback<?> callback) {
			OkHttpUtils.post().url(url).build().execute(callback);
	}

	/**
	 * 带参数get请求集合Object
	 *
	 * @param url
	 * @param params
	 * @param callback
	 */
	public static void getObjects(String url, Map params, Callback<?> callback) {
			OkHttpUtils.post().url(url).params(params).build().execute(callback);
	}


	/**
	 * 不带参post提交string
	 *
	 * @param url
	 * @param obj
	 * @param callback
	 */
	public static void postString(String url, Object obj, Callback<?> callback) {
			OkHttpUtils.postString().url(url).content(new Gson().toJson(obj))
					.build().execute(callback);
	}


	/**
	 * 不带参post提交文件
	 *
	 * @param context
	 * @param url
	 * @param file
	 * @param callback
	 */
	public static void postFile(Context context, String url, File file, Callback<?> callback) {
			if (!file.exists()) {
				Toast.makeText(context, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
				return;
			}
			OkHttpUtils.postFile().url(url).file(file).build().execute(callback);
	}


	/**
	 * 不带参数的单文件上传
	 *
	 * @param context
	 * @param url
	 * @param name
	 * @param fileName
	 * @param file
	 * @param callback
	 */
	public static void uploadFile(Context context, String url, String name,
								  String fileName, File file, Callback<?> callback) {
			if (!file.exists()) {
				Toast.makeText(context, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
				return;
			}
			OkHttpUtils.post().addFile(name, fileName, file).url(url).build().execute(callback);
	}

	/**
	 * 带参数的单文件上传
	 *
	 * @param context
	 * @param url
	 * @param name
	 * @param fileName
	 * @param file
	 * @param callback
	 */
	public static void uploadFile(Context context, String url, String name,
								  String fileName, Map params, File file, Callback<?> callback) {
			if (!file.exists()) {
				Toast.makeText(context, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
				return;
			}
			OkHttpUtils.post().addFile(name, fileName, file).url(url).params(params).build().execute(callback);
	}

	/**
	 * 不带参数下载
	 *
	 * @param context
	 * @param url
	 * @param callback
	 */
	public static void downloadFile(Context context, String url, Callback<?> callback) {
			OkHttpUtils.post().url(url).build().execute(callback);
	}

	/**
	 * 带参数下载
	 *
	 * @param url
	 * @param params
	 * @param callback
	 */
	public static void downloadFile(String url, Map params, Callback<?> callback) {
			OkHttpUtils.post().url(url).params(params).build().execute(callback);
	}

}
