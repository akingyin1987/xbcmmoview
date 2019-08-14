package com.xbcmmoview.widget;

import android.annotation.SuppressLint;
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

import com.akingyin.wificamera.R;
import com.xbcmmoview.application.WingedCamApplication;

public class FourAxisRocker extends View {
    private int F_MAX = 166;
    private int F_MIN = 90;
    private int H_MID;
    private int V_MID;
    private float centreX = 0.0f;
    private float centreY = 0.0f;
    private byte[] channel = new byte[2];
    private byte channel_1;
    private byte channel_2;
    private DIRECTION direction;
    private boolean isInit = false;
    private left_rocker_listeren listern = null;
    private float mMotionX = 0.0f;
    private float mMotionY = 0.0f;
    private Paint paint = new Paint();
    private float r = 0.0f;
    private double rad = 0.0d;
    private Bitmap resizeBmp;
    private float rocker_h = 0.0f;
    private float rocker_w = 0.0f;
    private int rokcer_type = 0;

    public enum DIRECTION {
        INVALID,
        CENTRE,
        LEFT,
        RIGHT,
        UP,
        DOWN,
        LEFT_UP,
        LEFT_DOWN,
        RIGHT_UP,
        RIGHT_DOWN
    }

    public interface left_rocker_listeren {
        void left_rocker_listeren_(byte b, byte b2);
    }

    public void set_listern(left_rocker_listeren m_listern) {
        this.listern = m_listern;
    }

