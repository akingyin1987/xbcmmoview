package com.xbcmmoview.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sosocam.rcipcam3x.DISCOVERED_CAMERA_INFO;
import com.sosocam.rcipcam3x.DiscoverListener;
import com.sosocam.rcipcam3x.RCIPCam3X;
import com.wingedcam.ipcam.IPCam;
import com.wingedcam.ipcam.IPCamVideoView;
import com.wingedcam.storage.Storage;
import com.wingedcam.util.HttpClient;
import com.wingedcam.util.ThumbnailUtils;
import com.wingedcam.util.Tools;
import com.xbcmmoview.application.WifiCamApplication;
import com.xbcmmoview.tools.SpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Name: BaseWifiCameraActivity
 * Author: akingyin
 * Email:
 * Comment: //TODO
 * Date: 2019-08-14 20:23
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class BaseWifiCameraActivity  extends FragmentActivity  implements IPCamVideoView.IPCamVideoView_Listener, IPCam.IPCam_Listener, DiscoverListener, IPCam.get_properties_listener {


    public static IPCam cam;

    private int currentResolution = 0;

    public boolean isRecording = false;

    private String mBSSID = null;





    class WifiStatusReceiver extends BroadcastReceiver {
        WifiStatusReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (!info.getState().equals(NetworkInfo.State.DISCONNECTED) && info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiInfo wifiInfo = ((WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
                    if (BaseWifiCameraActivity.this.mBSSID != null && null != wifiInfo) {
                        String bssid = wifiInfo.getBSSID();
                        if (bssid != null && !bssid.equals(BaseWifiCameraActivity.this.mBSSID)) {
                            restartApp(intent);
                            return;
                        }
                        return;
                    }
                    if(null != wifiInfo){
                        mBSSID = wifiInfo.getBSSID();
                    }

                }
            } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction())) {
                switch (intent.getIntExtra("wifi_state", 0)) {
                    case 1:
                        BaseWifiCameraActivity.this.mBSSID = null;
                        Toast.makeText(context, "没有网络连接，请检查WIFI", Toast.LENGTH_SHORT).show();
                        return;
                    default:
                        return;
                }
            }
        }

        private void restartApp(Intent intent1) {
            wifi_init();

        }
    }

    private static boolean is_camera_ap(String ssid) {
        return true;
    }

    private    long    startSearchWifiTime =0L;
    public boolean wifi_init() {
        WifiInfo info = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        if (info != null) {
            String news = info.getSSID().replace("\"", "");
            boolean exsit = is_camera_ap(news);
            Log.e("-----------exsit--->>> ", "ssid: " + news + "" + exsit + " cam.status():" + cam.status());
            if (exsit) {
                WifiCamApplication.set_ssid(news);
                if (cam != null && cam.status() == IPCam.CONN_STATUS.IDLE) {
                    Log.e("-----------exsit--->>> ", "StartDiscoverCameras: ");
                    RCIPCam3X.StartDiscoverCameras(this);
                    startSearchWifiTime = System.currentTimeMillis();
                    UI_Handler.sendEmptyMessageDelayed(106,5000);
                } else if (cam == null || cam.status() != IPCam.CONN_STATUS.CONNECTED) {
                }
            }
        }
        return true;
    }


    private IPCamVideoView video_view4;
    private ExecutorService threadPool;
    private  String   localPath="";
    private WifiStatusReceiver wifiStatusReceiver;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UI_Handler = new MyHandler(this);
        threadPool = Executors.newFixedThreadPool(3);
        localPath = getIntent().getStringExtra("localPath");
        registerWifiReceiver();
    }



    public void screen_single() {
        Log.e("log", "screen_single: ");
        this.video_view4.setVisibility(View.VISIBLE);
        cam.set_video_view(this.video_view4, null);
    }


    private void get_model() {
        if (cam != null) {
            cam.get_properties("model=", new IPCam.get_properties_listener() {
                @Override
                public void on_result(IPCam ipcam, IPCam.ERROR error, JSONObject json) {
                    Log.e("log", "josn: " + json);
                    try {
                        cam.model = json.getInt("model");
                        set_reslution();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void set_reslution() {
        if (cam != null) {
            if (20 == cam.sensor_id) {
                this.currentResolution = SpUtil.getInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4)).intValue();
                if (!(4 == this.currentResolution || 5 == this.currentResolution)) {
                    this.currentResolution = 4;
                    SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4));
                }
            } else if (this.currentResolution <= 0) {
                return;
            }
            if (cam.model != 0) {
                int set_resolution = this.currentResolution;
                if (5 == this.currentResolution) {
                    set_resolution = 4;
                }
                cam.set_params("resolution=" + set_resolution + "&reinit_camera=1&save=1", new IPCam.set_params_listener() {
                    public void on_result(IPCam ipcam, IPCam.ERROR error) {
                        if (error == IPCam.ERROR.NO_ERROR) {
                            Log.e("TAG", "cam.model:" + cam.model + "-设置成功");
                            Log.e("TAG", "分辨率：" + SpUtil.getInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(6)));
                            return;
                        }
                        Log.e("TAG", "设置失败");
                    }
                });
            }
        }
    }

    @Override
    public void OnCameraDisappeared(String p1) {

         startSearchWifiTime = 0L;
    }

    @Override
    public void OnCameraDiscovered(DISCOVERED_CAMERA_INFO info) {
           startSearchWifiTime = 0L;
           if(cam.status() == IPCam.CONN_STATUS.IDLE){
               cam.set_id(info.id);
               cam.set_user("admin");
               cam.set_pwd("");
               cam.model = info.model;
               cam.sensor_id = info.sensor_id;
               Log.e("software ", "cam.sensor_id :" + cam.sensor_id);
               if (cam.sensor_id == 20) {
                   RCIPCam3X.SetVideoScaleFlag(true);
               }
               cam.update_lan_status(true, info.current_ip, info.port, info.https);
               cam.add_listener(this);
               cam.start_connect(true, 5);
           }      




    }

    @Override
    public void OnCameraUpdated(DISCOVERED_CAMERA_INFO p1) {
        startSearchWifiTime = 0L;

    }

    @Override
    public void on_audio_status_changed(IPCam iPCam) {

    }

    @Override
    public void on_camera_alarm_ioout(IPCam iPCam) {
        WifiCamApplication.getSound(1);
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
             Bitmap image = HttpClient.get_image("http://" + cam.ip() + ":" + cam.port() + "/alarm_snapshots.cgi?number=1&user=" + cam.user() + "&pwd=" + cam.pwd());
             if(null != image){
                 try {
                     FileOutputStream out = new FileOutputStream(new File(localPath));
                     image.compress(Bitmap.CompressFormat.JPEG, 100, out);
                     out.flush();
                     out.close();
                     Message   message = UI_Handler.obtainMessage();
                     message.what=1;
                     message.obj = localPath;
                     UI_Handler.sendMessage(message);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }

            }
        });

    }

    @Override
    public void on_camera_battery_changed(IPCam iPCam) {

    }

    @Override
    public void on_camera_recording_changed(IPCam iPCam) {

    }

    @Override
    public void on_camera_tf_changed(IPCam iPCam) {

    }

    @Override
    public void on_camera_wifi_changed(IPCam iPCam) {

    }

    @Override
    public void on_can_set_video_performance(IPCam iPCam) {

    }

    @Override
    public void on_local_record_result(IPCam iPCam, IPCam.ERROR error) {

        IPCam.PLAY_STATUS play_status = iPCam.local_record_status();
        if (error == IPCam.ERROR.NO_ERROR && iPCam.local_record_status() == IPCam.PLAY_STATUS.PLAYING) {
            this.isRecording = true;

            //Tools.showShortToast(this, getString(R.string.record));

            return;
        }

        this.isRecording = false;

    }

    @Override
    public void on_speak_status_changed(IPCam iPCam) {

    }

    @Override
    public void on_statistic(IPCam iPCam) {

    }

    @Override
    public void on_status_changed(IPCam iPCam) {
        Log.e("log", "on_status_changed: " + iPCam.status());
        if (iPCam.status() == IPCam.CONN_STATUS.CONNECTED) {
            this.UI_Handler.sendEmptyMessage(101);
            iPCam.set_cache(0);
        } else if (iPCam.status() == IPCam.CONN_STATUS.IDLE || iPCam.error() == IPCam.ERROR.BAD_AUTH) {
            this.UI_Handler.sendEmptyMessage(103);
            if (this.isRecording) {
                this.isRecording = false;
                if (cam.sensor_id == 20) {

                }
            }
        }
    }

    @Override
    public void on_tf_record_event(IPCam iPCam, boolean z, int i, boolean z2) {

    }

    @Override
    public void on_tf_record_status_changed(IPCam iPCam) {

    }

    @Override
    public void on_video_status_changed(IPCam iPCam) {

    }

    @Override
    public void on_result(IPCam iPCam, IPCam.ERROR error, JSONObject jSONObject) throws JSONException {
        WifiCamApplication.resulotion_cap = jSONObject.getInt("resolution_capability");
    }

    @Override
    public void on_touch_event(TOUCH_EVENT touch_event) {

    }

    private   MyHandler  UI_Handler = null;

    static class MyHandler extends Handler {
        WeakReference<BaseWifiCameraActivity > mActivityReference;
        MyHandler(BaseWifiCameraActivity activity) {
            mActivityReference= new WeakReference<BaseWifiCameraActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
             BaseWifiCameraActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case 100:
                        Log.e("log", "TIME_UPDATE: ");
                        return;
                    case 101:
                        Log.e("log", "CONNECTED: ");
                        cam.set_album_folder(Storage.get_album_folder());
                        cam.play_video(0);
                        Log.e("log", "no3D: ");
                         activity.screen_single();
                         activity.get_model();
                        activity.adjustTime();
                        cam.get_properties("resolution_capability=", activity);
                        cam.add_listener(activity);
                        return;
                    case 102:
                        Log.e("log", "CONNECTING: ");
                        return;
                    case 103:
                        Log.e("log", "DISCONNECT: ");
                        activity.wifi_init();
                        return;
                    case 104:
                        Log.e("log", "PLAY: ");
                        return;
                    case 105:
                        Log.e("log", "STOP: ");
                        activity.wifi_init();
                        return;
                    case 106:
                        if(activity.startSearchWifiTime>0 && System.currentTimeMillis() - activity.startSearchWifiTime>5000){
                            Toast.makeText(activity, WifiCamApplication.get_ssid()+"无法连接到摄像头",Toast.LENGTH_SHORT).show();
                            activity.showWifiDialog();
                        }
                        activity.startSearchWifiTime = 0;

                        break;
                    case 1:
                         Tools.add_media(activity,"拍照成功");
                        break;
                    default:
                        return;
                }
            }
        }
    }


    private void takingPictures() {
        if (cam.status() == IPCam.CONN_STATUS.CONNECTED) {
            String path="";
            Log.e("photo", "拍照----------------------: ");
            WifiCamApplication.getSound(1);
            if (20 != cam.sensor_id) {
                path = cam.snapshot();
            } else if (4 == this.currentResolution) {
                path = cam.snapshot_new(ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, 240);
            } else {
                path = cam.snapshot_new(640, 480);
            }
            System.out.println("path="+path);
            Tools.add_media(this, path);
        }
    }

    public  boolean adjustTime() {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                String url = "http://192.168.1.1:80/set_params.cgi?json=1&save=1&update_tz=1&tz=" + ((-TimeZone.getDefault().getRawOffset()) / IPCam.PLAY_TF_RECORD_CACHE_NORMAL) + "&clock=" + String.format("%.3f", new Object[]{Float.valueOf(((float) System.currentTimeMillis()) / 1000.0f)}) + "&user=" + "admin" + "&pwd=" + "";
                Log.d("adjustTime", "TimeUrl:" + url);
                HttpClient.get_json(url);
            }
        });
        return true;
    }


    private List<IPCam.IPCam_Listener> m_listener_list = new ArrayList();
    public void add_listener(IPCam.IPCam_Listener listener) {
        if (!this.m_listener_list.contains(listener)) {
            this.m_listener_list.add(listener);
        }
    }

    public   void   showWifiDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("连接摄像头WIFI失败");
        builder.setMessage("是否进入wifi设置，或退出当前操作!");
        builder.setPositiveButton("wifi设置", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }
    private void registerWifiReceiver() {
        this.wifiStatusReceiver = new WifiStatusReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(this.wifiStatusReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("close", "onResume: ");
        wifi_init();
        Log.e("status", "status:" + cam.status());
        if (cam != null && cam.status() == IPCam.CONN_STATUS.CONNECTED) {
            if (cam.video_status() == IPCam.PLAY_STATUS.STOPPED) {
                cam.play_video(0);
            }
            cam.set_album_folder(Storage.get_album_folder());
            cam.set_video_view(this.video_view4, null);
        }
    }

    @Override protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.e("close", "onStop: ");
        if (cam != null && this.isRecording) {

            if (this.isRecording) {
                this.isRecording = false;
                if (cam.sensor_id == 20) {

                }
            }
            cam.stop_local_record();

        }
        cam.set_video_view(null, null);
        cam.remove_listener(this);
        cam.stop_video();
        cam.stop_connect();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.e("close", "onPause: ");
        cam.set_video_view(null, null);
        RCIPCam3X.StopDiscoverCameras();
        if (cam != null && this.isRecording) {

            if (this.isRecording) {
                this.isRecording = false;
                if (cam.sensor_id == 20) {

                }
            }
            cam.stop_local_record();

        }
    }

    @Override
    protected void onDestroy() {
        cam.set_video_view(null, null);
        cam.remove_listener(this);
        cam.stop_video();
        cam.stop_connect();

        unregisterReceiver(wifiStatusReceiver);
        Log.e("close", "onDestroy: ");
        super.onDestroy();
    }
}
