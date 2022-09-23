package com.test.sockettestclient.logininfo;

import static com.test.sockettestclient.LoginActivity.ANDROID_ID;
import static com.test.sockettestclient.constant.constants.BASE_URL;
import static com.test.sockettestclient.constant.constants.PORT;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.test.sockettestclient.LoginActivity;
import com.test.sockettestclient.MainActivity;
import com.test.sockettestclient.R;
import com.test.sockettestclient.retrofit.ObjectResult;
import com.test.sockettestclient.retrofit.RetrofitFactory;
import com.test.sockettestclient.retrofit.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoSaveDialog extends Dialog {

    private final String TAG = "AlarmTest";

    private EditText edt_ip;
    private String ip_text, url, domain;
    private Button btn_save;
    private TextView tv_infoSave, tv_andId;
    private int tvClickNum = 0;

    public InfoSaveDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infosave_dialog);

        setCancelable(false); // 외부 클릭시 창닫기 금지

        edt_ip = findViewById(R.id.edt_ip);
        btn_save = findViewById(R.id.btn_save);
        tv_infoSave = findViewById(R.id.tv_infoSave);
        tv_andId = findViewById(R.id.tv_andId);



        //edt_ip.requestFocus(); // EditText에 자동 포커스
        edt_ip.setInputType(0);

        ip_text = com.test.sockettestclient.logininfo.PreferenceManager.getIpString(getContext(), "ip");

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ip_text.length() == 0) {
                    domain = edt_ip.getText().toString(); // 입력받은 도메인 주소

                    // static BASE_URL 할당전에 임시 생성한 url로 서버접속 확인해 틀리면 다시 입력받고 맞으면 BASE_URL에할당
                    url = "http://" + domain + ":" + PORT + "/lifesupporter/";


                    RetrofitService retrofitService = RetrofitFactory.create(url);
                    retrofitService.getPost(ANDROID_ID, 87).enqueue(new Callback<ObjectResult>() {
                        @Override
                        public void onResponse(Call<ObjectResult> call, Response<ObjectResult> response) {
                            if(!response.isSuccessful()){ // 서버 요청 실패시
                                Log.e(TAG, "서버 연결실패 code : " + response.code() + "InfoSaveDialog");
                                return;
                            }
                            com.test.sockettestclient.logininfo.PreferenceManager.setIpString(getContext(), "ip", edt_ip.getText().toString());
                            dismiss();
                            LoginActivity.DOMAIN = domain;
                            BASE_URL = url;
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Intent i = new Intent(getContext(), MainActivity.class);
                            getContext().startActivity(i);
                        }

                        @Override
                        public void onFailure(Call<ObjectResult> call, Throwable t) {
                            Log.e(TAG, "서버 연결실패 : " + t + " InfoSaveDialog");
                            Toast.makeText(getContext(), "도메인 주소를 확인해주세요.", Toast.LENGTH_SHORT).show();
                            LoginActivity.DOMAIN = "";
                            edt_ip.setText("");
                        }
                    });
                }
            }
        });

        // 정보입력창 5번 클릭하면 ANDROID_ID텍스트뷰에 ANDROID_ID 출력
        tv_infoSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvClickNum++;
                if (tvClickNum == 5){
                    tv_andId.setText(ANDROID_ID);
                }
                Log.e(TAG, "정보입력 텍스트뷰 클릭! 클릭횟수 : " + tvClickNum);
            }
        });
    }

}
