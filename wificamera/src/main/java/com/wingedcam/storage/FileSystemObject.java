package com.wingedcam.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileSystemObject {
    public static void getFiles(ArrayList<File> fileList, String path) {
        File[] allFiles = new File(path).listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                if (file.isFile()) {
                    fileList.add(file);
                }
            }
        }
    }

    public static ArrayList<File> get_file_list(String path) {
        ArrayList<File> fileList = new ArrayList();
        File[] allFiles = new File(path).listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                if (file.isFile()) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    public static void delete(String file_name) {
        File file = new File(file_name);
        if (file.exists()) {
            if (file.listFiles().length > 0) {
                for (File tmp : file.listFiles()) {
                    if (tmp.isDirectory()) {
                        delete(tmp.getAbsolutePath());
                    } else {
                        tmp.delete();
                    }
                }
            }
            file.delete();
        }
    }

    public static void delete(String file_name, Context context) {
        File file = new File(file_name);
        if (file.exists()) {
            if (file.listFiles().length > 0) {
                for (File tmp : file.listFiles()) {
                    if (tmp.isDirectory()) {
                        delete(tmp.getAbsolutePath(), context);
                    } else {
                        tmp.delete();
                    }
                }
            }
            file.delete();
        }
    }

    public static void delete_file(String file_name, Context context) {
        if (!TextUtils.isEmpty(file_name)) {
            Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            File file = new File(file_name);
            intent.setData(Uri.fromFile(file));
            context.sendBroadcast(intent);
            file.delete();
        }
    }

    public static void mkdir(String name) {
        new File(name).mkdir();
    }

    public static boolean is_exist_file(String name) {
        return new File(name).exists();
    }

    @SuppressLint({"DefaultLocale"})
    public static String getExternalPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static void write(Object obj, String file_name) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file_name));
            out.writeObject(obj);
            out.close();
        } catch (Exception e) {
        }
    }

    public static void write(List<Object> list, String file_name) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file_name));
            out.writeObject(list);
            out.close();
        } catch (Exception e) {
        }
    }

    public static Object read(String file_name) {
        Object obj = null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file_name));
            obj = in.readObject();
            in.close();
            return obj;
        } catch (Exception e) {
            return obj;
        }
    }

    public static void read(List<Object> list, String file_name) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file_name));
            list = (List) in.readObject();
            in.close();
        } catch (Exception e) {
        }
    }

    public static void write_file(byte[] data, String filename) {
        try {
            File f = new File(filename);
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            fOut.write(data);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
        }
    }

    public static boolean change_file_name(String old_file, String filename) {
        File file = new File(old_file);
        if (!file.exists()) {
            return false;
        }
        Log.e("rename", "old_file: " + old_file + " filename:" + filename);
        return file.renameTo(new File(Storage.get_album_folder() + "/" + filename + ".jpg"));
    }

    public static boolean change_record_name(String old_file, String filename) {
        File file = new File(old_file);
        if (!file.exists()) {
            return false;
        }
        Log.e("rename", "old_file: " + old_file + " filename:" + filename);
        return file.renameTo(new File(Storage.get_album_folder() + "/" + filename + ".mov"));
    }
}
