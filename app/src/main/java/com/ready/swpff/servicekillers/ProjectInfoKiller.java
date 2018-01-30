package com.ready.swpff.servicekillers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ready.swpff.HeadService;

public class ProjectInfoKiller extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, HeadService.class);
        context.stopService(service);
    }
}