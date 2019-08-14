package com.wingedcam.ipcam;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.wingedcam.ipcam.IPCam.ERROR;
import com.wingedcam.ipcam.IPCam.IPCam_Listener;
import com.wingedcam.ipcam.IPCam.TF_RECORD_CLIP_INFO;
import com.wingedcam.ipcam.IPCam.TF_RECORD_CLIP_TIME;
import com.wingedcam.ipcam.IPCam.TF_RECORD_DAY_INFO;
import com.wingedcam.ipcam.IPCam.TF_RECORD_QUARTER_TIME;
import com.wingedcam.ipcam.IPCam.TF_RECORD_STATUS;
import com.wingedcam.ipcam.IPCam.get_tf_record_clip_thumb_listener;
import com.wingedcam.ipcam.IPCam.get_tf_record_quarter_detail_listener;
import com.wingedcam.util.Tools;

public class IPCamRecordSlider extends LinearLayout implements IPCam_Listener, get_tf_record_clip_thumb_listener, get_tf_record_quarter_detail_listener, OnSeekBarChangeListener {
    private ImageView download_record;
    private TextView duration_time;
    private TextView m_clip_alarm;
    private LinearLayout m_clip_info;
    private ImageView m_clip_thumb;
    private TextView m_clip_time;
    private View[] m_clip_views;
    private LinearLayout m_clips;
    private TF_RECORD_CLIP_INFO[] m_clips_info = null;
    private Context m_context;
    private int m_day;
    private boolean m_download_record = false;
    private int m_hour;
    private LayoutInflater m_inflater;
    private IPCam m_ipcam = null;
    private IPCamRecordSlider_Listener m_listener;
    private String m_local_record_filepath;
    private boolean m_local_recording = false;
    private int m_no;
    String m_package_name;
    private ProgressBar m_progress_bar;
    private int m_quarter;
    private SeekBar m_seek_bar;
    private boolean m_seeking = false;
    private int m_seeking_no;
    private LinearLayout m_top_menu;
    private ImageView play_record;
    private TextView record_play_speed;
    private ImageView speed_faster;
    private ImageView speed_slower;
    private ImageView tf_play;
    private ImageView tf_play_next;
    private ImageView tf_play_prev;

    public interface IPCamRecordSlider_Listener {
        void on_clip_selected(int i, int i2, int i3, int i4);

        void on_go_back();
    }

