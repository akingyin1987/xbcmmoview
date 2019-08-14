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


public class CircleRocker extends View {
    private int centreX = 0;
    private int centreY = 0;
    private DIRECTION direction;
    private boolean isInit = false;
    private int lastAngle = 0;
    private int mMotionX = 0;
    private int mMotionY = 0;
    private Paint paint = new Paint();
    private int r = 0;
    private double rad = 0.0d;
    private Bitmap resizeBmp;
    private int rocker_h = 0;
    private int rocker_w = 0;
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

    public CircleRocker(Context context, AttributeSet attrs) {
        super(context, attrs);
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.live_button);
        Log.e("imageview", "X: " + this.mMotionX + ", Y: " + this.mMotionY);
        Log.e("imageview", "X: " + getWidth() + ", Y: " + getHeight() + "");
        Matrix matrix = new Matrix();
        matrix.postScale(0.6f, 0.6f);
        this.resizeBmp = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        this.rocker_h = this.resizeBmp.getHeight() / 2;
        this.rocker_w = this.resizeBmp.getWidth() / 2;
    }

    @SuppressLint({"NewApi"})
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.isInit) {
            this.isInit = true;
            this.mMotionX = getHeight() / 2;
            this.mMotionY = getWidth() / 2;
            this.centreX = this.mMotionX;
            this.centreY = this.mMotionY;
            this.r = this.centreX;
        }
        canvas.drawBitmap(this.resizeBmp, (float) (this.mMotionX - this.rocker_h), (float) (this.mMotionY - this.rocker_w), this.paint);
        Log.e("4 imageview", "direction: " + this.direction);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        this.rad = getRad((float) this.centreX, (float) this.centreY, ev.getX(), ev.getY());
        float dst = pointDistance((float) this.centreX, (float) this.centreY, ev.getX(), ev.getY());
        if (ev.getAction() == 0) {
            return true;
        }
        if (ev.getAction() == 2) {
            if (dst > ((float) (this.r - this.rocker_h))) {
                getXY((float) this.centreX, (float) this.centreY, (float) (this.r - this.rocker_h), this.rad);
            } else {
                this.mMotionX = (int) ev.getX();
                this.mMotionY = (int) ev.getY();
            }
            invalidate();
            this.direction = moveDriection((float) this.mMotionX, (float) this.mMotionY, this.rad);
            return true;
        } else if (ev.getAction() != 1) {
            return super.onTouchEvent(ev);
        } else {
            this.mMotionX = getHeight() / 2;
            this.mMotionY = getWidth() / 2;
            invalidate();
            this.rad = 0.0d;
            this.direction = moveDriection((float) this.mMotionX, (float) this.mMotionY, this.rad);
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
        if (y == ((float) this.centreY) && x == ((float) this.centreX) && rad == 0.0d) {
            return DIRECTION.CENTRE;
        }
        if (x < ((float) this.centreX) && rad >= 2.25d && rad <= 3.33d) {
            return DIRECTION.LEFT;
        }
        if (x > ((float) this.centreX) && rad >= 0.0d && rad <= 0.75d) {
            return DIRECTION.RIGHT;
        }
        if (y < ((float) this.centreY) && rad >= 0.75d && rad <= 2.25d) {
            return DIRECTION.UP;
        }
        if (y <= ((float) this.centreY) || rad < 0.75d || rad > 2.25d) {
            return DIRECTION.INVALID;
        }
        return DIRECTION.DOWN;
    }

    private DIRECTION rocker_eight(float x, float y, double rad) {
        if (y == ((float) this.centreY) && x == ((float) this.centreX) && rad == 0.0d) {
            return DIRECTION.CENTRE;
        }
        if (x > ((float) this.centreX) && rad <= 0.375d && rad >= 0.0d) {
            return DIRECTION.RIGHT;
        }
        if (x > ((float) this.centreX) && y < ((float) this.centreY) && rad >= 0.375d && rad <= 1.125d) {
            return DIRECTION.RIGHT_UP;
        }
        if (x > ((float) this.centreX) && y > ((float) this.centreY) && rad >= 0.375d && rad <= 1.125d) {
            return DIRECTION.RIGHT_DOWN;
        }
        if (y < ((float) this.centreY) && rad >= 1.125d && rad <= 1.875d) {
            return DIRECTION.UP;
        }
        if (y < ((float) this.centreY) && x < ((float) this.centreX) && rad >= 1.875d && rad <= 2.625d) {
            return DIRECTION.LEFT_UP;
        }
        if (x < ((float) this.centreX) && rad >= 2.625d && rad <= 3.333d) {
            return DIRECTION.LEFT;
        }
        if (x < ((float) this.centreX) && y > ((float) this.centreY) && rad >= 1.875d && rad <= 2.625d) {
            return DIRECTION.LEFT_DOWN;
        }
        if (y <= ((float) this.centreY) || rad > 1.875d || rad < 1.125d) {
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
        this.mMotionX = (int) ((((double) R) * Math.cos(rad)) + ((double) centerX));
        this.mMotionY = (int) ((((double) R) * Math.sin(rad)) + ((double) centerY));
    }
}
