package com.wingedcam.ipcam;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import com.sosocam.rcipcam3x.ManipulateListener;
import com.sosocam.rcipcam3x.RCIPCam3X;
import com.wingedcam.util.HttpClient;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author king
 * @version V1.0
 * @ Description:
 * @ Date 2019/4/23 16:09
 */
public class IPCam implements ManipulateListener {

  private static final int EC_DELAY = 100;
  private static final int EC_MODE = 4;
  private static final int ES_MODE = 2;
  private static final int MSG_AUDIO_DATA = 6;
  private static final int MSG_AUDIO_STATUS_CHNAGED = 5;
  private static final int MSG_CAMERA_STATUS_CHNAGED = 0;
  private static final int MSG_CAN_SET_VIDEO_PERFORMANCE = 7;
  private static final int MSG_LOCAL_RECORD_RESULT = 9;
  private static final int MSG_MONITORED_STATUS_CHANGED = 1;
  private static final int MSG_PROPERTY = 2;
  private static final int MSG_SPEAK_STATUS_CHNAGED = 8;
  private static final int MSG_STATISTIC = 4;
  private static final int MSG_TF_RECORD_STATUS_CHANGED = 10;
  private static final int MSG_VIDEO_STATUS_CHNAGED = 3;
  public static final int PLAY_TF_RECORD_CACHE_FORWARD = 0;
  public static final int PLAY_TF_RECORD_CACHE_IGNORE = -1;
  public static final int PLAY_TF_RECORD_CACHE_NORMAL = 1000;
  public static final int STREAM_HD = 1;
  public static final int STREAM_SD = 0;
  private static final String m_alarm_ioout_status_str = "alarm";
  private static final String m_all_status_str = "disk\u0000record\u0000wifi_signal_level\u0000battery_power\u0000alarm\u0000";
  private static AudioPlayer m_audio_player = null;
  private static AudioRecorder m_audio_recorder = null;
  private static final String m_battery_status_str = "battery_power";
  private static final String m_brightness_max_str = "brightness_max";
  private static final String m_contrast_max_str = "contrast_max";
  private static final String m_fw_verion_property_str = "firmware_ver";
  private static final String m_recording_status_str = "record";
  private static final String m_tf_status_str = "disk";
  private static final String m_wifi_status_str = "wifi_signal_level";
  private int m_alarm_ioout = 0;
  private String m_album_folder = Environment.getExternalStorageDirectory().getPath();
  private int m_audio_byterate = 0;
  private int m_audio_sps = 0;
  private PLAY_STATUS m_audio_status = PLAY_STATUS.STOPPED;
  private int m_battery_power = -1;
  private String m_brightness_max = "";
  private int m_cache = 0;
  private int m_camera;
  private boolean m_camera_recording = false;
  private boolean m_can_set_video_performance = false;
  private String m_contrast_max = "";
  private int m_delay_of_retry_auth = 0;
  private ERROR m_error = ERROR.NO_ERROR;
  private String m_fw_version = "";
  private ArrayList<GetTFRecordClipThumbTask> m_get_tf_record_clip_thumb_tasks = new ArrayList();
  private ArrayList<GetTFRecordQuarterDetailTask> m_get_tf_record_quarter_detail_tasks = new ArrayList();
  private ArrayList<GetTFRecordQuarterThumbTask> m_get_tf_record_quarter_thumb_tasks = new ArrayList();
  private ArrayList<GetTFRecordThumbTask> m_get_tf_record_thumb_tasks = new ArrayList();
  private int m_group = 0;
  private boolean m_https = false;
  private String m_id = "";
  private boolean m_in_lan = false;
  private String m_ip = "";
  private boolean m_jpeg = true;
  private ArrayList<IPCam_Listener> m_listener_list = new ArrayList();
  private ArrayList<LoadTFRecordsTask> m_load_tf_records_tasks = new ArrayList();
  private PLAY_STATUS m_local_record_status = PLAY_STATUS.STOPPED;
  private ReentrantLock m_lock = new ReentrantLock(false);
  private IPCamHandler m_message_handler = new IPCamHandler(this);
  private int m_port = 0;
  private String m_pwd = "";
  private int m_record_performance_mode = 0;
  private int m_record_performance_speed = 0;
  private int m_retry_delay = 5;
  private boolean m_retryable = false;
  private int m_speak_byterate = 0;
  private int m_speak_sps = 0;
  private PLAY_STATUS m_speak_status = PLAY_STATUS.STOPPED;
  private CONN_STATUS m_status = CONN_STATUS.IDLE;
  private int m_tf_free = 0;
  private _TF_RECORD_INFO m_tf_record_info = null;
  private TF_RECORD_STATUS m_tf_record_status = TF_RECORD_STATUS.STOPPED;
  private TF_STATUS m_tf_status = TF_STATUS.NONE;
  private int m_times_of_retry_auth = 0;
  private String m_user = "";
  private int m_video_byterate = 0;
  private int m_video_frames_rendered = 0;
  private int m_video_performance_mode = 1;
  private int m_video_recv_fps = 0;
  private int m_video_render_fps = 0;
  private PLAY_STATUS m_video_status = PLAY_STATUS.STOPPED;
  private int m_video_stream;
  private IPCamVideoView m_view = null;
  private IPCamVideoView m_view_1 = null;
  private int m_wifi_power = 0;
  public int model = 0;
  ArrayList<TF_VIDEO_RECORD_INFO> search_record_list = new ArrayList();
  SearchTFRecordsTask search_record_task;
  public int sensor_id = 0;
  private VedioData_Listener vedioData_listener;

  /* renamed from: com.wingedcam.ipcam.IPCam$1reset_https_runnable */
  class reset_https_runnable implements Runnable {
    private reset_https_listener m_listener;

    public reset_https_runnable(reset_https_listener listener) {
      this.m_listener = listener;
    }

    public void run() {
      this.m_listener.on_reset_https_result(IPCam.this, ERROR.NO_ERROR);
    }
  }


  private class _TF_RECORD_DAY_INFO {
    public boolean alarm;
    public boolean dby;
    public _TF_RECORD_HOUR_INFO[] hours;
    public boolean today;
    public boolean valid;
    public int valid_hours;
    public int week;
    public boolean yesterday;
    public Calendar zero;

    private _TF_RECORD_DAY_INFO() {
      this.hours = new _TF_RECORD_HOUR_INFO[24];
    }
  }

  private class _TF_RECORD_HOUR_INFO {
    public _TF_RECORD_QUARTER_INFO[] quarters;
    public boolean valid;

    private _TF_RECORD_HOUR_INFO() {
      this.quarters = new _TF_RECORD_QUARTER_INFO[4];
    }
  }
  private class _TF_RECORD_QUARTER_INFO {
    public boolean alarm;
    public _TF_RECORD_CLIP_INFO[] clips;
    public int record_id;
    public boolean valid;

    private _TF_RECORD_QUARTER_INFO() {
    }
  }
  private class _TF_RECORD_CLIP_INFO {
    public int alarm;
    public boolean thumb;
    public byte[] thumb_image;
    public boolean valid;

    private _TF_RECORD_CLIP_INFO() {
    }
  }

  private class _TF_RECORD_INFO {
    public _TF_RECORD_DAY_INFO[] days;

    private _TF_RECORD_INFO() {
      this.days = new _TF_RECORD_DAY_INFO[7];
    }
  }

  private class AUDIO_DATA {
    public int camera;
    public byte[] pcm;

    private AUDIO_DATA() {
    }
  }

  private class AUDIO_STATUS_CHANGED_INFO {
    public int camera;
    public int error;
    public int status;

    private AUDIO_STATUS_CHANGED_INFO() {
    }
  }


  private static class AudioPlayer extends Thread {
    private static final int SAMPLERATE = 8000;
    private static final int TIMEOUT = 20;
    private ArrayBlockingQueue<byte[]> m_buffer;
    private boolean m_playing;
    private AudioTrack m_track;

    private AudioPlayer() {
      this.m_buffer = new ArrayBlockingQueue(25, true);
      this.m_playing = false;
      this.m_track = null;
    }

    public boolean start_play() {
      if (this.m_playing) {
        return false;
      }
      try {
        this.m_track = new AudioTrack(3, SAMPLERATE, 4, 2, AudioTrack.getMinBufferSize(SAMPLERATE, 4, 2), 1);
        this.m_track.play();
        try {
          start();
          this.m_playing = true;
          return true;
        } catch (Exception e) {
          this.m_track.stop();
          this.m_track.release();
          this.m_track = null;
          return false;
        }
      } catch (Exception e2) {
        if (this.m_track != null) {
          this.m_track.release();
          this.m_track = null;
        }
        return false;
      }
    }

    public void stop_play() {
      if (this.m_playing) {
        this.m_playing = false;
        try {
          join();
        } catch (Exception e) {
        }
        this.m_track.stop();
        this.m_track.release();
        this.m_track = null;
        this.m_buffer.clear();
      }
    }

    public void feed_data(byte[] pcm) {
      if (this.m_playing && this.m_buffer != null) {
        this.m_buffer.offer(pcm);
      }
    }