    public IPCamRecordSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.m_context = context;
        this.m_inflater = LayoutInflater.from(context);
        this.m_package_name = context.getPackageName();
        this.m_inflater.inflate(getResources().getIdentifier("slider", "layout", this.m_package_name), this);
        this.m_clip_info = (LinearLayout) findViewById(getResources().getIdentifier("clip_info", "id", this.m_package_name));
        this.m_clip_time = (TextView) findViewById(getResources().getIdentifier("clip_time", "id", this.m_package_name));
        this.m_clip_alarm = (TextView) findViewById(getResources().getIdentifier("clip_alarm", "id", this.m_package_name));
        this.m_clip_thumb = (ImageView) findViewById(getResources().getIdentifier("clip_thumb", "id", this.m_package_name));
        this.m_progress_bar = (ProgressBar) findViewById(getResources().getIdentifier("progress", "id", this.m_package_name));
        this.m_seek_bar = (SeekBar) findViewById(getResources().getIdentifier("seek", "id", this.m_package_name));
        this.m_seek_bar.setMax(89);
        this.m_seek_bar.setProgress(0);
        this.m_seek_bar.setOnSeekBarChangeListener(this);
        this.m_clips = (LinearLayout) findViewById(getResources().getIdentifier("clips", "id", this.m_package_name));
        this.m_clips.setVisibility(INVISIBLE);
        this.m_clips.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        this.m_clip_views = new View[90];
        for (int i = 0; i < 90; i++) {
            this.m_clip_views[i] = findViewById(getResources().getIdentifier("clip" + i, "id", this.m_package_name));
        }
        this.duration_time = (TextView) findViewById(getResources().getIdentifier("duration_time", "id", this.m_package_name));
    }

    private void tf_play_status() {
        if (this.m_ipcam.tf_record_status() == TF_RECORD_STATUS.PLAYING) {
            this.tf_play.setImageResource(getResources().getIdentifier("tf_play", "drawable", this.m_package_name));
        } else {
            this.tf_play.setImageResource(getResources().getIdentifier("tf_stop", "drawable", this.m_package_name));
        }
    }

    private String get_performance_speed_string(int speed) {
        String string = "";
        if (speed == 0) {
            string = "1X";
        }
        if (speed == 3) {
            string = "8X";
        }
        if (speed == 4) {
            string = "16X";
        }
        if (speed == 5) {
            return "32X";
        }
        return string;
    }

    private void initWiget() {
        this.m_top_menu = (LinearLayout) findViewById(getResources().getIdentifier("TopBar", "id", this.m_package_name));
        this.m_top_menu.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (IPCamRecordSlider.this.m_listener != null) {
                    IPCamRecordSlider.this.m_listener.on_go_back();
                }
            }
        });
        this.tf_play_prev = (ImageView) findViewById(getResources().getIdentifier("tf_play_prev", "id", this.m_package_name));
        this.tf_play_prev.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (IPCamRecordSlider.this.m_ipcam != null) {
                    TF_RECORD_QUARTER_TIME t = IPCamRecordSlider.this.m_ipcam.get_previous_tf_record_quarter_time(IPCamRecordSlider.this.m_day, IPCamRecordSlider.this.m_hour, IPCamRecordSlider.this.m_quarter);
                    if (t != null) {
                        IPCamRecordSlider.this.set_record(t.day, t.hour, t.quarter);
                        if (IPCamRecordSlider.this.m_listener != null) {
                            IPCamRecordSlider.this.m_listener.on_clip_selected(t.day, t.hour, t.quarter, 0);
                        }
                    }
                }
            }
        });
        this.tf_play_next = (ImageView) findViewById(getResources().getIdentifier("tf_play_next", "id", this.m_package_name));
        this.tf_play_next.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (IPCamRecordSlider.this.m_ipcam != null) {
                    TF_RECORD_QUARTER_TIME t = IPCamRecordSlider.this.m_ipcam.get_next_tf_record_quarter_time(IPCamRecordSlider.this.m_day, IPCamRecordSlider.this.m_hour, IPCamRecordSlider.this.m_quarter);
                    if (t != null) {
                        IPCamRecordSlider.this.set_record(t.day, t.hour, t.quarter);
                        if (IPCamRecordSlider.this.m_listener != null) {
                            IPCamRecordSlider.this.m_listener.on_clip_selected(t.day, t.hour, t.quarter, 0);
                        }
                    }
                }
            }
        });
        this.play_record = (ImageView) findViewById(getResources().getIdentifier("play_record", "id", this.m_package_name));
        this.play_record.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (IPCamRecordSlider.this.m_local_recording) {
                    IPCamRecordSlider.this.stop_local_record(true);
                    return;
                }
                IPCamRecordSlider.this.m_local_record_filepath = IPCamRecordSlider.this.m_ipcam.start_local_record();
                if (IPCamRecordSlider.this.m_local_record_filepath == null) {
                    Tools.showShortToast(IPCamRecordSlider.this.m_context, IPCamRecordSlider.this.getResources().getString(IPCamRecordSlider.this.getResources().getIdentifier("record_failed", "string", IPCamRecordSlider.this.m_package_name)));
                    return;
                }
                IPCamRecordSlider.this.m_local_recording = true;
                IPCamRecordSlider.this.play_record.setImageResource(IPCamRecordSlider.this.getResources().getIdentifier("play_record_active", "drawable", IPCamRecordSlider.this.m_package_name));
                IPCamRecordSlider.this.download_record.setImageResource(IPCamRecordSlider.this.getResources().getIdentifier("download_record", "drawable", IPCamRecordSlider.this.m_package_name));
                IPCamRecordSlider.this.download_record.setVisibility(GONE);
            }
        });
        this.download_record = (ImageView) findViewById(getResources().getIdentifier("download_record", "id", this.m_package_name));
        this.download_record.setVisibility(GONE);
        this.download_record.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (IPCamRecordSlider.this.m_download_record) {
                    IPCamRecordSlider.this.m_ipcam.set_play_tf_record_cache(IPCam.PLAY_TF_RECORD_CACHE_NORMAL);
                    IPCamRecordSlider.this.m_download_record = false;
                    IPCamRecordSlider.this.download_record.setImageResource(IPCamRecordSlider.this.getResources().getIdentifier("download_record", "drawable", IPCamRecordSlider.this.m_package_name));
                    return;
                }
                IPCamRecordSlider.this.m_ipcam.set_play_tf_record_cache(-1);
                IPCamRecordSlider.this.m_download_record = true;
                IPCamRecordSlider.this.download_record.setImageResource(IPCamRecordSlider.this.getResources().getIdentifier("download_record_active", "drawable", IPCamRecordSlider.this.m_package_name));
            }
        });
        this.tf_play = (ImageView) findViewById(getResources().getIdentifier("tf_play", "id", this.m_package_name));
        this.tf_play.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                TF_RECORD_STATUS tf_r_s = IPCamRecordSlider.this.m_ipcam.tf_record_status();
                if (tf_r_s == TF_RECORD_STATUS.STOPPED) {
                    IPCamRecordSlider.this.m_ipcam.play_tf_record(IPCamRecordSlider.this.m_ipcam.get_tf_record_play_id(IPCamRecordSlider.this.m_day, IPCamRecordSlider.this.m_hour, IPCamRecordSlider.this.m_quarter, IPCamRecordSlider.this.m_no), null);
                } else if (tf_r_s == TF_RECORD_STATUS.PAUSING) {
                    IPCamRecordSlider.this.m_ipcam.continue_tf_record();
                } else {
                    IPCamRecordSlider.this.m_ipcam.pause_tf_record();
                }
            }
        });
        this.speed_slower = (ImageView) findViewById(getResources().getIdentifier("speed_slower", "id", this.m_package_name));
        this.speed_slower.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                String speed_v = "";
                speed_v = (String) IPCamRecordSlider.this.record_play_speed.getText();
                if (speed_v.equals("1X")) {
                    IPCamRecordSlider.this.m_ipcam.set_record_performance_speed(5);
                } else if (speed_v.equals("8X")) {
                    IPCamRecordSlider.this.m_ipcam.set_record_performance_speed(0);
                } else if (speed_v.equals("16X")) {
                    IPCamRecordSlider.this.m_ipcam.set_record_performance_speed(3);
                } else if (speed_v.equals("32X")) {
                    IPCamRecordSlider.this.m_ipcam.set_record_performance_speed(4);
                }
                IPCamRecordSlider.this.stop_local_record(true);
                IPCamRecordSlider.this.m_ipcam.stop_tf_record();
                IPCamRecordSlider.this.record_play_speed.setText(IPCamRecordSlider.this.get_performance_speed_string(IPCamRecordSlider.this.m_ipcam.record_performance_speed()));
                IPCamRecordSlider.this.m_ipcam.play_tf_record(IPCamRecordSlider.this.m_ipcam.get_tf_record_play_id(IPCamRecordSlider.this.m_day, IPCamRecordSlider.this.m_hour, IPCamRecordSlider.this.m_quarter, IPCamRecordSlider.this.m_no), null);
            }
        });
        this.record_play_speed = (TextView) findViewById(getResources().getIdentifier("record_play_speed", "id", this.m_package_name));
        this.record_play_speed.setText(get_performance_speed_string(this.m_ipcam.record_performance_speed()));
        this.speed_faster = (ImageView) findViewById(getResources().getIdentifier("speed_faster", "id", this.m_package_name));
        this.speed_faster.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                String speed_v = "";
                speed_v = (String) IPCamRecordSlider.this.record_play_speed.getText();
                if (speed_v.equals("32X")) {
                    IPCamRecordSlider.this.m_ipcam.set_record_performance_speed(0);
                } else if (speed_v.equals("1X")) {
                    IPCamRecordSlider.this.m_ipcam.set_record_performance_speed(3);
                } else if (speed_v.equals("8X")) {
                    IPCamRecordSlider.this.m_ipcam.set_record_performance_speed(4);
                } else if (speed_v.equals("16X")) {
                    IPCamRecordSlider.this.m_ipcam.set_record_performance_speed(5);
                }
                IPCamRecordSlider.this.stop_local_record(true);
                IPCamRecordSlider.this.m_ipcam.stop_tf_record();
                IPCamRecordSlider.this.record_play_speed.setText(IPCamRecordSlider.this.get_performance_speed_string(IPCamRecordSlider.this.m_ipcam.record_performance_speed()));
                IPCamRecordSlider.this.m_ipcam.play_tf_record(IPCamRecordSlider.this.m_ipcam.get_tf_record_play_id(IPCamRecordSlider.this.m_day, IPCamRecordSlider.this.m_hour, IPCamRecordSlider.this.m_quarter, IPCamRecordSlider.this.m_no), null);
            }
        });
    }

    public void init(IPCam ipcam, IPCamRecordSlider_Listener listener) {
        if (ipcam != null) {
            this.m_ipcam = ipcam;
            this.m_listener = listener;
            this.m_ipcam.add_listener(this);
            initWiget();
            tf_play_status();
        }
    }

    public void deinit() {
        this.m_listener = null;
        if (this.m_ipcam != null) {
            this.m_ipcam.remove_listener(this);
            this.m_ipcam = null;
        }
    }

    private String get_start_time_str(int day, int hour, int quarter) {
        TF_RECORD_DAY_INFO info = this.m_ipcam.get_tf_record_day_info(day);
        if (info == null) {
            return "";
        }
        String s;
        if (info.today) {
            s = getResources().getString(getResources().getIdentifier("today", "string", this.m_package_name));
        } else if (info.yesterday) {
            s = getResources().getString(getResources().getIdentifier("yesterday", "string", this.m_package_name));
        } else if (info.week == 1) {
            s = getResources().getString(getResources().getIdentifier("sunday", "string", this.m_package_name));
        } else if (info.week == 2) {
            s = getResources().getString(getResources().getIdentifier("monday", "string", this.m_package_name));
        } else if (info.week == 3) {
            s = getResources().getString(getResources().getIdentifier("tuesday", "string", this.m_package_name));
        } else if (info.week == 4) {
            s = getResources().getString(getResources().getIdentifier("wednesday", "string", this.m_package_name));
        } else if (info.week == 5) {
            s = getResources().getString(getResources().getIdentifier("thursday", "string", this.m_package_name));
        } else if (info.week == 6) {
            s = getResources().getString(getResources().getIdentifier("friday", "string", this.m_package_name));
        } else {
            s = getResources().getString(getResources().getIdentifier("saturday", "string", this.m_package_name));
        }
        s = s + " " + hour + ":";
        if (quarter == 0) {
            return s + "00";
        }
        if (quarter == 1) {
            return s + "15";
        }
        if (quarter == 2) {
            return s + "30";
        }
        return s + "45";
    }

    public String get_end_time_str(int day, int hour, int quarter) {
        TF_RECORD_DAY_INFO info = this.m_ipcam.get_tf_record_day_info(day);
        if (info == null) {
            return "";
        }
        String s;
        if (info.today) {
            s = getResources().getString(getResources().getIdentifier("today", "string", this.m_package_name));
        } else if (info.yesterday) {
            s = getResources().getString(getResources().getIdentifier("yesterday", "string", this.m_package_name));
        } else if (info.week == 1) {
            s = getResources().getString(getResources().getIdentifier("sunday", "string", this.m_package_name));
        } else if (info.week == 2) {
            s = getResources().getString(getResources().getIdentifier("monday", "string", this.m_package_name));
        } else if (info.week == 3) {
            s = getResources().getString(getResources().getIdentifier("tuesday", "string", this.m_package_name));
        } else if (info.week == 4) {
            s = getResources().getString(getResources().getIdentifier("wednesday", "string", this.m_package_name));
        } else if (info.week == 5) {
            s = getResources().getString(getResources().getIdentifier("thursday", "string", this.m_package_name));
        } else if (info.week == 6) {
            s = getResources().getString(getResources().getIdentifier("friday", "string", this.m_package_name));
        } else {
            s = getResources().getString(getResources().getIdentifier("saturday", "string", this.m_package_name));
        }
        if (quarter == 3) {
            s = s + " " + (hour + 1) + ":";
        } else {
            s = s + " " + hour + ":";
        }
        if (quarter == 0) {
            return s + "15";
        }
        if (quarter == 1) {
            return s + "30";
        }
        if (quarter == 2) {
            return s + "45";
        }
        return s + "00";
    }

    private String get_time_str(int day, int hour, int quarter, int no) {
        TF_RECORD_DAY_INFO info = this.m_ipcam.get_tf_record_day_info(day);
        if (info == null) {
            return "";
        }
        String s;
        if (info.today) {
            s = getResources().getString(getResources().getIdentifier("today", "string", this.m_package_name));
        } else if (info.yesterday) {
            s = getResources().getString(getResources().getIdentifier("yesterday", "string", this.m_package_name));
        } else if (info.week == 1) {
            s = getResources().getString(getResources().getIdentifier("sunday", "string", this.m_package_name));
        } else if (info.week == 2) {
            s = getResources().getString(getResources().getIdentifier("monday", "string", this.m_package_name));
        } else if (info.week == 3) {
            s = getResources().getString(getResources().getIdentifier("tuesday", "string", this.m_package_name));
        } else if (info.week == 4) {
            s = getResources().getString(getResources().getIdentifier("wednesday", "string", this.m_package_name));
        } else if (info.week == 5) {
            s = getResources().getString(getResources().getIdentifier("thursday", "string", this.m_package_name));
        } else if (info.week == 6) {
            s = getResources().getString(getResources().getIdentifier("friday", "string", this.m_package_name));
        } else {
            s = getResources().getString(getResources().getIdentifier("saturday", "string", this.m_package_name));
        }
        return s + " " + hour + ":" + ((quarter * 15) + (no / 6)) + ":" + ((no * 10) % 60);
    }

    public void set_record(int day, int hour, int quarter) {
        this.m_seek_bar.setProgress(0);
        this.m_clips.setVisibility(INVISIBLE);
        String start_time = "";
        String end_time = "";
        String t = get_start_time_str(day, hour, quarter);
        if (t == null) {
            this.duration_time.setText("");
        } else {
            start_time = t;
        }
        t = get_end_time_str(day, hour, quarter);
        if (t == null) {
            this.duration_time.setText("");
        } else {
            end_time = t;
        }
        if (!(start_time == null || end_time == null)) {
            this.duration_time.setText(start_time + " - " + end_time);
        }
        this.m_day = day;
        this.m_hour = hour;
        this.m_quarter = quarter;
        this.m_clips_info = null;
        this.m_ipcam.get_tf_record_quarter_detail(day, hour, quarter, this);
    }

    public void clear_async_tasks() {
        this.m_ipcam.cancel_tf_record_tasks();
    }

    public void on_result(IPCam ipcam, int day, int hour, int quarter, TF_RECORD_CLIP_INFO[] clips) {
        if (this.m_day == day && this.m_hour == hour && this.m_quarter == quarter && clips != null) {
            this.m_clips_info = clips;
            for (int i = 0; i < 90; i++) {
                if (!clips[i].valid) {
                    this.m_clip_views[i].setBackgroundColor(-7829368);
                } else if (clips[i].alarm == 0) {
                    this.m_clip_views[i].setBackgroundColor(-1);
                } else {
                    this.m_clip_views[i].setBackgroundColor(-40960);
                }
            }
            this.m_clips.setVisibility(VISIBLE);
        }
    }

    public void on_video_status_changed(IPCam ipcam) {
    }

    public void on_audio_status_changed(IPCam ipcam) {
    }

    public void on_speak_status_changed(IPCam ipcam) {
    }

    public void on_camera_tf_changed(IPCam ipcam) {
    }

    public void on_camera_wifi_changed(IPCam ipcam) {
    }

    public void on_camera_battery_changed(IPCam ipcam) {
    }

    public void on_camera_alarm_ioout(IPCam ipcam) {
    }

    public void on_camera_recording_changed(IPCam ipcam) {
    }

    public void on_statistic(IPCam ipcam) {
    }

    public void on_can_set_video_performance(IPCam ipcam) {
    }

    public void on_status_changed(IPCam ipcam) {
    }

    public void on_local_record_result(IPCam ipcam, ERROR error) {
        if (error == ERROR.NO_ERROR) {
            Tools.showShortToast(this.m_context, getResources().getString(getResources().getIdentifier("record_started", "string", this.m_package_name)));
            return;
        }
        this.m_local_recording = false;
        this.play_record.setImageResource(getResources().getIdentifier("play_record", "drawable", this.m_package_name));
        this.m_ipcam.set_play_tf_record_cache(IPCam.PLAY_TF_RECORD_CACHE_NORMAL);
        this.m_download_record = false;
        this.download_record.setVisibility(GONE);
        Tools.showShortToast(this.m_context, getResources().getString(getResources().getIdentifier("record_failed", "string", this.m_package_name)));
    }

    public void on_tf_record_status_changed(IPCam ipcam) {
        switch (ipcam.tf_record_status()) {
            case PLAYING:
                this.tf_play.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("tf_stop", "drawable", this.m_package_name)));
                return;
            case STOPPED:
                stop_local_record(true);
                break;
            case PAUSING:
                break;
            default:
                return;
        }
        this.tf_play.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("tf_play", "drawable", this.m_package_name)));
    }

    public void on_tf_record_event(IPCam ipcam, boolean new_record, int record_id, boolean error) {
        if (new_record) {
            TF_RECORD_CLIP_TIME t = this.m_ipcam.get_tf_record_clip_time(record_id);
            if (t == null) {
                Log.e("sosocam", "get record info failed !!!");
                return;
            }
            if (!(this.m_day == t.day && this.m_hour == t.hour && this.m_quarter == t.quarter)) {
                set_record(t.day, t.hour, t.quarter);
            }
            if (!this.m_seeking) {
                this.m_seek_bar.setProgress(t.no);
            }
            this.m_no = t.no;
        }
    }

    public void on_result(IPCam ipcam, int day, int hour, int quarter, int no, byte[] thumb) {
        if (this.m_seeking && day == this.m_day && hour == this.m_hour && quarter == this.m_quarter && no == this.m_seeking_no) {
            if (thumb == null) {
                this.m_clip_thumb.setImageBitmap(null);
            } else {
                this.m_clip_thumb.setImageBitmap(BitmapFactory.decodeByteArray(thumb, 0, thumb.length));
            }
            this.m_progress_bar.setVisibility(GONE);
            this.m_clip_thumb.setVisibility(VISIBLE);
        }
    }

    public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
        if (this.m_seeking && fromUser) {
            this.m_progress_bar.setVisibility(GONE);
            this.m_clip_thumb.setVisibility(VISIBLE);
            int no = progress;
            if (this.m_clips_info == null || !this.m_clips_info[no].valid) {
                this.m_clip_time.setText(getResources().getIdentifier("invalid_clip", "string", this.m_package_name));
                this.m_clip_alarm.setText("");
                this.m_clip_thumb.setImageBitmap(null);
                return;
            }
            this.m_clip_time.setText(get_time_str(this.m_day, this.m_hour, this.m_quarter, no));
            if (this.m_clips_info[no].alarm == 0) {
                this.m_clip_alarm.setText("");
            } else if (this.m_clips_info[no].alarm == 1) {
                this.m_clip_alarm.setText(getResources().getIdentifier("md_alarm", "string", this.m_package_name));
            } else if (this.m_clips_info[no].alarm == 4) {
                this.m_clip_alarm.setText(getResources().getIdentifier("sd_alarm", "string", this.m_package_name));
            } else {
                this.m_clip_alarm.setText(getResources().getIdentifier("unknown_alarm", "string", this.m_package_name));
            }
            if (this.m_clips_info[no].thumb) {
                this.m_seeking_no = no;
                this.m_clip_thumb.setVisibility(GONE);
                this.m_progress_bar.setVisibility(VISIBLE);
                this.m_ipcam.get_tf_record_clip_thumb(this.m_day, this.m_hour, this.m_quarter, no, this);
                return;
            }
            this.m_clip_thumb.setImageBitmap(null);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        this.m_seeking = true;
        this.m_clip_info.setVisibility(VISIBLE);
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.m_seeking = false;
        this.m_clip_info.setVisibility(GONE);
        int no = this.m_seek_bar.getProgress();
        if (no != this.m_no) {
            this.m_no = no;
            if (this.m_listener != null) {
                this.m_listener.on_clip_selected(this.m_day, this.m_hour, this.m_quarter, this.m_no);
            }
        }
    }

    public void set_speed(int speed) {
        this.m_ipcam.set_record_performance_speed(speed);
        this.record_play_speed.setText(get_performance_speed_string(this.m_ipcam.record_performance_speed()));
    }

    public void stop_local_record(boolean show_tip) {
        if (this.m_local_recording) {
            this.m_ipcam.stop_local_record();
            this.m_local_recording = false;
            this.play_record.setImageResource(getResources().getIdentifier("play_record", "drawable", this.m_package_name));
            this.m_ipcam.set_play_tf_record_cache(IPCam.PLAY_TF_RECORD_CACHE_NORMAL);
            this.m_download_record = false;
            this.download_record.setVisibility(GONE);
            Tools.add_media(this.m_context, this.m_local_record_filepath);
            if (show_tip) {
                Tools.showShortToast(this.m_context, getResources().getString(getResources().getIdentifier("record_stopped", "string", this.m_package_name)));
            }
        }
    }
}
