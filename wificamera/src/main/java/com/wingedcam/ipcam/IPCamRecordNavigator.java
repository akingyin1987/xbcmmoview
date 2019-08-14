package com.wingedcam.ipcam;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.wingedcam.ipcam.IPCam.CONN_STATUS;
import com.wingedcam.ipcam.IPCam.ERROR;
import com.wingedcam.ipcam.IPCam.IPCam_Listener;
import com.wingedcam.ipcam.IPCam.TF_RECORD_DAY_INFO;
import com.wingedcam.ipcam.IPCam.TF_RECORD_QUARTER_INFO;
import com.wingedcam.ipcam.IPCam.TF_RECORD_QUARTER_TIME;
import com.wingedcam.ipcam.IPCam.get_tf_record_quarter_thumb_listener;
import com.wingedcam.ipcam.IPCam.load_tf_records_listener;
import java.text.MessageFormat;
import java.util.Arrays;

public class IPCamRecordNavigator extends LinearLayout implements IPCam_Listener, load_tf_records_listener {
    private int m_current_day = 0;
    private GridView m_grid_hour = null;
    private GridHourAdapter m_grid_hour_adapter = new GridHourAdapter();
    private LayoutInflater m_inflater;
    private TextView m_info = null;
    private IPCam m_ipcam = null;
    private ListView m_list_day = null;
    private ListDayAdapter m_list_day_adapter = new ListDayAdapter();
    private IPCamRecordNavigator_Listener m_listener;
    private LOAD_STATE m_loadrecord_state = LOAD_STATE.UNLOAD;
    String m_package_name;
    private ProgressBar m_progress_bar = null;
    private RECORD_DAY_INFO[] m_record_info = null;

    public interface IPCamRecordNavigator_Listener {
        boolean on_record_loaded();

        void on_record_selected(int i, int i2, int i3);
    }

    private enum LOAD_STATE {
        UNLOAD,
        LOADING,
        LOADED
    }

    class ListDayAdapter extends BaseAdapter {
        private int position_flag = -1;

        ListDayAdapter() {
        }

