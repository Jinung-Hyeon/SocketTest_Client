package com.test.sockettestclient.retrofit;

import static com.test.sockettestclient.MainActivity.playImageStop;
import static com.test.sockettestclient.SplashActivity.ANDROID_ID;
import static com.test.sockettestclient.constant.Constants.IMAGE_URL;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.test.sockettestclient.MainActivity;
import com.test.sockettestclient.R;
import com.test.sockettestclient.contentsdb.AppDatabase;
import com.test.sockettestclient.contentsdb.Contents;
import com.test.sockettestclient.contentsdb.QueryContents;
import com.test.sockettestclient.worktime.WorkTime;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitStart{

    private final String TAG = "AlarmTest";
    private WorkTime workTime = new WorkTime();

    private QueryContents queryContents;
    private List<ContentsResult> contentsResultList;

    private PlayImage playImage;

    private Context mContext;
    private Activity activity;
    private ImageView imageView;
    private TextToSpeech tts;

    public RetrofitStart(Context context, Activity activity, TextToSpeech tts){
        this.activity = activity;
        this.mContext = context;
        this.tts = tts;
        this.imageView = activity.findViewById(R.id.imageView);
    }

    public void startRetrofit(){
        try {
            RetrofitService retrofitService = RetrofitFactory.create();
            retrofitService.getPosts(ANDROID_ID)
                    .enqueue(new Callback<ObjectResult>() {
                        @Override
                        public void onResponse(Call<ObjectResult> call, Response<ObjectResult> response) {
                        if(!response.isSuccessful()){
                            Log.e(TAG, "code : " + response.code());
                            playImage = new PlayImage();
                            playImage.start();
                            return;
                        }

                            ObjectResult objectResults = response.body();
                            contentsResultList = objectResults.contents;

                            Log.e(TAG, "서버 요청 성공 (RetrofitStart.class)");
                            //Log.d("msg", objectResults.toString());


                            queryContents = new QueryContents(mContext, contentsResultList);
                            queryContents.insertContentsDB();

                            playImage = new PlayImage();
                            playImage.start();

                        }

                        @Override
                        public void onFailure(Call<ObjectResult> call, Throwable t) {
                            //textViewResult.setText(t.getMessage());
                            Log.d("msg", t.toString());
                        }

                    });

        } catch (Exception e) {

        }
    }



    class PlayImage extends Thread {
        @Override
        public void run() {
            //Log.e(TAG, "스레드 이름 : " + Thread.currentThread().getName());
            AppDatabase db = AppDatabase.getDBInstance(mContext.getApplicationContext());
            List<Contents> contentsList = db.contentsDao().getAllContents();
            Log.e(TAG, "(PlayImage) contentsListSize : " + contentsList.size());
            try {
                int i = 0;
                int toggle = 0;
                while (playImageStop) {
                    int j = i;
                    if (contentsList.size() != 0){ // 내부 DB에 저장되어있는 데이터가 있다면
                        //Log.e(TAG, "i1 : " + i);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(activity)
                                        //.load(IMAGE_URL + contentsPathList.get(j))
                                        //.load(IMAGE_URL + roomDbData.get(j).contentsPath)
                                        .load(IMAGE_URL + contentsList.get(j).imgUrl)
                                        .listener(new RequestListener<Drawable>() {
                                            // 이미지 로드 실패시
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                //Log.e(TAG, "이미지 로딩 실패");
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                //Log.e(TAG, "이미지 로딩 성공");
                                                return false;
                                            }
                                        })
                                        .error(R.drawable.errorimg)
                                        .into(imageView);
                                speakOut(contentsList.get(j).text);
                                Log.e(TAG, contentsList.get(j).text);
                            }
                        });
                        if (Thread.currentThread().interrupted()) {
                            Log.e(TAG, "스레드 종료됨!");
                            break;
                        }
                        //Log.e(TAG, "슬립들어옴");
                        Thread.sleep(contentsList.get(i).speechTime * 1000);
                        i++;
                        //Log.e(TAG, "i2 : " + i);

                        // 리스트 다 돌면 다시 처음부터 재생하기위해 i값 0으로 초기화
                        if (i == contentsList.size()) {
                            i = 0;
                            //Log.e(TAG, "i가 큽니다. 초기화시킵니다." + i);
                        }
                    } else if (contentsList.size() == 0) { // 내부 DB에 저장되어있는 데이터가 없다면 에러이미지를 계속 재생. 이후 서버에 계속 접속하는
                        if (toggle == 0){
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(activity)
                                            .load(R.drawable.errorimg)
                                            .into(imageView);
                                }
                            });
                            toggle++;
                        } else {
                            Log.e(TAG, "서버가 아직 응답하지 않음 (RetrofitStart.class)");
                            Thread.sleep(3 * 6 * 10 * 1000);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Log.e(TAG, "무한루프 종료");
            if (System.currentTimeMillis() < workTime.finishWorkTime().getTimeInMillis()){ // 일과시간중 서버에 내용이 업데이트 되었을때 다시 새로운 내용으로 플레이 하기위해 만든 조건문
                rePlay();
            }
        }
    }

    // TTS
    private void speakOut(String ttsText){
        MainActivity ma = new MainActivity();
        ma.speakOut(ttsText, tts);
    }

    // playImage 재시작
    private void rePlay(){
        //Log.e(TAG, "다시시작");
        playImageStop = true;
        PlayImage playImage = new PlayImage();
        playImage.start();
    }
}
