package com.sosocam.rcipcam3x;

/**
 * @author king
 * @version V1.0
 * @ Description:
 * @ Date 2019/4/20 13:30
 */
public class DISCOVERED_CAMERA_INFO {

  public String alias;
  public String current_ip;
  public String current_mask;
  public boolean dhcp;
  public String dns1;
  public String dns2;
  public String fw_version;
  public String gateway;
  public boolean https;
  public String id;
  public int id_type;
  public String ip;
  public String mask;
  public int model;
  public int port;
  public int sensor_id;
  public String ui_version;
  public boolean used;

  @Override public String toString() {
    return "DISCOVERED_CAMERA_INFO{"
        + "alias='"
        + alias
        + '\''
        + ", current_ip='"
        + current_ip
        + '\''
        + ", current_mask='"
        + current_mask
        + '\''
        + ", dhcp="
        + dhcp
        + ", dns1='"
        + dns1
        + '\''
        + ", dns2='"
        + dns2
        + '\''
        + ", fw_version='"
        + fw_version
        + '\''
        + ", gateway='"
        + gateway
        + '\''
        + ", https="
        + https
        + ", id='"
        + id
        + '\''
        + ", id_type="
        + id_type
        + ", ip='"
        + ip
        + '\''
        + ", mask='"
        + mask
        + '\''
        + ", model="
        + model
        + ", port="
        + port
        + ", sensor_id="
        + sensor_id
        + ", ui_version='"
        + ui_version
        + '\''
        + ", used="
        + used
        + '}';
  }
}
