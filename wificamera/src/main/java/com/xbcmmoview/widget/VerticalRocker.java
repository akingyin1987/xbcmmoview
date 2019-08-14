package com.xbcmmoview.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.xbcmmoview.R;

public class VerticalRocker extends View {
    private boolean isInit = false;
    private int mMotionX = 0;
    private int mMotionY = 0;
    private Paint paint = new Paint();
    private Bitmap resizeBmp;
    private int rocker_h = 0;
    private float rocker_value = 0.0f;
    private int rocker_w = 0;

    public VerticalRocker(Context context, AttributeSet attrs) {
        super(context, attrs);
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.live_button);
        Matrix matrix = new Matrix();
        matrix.postScale(0.6f, 0.6f);
        this.resizeBmp = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        this.rocker_h = this.resizeBmp.getHeight();
        this.rocker_w = this.resizeBmp.getWidth();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.isInit) {
            this.isInit = true;
            this.mMotionX = 0;
            this.mMotionY = (getHeight() / 2) - (this.rocker_h / 2);
        }
        canvas.drawBitmap(this.resizeBmp, (float) (this.mMotionX - 1), (float) this.mMotionY, this.paint);
        Log.e("FLY", "value: " + this.rocker_value);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getX() > ((float) ((getWidth() / 3) * 2)) || ev.getX() < 0.0f) {
            this.mMotionX = 0;
            this.mMotionY = (getHeight() / 2) - (this.rocker_h / 2);
            invalidate();
            this.rocker_value = 0.0f;
            return true;
        } else if (ev.getAction() == 1) {
            this.rocker_value = 0.0f;
            this.mMotionX = 0;
            this.mMotionY = (getHeight() / 2) - (this.rocker_h / 2);
            invalidate();
            return true;
        } else if (ev.getAction() == 0) {
            return true;
        } else {
            if (ev.getAction() != 2) {
                return super.onTouchEvent(ev);
            }
            if (ev.getY() - ((float) (this.rocker_h / 2)) < 0.0f) {
                this.mMotionX = 0;
                this.mMotionY = 0;
                if (ev.getY() > 0.0f) {
                    this.rocker_value = slideValue(ev.getY());
                } else {
                    this.rocker_value = slideValue(0.0f);
                }
                invalidate();
                return true;
            } else if (ev.getY() > ((float) (getHeight() - (this.rocker_h / 2)))) {
                this.mMotionX = 0;
                this.mMotionY = getHeight() - this.rocker_h;
                if (ev.getY() <= ((float) getHeight())) {
                    this.rocker_value = slideValue(ev.getY());
                } else {
                    this.rocker_value = slideValue((float) getHeight());
                }
                invalidate();
                return true;
            } else {
                this.mMotionX = 0;
                this.mMotionY = ((int) ev.getY()) - (this.rocker_h / 2);
                this.rocker_value = slideValue(ev.getY());
                invalidate();
                return true;
            }
        }
    }

    private float slideValue(float cur_value) {
        if (cur_value > ((float) (getHeight() / 2))) {
            return ((-(cur_value - ((float) (getHeight() / 2)))) / ((float) getHeight())) / 2.0f;
        }
        if (cur_value < ((float) (getHeight() / 2))) {
            return ((((float) (getHeight() / 2)) - cur_value) / ((float) getHeight())) / 2.0f;
        }
        if (cur_value == ((float) getHeight())) {
            return 0.0f;
        }
        return 0.0f;
    }
}
