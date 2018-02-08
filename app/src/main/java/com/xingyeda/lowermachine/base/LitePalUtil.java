package com.xingyeda.lowermachine.base;

import android.content.Context;

import com.xingyeda.lowermachine.bean.CardBean;
import com.xingyeda.lowermachine.service.DoorService;
import com.xingyeda.lowermachine.utils.BaseUtils;
import com.xingyeda.lowermachine.utils.SharedPreUtil;

import org.apache.http.impl.cookie.DateUtils;
import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

/**
 * Created by LDL on 2018/2/8.
 */

public class LitePalUtil {
    /**
     * 查询卡号是否存在
     * @return
     */
    public static List<CardBean> getList(String cardId){
        List<CardBean> list = DataSupport.where("mDongShuId = ? and mCardId = ?", SharedPreUtil.getString(MainApplication.getmContext(), "DongShuId"),cardId).find(CardBean.class);
        if (list != null && !list.isEmpty()) {
            return list;
//            List<CardBean> listNow = new ArrayList<>();
//            for (CardBean bean : list) {
//                if (getDate(bean.getmCardDate())==1) {
//                    listNow.add(bean);
//                }
//            }
//            if (listNow != null && !listNow.isEmpty()) {
//                return listNow;
//            }else{
//                return null;
//            }
        }else{
            return null;
        }
    }
    public static int getDate(String strDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        try {
            date1 = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date now = new Date();

        if(date1.after(new Date())) {
           return 1;
        } else {
           return 0;
        }
    }

//    public static int compare_date(String DATE1, String DATE2) {
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            Date dt1 = df.parse(DATE1);
//            Date dt2 = df.parse(DATE2);
//            if (dt1.getTime() > dt2.getTime()) {//dt1 在dt2后
//                return 1;
//            } else if (dt1.getTime() < dt2.getTime()) {//dt1在dt2前
//                return -1;
//            } else {
//                return 0;
//            }
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
//        return 0;
//    }
}
