package com.xbcmmoview.tools;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import com.sosocam.rcipcam3x.RCIPCam3X;
import com.wingedcam.util.ThumbnailUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AvcEncoder {
    private static final String TAG = "MeidaCodec";
    private static String path = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.h264");
    private static String rgb_path = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_640.rgb");
    private static String yuv_640_path = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_640.yuv");
    private static String yuv_path = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.yuv");
    private final int ENCODE_VIDEO = 0;
    HandlerThread EncoderThread;
    private final int H264 = 2;
    private final int H720P = 11;
    private final int NV12 = 0;
    private final int QVGA = 4;
    private final int VGA = 6;
    private final int W1920_H1080 = 20;
    private final int YUV420P = 11;
    private final int YV12_To_NV12 = 1;
    public byte[] configbyte;
    int count = 0;
    ByteBuffer[] inputBuffers;
    public boolean isRuning = false;
    int m_SupportColorFormat = -1;
    int m_camera = 0;
    Handler m_encoderHandler;
    int m_framerate;
    int m_height;
    byte[] m_info = null;
    int m_srcHeight;
    int m_srcWidth;
    int m_width;
    private MediaCodec mediaCodec;
    FileOutputStream outStream;
    ByteBuffer[] outputBuffers;
    private BufferedOutputStream outputStream;
    private BufferedOutputStream rgb_stream;
    private int skip_frame = 0;
    private int video_count = 0;
    byte[] yuv2data = null;
    private BufferedOutputStream yuv_640_output_stream;
    FileOutputStream yuv_outStream;
    private BufferedOutputStream yuv_outputStream;
    byte[] yuvdata = null;

    @SuppressLint({"NewApi"})
    public AvcEncoder(int srcWidth, int srcHeight, int width, int height, int framerate, int bitrate) {
        this.m_srcWidth = srcWidth;
        this.m_srcHeight = srcHeight;
        this.m_width = width;
        this.m_height = height;
        this.m_framerate = framerate;
        Log.e("Encoder", "AvcEncoder: " + this.m_width + " " + this.m_height);
    }

    @SuppressLint({"NewApi"})
    private int getSupportColorFormat() {
        int i;
        int numCodecs = MediaCodecList.getCodecCount();
        MediaCodecInfo codecInfo = null;
        for (i = 0; i < numCodecs && codecInfo == null; i++) {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
            if (info.isEncoder()) {
                String[] types = info.getSupportedTypes();
                boolean found = false;
                for (int j = 0; j < types.length && !found; j++) {
                    if (types[j].equals("video/avc")) {
                        System.out.println("found");
                        found = true;
                    }
                }
                if (found) {
                    codecInfo = info;
                }
            }
        }
        Log.e("AvcEncoder", "Found " + codecInfo.getName() + " supporting " + "video/avc");
        CodecCapabilities capabilities = codecInfo.getCapabilitiesForType("video/avc");
        Log.e("AvcEncoder", "length-" + capabilities.colorFormats.length + "==" + Arrays.toString(capabilities.colorFormats));
        i = 0;
        while (i < capabilities.colorFormats.length) {
            switch (capabilities.colorFormats[i]) {
                case 19:
                case 21:
                case 39:
                case 2130706688:
                case 2141391872:
                    Log.e("AvcEncoder", "supported color format::" + capabilities.colorFormats[i]);
                    return capabilities.colorFormats[i];
                default:
                    Log.e("AvcEncoder", "unsupported color format " + capabilities.colorFormats[i]);
                    i++;
            }
        }
        return -1;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void createfile() {
        this.m_SupportColorFormat = getSupportColorFormat();
        Log.e("AvcEncoder", "m_SupportColorFormat ：" + this.m_SupportColorFormat);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", this.m_width, this.m_height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, this.m_SupportColorFormat);
        mediaFormat.setInteger("bitrate", 4000000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
        mediaFormat.setInteger("bitrate-mode", 2);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try {
            this.mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        this.mediaCodec.start();
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            this.outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void create_yuv_file() {
        File file = new File(yuv_path);
        File file_640 = new File(yuv_640_path);
        File file_rgb = new File(rgb_path);
        if (file.exists()) {
            file.delete();
        }
        if (file_640.exists()) {
            file_640.delete();
        }
        if (file_rgb.exists()) {
            file_rgb.delete();
        }
    }

    @SuppressLint({"NewApi"})
    private void StopEncoder() {
        try {
            this.mediaCodec.stop();
            this.mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRuning() {
        return this.isRuning;
    }

    public void StopThread() {
        this.isRuning = false;
        this.EncoderThread = null;
        this.video_count = 0;
        this.skip_frame = 0;
        Log.e("AVEncode", "StopThread: " + this.m_width);
        try {
            StopEncoder();
            this.outputStream.flush();
            this.outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeArgbData(int[] argb, byte[] yuv) {
        if (isRuning()) {
            try {
                if (this.rgb_stream != null) {
                    this.rgb_stream.write(yuv, 0, yuv.length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int setVedioData(int camera, byte[] data, int tick) {
        this.m_camera = camera;
        if (this.isRuning) {
            Log.e("AvcEncoder", "send msg: " + tick);
            synchronized (this) {
                if (5 <= this.video_count && this.skip_frame == 0) {
                    this.skip_frame = 5;
                } else if (10 < this.video_count && 5 == this.skip_frame) {
                    this.skip_frame = 4;
                } else if (13 < this.video_count && 4 == this.skip_frame) {
                    this.skip_frame = 3;
                } else if (15 < this.video_count && 3 == this.skip_frame) {
                    this.skip_frame = 2;
                } else if (this.skip_frame <= 0 || this.video_count <= 0 || this.video_count % this.skip_frame != 0) {
                    this.video_count++;
                    Log.e("AvcEncoder", "video:" + this.video_count + " - " + this.skip_frame);
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = data;
                    msg.arg1 = tick;
                    this.m_encoderHandler.sendMessage(msg);
                } else {
                    Log.e("AvcEncoder", "skip:" + this.video_count + " - " + this.skip_frame);
                }
            }
        }
        return camera;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void encode_video_data(byte[] data, int tick) {
        int convert_type = 0;
        synchronized (this) {
            if (this.video_count > 0) {
                this.video_count--;
            }
        }
        Log.e("AvcEncoder", "recv msg: " + tick);
        switch (this.m_SupportColorFormat) {
            case 21:
                convert_type = 1;
                break;
                default:
        }
        int len = ((this.m_width * this.m_height) * 3) / 2;
        byte[] scaled_data = new byte[len];
        Log.e("AvcEncoder", "start scale len " + len + " width " + this.m_width);
        RCIPCam3X.RawVideoScale(11, data, this.m_srcWidth, this.m_srcHeight, scaled_data, this.m_width, this.m_height, convert_type);
        Log.e("AvcEncoder", "end scale");
        int encode_resolution = 0;
        if (ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT == this.m_width) {
            encode_resolution = 4;
        } else if (640 == this.m_width) {
            encode_resolution = 6;
        }
        try {
            ByteBuffer[] inputBuffers = this.mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = this.mediaCodec.getOutputBuffers();
            int inputBufferIndex = this.mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(scaled_data);
                this.mediaCodec.queueInputBuffer(inputBufferIndex, 0, scaled_data.length, (((long) tick) * 10) * 1000, 0);
                Log.e("AvcEncoder", "input  : " + ((((long) tick) * 10) * 1000));
            }
            BufferInfo bufferInfo = new BufferInfo();
            int outputBufferIndex = this.mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                Log.e("AvcEncoder", "Get H264 Buffer Success! flag = " + bufferInfo.flags + ",pts = " + bufferInfo.presentationTimeUs + "");
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                Log.e("AvcEncoder", "resolution: " + encode_resolution);
                if (bufferInfo.flags == 2) {
                    this.configbyte = new byte[bufferInfo.size];
                    outputBuffer.get(this.configbyte);
                } else if (bufferInfo.flags == 1 || bufferInfo.flags == 9) {
                    byte[] keyframe = new byte[(bufferInfo.size + this.configbyte.length)];
                    System.arraycopy(this.configbyte, 0, keyframe, 0, this.configbyte.length);
                    outputBuffer.get(keyframe, this.configbyte.length, bufferInfo.size);
                    RCIPCam3X.WriteEncodedVideoData(this.m_camera, true, encode_resolution, 2, (bufferInfo.presentationTimeUs / 10) / 1000, keyframe);
                    this.outputStream.write(keyframe, 0, keyframe.length);
                    Log.e("AvcEncoder", " keyframe.length： " + keyframe.length + " get tick:" + ((bufferInfo.presentationTimeUs / 10) / 1000));
                } else {
                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);
                    RCIPCam3X.WriteEncodedVideoData(this.m_camera, false, encode_resolution, 2, (bufferInfo.presentationTimeUs / 10) / 1000, outData);
                    this.outputStream.write(outData, 0, outData.length);
                    Log.e("AvcEncoder", " outData.length  ： " + outData.length + " get tick:" + ((bufferInfo.presentationTimeUs / 10) / 1000));
                }
                this.mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = this.mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("AvcEncoder_time", "Throwable:");
        }
    }

    public void StartEncoderThread() {
        this.isRuning = true;
        this.EncoderThread = new HandlerThread("");
        this.EncoderThread.start();
        this.m_encoderHandler = new Handler(this.EncoderThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        AvcEncoder.this.encode_video_data((byte[]) msg.obj, msg.arg1);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void swapNV12toI420(byte[] nv12bytes, byte[] i420bytes, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;
        System.arraycopy(nv12bytes, 0, i420bytes, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            i420bytes[nLenY + i] = nv12bytes[(i * 2) + nLenY];
            i420bytes[(nLenY + nLenU) + i] = nv12bytes[((i * 2) + nLenY) + 1];
        }
    }

    private long computePresentationTime(long frameIndex) {
        return 132 + ((1000000 * frameIndex) / ((long) this.m_framerate));
    }

    public byte[] rgb2YCbCr420(int[] pixels, int width, int height) {
        int len = width * height;
        byte[] yuv = new byte[((len * 3) / 2)];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = pixels[(i * width) + j] & ViewCompat.MEASURED_SIZE_MASK;
                int b = rgb & 255;
                int g = (rgb >> 8) & 255;
                int r = (rgb >> 16) & 255;
                int y = (((((r * 66) + (g * 129)) + (b * 25)) + 128) >> 8) + 16;
                int u = (((((r * -38) - (g * 74)) + (b * 112)) + 128) >> 8) + 128;
                int v = (((((r * 112) - (g * 94)) - (b * 18)) + 128) >> 8) + 128;
                if (y < 16) {
                    y = 16;
                } else if (y > 255) {
                    y = 255;
                }
                if (u < 0) {
                    u = 0;
                } else if (u > 255) {
                    u = 255;
                }
                if (v < 0) {
                    v = 0;
                } else if (v > 255) {
                    v = 255;
                }
                yuv[(i * width) + j] = (byte) y;
                yuv[((((i >> 1) * width) + len) + (j & -2)) + 0] = (byte) u;
                yuv[((((i >> 1) * width) + len) + (j & -2)) + 1] = (byte) v;
            }
        }
        return yuv;
    }

    void swapYV12toNV12(byte[] yv12bytes, byte[] nv12bytes, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;
        System.arraycopy(yv12bytes, 0, nv12bytes, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            nv12bytes[(i * 2) + nLenY] = yv12bytes[nLenY + i];
            nv12bytes[((i * 2) + nLenY) + 1] = yv12bytes[(nLenY + nLenU) + i];
        }
        Log.e("YUVData", "swapYV12toNV12 :");
    }
}