    public FourAxisRocker(Context context, AttributeSet attrs) {
        super(context, attrs);
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rocker);
        Log.e("imageview", "X: " + this.mMotionX + ", Y: " + this.mMotionY);
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f);
        this.resizeBmp = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        this.rocker_h = (float) (this.resizeBmp.getHeight() / 2);
        this.rocker_w = (float) (this.resizeBmp.getWidth() / 2);
    }

    private void channel_rate(float x, float y) {
        this.H_MID = WingedCamApplication.getH_mid();
        this.V_MID = WingedCamApplication.getV_mid();
        this.F_MIN = WingedCamApplication.getF_min();
        this.F_MAX = WingedCamApplication.getF_max();
        Log.e("weitiao", "x:" + x + "  y:" + y);
        if (x > getRange() && ((double) x) < 0.5d) {
            this.channel_1 = (byte) ((int) (((double) this.H_MID) - ((((0.5d - ((double) x)) / 0.5d) * ((double) (this.H_MID - this.F_MIN))) * 1.2658228d)));
        } else if (((double) x) > 0.5d && x < 1.0f - getRange()) {
            this.channel_1 = (byte) ((int) (((((double) (this.F_MAX - this.H_MID)) * ((((double) x) - 0.5d) / 0.5d)) * 1.2658228d) + ((double) this.H_MID)));
        } else if (x >= 1.0f - getRange()) {
            this.channel_1 = (byte) this.F_MAX;
        } else if (x <= getRange()) {
            this.channel_1 = (byte) this.F_MIN;
        } else {
            Log.e("weitiao", "channel_1——5");
            this.channel_1 = (byte) this.H_MID;
        }
        if (y > getRange() && ((double) y) < 0.5d) {
            this.channel_2 = (byte) ((int) (((double) this.V_MID) - ((((0.5d - ((double) y)) / 0.5d) * ((double) (this.V_MID - this.F_MIN))) * 1.2658228d)));
        } else if (((double) y) > 0.5d && y < 1.0f - getRange()) {
            this.channel_2 = (byte) ((int) (((((double) (this.F_MAX - this.V_MID)) * ((((double) y) - 0.5d) / 0.5d)) * 1.2658228d) + ((double) this.V_MID)));
        } else if (((double) y) == 0.5d) {
            this.channel_2 = (byte) this.V_MID;
        } else if (y <= getRange()) {
            this.channel_2 = (byte) this.F_MIN;
        } else {
            this.channel_2 = (byte) this.F_MAX;
        }
        if (this.listern != null) {
            this.listern.left_rocker_listeren_(this.channel_2, this.channel_1);
        }
        this.channel[0] = this.channel_1;
        this.channel[1] = this.channel_2;
    }

    @SuppressLint({"NewApi"})
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("onDraw", "onDraw: mMotionX：" + this.mMotionX + " mMotionY:" + this.mMotionY);
        if (!this.isInit) {
            this.isInit = true;
            this.mMotionX = (float) (getHeight() / 2);
            this.mMotionY = (float) (getWidth() / 2);
            this.centreX = this.mMotionX;
            this.centreY = this.mMotionY;
            this.r = this.centreX;
        }
        this.rad = getRad(this.centreX, this.centreY, this.mMotionX, this.mMotionY);
        if (pointDistance(this.centreX, this.centreY, this.mMotionX, this.mMotionY) > this.r - this.rocker_h) {
            getXY(this.centreX, this.centreY, this.r - this.rocker_h, this.rad);
        }
        canvas.drawBitmap(this.resizeBmp, this.mMotionX - this.rocker_h, this.mMotionY - this.rocker_w, this.paint);
        Log.e("imageview", "r: " + this.r + ", rocker_w: " + this.rocker_w + "(r/(r-rocker_w)):" + (this.r / (this.r - this.rocker_w)));
        channel_rate(this.mMotionX / (this.centreX * 2.0f), ((this.centreY * 2.0f) - this.mMotionY) / (this.centreY * 2.0f));
    }

    public boolean onTouchEvent(MotionEvent ev) {
        this.rad = getRad(this.centreX, this.centreY, ev.getX(), ev.getY());
        float dst = pointDistance(this.centreX, this.centreY, ev.getX(), ev.getY());
        if (ev.getAction() == 0) {
            return true;
        }
        if (ev.getAction() == 2) {
            if (dst > this.r - this.rocker_h) {
                getXY(this.centreX, this.centreY, this.r - this.rocker_h, this.rad);
            } else {
                this.mMotionX = (float) ((int) ev.getX());
                this.mMotionY = (float) ((int) ev.getY());
            }
            invalidate();
            this.direction = moveDriection(this.mMotionX, this.mMotionY, this.rad);
            return true;
        } else if (ev.getAction() != 1) {
            return super.onTouchEvent(ev);
        } else {
            this.mMotionX = (float) (getHeight() / 2);
            this.mMotionY = (float) (getWidth() / 2);
            invalidate();
            this.rad = 0.0d;
            this.direction = moveDriection(this.mMotionX, this.mMotionY, this.rad);
            return true;
        }
    }

    private DIRECTION moveDriection(float x, float y, double rad) {
        if (rad < 0.0d) {
            rad = -rad;
        }
        if (this.rokcer_type == 0) {
            return rocker_four(x, y, rad);
        }
        return rocker_eight(x, y, rad);
    }

    private DIRECTION rocker_four(float x, float y, double rad) {
        if (y == this.centreY && x == this.centreX && rad == 0.0d) {
            return DIRECTION.CENTRE;
        }
        if (x < this.centreX && rad >= 2.25d && rad <= 3.33d) {
            return DIRECTION.LEFT;
        }
        if (x > this.centreX && rad >= 0.0d && rad <= 0.75d) {
            return DIRECTION.RIGHT;
        }
        if (y < this.centreY && rad >= 0.75d && rad <= 2.25d) {
            return DIRECTION.UP;
        }
        if (y <= this.centreY || rad < 0.75d || rad > 2.25d) {
            return DIRECTION.INVALID;
        }
        return DIRECTION.DOWN;
    }

    private DIRECTION rocker_eight(float x, float y, double rad) {
        if (y == this.centreY && x == this.centreX && rad == 0.0d) {
            return DIRECTION.CENTRE;
        }
        if (x > this.centreX && rad <= 0.375d && rad >= 0.0d) {
            return DIRECTION.RIGHT;
        }
        if (x > this.centreX && y < this.centreY && rad >= 0.375d && rad <= 1.125d) {
            return DIRECTION.RIGHT_UP;
        }
        if (x > this.centreX && y > this.centreY && rad >= 0.375d && rad <= 1.125d) {
            return DIRECTION.RIGHT_DOWN;
        }
        if (y < this.centreY && rad >= 1.125d && rad <= 1.875d) {
            return DIRECTION.UP;
        }
        if (y < this.centreY && x < this.centreX && rad >= 1.875d && rad <= 2.625d) {
            return DIRECTION.LEFT_UP;
        }
        if (x < this.centreX && rad >= 2.625d && rad <= 3.333d) {
            return DIRECTION.LEFT;
        }
        if (x < this.centreX && y > this.centreY && rad >= 1.875d && rad <= 2.625d) {
            return DIRECTION.LEFT_DOWN;
        }
        if (y <= this.centreY || rad > 1.875d || rad < 1.125d) {
            return DIRECTION.INVALID;
        }
        return DIRECTION.DOWN;
    }

    private float pointDistance(float px1, float py1, float px2, float py2) {
        return (float) Math.sqrt(Math.pow((double) (px2 - px1), 2.0d) + Math.pow((double) (py1 - py2), 2.0d));
    }

    public double getRad(float px1, float py1, float px2, float py2) {
        float x = px2 - px1;
        float rad = (float) Math.acos((double) (x / ((float) Math.sqrt(Math.pow((double) x, 2.0d) + Math.pow((double) (py1 - py2), 2.0d)))));
        if (py2 < py1) {
            rad = -rad;
        }
        return (double) rad;
    }

    public void getXY(float centerX, float centerY, float R, double rad) {
        this.mMotionX = (float) ((int) ((((double) R) * Math.cos(rad)) + ((double) centerX)));
        this.mMotionY = (float) ((int) ((((double) R) * Math.sin(rad)) + ((double) centerY)));
    }

    public void setXY(float centerX, float centerY) {
        this.mMotionX = centerX;
        this.mMotionY = centerY;
        postInvalidate();
    }

    public void setCenter() {
        this.mMotionX = this.centreX;
        this.mMotionY = this.centreY;
        postInvalidate();
    }

    public float getR() {
        return this.r;
    }

    private float getRange() {
        Log.e("imageview", "getRange: " + ((this.rocker_h + 1.0f) / (this.r * 2.0f)));
        return (this.rocker_h + 1.0f) / (this.r * 2.0f);
    }
}
