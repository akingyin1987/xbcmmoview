package com.wingedcam.storage;

import java.io.Serializable;

public class Device implements Serializable {
    private String alias = "";
    private int fps = 0;
    private String id = "";
    private String ip = "";
    private String new_version_path = "";
    private int port = 0;
    private String pwd = "";
    private String ssid = "";
    private String user = "Admin";
    private String version = "";
    private VERSION_STATE version_state = VERSION_STATE.NONE;

    public enum VERSION_STATE {
        NONE,
        LATEST_VERSION,
        UPDATEABLE
    }

    public void setVersion(String new_version) {
        this.version = new_version;
    }

    public String getVersion() {
        return this.version;
    }

    public void setAlias(String m_alias) {
        this.alias = m_alias;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setSSID(String m_ssid) {
        this.ssid = m_ssid;
    }

    public String getSSID() {
        return this.ssid;
    }

    public void setUser(String m_user) {
        this.user = m_user;
    }

    public String getUser() {
        return this.user;
    }

    public void setPwd(String m_pwd) {
        this.pwd = m_pwd;
    }

    public String getPwd() {
        return this.pwd;
    }

    public void setId(String m_id) {
        this.id = m_id;
    }

    public int getFps() {
        return this.fps;
    }

    public void setMaxFps(int m_fps) {
        this.fps = m_fps;
    }
}
