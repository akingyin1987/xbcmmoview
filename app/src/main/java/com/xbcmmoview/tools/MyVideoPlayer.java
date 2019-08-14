package com.xbcmmoview.tools;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.xbcmmoview.R;
import io.vov.vitamio.MediaFormat;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class MyVideoPlayer extends Activity {
    private VideoView mVideoView;
    private String path = null;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.path = getIntent().getStringExtra(MediaFormat.KEY_PATH);
        Log.e("photo", "--------- 0-path:　" + this.path);
        System.out.println("--------- 1-path:　" + this.path);
        setContentView(R.layout.vitamio_videoview);
        this.mVideoView = (VideoView) findViewById(R.id.surface_view);
        this.mVideoView.setVideoPath(this.path);
        this.mVideoView.setVideoQuality(16);
        this.mVideoView.setMediaController(new MediaController(MyVideoPlayer.this));
        this.mVideoView.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer arg0) {
                MyVideoPlayer.this.finish();
            }
        });
    }

    @Override
    protected void onPause() {
        System.out.println("onConfigurationChanged ");
        super.onPause();
    }
}
