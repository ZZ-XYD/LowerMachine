package com.xingyeda.lowermachine.base;

/*
HTTP请求接口类
 */
public class ConnectPath {

//    public static String HOST = "http://service.xyd999.com:8080/";
//    public static final String HOST = "http://192.168.10.200:8080/";
    public static final String HOST = "http://192.168.10.250:8080/";

    public static final String HOST_PATH = HOST + "xydServer/servlet/";

    //传送MAC地址添加设备
    public static final String ADDSHEBEIFORAPP = HOST_PATH + "addShebeiForApp";
}
