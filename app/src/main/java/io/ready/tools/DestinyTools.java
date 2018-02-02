package io.ready.tools;

import android.app.Activity;
import android.content.res.TypedArray;

public abstract class DestinyTools {

    public static Boolean isWideScreen(int height, int width) {
        int absolute = Math.round((float) Math.floor(height / width));

        if (absolute >= 2) {
            return true;
        } else {
            return false;
        }
    }

    public static int getSystemBarsHeight(Activity activity) {
        int statusBarHeight = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }

        // action bar height
        int actionBarHeight = 0;
        final TypedArray styledAttributes = activity.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize}
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        // navigation bar height
        int navigationBarHeight = 0;
        int resourceId2 = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }

        return statusBarHeight + navigationBarHeight;
    }
}
