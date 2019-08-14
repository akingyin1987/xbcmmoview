package com.xbcmmoview.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.AnimationDrawable;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.sosocam.rcipcam3x.DISCOVERED_CAMERA_INFO;
import com.sosocam.rcipcam3x.DiscoverListener;
import com.sosocam.rcipcam3x.RCIPCam3X;
import com.wingedcam.ipcam.IPCam;
import com.wingedcam.ipcam.IPCam.CONN_STATUS;
import com.wingedcam.ipcam.IPCam.ERROR;
import com.wingedcam.ipcam.IPCam.IPCam_Listener;
import com.wingedcam.ipcam.IPCam.PLAY_STATUS;
import com.wingedcam.ipcam.IPCam.VedioData_Listener;
import com.wingedcam.ipcam.IPCam.get_properties_listener;
import com.wingedcam.ipcam.IPCam.set_params_listener;
import com.wingedcam.ipcam.IPCamVideoView;
import com.wingedcam.ipcam.IPCamVideoView.IPCamVideoView_Listener;
import com.wingedcam.storage.Storage;
import com.wingedcam.util.HttpClient;
import com.wingedcam.util.Tools;
import com.xbcmmoview.R;
import com.xbcmmoview.application.WingedCamApplication;
import com.xbcmmoview.dialog.ExitAppDialog;
import com.xbcmmoview.tools.AvcEncoder;
import com.xbcmmoview.tools.SpUtil;
import io.vov.vitamio.MediaMetadataRetriever;
import io.vov.vitamio.ThumbnailUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ShowActivity extends Activity implements IPCamVideoView_Listener, IPCam_Listener, DiscoverListener, get_properties_listener, OnClickListener, OnItemSelectedListener {
    private static final int SDK_PERMISSION_ALBUM = 1004;
    private static final int SDK_PERMISSION_FIRST_LAUNCH = 1001;
    private static final int SDK_PERMISSION_PICTURE = 1002;
    private static final int SDK_PERMISSION_RECORD = 1003;
    public static IPCam cam;
    private final int CONNECTED = 101;
    private final int CONNECTING = 102;
    private final int DISCONNECT = 103;
    private final int PLAY = 104;
    final int SENSOR_ID_A3399_GC0308 = 7;
    final int SENSOR_ID_A3399_OV7725 = 4;
    final int SENSOR_ID_A3399_OV7740 = 6;
    final int SENSOR_ID_A3399_PAS6371 = 9;
    final int SENSOR_ID_A3518_7676 = 18;
    final int SENSOR_ID_AU3861_PAS6375 = 12;
    final int SENSOR_ID_GM8125_OV7725 = 2;
    final int SENSOR_ID_GM8126_HM1375 = 10;
    final int SENSOR_ID_GM8126_OV9715 = 1;
    final int SENSOR_ID_HI3518_AR0310 = 15;
    final int SENSOR_ID_HI3518_GC0308 = 16;
    final int SENSOR_ID_HI3518_H42 = 17;
    final int SENSOR_ID_HI3518_H62 = 19;
    final int SENSOR_ID_HI3518_OV9712 = 14;
    final int SENSOR_ID_PAP7501V_PAS6371 = 3;
    final int SENSOR_ID_SM3732_GC0309 = 11;
    final int SENSOR_ID_SM3732_HM2057 = 13;
    final int SENSOR_ID_SN9C270A_OV9716 = 8;
    final int SENSOR_ID_SN9C291_OV9712 = 5;
    final int SENSOR_ID_UNKNOWN = 0;
    final int SENSOR_ID_YUYV = 20;
    private final int STOP = 105;
    private final int TIME_UPDATE = 100;
    private Handler UI_Handler;
    private AnimationDrawable animationDrawable;
    private ImageView animationIV;
    public WingedCamApplication app;
    private AvcEncoder avcCodec;
    private Button btMore;
    private Button btnReboot;
    private Button btnResetPwd;
    private Button btnSettingPwd;
    private Button btnSettingSSid;
    private String control_interface = "0";
    private int cur_stream = 0;
    private int currentResolution = 0;
    private EditText editInputPwd;
    private EditText editInputSsid;
    private final String expire_date = "2018-01-15";
    private boolean is3D = false;
    public boolean isRecording = false;
    private ImageView iv_videoview_rotation;
    private LinearLayout layout_more_setting;
    private RelativeLayout live_layout;
    private LinearLayout ll_camera_turn;
    private String m;
    private String mBSSID = null;
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";
        String SYSTEM_REASON = "reason";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                String reason = intent.getStringExtra(this.SYSTEM_REASON);
                if (TextUtils.equals(reason, this.SYSTEM_HOME_KEY)) {
                    if (ShowActivity.cam != null && ShowActivity.this.isRecording) {
                        ShowActivity.this.video.setImageResource(R.drawable.live_record);
                        ShowActivity.this.isRecording = false;
                        if (ShowActivity.cam.sensor_id == 20) {
                            ShowActivity.this.stopRecord();
                        }
                        ShowActivity.cam.stop_local_record();
                        ShowActivity.this.video_time.setVisibility(View.GONE);
                        ShowActivity.this.StopTimerTask();
                        Tools.add_media(ShowActivity.this, ShowActivity.this.m_local_record_filepath);
                    }
                    Process.killProcess(Process.myPid());
                    System.exit(0);
                    return;
                }
                if (TextUtils.equals(reason, this.SYSTEM_HOME_KEY_LONG)) {
                }
            }
        }
    };
    private String m_local_record_filepath;
    private int min = 0;
    private ImageView photo;
    private ImageView pictures;
    private TextView reslution_cap_label;
    private int reslution_model = 2;
    private String right_hand = "0";
    private String s;
    private int sec = 0;
    private ImageView setting;
    private int speed_index = 1;
    private Spinner spinner_resolution_select;
    private Timer timer;
    private ImageView video;
    private TextView video_time;
    private IPCamVideoView video_view4;
    private RelativeLayout welcome_layout;
    private WifiStatusReceiver wifiStatusReceiver;

    class WifiStatusReceiver extends BroadcastReceiver {
        WifiStatusReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (!info.getState().equals(State.DISCONNECTED) && info.getState().equals(State.CONNECTED)) {
                    WifiInfo wifiInfo = ((WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
                    if (ShowActivity.this.mBSSID != null && null != wifiInfo) {
                        String bssid = wifiInfo.getBSSID();
                        if (bssid != null && !bssid.equals(ShowActivity.this.mBSSID)) {
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
                        ShowActivity.this.mBSSID = null;
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

    public class snapshot_thread extends Thread {
        private Context context;
        private String host;
        private int port;
        private String pwd;
        private String url;
        private String user;

        public snapshot_thread(Context m_context, String m_host, int m_port, String m_user, String m_pwd) {
            this.host = m_host;
            this.port = m_port;
            this.user = m_user;
            this.pwd = m_pwd;
            this.context = m_context;
            this.url = "http://" + this.host + ":" + this.port + "/snapshot.cgi?resolution=" + ShowActivity.this.currentResolution + "&user=" + this.user + "&pwd=" + this.pwd;
        }

        @Override
        public void run() {
            Log.e("photo", "1 snapshot" + this.url);
            Bitmap image = HttpClient.get_image(this.url);
            Log.e("photo", "2 snapshot image" + image);
            if (image != null) {
                String image_path = Storage.get_album_folder() + "/" + DateFormat.format("yyyyMMddkkmmss", System.currentTimeMillis()) + ".jpg";
                try {
                    FileOutputStream out = new FileOutputStream(new File(image_path));
                    image.compress(CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    Log.e("photo", "image_path" + image_path);
                    if (this.context != null) {
                        Tools.add_media(this.context, image_path);
                    }
                } catch (Exception e) {
                    Log.e("photo", "image_path failed");
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(128, 128);
        setContentView(R.layout.activity_show);
        this.app = (WingedCamApplication) getApplicationContext();
        WingedCamApplication wingedCamApplication = this.app;
        cam = WingedCamApplication.create_cam();
        welcome_layout = (RelativeLayout) findViewById(R.id.welcome_view);
        //new Handler().postDelayed(new Runnable() {
        //    public void run() {
        //        ShowActivity.this.welcome_layout.setVisibility(View.INVISIBLE);
        //    }
        //}, 2500);
        //final LinearLayout ll_bg = (LinearLayout) findViewById(R.id.bg_ll);
        //new Timer().schedule(new TimerTask() {
        //    public void run() {
        //        ShowActivity.this.runOnUiThread(new Runnable() {
        //            public void run() {
        //                ll_bg.setVisibility(View.GONE);
        //            }
        //        });
        //    }
        //}, 1500);
        app.initSound();
        initView();
        registerWifiReceiver();
        registerReceiver(this.mHomeKeyEventReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        Date date_start = new Date();
        Date date_end = null;
        try {
            date_end = new SimpleDateFormat("yyyy-MM-dd").parse("2018-01-15");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time_start = date_start.getTime();
        long time_end = date_end.getTime();
        Log.e(MediaMetadataRetriever.METADATA_KEY_DATE, "start: " + date_start.getTime() + " end: " + date_end.getTime());
        this.UI_Handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.e("log", "msg: " + msg.what);
                System.out.println("--------------handleMessage: " + msg.what);
                switch (msg.what) {
                    case 100:
                        Log.e("log", "TIME_UPDATE: ");
                        ShowActivity.this.video_time.setText(ShowActivity.this.m + ":" + ShowActivity.this.s);
                        return;
                    case 101:
                        Log.e("log", "CONNECTED: ");
                        ShowActivity.cam.set_album_folder(Storage.get_album_folder());
                        ShowActivity.cam.play_video(0);
                        Log.e("log", "no3D: ");
                        ShowActivity.this.screen_single();
                        ShowActivity.this.get_model();
                        ShowActivity.adjustTime();
                        ShowActivity.cam.get_properties("resolution_capability=", ShowActivity.this);
                        ShowActivity.cam.add_listener(ShowActivity.this);
                        return;
                    case 102:
                        Log.e("log", "CONNECTING: ");
                        return;
                    case 103:
                        Log.e("log", "DISCONNECT: ");
                        ShowActivity.this.wifi_init();
                        return;
                    case 104:
                        Log.e("log", "PLAY: ");
                        return;
                    case 105:
                        Log.e("log", "STOP: ");
                        ShowActivity.this.wifi_init();
                        return;
                    case 106:
                          if(startSearchWifiTime>0 && System.currentTimeMillis() - startSearchWifiTime>5000){
                              Toast.makeText(ShowActivity.this,WingedCamApplication.get_ssid()+"无法连接到摄像头",Toast.LENGTH_SHORT).show();
                              showWifiDialog();
                          }
                          startSearchWifiTime = 0;

                        break;
                    default:
                        return;
                }
            }
        };
    }

  private void startRecord() {
        int currentResolution = SpUtil.getInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4)).intValue();
        if (!(4 == currentResolution || 5 == currentResolution)) {
            SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4));
            currentResolution = 4;
            get_model();
        }
        if (4 == currentResolution) {
            this.avcCodec = new AvcEncoder(ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, 240, ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, 240, 30, 4000000);
        } else {
            this.avcCodec = new AvcEncoder(ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, 240, 640, 480, 30, 8000000);
        }
        cam.newVedioDataListen(new VedioData_Listener() {
            public void on_get_video_data_ARGB(int camera, byte[] yuvdata, int width, int height, int tick) {

                ShowActivity.this.avcCodec.setVedioData(camera, yuvdata, tick);
            }
        });
        Log.e("record", "avcCodec.isRuning()   ：" + this.avcCodec.isRuning());
        this.avcCodec.createfile();
        if (!this.avcCodec.isRuning()) {
            this.avcCodec.StartEncoderThread();
        }
    }

    private void stopRecord() {
        Log.e("record", "avcCodec.isRuning()   ：" + this.avcCodec.isRuning());
        this.avcCodec.StopThread();
        cam.newVedioDataListen(null);
        this.avcCodec = null;
    }

    private void registerWifiReceiver() {
        this.wifiStatusReceiver = new WifiStatusReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(this.wifiStatusReceiver, intentFilter);
    }

    @TargetApi(23)
    private void showGetPermissionDialog(final int requestCode) {
        Builder builder = new Builder(this);
        builder.setTitle("提示");
        builder.setMessage("应用需要先获取SD卡的读取和存储权限，才能保存照片和录像信息");
        builder.setNegativeButton("同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ShowActivity.this.shouldShowRequestPermissionRationale("android.permission.WRITE_EXTERNAL_STORAGE")) {
                    ShowActivity.this.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, requestCode);
                    return;
                }
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", ShowActivity.this.getPackageName(), null));
                ShowActivity.this.startActivity(intent);
            }
        });
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private void initView() {
        this.video_view4 = (IPCamVideoView) findViewById(R.id.VideoView1);
        this.pictures = (ImageView) findViewById(R.id.pictures);
        this.video = (ImageView) findViewById(R.id.video);
        this.photo = (ImageView) findViewById(R.id.photo);
        this.video_time = (TextView) findViewById(R.id.video_time);
        this.setting = (ImageView) findViewById(R.id.setting);
        this.video_view4.set_keep_image_radio(false);
        this.spinner_resolution_select = (Spinner) findViewById(R.id.spinner_resolution_select);
        this.ll_camera_turn = (LinearLayout) findViewById(R.id.ll_camera_turn);
        this.iv_videoview_rotation = (ImageView) findViewById(R.id.iv_videoview_rotation);
        this.spinner_resolution_select.setOnItemSelectedListener(this);
        this.layout_more_setting = (LinearLayout) findViewById(R.id.more_setting);
        this.btMore = (Button) findViewById(R.id.bt_more);
        this.btnSettingSSid = (Button) findViewById(R.id.setting_ssid);
        this.btnSettingPwd = (Button) findViewById(R.id.setting_pwd);
        this.btnResetPwd = (Button) findViewById(R.id.clear_pwd);
        this.btnReboot = (Button) findViewById(R.id.reboot);
        this.editInputSsid = (EditText) findViewById(R.id.input_ssid);
        this.editInputPwd = (EditText) findViewById(R.id.input_pwd);
        this.reslution_cap_label = (TextView) findViewById(R.id.reslution_cap_label);
        this.iv_videoview_rotation.setOnClickListener(this);
        this.pictures.setOnClickListener(this);
        this.video.setOnClickListener(this);
        this.photo.setOnClickListener(this);
        this.video_view4.set_listener(this);
        this.setting.setOnClickListener(this);
        this.btMore.setOnClickListener(this);
        this.btnSettingSSid.setOnClickListener(this);
        this.btnSettingPwd.setOnClickListener(this);
        this.btnResetPwd.setOnClickListener(this);
        this.btnReboot.setOnClickListener(this);
    }

    private void distanceMove(RelativeLayout layout, ImageView imageView, LayoutParams layoutParams) {
        layout.removeView(imageView);
        imageView.setLayoutParams(layoutParams);
        layout.addView(imageView);
    }

    public void screen_single() {
        Log.e("log", "screen_single: ");
        this.video_view4.setVisibility(View.VISIBLE);
        cam.set_video_view(this.video_view4, null);
    }

    private void get_model() {
        if (cam != null) {
            cam.get_properties("model=", new get_properties_listener() {
                @Override
                public void on_result(IPCam ipcam, ERROR error, JSONObject json) {
                    Log.e("log", "josn: " + json);
                    try {
                        ShowActivity.cam.model = json.getInt("model");
                        ShowActivity.this.set_reslution();
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
                cam.set_params("resolution=" + set_resolution + "&reinit_camera=1&save=1", new set_params_listener() {
                    public void on_result(IPCam ipcam, ERROR error) {
                        if (error == ERROR.NO_ERROR) {
                            Log.e("TAG", "cam.model:" + ShowActivity.cam.model + "-设置成功");
                            Log.e("TAG", "分辨率：" + SpUtil.getInt(ShowActivity.this.getApplicationContext(), "spinner_resolution", Integer.valueOf(6)));
                            return;
                        }
                        Log.e("TAG", "设置失败");
                    }
                });
            }
        }
    }

    public static boolean adjustTime() {
        new Thread() {
            @Override
            public void run() {
                String url = "http://192.168.1.1:80/set_params.cgi?json=1&save=1&update_tz=1&tz=" + ((-TimeZone.getDefault().getRawOffset()) / IPCam.PLAY_TF_RECORD_CACHE_NORMAL) + "&clock=" + String.format("%.3f", new Object[]{Float.valueOf(((float) System.currentTimeMillis()) / 1000.0f)}) + "&user=" + "admin" + "&pwd=" + "";
                Log.d("adjustTime", "TimeUrl:" + url);
                HttpClient.get_json(url);
            }
        }.start();
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
                WingedCamApplication.set_ssid(news);
                if (cam != null && cam.status() == CONN_STATUS.IDLE) {
                    Log.e("-----------exsit--->>> ", "StartDiscoverCameras: ");
                    RCIPCam3X.StartDiscoverCameras(this);
                    startSearchWifiTime = System.currentTimeMillis();
                    UI_Handler.sendEmptyMessageDelayed(106,5000);
                } else if (cam == null || cam.status() != CONN_STATUS.CONNECTED) {
                }
            }
        }
        return true;
    }

    private static boolean is_camera_ap(String ssid) {
        return true;
    }

    protected void onResume() {
        super.onResume();
        Log.e("close", "onResume: ");
        wifi_init();
        Log.e("status", "status:" + cam.status());
        if (cam != null && cam.status() == CONN_STATUS.CONNECTED) {
            if (cam.video_status() == PLAY_STATUS.STOPPED) {
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
            this.video.setImageResource(R.drawable.live_record);
            if (this.isRecording) {
                this.isRecording = false;
                if (cam.sensor_id == 20) {
                    stopRecord();
                }
            }
            cam.stop_local_record();
            this.video_time.setVisibility(View.GONE);
            StopTimerTask();
            Tools.add_media(this, this.m_local_record_filepath);
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
            this.video.setImageResource(R.drawable.live_record);
            if (this.isRecording) {
                this.isRecording = false;
                if (cam.sensor_id == 20) {
                    stopRecord();
                }
            }
            cam.stop_local_record();
            this.video_time.setVisibility(View.GONE);
            StopTimerTask();
            Tools.add_media(this, this.m_local_record_filepath);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("close", "key: ");
        if (keyCode != 4) {
            return false;
        }
        if (cam != null && this.isRecording) {
            this.video.setImageResource(R.drawable.live_record);
            if (this.isRecording) {
                this.isRecording = false;
                if (cam.sensor_id == 20) {
                    stopRecord();
                }
            }
            cam.stop_local_record();
            this.video_time.setVisibility(View.GONE);
            StopTimerTask();
            Tools.add_media(this, this.m_local_record_filepath);
        }
        new ExitAppDialog(this).show();
        return true;
    }
    @Override
    protected void onDestroy() {
        cam.set_video_view(null, null);
        cam.remove_listener(this);
        cam.stop_video();
        cam.stop_connect();
        unregisterReceiver(mHomeKeyEventReceiver);
        unregisterReceiver(wifiStatusReceiver);
        Log.e("close", "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void on_touch_event(TOUCH_EVENT event) {
    }
    @Override
    public void on_status_changed(IPCam ipcam) {
        Log.e("log", "on_status_changed: " + ipcam.status());
        if (ipcam.status() == CONN_STATUS.CONNECTED) {
            this.UI_Handler.sendEmptyMessage(101);
            ipcam.set_cache(0);
        } else if (ipcam.status() == CONN_STATUS.IDLE || ipcam.error() == ERROR.BAD_AUTH) {
            this.UI_Handler.sendEmptyMessage(103);
            if (this.isRecording) {
                this.isRecording = false;
                if (cam.sensor_id == 20) {
                    stopRecord();
                }
            }
        }
    }
    @Override
    public void on_video_status_changed(IPCam ipcam) {
        Log.e("log", "on_video_status_changed: " + ipcam.video_status());
        if (ipcam.video_status() == PLAY_STATUS.STOPPED) {
            this.UI_Handler.sendEmptyMessage(105);
        } else if (ipcam.video_status() == PLAY_STATUS.PLAYING) {
            this.UI_Handler.sendEmptyMessage(104);
            ipcam.set_cache(0);
        }
    }
    @Override
    public void on_audio_status_changed(IPCam ipcam) {
    }
    @Override
    public void on_speak_status_changed(IPCam ipcam) {
    }
    @Override
    public void on_local_record_result(IPCam ipcam, ERROR error) {
        PLAY_STATUS play_status = ipcam.local_record_status();
        if (error == ERROR.NO_ERROR && ipcam.local_record_status() == PLAY_STATUS.PLAYING) {
            this.isRecording = true;
            StartTimerTask();
            Tools.showShortToast(this, getString(R.string.record));
            this.video_time.setVisibility(View.VISIBLE);
            return;
        }
        this.video.setImageResource(R.drawable.live_record);
        this.isRecording = false;
    }
    @Override
    public void on_tf_record_status_changed(IPCam ipcam) {
    }
    @Override
    public void on_tf_record_event(IPCam ipcam, boolean new_record, int record_id, boolean error) {
    }
    @Override
    public void on_camera_tf_changed(IPCam ipcam) {
    }
    @Override
    public void on_camera_wifi_changed(IPCam ipcam) {
        Log.e("wingedcam", "wifi signal: " + ipcam.wifi_power());
    }
    @Override
    public void on_camera_battery_changed(IPCam ipcam) {
        Log.e("wingedcam", "power signal: " + ipcam.battery_power());
    }
    @Override
    public void on_camera_alarm_ioout(IPCam ipcam) {
        WingedCamApplication.getSound(1);
        this.pictures.setBackgroundResource(R.drawable.live_photograph_active);
        this.UI_Handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap image = HttpClient.get_image("http://" + ShowActivity.cam.ip() + ":" + ShowActivity.cam.port() + "/alarm_snapshots.cgi?number=1&user=" + ShowActivity.cam.user() + "&pwd=" + ShowActivity.cam.pwd());
                if (image == null) {
                    ShowActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ShowActivity.this.pictures.setBackgroundResource(R.drawable.live_photograph);
                        }
                    });
                    return;
                }
                String image_path = Storage.get_album_folder() + "/" + DateFormat.format("yyyyMMddkkmmss", System.currentTimeMillis()) + ".jpg";
                try {
                    FileOutputStream out = new FileOutputStream(new File(image_path));
                    image.compress(CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    Tools.add_media(ShowActivity.this, image_path);
                    ShowActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ShowActivity.this.pictures.setBackgroundResource(R.drawable.live_photograph);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }
    @Override
    public void on_camera_recording_changed(IPCam ipcam) {
        PLAY_STATUS play_status = ipcam.local_record_status();
    }
    @Override
    public void on_statistic(IPCam ipcam) {
        String Text = ipcam.video_render_fps() + "/" + ipcam.video_recv_fps() + "    " + (ipcam.video_byterate() / 1024) + "." + (ipcam.video_byterate() % 1024) + "KB/s";
    }
    @Override
    public void on_can_set_video_performance(IPCam ipcam) {
    }
    @Override
    public void OnCameraDiscovered(DISCOVERED_CAMERA_INFO info) {
        startSearchWifiTime = 0L;
        Log.e("-----------exsit--->>> ", "OnCameraDiscovered: cam.status():" + cam.status());
        if (cam.status() == CONN_STATUS.IDLE) {
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
    public void OnCameraUpdated(DISCOVERED_CAMERA_INFO info) {
        startSearchWifiTime = 0L;
    }
    @Override
    public void OnCameraDisappeared(String id) {
        startSearchWifiTime = 0L;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video:
                if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                    showGetPermissionDialog(SDK_PERMISSION_RECORD);
                    return;
                } else {
                    recording();
                    return;
                }
            case R.id.photo:
                if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                    showGetPermissionDialog(SDK_PERMISSION_ALBUM);
                    return;
                } else {
                    checkPhotos();
                    return;
                }
            case R.id.iv_videoview_rotation:
                if (this.video_view4.getFlip() == 0) {
                    this.video_view4.setFlip(1);
                    return;
                } else if (this.video_view4.getFlip() == 1) {
                    this.video_view4.setFlip(2);
                    return;
                } else if (this.video_view4.getFlip() == 2) {
                    this.video_view4.setFlip(3);
                    return;
                } else if (this.video_view4.getFlip() == 3) {
                    this.video_view4.setFlip(0);
                    return;
                } else {
                    return;
                }
            case R.id.bt_more:
                if (this.layout_more_setting.getVisibility() == View.VISIBLE) {
                    this.layout_more_setting.setVisibility(View.INVISIBLE);
                    return;
                } else {
                    this.layout_more_setting.setVisibility(View.VISIBLE);
                    return;
                }
            case R.id.pictures:
                if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                    showGetPermissionDialog(1002);
                    return;
                } else {
                    takingPictures();
                    return;
                }
            case R.id.setting:
                if (cam.status() == CONN_STATUS.CONNECTED) {
                    String[] items;
                    if (this.ll_camera_turn.getVisibility() == View.GONE) {
                        this.ll_camera_turn.setVisibility(View.VISIBLE);
                    } else {
                        this.ll_camera_turn.setVisibility(View.GONE);
                    }
                    if (this.layout_more_setting.getVisibility() == View.VISIBLE) {
                        this.layout_more_setting.setVisibility(View.INVISIBLE);
                    }
                    if (cam.sensor_id == 20) {
                        items = getResources().getStringArray(R.array.resolution_array_yuv);
                        int resolution = SpUtil.getInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4)).intValue();
                        if (!(4 == resolution || 5 == resolution)) {
                            SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4));
                            this.currentResolution = 4;
                            get_model();
                        }
                    } else if ((WingedCamApplication.resulotion_cap & 2097152) == 0) {
                        items = getResources().getStringArray(R.array.resolution_array_960P);
                        if (21 == SpUtil.getInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(6)).intValue()) {
                            SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(6));
                            this.currentResolution = 6;
                            get_model();
                        }
                    } else {
                        items = getResources().getStringArray(R.array.resolution_array_1200P);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);

                    this.spinner_resolution_select.setAdapter(adapter);
                    this.currentResolution = SpUtil.getInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(6)).intValue();
                    if (20 == cam.sensor_id) {
                        switch (this.currentResolution) {
                            case 4:
                                this.spinner_resolution_select.setSelection(0);
                                return;
                            case 5:
                                this.spinner_resolution_select.setSelection(1);
                                return;
                            default:
                                this.spinner_resolution_select.setSelection(0);
                                return;
                        }
                    }
                    switch (this.currentResolution) {
                        case 4:
                            this.spinner_resolution_select.setSelection(0);
                            return;
                        case 6:
                            this.spinner_resolution_select.setSelection(1);
                            return;
                        case 11:
                            this.spinner_resolution_select.setSelection(2);
                            return;
                        case 19:
                            this.spinner_resolution_select.setSelection(3);
                            return;
                        case 21:
                            this.spinner_resolution_select.setSelection(3);
                            return;
                        default:
                            this.spinner_resolution_select.setSelection(0);
                            return;
                    }
                }
                return;
            case R.id.setting_ssid:
                if (this.editInputSsid.getText().length() == 0) {
                    Toast.makeText(this.app, getString(R.string.please_input_ssid), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    cam.set_params("ap_ssid=" + this.editInputSsid.getText() + "&save=1", new set_params_listener() {
                        @Override
                        public void on_result(IPCam ipcam, ERROR error) {
                            if (error == ERROR.NO_ERROR) {
                                Log.e("TAG", "设置成功");
                                Toast.makeText(ShowActivity.this.app, ShowActivity.this.getString(R.string.set_success), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Log.e("TAG", "设置失败");
                        }
                    });
                    return;
                }
            case R.id.setting_pwd:
                if (8 != this.editInputPwd.getText().length()) {
                    Toast.makeText(this.app, getString(R.string.set_password_8_length), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    cam.set_params("ap_wpapsk=" + this.editInputPwd.getText() + "&ap_safe=1&save=1", new set_params_listener() {
                        @Override
                        public void on_result(IPCam ipcam, ERROR error) {
                            if (error == ERROR.NO_ERROR) {
                                Log.e("TAG", "设置成功");
                                Toast.makeText(ShowActivity.this.app, ShowActivity.this.getString(R.string.set_success), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Log.e("TAG", "设置失败");
                        }
                    });
                    return;
                }
            case R.id.reboot:
                cam.set_params("reboot=1", new set_params_listener() {
                    @Override
                    public void on_result(IPCam ipcam, ERROR error) {
                        if (error == ERROR.NO_ERROR) {
                            Log.e("TAG", "设置成功");
                            Toast.makeText(ShowActivity.this.app, ShowActivity.this.getString(R.string.rebooting), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.e("TAG", "设置失败");
                    }
                });
                this.layout_more_setting.setVisibility(View.INVISIBLE);
                return;
            case R.id.clear_pwd:
                cam.set_params("ap_wpapsk=12345678&ap_safe=1&save=1", new set_params_listener() {
                    @Override
                    public void on_result(IPCam ipcam, ERROR error) {
                        if (error == ERROR.NO_ERROR) {
                            Log.e("TAG", "设置成功");
                            Toast.makeText(ShowActivity.this.app, ShowActivity.this.getString(R.string.set_success), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.e("TAG", "设置失败");
                    }
                });
                return;
            default:
                return;
        }
    }

    private void recording() {
        if (cam.status() == CONN_STATUS.CONNECTED) {
            Log.e("photo", "  isRecording:  " + this.isRecording);
            if (this.isRecording) {
                this.video.setImageResource(R.drawable.live_record);
                this.isRecording = false;
                if (cam.sensor_id == 20) {
                    stopRecord();
                }
                cam.stop_local_record();
                this.video_time.setVisibility(View.GONE);
                StopTimerTask();
                Toast.makeText(this.app, getString(R.string.stop_record), Toast.LENGTH_SHORT).show();
                Tools.add_media(this, this.m_local_record_filepath);
                return;
            }
            if (cam.sensor_id == 20) {
                startRecord();
            }
            this.video.setImageResource(R.drawable.live_record_active);
            this.m_local_record_filepath = cam.start_local_record();
            Log.e("photo", "  isRecording:  " + this.m_local_record_filepath);
        }
    }

    private void checkPhotos() {
        Log.e("photo", "查看照片和录像----------------------: ");
       // startActivity(new Intent(this, ShareActivity.class));
    }

    private void takingPictures() {
        if (cam.status() == CONN_STATUS.CONNECTED) {
            String path="";
            Log.e("photo", "拍照----------------------: ");
            WingedCamApplication.getSound(1);
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

    private void doubleToSingle() {
        WingedCamApplication.getSound(2);
        screen_single();
        this.is3D = false;
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (20 == cam.sensor_id) {
            switch (position) {
                case 0:
                    SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4));
                    this.currentResolution = 4;
                    get_model();
                    return;
                case 1:
                    SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(5));
                    this.currentResolution = 5;
                    get_model();
                    return;
                default:
                    SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4));
                    this.currentResolution = 4;
                    get_model();
                    return;
            }
        }
        switch (position) {
            case 0:
                SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4));
                this.currentResolution = 4;
                get_model();
                return;
            case 1:
                SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(6));
                this.currentResolution = 6;
                get_model();
                return;
            case 2:
                SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(11));
                this.currentResolution = 11;
                get_model();
                return;
            case 3:
                if ((WingedCamApplication.resulotion_cap & 524288) != 0) {
                    SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(19));
                    this.currentResolution = 19;
                } else if ((WingedCamApplication.resulotion_cap & 2097152) != 0) {
                    SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(21));
                    this.currentResolution = 21;
                }
                get_model();
                return;
            default:
                SpUtil.putInt(getApplicationContext(), "spinner_resolution", Integer.valueOf(4));
                this.currentResolution = 4;
                get_model();
                return;
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void on_result(IPCam ipcam, ERROR error, JSONObject json) throws JSONException {
        WingedCamApplication.resulotion_cap = json.getInt("resolution_capability");
        this.reslution_cap_label.setText("" + WingedCamApplication.resulotion_cap);
        Log.e("resolution", "resolution_cap:" + WingedCamApplication.resulotion_cap);
    }

    private void StopTimerTask() {
        this.timer.cancel();
    }

    private void StartTimerTask() {
        this.min = 0;
        this.sec = -1;
        this.s = null;
        this.m = null;
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ShowActivity.this.sec = ShowActivity.this.sec + 1;
                if (ShowActivity.this.sec == 60) {
                    ShowActivity.this.sec = 0;
                    ShowActivity.this.min = ShowActivity.this.min + 1;
                }
                if (ShowActivity.this.min < 10) {
                    ShowActivity.this.m = "0" + ShowActivity.this.min;
                } else {
                    ShowActivity.this.m = "" + ShowActivity.this.min;
                }
                if (ShowActivity.this.sec < 10) {
                    ShowActivity.this.s = "0" + ShowActivity.this.sec;
                } else {
                    ShowActivity.this.s = "" + ShowActivity.this.sec;
                }
                Message message = new Message();
                message.what = 100;
                ShowActivity.this.UI_Handler.sendMessage(message);
            }
        }, 0, 1000);
    }

    @TargetApi(23)
    private void getPermissions() {
        ArrayList<String> permissions = new ArrayList();
        if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            permissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
            if (checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                permissions.add("android.permission.ACCESS_FINE_LOCATION");
            }
        }
        if (permissions.size() > 0) {
            requestPermissions((String[]) permissions.toArray(new String[permissions.size()]), 1001);
        }
    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1001:
                return;
            case 1002:
                if (grantResults[0] == 0) {
                    takingPictures();
                    return;
                }
                return;
            case SDK_PERMISSION_RECORD /*1003*/:
                if (grantResults[0] == 0) {
                    recording();
                    return;
                }
                return;
            case SDK_PERMISSION_ALBUM /*1004*/:
                if (grantResults[0] == 0) {
                    checkPhotos();
                    return;
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
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


}
