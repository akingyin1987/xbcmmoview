//package com.wingedcam.util;
//
//import android.graphics.Canvas;
//import android.util.Log;
//import android.view.SurfaceHolder;
//
///**
// * @author king
// * @version V1.0
// * @ Description:
// * @ Date 2019/4/20 13:51
// */
//public class ViewThread  extends  Thread{
//
//  private long mElapsed;
//  private SurfaceHolder mHolder;
//  private IPCamVideoView mPanel;
//  private long mStartTime;
//  private boolean mRun = false;
//
//  public ViewThread(IPCamVideoView panel) {
//    mPanel = panel;
//    mHolder = mPanel.getHolder();
//  }
//
//  public void setRunning(boolean run) {
//    mRun = run;
//  }
//
//  public void run() {
//    Canvas canvas = null;
//    mStartTime = System.currentTimeMillis();
//    while(mRun) {
//      canvas = mHolder.lockCanvas();
//      Log.e("doDraw", "run canvas:" + canvas);
//      if(canvas != null) {
//        mHolder.unlockCanvasAndPost(canvas);
//      }
//      mStartTime = System.currentTimeMillis();
//    }
//  }
//}
