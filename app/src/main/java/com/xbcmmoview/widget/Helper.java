package com.xbcmmoview.widget;

import android.content.Context;

public class Helper {
    private static float scale;

    public static int dpToPixel(float dp, Context context) {
        if (scale == 0.0f) {
            scale = context.getResources().getDisplayMetrics().density;
        }
        return (int) (scale * dp);
    }
}
