package com.wingedcam.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.wingedcam.util.HttpClient;
import com.wingedcam.util.Tools;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

public class Storage {
    private static final String ALBUM_FOLDER = "/EyeAlbum";
    private static final String DEV_NAME_FILE = "/EyeVideoes.dat";
    private static final String DEV_USING_FILE = "/Using.dat";
    private static final String DEV_WORK_FOLDER = "/EyeVideoes";
    private static final String VIDEO_FOLDER = "/VIDEO";
    private static String data_path;
    private static String dev_path;
    private static ArrayList<Device> device_list = new ArrayList();
    private static LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.e("location", "location success: " + location.getLongitude() + location.getLatitude());
                Storage.locationManager.removeUpdates(Storage.locationListener);
                Storage.UploadInfo(location);
                return;
            }
            Log.i("location", "location failed");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i("location", "onStatusChanged");
        }

        public void onProviderEnabled(String provider) {
            Log.i("location", "onProviderEnabled");
        }

        public void onProviderDisabled(String provider) {
            Log.i("location", "onProviderDisabled");
        }
    };
    private static LocationManager locationManager = null;
    private static Context m_context;

    static class UploadInfoTask extends AsyncTask<String, Void, Void> {
        UploadInfoTask() {
        }

        protected Void doInBackground(String... params) {
            try {
                if (HttpClient.get_json(params[0], 30000) != null) {
                    Log.i("upload", "upload success");
                }
            } catch (Exception e) {
            }
            return null;
        }
    }

    public static void init(Context context) {
        if (m_context == null) {
            dev_path = context.getApplicationContext().getFilesDir().getAbsolutePath();
            data_path = FileSystemObject.getExternalPath();
            if (!FileSystemObject.is_exist_file(data_path + ALBUM_FOLDER)) {
                FileSystemObject.mkdir(data_path + ALBUM_FOLDER);
            }
            if (!FileSystemObject.is_exist_file(data_path + ALBUM_FOLDER + VIDEO_FOLDER)) {
                FileSystemObject.mkdir(data_path + ALBUM_FOLDER + VIDEO_FOLDER);
            }
            if (!FileSystemObject.is_exist_file(dev_path + DEV_WORK_FOLDER)) {
                FileSystemObject.mkdir(dev_path + DEV_WORK_FOLDER);
            }
        }
        device_list.clear();
        m_context = context;
        device_list = read_device_list();
    }

    public static void setVersion(String ssid, String version) {
        if (ssid != null && !ssid.equals("")) {
            Iterator it = device_list.iterator();
            while (it.hasNext()) {
                Device item = (Device) it.next();
                if (item.getSSID().equals(ssid)) {
                    item.setVersion(version);
                    write_device_list();
                    return;
                }
            }
        }
    }

    public static void setAlias(String ssid, String m_alias) {
        if (ssid != null && !ssid.equals("")) {
            Iterator it = device_list.iterator();
            while (it.hasNext()) {
                Device item = (Device) it.next();
                if (item.getSSID().equals(ssid)) {
                    item.setAlias(m_alias);
                    write_device_list();
                    return;
                }
            }
        }
    }

    public static void setUser(String ssid, String m_user) {
        if (ssid != null && !ssid.equals("")) {
            Iterator it = device_list.iterator();
            while (it.hasNext()) {
                Device item = (Device) it.next();
                if (item.getSSID().equals(ssid)) {
                    item.setUser(m_user);
                    write_device_list();
                    return;
                }
            }
        }
    }

    public static void setPwd(String ssid, String m_pwd) {
        if (ssid != null && !ssid.equals("")) {
            Iterator it = device_list.iterator();
            while (it.hasNext()) {
                Device item = (Device) it.next();
                if (item.getSSID().equals(ssid)) {
                    item.setPwd(m_pwd);
                    write_device_list();
                    return;
                }
            }
        }
    }

    public static void setMaxFps(int fps) {
        Iterator it = device_list.iterator();
        while (it.hasNext()) {
            Device item = (Device) it.next();
            if (item.getSSID().equals("MaxFpsValue")) {
                item.setMaxFps(fps);
                write_device_list();
                return;
            }
        }
    }

    public static int getMaxFps() {
        Iterator it = device_list.iterator();
        while (it.hasNext()) {
            Device item = (Device) it.next();
            if (item.getSSID().equals("MaxFpsValue")) {
                return item.getFps();
            }
        }
        return 0;
    }

    public static void remove_device(String ssid) {
        if (ssid != null && !ssid.equals("")) {
            Iterator it = device_list.iterator();
            while (it.hasNext()) {
                Device item = (Device) it.next();
                if (item.getSSID().equals(ssid)) {
                    device_list.remove(item);
                    write_device_list();
                    return;
                }
            }
        }
    }

    public static Device add_device(String alias, String ssid, String id, String user, String pwd, String version) {
        if (ssid == null || ssid.equals("")) {
            return null;
        }
        Device item;
        Iterator it = device_list.iterator();
        while (it.hasNext()) {
            item = (Device) it.next();
            if (item.getSSID().equals(ssid)) {
                item.setPwd(pwd);
                write_device_list();
                return null;
            }
        }
        item = new Device();
        item.setAlias(alias);
        item.setSSID(ssid);
        item.setUser(user);
        item.setPwd(pwd);
        item.setId(id);
        item.setVersion(version);
        device_list.add(item);
        write_device_list();
        return item;
    }

    public static Device get_device(String ssid) {
        Iterator it = device_list.iterator();
        while (it.hasNext()) {
            Device item = (Device) it.next();
            if (item.getSSID().equals(ssid)) {
                return item;
            }
        }
        return null;
    }

    public static ArrayList<Device> get_device_list() {
        return device_list;
    }

    private static ArrayList<Device> read_device_list() {
        ArrayList<Device> aircaft_list = (ArrayList) FileSystemObject.read(dev_path + DEV_WORK_FOLDER + DEV_NAME_FILE);
        if (aircaft_list == null) {
            return new ArrayList();
        }
        return aircaft_list;
    }

    private static void write_device_list() {
        FileSystemObject.write(device_list, dev_path + DEV_WORK_FOLDER + DEV_NAME_FILE);
    }

    public static String get_album_folder() {
        if (!FileSystemObject.is_exist_file(data_path + ALBUM_FOLDER)) {
            FileSystemObject.mkdir(data_path + ALBUM_FOLDER);
        }
        if (!FileSystemObject.is_exist_file(data_path + ALBUM_FOLDER + VIDEO_FOLDER)) {
            FileSystemObject.mkdir(data_path + ALBUM_FOLDER + VIDEO_FOLDER);
        }
        if (!FileSystemObject.is_exist_file(dev_path + DEV_WORK_FOLDER)) {
            FileSystemObject.mkdir(dev_path + DEV_WORK_FOLDER);
        }
        return data_path + ALBUM_FOLDER;
    }

    public static ArrayList<ALBUM_ITEM> get_album_item_list() {
        ArrayList<ALBUM_ITEM> album_list = new ArrayList();
        ArrayList<File> files = FileSystemObject.get_file_list(data_path + ALBUM_FOLDER);
        for (int i = 0; i < files.size(); i++) {
            File tmp = (File) files.get(i);
            if (tmp.isFile() && tmp.length() != 0) {
                ALBUM_ITEM item = new ALBUM_ITEM();
                String name = tmp.getName();
                if (name.matches("^.*?\\.(jpg)$")) {
                    item.setImage(true);
                } else {
                    item.setImage(false);
                }
                item.setSize(tmp.length());
                Date d = Tools.LongToDate(tmp.lastModified());
                if (d != null) {
                    item.setDate(d);
                }
                if (name != null) {
                    item.setFile_name(name.substring(0, name.lastIndexOf(".")));
                }
                item.setPath(tmp.getAbsolutePath());
                album_list.add(0, item);
            }
        }
        Collections.sort(album_list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((ALBUM_ITEM) o2).getDate().compareTo(((ALBUM_ITEM) o1).getDate());
            }
        });
        return album_list;
    }

    public static ArrayList<ALBUM_ITEM> get_record_item_list() {
        int i;
        ArrayList<ALBUM_ITEM> album_list = new ArrayList();
        ArrayList<ALBUM_ITEM> jpg_list = new ArrayList();
        ArrayList<ALBUM_ITEM> video_list = new ArrayList();
        ArrayList<File> files = FileSystemObject.get_file_list(data_path + ALBUM_FOLDER + VIDEO_FOLDER);
        Log.e("WingCam", "path: " + data_path + ALBUM_FOLDER + VIDEO_FOLDER + ", size: " + files.size());
        for (i = 0; i < files.size(); i++) {
            File tmp = (File) files.get(i);
            if (tmp.isFile() && tmp.getName().matches("^\\d*\\.\\w{3}$") && tmp.length() != 0) {
                ALBUM_ITEM item = new ALBUM_ITEM();
                String name = tmp.getName();
                if (name.matches("^\\d*\\.jpg$")) {
                    item.setImage(true);
                } else {
                    item.setImage(false);
                }
                item.setSize(tmp.length());
                Date d = Tools.StrToDate(name.substring(0, 14));
                if (d != null) {
                    item.setDate(d);
                }
                item.setPath(tmp.getAbsolutePath());
                if (item.getImage()) {
                    jpg_list.add(item);
                } else {
                    video_list.add(item);
                }
            }
        }
        for (i = 0; i < video_list.size(); i++) {
            ALBUM_ITEM v_item = (ALBUM_ITEM) video_list.get(i);
            int j = 0;
            while (j < jpg_list.size()) {
                ALBUM_ITEM j_item = (ALBUM_ITEM) jpg_list.get(i);
                if (v_item.getPath().subSequence(0, v_item.getPath().length() - 3).equals(j_item.getPath().subSequence(0, j_item.getPath().length() - 3))) {
                    try {
                        ALBUM_ITEM new_item = (ALBUM_ITEM) j_item.clone();
                        new_item.video_path = v_item.getPath();
                        album_list.add(0, new_item);
                        break;
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                } else {
                    j++;
                }
            }
        }
        Collections.sort(album_list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((ALBUM_ITEM) o2).getDate().compareTo(((ALBUM_ITEM) o1).getDate());
            }
        });
        return album_list;
    }

    public static void delete_album_file(String path) {
        if (FileSystemObject.is_exist_file(path)) {
            FileSystemObject.delete_file(path, m_context);
        }
    }

    public static byte[] read_album_file(String filename) {
        if (filename == null || filename.equals("")) {
            return null;
        }
        return (byte[]) FileSystemObject.read(data_path + ALBUM_FOLDER + "/" + filename);
    }

    public static void rename_album_file(String old_file, String filename) {
        FileSystemObject.change_file_name(old_file, filename);
    }

    public static void rename_record_file(String old_file, String filename) {
        FileSystemObject.change_record_name(old_file, filename);
    }

    private static void UploadInfo(Location location) {
        if (get_using_cam_id() != null) {
            Date date = new Date();
            new UploadInfoTask().execute(new String[]{null});
        }
    }
    @SuppressLint("MissingPermission")
    private static void start_location(Context context) {
        Log.e("location", "start_location: ");
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Log.e("location", "locationManager: " + locationManager);
        if (locationManager.getProvider("network") != null) {
            String provider = locationManager.getProvider("network").getName();
            Log.e("location", "provider: " + provider);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.e("location", "location: " + location);
            if (location == null) {
                locationManager.requestLocationUpdates(provider, 1000, 0.0f, locationListener);
                return;
            }
            Log.e("location", "location success: " + location.getLongitude() + location.getLatitude());
            UploadInfo(location);
        }
    }

    private static String get_using_cam_id() {
        String file_name = dev_path + DEV_WORK_FOLDER + DEV_USING_FILE;
        if (FileSystemObject.is_exist_file(file_name)) {
            return (String) FileSystemObject.read(file_name);
        }
        return null;
    }

    public static void send_location() {
    }
}
