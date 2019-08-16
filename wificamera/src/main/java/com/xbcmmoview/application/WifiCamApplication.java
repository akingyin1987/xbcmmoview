package com.xbcmmoview.application;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

import com.akingyin.wificamera.R;
import com.sosocam.rcipcam3x.RCIPCam3X;
import com.wingedcam.ipcam.IPCam;
import com.wingedcam.storage.Storage;

import com.xbcmmoview.tools.CrashHandler;
import java.lang.ref.WeakReference;

public class WifiCamApplication {
  public static final String SILENAME = "Setting";
  static AudioManager am;
  private static int back_adjust = 0;
  private static int draw_line_height = 0;
  private static int f_max = 166;
  private static int f_min = 90;
  private static int flipStatus = 0;
  private static int front_adjust = 0;
  private static int h_mid = 128;
  private static int left_fly_distance = 0;
  private static int left_rotate = 0;
  private static String light_state = "1";
  private static SoundPool mSoundPool;
  public static IPCam m_cam = null;
  private static String m_ssid = "";
  public static int max_fps = 0;
  private static String preview_720p = "0";
  private static String prompt_tone = "0";
  private static int r_mid = 128;
  public static int resulotion_cap = 0;
  private static int right_fly_distance = 0;
  private static String right_hand = "0";
  private static int right_rotate = 0;
  private static int rotate_mid = 128;
  private static String save_param = "0";
  static SharedPreferences setPreference;
  private static int soundId_1;
  private static int soundId_2;
  private static int soundId_3;
  private static int v_mid = 128;
  private CrashHandler crashHandler;
  private WeakReference<Context>  mContextWeakReference = null;

  private WifiCamApplication(Context  context) {
    mContextWeakReference = new WeakReference<>(context.getApplicationContext());
    Storage.init(context);
    RCIPCam3X.Init();
    Log.e("flying", "application create");
    Storage.add_device("MaxFpsValue", "MaxFpsValue", "MaxFpsValue", "MaxFpsValue",
        "MaxFpsValue", "MaxFpsValue");
    getSetSave();
    initSound();
  }

  private volatile static WifiCamApplication singleton;

  public static WifiCamApplication getInstance(Context  context) {
    if (singleton == null) {
      synchronized (WifiCamApplication.class) {
        if (singleton == null) {
          singleton = new WifiCamApplication(context);
        }
      }
    }
    return singleton;
  }
      public static int getFlipStatus() {
        return flipStatus;
      }

      public static void setFlipStatus ( int flipStatus){
        WifiCamApplication.flipStatus = flipStatus;
      }

      public static String getLight_state () {
        return light_state;
      }

      public static void setLight_state (String light_state){
        WifiCamApplication.light_state = light_state;
      }

      public static int getDraw_line_height () {
        return draw_line_height;
      }

      public static void setDraw_line_height ( int draw_line_height){
        draw_line_height = draw_line_height;
      }

