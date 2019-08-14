package com.wingedcam.ipcam;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.wingedcam.util.ThumbnailUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

@SuppressLint({"NewApi", "ClickableViewAccessibility"})
public class IPCamVideoView extends SurfaceView implements Callback, OnTouchListener {
    private static final int DRAG = 1;
    public static final float MAX_ZOOM = 4.0f;
    public static final float MIN_ZOOM = 1.0f;
    private static final int MSG_SHOW_IMAGE = 0;
    private static final int NONE = 0;
    private static final int ZOOM = 2;
    public static int decode_model = 1;
    public static int mediacodec_h = 0;
    public static int mediacodec_w = 0;
    private boolean ShowTag = false;
    private byte[] buffer_raw_1 = null;
    private byte[] buffer_raw_2 = null;
    private boolean first = false;
    private int flip = 0;
    private boolean isInput = true;
    private int mCount = 0;
    private MediaCodec mDecoder;
    private String mScreenshotPath = (Environment.getExternalStorageDirectory() + "/droidnova");
    private int m_action = 0;
    private int[] m_argb = null;
    private Bitmap[] m_buffing_logo = null;
    private int m_buffing_logo_offset = 0;
    private Runnable m_buffing_run = new Runnable() {
        public void run() {
            if (IPCamVideoView.this.m_state == STATE.BUFFING) {
                IPCamVideoView.this.m_buffing_logo_offset = IPCamVideoView.this.m_buffing_logo_offset + 1;
                if (IPCamVideoView.this.m_buffing_logo_offset >= IPCamVideoView.this.m_buffing_logo.length) {
                    IPCamVideoView.this.m_buffing_logo_offset = 0;
                }
                IPCamVideoView.this.show_image();
                new Handler().postAtTime(IPCamVideoView.this.m_buffing_run, SystemClock.uptimeMillis() + 1000);
            }
        }
    };
    private int m_center_x = 0;
    private int m_center_y = 0;
    private Rect m_dest_rect = new Rect();
    private ImageBuffer m_image_buffer = new ImageBuffer();
    private int m_image_height = 360;
    private int m_image_width = 640;
    private boolean m_keep_image_radio = true;
    private PointF m_last_point = new PointF();
    private IPCamVideoView_Listener m_listener = null;
    private IPCamVideoViewHandler m_message_handler = new IPCamVideoViewHandler(this);
    private Bitmap m_pausing_logo = null;
    private Rect m_src_rect = new Rect();
    private float m_start_distance = 0.0f;
    private PointF m_start_point = new PointF();
    private STATE m_state = STATE.PLAYING;
    private int m_surface_height = 0;
    private SurfaceHolder m_surface_holder = null;
    private int m_surface_width = 0;
    private float m_zoom = 1.0f;
    private MediaFormat mediaFormat;
    public screen_change_listener on_screen_change_listener = null;
    private MediaFormat outputFormat;
    private long startWhen = 0;
    private boolean surface_creae = false;

    private static class IPCamVideoViewHandler extends Handler {
        private final WeakReference<IPCamVideoView> m_view;

        public IPCamVideoViewHandler(IPCamVideoView view) {
            this.m_view = new WeakReference(view);
        }

