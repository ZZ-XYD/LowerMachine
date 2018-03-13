package com.vvsip.ansip;

import android.content.Intent;
import android.os.Handler;

import com.xingyeda.lowermachine.base.MainApplication;
import com.xingyeda.lowermachine.utils.MyLog;

public class VvsipDTMF {
	
	private String mTag = "VvsipDTMF";
	private static VvsipDTMF mVvsipTask;
	public static int global_failure=0;
	
	private static Handler mainActivityMessageHandler;
	
	public static VvsipDTMF getVvsipDTMF()  {
		if (mVvsipTask!=null)
			return mVvsipTask;
		return new VvsipDTMF();
	}
	
	public VvsipDTMF() {
		mVvsipTask = this;
	}
	
	public int testdtmf(int dtmf){
		MyLog.d("printDTMF:"+dtmf);
		
//		String strDTMF = null;
//
//		if (mainActivityMessageHandler != null) {
//			Message m = new Message();
//			m.what = 88;
//
			if(dtmf==35){
//				strDTMF = "#";
				sendBroadcast();
			}else if(dtmf==42){
//				strDTMF = "*";
				sendBroadcast();
			}else if (dtmf>=48){
//				strDTMF = "0";
				sendBroadcast();
			}else if(dtmf>48 && dtmf <=57){
//				strDTMF = String.valueOf(dtmf-48);
			}else{
//				strDTMF = "unknown";
			}
			
			
//			m.obj = "Received DTMF: " + strDTMF + "\n";

//			mainActivityMessageHandler.sendMessage(m);
//		} else {
//			LogUtils.d("mainActivityMessageHandler==null");
//		}
		
		return 0;
	}

	public void setHandler(Handler _mainActivityEventHandler) {
		mainActivityMessageHandler = _mainActivityEventHandler;
	}
	private void sendBroadcast(){
		Intent intent = new Intent();
		intent.setAction("VvsipDTMF.DTMF");
		MainApplication.getmContext().sendBroadcast(intent);
	}
}