      public void initSound () {
        mSoundPool = new SoundPool(1, 1, 0);
        soundId_1 = mSoundPool.load(mContextWeakReference.get(), R.raw.capture, 1);
        soundId_2 = mSoundPool.load(mContextWeakReference.get(), R.raw.btn_turn, 1);
        soundId_3 = mSoundPool.load(mContextWeakReference.get(), R.raw.center_sound, 1);
        mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
          @Override
          public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
          }
        });
        if(null != mContextWeakReference && null != mContextWeakReference.get()){
          am = (AudioManager) mContextWeakReference.get().getSystemService(Context.AUDIO_SERVICE);
        }

      }

      public static void getSound ( int tag){
        Log.e("Application", "getPrompt_tone()：" + getPrompt_tone());
        float volumnRatio = ((float) am.getStreamVolume(1)) / ((float) am.getStreamMaxVolume(1));
        if (!getPrompt_tone().equals("1")) {
          return;
        }
        if (tag == 1) {
          mSoundPool.play(soundId_1, volumnRatio, volumnRatio, 0, 0, 1.0f);
        } else if (tag == 2) {
          mSoundPool.play(soundId_2, volumnRatio, volumnRatio, 0, 0, 1.0f);
        } else if (tag == 3) {
          mSoundPool.play(soundId_3, volumnRatio, volumnRatio, 0, 0, 1.0f);
        }
      }

      public static int getRight_rotate () {
        return right_rotate;
      }

      public static void setRight_rotate ( int right_rotate){
        right_rotate = right_rotate;
      }

      public static int getLeft_rotate () {
        return left_rotate;
      }

      public static void setLeft_rotate ( int left_rotate){
        left_rotate = left_rotate;
      }

      public static int getR_mid () {
        return r_mid;
      }

      public static void setR_mid ( int r_mid){
        Log.e("Application", "r_mid：" + r_mid);
        r_mid = r_mid;
      }

      public static int getF_max () {
        return f_max;
      }

      public static void setF_max ( int f_max){
        Log.e("Application", "f_max：" + f_max);
        f_max = f_max;
      }

      public static int getF_min () {
        return f_min;
      }

      public static void setF_min ( int f_min){
        Log.e("Application", "f_min：" + f_min);
        f_min = f_min;
      }

      public static int getBack_adjust () {
        return back_adjust;
      }

      public static void setBack_adjust ( int back_adjust){
        back_adjust = back_adjust;
      }

      public static int getFront_adjust () {
        return front_adjust;
      }

      public static void setFront_adjust ( int front_adjust){
        front_adjust = front_adjust;
      }

      public static int getLeft_fly_distance () {
        return left_fly_distance;
      }

      public static void setLeft_fly_distance ( int left_fly_distance){
        left_fly_distance = left_fly_distance;
      }

      public static int getRight_fly_distance () {
        return right_fly_distance;
      }

      public static void setRight_fly_distance ( int right_fly_distance){
        right_fly_distance = right_fly_distance;
      }

      public static int getRotate_mid () {
        return rotate_mid;
      }

      public static void setRotate_mid ( int rotate_mid){
        rotate_mid = rotate_mid;
      }

      public static int getV_mid () {
        return v_mid;
      }

      public static void setV_mid ( int v_mid){
        Log.e("Application", "v_mid：" + v_mid);
        v_mid = v_mid;
      }

      public static int getH_mid () {
        return h_mid;
      }

      public static void setH_mid ( int h_mid){
        h_mid = h_mid;
      }

      public static String getPrompt_tone () {
        return prompt_tone;
      }

      public static void setPrompt_tone (String prompt_tone){
        prompt_tone = prompt_tone;
        Log.e("Application", "prompt_tone：" + prompt_tone);
      }

      public static String getRight_hand () {
        return right_hand;
      }

      public static void setRight_hand (String right_hand){
        right_hand = right_hand;
      }

      public static String getSave_param () {
        return save_param;
      }

      public static void setSave_param (String save_param){
        save_param = save_param;
      }

      public static String getPreview_720p () {
        return preview_720p;
      }

      public static void setPreview_720p (String preview_720p){
        preview_720p = preview_720p;
      }



      public static IPCam create_cam () {
        Log.e("wingedcam", "create_cam: ");
        m_cam = new IPCam();
        return m_cam;
      }

      public static void set_ssid (String ssid){
        Log.e("wingedcam", "set_ssid: " + ssid);
        m_ssid = ssid;
      }

      public static String get_ssid () {
        Log.e("wingedcam", "get_ssid: " + m_ssid);
        return m_ssid;
      }

      public void exit () {
        Log.e("wingedcam", "exit");
        setSave();
        RCIPCam3X.Deinit();

      }

      private void setSave () {
        Editor setEdit = setPreference.edit();
        setEdit.putString("right_hand", getRight_hand());
        setEdit.putString("save_param", getSave_param());
        setEdit.putString("preview_720p", getPreview_720p());
        setEdit.putString("prompt_tone", "1");
        setEdit.putString("light_state", getLight_state());
        setEdit.putInt("h_mid", getH_mid());
        setEdit.putInt("v_mid", getV_mid());
        setEdit.putInt("r_mid", getR_mid());
        setEdit.putInt("rotate_mid", getRotate_mid());
        setEdit.putInt("right_fly_distance", getRight_fly_distance());
        setEdit.putInt("left_fly_distance", getLeft_fly_distance());
        setEdit.putInt("front_adjust", getFront_adjust());
        setEdit.putInt("back_adjust", getBack_adjust());
        setEdit.putInt("left_rotate", getLeft_rotate());
        setEdit.putInt("right_rotate", getRight_rotate());
        setEdit.putInt("f_min", getF_min());
        setEdit.putInt("f_max", getF_max());
        setEdit.commit();
      }

      public void getSetSave () {
        if(null == mContextWeakReference || null == mContextWeakReference.get()){
          return;
        }
        setPreference = mContextWeakReference.get().getSharedPreferences(SILENAME, 0);
        right_hand = setPreference.getString("right_hand", "0");
        save_param = setPreference.getString("save_param", "0");
        preview_720p = setPreference.getString("preview_720p", "0");
        prompt_tone = setPreference.getString("prompt_tone", "1");
        if (save_param.equals("1")) {
          h_mid = setPreference.getInt("h_mid", 128);
          v_mid = setPreference.getInt("v_mid", 128);
          r_mid = setPreference.getInt("r_mid", 128);
          right_fly_distance = setPreference.getInt("right_fly_distance", 0);
          left_fly_distance = setPreference.getInt("left_fly_distance", 0);
          front_adjust = setPreference.getInt("front_adjust", 0);
          back_adjust = setPreference.getInt("back_adjust", 0);
          left_rotate = setPreference.getInt("left_rotate", 0);
          right_rotate = setPreference.getInt("right_rotate", 0);
          light_state = setPreference.getString("light_state", "1");
        }
        f_min = setPreference.getInt("f_min", 85);
        f_max = setPreference.getInt("f_max", 170);
      }

      public static void getByte ( byte[] reqBytes){
        for (byte b : reqBytes) {
          String hex = Integer.toHexString(b & 255);
          if (hex.length() == 1) {
            hex = '0' + hex;
          }
          Log.e("shuchu", "输出:" + hex.toUpperCase());
        }
      }
    }
