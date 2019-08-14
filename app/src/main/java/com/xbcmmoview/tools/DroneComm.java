package com.xbcmmoview.tools;

import com.wingedcam.ipcam.IPCam;
import com.xbcmmoview.application.WingedCamApplication;

public class DroneComm {
    OnGetDataListener exp_data = null;
    boolean m_bRun = false;
    IPCam m_cam;
    Thread sendThread = null;

    public interface OnGetDataListener {
        byte[] OnGetData();
    }

    public DroneComm(IPCam cam) {
        this.m_cam = cam;
    }

    public void SetOnGetDataListener(OnGetDataListener data) {
        this.exp_data = data;
    }

    private void send2Drone() {
        if (this.exp_data != null) {
            byte[] data = this.exp_data.OnGetData();
            byte[] pkt = new byte[(data.length + 3)];
            pkt[0] = (byte) 102;
            System.arraycopy(data, 0, pkt, 1, data.length);
            pkt[data.length + 1] = (byte) (((byte) ((((data[0] ^ data[1]) ^ data[2]) ^ data[3]) ^ data[4])) & 255);
            pkt[data.length + 2] = (byte) -103;
            WingedCamApplication.getByte(pkt);
            this.m_cam.write_comm(pkt);
        }
    }

    public boolean start(final long send_interval) {
        if (this.sendThread == null) {
            this.sendThread = new Thread() {
                @Override
                public void run() {
                    while (DroneComm.this.m_bRun) {
                        DroneComm.this.send2Drone();
                        try {
                           Thread.sleep(send_interval);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            };
            this.m_bRun = true;
            this.sendThread.start();
        }
        return true;
    }

    public void stop() {
        this.m_bRun = false;
        this.sendThread = null;
    }

    public boolean sendNow() {
        if (this.sendThread == null) {
            return false;
        }
        this.sendThread.interrupt();
        return true;
    }
}
