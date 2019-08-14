package com.sosocam.rcipcam3x;

/**
 * @author king
 * @version V1.0
 * @ Description:
 * @ Date 2019/4/20 13:33
 */
public interface   DiscoverListener {

  void OnCameraDisappeared(String p1);


  void OnCameraDiscovered(DISCOVERED_CAMERA_INFO p1);


  void OnCameraUpdated(DISCOVERED_CAMERA_INFO p1);

}
