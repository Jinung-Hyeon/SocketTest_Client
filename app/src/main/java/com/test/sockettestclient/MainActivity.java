package com.test.sockettestclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.test.sockettestclient.retrofit.RetrofitStart;
import com.test.sockettestclient.retrofit.UpdateCheckThread;
import com.test.sockettestclient.socket.SocketThread;
import com.test.sockettestclient.socket.UdpThread;
import com.test.sockettestclient.worktime.Alarm;
import com.test.sockettestclient.worktime.GoToSleepAlarm;
import com.test.sockettestclient.worktime.WorkTime;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private final String TAG = "AlarmTest";

    // 고유 식별자 - 디바이스 ANDROID_ID 사용 (SplashActivity 에서 생성한 아이디값 사용)
    public String ANDROID_ID = SplashActivity.ANDROID_ID;

    // PlayImage 스레드와 서버요청 스레드를 중지 시키기 위한 토글 변수
    public static boolean playImageStop = true;
    public static boolean requestServerStop = true;

    // 일과시간 생성해주는 클래스 정의
    WorkTime workTime = new WorkTime();

    // WatchDog과 소켓통신할 소켓 변수
    public Socket socket;     //클라이언트의 소켓

    // 뒤로가기 두번 누를때 사용할 변수
    private long backKeyPressedTime = 0;
    private Toast toast;

    // UDP 소켓통신 스레드
    private SocketThread socketthread;
    private UdpThread udpThread;

    public ImageView imageView;

    static int toggle = 0;

    // TTS
    private TextToSpeech mainTts;

    //private PlayImage playImage;
    private UpdateCheckThread updateCheckThread;
    private RetrofitStart retrofitStart;


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart!!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume!!" );

        if(System.currentTimeMillis() > workTime.finishWorkTime().getTimeInMillis()){ // 현재시간 > 일과종료시간 -> 일과종료 이후 조건문 : 화면 계속 켜기나 소켓통신을 하지않음
            Log.e(TAG, "업무시간이 아닙니다. (일과종료 이후 조건문)");
        } else if (System.currentTimeMillis() < workTime.startWorkTime().getTimeInMillis()) { // 현재시간 < 일과시작시간 -> 일과 시작전 동작 조건문 : 화면 계속 켜기나 소켓통신을 하지않음
            Log.e(TAG, "업무시간이 아닙니다. (일과시작전 조건문)");
        } else if(System.currentTimeMillis() < workTime.finishWorkTime().getTimeInMillis()){ // 현재시간 > 일과종료시간  -> 일과시간이니 화면계속켜기나 소켓통신을 계속 해야함
            // 화면 절전 모드 해제
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Log.e(TAG, "화면 계속 켜기 ON! (MainActivity - onResume)");

            Log.e(TAG, "일과시간입니다. 소켓을 실행합니다. (MainActivity - onResume)");
            socketthread = new SocketThread(this);
            socketthread.start();
        }

        Alarm alarm = new Alarm();
        IntentFilter filter = new IntentFilter();
        if (toggle == 0){
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            toggle++;
        }
        registerReceiver(alarm, filter);

        try {
            overridePendingTransition(0,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "여기는 mainActivity");

        mainTts = new TextToSpeech(this, this);

        imageView = findViewById(R.id.imageView);
        //Log.e(TAG, "ANDROIDID : " + ANDROID_ID);


        // 화면 절전 모드 해제
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ActionBar ac = getSupportActionBar();
        //ac.setTitle("2022년 지역 SW서비스사업화 사업 [지역현안해결형 SW개발]");
        ac.hide();


        // 알람 브로드캐스트 리시버에서 intent로 넘어오면 intent값을 받아서 처리리
        Intent intent = getIntent();
        String wakeUpValue = intent.getStringExtra("KeepScreenOn");
        String goToSleepValue = intent.getStringExtra("ScreenOff");
        Log.e(TAG, "wakeUpValue : " + wakeUpValue + ", goToSleepValue : " + goToSleepValue);



        if (goToSleepValue != null){ // 일과 시간 종료 알람 받고 넘오면 처리할 조건문. 화면 절전 모드 진입 시키고 5분뒤 TV꺼짐
            if (goToSleepValue.equals("ScreenOff")) {
                // 화면 절전 모드 진입
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Toast.makeText(this, "잠시 후 화면이 종료됩니다.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "잠시 후 화면이 종료됩니다.");
            }
        } else if (wakeUpValue != null){
            if (wakeUpValue.equals("KeepScreenOn")) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.e(TAG, "화면 계속 켜기 ON!");
            }
        } else if (wakeUpValue == null && goToSleepValue == null) { // 앱이 잘못 실행되어 시작, 종료 값이 없을때를 대비해 만든 조건문.
            Intent sendGoToSleepAlarmIntent = new Intent(this, GoToSleepAlarm.class);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            if(workTime.finishWorkTime().before(Calendar.getInstance())){
                workTime.finishWorkTime().add(Calendar.DATE, 1);
            }

            Log.e(TAG, "GoToSleep 알람 예약!!! 꺼짐 예약 시간 : " + dateFormat.format(workTime.finishWorkTime().getTimeInMillis()) + " || 현재시간 : " + dateFormat.format(System.currentTimeMillis()));
            PendingIntent goToSleepPendingIntent = PendingIntent.getBroadcast(this, 0, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager goToSleepAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            AlarmManager.AlarmClockInfo goToSleepAc = new AlarmManager.AlarmClockInfo(workTime.finishWorkTime().getTimeInMillis(), goToSleepPendingIntent);
            goToSleepAlarmManager.setAlarmClock(goToSleepAc, goToSleepPendingIntent);



        }

    }

    // TTS 엔진 세팅되었을때 동작.
    @Override
    public void onInit(int status) {
        // 정상적으로 세팅되면 레트로핏을 통해 서버에 json 요청
        if(status == TextToSpeech.SUCCESS){
            int result = mainTts.setLanguage(Locale.KOREA); // TTS언어 한국어로 설정
            Log.e(TAG, "TTS 세팅완료 ");

            if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                Log.e("TTS", "This Language is not supported");
            }

            updateCheckThread = new UpdateCheckThread(this);
            updateCheckThread.start();

            retrofitStart = new RetrofitStart(this, this, mainTts);
            retrofitStart.startRetrofit();



        }else{
            Log.e("TTS", "Initialization Failed!");
        }
    }

    // TTS
    public void speakOut(String ttsText, TextToSpeech tts){
        CharSequence text = ttsText;
        //Log.d("msg", "ttsText : " + text);
        //tts.speak(text, TextToSpeech.QUEUE_ADD, null, "id1");
        String[] splitText = text.toString().split("\r\n\r\n");


        tts.setPitch(1.0f); // 음성 톤 높이 지정
        tts.setSpeechRate(0.8f); // 음성 속도 지정

//        for (int i = 0; i < splitText.length; i++) {
//            tts.speak(splitText[i], TextToSpeech.QUEUE_ADD, null, "id1");
//            Log.d("msg", "log in speakOut() method : " + splitText[i]);
//            tts.playSilentUtterance(100, TextToSpeech.QUEUE_ADD, null);
//        }

        for (String arr : splitText){
            tts.speak(arr, TextToSpeech.QUEUE_ADD, null, "id1");
            //Log.d("msg", "log in speakOut() method : " + arr);
            tts.playSilentUtterance(100, TextToSpeech.QUEUE_ADD, null);
        }

        // 첫 번째 매개변수: 음성 출력을 할 텍스트
        // 두 번째 매개변수: 1. TextToSpeech.QUEUE_FLUSH - 진행중인 음성 출력을 끊고 이번 TTS의 음성 출력
        //                 2. TextToSpeech.QUEUE_ADD - 진행중인 음성 출력이 끝난 후에 이번 TTS의 음성 출력
        //tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1");
    }


    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // 홈 버튼 누르면
        Log.d(TAG, "homeKey: ");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause!!");
        try {
            // 소켓이 생성되어있으면 소켓 종료
            if(socket != null){
                socket.close();
            }

            if(System.currentTimeMillis() > workTime.finishWorkTime().getTimeInMillis()){
                Log.e(TAG, "onPause쪽 조건문. 업무시간이 아닙니다. (일과종료 이후 조건문)");
                mainTts.stop();
                mainTts.shutdown();
                playImageStop = false;
                requestServerStop = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean moveTaskToBack(boolean nonRoot) {
        return super.moveTaskToBack(nonRoot);
    }

    @Override
    public void finishAndRemoveTask() {
        super.finishAndRemoveTask();
    }

    // 뒤로가기버튼 이벤트
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backKeyPressed("뒤로가기 버튼을 한번 더 누르면 종료됩니다.", 5);
    }

    // 뒤로가기 두번 누르면 앱 종료시키는 메소드
    public void backKeyPressed(String msg, double time) {
        if (System.currentTimeMillis() > backKeyPressedTime + (time * 1000)) {
            try {
                // UDP 통신을 통해 WatchDog에 시그널 1을보냄 (의도적으로 껐다는 신호)
                UdpThread.toServerSignal = "1";
                udpThread = new UdpThread();
                udpThread.start();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            backKeyPressedTime = System.currentTimeMillis();
            showGuide(msg);
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            udpThread.interrupt();
            moveTaskToBack(true);
            finishAndRemoveTask();
            android.os.Process.killProcess(android.os.Process.myPid());
            ActivityCompat.finishAffinity(this);
            System.exit(0);
            toast.cancel();
        }
    }

    private void showGuide(String msg) {
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }


    @Override
    protected void onDestroy() {
        mainTts.stop();
        mainTts.shutdown();
        playImageStop = false;
        requestServerStop = false;
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }



}