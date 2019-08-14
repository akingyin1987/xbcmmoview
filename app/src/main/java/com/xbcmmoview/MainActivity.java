package com.xbcmmoview;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.sosocam.rcipcam3x.DISCOVERED_CAMERA_INFO;
import com.sosocam.rcipcam3x.DiscoverListener;
import com.sosocam.rcipcam3x.ManipulateListener;
import com.sosocam.rcipcam3x.RCIPCam3X;
import com.wingedcam.ipcam.IpCamStatisticInfo;

/**
 * @author king
 * @version V1.0
 * @ Description:
 * @ Date 2019/4/20 15:28
 */
public class MainActivity  extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        connectCamera();
      }
    });
    WifiManager wifiMgr = (WifiManager)getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    if(null != wifiMgr){
      WifiInfo info = wifiMgr.getConnectionInfo();
      String ssid = info.getSSID();
      System.out.println("ssid="+info.toString());
      boolean  result =  RCIPCam3X.Init();
      System.out.println("result="+result);
      RCIPCam3X.StartDiscoverCameras(new DiscoverListener() {
        @Override public void OnCameraDisappeared(String p1) {
          System.out.println("OnCameraDisappeared");
        }

        @Override public void OnCameraDiscovered(DISCOVERED_CAMERA_INFO p1) {
          System.out.println("OnCameraDiscovered"+p1.toString());
          mCAMERA_info = p1;
          if(mCAMERA_info.sensor_id == 20){
            RCIPCam3X.SetVideoScaleFlag(true);
          }
          connectCamera();
        }

        @Override public void OnCameraUpdated(DISCOVERED_CAMERA_INFO p1) {
          System.out.println("OnCameraUpdated");
        }
      });
    }

  }


  private   boolean  m_in_lan = false;
  public void update_lan_status(boolean in_lan, String ip, int port, boolean https) {


  }

 private  DISCOVERED_CAMERA_INFO  mCAMERA_info = null;
  int  camera_id = 0;
  void   connectCamera(){
    if(null == mCAMERA_info){
      return;
    }
    camera_id =  RCIPCam3X.NewCamera(false, new ManipulateListener() {
        @Override public void OnAudioData(int camera, byte[] pcm) {
          System.out.println("OnAudioData");
        }

        @Override public void OnAudioStatusChanged(int p1, int p2, int p3) {
          System.out.println("OnAudioStatusChanged");
        }

        @Override public void OnCameraStatusChanged(int camera, int status, int error, int bad_auth_param) {
          System.out.println("OnCameraStatusChanged");
        }

        @Override public void OnCanSetVideoPerformance(int p1) {
          System.out.println("OnCanSetVideoPerformance");
        }

        @Override public void OnCommData(int p1, byte[] p2) {
          System.out.println("OnCommData");
        }

        @Override public void OnFileDataARGB(int p1, int p2, int p3, byte[] p4) {
          System.out.println("OnFileDataARGB");
        }

        @Override public void OnLocalRecordResult(int p1, int p2) {
          System.out.println("OnLocalRecordResult");
        }

        @Override public void OnMonitoredStatusChanged(int p1, String p2, int p3) {
          System.out.println("OnMonitoredStatusChanged");
        }

        @Override public void OnOpenCommResult(int p1, int p2) {
          System.out.println("OnOpenCommResult");
        }

        @Override public void OnProperty(int p1, String p2, String p3) {
          System.out.println("OnProperty");
        }

        @Override public void OnSpeakStatusChanged(int p1, int p2, int p3) {
          System.out.println("OnSpeakStatusChanged");
        }

        @Override public void OnStatistic(int camera, int video_fps, int video_byterate, int audio_sps, int audio_byterate, int speak_sps, int speak_byterate) {

          IpCamStatisticInfo  ipCamStatisticInfo = new IpCamStatisticInfo();
          ipCamStatisticInfo.camera =camera;
          ipCamStatisticInfo.video_fps = video_fps;
          ipCamStatisticInfo.video_byterate = video_byterate;
          ipCamStatisticInfo.audio_byterate = audio_byterate;
          ipCamStatisticInfo.audio_sps = audio_sps;
          ipCamStatisticInfo.speak_byterate = speak_byterate;
          ipCamStatisticInfo.speak_sps = speak_sps;
          System.out.println("OnStatistic="+ipCamStatisticInfo.toString());
        }

        @Override public void OnTFRecordStatusChanged(int p1, int p2, int p3, int p4) {
          System.out.println("OnTFRecordStatusChanged");
        }

        @Override public void OnVideoDataARGB(int p1, int p2, int p3, int p4, int[] p5) {
          System.out.println("OnVideoDataARGB");
        }

        @Override
        public void OnVideoDataARGB2(int p1, int p2, int p3, int p4, int[] p5, byte[] p6) {
          System.out.println("OnVideoDataARGB2");
        }

        @Override
        public void OnVideoDataRAW(int p1, int p2, int p3, int p4, int p5, boolean p6, byte[] p7) {
          System.out.println("OnVideoDataRAW");
        }

        @Override public void OnVideoStatusChanged(int p1, int p2, int p3) {
          System.out.println("OnVideoStatusChanged");
        }

        @Override public void OnWriteCommResult(int p1, int p2) {
          System.out.println("OnWriteCommResult");
        }
      });

     RCIPCam3X.ConnectCameraByIP(camera_id,mCAMERA_info.id,mCAMERA_info.ip,mCAMERA_info.port,mCAMERA_info.https,"admin","",true,5);
  }

  @Override public void onBackPressed() {
    RCIPCam3X.StopDiscoverCameras();
    if(null != mCAMERA_info){
      RCIPCam3X.CloseComm(camera_id);
    }

    super.onBackPressed();
  }
}
