package com.xingyeda.lowermachine.base;

/*
服务器命令
 */

public class Commond {

    //远程开门
    public static String REMOTE_OPEN = "M001";

    //APP远程监控开启
    public static String REMOTE_LINSTEN_OPEN = "M007";
    //APP远程监控关闭
    public static String REMOTE_LINSTEN_CLOSE = "M008";

    //远程接通物业
    public static String REMOTE_MANAGER = "M137";

    //远程推送配置
    public static String REMOTE_CONFIG = "M004";

    //APP挂断
    public static String REMOTE_RELEASE = "M009";

    //手机接通视频通话
    public static String MOBILE_ANSWER = "M555";

    //手机收到呼入
    public static String MOBILE_RECEIVE = "M918";

    //物业接到了呼入
    public static String WUYE_RECEIVE = "M486";

    //重启
    public static String PC_RESTART = "M9527";

    //推流重启
    public static String PUBLISH_RESTART = "M886";

    //无应答
    public static String NO_ANSWER = "M333";

    //用户通话中
    public static String BUSY = "M147";

    //更新广告机
    public static String RELOADIMG = "M148";

    //普通呼叫
    public static String CALL_USER = "M1233";

    //呼叫管理员
    public static String CALL_MANAGER = "M2311";

    //刷卡
    public static String CARD = "M574";

    public static String DELETE_USER = "M579";

    //挂断
    public static String HANG_UP = "M5744";

    //拨打轻码云电话
    public static String QMY = "M110";

    //添加设备成功
    public static String Add_SUCCESS = "M911";

    //更新设备
    public static String UPDATE_DEVICE = "ME41";

}
