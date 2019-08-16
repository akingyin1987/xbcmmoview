package com.xbcmmoview.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import com.xbcmmoview.R;
import com.xbcmmoview.application.WifiCamApplication;

public class ExitAppDialog extends Dialog {
    private WifiCamApplication app;
    private Button cancel;
    private LayoutInflater inflater = null;
    private Activity m_context;
    private View main_group = null;
    private Button sure;

    public ExitAppDialog(Activity context) {
        super(context, R.style.Dialog);
        inflater = LayoutInflater.from(context);
        main_group = inflater.inflate(R.layout.dialog_exit_app, null, false);
        setTitle("");
        this.m_context = context;
        setContentView(this.main_group);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.app = (WifiCamApplication) getContext().getApplicationContext();
        this.cancel = (Button) findViewById(R.id.cancel);
        this.sure = (Button) findViewById(R.id.sure);
        this.sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExitAppDialog.this.dismiss();
                ExitAppDialog.this.app.exit();
            }
        });
        this.cancel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ExitAppDialog.this.dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
