package io.ready.tools;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static String getServiceName(Class<? extends Service> service) {
        return service.getName();
    }

    public static String getServiceModifiers(Class<? extends Service> service) {
        return Modifier.toString(service.getModifiers());
    }

    public static List<String> getServiceAnnotations(Class<? extends Service> service) {
        ArrayList<String> n = new ArrayList<>();
        for(Annotation annotation : service.getAnnotations()) {
            n.add(annotation.getClass().getName());
        }
        return n;
    }

    public static List<String> getServiceClasses(Class<? extends Service> service) {
        ArrayList<String> n = new ArrayList<>();
        for(Class c : service.getClasses()) {
            n.add(c.getClass().getName());
        }
        return n;
    }
}