    public void run() {
      while (this.m_playing) {
        byte[] data = null;
        try {
          data = (byte[]) this.m_buffer.poll(20, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        if (data != null) {
          this.m_track.write(data, 0, data.length);
          RCIPCam3X.ECProcessRemoteData(data);
        }
      }
    }
  }

  private static class AudioRecorder extends Thread {
    private static final int SAMPLERATE = 8000;
    private AudioRecord m_audio_record;
    private int m_camera;
    private boolean m_recording;

    private AudioRecorder() {
      this.m_recording = false;
      this.m_audio_record = null;
    }

    public boolean start_record(int camera) {
      if (this.m_recording) {
        return false;
      }
      this.m_camera = camera;
      try {
        this.m_audio_record = new AudioRecord(1, SAMPLERATE, 16, 2, AudioRecord.getMinBufferSize(SAMPLERATE, 16, 2) * 2);
        this.m_audio_record.startRecording();
        try {
          start();
          this.m_recording = true;
          return true;
        } catch (Exception e) {
          this.m_audio_record.stop();
          this.m_audio_record.release();
          this.m_audio_record = null;
          return false;
        }
      } catch (Exception e2) {
        if (this.m_audio_record != null) {
          this.m_audio_record.release();
          this.m_audio_record = null;
        }
        return false;
      }
    }

    public void stop_record() {
      if (this.m_recording) {
        this.m_recording = false;
        try {
          join();
        } catch (Exception e) {
        }
        this.m_audio_record.stop();
        this.m_audio_record.release();
        this.m_audio_record = null;
      }
    }

    public void run() {
      byte[] pcm = new byte[640];
      while (this.m_recording) {
        this.m_audio_record.read(pcm, 0, pcm.length);
        RCIPCam3X.SendCameraSpeakData(this.m_camera, pcm);
      }
    }
  }


  private class CAMERA_STATUS_CHANGED_INFO {
    public int bad_auth_param;
    public int camera;
    public int error;
    public int status;

    private CAMERA_STATUS_CHANGED_INFO() {
    }
  }


  public enum TF_STATUS {
    NONE,
    READY,
    ERROR,
    FULL
  }


  public enum TF_RECORD_STATUS {
    STOPPED,
    REQUESTING,
    PLAYING,
    BUFFING,
    PAUSING
  }

  public enum CONN_STATUS {
    IDLE,
    P2P_CONNECTING,
    CONNECTING,
    AUTHING,
    CONNECTED,
    WAIT_CONNECTING
  }
  public enum PLAY_STATUS {
    STOPPED,
    REQUESTING,
    PLAYING
  }


  public enum ERROR {
    NO_ERROR,
    UNKNOWN,
    INTERNAL_ERROR,
    BAD_PARAM,
    BAD_STATUS,
    BAD_ID,
    NETWORK_ERROR,
    CLOSED_BY_DEVICE,
    BAD_AUTH,
    DEVICE_TOO_MANY_SESSIONS,
    DEVICE_INTERNAL_ERROR,
    DEVICE_BAD_PARAM,
    DEVICE_FORBIDDEN,
    DEVICE_BAD_STATUS,
    DEVICE_OPERATION_FAIL,
    P2P_DISCONNECTED,
    P2P_INVALID_ID,
    P2P_DEVICE_OFFLINE,
    P2P_TIMEOUT,
    P2P_TOO_MANY_SESSIONS,
    P2P_NETWORK_ERROR,
    HTTP_GET_ERROR,
    DEVICE_TIMEOUT,
    DEVICE_BAD_REQUEST,
    UPGRADE_BAD_FILE,
    UPGRADE_BAD_SERVER,
    UPGRADE_DOWNLOAD_FAILED,
    SOSOCAM_BAD_ID,
    SOSOCAM_BAD_ACCESS,
    SOSOCAM_UNREGISTERED
  }

  public interface get_tf_record_clip_thumb_listener {
    void on_result(IPCam iPCam, int i, int i2, int i3, int i4, byte[] bArr);
  }


  private class GetTFRecordClipThumbTaskParams {
    int day;
    int hour;
    get_tf_record_clip_thumb_listener listener;
    int no;
    int quarter;
    String url;

    private GetTFRecordClipThumbTaskParams() {
    }
  }
  private class GetTFRecordClipThumbTaskResult {
    int day;
    int hour;
    get_tf_record_clip_thumb_listener listener;
    int no;
    int quarter;
    byte[] thumb;

    private GetTFRecordClipThumbTaskResult() {
    }
  }

  private class GetTFRecordClipThumbTask extends
      AsyncTask<GetTFRecordClipThumbTaskParams, Void, GetTFRecordClipThumbTaskResult> {
    private GetTFRecordClipThumbTask() {
    }

    protected GetTFRecordClipThumbTaskResult doInBackground(GetTFRecordClipThumbTaskParams... params) {
      GetTFRecordClipThumbTaskResult result = new GetTFRecordClipThumbTaskResult();
      result.listener = params[0].listener;
      result.day = params[0].day;
      result.hour = params[0].hour;
      result.quarter = params[0].quarter;
      result.no = params[0].no;
      result.thumb = HttpClient.get_binary(params[0].url);
      return result;
    }

    protected void onPostExecute(GetTFRecordClipThumbTaskResult result) {
      super.onPostExecute(result);
      m_get_tf_record_clip_thumb_tasks.remove(this);
      if (result.thumb != null && m_tf_record_info != null && m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips != null && IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[result.no].valid && IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[result.no].thumb && IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[result.no].thumb_image == null) {
        m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[result.no].thumb_image = result.thumb;
      }
      if (IPCam.this.m_tf_record_info == null || IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips == null || !IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[result.no].valid || !IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[result.no].thumb) {
        result.listener.on_result(IPCam.this, result.day, result.hour, result.quarter, result.no, null);
      } else {
        result.listener.on_result(IPCam.this, result.day, result.hour, result.quarter, result.no, IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[result.no].thumb_image);
      }
    }
  }
  public class TF_VIDEO_RECORD_INFO {
    public int end_time;
    public int flag;
    public String name;
    public String path;
    public String preview_name;
    public int size;
    public int start_time;
    public byte[] thumb;

    public String toString() {
      return "path: " + this.path + ", start_time: " + this.start_time + ",flag: " + this.flag + ", end_time: " + this.end_time + ", size: " + this.size + ", name: " + this.name + "\n";
    }
  }

  public interface VedioData_Listener {
    void on_get_video_data_ARGB(int i, byte[] bArr, int i2, int i3, int i4);
  }
  public class TF_RECORD_CLIP_INFO {
    public int alarm;
    public boolean thumb;
    public boolean valid;
  }

  public interface get_tf_record_quarter_detail_listener {
    void on_result(IPCam iPCam, int i, int i2, int i3, TF_RECORD_CLIP_INFO[] tf_record_clip_infoArr);
  }
  private static ERROR parse_cgi_error(int error) {
    switch (error) {
      case -6:
        return ERROR.DEVICE_BAD_REQUEST;
      case MediaPlayer.MEDIA_ERROR_IO /*-5*/:
        return ERROR.DEVICE_TIMEOUT;
      case -4:
        return ERROR.DEVICE_INTERNAL_ERROR;
      case -2:
        return ERROR.DEVICE_BAD_PARAM;
      case -1:
        return ERROR.BAD_AUTH;
      case 0:
        return ERROR.NO_ERROR;
      default:
        Log.e("sosocam", "parse unknown cgi error: " + error);
        return ERROR.UNKNOWN;
    }
  }

  private class GetTFRecordQuarterDetailTaskParams {
    int day;
    int hour;
    get_tf_record_quarter_detail_listener listener;
    int quarter;
    String url;

    private GetTFRecordQuarterDetailTaskParams() {
    }
  }

  private class GetTFRecordQuarterDetailTaskResult {
    int day;
    int hour;
    JSONObject json;
    get_tf_record_quarter_detail_listener listener;
    int quarter;

    private GetTFRecordQuarterDetailTaskResult() {
    }
  }

  private class GetTFRecordQuarterDetailTask extends AsyncTask<GetTFRecordQuarterDetailTaskParams, Void, GetTFRecordQuarterDetailTaskResult> {
    private GetTFRecordQuarterDetailTask() {
    }

    protected GetTFRecordQuarterDetailTaskResult doInBackground(GetTFRecordQuarterDetailTaskParams... params) {
      GetTFRecordQuarterDetailTaskResult result = new GetTFRecordQuarterDetailTaskResult();
      result.listener = params[0].listener;
      result.day = params[0].day;
      result.hour = params[0].hour;
      result.quarter = params[0].quarter;
      result.json = HttpClient.get_json(params[0].url);
      return result;
    }

    protected void onPostExecute(GetTFRecordQuarterDetailTaskResult result) {
      int i;
      super.onPostExecute(result);
      TF_RECORD_CLIP_INFO[] clips = null;
      IPCam.this.m_get_tf_record_quarter_detail_tasks.remove(this);
      if (!(result.json == null || IPCam.this.m_tf_record_info == null || IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips != null)) {
        try {
          if (ERROR.NO_ERROR == IPCam.parse_cgi_error(result.json.getInt("error"))) {
            JSONArray j_subrecords = result.json.getJSONArray("subrecords");
            if (j_subrecords.length() == 90) {
              IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips = new _TF_RECORD_CLIP_INFO[90];
              for (i = 0; i < 90; i++) {
                boolean z;
                IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[i] = new _TF_RECORD_CLIP_INFO();
                JSONObject j_subrecord = j_subrecords.getJSONObject(i);
                _TF_RECORD_CLIP_INFO _tf_record_clip_info = IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[i];
                if (j_subrecord.getInt("clip") != 0) {
                  z = true;
                } else {
                  z = false;
                }
                _tf_record_clip_info.valid = z;
                _tf_record_clip_info = IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[i];
                if (j_subrecord.getInt("thumb") != 0) {
                  z = true;
                } else {
                  z = false;
                }
                _tf_record_clip_info.thumb = z;
                IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[i].alarm = j_subrecord.getInt("alarm");
                IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[i].thumb_image = null;
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips = null;
        }
      }
      if (!(IPCam.this.m_tf_record_info == null || IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips == null)) {
        clips = new TF_RECORD_CLIP_INFO[90];
        for (i = 0; i < 90; i++) {
          clips[i] = new TF_RECORD_CLIP_INFO();
          clips[i].valid = IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[i].valid;
          clips[i].alarm = IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[i].alarm;
          clips[i].thumb = IPCam.this.m_tf_record_info.days[result.day].hours[result.hour].quarters[result.quarter].clips[i].thumb;
        }
      }
      result.listener.on_result(IPCam.this, result.day, result.hour, result.quarter, clips);
    }
  }


  public interface get_tf_record_quarter_thumb_listener {
    void on_result(IPCam iPCam, int i, int i2, int i3, int i4, byte[] bArr);
  }


  private class GetTFRecordQuarterThumbTaskParams {
    int day;
    int hour;
    boolean https;
    String ip;
    get_tf_record_quarter_thumb_listener listener;
    int no;
    int port;
    String pwd;
    int quarter;
    boolean step1;
    int t;
    String user;
    int valid_hour_index;

    private GetTFRecordQuarterThumbTaskParams() {
    }
  }
  private class GetTFRecordQuarterThumbTaskProgress {
    int day;
    int hour;
    JSONArray j_subrecords;
    int quarter;

    private GetTFRecordQuarterThumbTaskProgress() {
    }
  }

  private class GetTFRecordQuarterThumbTaskResult {
    int day;
    int hour;
    get_tf_record_quarter_thumb_listener listener;
    int quarter;
    byte[] thumb;
    int valid_hour_index;

    private GetTFRecordQuarterThumbTaskResult() {
    }
  }

  private class GetTFRecordQuarterThumbTask extends AsyncTask<GetTFRecordQuarterThumbTaskParams, GetTFRecordQuarterThumbTaskProgress, GetTFRecordQuarterThumbTaskResult> {
    private GetTFRecordQuarterThumbTask() {
    }

    /* JADX WARNING: Missing block: B:24:0x010f, code:
        if (isCancelled() == false) goto L_0x0111;
 */
    protected IPCam.GetTFRecordQuarterThumbTaskResult doInBackground(IPCam.GetTFRecordQuarterThumbTaskParams... params) {
      IPCam.GetTFRecordQuarterThumbTaskResult result = new IPCam.GetTFRecordQuarterThumbTaskResult();
      result.listener = params[0x0].listener;
      result.day = params[0x0].day;
      result.hour = params[0x0].hour;
      result.valid_hour_index = params[0x0].valid_hour_index;
      result.quarter = params[0x0].quarter;
      result.thumb = null;
      if(params[0x0].step1){
        try {
          String url = params[0x0].https ? "https://" : "http://" + params[0x0].ip + ":" + params[0x0].port + "/list_subrecords.cgi?" + "user=" + params[0x0].user + "&pwd=" + params[0x0].pwd + "&json=1" + "&t=" + params[0x0].t;
          JSONObject json = HttpClient.get_json(url);
          if(json == null) {
            return result;
          }
          if(IPCam.ERROR.NO_ERROR == parse_cgi_error(json.getInt("error"))){
            JSONArray j_subrecords = json.getJSONArray("subrecords");
            if(j_subrecords.length() == 90){
              params[0x0].no = params[0x0].no + 1;
              JSONObject j_subrecord = j_subrecords.getJSONObject(params[0x0].no);
              if(j_subrecord.getInt("clip") != 0) {
                if(j_subrecord.getInt("thumb") != 0) {
                  return result;
                }
              }
              IPCam.GetTFRecordQuarterThumbTaskProgress progress = new IPCam.GetTFRecordQuarterThumbTaskProgress();
              progress.day = params[0x0].day;
              progress.hour = params[0x0].hour;
              progress.quarter = params[0x0].quarter;
              progress.j_subrecords = j_subrecords;
              publishProgress(new IPCam.GetTFRecordQuarterThumbTaskProgress[] {progress});
            }
            return result;

          }
        }catch (Exception e){
          e.printStackTrace();
        }
        if(isCancelled()) {
          return result;
        }
      }
      String  url = params[0x0].https ? "https://" : "http://" + params[0x0].ip + ":" + params[0x0].port + "/get_thumb.cgi?" + "user=" + params[0x0].user + "&pwd=" + params[0x0].pwd + "&json=1" + "&t=" + params[0x0].t + "&no=" + params[0x0].no;
      result.thumb = HttpClient.get_binary(url);
      return result;
    }

    protected void onProgressUpdate(GetTFRecordQuarterThumbTaskProgress... progress) {
      super.onProgressUpdate(progress);
      if (IPCam.this.m_tf_record_info != null && IPCam.this.m_tf_record_info.days[progress[0].day].hours[progress[0].hour].quarters[progress[0].quarter].clips == null) {
        IPCam.this.m_tf_record_info.days[progress[0].day].hours[progress[0].hour].quarters[progress[0].quarter].clips = new _TF_RECORD_CLIP_INFO[90];
        int i = 0;
        while (i < 90) {
          try {
            boolean z;
            IPCam.this.m_tf_record_info.days[progress[0].day].hours[progress[0].hour].quarters[progress[0].quarter].clips[i] = new _TF_RECORD_CLIP_INFO();
            JSONObject j_subrecord = progress[0].j_subrecords.getJSONObject(i);
            _TF_RECORD_CLIP_INFO _tf_record_clip_info = IPCam.this.m_tf_record_info.days[progress[0].day].hours[progress[0].hour].quarters[progress[0].quarter].clips[i];
            if (j_subrecord.getInt("clip") != 0) {
              z = true;
            } else {
              z = false;
            }
            _tf_record_clip_info.valid = z;
            _tf_record_clip_info = IPCam.this.m_tf_record_info.days[progress[0].day].hours[progress[0].hour].quarters[progress[0].quarter].clips[i];
            if (j_subrecord.getInt("thumb") != 0) {
              z = true;
            } else {
              z = false;
            }
            _tf_record_clip_info.thumb = z;
            IPCam.this.m_tf_record_info.days[progress[0].day].hours[progress[0].hour].quarters[progress[0].quarter].clips[i].alarm = j_subrecord.getInt("alarm");
            IPCam.this.m_tf_record_info.days[progress[0].day].hours[progress[0].hour].quarters[progress[0].quarter].clips[i].thumb_image = null;
            i++;
          } catch (Exception e) {
            e.printStackTrace();
            IPCam.this.m_tf_record_info.days[progress[0].day].hours[progress[0].hour].quarters[progress[0].quarter].clips = null;
            return;
          }
        }
      }
    }

    protected void onPostExecute(GetTFRecordQuarterThumbTaskResult result) {
      super.onPostExecute(result);
      IPCam.this.m_get_tf_record_quarter_thumb_tasks.remove(this);
      result.listener.on_result(IPCam.this, result.day, result.hour, result.valid_hour_index, result.quarter, result.thumb);
    }
  }

  public interface get_tf_record_thumb_listener {
    void on_result(IPCam iPCam, String str, byte[] bArr);
  }


  private class GetTFRecordThumbTaskParams {
    String des;
    get_tf_record_thumb_listener listener;
    String url;

    private GetTFRecordThumbTaskParams() {
    }
  }

  private class GetTFRecordThumbTaskResult {
    String des;
    get_tf_record_thumb_listener listener;
    byte[] thumb;

    private GetTFRecordThumbTaskResult() {
    }
  }



  private class GetTFRecordThumbTask extends AsyncTask<GetTFRecordThumbTaskParams, Void, GetTFRecordThumbTaskResult> {
    private GetTFRecordThumbTask() {
    }

    protected GetTFRecordThumbTaskResult doInBackground(GetTFRecordThumbTaskParams... params) {
      GetTFRecordThumbTaskResult result = new GetTFRecordThumbTaskResult();
      result.listener = params[0].listener;
      result.des = params[0].des;
      result.thumb = HttpClient.get_binary(params[0].url);
      return result;
    }

    protected void onPostExecute(GetTFRecordThumbTaskResult result) {
      super.onPostExecute(result);
      IPCam.this.m_get_tf_record_thumb_tasks.remove(this);
      if (result.thumb == null) {
        result.listener.on_result(IPCam.this, result.des, null);
      } else {
        result.listener.on_result(IPCam.this, result.des, result.thumb);
      }
    }
  }

  public interface IPCam_Listener {
    void on_audio_status_changed(IPCam iPCam);

    void on_camera_alarm_ioout(IPCam iPCam);

    void on_camera_battery_changed(IPCam iPCam);

    void on_camera_recording_changed(IPCam iPCam);

    void on_camera_tf_changed(IPCam iPCam);

    void on_camera_wifi_changed(IPCam iPCam);

    void on_can_set_video_performance(IPCam iPCam);

    void on_local_record_result(IPCam iPCam, ERROR error);

    void on_speak_status_changed(IPCam iPCam);

    void on_statistic(IPCam iPCam);

    void on_status_changed(IPCam iPCam);

    void on_tf_record_event(IPCam iPCam, boolean z, int i, boolean z2);

    void on_tf_record_status_changed(IPCam iPCam);

    void on_video_status_changed(IPCam iPCam);
  }
  public interface load_tf_records_listener {
    void on_result(IPCam iPCam, ERROR error);
  }

  private class LoadTFRecordsTaskParams {
    load_tf_records_listener listener;
    String url;

    private LoadTFRecordsTaskParams() {
    }
  }

  private class LoadTFRecordsTaskResult {
    ERROR error;
    JSONObject json;
    load_tf_records_listener listener;

    private LoadTFRecordsTaskResult() {
    }
  }


  private class LoadTFRecordsTask extends AsyncTask<LoadTFRecordsTaskParams, Void, LoadTFRecordsTaskResult> {
    private LoadTFRecordsTask() {
    }

    protected LoadTFRecordsTaskResult doInBackground(LoadTFRecordsTaskParams... params) {
      LoadTFRecordsTaskResult result = new LoadTFRecordsTaskResult();
      result.listener = params[0].listener;
      result.json = null;
      JSONObject json = HttpClient.get_json(params[0].url);
      if (json == null) {
        result.error = ERROR.HTTP_GET_ERROR;
      } else {
        try {
          result.error = IPCam.parse_cgi_error(json.getInt("error"));
          result.json = json;
        } catch (Exception e) {
          result.error = ERROR.HTTP_GET_ERROR;
        }
      }
      return result;
    }

    protected void onPostExecute(LoadTFRecordsTaskResult result) {
      super.onPostExecute(result);
      IPCam.this.m_load_tf_records_tasks.remove(this);
      if (result.error == ERROR.NO_ERROR) {
        int h;
        int q;
        IPCam.this.m_tf_record_info = new _TF_RECORD_INFO();
        int d = 0;
        while (d < 7) {
          IPCam.this.m_tf_record_info.days[d] = new _TF_RECORD_DAY_INFO();
          IPCam.this.m_tf_record_info.days[d].zero = Calendar.getInstance();
          IPCam.this.m_tf_record_info.days[d].zero.set(11, 0);
          IPCam.this.m_tf_record_info.days[d].zero.set(12, 0);
          IPCam.this.m_tf_record_info.days[d].zero.set(13, 0);
          IPCam.this.m_tf_record_info.days[d].zero.set(14, 0);
          IPCam.this.m_tf_record_info.days[d].zero.add(5, 0 - (6 - d));
          IPCam.this.m_tf_record_info.days[d].today = d == 6;
          IPCam.this.m_tf_record_info.days[d].yesterday = d == 5;
          IPCam.this.m_tf_record_info.days[d].dby = d == 4;
          IPCam.this.m_tf_record_info.days[d].week = IPCam.this.m_tf_record_info.days[d].zero.get(7);
          IPCam.this.m_tf_record_info.days[d].valid = false;
          IPCam.this.m_tf_record_info.days[d].alarm = false;
          IPCam.this.m_tf_record_info.days[d].valid_hours = 0;
          for (h = 0; h < 24; h++) {
            IPCam.this.m_tf_record_info.days[d].hours[h] = new _TF_RECORD_HOUR_INFO();
            IPCam.this.m_tf_record_info.days[d].hours[h].valid = false;
            for (q = 0; q < 4; q++) {
              IPCam.this.m_tf_record_info.days[d].hours[h].quarters[q] = new _TF_RECORD_QUARTER_INFO();
              IPCam.this.m_tf_record_info.days[d].hours[h].quarters[q].valid = false;
              IPCam.this.m_tf_record_info.days[d].hours[h].quarters[q].alarm = false;
              IPCam.this.m_tf_record_info.days[d].hours[h].quarters[q].record_id = 0;
              IPCam.this.m_tf_record_info.days[d].hours[h].quarters[q].clips = null;
            }
          }
          d++;
        }
        try {
          JSONArray records = result.json.getJSONArray("records");
          Calendar tomorrow_zero = Calendar.getInstance();
          tomorrow_zero.set(11, 0);
          tomorrow_zero.set(12, 0);
          tomorrow_zero.set(13, 0);
          tomorrow_zero.set(14, 0);
          tomorrow_zero.add(5, 1);
          Calendar record_t = Calendar.getInstance();
          for (int i = 0; i < records.length(); i++) {
            JSONObject record = records.getJSONObject(i);
            int t = record.getInt("t");
            record_t.setTimeInMillis(((((long) t) * 15) * 60) * 1000);
            if (!record_t.before(IPCam.this.m_tf_record_info.days[0].zero) && record_t.before(tomorrow_zero)) {
              d = 1;
              while (d < 7 && !record_t.before(IPCam.this.m_tf_record_info.days[d].zero)) {
                d++;
              }
              d--;
              h = record_t.get(11);
              q = record_t.get(12) / 15;
              IPCam.this.m_tf_record_info.days[d].hours[h].quarters[q].valid = true;
              IPCam.this.m_tf_record_info.days[d].hours[h].quarters[q].record_id = t;
              IPCam.this.m_tf_record_info.days[d].hours[h].quarters[q].alarm = record.getInt("alarm") != 0;
              if (IPCam.this.m_tf_record_info.days[d].hours[h].quarters[q].alarm) {
                IPCam.this.m_tf_record_info.days[d].alarm = true;
              }
              IPCam.this.m_tf_record_info.days[d].hours[h].valid = true;
              IPCam.this.m_tf_record_info.days[d].valid = true;
            }
          }
          for (d = 0; d < 7; d++) {
            IPCam.this.m_tf_record_info.days[d].valid_hours = 0;
            for (h = 0; h < 24; h++) {
              if (IPCam.this.m_tf_record_info.days[d].hours[h].valid) {
                _TF_RECORD_DAY_INFO _tf_record_day_info = IPCam.this.m_tf_record_info.days[d];
                _tf_record_day_info.valid_hours++;
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      result.listener.on_result(IPCam.this, result.error);
    }
  }

  public interface reset_https_listener {
    void on_reset_https_result(IPCam iPCam, ERROR error);
  }


  private static ERROR parse_sdk_error(int error) {
    switch (error) {
      case MediaPlayer.MEDIA_ERROR_TIMED_OUT /*-110*/:
        return ERROR.P2P_NETWORK_ERROR;
      case -109:
        return ERROR.P2P_TOO_MANY_SESSIONS;
      case -107:
        return ERROR.P2P_TIMEOUT;
      case -105:
        return ERROR.P2P_DEVICE_OFFLINE;
      case -103:
        return ERROR.P2P_INVALID_ID;
      case -101:
        return ERROR.P2P_DISCONNECTED;
      case -100:
        return ERROR.NO_ERROR;
      case -23:
        return ERROR.BAD_STATUS;
      case -20:
        return ERROR.DEVICE_BAD_STATUS;
      case -19:
        return ERROR.DEVICE_OPERATION_FAIL;
      case -18:
        return ERROR.DEVICE_FORBIDDEN;
      case -17:
        return ERROR.DEVICE_BAD_PARAM;
      case -16: /*-16*/
        return ERROR.DEVICE_INTERNAL_ERROR;
      case -15:
        return ERROR.DEVICE_TOO_MANY_SESSIONS;
      case -14:
        return ERROR.BAD_AUTH;
      case -13:
      case -12:
      case -10:
        return ERROR.NETWORK_ERROR;
      case -11:
        return ERROR.CLOSED_BY_DEVICE;
      case -8:
        return ERROR.BAD_ID;
      case -7:
        return ERROR.BAD_STATUS;
      case -6:
        return ERROR.BAD_PARAM;
      case -3:
        return ERROR.INTERNAL_ERROR;
      case 0:
        return ERROR.NO_ERROR;
      default:
        Log.e("sosocam", "parse unknown sdk error: " + error);
        return ERROR.UNKNOWN;
    }
  }


  private static class IPCamHandler extends Handler {
    private final WeakReference<IPCam> m_ipcam;

    public IPCamHandler(IPCam ipcam) {
      this.m_ipcam = new WeakReference(ipcam);
    }

    public void handleMessage(Message msg) {
      IPCam ipcam = m_ipcam.get();
      if (ipcam != null && ipcam.m_status != CONN_STATUS.IDLE) {
        Iterator it;
        IPCam_Listener listener;
        switch (msg.what) {
          case 0:
            CAMERA_STATUS_CHANGED_INFO camera_status_changed_info =
                (CAMERA_STATUS_CHANGED_INFO) msg.obj;
            Log.e("sys", "5---MSG_CAMERA_STATUS_CHNAGED" + camera_status_changed_info.status);
            if (camera_status_changed_info.camera == ipcam.m_camera) {
              switch (camera_status_changed_info.status) {
                case 0:
                  ipcam.m_status = CONN_STATUS.IDLE;
                  RCIPCam3X.DeleteCamera(ipcam.m_camera);
                  ipcam.m_error = IPCam.parse_sdk_error(camera_status_changed_info.error);
                  if (ipcam.m_error == ERROR.BAD_AUTH) {
                    if (camera_status_changed_info.bad_auth_param > 100) {
                      ipcam.m_times_of_retry_auth = 0;
                      ipcam.m_delay_of_retry_auth = camera_status_changed_info.bad_auth_param - 100;
                    } else {
                      ipcam.m_times_of_retry_auth = camera_status_changed_info.bad_auth_param;
                      ipcam.m_delay_of_retry_auth = 0;
                    }
                  }
                  ipcam.on_disconnect();
                  break;
                case 1:
                  ipcam.m_status = CONN_STATUS.CONNECTING;
                  break;
                case 2:
                  ipcam.m_status = CONN_STATUS.AUTHING;
                  break;
                case 3:
                  ipcam.m_status = CONN_STATUS.CONNECTED;
                  ipcam.m_group = camera_status_changed_info.error;
                  RCIPCam3X.MonitorCameraStatus(ipcam.m_camera, IPCam.m_all_status_str.getBytes());
                  ipcam.m_cache = 0;
                  ipcam.m_record_performance_mode = 0;
                  ipcam.m_record_performance_speed = 0;
                  RCIPCam3X.SetCameraRecordPerformance(ipcam.m_camera, ipcam.m_record_performance_mode, ipcam.m_record_performance_speed);
                  RCIPCam3X.SetCameraLocalCache(ipcam.m_camera, ipcam.m_cache);
                  break;
                case 4:
                  ipcam.m_status = CONN_STATUS.WAIT_CONNECTING;
                  ipcam.m_error = IPCam.parse_sdk_error(camera_status_changed_info.error);
                  ipcam.on_disconnect();
                  if (!ipcam.m_in_lan) {
                    ipcam.stop_connect();
                    ipcam.start_connect(ipcam.m_retryable, ipcam.m_retry_delay);
                    break;
                  }
                  break;
              }
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                listener = (IPCam_Listener) it.next();
                Log.e("sys", "5---on_status_changed");
                listener.on_status_changed(ipcam);
              }
              return;
            }
            return;
          case 1:
            MONITORED_STATUS_CHANGED_INFO monitored_status_changed_info =
                (MONITORED_STATUS_CHANGED_INFO) msg.obj;
            if (monitored_status_changed_info.camera != ipcam.m_camera) {
              return;
            }
            if (monitored_status_changed_info.name.equals(IPCam.m_battery_status_str)) {
              ipcam.m_battery_power = monitored_status_changed_info.value;
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                ((IPCam_Listener) it.next()).on_camera_battery_changed(ipcam);
              }
              return;
            } else if (monitored_status_changed_info.name.equals(IPCam.m_tf_status_str)) {
              if (TF_STATUS.READY == (ipcam.m_tf_status = IPCam.parse_tf_status(monitored_status_changed_info.value))) {
                ipcam.m_tf_free = monitored_status_changed_info.value;
              }
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                ((IPCam_Listener) it.next()).on_camera_tf_changed(ipcam);
              }
              return;
            } else if (monitored_status_changed_info.name.equals(IPCam.m_recording_status_str)) {
              ipcam.m_camera_recording = monitored_status_changed_info.value != 0;
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                ((IPCam_Listener) it.next()).on_camera_recording_changed(ipcam);
              }
              return;
            } else if (monitored_status_changed_info.name.equals(IPCam.m_wifi_status_str)) {
              ipcam.m_wifi_power = monitored_status_changed_info.value;
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                ((IPCam_Listener) it.next()).on_camera_wifi_changed(ipcam);
              }
              return;
            } else if (monitored_status_changed_info.name.equals("alarm")) {
              Log.e("TAG_IOOUT", "m_alarm_ioout" + monitored_status_changed_info.value);
              ipcam.m_alarm_ioout = monitored_status_changed_info.value;
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                listener = (IPCam_Listener) it.next();
                if (ipcam.m_alarm_ioout == 2) {
                  listener.on_camera_alarm_ioout(ipcam);
                }
              }
              return;
            } else {
              return;
            }
          case 2:
            PROPERTY_INFO property_info = (PROPERTY_INFO) msg.obj;
            if (property_info.camera != ipcam.m_camera) {
              return;
            }
            if (property_info.name.equals(IPCam.m_fw_verion_property_str)) {
              ipcam.m_fw_version = property_info.value;
              if (ipcam.m_fw_version.matches("^\\d+.3.[0-9,a-f,A-F]+.\\d+.\\d+$")) {
                ipcam.m_jpeg = true;
              }
              Log.e("rc_winedcam", "fw version is " + ipcam.m_fw_version);
              return;
            } else if (property_info.name.equals(IPCam.m_brightness_max_str)) {
              ipcam.m_brightness_max = property_info.value;
              return;
            } else if (property_info.name.equals(IPCam.m_contrast_max_str)) {
              ipcam.m_contrast_max = property_info.value;
              return;
            } else {
              return;
            }
          case 3:
            VIDEO_STATUS_CHANGED_INFO video_status_changed_info =
                (VIDEO_STATUS_CHANGED_INFO) msg.obj;
            if (video_status_changed_info.camera == ipcam.m_camera) {
              switch (video_status_changed_info.status) {
                case 0:
                  ipcam.m_error = IPCam.parse_sdk_error(video_status_changed_info.error);
                  ipcam.on_video_stopped();
                  break;
                case 1:
                  ipcam.m_video_status = PLAY_STATUS.REQUESTING;
                  break;
                case 2:
                  Log.e("sys", "8----playing:" + ipcam.m_view);
                  ipcam.m_video_status = PLAY_STATUS.PLAYING;
                  if (ipcam.m_view != null) {
                    ipcam.m_view.set_state(IPCamVideoView.STATE.PLAYING);
                  }
                  if (ipcam.m_view_1 != null) {
                    ipcam.m_view_1.set_state(IPCamVideoView.STATE.PLAYING);
                    break;
                  }
                  break;
              }
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                listener = (IPCam_Listener) it.next();
                Log.e("sys", "8----on_video_status_changed");
                listener.on_video_status_changed(ipcam);
              }
              return;
            }
            return;
          case 4:
            STATISTIC_INFO statistic_info = (STATISTIC_INFO) msg.obj;
            if (statistic_info.camera == ipcam.m_camera) {
              ipcam.m_video_render_fps = statistic_info.video_fps_rendered;
              ipcam.m_video_recv_fps = statistic_info.video_fps;
              ipcam.m_video_byterate = statistic_info.video_byterate;
              ipcam.m_audio_sps = statistic_info.audio_sps;
              ipcam.m_audio_byterate = statistic_info.audio_byterate;
              ipcam.m_speak_sps = statistic_info.speak_sps;
              ipcam.m_speak_byterate = statistic_info.speak_byterate;
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                ((IPCam_Listener) it.next()).on_statistic(ipcam);
              }
              return;
            }
            return;
          case 5:
            AUDIO_STATUS_CHANGED_INFO audio_status_changed_info =
                (AUDIO_STATUS_CHANGED_INFO) msg.obj;
            if (audio_status_changed_info.camera == ipcam.m_camera) {
              switch (audio_status_changed_info.status) {
                case 0:
                  ipcam.m_error = IPCam.parse_sdk_error(audio_status_changed_info.error);
                  ipcam.on_audio_stopped();
                  break;
                case 1:
                  ipcam.m_audio_status = PLAY_STATUS.REQUESTING;
                  break;
                case 2:
                  ipcam.m_audio_status = PLAY_STATUS.PLAYING;
                  break;
              }
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                ((IPCam_Listener) it.next()).on_audio_status_changed(ipcam);
              }
              return;
            }
            return;
          case 6:
            AUDIO_DATA audio_data = (AUDIO_DATA) msg.obj;
            if (audio_data.camera != ipcam.m_camera) {
              return;
            }
            if ((ipcam.m_audio_status == PLAY_STATUS.PLAYING || ipcam.m_tf_record_status == TF_RECORD_STATUS.PLAYING) && IPCam.m_audio_player != null) {
              IPCam.m_audio_player.feed_data(audio_data.pcm);
              return;
            }
            return;
          case 7:
            Log.e("sys", "8--MSG_CAN_SET_VIDEO_PERFORMANCE: ");
            if (msg.arg1 == ipcam.m_camera) {
              if (ipcam.m_video_stream == 1) {
                RCIPCam3X.SetCameraVideoMaxFPS(ipcam.m_camera, 25);
              } else {
                RCIPCam3X.SetCameraVideoMaxFPS(ipcam.m_camera, 25);
              }
              ipcam.m_can_set_video_performance = true;
              ipcam.m_video_performance_mode = 1;
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                ((IPCam_Listener) it.next()).on_can_set_video_performance(ipcam);
              }
              return;
            }
            return;
          case 8:
            SPEAK_STATUS_CHANGED_INFO speak_status_changed_info =
                (SPEAK_STATUS_CHANGED_INFO) msg.obj;
            if (speak_status_changed_info.camera == ipcam.m_camera) {
              switch (speak_status_changed_info.status) {
                case 0:
                  ipcam.m_error = IPCam.parse_sdk_error(speak_status_changed_info.error);
                  ipcam.on_speak_stopped();
                  break;
                case 1:
                  ipcam.m_speak_status = PLAY_STATUS.REQUESTING;
                  break;
                case 2:
                  ipcam.m_speak_status = PLAY_STATUS.PLAYING;
                  break;
              }
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                ((IPCam_Listener) it.next()).on_speak_status_changed(ipcam);
              }
              return;
            }
            return;
          case 9:
            if (msg.arg1 == ipcam.m_camera && ipcam.m_local_record_status == PLAY_STATUS.REQUESTING) {
              ipcam.m_error = IPCam.parse_sdk_error(msg.arg2);
              if (ipcam.m_error == ERROR.NO_ERROR) {
                ipcam.m_local_record_status = PLAY_STATUS.PLAYING;
                if (ipcam.m_video_status == PLAY_STATUS.PLAYING && ipcam.m_can_set_video_performance) {
                  Log.e("sys", "-------------SetCameraVideoPerformance");
                  RCIPCam3X.SetCameraVideoPerformance(ipcam.m_camera, ipcam.m_video_performance_mode);
                }
              } else {
                ipcam.m_local_record_status = PLAY_STATUS.STOPPED;
              }
              it = ipcam.m_listener_list.iterator();
              while (it.hasNext()) {
                ((IPCam_Listener) it.next()).on_local_record_result(ipcam, ipcam.m_error);
              }
              return;
            }
            return;
          case 10:
            TF_RECORD_STATUS_CHANGED_INFO tf_record_status_changed_info =
                (TF_RECORD_STATUS_CHANGED_INFO) msg.obj;
            if (tf_record_status_changed_info.camera == ipcam.m_camera) {
              switch (tf_record_status_changed_info.status) {
                case 0:
                  ipcam.m_error = IPCam.parse_sdk_error(tf_record_status_changed_info.error);
                  ipcam.on_tf_record_stopped();
                  it = ipcam.m_listener_list.iterator();
                  while (it.hasNext()) {
                    ((IPCam_Listener) it.next()).on_tf_record_status_changed(ipcam);
                  }
                  return;
                case 1:
                  ipcam.m_tf_record_status = TF_RECORD_STATUS.REQUESTING;
                  it = ipcam.m_listener_list.iterator();
                  while (it.hasNext()) {
                    ((IPCam_Listener) it.next()).on_tf_record_status_changed(ipcam);
                  }
                  return;
                case 2:
                  if (ipcam.m_view != null) {
                    ipcam.m_view.set_state(IPCamVideoView.STATE.PLAYING);
                  }
                  ipcam.m_tf_record_status = TF_RECORD_STATUS.PLAYING;
                  it = ipcam.m_listener_list.iterator();
                  while (it.hasNext()) {
                    ((IPCam_Listener) it.next()).on_tf_record_status_changed(ipcam);
                  }
                  return;
                case 3:
                  it = ipcam.m_listener_list.iterator();
                  while (it.hasNext()) {
                    ((IPCam_Listener) it.next()).on_tf_record_event(ipcam, true, tf_record_status_changed_info.record_id, false);
                  }
                  return;
                case 4:
                  ipcam.m_error = ERROR.NO_ERROR;
                  ipcam.on_tf_record_stopped();
                  it = ipcam.m_listener_list.iterator();
                  while (it.hasNext()) {
                    ((IPCam_Listener) it.next()).on_tf_record_status_changed(ipcam);
                  }
                  return;
                case 5:
                  it = ipcam.m_listener_list.iterator();
                  while (it.hasNext()) {
                    ((IPCam_Listener) it.next()).on_tf_record_event(ipcam, false, 0, true);
                  }
                  return;
                case 6:
                  if (ipcam.m_view != null) {
                    ipcam.m_view.set_state(IPCamVideoView.STATE.PAUSING);
                  }
                  ipcam.m_tf_record_status = TF_RECORD_STATUS.PAUSING;
                  it = ipcam.m_listener_list.iterator();
                  while (it.hasNext()) {
                    ((IPCam_Listener) it.next()).on_tf_record_status_changed(ipcam);
                  }
                  return;
                case 7:
                  if (ipcam.m_view != null) {
                    ipcam.m_view.set_state(IPCamVideoView.STATE.PLAYING);
                  }
                  ipcam.m_tf_record_status = TF_RECORD_STATUS.PLAYING;
                  it = ipcam.m_listener_list.iterator();
                  while (it.hasNext()) {
                    ((IPCam_Listener) it.next()).on_tf_record_status_changed(ipcam);
                  }
                  return;
                case 8:
                  if (ipcam.m_tf_record_status == TF_RECORD_STATUS.PLAYING) {
                    if (ipcam.m_view != null) {
                      ipcam.m_view.set_state(IPCamVideoView.STATE.BUFFING);
                    }
                    ipcam.m_tf_record_status = TF_RECORD_STATUS.BUFFING;
                    it = ipcam.m_listener_list.iterator();
                    while (it.hasNext()) {
                      ((IPCam_Listener) it.next()).on_tf_record_status_changed(ipcam);
                    }
                    return;
                  }
                  return;
                case 9:
                  if (ipcam.m_tf_record_status == TF_RECORD_STATUS.BUFFING) {
                    if (ipcam.m_view != null) {
                      ipcam.m_view.set_state(IPCamVideoView.STATE.PLAYING);
                    }
                    ipcam.m_tf_record_status = TF_RECORD_STATUS.PLAYING;
                    it = ipcam.m_listener_list.iterator();
                    while (it.hasNext()) {
                      ((IPCam_Listener) it.next()).on_tf_record_status_changed(ipcam);
                    }
                    return;
                  }
                  return;
                default:
                  return;
              }
            }
            return;
          default:
            return;
        }
      }
    }
  }

  private class VIDEO_STATUS_CHANGED_INFO {
    public int camera;
    public int error;
    public int status;

    private VIDEO_STATUS_CHANGED_INFO() {
    }
  }

  private class PROPERTY_INFO {
    public int camera;
    public String name;
    public String value;

    private PROPERTY_INFO() {
    }
  }



  private void on_disconnect() {
    this.m_group = 0;
    this.m_tf_status = TF_STATUS.NONE;
    this.m_tf_free = 0;
    this.m_camera_recording = false;
    this.m_wifi_power = 0;
    this.m_battery_power = -1;
    on_video_stopped();
    on_audio_stopped();
    on_speak_stopped();
    on_tf_record_stopped();
  }
  public void stop_connect() {
    if (this.m_status != CONN_STATUS.IDLE) {
      on_disconnect();
      RCIPCam3X.DisconnectCamera(this.m_camera);
      RCIPCam3X.DeleteCamera(this.m_camera);
      this.m_status = CONN_STATUS.IDLE;
      this.m_error = ERROR.NO_ERROR;
      Iterator it = this.m_listener_list.iterator();
      while (it.hasNext()) {
        ((IPCam_Listener) it.next()).on_status_changed(this);
      }
    }
  }

  public ERROR start_connect(boolean retryable, int retry_delay) {
    Log.e("sys", "3---m_in_lan: ");
    if (this.m_status != CONN_STATUS.IDLE) {
      return ERROR.BAD_STATUS;
    }
    if (this.m_user.equals("")) {
      return ERROR.BAD_PARAM;
    }
    this.m_retryable = retryable;
    this.m_retry_delay = retry_delay;
    Log.e("HardDecoder", "isSupportMediaCodecHardDecoder:  " );
    this.m_camera = RCIPCam3X.NewCamera(false, this);
    if (!this.m_in_lan) {
      return ERROR.BAD_STATUS;
    }
    Log.e("sys", "3---m_in_lan: " + this.m_in_lan);
    RCIPCam3X.ConnectCameraByIP(this.m_camera, this.m_id, this.m_ip, this.m_port, this.m_https, this.m_user, this.m_pwd, this.m_retryable, this.m_retry_delay);
    this.m_status = CONN_STATUS.CONNECTING;
    return ERROR.NO_ERROR;
  }

  @Override public void OnAudioData(int camera, byte[] pcm) {
    IPCam.AUDIO_DATA data = new IPCam.AUDIO_DATA();
    data.camera = camera;
    data.pcm = pcm;
    m_message_handler.obtainMessage(0x6, data).sendToTarget();
  }

  @Override public void OnAudioStatusChanged(int camera, int status, int error) {
    AUDIO_STATUS_CHANGED_INFO info = new AUDIO_STATUS_CHANGED_INFO();
    info.camera = camera;
    info.status = status;
    info.error = error;
    this.m_message_handler.obtainMessage(5, info).sendToTarget();

  }

  @Override
  public void OnCameraStatusChanged(int camera, int status, int error, int bad_auth_param) {
    Log.e("sys", "4---OnCameraStatusChanged: " + status);
    CAMERA_STATUS_CHANGED_INFO info = new CAMERA_STATUS_CHANGED_INFO();
    info.camera = camera;
    info.status = status;
    info.error = error;
    info.bad_auth_param = bad_auth_param;
    this.m_message_handler.obtainMessage(0, info).sendToTarget();

  }

  @Override public void OnCanSetVideoPerformance(int camera) {
    this.m_message_handler.obtainMessage(7, camera, 0).sendToTarget();

  }

  @Override public void OnCommData(int camera, byte[] data) {

  }

  @Override public void OnFileDataARGB(int state, int width, int height, byte[] h264) {

  }

  @Override public void OnLocalRecordResult(int camera, int result) {
    m_message_handler.obtainMessage(0x9, camera, result).sendToTarget();
  }

  @Override public void OnMonitoredStatusChanged(int camera, String name, int status) {
    Log.e("sys", "7---OnMonitoredStatusChanged: " + status);
    MONITORED_STATUS_CHANGED_INFO info = new MONITORED_STATUS_CHANGED_INFO();
    info.camera = camera;
    info.name = name;
    info.value = status;
    this.m_message_handler.obtainMessage(1, info).sendToTarget();

  }

  @Override public void OnOpenCommResult(int camera, int error) {

  }

  @Override public void OnProperty(int camera, String name, String value) {
    IPCam.PROPERTY_INFO info = new IPCam.PROPERTY_INFO();
    info.camera = camera;
    info.name = name;
    info.value = value;
    m_message_handler.obtainMessage(0x2, info).sendToTarget();
  }

  @Override public void OnSpeakStatusChanged(int camera, int status, int error) {
    IPCam.SPEAK_STATUS_CHANGED_INFO info = new IPCam.SPEAK_STATUS_CHANGED_INFO();
    info.camera = camera;
    info.status = status;
    info.error = error;
    m_message_handler.obtainMessage(0x8, info).sendToTarget();
  }

  @Override public void OnStatistic(int camera, int video_fps, int video_byterate, int audio_sps,
      int audio_byterate, int speak_sps, int speak_byterate) {
    STATISTIC_INFO info = new STATISTIC_INFO();
    info.camera = camera;
    info.video_fps_rendered = this.m_video_frames_rendered;
    this.m_video_frames_rendered = 0;
    info.audio_byterate = audio_byterate;
    info.audio_sps = audio_sps;
    info.speak_byterate = speak_byterate;
    info.speak_sps = speak_sps;
    info.video_byterate = video_byterate;
    info.video_fps = video_fps;
    this.m_message_handler.obtainMessage(4, info).sendToTarget();

  }

  @Override public void OnTFRecordStatusChanged(int camera, int status, int error, int record_id) {
    IPCam.TF_RECORD_STATUS_CHANGED_INFO info = new IPCam.TF_RECORD_STATUS_CHANGED_INFO();
    info.camera = camera;
    info.status = status;
    info.error = error;
    info.record_id = record_id;
    m_message_handler.obtainMessage(0xa, info).sendToTarget();
  }

  @Override
  public void OnVideoDataARGB(int camera, int width, int height, int playtick, int[] argb) {
    if (camera == this.m_camera) {
      this.m_lock.lock();
      if (this.m_view != null && ((this.m_video_status == PLAY_STATUS.PLAYING || this.m_tf_record_status == TF_RECORD_STATUS.PLAYING) && this.m_view.set_image(argb, width, height))) {
        this.m_video_frames_rendered++;
      }
      if (this.m_view_1 != null) {
        this.m_view_1.set_image(argb, width, height);
      }
      this.m_lock.unlock();
    }

  }

  @Override
  public void OnVideoDataARGB2(int camera, int width, int height, int playtick, int[] argb,
      byte[] yuvdata) {
    if (this.vedioData_listener != null) {
      this.vedioData_listener.on_get_video_data_ARGB(this.m_camera, yuvdata, width, height, playtick);
    }
    OnVideoDataARGB(camera, width, height, playtick, argb);

  }

  @Override public void OnVideoDataRAW(int camera, int width, int height, int codec, int playtick,
      boolean key, byte[] raw) {
    if (this.m_view != null) {
      this.m_view.set_raw(raw, width, height);
    }
    if (this.m_view_1 != null) {
      this.m_view_1.set_raw(raw, width, height);
    }

  }

  @Override public void OnVideoStatusChanged(int camera, int status, int error) {
    Log.e("sys", "8---OnVideoStatusChanged: " + status);
    VIDEO_STATUS_CHANGED_INFO info = new VIDEO_STATUS_CHANGED_INFO();
    info.camera = camera;
    info.status = status;
    info.error = error;
    this.m_message_handler.obtainMessage(3, info).sendToTarget();

  }

  @Override public void OnWriteCommResult(int camera, int error) {

  }

  private class MONITORED_STATUS_CHANGED_INFO {
    public int camera;
    public String name;
    public int value;

    private MONITORED_STATUS_CHANGED_INFO() {
    }
  }

  public static TF_STATUS parse_tf_status(int disk) {
    switch (disk) {
      case -2:
        return TF_STATUS.FULL;
      case -1:
        return TF_STATUS.ERROR;
      case 0:
        return TF_STATUS.NONE;
      default:
        return TF_STATUS.READY;
    }
  }

  private void on_video_stopped() {
    this.m_can_set_video_performance = false;
    on_local_record_stopped();
    this.m_lock.lock();
    this.m_video_status = PLAY_STATUS.STOPPED;
    this.m_lock.unlock();
    if (this.m_view != null) {
      this.m_view.set_state(IPCamVideoView.STATE.PAUSING);
    }
    if (this.m_view_1 != null) {
      this.m_view_1.set_state(IPCamVideoView.STATE.PAUSING);
    }
  }
  private class STATISTIC_INFO {
    public int audio_byterate;
    public int audio_sps;
    public int camera;
    public int speak_byterate;
    public int speak_sps;
    public int video_byterate;
    public int video_fps;
    public int video_fps_rendered;

    private STATISTIC_INFO() {
    }
  }

  private void on_local_record_stopped() {
    if (this.m_status != CONN_STATUS.IDLE && this.m_local_record_status != PLAY_STATUS.STOPPED) {
      RCIPCam3X.StopCameraLocalRecord(this.m_camera);
      this.m_local_record_status = PLAY_STATUS.STOPPED;
    }
  }


  private void on_audio_stopped() {
    if (this.m_audio_status != PLAY_STATUS.STOPPED) {
      if (m_audio_player != null) {
        m_audio_player.stop_play();
        m_audio_player = null;
        RCIPCam3X.StopEC();
      }
      this.m_audio_status = PLAY_STATUS.STOPPED;
    }
  }

  private class SPEAK_STATUS_CHANGED_INFO {
    public int camera;
    public int error;
    public int status;

    private SPEAK_STATUS_CHANGED_INFO() {
    }
  }

  private void on_speak_stopped() {
    if (this.m_speak_status != PLAY_STATUS.STOPPED) {
      if (m_audio_recorder != null) {
        m_audio_recorder.stop_record();
        m_audio_recorder = null;
        RCIPCam3X.StopEC();
      }
      this.m_speak_status = PLAY_STATUS.STOPPED;
    }
  }


  private class TF_RECORD_STATUS_CHANGED_INFO {
    public int camera;
    public int error;
    public int record_id;
    public int status;

    private TF_RECORD_STATUS_CHANGED_INFO() {
    }
  }



  private void on_tf_record_stopped() {
    on_local_record_stopped();
    if (this.m_tf_record_status != TF_RECORD_STATUS.STOPPED) {
      if (m_audio_player != null) {
        m_audio_player.stop_play();
        m_audio_player = null;
      }
      this.m_lock.lock();
      this.m_tf_record_status = TF_RECORD_STATUS.STOPPED;
      this.m_lock.unlock();
    }
    if (this.m_view != null) {
      this.m_view.set_state(IPCamVideoView.STATE.PAUSING);
    }
  }


  public interface search_tf_records_listener {
    void on_result(IPCam iPCam, ERROR error);
  }



  private class SearchTFRecordsTaskParams {
    search_tf_records_listener listener;
    String url;

    private SearchTFRecordsTaskParams() {
    }
  }

  private class SearchTFRecordsTaskResult {
    ERROR error;
    JSONObject json;
    search_tf_records_listener listener;

    private SearchTFRecordsTaskResult() {
    }
  }



  private class SearchTFRecordsTask extends AsyncTask<SearchTFRecordsTaskParams, Void, SearchTFRecordsTaskResult> {
    private SearchTFRecordsTask() {
    }

    protected SearchTFRecordsTaskResult doInBackground(SearchTFRecordsTaskParams... params) {
      SearchTFRecordsTaskResult result = new SearchTFRecordsTaskResult();
      result.listener = params[0].listener;
      result.json = null;
      Log.e("WingCam", "SearchTFRecordsTask url: " + params[0].url);
      JSONObject json = HttpClient.get_json(params[0].url);
      Log.e("WingCam", "SearchTFRecordsTask result: " + json);
      if (json == null) {
        result.error = ERROR.HTTP_GET_ERROR;
      } else {
        try {
          result.error = IPCam.parse_cgi_error(json.getInt("error"));
          result.json = json;
        } catch (Exception e) {
          result.error = ERROR.HTTP_GET_ERROR;
        }
      }
      return result;
    }

    protected void onPostExecute(SearchTFRecordsTaskResult result) {
      super.onPostExecute(result);
      if (result.error == ERROR.NO_ERROR) {
        try {
          JSONArray array = result.json.getJSONArray(IPCam.m_recording_status_str);
          Log.e("WingCam", "SearchTFRecordsTask : " + array);
          IPCam.this.search_record_list.clear();
          for (int i = 0; i < array.length(); i++) {
            JSONObject js_obj = array.getJSONObject(i);
            TF_VIDEO_RECORD_INFO obj = new TF_VIDEO_RECORD_INFO();
            obj.path = js_obj.getString("path");
            obj.start_time = js_obj.getInt("start_time");
            obj.flag = js_obj.getInt("flag");
            obj.end_time = js_obj.getInt("end_time");
            obj.size = js_obj.getInt("size");
            obj.name = js_obj.getString("name");
            obj.preview_name = js_obj.getString("preview_path");
            IPCam.this.search_record_list.add(obj);
          }
        } catch (JSONException e) {
          e.printStackTrace();
          result.error = ERROR.BAD_PARAM;
        }
      }
      if (!isCancelled()) {
        result.listener.on_result(IPCam.this, result.error);
      }
    }
  }

  public class TF_RECORD_DAY_INFO {
    public boolean alarm;
    public boolean dby;
    public boolean today;
    public boolean valid;
    public int valid_hours;
    public int week;
    public boolean yesterday;
  }

  public class TF_RECORD_QUARTER_INFO {
    public boolean alarm;
    public boolean valid;
  }

  public static class TF_RECORD_QUARTER_TIME {
    public int day;
    public int hour;
    public int quarter;

    public TF_RECORD_QUARTER_TIME(int day, int hour, int quarter) {
      this.day = day;
      this.hour = hour;
      this.quarter = quarter;
    }

    public TF_RECORD_QUARTER_TIME() {
    }
  }

  public IPCam.ERROR get_tf_record_quarter_thumb(int day, int hour, int valid_hour_index, int quarter, IPCam.get_tf_record_quarter_thumb_listener listener) {
    if((day < 0) || (day > 0x6)) {
      return IPCam.ERROR.BAD_PARAM;
    }
    if((hour < 0) || (hour > 0x17)) {
      return IPCam.ERROR.BAD_PARAM;
    }
    if((quarter < 0) || (quarter > 0x3)) {
      return IPCam.ERROR.BAD_PARAM;
    }
    if(m_status != IPCam.CONN_STATUS.CONNECTED) {
      return IPCam.ERROR.BAD_STATUS;
    }
    if(m_tf_record_info == null) {
      return IPCam.ERROR.BAD_STATUS;
    }
    if(!m_tf_record_info.days[day].hours[hour].quarters[quarter].valid) {
      return IPCam.ERROR.BAD_PARAM;
    }
    IPCam.GetTFRecordQuarterThumbTaskParams task_params = new IPCam.GetTFRecordQuarterThumbTaskParams();
    task_params.listener = listener;
    task_params.day = day;
    task_params.hour = hour;
    task_params.valid_hour_index = valid_hour_index;
    task_params.quarter = quarter;
    task_params.https = m_https;
    task_params.ip = m_ip;
    task_params.port = m_port;
    task_params.user = m_user;
    task_params.pwd = m_pwd;
    task_params.t = m_tf_record_info.days[day].hours[hour].quarters[quarter].record_id;
    task_params.step1 = false;
    if(m_tf_record_info.days[day].hours[hour].quarters[quarter].clips == null) {
      task_params.step1 = true;
    }
    for(int i = 0x0; i < 0x5a; i = i + 0x1) {
      if((m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[i].valid) && (m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[i].thumb)) {
        if(m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[i].thumb_image == null) {
          task_params.no = i;
          break;
        }
        listener.on_result(this, day, hour, valid_hour_index, quarter, m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[i].thumb_image);
        return IPCam.ERROR.NO_ERROR;
      }
    }
    if(  task_params.no == 0x5a) {
      listener.on_result(this, day, hour, valid_hour_index, quarter, null);
      return IPCam.ERROR.NO_ERROR;
    }
    IPCam.GetTFRecordQuarterThumbTask task = new IPCam.GetTFRecordQuarterThumbTask();
    task.execute(new IPCam.GetTFRecordQuarterThumbTaskParams[] {task_params});
    m_get_tf_record_quarter_thumb_tasks.add(task);
    return IPCam.ERROR.NO_ERROR;
  }

  public void cancel_tf_record_tasks() {
    cancel_load_tf_records_tasks();
    cancel_get_tf_record_quarter_thumb_tasks();
    cancel_get_tf_record_quarter_detail_tasks();
    cancel_get_tf_record_clip_thumb_tasks();
  }
  public void cancel_get_tf_record_clip_thumb_tasks() {
    Iterator it = this.m_get_tf_record_clip_thumb_tasks.iterator();
    while (it.hasNext()) {
      ((GetTFRecordClipThumbTask) it.next()).cancel(true);
    }
    this.m_get_tf_record_clip_thumb_tasks.clear();
  }

  private void cancel_get_tf_record_quarter_detail_tasks() {
    Iterator it = this.m_get_tf_record_quarter_detail_tasks.iterator();
    while (it.hasNext()) {
      ((GetTFRecordQuarterDetailTask) it.next()).cancel(true);
    }
    this.m_get_tf_record_quarter_detail_tasks.clear();
  }


  private void cancel_get_tf_record_quarter_thumb_tasks() {
    Iterator it = this.m_get_tf_record_quarter_thumb_tasks.iterator();
    while (it.hasNext()) {
      ((GetTFRecordQuarterThumbTask) it.next()).cancel(true);
    }
    this.m_get_tf_record_quarter_thumb_tasks.clear();
  }



  private void cancel_load_tf_records_tasks() {
    Iterator it = this.m_load_tf_records_tasks.iterator();
    while (it.hasNext()) {
      ((LoadTFRecordsTask) it.next()).cancel(true);
    }
    this.m_load_tf_records_tasks.clear();
  }

  public void add_listener(IPCam_Listener listener) {
    if (!this.m_listener_list.contains(listener)) {
      this.m_listener_list.add(listener);
    }
  }

  public void remove_listener(IPCam_Listener listener) {
    this.m_listener_list.remove(listener);
  }

  public CONN_STATUS status() {
    return this.m_status;
  }

  public void clear_tf_records() {
    cancel_tf_record_tasks();
    this.m_tf_record_info = null;
  }

  public ERROR load_tf_records(load_tf_records_listener listener) {
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return ERROR.BAD_STATUS;
    }
    if (this.m_load_tf_records_tasks.size() != 0) {
      return ERROR.BAD_STATUS;
    }
    clear_tf_records();
    LoadTFRecordsTaskParams task_params = new LoadTFRecordsTaskParams();
    task_params.listener = listener;
    task_params.url = (this.m_https ? "https://" : "http://") + this.m_ip + ":" + this.m_port + "/list_records.cgi?" + "user=" + this.m_user + "&pwd=" + this.m_pwd + "&json=1";
    LoadTFRecordsTask task = new LoadTFRecordsTask();
    task.execute(new LoadTFRecordsTaskParams[]{task_params});
    this.m_load_tf_records_tasks.add(task);
    return ERROR.NO_ERROR;
  }
  public TF_RECORD_DAY_INFO get_tf_record_day_info(int day) {
    if (day < 0 || day > 6 || this.m_tf_record_info == null) {
      return null;
    }
    TF_RECORD_DAY_INFO info = new TF_RECORD_DAY_INFO();
    info.alarm = this.m_tf_record_info.days[day].alarm;
    info.dby = this.m_tf_record_info.days[day].dby;
    info.today = this.m_tf_record_info.days[day].today;
    info.valid = this.m_tf_record_info.days[day].valid;
    info.week = this.m_tf_record_info.days[day].week;
    info.yesterday = this.m_tf_record_info.days[day].yesterday;
    info.valid_hours = this.m_tf_record_info.days[day].valid_hours;
    return info;
  }
  public boolean get_tf_record_hour_valid(int day, int hour) {
    if (day < 0 || day > 6 || hour < 0 || hour > 23 || this.m_tf_record_info == null) {
      return false;
    }
    return this.m_tf_record_info.days[day].hours[hour].valid;
  }

  public TF_RECORD_QUARTER_INFO get_tf_record_quarter_info(int day, int hour, int quarter) {
    if (day < 0 || day > 6 || hour < 0 || hour > 23 || quarter < 0 || quarter > 3 || this.m_tf_record_info == null) {
      return null;
    }
    TF_RECORD_QUARTER_INFO info = new TF_RECORD_QUARTER_INFO();
    info.alarm = this.m_tf_record_info.days[day].hours[hour].quarters[quarter].alarm;
    info.valid = this.m_tf_record_info.days[day].hours[hour].quarters[quarter].valid;
    return info;
  }

  public class TF_RECORD_CLIP_TIME {
    public int day;
    public int hour;
    public int no;
    public int quarter;
  }
  public TF_RECORD_STATUS tf_record_status() {
    return this.m_tf_record_status;
  }

  public IPCam.TF_RECORD_QUARTER_TIME get_previous_tf_record_quarter_time(int day, int hour, int quarter) {
    if((day < 0) || (day > 0x6)) {
      return null;
    }
    if((hour >= 0) && (hour <= 0x17)) {
      if((quarter >= 0) && (quarter <= 0x3)) {
        if(m_status == IPCam.CONN_STATUS.CONNECTED) {
          if(m_tf_record_info != null) {
            quarter = quarter - 0x1;
            for(; day >= 0; day = day - 0x1) {
              for(; hour >= 0; hour = hour - 0x1) {
                for(; quarter >= 0; quarter = quarter - 0x1) {
                  if(m_tf_record_info.days[day].hours[hour].quarters[quarter].valid) {
                    IPCam.TF_RECORD_QUARTER_TIME t = new IPCam.TF_RECORD_QUARTER_TIME();
                    t.day = day;
                    t.hour = hour;
                    t.quarter = quarter;
                    return t;
                  }
                }
                quarter = 0x3;
              }
              hour = 0x17;
            }
          }
        }
      }
    }
    return new TF_RECORD_QUARTER_TIME();
  }
  public IPCam.TF_RECORD_QUARTER_TIME get_next_tf_record_quarter_time(int day, int hour, int quarter) {
    if((day < 0) || (day > 0x6)) {
      return null;
    }
    if((hour >= 0) && (hour <= 0x17)) {
      if((quarter >= 0) && (quarter <= 0x3)) {
        if(m_status == IPCam.CONN_STATUS.CONNECTED) {
          if(m_tf_record_info != null) {
            quarter = quarter + 0x1;
            for(; day < 0x7; day = day + 0x1) {
              for(; hour < 0x18; hour = hour + 0x1) {
                for(; quarter < 0x4; quarter = quarter + 0x1) {
                  if(m_tf_record_info.days[day].hours[hour].quarters[quarter].valid) {
                    IPCam.TF_RECORD_QUARTER_TIME t = new IPCam.TF_RECORD_QUARTER_TIME();
                    t.day = day;
                    t.hour = hour;
                    t.quarter = quarter;
                    return t;
                  }
                }
                quarter = 0x0;
              }
              hour = 0x0;
            }
          }
        }
      }
    }
    return null;
  }



  public String start_local_record() {
    String path = null;
    if (this.m_status == CONN_STATUS.CONNECTED && ((this.m_video_status == PLAY_STATUS.PLAYING || this.m_tf_record_status == TF_RECORD_STATUS.PLAYING || this.m_tf_record_status == TF_RECORD_STATUS.PAUSING || this.m_tf_record_status == TF_RECORD_STATUS.BUFFING) && this.m_local_record_status == PLAY_STATUS.STOPPED)) {
      int FORMAT_MP4 = 3;
      path = this.m_album_folder + "/" + DateFormat.format("yyyyMMddkkmmss", System.currentTimeMillis()) + (this.m_jpeg ? ".mp4" : ".3gp");
      int i = this.m_camera;
      if (!this.m_jpeg) {
        FORMAT_MP4 = 1;
      }
      RCIPCam3X.StartCameraLocalRecord(i, FORMAT_MP4, path);
      this.m_local_record_status = PLAY_STATUS.REQUESTING;
    }
    return path;
  }

  public ERROR set_play_tf_record_cache(int cache) {
    if (this.m_tf_record_status == TF_RECORD_STATUS.STOPPED) {
      return ERROR.BAD_STATUS;
    }
    RCIPCam3X.SetPlayCameraTFRecordCache(this.m_camera, cache);
    return ERROR.NO_ERROR;
  }
  public ERROR play_tf_record(int record_id, String path) {
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return ERROR.BAD_STATUS;
    }
    if (this.m_video_status != PLAY_STATUS.STOPPED) {
      return ERROR.BAD_STATUS;
    }
    if (this.m_audio_status != PLAY_STATUS.STOPPED) {
      return ERROR.BAD_STATUS;
    }
    if (this.m_tf_record_status != TF_RECORD_STATUS.STOPPED) {
      return ERROR.BAD_STATUS;
    }
    if (m_audio_player != null) {
      return ERROR.BAD_STATUS;
    }
    m_audio_player = new AudioPlayer();
    if (m_audio_player.start_play()) {
      RCIPCam3X.PlayCameraTFRecord(this.m_camera, 0, path, PLAY_TF_RECORD_CACHE_NORMAL);
      this.m_tf_record_status = TF_RECORD_STATUS.REQUESTING;
      if (this.m_view != null) {
        this.m_view.set_state(IPCamVideoView.STATE.BUFFING);
      }
      return ERROR.NO_ERROR;
    }
    m_audio_player = null;
    return ERROR.INTERNAL_ERROR;
  }

  public ERROR continue_tf_record() {
    if (this.m_tf_record_status != TF_RECORD_STATUS.PAUSING) {
      return ERROR.BAD_STATUS;
    }
    RCIPCam3X.ContinueCameraTFRecord(this.m_camera);
    return ERROR.NO_ERROR;
  }


  public ERROR pause_tf_record() {
    if (this.m_tf_record_status == TF_RECORD_STATUS.STOPPED || this.m_tf_record_status == TF_RECORD_STATUS.PAUSING) {
      return ERROR.BAD_STATUS;
    }
    RCIPCam3X.PauseCameraTFRecord(this.m_camera);
    return ERROR.NO_ERROR;
  }
  public void set_record_performance_speed(int speed) {
    if (speed >= 0 && speed <= 5 && this.m_status == CONN_STATUS.CONNECTED && this.m_record_performance_speed != speed) {
      this.m_record_performance_speed = speed;
      RCIPCam3X.SetCameraRecordPerformance(this.m_camera, this.m_record_performance_mode, this.m_record_performance_speed);
    }
  }

  public void stop_tf_record() {
    if (this.m_tf_record_status != TF_RECORD_STATUS.STOPPED) {
      RCIPCam3X.StopCameraTFRecord(this.m_camera);
      this.m_error = ERROR.NO_ERROR;
      on_tf_record_stopped();
    }
  }
  public static int get_tf_record_play_id(Date date) {
    return (int) (date.getTime() / 1000);
  }
  public int get_tf_record_play_id(int day, int hour, int quarter) {
    if (day < 0 || day > 6 || hour < 0 || hour > 23 || quarter < 0 || quarter > 3 || this.m_tf_record_info == null) {
      return 0;
    }
    return (this.m_tf_record_info.days[day].hours[hour].quarters[quarter].record_id * 15) * 60;
  }

  public int get_tf_record_play_id(int day, int hour, int quarter, int no) {
    if (day < 0 || day > 6 || hour < 0 || hour > 23 || quarter < 0 || quarter > 3 || this.m_tf_record_info == null) {
      return 0;
    }
    return ((this.m_tf_record_info.days[day].hours[hour].quarters[quarter].record_id * 15) * 60) + (no * 10);
  }

  public int record_performance_speed() {
    return this.m_record_performance_speed;
  }


  public ERROR get_tf_record_quarter_detail(int day, int hour, int quarter, get_tf_record_quarter_detail_listener listener) {
    if (day < 0 || day > 6) {
      return ERROR.BAD_PARAM;
    }
    if (hour < 0 || hour > 23) {
      return ERROR.BAD_PARAM;
    }
    if (quarter < 0 || quarter > 3) {
      return ERROR.BAD_PARAM;
    }
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return ERROR.BAD_STATUS;
    }
    if (this.m_tf_record_info == null) {
      return ERROR.BAD_STATUS;
    }
    if (!this.m_tf_record_info.days[day].hours[hour].quarters[quarter].valid) {
      return ERROR.BAD_PARAM;
    }
    if (this.m_tf_record_info.days[day].hours[hour].quarters[quarter].clips == null) {
      GetTFRecordQuarterDetailTaskParams task_params = new GetTFRecordQuarterDetailTaskParams();
      task_params.listener = listener;
      task_params.day = day;
      task_params.hour = hour;
      task_params.quarter = quarter;
      task_params.url = (this.m_https ? "https://" : "http://") + this.m_ip + ":" + this.m_port + "/list_subrecords.cgi?" + "user=" + this.m_user + "&pwd=" + this.m_pwd + "&json=1" + "&t=" + this.m_tf_record_info.days[day].hours[hour].quarters[quarter].record_id;
      GetTFRecordQuarterDetailTask task = new GetTFRecordQuarterDetailTask();
      task.execute(new GetTFRecordQuarterDetailTaskParams[]{task_params});
      this.m_get_tf_record_quarter_detail_tasks.add(task);
    } else {
      TF_RECORD_CLIP_INFO[] clips = new TF_RECORD_CLIP_INFO[90];
      for (int i = 0; i < 90; i++) {
        clips[i] = new TF_RECORD_CLIP_INFO();
        clips[i].valid = this.m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[i].valid;
        clips[i].alarm = this.m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[i].alarm;
        clips[i].thumb = this.m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[i].thumb;
      }
      listener.on_result(this, day, hour, quarter, clips);
    }
    return ERROR.NO_ERROR;
  }
  public TF_RECORD_CLIP_TIME get_tf_record_clip_time(int subrecord_id) {
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return null;
    }
    if (this.m_tf_record_info == null) {
      Log.e("sosocam", "m_tf_record_info == null");
      return null;
    }
    Calendar clip_t = Calendar.getInstance();
    clip_t.setTimeInMillis(((long) subrecord_id) * 1000);
    Calendar tomorrow_zero = Calendar.getInstance();
    tomorrow_zero.set(11, 0);
    tomorrow_zero.set(12, 0);
    tomorrow_zero.set(13, 0);
    tomorrow_zero.set(14, 0);
    tomorrow_zero.add(5, 1);
    if (clip_t.before(this.m_tf_record_info.days[0].zero) || !clip_t.before(tomorrow_zero)) {
      return null;
    }
    int d = 1;
    while (d < 7 && !clip_t.before(this.m_tf_record_info.days[d].zero)) {
      d++;
    }
    d--;
    TF_RECORD_CLIP_TIME t = new TF_RECORD_CLIP_TIME();
    t.day = d;
    t.hour = clip_t.get(11);
    t.quarter = clip_t.get(12) / 15;
    t.no = ((clip_t.get(12) % 15) * 6) + (clip_t.get(13) / 10);
    return t;
  }

  public ERROR get_tf_record_clip_thumb(int day, int hour, int quarter, int no, get_tf_record_clip_thumb_listener listener) {
    if (day < 0 || day > 6) {
      return ERROR.BAD_PARAM;
    }
    if (hour < 0 || hour > 23) {
      return ERROR.BAD_PARAM;
    }
    if (quarter < 0 || quarter > 3) {
      return ERROR.BAD_PARAM;
    }
    if (no < 0 || no >= 90) {
      return ERROR.BAD_PARAM;
    }
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return ERROR.BAD_STATUS;
    }
    if (this.m_tf_record_info == null) {
      return ERROR.BAD_STATUS;
    }
    if (!this.m_tf_record_info.days[day].hours[hour].quarters[quarter].valid) {
      return ERROR.BAD_PARAM;
    }
    if (this.m_tf_record_info.days[day].hours[hour].quarters[quarter].clips == null) {
      return ERROR.BAD_STATUS;
    }
    if (!this.m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[no].valid) {
      return ERROR.BAD_PARAM;
    }
    if (!this.m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[no].thumb) {
      return ERROR.BAD_PARAM;
    }
    if (this.m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[no].thumb_image == null) {
      GetTFRecordClipThumbTaskParams task_params = new GetTFRecordClipThumbTaskParams();
      task_params.listener = listener;
      task_params.day = day;
      task_params.hour = hour;
      task_params.quarter = quarter;
      task_params.no = no;
      task_params.url = (this.m_https ? "https://" : "http://") + this.m_ip + ":" + this.m_port + "/get_thumb.cgi?" + "user=" + this.m_user + "&pwd=" + this.m_pwd + "&json=1" + "&t=" + this.m_tf_record_info.days[day].hours[hour].quarters[quarter].record_id + "&no=" + no;
      GetTFRecordClipThumbTask task = new GetTFRecordClipThumbTask();
      task.execute(new GetTFRecordClipThumbTaskParams[]{task_params});
      this.m_get_tf_record_clip_thumb_tasks.add(task);
    } else {
      listener.on_result(this, day, hour, quarter, no, this.m_tf_record_info.days[day].hours[hour].quarters[quarter].clips[no].thumb_image);
    }
    return ERROR.NO_ERROR;
  }
  public void stop_local_record() {
    if (this.m_local_record_status != PLAY_STATUS.STOPPED) {
      RCIPCam3X.StopCameraLocalRecord(this.m_camera);
      this.m_error = ERROR.NO_ERROR;
      this.m_local_record_status = PLAY_STATUS.STOPPED;
    }
  }

  public ERROR get_tf_record_thumb(String des, get_tf_record_thumb_listener listener) {
    GetTFRecordThumbTaskParams task_params = new GetTFRecordThumbTaskParams();
    task_params.listener = listener;
    task_params.url = (this.m_https ? "https://" : "http://") + this.m_ip + ":" + this.m_port + "/get_record.cgi?" + "user=" + this.m_user + "&pwd=" + this.m_pwd + "&path=" + des;
    task_params.des = des;
    GetTFRecordThumbTask task = new GetTFRecordThumbTask();
    task.execute(new GetTFRecordThumbTaskParams[]{task_params});
    this.m_get_tf_record_thumb_tasks.add(task);
    return ERROR.NO_ERROR;
  }

  public ERROR start_search_tf_records(int start_time, search_tf_records_listener listener) {
    Log.e("WingCam", "start_search_tf_records in status: " + this.m_status);
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return ERROR.BAD_STATUS;
    }
    SearchTFRecordsTaskParams task_params = new SearchTFRecordsTaskParams();
    task_params.listener = listener;
    task_params.url = (this.m_https ? "https://" : "http://") + this.m_ip + ":" + this.m_port + "/search_record.cgi?" + "user=" + this.m_user + "&pwd=" + this.m_pwd + "&json=1" + "&from=" + start_time;
    this.search_record_task = new SearchTFRecordsTask();
    this.search_record_task.execute(new SearchTFRecordsTaskParams[]{task_params});
    Log.e("WingCam", "start_search_tf_records out");
    return ERROR.NO_ERROR;
  }
  public ArrayList<TF_VIDEO_RECORD_INFO> get_search_record_result() {
    return this.search_record_list;
  }
  public interface get_properties_listener {
    void on_result(IPCam iPCam, ERROR error, JSONObject jSONObject) throws JSONException;
  }
  public interface set_params_listener {
    void on_result(IPCam iPCam, ERROR error);
  }
  public void set_album_folder(String folder) {
    this.m_album_folder = folder;
  }
  public ERROR play_video(int stream) {
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return ERROR.BAD_STATUS;
    }
    if (this.m_tf_record_status != TF_RECORD_STATUS.STOPPED) {
      return ERROR.BAD_STATUS;
    }
    if (this.m_video_status != PLAY_STATUS.STOPPED) {
      return ERROR.BAD_STATUS;
    }
    if (stream != 1 && stream != 0) {
      return ERROR.BAD_PARAM;
    }
    RCIPCam3X.PlayCameraVideo(this.m_camera, stream);
    this.m_video_status = PLAY_STATUS.REQUESTING;
    this.m_video_stream = stream;
    if (this.m_view != null) {
      this.m_view.set_state(IPCamVideoView.STATE.BUFFING);
    }
    if (this.m_view_1 != null) {
      this.m_view_1.set_state(IPCamVideoView.STATE.BUFFING);
    }
    return ERROR.NO_ERROR;
  }




  class GetPropertiesTaskParams {
    IPCam.get_properties_listener listener;
    String url;
  }

  class GetPropertiesTaskResult {
    IPCam.ERROR error;
    JSONObject json;
    IPCam.get_properties_listener listener;
  }


  public IPCam.ERROR get_properties(String params, IPCam.get_properties_listener listener) {
    if(m_status != IPCam.CONN_STATUS.CONNECTED) {
      return IPCam.ERROR.BAD_STATUS;
    }
    IPCam.GetPropertiesTaskParams task_params = new IPCam.GetPropertiesTaskParams();
    task_params.listener = listener;
    task_params.url = m_https ? "https://" : "http://" + m_ip + ":" + m_port + "/get_properties.cgi?" + "user=" + m_user + "&pwd=" + m_pwd + "&json=1";
    if(!params.equals("")) {
      task_params.url = task_params.url + "&" + params;
    }
    new IPCam.GetPropertiesTask().execute(new IPCam.GetPropertiesTaskParams[] {task_params});
    return IPCam.ERROR.NO_ERROR;
  }

  class GetPropertiesTask extends AsyncTask<IPCam.GetPropertiesTaskParams,Void,IPCam.GetPropertiesTaskResult> {

    @Override
    protected IPCam.GetPropertiesTaskResult doInBackground(IPCam.GetPropertiesTaskParams[] params) {
      IPCam.GetPropertiesTaskResult result = new IPCam.GetPropertiesTaskResult();
      result.listener = params[0x0].listener;

      JSONObject json = HttpClient.get_json(params[0x0].url);
      result.json = json;
      if(json == null) {
        result.error = IPCam.ERROR.HTTP_GET_ERROR;
        return result;
      }
      try {
        result.error = parse_cgi_error(json.getInt("error"));
        result.json = json;
        return result;
      } catch(Exception e) {
        e.printStackTrace();
        result.error = IPCam.ERROR.HTTP_GET_ERROR;
        return result;
      }
    }
    @Override
    protected void onPostExecute(IPCam.GetPropertiesTaskResult result) {
      super.onPostExecute(result);
      try {
        result.listener.on_result(IPCam.this, result.error, result.json);

      } catch(JSONException e) {
        e.printStackTrace();
      }
    }
  }

  public void newVedioDataListen(VedioData_Listener vedioDataListener) {
    this.vedioData_listener = vedioDataListener;
  }
  public void set_video_view(IPCamVideoView view1, IPCamVideoView view2) {
    this.m_lock.lock();
    this.m_view = view1;
    this.m_view_1 = view2;
    this.m_lock.unlock();
  }



  public IPCam.ERROR set_params(String params, IPCam.set_params_listener listener) {
    if(m_status != IPCam.CONN_STATUS.CONNECTED) {
      return IPCam.ERROR.BAD_STATUS;
    }
    IPCam.SetParamsTaskParams task_params = new IPCam.SetParamsTaskParams();
    task_params.listener = listener;
    task_params.url = m_https ? "https://" : "http://" + m_ip + ":" + m_port + "/set_params.cgi?" + "user=" + m_user + "&pwd=" + m_pwd + "&json=1&" + params;
    new IPCam.SetParamsTask().execute(new IPCam.SetParamsTaskParams[] {task_params});
    return IPCam.ERROR.NO_ERROR;
  }

  class SetParamsTaskParams {
    IPCam.set_params_listener listener;
    String url;
  }

  class SetParamsTaskResult {
    IPCam.ERROR error;
    IPCam.set_params_listener listener;
  }

  class SetParamsTask extends AsyncTask<IPCam.SetParamsTaskParams,Void,IPCam.SetParamsTaskResult> {

    protected IPCam.SetParamsTaskResult doInBackground(IPCam.SetParamsTaskParams[] params) {
      IPCam.SetParamsTaskResult result = new IPCam.SetParamsTaskResult();
      result.listener = params[0x0].listener;
      JSONObject json = HttpClient.get_json(params[0x0].url);
      if(json == null) {
        result.error = IPCam.ERROR.HTTP_GET_ERROR;
        return result;
      }
      try {
        result.error = parse_cgi_error(json.getInt("error"));
        return result;
      } catch(Exception e) {
        result.error = IPCam.ERROR.HTTP_GET_ERROR;
        return result;
      }
    }

    protected void onPostExecute(IPCam.SetParamsTaskResult result) {
      super.onPostExecute(result);
      result.listener.on_result(IPCam.this, result.error);
    }
  }


  public PLAY_STATUS video_status() {
    return this.m_video_status;
  }


  public void stop_video() {
    if (this.m_video_status != PLAY_STATUS.STOPPED) {
      RCIPCam3X.StopCameraVideo(this.m_camera);
      this.m_error = ERROR.NO_ERROR;
      on_video_stopped();
    }
  }


  public void set_cache(int cache) {
    if (this.m_status != CONN_STATUS.IDLE) {
      this.m_cache = cache;
      RCIPCam3X.SetCameraLocalCache(this.m_camera, cache);
    }
  }
  public ERROR error() {
    return this.m_error;
  }
  public PLAY_STATUS local_record_status() {
    return this.m_local_record_status;
  }
  public int wifi_power() {
    return this.m_wifi_power;
  }
  public int battery_power() {
    return this.m_battery_power;
  }

  public int video_render_fps() {
    return this.m_video_render_fps;
  }

  public int video_recv_fps() {
    return this.m_video_recv_fps;
  }

  public int video_byterate() {
    return this.m_video_byterate;
  }

  public int audio_byterate() {
    return this.m_audio_byterate;
  }

  public int audio_sps() {
    return this.m_audio_sps;
  }

  public int speak_byterate() {
    return this.m_speak_byterate;
  }

  public int speak_sps() {
    return this.m_speak_sps;
  }
  public String ip() {
    return this.m_ip;
  }

  public int port() {
    return this.m_port;
  }
  public void set_id(String id) {
    if (id != null && this.m_status == CONN_STATUS.IDLE) {
      this.m_id = id;
    }
  }

  public String id() {
    return this.m_id;
  }

  public void set_user(String user) {
    if (user != null && this.m_status == CONN_STATUS.IDLE) {
      this.m_user = user;
    }
  }

  public String user() {
    return this.m_user;
  }

  public void set_pwd(String pwd) {
    if (pwd != null && this.m_status == CONN_STATUS.IDLE) {
      this.m_pwd = pwd;
    }
  }

  public void update_pwd(String pwd) {
    this.m_pwd = pwd;
  }

  public String pwd() {
    return this.m_pwd;
  }





  public void update_lan_status(boolean in_lan, String ip, int port, boolean https) {
    Log.e("sys", "2---m_status: " + this.m_status);
    if (in_lan) {
      boolean reconnect = false;
      if (this.m_status != CONN_STATUS.IDLE && this.m_retryable) {
        reconnect = true;
      }
      stop_connect();
      this.m_in_lan = true;
      this.m_ip = ip;
      this.m_port = port;
      this.m_https = https;
      if (reconnect) {
        start_connect(this.m_retryable, this.m_retry_delay);
        return;
      }
      return;
    }
    this.m_in_lan = false;
  }

  public String snapshot() {
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return null;
    }
    if (this.m_view == null) {
      return null;
    }
    String path = this.m_album_folder + "/" + DateFormat.format("yyyyMMddkkmmss", System.currentTimeMillis()) + ".jpg";
    if (this.m_view.snapshot(path)) {
      return path;
    }
    return null;
  }
  public String snapshot_new(int width, int height) {
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return null;
    }
    if (this.m_view == null) {
      return null;
    }
    String path = this.m_album_folder + "/" + DateFormat.format("yyyyMMddkkmmss", System.currentTimeMillis()) + ".jpg";
    if (this.m_view.snapshot(path, width, height)) {
      return path;
    }
    return null;
  }


  public ERROR write_comm(byte[] data) {
    if (this.m_status != CONN_STATUS.CONNECTED) {
      return ERROR.BAD_STATUS;
    }
    RCIPCam3X.WriteComm(this.m_camera, data);
    return ERROR.NO_ERROR;
  }


}
