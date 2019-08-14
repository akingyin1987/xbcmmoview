package com.sosocam.rcipcam3x;

/**
 * @author king
 * @version V1.0
 * @ Description:
 * @ Date 2019/4/20 13:35
 */
public interface ManipulateListener {

  void OnAudioData(int camera, byte[] pcm);


  void OnAudioStatusChanged(int camera, int status, int error);


   void OnCameraStatusChanged(int camera, int status, int error, int bad_auth_param);


  void OnCanSetVideoPerformance(int camera);


   void OnCommData(int camera, byte[] data);


   void OnFileDataARGB(int state, int width, int height, byte[] h264);


   void OnLocalRecordResult(int camera, int result);


   void OnMonitoredStatusChanged(int camera, String name, int status);


   void OnOpenCommResult(int camera, int error);


   void OnProperty(int camera, String name, String value);


   void OnSpeakStatusChanged(int camera, int status, int error);


  void OnStatistic(int camera, int video_fps, int video_byterate, int audio_sps, int audio_byterate, int speak_sps, int speak_byterate);


   void OnTFRecordStatusChanged(int camera, int status, int error, int record_id);


  void OnVideoDataARGB(int camera, int width, int height, int playtick, int[] argb);


  void OnVideoDataARGB2(int camera, int width, int height, int playtick, int[] argb, byte[] yuvdata);


  void OnVideoDataRAW(int camera, int width, int height, int codec, int playtick, boolean key, byte[] raw);


  void OnVideoStatusChanged(int camera, int status, int error);


   void OnWriteCommResult(int camera, int error);
}
