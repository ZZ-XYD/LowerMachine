package com.xingyeda.lowermachine.base;

import android.content.Context;

import com.xingyeda.lowermachine.utils.SharedPreUtil;

/*
HTTP请求接口类
 */
public class ConnectPath {

    public static final String DOMAIN = "http://";

//    public static final String HOST = "192.168.10.250";
    public static final String HOST = "120.25.245.234";


    /*
    Socket地址
     */
//    public static final String SOCKET_HOST = "192.168.10.250";
//    public static final String SOCKET_HOST = "120.25.245.234";

    public static final int SOCKET_PORT = 5888;


    /*
    SIP地址
     */
    public static final String SIP_HOST = "393818.2d09f8.sip.newrocktech.com:5090";//sip服务器
    public static final String SIP_REMARK = "393818.2d09f8.sip.newrocktech.com:5090";//sip备用服务器

    //SIP默认账号
    public static final String SIP_NAME = "215";
    //SIP默认密码
    public static final String SIP_PWD = "467464";


    //    public static final String HOST_PATH = HOST + "xydServer/servlet/";
    private static final String HOST_PATH = ":8080/xydServer/servlet/";

    //传送MAC地址添加设备
    public static final String ADDSHEBEIFORAPP = HOST_PATH + "addShebeiForApp";
    //图片地址
    public static final String IMAGE_PATH = HOST_PATH + "getScreenImgs";
    //获取绑定信息
    public static final String BINDMSG_PATH = HOST_PATH + "getshebeibymac";
    //呼叫用户
    public static final String CALLUSER_PATH = HOST_PATH + "calluser";
    //用户设置
    public static final String USERSET_PATH = HOST_PATH + "econfig";
    //刷卡
    public static final String CARD = HOST_PATH + "card";
    //挂断
    public static final String CANCEL_PATH = HOST_PATH + "cancel";
    //电话号码本地查询
    public static final String CHECKPHONE_PATH = HOST_PATH + "checkPhone";
    //获取通告
    public static final String INFORM_PATH = HOST_PATH + "getEquipmentAnnouncement";
    //天气
    public static final String WEATHER_PATH = HOST_PATH + "getTianqi";
    //呼叫电话
    public static final String PHONECALL_PATH = HOST_PATH + "phonecall";
    //查询版本
    public static final String GETSERVERVERSION = HOST_PATH + "getServerVersion";
    //获取SIP账户
    public static final String GETACCOUNT = HOST_PATH + "getAccount";
    //释放SIP账户
    public static final String RELEASEACCOUNT = HOST_PATH + "releaseAccount";
    //更新反馈
    public static final String ACCEPTCOMMOND = HOST_PATH + "acceptCommond";

    //获取地址
    public static String getPath(Context context, String url) {
        if (SharedPreUtil.getString(context, "ip").equals("")) {
            return DOMAIN + HOST + url;
        }
        return DOMAIN + SharedPreUtil.getString(context, "ip") + url;
    }

    public static String getHost(Context context) {
        if (SharedPreUtil.getString(context, "ip").equals("")) {
            return HOST;
        }
        return SharedPreUtil.getString(context, "ip");
    }


//    //传送MAC地址添加设备
//    public static String ADDSHEBEIFORAPP(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + ADDSHEBEIFORAPP;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + ADDSHEBEIFORAPP;
//    }
//
//    //图片地址
//    public static String IMAGE_PATH(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + IMAGE_PATH;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + IMAGE_PATH;
//    }
//
//    //获取绑定信息
//    public static String BINDMSG_PATH(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + BINDMSG_PATH;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + BINDMSG_PATH;
//    }
//
//    //呼叫用户
//    public static String CALLUSER_PATH(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + CALLUSER_PATH;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + CALLUSER_PATH;
//    }
//
//    //呼叫用户
//    public static String USERSET_PATH(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + USERSET_PATH;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + USERSET_PATH;
//    }
//
//    //刷卡
//    public static String CARD(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + CARD;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + CARD;
//    }
//
//    //挂断
//    public static String CANCEL_PATH(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + CANCEL_PATH;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + CANCEL_PATH;
//    }
//
//    //电话号码本地查询
//    public static String CHECKPHONE_PATH(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + CHECKPHONE_PATH;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + CHECKPHONE_PATH;
//    }
//
//    //获取通告
//    public static String INFORM_PATH(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + INFORM_PATH;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + INFORM_PATH;
//    }
//
//    //获取通告
//    public static String WEATHER_PATH(Context context) {
//        if (SharedPreUtil.getString(context, "ip").equals("")) {
//            return DOMAIN+HOST + WEATHER_PATH;
//        }
//        return DOMAIN+SharedPreUtil.getString(context, "ip") + WEATHER_PATH;
//    }

}
