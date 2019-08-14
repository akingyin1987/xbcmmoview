package com.wingedcam.util;

import android.graphics.Bitmap;
import com.sosocam.rcipcam3x.RCIPCam3X;
import com.wingedcam.ipcam.IPCam;
import org.json.JSONObject;

/**
 * @author king
 * @version V1.0
 * @ Description:
 * @ Date 2019/4/20 13:41
 */
public class HttpClient {


  public   static   int   TIMEOUT = 10000;
  public static JSONObject get_json(String url) {
    return get_json(url, TIMEOUT);
  }

  public static JSONObject get_json(String url, int timeout) {
    byte[] content = RCIPCam3X.HttpClientGet(url, timeout / IPCam.PLAY_TF_RECORD_CACHE_NORMAL);
    if (content == null || content.length == 0) {
      return null;
    }
    try {
      return new JSONObject(new String(content, "UTF-8"));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }


  public static Bitmap get_image(String url) {
    return get_image(url, TIMEOUT);
  }

  public static Bitmap get_image(String url, int timeout) {
    Bitmap image = null;
    byte[] content = get_binary(url, timeout);
    if((content != null) && (content.length != 0)) {
      try {
        return image;
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    return image;
  }

  public static byte[] get_binary(String url) {
    return get_binary(url, TIMEOUT);
  }

  public static byte[] get_binary(String url, int timeout) {
    byte[] binary = null;
    binary = RCIPCam3X.HttpClientGet(url, (timeout / 1000));
    return binary;
  }

  public static byte[] get_gif(String url) {
    return get_binary(url, TIMEOUT);
  }
}
