package com.xingyeda.lowermachine.bean;

import com.google.gson.annotations.SerializedName;

public class SipResult {
    @SerializedName("obj")
    private SipValue sipValue;
    private String status;
    private String msg;

    public SipValue getSipValue() {
        return sipValue;
    }

    public void setSipValue(SipValue sipValue) {
        this.sipValue = sipValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public class SipValue {
        private String id;
        private String user_name;
        private String user_pwd;
        private String state;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUser_name() {
            return user_name;
        }

        public void setUser_name(String user_name) {
            this.user_name = user_name;
        }

        public String getUser_pwd() {
            return user_pwd;
        }

        public void setUser_pwd(String user_pwd) {
            this.user_pwd = user_pwd;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
