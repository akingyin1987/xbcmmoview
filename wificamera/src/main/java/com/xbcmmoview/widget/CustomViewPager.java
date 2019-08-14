package com.xbcmmoview.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
    private boolean isCanScroll = false;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }

    public boolean onTouchEvent(MotionEvent arg0) {
        if (this.isCanScroll) {
            return super.onTouchEvent(arg0);
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (this.isCanScroll) {
            return super.onInterceptTouchEvent(arg0);
        }
        return false;
    }
}
