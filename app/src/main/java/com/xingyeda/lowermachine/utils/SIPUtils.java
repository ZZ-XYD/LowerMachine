package com.xingyeda.lowermachine.utils;

import android.content.Context;
import android.content.Intent;

import com.xingyeda.lowermachine.base.ConnectPath;

import org.linphone.CallOutgoingActivity;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCoreException;

public class SIPUtils {

    /*
    登录LinPhone账户
     */
    public static void syncAccount(String username, String password, String domain) {

        LinphonePreferences mPrefs = LinphonePreferences.instance();
        if (mPrefs.isFirstLaunch()) {
            mPrefs.setAutomaticallyAcceptVideoRequests(true);
//            mPrefs.setInitiateVideoCall(true);
            mPrefs.enableVideo(true);
        }
        int nbAccounts = mPrefs.getAccountCount();
        if (nbAccounts > 0) {
            String nbUsername = mPrefs.getAccountUsername(0);
            if (nbUsername != null && !nbUsername.equals(username)) {
                mPrefs.deleteAccount(0);
                saveNewAccount(username, password, domain);
            }
        } else {
            saveNewAccount(username, password, domain);
            mPrefs.firstLaunchSuccessful();
        }
    }

    /*
    存储LinPhone账户
    */
    public static void saveNewAccount(String username, String password, String domain) {
        LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setPassword(password)
                .setDisplayName("株洲兴业达科技")
                .setTransport(LinphoneAddress.TransportType.LinphoneTransportTls);

        try {
            builder.saveNewAccount();
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    /*
    呼出SIP电话
     */
    public static void callOutGoing(Context context, String number) {
        try {
            if (!LinphoneManager.getInstance().acceptCallIfIncomingPending()) {
                String to = String.format("sip:%s@%s", number, ConnectPath.SIP_HOST);
                LinphoneManager.getInstance().newOutgoingCall(to, number);

                context.startActivity(new Intent().setClass(context, CallOutgoingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } catch (LinphoneCoreException e) {
            LinphoneManager.getInstance().terminateCall();
        }
    }
}
