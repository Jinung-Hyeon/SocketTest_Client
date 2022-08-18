package com.test.sockettestclient;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class InfoSaveDialog extends Dialog {

    EditText edt_ip, edt_port;
    String ip_text, port_text;
    Button btn_save;

    public InfoSaveDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.infosave_dialog);
        edt_ip = findViewById(R.id.edt_ip);
        edt_port = findViewById(R.id.edt_port);
        btn_save = findViewById(R.id.btn_save);


        //edt_ip.requestFocus(); // EditText에 자동 포커스
        edt_ip.setInputType(0);
        edt_port.setInputType(0);

        ip_text = PreferenceManager.getIpString(getContext(), "ip");

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ip_text.length() == 0 || port_text.length() == 0) {
                    PreferenceManager.setIpString(getContext(), "ip", edt_ip.getText().toString());
                    SplashActivity.IP = edt_ip.getText().toString();
                    Log.e("msg", "저장된 정보 : ip = " + ip_text);
                    dismiss();

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Intent i = new Intent(getContext(), MainActivity.class);
                    getContext().startActivity(i);

                }
            }
        });
    }
}
