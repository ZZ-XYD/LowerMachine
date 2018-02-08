package com.xingyeda.lowermachine.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by LDL on 2018/2/8.
 */

public class CardBean extends DataSupport {
    private String mDongShuId;//栋数id
    private String mCardId;//卡id
    private String mCardType;//卡类型
    private String mCardDate;//到期时间
    private String mPhone;//电话

    public CardBean() {
    }

    public CardBean(String mDongShuId, String mCardId, String mCardType, String mCardDate, String mPhone) {
        this.mDongShuId = mDongShuId;
        this.mCardId = mCardId;
        this.mCardType = mCardType;
        this.mCardDate = mCardDate;
        this.mPhone = mPhone;
    }

    public String getmDongShuId() {
        return mDongShuId;
    }

    public void setmDongShuId(String mDongShuId) {
        this.mDongShuId = mDongShuId;
    }

    public String getmCardId() {
        return mCardId;
    }

    public void setmCardId(String mCardId) {
        this.mCardId = mCardId;
    }

    public String getmCardType() {
        return mCardType;
    }

    public void setmCardType(String mCardType) {
        this.mCardType = mCardType;
    }

    public String getmCardDate() {
        return mCardDate;
    }

    public void setmCardDate(String mCardDate) {
        this.mCardDate = mCardDate;
    }

    public String getmPhone() {
        return mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }
}
