package com.wingedcam.ipcam;

/**
 * @author king
 * @version V1.0
 * @ Description:
 * @ Date 2019/4/20 16:21
 */
public class IpCamStatisticInfo {

  public int audio_byterate;
  public int audio_sps;
  public int camera;
  public int speak_byterate;
  public int speak_sps;
  public int video_byterate;
  public int video_fps;
  public int video_fps_rendered;

  @Override public String toString() {
    return "IpCamStatisticInfo{"
        + "audio_byterate="
        + audio_byterate
        + ", audio_sps="
        + audio_sps
        + ", camera="
        + camera
        + ", speak_byterate="
        + speak_byterate
        + ", speak_sps="
        + speak_sps
        + ", video_byterate="
        + video_byterate
        + ", video_fps="
        + video_fps
        + ", video_fps_rendered="
        + video_fps_rendered
        + '}';
  }
}
