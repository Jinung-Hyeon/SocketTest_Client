package com.test.sockettestclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    Socket socket;     //클라이언트의 소켓
    DataInputStream is;
    DataOutputStream os;

    String ip;
    String port;

    TextView text_msg;  //서버로 부터 받은 메세지를 보여주는 TextView
    EditText edit_msg;  //서버로 전송할 메세지를 작성하는 EditText
    EditText edit_ip;   //서버의 IP를 작성할 수 있는 EditText
    EditText edit_port;
    Button btn_connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edit_ip = findViewById(R.id.ip);
        edit_port = findViewById(R.id.port);
        btn_connect = findViewById(R.id.connect);
        edit_msg = findViewById(R.id.msg);
        text_msg = findViewById(R.id.chatting);
    }

    public void ClientSocketOpen(View view) {
        ip = "192.168.0.50";
        port = "5001";
        //ip = edit_ip.getText().toString(); //EditText에 적은 IP주소 얻어오기
        //port = edit_port.getText().toString();

        if (ip.isEmpty() || port.isEmpty()) {
            Toast.makeText(MainActivity.this, "ip주소와 포트번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "서버 접속완료", Toast.LENGTH_SHORT).show();
            new Thread((Runnable) () -> {
                try {
                    //서버와 연결하는 소켓 생성
                    socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    //서버와 메세지를 주고받을 통로 구축
                    is = new DataInputStream(socket.getInputStream());
                    os = new DataOutputStream(socket.getOutputStream());


                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Connected With Server", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close(); //소켓을 닫는다.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}