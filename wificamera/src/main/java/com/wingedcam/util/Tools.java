package com.wingedcam.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author king
 * @version V1.0
 * @ Description:
 * @ Date 2019/4/20 13:45
 */
public class Tools {

  private static final int RADIX = 0x10;
  private static final String SEED = "0933910847463829232312312";
  private static Toast m_toast = null;

  public static void showLongToast(Context context, String tip) {
    if(m_toast != null) {
      m_toast.cancel();
    }
    m_toast = Toast.makeText(context, tip, Toast.LENGTH_SHORT);
    m_toast.show();
  }

  public static void showShortToast(Context context, String tip) {
    if(m_toast != null) {
      m_toast.cancel();
    }
    m_toast = Toast.makeText(context, tip, Toast.LENGTH_SHORT);
    m_toast.show();
  }

  public static void cancelToast() {
    if(m_toast != null) {
      m_toast.cancel();
    }
  }

  public static int passwordStrength(String str) {
    int len = str.length();
    int score = 0;
    if(len < 5) {
      return score;
    }
    score = score + 1;

    return score;
  }

  public static String check_language() {
    String language = Locale.getDefault().getLanguage();
    if((language.equals("zh")) || (language.equals("zh_CN"))) {
      return language;
    }

    return language;
  }

  public static void add_media(Context context, String path) {
    context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse("file://" + path)));
  }


  public static void remove_media(String path) {
  }

  public static Date StrToDate(String str) {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    Date date = null;
    try {
      return format.parse(str);
    } catch(Exception e) {
      e.printStackTrace();
    }
    return date;
  }

  public static Date LongToDate(long data) {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
    return new Date(data);
  }

  public static String encrypt(String data) {
    if(data == null) {
      return "";
    }
    if(data.length() == 0) {
      return "";
    }
    BigInteger bi_passwd = new BigInteger(data.getBytes());
    BigInteger bi_r0 = new BigInteger("0933910847463829232312312");
    BigInteger bi_r1 = bi_r0.xor(bi_passwd);
    return bi_r1.toString(0x10);
  }

  public static String decrypt(String data) {
    if(data == null) {
      return "";
    }
    if(data.length() == 0) {
      return "";
    }
    BigInteger bi_confuse = new BigInteger("0933910847463829232312312");
    BigInteger bi_r1 = new BigInteger(data, 0x10);
    BigInteger bi_r0 = bi_r1.xor(bi_confuse);
    return new String(bi_r0.toByteArray());
  }

  public static String GetCurrentVersion(Context context) {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch(Exception localException1) {
    }
    return "";
  }
}