        public int getCount() {
            if (IPCamRecordNavigator.this.m_record_info == null) {
                return 1;
            }
            return IPCamRecordNavigator.this.m_record_info.length + 1;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        @SuppressLint({"NewApi"})
        public View getView(int position, View convertView, ViewGroup parent) {
            int index = 0;
            if (position != 0) {
                index = 8 - position;
            }
            if (convertView == null) {
                convertView = IPCamRecordNavigator.this.m_inflater.inflate(IPCamRecordNavigator.this.getResources().getIdentifier("listday_item", "layout", IPCamRecordNavigator.this.m_package_name), null);
            }
            LinearLayout listday_item = (LinearLayout) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("listday_item", "id", IPCamRecordNavigator.this.m_package_name));
            final ImageView listday_item_status = (ImageView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("listday_item_status", "id", IPCamRecordNavigator.this.m_package_name));
            View listday_item_top = convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("listday_item_top", "id", IPCamRecordNavigator.this.m_package_name));
            View listday_item_bottom = convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("listday_item_bottom", "id", IPCamRecordNavigator.this.m_package_name));
            TextView listday_item_prompt = (TextView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("listday_item_prompt", "id", IPCamRecordNavigator.this.m_package_name));
            final View listday_item_prompt_under_line = convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("listday_item_prompt_under_line", "id", IPCamRecordNavigator.this.m_package_name));
            listday_item_prompt_under_line.setVisibility(View.GONE);
            if (this.position_flag != index) {
                listday_item_prompt_under_line.setVisibility(View.GONE);
                listday_item_status.setImageResource(IPCamRecordNavigator.this.getResources().getIdentifier("dot", "drawable", IPCamRecordNavigator.this.m_package_name));
            } else {
                listday_item_prompt_under_line.setVisibility(View.VISIBLE);
                listday_item_status.setImageResource(IPCamRecordNavigator.this.getResources().getIdentifier("dot_alarm_active", "drawable", IPCamRecordNavigator.this.m_package_name));
                listday_item_status.setMaxHeight(30);
                listday_item_status.setMaxWidth(30);
            }
            if (IPCamRecordNavigator.this.m_current_day != index - 1) {
                listday_item_prompt_under_line.setVisibility(View.GONE);
                listday_item_status.setImageResource(IPCamRecordNavigator.this.getResources().getIdentifier("dot", "drawable", IPCamRecordNavigator.this.m_package_name));
            } else {
                listday_item_prompt_under_line.setVisibility(VISIBLE);
                listday_item_status.setImageResource(IPCamRecordNavigator.this.getResources().getIdentifier("dot_alarm_active", "drawable", IPCamRecordNavigator.this.m_package_name));
                listday_item_status.setMaxHeight(30);
                listday_item_status.setMaxWidth(30);
            }
            listday_item.setTag(Integer.valueOf(index));
            listday_item.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    IPCamRecordNavigator.this.OnListDayItemClick(((Integer) arg0.getTag()).intValue());
                    listday_item_prompt_under_line.setVisibility(VISIBLE);
                    listday_item_status.setImageResource(IPCamRecordNavigator.this.getResources().getIdentifier("dot_alarm_active", "drawable", IPCamRecordNavigator.this.m_package_name));
                    ListDayAdapter.this.position_flag = ((Integer) arg0.getTag()).intValue();
                    IPCamRecordNavigator.this.m_list_day_adapter.notifyDataSetChanged();
                }
            });
            if (position == 0) {
                listday_item_top.setBackgroundColor(0);
                listday_item_bottom.setBackgroundColor(-16728051);
            } else if (position == 7) {
                listday_item_top.setBackgroundColor(-16728051);
                listday_item_bottom.setBackgroundColor(0);
            } else {
                listday_item_top.setBackgroundColor(-16728051);
                listday_item_bottom.setBackgroundColor(-16728051);
            }
            if (position == 0) {
                listday_item.setEnabled(true);
                listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("reload", "string", IPCamRecordNavigator.this.m_package_name));
                listday_item_prompt.setTextColor(-1);
            } else {
                index--;
                listday_item.setEnabled(IPCamRecordNavigator.this.m_record_info[index].valid);
                if (!listday_item.isEnabled()) {
                    listday_item_prompt.setTextColor(-7829368);
                    listday_item_status.setImageResource(IPCamRecordNavigator.this.getResources().getIdentifier("dot_empty", "drawable", IPCamRecordNavigator.this.m_package_name));
                }
                if (IPCamRecordNavigator.this.m_record_info[index].today) {
                    listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("today", "string", IPCamRecordNavigator.this.m_package_name));
                } else if (IPCamRecordNavigator.this.m_record_info[index].yesterday) {
                    listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("yesterday", "string", IPCamRecordNavigator.this.m_package_name));
                } else if (IPCamRecordNavigator.this.m_record_info[index].week == 1) {
                    listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("sunday", "string", IPCamRecordNavigator.this.m_package_name));
                } else if (IPCamRecordNavigator.this.m_record_info[index].week == 2) {
                    listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("monday", "string", IPCamRecordNavigator.this.m_package_name));
                } else if (IPCamRecordNavigator.this.m_record_info[index].week == 3) {
                    listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("tuesday", "string", IPCamRecordNavigator.this.m_package_name));
                } else if (IPCamRecordNavigator.this.m_record_info[index].week == 4) {
                    listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("wednesday", "string", IPCamRecordNavigator.this.m_package_name));
                } else if (IPCamRecordNavigator.this.m_record_info[index].week == 5) {
                    listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("thursday", "string", IPCamRecordNavigator.this.m_package_name));
                } else if (IPCamRecordNavigator.this.m_record_info[index].week == 6) {
                    listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("friday", "string", IPCamRecordNavigator.this.m_package_name));
                } else if (IPCamRecordNavigator.this.m_record_info[index].week == 7) {
                    listday_item_prompt.setText(IPCamRecordNavigator.this.getResources().getIdentifier("saturday", "string", IPCamRecordNavigator.this.m_package_name));
                }
            }
            return convertView;
        }
    }

    private class RECORD_DAY_INFO {
        public boolean alarm;
        public RECORD_HOUR_INFO[] hours;
        public boolean today;
        public boolean valid;
        public int valid_hours;
        public int week;
        public boolean yesterday;

        private RECORD_DAY_INFO() {
        }


    }

    private class RECORD_HOUR_INFO {
        public int hour;
        public RECORD_QUARTER_INFO[] quarters;

        private RECORD_HOUR_INFO() {
        }


    }

    private class RECORD_QUARTER_INFO {
        public boolean alarm;
        public LOAD_STATE load_state;
        public byte[] thumb;
        public boolean valid;

        private RECORD_QUARTER_INFO() {
        }


    }

    class GridHourAdapter extends BaseAdapter implements get_tf_record_quarter_thumb_listener {
        GridHourAdapter() {
        }

        public int getCount() {
            if (IPCamRecordNavigator.this.m_record_info != null && IPCamRecordNavigator.this.m_record_info[IPCamRecordNavigator.this.m_current_day].valid) {
                return IPCamRecordNavigator.this.m_record_info[IPCamRecordNavigator.this.m_current_day].valid_hours;
            }
            return 0;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = IPCamRecordNavigator.this.m_inflater.inflate(IPCamRecordNavigator.this.getResources().getIdentifier("gridhour_item", "layout", IPCamRecordNavigator.this.m_package_name), null);
            }
            TextView title = (TextView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("title", "id", IPCamRecordNavigator.this.m_package_name));
            int hour = IPCamRecordNavigator.this.m_record_info[IPCamRecordNavigator.this.m_current_day].hours[position].hour;
            title.setText(MessageFormat.format("{0}:00 - {1}:00", hour, hour + 1));
            for (int q = 0; q < 4; q++) {
                LinearLayout quarter;
                ProgressBar progress;
                ImageView image;
                ImageView alarm;
                RECORD_QUARTER_INFO info = IPCamRecordNavigator.this.m_record_info[IPCamRecordNavigator.this.m_current_day].hours[position].quarters[q];
                if (q == 0) {
                    quarter = (LinearLayout) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("quarter1", "id", IPCamRecordNavigator.this.m_package_name));
                    progress = (ProgressBar) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("progress1", "id", IPCamRecordNavigator.this.m_package_name));
                    image = (ImageView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("image1", "id", IPCamRecordNavigator.this.m_package_name));
                    alarm = (ImageView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("alarm_1", "id", IPCamRecordNavigator.this.m_package_name));
                } else if (q == 1) {
                    quarter = (LinearLayout) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("quarter2", "id", IPCamRecordNavigator.this.m_package_name));
                    progress = (ProgressBar) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("progress2", "id", IPCamRecordNavigator.this.m_package_name));
                    image = (ImageView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("image2", "id", IPCamRecordNavigator.this.m_package_name));
                    alarm = (ImageView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("alarm_2", "id", IPCamRecordNavigator.this.m_package_name));
                } else if (q == 2) {
                    quarter = (LinearLayout) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("quarter3", "id", IPCamRecordNavigator.this.m_package_name));
                    progress = (ProgressBar) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("progress3", "id", IPCamRecordNavigator.this.m_package_name));
                    image = (ImageView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("image3", "id", IPCamRecordNavigator.this.m_package_name));
                    alarm = (ImageView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("alarm_3", "id", IPCamRecordNavigator.this.m_package_name));
                } else {
                    quarter = (LinearLayout) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("quarter4", "id", IPCamRecordNavigator.this.m_package_name));
                    progress = (ProgressBar) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("progress4", "id", IPCamRecordNavigator.this.m_package_name));
                    image = (ImageView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("image4", "id", IPCamRecordNavigator.this.m_package_name));
                    alarm = (ImageView) convertView.findViewById(IPCamRecordNavigator.this.getResources().getIdentifier("alarm_4", "id", IPCamRecordNavigator.this.m_package_name));
                }
                if (info.valid) {
                    //IPCam access$1000 = IPCamRecordNavigator.this.m_ipcam;
                    //access$1000.getClass();
                    TF_RECORD_QUARTER_TIME t = new TF_RECORD_QUARTER_TIME();
                    t.day = IPCamRecordNavigator.this.m_current_day;
                    t.hour = hour;
                    t.quarter = q;
                    quarter.setTag(t);
                    quarter.setOnClickListener(new OnClickListener() {
                        public void onClick(View arg0) {
                            IPCamRecordNavigator.this.OnGridHourItemClick((TF_RECORD_QUARTER_TIME) arg0.getTag());
                        }
                    });
                    if (info.alarm) {
                        alarm.setVisibility(VISIBLE);
                    } else {
                        alarm.setVisibility(GONE);
                    }
                    if (info.load_state == LOAD_STATE.UNLOAD) {
                        progress.setVisibility(VISIBLE);
                        image.setVisibility(INVISIBLE);
                        if (ERROR.NO_ERROR != IPCamRecordNavigator.this.m_ipcam.get_tf_record_quarter_thumb(IPCamRecordNavigator.this.m_current_day, hour, position, q, this)) {
                            progress.setVisibility(INVISIBLE);
                            image.setVisibility(VISIBLE);
                        } else {
                            info.load_state = LOAD_STATE.LOADING;
                        }
                    } else if (info.load_state == LOAD_STATE.LOADING) {
                        progress.setVisibility(VISIBLE);
                        image.setVisibility(INVISIBLE);
                    } else if (info.load_state == LOAD_STATE.LOADED) {
                        progress.setVisibility(INVISIBLE);
                        if (info.thumb != null) {
                            image.setImageBitmap(BitmapFactory.decodeByteArray(info.thumb, 0, info.thumb.length));
                        } else {
                            image.setImageResource(IPCamRecordNavigator.this.getResources().getIdentifier("picture_lost", "drawable", IPCamRecordNavigator.this.m_package_name));
                        }
                        image.setVisibility(VISIBLE);
                    }
                } else {
                    progress.setVisibility(INVISIBLE);
                    image.setImageResource(IPCamRecordNavigator.this.getResources().getIdentifier("picture_lost", "drawable", IPCamRecordNavigator.this.m_package_name));
                    alarm.setVisibility(GONE);
                    image.setVisibility(VISIBLE);
                    quarter.setOnClickListener(null);
                }
            }
            return convertView;
        }

        public void on_result(IPCam ipcam, int day, int hour, int valid_hour_index, int quarter, byte[] thumb) {
            if (IPCamRecordNavigator.this.m_loadrecord_state == LOAD_STATE.LOADED) {
                RECORD_QUARTER_INFO info = null;
                if (day >= 0 && day < 7 && hour >= 0 && hour < 24 && quarter >= 0 && quarter < 4 && valid_hour_index >= 0 && valid_hour_index < IPCamRecordNavigator.this.m_record_info[day].valid_hours && IPCamRecordNavigator.this.m_record_info[day].hours[valid_hour_index].hour == hour) {
                    info = IPCamRecordNavigator.this.m_record_info[day].hours[valid_hour_index].quarters[quarter];
                }
                if (info == null || info.load_state == LOAD_STATE.UNLOAD) {
                    notifyDataSetChanged();
                    return;
                }
                info.load_state = LOAD_STATE.LOADED;
                info.thumb = thumb;
                IPCamRecordNavigator.this.m_grid_hour_adapter.notifyDataSetChanged();
            }
        }
    }

    private void cancel_current_aysnc_tasks() {
        this.m_ipcam.cancel_tf_record_tasks();
        if (this.m_record_info != null) {
            for (int i = 0; i < this.m_record_info[this.m_current_day].valid_hours; i++) {
                for (int q = 0; q < 4; q++) {
                    if (this.m_record_info[this.m_current_day].hours[i].quarters[q].load_state == LOAD_STATE.LOADING) {
                        this.m_record_info[this.m_current_day].hours[i].quarters[q].load_state = LOAD_STATE.UNLOAD;
                    }
                }
            }
        }
    }

    private void OnListDayItemClick(int position) {
        if (position == 0) {
            unload_record();
            load_record();
            return;
        }
        position--;
        if (this.m_current_day != position) {
            cancel_current_aysnc_tasks();
            this.m_current_day = position;
            this.m_grid_hour.setAdapter(this.m_grid_hour_adapter);
        }
    }

    private void OnGridHourItemClick(TF_RECORD_QUARTER_TIME t) {
        if (this.m_listener != null) {
            cancel_current_aysnc_tasks();
            this.m_listener.on_record_selected(t.day, t.hour, t.quarter);
        }
    }

    public IPCamRecordNavigator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.m_inflater = LayoutInflater.from(context);
        this.m_package_name = context.getPackageName();
        this.m_inflater.inflate(getResources().getIdentifier("navigator", "layout", this.m_package_name), this);
        this.m_list_day = (ListView) findViewById(getResources().getIdentifier("listDay", "id", this.m_package_name));
        this.m_list_day.setAdapter(null);
        this.m_grid_hour = (GridView) findViewById(getResources().getIdentifier("gridHour", "id", this.m_package_name));
        this.m_grid_hour.setAdapter(null);
        this.m_progress_bar = (ProgressBar) findViewById(getResources().getIdentifier("progressBar", "id", this.m_package_name));
        this.m_progress_bar.setVisibility(INVISIBLE);
        this.m_info = (TextView) findViewById(getResources().getIdentifier("info", "id", this.m_package_name));
        this.m_info.setVisibility(INVISIBLE);
    }

    public void init(IPCam ipcam, IPCamRecordNavigator_Listener listener) {
        if (ipcam != null) {
            deinit();
            this.m_listener = listener;
            this.m_ipcam = ipcam;
            this.m_ipcam.add_listener(this);
            if (this.m_ipcam.status() == CONN_STATUS.CONNECTED) {
                load_record();
            }
        }
    }

    public void deinit() {
        this.m_listener = null;
        if (this.m_ipcam != null) {
            this.m_ipcam.remove_listener(this);
            unload_record();
            this.m_ipcam = null;
        }
    }

    private void load_record() {
        if (this.m_loadrecord_state == LOAD_STATE.UNLOAD) {
            this.m_record_info = null;
            this.m_list_day.setAdapter(null);
            this.m_grid_hour.setAdapter(null);
            this.m_progress_bar.setVisibility(VISIBLE);
            this.m_info.setText(getResources().getIdentifier("loading", "string", this.m_package_name));
            this.m_info.setVisibility(VISIBLE);
            if (ERROR.NO_ERROR != this.m_ipcam.load_tf_records(this)) {
                this.m_progress_bar.setVisibility(INVISIBLE);
                this.m_info.setText(getResources().getIdentifier("load_fail", "string", this.m_package_name));
                this.m_list_day.setAdapter(this.m_list_day_adapter);
                return;
            }
            this.m_loadrecord_state = LOAD_STATE.LOADING;
        }
    }

    private void unload_record() {
        if (this.m_loadrecord_state != LOAD_STATE.UNLOAD) {
            this.m_loadrecord_state = LOAD_STATE.UNLOAD;
            this.m_list_day.setAdapter(null);
            this.m_grid_hour.setAdapter(null);
            this.m_progress_bar.setVisibility(INVISIBLE);
            this.m_info.setVisibility(INVISIBLE);
            this.m_ipcam.clear_tf_records();
            this.m_record_info = null;
        }
    }

    public void refresh() {
        this.m_grid_hour_adapter.notifyDataSetChanged();
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

    public void on_camera_recording_changed(IPCam ipcam) {
    }

    public void on_statistic(IPCam ipcam) {
    }

    public void on_can_set_video_performance(IPCam ipcam) {
    }

    public void on_camera_battery_changed(IPCam ipcam) {
    }

    public void on_camera_alarm_ioout(IPCam ipcam) {
    }

    public void on_status_changed(IPCam ipcam) {
        if (ipcam.status() == CONN_STATUS.CONNECTED) {
            load_record();
        } else {
            unload_record();
        }
    }

    public void on_local_record_result(IPCam ipcam, ERROR error) {
    }

    public void on_tf_record_status_changed(IPCam ipcam) {
    }

    public void on_tf_record_event(IPCam ipcam, boolean new_record, int record_id, boolean error) {
    }

    @SuppressLint({"NewApi"})
    private static RECORD_DAY_INFO[] TurnArray(RECORD_DAY_INFO[] a) {
        RECORD_DAY_INFO[] b = new RECORD_DAY_INFO[a.length];
        for (int i = 0; i < a.length; i++) {
            b[(b.length - 1) - i] = ((RECORD_DAY_INFO[]) Arrays.copyOfRange(a, i, i + 1))[0];
        }
        return b;
    }

    public void on_result(IPCam ipcam, ERROR error) {
        if (this.m_loadrecord_state != LOAD_STATE.UNLOAD) {
            this.m_progress_bar.setVisibility(INVISIBLE);
            if (error != ERROR.NO_ERROR) {
                this.m_loadrecord_state = LOAD_STATE.UNLOAD;
                this.m_info.setText(getResources().getIdentifier("load_fail", "string", this.m_package_name));
                this.m_info.setVisibility(VISIBLE);
                this.m_list_day.setAdapter(this.m_list_day_adapter);
                this.m_grid_hour.setAdapter(null);
                return;
            }
            if (this.m_listener != null && this.m_listener.on_record_loaded()) {
                new Handler().post(new Runnable() {
                    public void run() {
                        IPCamRecordNavigator.this.cancel_current_aysnc_tasks();
                    }
                });
            }
            this.m_loadrecord_state = LOAD_STATE.LOADED;
            this.m_info.setVisibility(INVISIBLE);
            this.m_record_info = new RECORD_DAY_INFO[7];
            this.m_current_day = 0;
            for (int d = 0; d < 7; d++) {
                this.m_record_info[d] = new RECORD_DAY_INFO();
                TF_RECORD_DAY_INFO day_info = this.m_ipcam.get_tf_record_day_info(d);
                this.m_record_info[d].alarm = day_info.alarm;
                this.m_record_info[d].today = day_info.today;
                this.m_record_info[d].valid = day_info.valid;
                this.m_record_info[d].week = day_info.week;
                this.m_record_info[d].yesterday = day_info.yesterday;
                this.m_record_info[d].valid_hours = day_info.valid_hours;
                if (this.m_record_info[d].valid) {
                    this.m_current_day = d;
                    this.m_record_info[d].hours = new RECORD_HOUR_INFO[this.m_record_info[d].valid_hours];
                    int vh = 0;
                    for (int h = 0; h < 24; h++) {
                        if (this.m_ipcam.get_tf_record_hour_valid(d, h)) {
                            this.m_record_info[d].hours[vh] = new RECORD_HOUR_INFO();
                            this.m_record_info[d].hours[vh].hour = h;
                            this.m_record_info[d].hours[vh].quarters = new RECORD_QUARTER_INFO[4];
                            for (int q = 0; q < 4; q++) {
                                this.m_record_info[d].hours[vh].quarters[q] = new RECORD_QUARTER_INFO();
                                TF_RECORD_QUARTER_INFO quarter_info = this.m_ipcam.get_tf_record_quarter_info(d, h, q);
                                this.m_record_info[d].hours[vh].quarters[q].valid = quarter_info.valid;
                                this.m_record_info[d].hours[vh].quarters[q].alarm = quarter_info.alarm;
                                this.m_record_info[d].hours[vh].quarters[q].load_state = LOAD_STATE.UNLOAD;
                                this.m_record_info[d].hours[vh].quarters[q].thumb = null;
                            }
                            vh++;
                        }
                    }
                }
            }
            this.m_list_day.setAdapter(this.m_list_day_adapter);
            this.m_grid_hour.setAdapter(this.m_grid_hour_adapter);
        }
    }
}
