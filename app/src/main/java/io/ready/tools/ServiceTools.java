package io.ready.tools;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;

public abstract class ServiceTools {

    public static boolean serviceRunning(Context context, Class<? extends Service> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }catch (Exception e) {
            return false;
        }
        return false;
    }
}
