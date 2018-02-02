package io.ready.tools;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.ready.swpff.R;

public class ThemeTools {

    public static void loadTheme(int theme, Activity activity) {
        if (theme == 0) {
            activity.setTheme(R.style.AppTheme);
        } else if (theme == 1) {
            activity.setTheme(R.style.AppTheme_Dark);
        } else if (theme == 2) {
            activity.setTheme(R.style.AppTheme_Amoled);
        }
    }

    public static void finishTheme(int theme, View root) {
        if (theme == 0) {
            //
        } else if (theme == 1) {
            //
        } else if (theme == 2) {
            root.setBackgroundColor(Color.BLACK);
        }
    }
}