        public void handleMessage(Message msg) {
            IPCamVideoView view = (IPCamVideoView) this.m_view.get();
            if (view != null) {
                switch (msg.what) {
                    case 0:
                        view.show_image();
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public interface IPCamVideoView_Listener {

        public enum TOUCH_EVENT {
            CLICK,
            MOVE_UP,
            MOVE_DOWN,
            MOVE_LEFT,
            MOVE_RIGHT
        }

        void on_touch_event(TOUCH_EVENT touch_event);
    }

    private class ImageBuffer {
        private Image[] m_images;
        private ReentrantLock m_lock;

        private class Image {
            int[] argb;
            int height;
            int index;
            boolean locking;
            boolean ready;
            boolean showed;
            int width;

            private Image() {
                this.index = 0;
                this.argb = null;
                this.width = 0;
                this.height = 0;
                this.ready = false;
                this.showed = false;
                this.locking = false;
            }


        }

        public ImageBuffer() {
            this.m_images = new Image[]{new Image(), new Image()};
            this.m_lock = new ReentrantLock(false);
            this.m_images[0].index = 0;
            this.m_images[1].index = 1;
        }

        public void clear() {
            int i;
            do {
                this.m_lock.lock();
                i = 0;
                while (i < 2 && !this.m_images[i].locking) {
                    this.m_images[i].ready = false;
                    this.m_images[i].showed = false;
                    this.m_images[i].argb = null;
                    i++;
                }
                this.m_lock.unlock();
            } while (i != 2);
        }

        public boolean push_image(int[] argb, int width, int height) {
            this.m_lock.lock();
            int i = 0;
            while (i < 2) {
                if (!this.m_images[i].ready || this.m_images[i].showed) {
                    i++;
                } else {
                    this.m_lock.unlock();
                    return false;
                }
            }
            i = 0;
            while (i < 2) {
                if (!this.m_images[i].ready) {
                    this.m_images[i].locking = true;
                    break;
                }
                i++;
            }
            this.m_lock.unlock();
            if (this.m_images[i].argb == null) {
                this.m_images[i].argb = new int[(width * height)];
            } else if (this.m_images[i].argb.length != width * height) {
                this.m_images[i].argb = new int[(width * height)];
            }
            System.arraycopy(argb, 0, this.m_images[i].argb, 0, width * height);
            this.m_images[i].width = width;
            this.m_images[i].height = height;
            this.m_lock.lock();
            this.m_images[i].ready = true;
            this.m_images[i].showed = false;
            this.m_images[i].locking = false;
            if (i == 0) {
                i = 1;
            } else {
                i = 0;
            }
            if (this.m_images[i].ready && !this.m_images[i].locking) {
                this.m_images[i].ready = false;
            }
            this.m_lock.unlock();
            return true;
        }

        public Image get_image() {
            Image image = null;
            this.m_lock.lock();
            for (int i = 0; i < 2; i++) {
                if (this.m_images[i].ready) {
                    this.m_images[i].showed = true;
                    this.m_images[i].locking = true;
                    image = this.m_images[i];
                    break;
                }
            }
            this.m_lock.unlock();
            return image;
        }

        public void release_image(Image image) {
            int i;
            if (image.index == 0) {
                i = 1;
            } else {
                i = 0;
            }
            this.m_lock.lock();
            image.locking = false;
            if (this.m_images[i].ready) {
                image.ready = false;
            }
            this.m_lock.unlock();
        }
    }

    public enum STATE {
        PLAYING,
        PAUSING,
        BUFFING
    }

    public interface screen_change_listener {
        void on_screen_change(int i, int i2, int i3, int i4);
    }

    public int getFlip() {
        return this.flip;
    }

    public void setFlip(int flip) {
        this.flip = flip;
    }

    public boolean isShowTag() {
        return this.ShowTag;
    }

    public void setShowTag(boolean showTag) {
        this.ShowTag = showTag;
    }

    public IPCamVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e("sfc", "IPCamVideoView");
        this.m_surface_holder = getHolder();
        this.m_surface_holder.addCallback(this);
        setOnTouchListener(this);
        String package_name = context.getPackageName();
        this.m_pausing_logo = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("start_play", "drawable", package_name));
        this.m_buffing_logo = new Bitmap[3];
        this.m_buffing_logo[0] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("loading_step1", "drawable", package_name));
        this.m_buffing_logo[1] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("loading_step2", "drawable", package_name));
        this.m_buffing_logo[2] = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("loading_step3", "drawable", package_name));
    }

    public void set_screen_change_listener(screen_change_listener m_on_screen_change_listener) {
        this.on_screen_change_listener = m_on_screen_change_listener;
    }

    public boolean set_image(int[] argb, int width, int height) {
        if (!this.m_image_buffer.push_image(argb, width, height)) {
            return false;
        }
        this.m_message_handler.sendEmptyMessage(0);
        return true;
    }

    private boolean show_raw(byte[] raw) {
        try {
            int inputBufferIndex = this.mDecoder.dequeueInputBuffer(0);
            if (inputBufferIndex < 0) {
                return false;
            }
            ByteBuffer inputBuffer = this.mDecoder.getInputBuffers()[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(raw, 0, raw.length);
            this.mDecoder.queueInputBuffer(inputBufferIndex, 0, raw.length, (long) ((this.mCount * IPCam.PLAY_TF_RECORD_CACHE_NORMAL) / 30), 0);
            this.mCount++;
            BufferInfo bufferInfo = new BufferInfo();
            int outputBufferIndex = this.mDecoder.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                Log.e("sfc", "outputBufferIndex >= 0");
                this.mDecoder.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = this.mDecoder.dequeueOutputBuffer(bufferInfo, 0);
                setShowTag(true);
            }
            return true;
        } catch (Exception e) {
            Log.e("sfc", "inputBufferIndex erre");
            reset_mediaCodec();
            return false;
        }
    }

    public void set_raw(byte[] raw, int width, int height) {
        if (raw == null || !this.surface_creae) {
            Log.e("sys", "set_raw raw: " + raw + ", surface_creae: " + this.surface_creae);
            return;
        }
        if (!(width == mediacodec_w && mediacodec_h == height)) {
            mediacodec_w = width;
            mediacodec_h = height;
            reset_mediaCodec();
        }
        boolean ret = false;
        while (!ret) {
            ret = show_raw(raw);
        }
    }

    public Bitmap get_cover() {
        ImageBuffer.Image image = this.m_image_buffer.get_image();
        if (image == null) {
            return null;
        }
        Bitmap temp = Bitmap.createBitmap(image.argb, image.width, image.height, Config.ARGB_8888);
        this.m_image_buffer.release_image(image);
        Bitmap cover = Bitmap.createScaledBitmap(temp, ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT, (image.height * ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT) / image.width, false);
        temp.recycle();
        return cover;
    }

    @SuppressLint("WrongThread")
    public boolean snapshot(String path) {
        ImageBuffer.Image image = this.m_image_buffer.get_image();
        if (image == null) {
            return false;
        }
        Bitmap temp = Bitmap.createBitmap(image.argb, image.width, image.height, Config.ARGB_8888);
        this.m_image_buffer.release_image(image);
        boolean ret = true;
        try {
            FileOutputStream out = new FileOutputStream(new File(path));
            temp.compress(CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            ret = false;
        }
        temp.recycle();
        return ret;
    }

    @SuppressLint("WrongThread")
    public boolean snapshot(String path, int width, int height) {
        ImageBuffer.Image image = this.m_image_buffer.get_image();
        if (image == null) {
            return false;
        }
        Bitmap temp = Bitmap.createBitmap(image.argb, image.width, image.height, Config.ARGB_8888);
        this.m_image_buffer.release_image(image);
        if (temp != null) {
            temp = Bitmap.createScaledBitmap(temp, width, height, true);
        }
        boolean ret = true;
        try {
            FileOutputStream out = new FileOutputStream(new File(path));
            temp.compress(CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            ret = false;
        }
        temp.recycle();
        return ret;
    }

    public void clear() {
        this.m_image_buffer.clear();
        show_image();
    }

    public STATE state() {
        return this.m_state;
    }

    public void set_state(STATE state) {
        if (this.m_state != state) {
            this.m_state = state;
            if (state == STATE.BUFFING) {
                this.m_buffing_logo_offset = 0;
                new Handler().postAtTime(this.m_buffing_run, SystemClock.uptimeMillis() + 1000);
            }
            show_image();
        }
    }

    public boolean keep_image_radio() {
        return this.m_keep_image_radio;
    }

    public void set_keep_image_radio(boolean keep_image_radio) {
        if (this.m_keep_image_radio != keep_image_radio) {
            this.m_keep_image_radio = keep_image_radio;
            calc_rect();
        }
    }

    public void set_listener(IPCamVideoView_Listener listener) {
        this.m_listener = listener;
    }

    private void show_image() {
        if (this.m_argb != null) {
            ImageBuffer.Image image = this.m_image_buffer.get_image();
            int width = (this.m_dest_rect.right - this.m_dest_rect.left) + 1;
            int height = (this.m_dest_rect.bottom - this.m_dest_rect.top) + 1;
            if (image != null) {
                if (!(image.width == this.m_image_width && image.height == this.m_image_height)) {
                    this.m_image_width = image.width;
                    this.m_image_height = image.height;
                    init();
                }
                this.m_image_buffer.release_image(image);
            }
            setShowTag(true);
            Canvas c = this.m_surface_holder.lockCanvas();
            if (c != null) {
                float scale = ((float) getHeight()) / ((float) getWidth());
                c.drawColor(ViewCompat.MEASURED_STATE_MASK);
                if (this.flip != 0) {
                    if (1 == this.flip) {
                        c.scale(scale, scale, (float) (getWidth() / 2), (float) (getHeight() / 2));
                        c.rotate(90.0f, (float) (getWidth() / 2), (float) (getHeight() / 2));
                    } else if (2 == this.flip) {
                        c.rotate(180.0f, (float) (getWidth() / 2), (float) (getHeight() / 2));
                    } else if (3 == this.flip) {
                        c.scale(scale, scale, (float) (getWidth() / 2), (float) (getHeight() / 2));
                        c.rotate(270.0f, (float) (getWidth() / 2), (float) (getHeight() / 2));
                    }
                }
                if (image != null) {
                    c.drawBitmap(Bitmap.createBitmap(image.argb, image.width, image.height, Config.ARGB_8888), this.m_src_rect, this.m_dest_rect, null);
                }
                if (this.m_state == STATE.PAUSING && this.m_pausing_logo != null) {
                    draw_logo(c, this.m_pausing_logo);
                } else if (this.m_state == STATE.BUFFING && this.m_buffing_logo != null && this.m_buffing_logo.length > this.m_buffing_logo_offset) {
                    draw_logo(c, this.m_buffing_logo[this.m_buffing_logo_offset]);
                }
                getHolder().unlockCanvasAndPost(c);
            }
        }
    }

    private void draw_logo(Canvas c, Bitmap logo) {
        int logo_width = this.m_pausing_logo.getWidth();
        int logo_height = this.m_pausing_logo.getHeight();
        Rect src_rect = new Rect(0, 0, logo_width - 1, logo_height - 1);
        Rect dest_rect = new Rect();
        dest_rect.left = (this.m_surface_width - logo_width) / 2;
        dest_rect.top = (this.m_surface_height - logo_height) / 2;
        dest_rect.right = (dest_rect.left + logo_width) - 1;
        dest_rect.bottom = (dest_rect.top + logo_height) - 1;
        c.drawBitmap(logo, src_rect, dest_rect, null);
    }

    private void init() {
        this.m_zoom = 1.0f;
        this.m_center_x = this.m_image_width / 2;
        this.m_center_y = this.m_image_height / 2;
        calc_rect();
    }

    private void adjust_center() {
        int w = (this.m_src_rect.right - this.m_src_rect.left) + 1;
        int h = (this.m_src_rect.bottom - this.m_src_rect.top) + 1;
        if (this.m_center_x - (w / 2) < 0) {
            this.m_center_x = w / 2;
            this.m_src_rect.left = 0;
            this.m_src_rect.right = w - 1;
        } else if (this.m_center_x + (w / 2) >= this.m_image_width) {
            this.m_center_x = this.m_image_width - (w / 2);
            this.m_src_rect.right = this.m_image_width - 1;
            this.m_src_rect.left = (this.m_src_rect.right - w) + 1;
        } else {
            this.m_src_rect.left = this.m_center_x - (w / 2);
            this.m_src_rect.right = (this.m_src_rect.left + w) - 1;
        }
        if (this.m_center_y - (h / 2) < 0) {
            this.m_center_y = h / 2;
            this.m_src_rect.top = 0;
            this.m_src_rect.bottom = h - 1;
        } else if (this.m_center_y + (h / 2) >= this.m_image_height) {
            this.m_center_y = this.m_image_height - (h / 2);
            this.m_src_rect.bottom = this.m_image_height - 1;
            this.m_src_rect.top = (this.m_src_rect.bottom - h) + 1;
        } else {
            this.m_src_rect.top = this.m_center_y - (h / 2);
            this.m_src_rect.bottom = (this.m_src_rect.top + h) - 1;
        }
    }

    private void calc_rect() {
        int w;
        int h;
        float imageRatio = (((float) this.m_image_width) * 1.0f) / ((float) this.m_image_height);
        float surfaceRatio = (((float) this.m_surface_width) * 1.0f) / ((float) this.m_surface_height);
        if (!this.m_keep_image_radio) {
            w = this.m_surface_width;
            h = this.m_surface_height;
        } else if (imageRatio < surfaceRatio) {
            h = this.m_surface_height;
            w = (int) (((float) h) * imageRatio);
        } else {
            w = this.m_surface_width;
            h = (int) (((float) w) / imageRatio);
        }
        if (this.m_zoom > 1.0f) {
            w = Math.min(this.m_surface_width, (int) (((float) w) * this.m_zoom));
            h = Math.min(this.m_surface_height, (int) (((float) h) * this.m_zoom));
        }
        if (this.on_screen_change_listener != null) {
            this.on_screen_change_listener.on_screen_change(this.m_surface_width, this.m_surface_height, w, h);
        }
        this.m_dest_rect.left = (this.m_surface_width - w) / 2;
        this.m_dest_rect.top = (this.m_surface_height - h) / 2;
        this.m_dest_rect.right = (this.m_dest_rect.left + w) - 1;
        this.m_dest_rect.bottom = (this.m_dest_rect.top + h) - 1;
        float showRatio = (((float) w) * 1.0f) / ((float) h);
        if (showRatio > imageRatio) {
            h = (int) (((float) this.m_image_height) / this.m_zoom);
            w = Math.min(this.m_image_width, (int) (((float) h) * showRatio));
        } else {
            w = (int) (((float) this.m_image_width) / this.m_zoom);
            h = Math.min(this.m_image_height, (int) (((float) w) / showRatio));
        }
        this.m_src_rect.left = this.m_center_x - (w / 2);
        this.m_src_rect.top = this.m_center_y - (h / 2);
        this.m_src_rect.right = (this.m_src_rect.left + w) - 1;
        this.m_src_rect.bottom = (this.m_src_rect.top + h) - 1;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("wingedcamlib", "surfaceCreated");
        Log.e("sfc", "surfaceCreated");
        decode_model = 0;
        if (decode_model == 1) {
            this.surface_creae = true;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("sfc", "surfaceChanged");
        if (decode_model == 0) {
            this.m_surface_height = height;
            this.m_surface_width = width;
            this.m_argb = new int[(width * height)];
            init();
            show_image();
        }
    }

    public void update_screen() {
        calc_rect();
        adjust_center();
        show_image();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("sfc", "surfaceDestroyed");
        if (decode_model == 0) {
            this.m_argb = null;
            return;
        }
        this.surface_creae = false;
        if (this.mDecoder != null) {
            try {
                this.mDecoder.stop();
                this.mDecoder.release();
                this.mDecoder = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        PointF current_point;
        int offset_x;
        int offset_y;
        float distance;
        switch (event.getAction() & 255) {
            case 0:
                this.m_start_point.set(event.getX(), event.getY());
                this.m_last_point.set(this.m_start_point);
                this.m_action = 1;
                break;
            case 1:
                if (this.m_listener != null) {
                    current_point = new PointF();
                    current_point.set(event.getX(), event.getY());
                    offset_x = ((int) current_point.x) - ((int) this.m_start_point.x);
                    offset_y = ((int) current_point.y) - ((int) this.m_start_point.y);
                    if (Math.abs(offset_x) < 10 && Math.abs(offset_y) < 10) {
                        this.m_listener.on_touch_event(IPCamVideoView_Listener.TOUCH_EVENT.CLICK);
                    }
                    if (this.m_zoom == 1.0f) {
                        if (Math.abs(offset_x) >= Math.abs(offset_y)) {
                            if (offset_x > 40) {
                                this.m_listener.on_touch_event(IPCamVideoView_Listener.TOUCH_EVENT.MOVE_LEFT);
                            } else if (offset_x < -40) {
                                this.m_listener.on_touch_event(IPCamVideoView_Listener.TOUCH_EVENT.MOVE_RIGHT);
                            }
                        } else if (offset_y > 40) {
                            this.m_listener.on_touch_event(IPCamVideoView_Listener.TOUCH_EVENT.MOVE_UP);
                        } else if (offset_y < -40) {
                            this.m_listener.on_touch_event(IPCamVideoView_Listener.TOUCH_EVENT.MOVE_DOWN);
                        }
                    }
                }
                this.m_action = 0;
                break;
            case 2:
                if (decode_model != 1) {
                    if (this.m_action != 1) {
                        if (event.getPointerCount() != 1) {
                            distance = spacing(event);
                            this.m_zoom *= distance / this.m_start_distance;
                            this.m_zoom = Math.max(1.0f, Math.min(this.m_zoom, 4.0f));
                            this.m_start_distance = distance;
                            calc_rect();
                            adjust_center();
                            show_image();
                            break;
                        }
                    } else if (this.m_zoom != 1.0f) {
                        current_point = new PointF();
                        current_point.set(event.getX(), event.getY());
                        if (this.flip == 0) {
                            offset_x = ((int) current_point.x) - ((int) this.m_last_point.x);
                            offset_y = ((int) current_point.y) - ((int) this.m_last_point.y);
                            this.m_last_point.set(current_point);
                            this.m_center_x -= offset_x;
                            this.m_center_y -= offset_y;
                        } else if (this.flip == 1) {
                            offset_x = ((int) current_point.y) - ((int) this.m_last_point.y);
                            offset_y = ((int) current_point.x) - ((int) this.m_last_point.x);
                            this.m_last_point.set(current_point);
                            this.m_center_x -= offset_x;
                            this.m_center_y += offset_y;
                        } else if (this.flip == 2) {
                            offset_x = ((int) current_point.x) - ((int) this.m_last_point.x);
                            offset_y = ((int) current_point.y) - ((int) this.m_last_point.y);
                            this.m_last_point.set(current_point);
                            this.m_center_x += offset_x;
                            this.m_center_y += offset_y;
                        } else if (this.flip == 3) {
                            offset_x = ((int) current_point.y) - ((int) this.m_last_point.y);
                            offset_y = ((int) current_point.x) - ((int) this.m_last_point.x);
                            this.m_last_point.set(current_point);
                            this.m_center_x += offset_x;
                            this.m_center_y -= offset_y;
                        }
                        adjust_center();
                        show_image();
                        break;
                    }
                }
                break;
            case 5:
                distance = spacing(event);
                if (distance > 10.0f) {
                    this.m_action = 2;
                    this.m_start_distance = distance;
                    break;
                }
                break;
            case 6:
                this.m_action = 0;
                break;
        }
        return true;
    }

    public void reset_mediaCodec() {
        Log.e("sfc", "reset_mediaCodec");
        String cpu = Build.HARDWARE;
        if (this.mDecoder != null) {
            try {
                this.mDecoder.stop();
                this.mDecoder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        try {
            this.mDecoder = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        this.mediaFormat = null;
        this.mediaFormat = MediaFormat.createVideoFormat("video/avc", mediacodec_w, mediacodec_h);
        if (cpu != null && (cpu.contains("mt") || cpu.contains("MT"))) {
            byte[] pps = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 104, (byte) -18, (byte) 60, Byte.MIN_VALUE};
            this.mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 103, (byte) 100, (byte) 0, (byte) 40, (byte) -84, (byte) 52, (byte) -59, (byte) 1, (byte) -32, (byte) 17, (byte) 31, (byte) 120, (byte) 11, (byte) 80, (byte) 16, (byte) 16, (byte) 31, (byte) 0, (byte) 0, (byte) 3, (byte) 3, (byte) -23, (byte) 0, (byte) 0, (byte) -22, (byte) 96, (byte) -108}));
            this.mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));
            this.mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mediacodec_w * mediacodec_h);
            this.mediaFormat.setInteger(MediaFormat.KEY_DURATION, 63446722);
        }
        try {
            this.mDecoder.configure(this.mediaFormat, this.m_surface_holder.getSurface(), null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            this.mDecoder.start();
        } catch (IllegalStateException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
        }
    }

    public boolean isSupportMediaCodecHardDecoder() {
        boolean isHardcode = false;
        InputStream inFile = null;
        try {
            inFile = new FileInputStream(new File("/system/etc/media_codecs.xml"));
        } catch (Exception e) {
        }
        if (inFile != null) {
            try {
                XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
                xmlPullParser.setInput(inFile, "UTF-8");
                for (int eventType = xmlPullParser.getEventType(); eventType != 1; eventType = xmlPullParser.next()) {
                    String tagName = xmlPullParser.getName();
                    switch (eventType) {
                        case 2:
                            if (!"MediaCodec".equals(tagName)) {
                                break;
                            }
                            String componentName = xmlPullParser.getAttributeValue(0);
                            if (componentName.startsWith("OMX.") && !componentName.startsWith("OMX.google.")) {
                                isHardcode = true;
                                break;
                            }
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e2) {
            }
        }
        return isHardcode;
    }
}
