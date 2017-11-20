package com.xingyeda.lowermachine.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xingyeda.lowermachine.activity.MainActivity;
import com.xingyeda.lowermachine.service.DoorService;
import com.xingyeda.lowermachine.service.HeartBeatService;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("HeartBeatService.onDestroy")) {
            Intent serviceIntent1 = new Intent();
            serviceIntent1.setClass(context, HeartBeatService.class);
            context.startService(serviceIntent1);
        } else if (intent.getAction().equals("DoorService.onDestroy")) {
            Intent serviceIntent2 = new Intent();
            serviceIntent2.setClass(context, DoorService.class);
            context.startService(serviceIntent2);
        } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent intent1 = new Intent();
            intent1.setClass(context, MainActivity.class);
            context.startActivity(intent1);
        }
    }
}
