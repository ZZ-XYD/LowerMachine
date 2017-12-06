package com.xingyeda.lowermachine.bean;

/**
 * Created by LDL on 2017/11/30.
 */

public class NotificationBean {

    private String mTitle;
    private String mContent;
    private String mTime;
    private String mDuration;


    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmDuration() {
        return mDuration;
    }

    public void setmDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    @Override
    public String toString() {
        return "NotificationBean{" +
                "mTitle='" + mTitle + '\'' +
                ", mContent='" + mContent + '\'' +
                ", mTime='" + mTime + '\'' +
                '}';
    }
}
