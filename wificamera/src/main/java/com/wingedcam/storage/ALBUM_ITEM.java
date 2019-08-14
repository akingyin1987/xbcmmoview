package com.wingedcam.storage;

import android.widget.ImageView;
import java.io.Serializable;
import java.util.Date;

public class ALBUM_ITEM implements Serializable, Cloneable {
    private static final long serialVersionUID = 1;
    private String duration;
    public String file_name;
    private boolean image;
    public ImageView image_view;
    public boolean no_player;
    private String path;
    public boolean selected = false;
    private long size;
    private Date t;
    public String video_path;

    public String getFile_name() {
        return this.file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public boolean getImage() {
        return this.image;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    public Date getDate() {
        return this.t;
    }

    public void setDate(Date t) {
        this.t = t;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ALBUM_ITEM)) {
            return false;
        }
        ALBUM_ITEM u = (ALBUM_ITEM) obj;
        if (this.image == u.image && this.t.equals(u.t)) {
            return true;
        }
        return false;
    }

    public Object clone() throws CloneNotSupportedException {
        ALBUM_ITEM item = new ALBUM_ITEM();
        item.setImage(this.image);
        item.setSize(this.size);
        item.setDate(this.t);
        item.setPath(this.path);
        item.video_path = this.video_path;
        item.file_name = this.file_name;
        return item;
    }
}
