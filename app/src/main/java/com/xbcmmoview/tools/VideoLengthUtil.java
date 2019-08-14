package com.xbcmmoview.tools;

import com.wingedcam.ipcam.IPCam;
import java.util.Formatter;
import java.util.Locale;

public class VideoLengthUtil {
    public static String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / IPCam.PLAY_TF_RECORD_CACHE_NORMAL;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        return mFormatter.format("%02d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)}).toString();
    }
}
