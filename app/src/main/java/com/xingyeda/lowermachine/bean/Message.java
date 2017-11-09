package com.xingyeda.lowermachine.bean;

public class Message {

    //机器ID
    private String mId;
    //操作类型（命令）
    private String commond;
    //需要转换的类型
    private String converType;
    //需要装还的对象
    private String content;

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getCommond() {
        return commond;
    }

    public void setCommond(String commond) {
        this.commond = commond;
    }

    public String getConverType() {
        return converType;
    }

    public void setConverType(String converType) {
        this.converType = converType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
