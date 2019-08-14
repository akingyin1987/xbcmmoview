package com.sosocam.rcipcam3x;

/**
 * @author king
 * @version V1.0
 * @ Description:
 * @ Date 2019/4/20 13:37
 */
public class RCIPCam3X {

  public static native boolean Argb2Rgb(int[] p1, int p2, int p3, byte[] p4);


  public static native void CloseComm(int p1);


  public static native void ConnectCameraByIP(int p1, String p2, String p3, int p4, boolean p5, String p6, String p7, boolean p8, int p9);


  public static native void ConnectCameraByPPCN(int p1, String p2, String p3, boolean p4, String p5, String p6, boolean p7, int p8);


  public static native void ContinueCameraTFRecord(int p1);


  public static native void ControlCameraPTZ(int p1, int p2, int p3);


  public static native boolean DecodeVideoH264(String p1, int p2, int p3, ManipulateListener p4);


  public static native byte[] DecryptCameraID(String p1, int p2);


  public static native void Deinit();


  public static native void DeleteCamera(int p1);


  public static native void DisconnectCamera(int p1);


  public static native void ECProcessRemoteData(byte[] p1);


  public static native byte[] HttpClientGet(String p1, int p2);


  public static native boolean Init();


  public static native void MonitorCameraStatus(int p1, byte[] p2);


  public static native int NewCamera(boolean p1, ManipulateListener p2);


  public static native void OpenComm(int p1);


  public static native void PauseCameraTFRecord(int p1);


  public static native void PlayCameraAudio(int p1);


  public static native void PlayCameraTFRecord(int p1, int p2, String p3, int p4);


  public static native void PlayCameraTFRecordByPath(int p1, int p2, String p3, int p4);


  public static native void PlayCameraVideo(int p1, int p2);


  public static native boolean RawVideoScale(int p1, byte[] p2, int p3, int p4, byte[] p5, int p6, int p7, int p8);


  public static native boolean ScaleARGB(int[] p1, int p2, int p3, int p4, int p5, int p6, int p7, int[] p8, int p9, int p10);


  public static native void SendCameraSpeakData(int p1, byte[] p2);


  public static native void SetCameraLocalCache(int p1, int p2);


  public static native void SetCameraParams(int p1, byte[] p2);


  public static native void SetCameraRecordPerformance(int p1, int p2, int p3);


  public static native void SetCameraVideoMaxFPS(int p1, int p2);


  public static native void SetCameraVideoPerformance(int p1, int p2);


  public static native void SetPlayCameraTFRecordCache(int p1, int p2);


  public static native void SetVideoScaleFlag(boolean p1);


  public static native void StartCameraLocalRecord(int p1, int p2, String p3);


  public static native void StartCameraSpeak(int p1);


  public static native void StartDiscoverCameras(DiscoverListener p1);


  public static native void StartEC(int p1, int p2, int p3);


  public static native void StopCameraAudio(int p1);


  public static native void StopCameraLocalRecord(int p1);


  public static native void StopCameraSpeak(int p1);


  public static native void StopCameraTFRecord(int p1);


  public static native void StopCameraVideo(int p1);


  public static native void StopDiscoverCameras();


  public static native void StopEC();


  public static native void WriteComm(int p1, byte[] p2);


  public static native void WriteEncodedVideoData(int p1, boolean p2, int p3, int p4, long p5, byte[] p6);


  static {
    System.loadLibrary("rcipcam3x");
  }
}
