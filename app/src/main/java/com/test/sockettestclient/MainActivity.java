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
    String msg="";
    boolean isConnected=true;

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

                /*
                //서버와 접속이 끊길 때까지 무한반복하면서 서버의 메세지 수신
                while (isConnected) {
                    try {
                        msg = is.readUTF(); //서버부터 메세지가 전송되면 이를 UTF형식으로 읽어서 String으로 리턴
                        runOnUiThread(() -> {
                            text_msg.setText("Server : " + msg);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

               */
            }).start();
        }
    }
    /*
    public void SendMessage(View view) {
        if(os==null) return;   //서버와 연결되어 있지 않다면 전송불가..

        //네트워크 작업이므로 Thread 생성
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //서버로 보낼 메세지 EditText로 부터 얻어오기
                String msg= edit_msg.getText().toString();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String msg= edit_msg.getText().toString();
                            // TODO Auto-generated method stub
                            text_msg.setText("Client : " +msg);
                        }
                    });
                    os.writeUTF(msg);  //서버로 메세지 보내기.UTF 방식으로(한글 전송가능...)
                    os.flush();        //다음 메세지 전송을 위해 연결통로의 버퍼를 지워주는 메소드..

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }//run method..

        }).start(); //Thread 실행..
    }
    */

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