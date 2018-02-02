package io.ready.tools;

import android.app.Activity;
import android.content.pm.PackageManager;

public abstract class AppManagerTools {

    public static Boolean isAppInstalled(CharSequence package_, Activity context) {
        PackageManager pm = context.getPackageManager();
        if(package_ != null && package_.length() != 0) {
            try {
                pm.getPackageInfo(package_.toString() ,PackageManager.GET_ACTIVITIES);
                return true;
            }
            catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        } else return false;
    }
}
