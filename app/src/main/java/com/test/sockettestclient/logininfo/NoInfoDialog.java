package com.test.sockettestclient.logininfo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.test.sockettestclient.R;

public class NoInfoDialog extends Dialog {
    public NoInfoDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog);
        setCancelable(false); // 외부 클릭시 창닫기 금지
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.test.sockettestclient.logininfo.InfoSaveDialog infoSaveDialog = new com.test.sockettestclient.logininfo.InfoSaveDialog(getContext());
                infoSaveDialog.show();
                dismiss();
            }
        });

    }
}
