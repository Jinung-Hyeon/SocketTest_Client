package com.test.sockettestclient;

import static com.test.sockettestclient.constant.Constants.IMAGE_URL;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final String TAG = "AlarmTest";

    // 고유 식별자 - 디바이스 ANDROID_ID 사용
    public static String ANDROID_ID = "";


    //로컬 IP사용시 127.0.0.1로 ip 변경하기
    private final String ip = "127.0.0.1";
    //private final String ip = "10.0.2.16";  // 에뮬레이터 ip
    private final String port = "5001";

    // 일과시간 생성해주는 클래스 정의
    WorkTime workTime = new WorkTime();

    // WatchDog과 소켓통신할 소켓 변수
    public Socket socket;     //클라이언트의 소켓
    public DataInputStream is;
    public DataOutputStream os;
    public static int endSignal = 0;

    // 뒤로가기 두번 누를때 사용할 변수
    private long backKeyPressedTime = 0;
    private Toast toast;

    // UDP 소켓통신 스레드
    private SocketThread socketthread;
    private UdpThread udpThread;

    private ImageView imageView;

    static int toggle = 0;

    // TTS
    private TextToSpeech tts;

    // 서버에서 받아온 json 정보를 담을 ArrayList 정의. (사용할 json 정보 : speechTime, text, contentPaths)
    ArrayList<Integer> speechTimeList;
    ArrayList<String> textList, contentsPathList;

    String androidId = "6f2d4597912ecc39";
    PlayImage playImage;


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
            // 화면 켜주는 기능
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            // 화면 절전 모드 해제
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Log.e(TAG, "화면 계속 켜기 ON!");

            Log.e(TAG, "일과시간입니다. 소켓을 실행합니다.");
            socketthread = new SocketThread();
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

        playImage = new PlayImage();

        tts = new TextToSpeech(this,this);

        speechTimeList = new ArrayList<>();
        textList = new ArrayList<>();
        contentsPathList = new ArrayList<>();

        imageView = findViewById(R.id.imageView);
        ANDROID_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

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
            int result = tts.setLanguage(Locale.KOREA); // TTS언어 한국어로 설정
            Log.e(TAG, "TTS 세팅완료 ");

            if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                Log.e("TTS", "This Language is not supported");
            }
            RetrofitService retrofitService = RetrofitFactory.create();
            retrofitService.getPosts(androidId)
                    .enqueue(new Callback<ObjectResult>() {
                        @Override
                        public void onResponse(Call<ObjectResult> call, Response<ObjectResult> response) {
//                        if(!response.isSuccessful()){
//                            textViewResult.setText("code : " + response.code());
//                            return;
//                        }

                            speechTimeList.clear();
                            textList.clear();
                            contentsPathList.clear();

                            ObjectResult objectResults = response.body();

                            Log.e(TAG, "레트로핏 성공");
                            //Log.d("msg", objectResults.toString());
                            for (int i = 0; i < objectResults.contents.size(); i++ ){
                                //Log.d("msg", objectResults.contents.get(i).toString());
                                Log.d(TAG, objectResults.contents.get(i).getContentsPath());
                                speechTimeList.add(objectResults.contents.get(i).speechTime);
                                textList.add(objectResults.contents.get(i).text);
                                contentsPathList.add(objectResults.contents.get(i).contentsPath);
                            }

                            Log.d("msg", "" + speechTimeList);
                            Log.d("msg", String.valueOf(textList));
                            Log.d("msg", String.valueOf(contentsPathList));


                            //iv.setImageResource(R.drawable.did_basic_image);
                            playImage.start();
                        }

                        @Override
                        public void onFailure(Call<ObjectResult> call, Throwable t) {
                            //textViewResult.setText(t.getMessage());
                            Log.d("msg", t.toString());
                        }

                    });
        }else{
            Log.e("TTS", "Initialization Failed!");
        }
    }

    // WatchDog과 TCP소켓통신 (앱이 죽었는지 살았는지 알기위한 통신)
    class SocketThread extends Thread{
        @Override
        public void run() {
            if (System.currentTimeMillis() > workTime.finishWorkTime().getTimeInMillis()) {
                Log.e(TAG, "업무시간이 아닙니다. (일과종료 이후 조건문)");
            } else if (System.currentTimeMillis() < workTime.startWorkTime().getTimeInMillis()) {  // 새벽에 켜졌을때 시간 비교 후 꺼지게
                Log.e(TAG, "업무시간이 아닙니다. (일과시작전 조건문)");
            } else {
                try {
                    //서버와 연결하는 소켓 생성
                    if (socket == null || socket.isClosed()){
                        socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
                        is = new DataInputStream(socket.getInputStream());
                        os = new DataOutputStream(socket.getOutputStream());
                    }
                    os.write(endSignal);
                    os.flush();
                } catch (Exception e) {
                    Log.e(TAG, " 서버 소켓이 안열려 있는듯? WatchDog 죽은거아님? 다시실행 ㄱㄱ : " + e);
                    // WatchDog앱이 죽어있을경우 앱을 다시 실행시켜줌.
                    getPackageList();
                    if(!socket.isClosed() || socket != null){
                        try {
                            is.close();
                            os.flush();
                            socket.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    class PlayImage extends Thread {
        @Override
        public void run() {
            //Log.e(TAG, "스레드 이름 : " + Thread.currentThread().getName());
            try {
                int i = 0;
                while (true) {
                    int j = i;
                    //Log.e(TAG, "i1 : " + i);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MainActivity.this)
                                    .load(IMAGE_URL + contentsPathList.get(j))
                                    .into(imageView);
                            speakOut(textList.get(j));
                            Log.e(TAG, textList.get(j));
                        }
                    });
                    if (Thread.currentThread().interrupted()){
                        Log.e(TAG, "스레드 종료됨!");
                        break;
                    }
                    Log.e(TAG, "슬립들어옴");
                    Thread.sleep(speechTimeList.get(i) * 1000);
                    i++;
                    //Log.e(TAG, "i2 : " + i);

                    // 리스트 다 돌면 다시 처음부터 재생하기위해 i값 0으로 초기화
                    if (i == contentsPathList.size()){
                        i = 0;
                        Log.e(TAG, "i가 큽니다. 초기화시킵니다." + i);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    // TTS
    private void speakOut(String ttsText){
        CharSequence text = ttsText;
        Log.d("msg", "ttsText : " + text);
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
            Log.d("msg", "log in speakOut() method : " + arr);
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
                tts.stop();
                tts.shutdown();
                //Log.e(TAG, "플레이스레드 이름 : " + playImage.getName());
                playImage.interrupt();

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
        tts.stop();
        tts.shutdown();
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    // 앱을 다시 실행시켜주는 메소드
    public void getPackageList() {
        //SDK30이상은 Manifest권한 추가가 필요 출처:https://inpro.tistory.com/214
        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith("com.test.sokettestserver")){
                    Log.d(TAG, "실행시킴");
                    break;
                }
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.test.sokettestserver");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}