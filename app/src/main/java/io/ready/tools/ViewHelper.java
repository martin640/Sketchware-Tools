package io.ready.tools;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public abstract class ViewHelper {

    private static ArrayList<View> views;

    public static void enableView(ViewGroup view, Boolean flag) throws NullPointerException {
        views = new ArrayList<>();
        if (view != null && view.getChildCount() != 0) {
            addViewGroup(view);

            for(View v : views) {
                v.setEnabled(flag);
            }
        } else {
            throw new NullPointerException("Null ViewGroup argument");
        }
    }

    public static void addViewGroup(ViewGroup viewGroup) {
        if (viewGroup != null && viewGroup.getChildCount() != 0) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child0 = viewGroup.getChildAt(i);
                views.add(child0);

                if (child0 instanceof ViewGroup) {
                    addViewGroup((ViewGroup) child0);
                }
            }
            if(!views.contains(viewGroup)) views.add(viewGroup);
        } else {
            throw new NullPointerException("Null ViewGroup argument");
        }
    }
}
