package com.xbcmmoview.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.akingyin.wificamera.R;


public class BackgroundLayout extends LinearLayout {
    private int mBackgroundColor;
    private float mCornerRadius;

    public BackgroundLayout(Context context) {
        super(context);
        init();
    }

    public BackgroundLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(11)
    public BackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initBackground(getContext().getResources().getColor(R.color.kprogresshud_default_color), this.mCornerRadius);
    }

    private void initBackground(int color, float cornerRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.LINEAR_GRADIENT);
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius);
        if (VERSION.SDK_INT >= 16) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    public void setCornerRadius(float radius) {
        this.mCornerRadius = (float) Helper.dpToPixel(radius, getContext());
        initBackground(this.mBackgroundColor, this.mCornerRadius);
    }

    public void setBaseColor(int color) {
        this.mBackgroundColor = color;
        initBackground(this.mBackgroundColor, this.mCornerRadius);
    }
}
