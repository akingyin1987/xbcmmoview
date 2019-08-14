package com.xbcmmoview.adapter;

import java.util.ArrayList;

public class AblumFiles {
    public String date;
    public ArrayList<FilesItem> files_list = new ArrayList();

    public class FilesItem {
        public boolean isImage = true;
        public String path = "";
    }
}
