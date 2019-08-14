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
import com.xbcmmoview.R;
import com.xbcmmoview.application.WingedCamApplication;

public class AccelerateRocker extends View {
    private int MAX = 255;
    private int MID = 128;
    private int MIN = 0;
    private int R_MID = 128;
    private float centreX = 0.0f;
    private float centreY = 0.0f;
    private byte[] channel = new byte[2];
    private byte channel_3;
    private byte channel_4;
    private DIRECTION direction;
    private boolean isInit = false;
    private boolean isSet_high = false;
    private Right_rocker_listeren listern = null;
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

    public interface Right_rocker_listeren {
        void Right_rocker_listeren_(byte b, byte b2);
    }

    public void set_listern(Right_rocker_listeren m_listern) {
        this.listern = m_listern;
    }

    public AccelerateRocker(Context context, AttributeSet attrs) {
        super(context, attrs);
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rocker);
        Log.e("imageview", "X: " + this.mMotionX + ", Y: " + this.mMotionY);
        Log.e("TAG", "mBitmap.getWidth(): " + mBitmap.getWidth() + ", mBitmap.getHeight(): " + mBitmap.getHeight());
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f);
        this.resizeBmp = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
        this.rocker_h = (float) (this.resizeBmp.getHeight() / 2);
        this.rocker_w = (float) (this.resizeBmp.getWidth() / 2);
        Log.e("TAG", "rocker_h:" + this.rocker_h + "--rocker_w:" + this.rocker_w);
        this.R_MID = WingedCamApplication.getRotate_mid();
    }

    private void channel_rate(float x, float y) {
        this.R_MID = WingedCamApplication.getR_mid();
        if (x <= getRange() || ((double) x) >= 0.5d) {
            if (((double) x) <= 0.5d || x >= 1.0f - getRange()) {
                if (x >= 1.0f - getRange()) {
                    this.channel_4 = (byte) -1;
                } else if (x <= getRange()) {
                    this.channel_4 = (byte) 0;
                } else {
                    this.channel_4 = (byte) this.R_MID;
                }
            } else if (((double) x) <= 0.625d) {
                this.channel_4 = (byte) this.R_MID;
            } else {
                this.channel_4 = (byte) ((int) (((((double) (255 - this.R_MID)) * ((((double) x) - 0.625d) / 0.625d)) * 2.32919257551792d) + ((double) this.R_MID)));
                Log.e("weitiao", "channel_4——3 (channel_4 &0xff):" + (this.channel_4 & 255) + "  x:" + x);
            }
        } else if (((double) x) >= 0.375d) {
            this.channel_4 = (byte) this.R_MID;
        } else {
            this.channel_4 = (byte) ((int) (((double) this.R_MID) - ((((0.375d - ((double) x)) / 0.375d) * ((double) this.R_MID)) * 1.3975155453107526d)));
            Log.e("weitiao", "channel_4——2 (R_MID * x):" + (this.channel_4 & 255) + " x:" + x);
        }
        if (y > getRange() && y < 1.0f - getRange() && ((double) y) != 0.5d) {
            this.channel_3 = (byte) ((int) (((double) this.MID) - ((((0.5d - ((double) y)) / 0.5d) * ((double) this.MID)) * 1.2658228d)));
        } else if (((double) y) > 0.5d && y < 1.0f - getRange()) {
            this.channel_3 = (byte) ((int) (((((double) (255 - this.MID)) * ((((double) y) - 0.5d) / 0.5d)) * 1.2658228d) + ((double) this.MID)));
        } else if (((double) y) == 0.5d) {
            this.channel_3 = (byte) this.MID;
        } else if (y <= getRange()) {
            this.channel_3 = (byte) 0;
        } else {
            this.channel_3 = (byte) -1;
        }
        if (this.listern != null) {
            this.listern.Right_rocker_listeren_(this.channel_3, this.channel_4);
        }
        this.channel[0] = this.channel_4;
        this.channel[1] = this.channel_3;
    }

    @SuppressLint({"NewApi"})
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.isInit) {
            this.isInit = true;
            this.mMotionX = (float) (getHeight() / 2);
            this.mMotionY = (float) (getWidth() / 2);
            Log.e("TAG", "mMotionX:" + this.mMotionX + "--mMotionY:" + this.mMotionY);
            this.centreX = this.mMotionX;
            this.centreY = this.mMotionY;
            if (this.isSet_high) {
                this.mMotionY = this.centreX;
            } else {
                this.mMotionY = ((float) getWidth()) - this.rocker_w;
            }
            this.r = this.centreX;
        }
        Log.e("imageview", "r: " + this.r + ", rocker_w: " + this.rocker_w + "(r/(r-rocker_w)):" + ((((double) this.r) * 0.375d) / ((((double) this.r) * 0.375d) - ((double) this.rocker_w))));
        canvas.drawBitmap(this.resizeBmp, this.mMotionX - this.rocker_h, this.mMotionY - this.rocker_w, this.paint);
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
            if (this.isSet_high) {
                this.mMotionX = (float) (getHeight() / 2);
                this.mMotionY = (float) (getWidth() / 2);
            } else {
                this.mMotionX = this.centreX;
                if (dst > this.r - this.rocker_h) {
                    Log.e("rocker", dst + "");
                } else {
                    this.mMotionY = (float) ((int) ev.getY());
                }
            }
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

    public void setSet_high(boolean set_high) {
        this.isSet_high = set_high;
        if (this.isSet_high) {
            this.mMotionY = this.centreX;
        } else {
            this.mMotionY = ((float) getWidth()) - this.rocker_w;
        }
        postInvalidate();
    }

    private float getRange() {
        Log.e("imageview", "getRange: " + ((this.rocker_h + 1.0f) / (this.r * 2.0f)));
        return (this.rocker_h + 1.0f) / (this.r * 2.0f);
    }
}
