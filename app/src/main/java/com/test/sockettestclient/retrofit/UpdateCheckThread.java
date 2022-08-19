package com.test.sockettestclient.retrofit;

import static com.test.sockettestclient.MainActivity.playImageStop;
import static com.test.sockettestclient.MainActivity.requestServerStop;
import static com.test.sockettestclient.SplashActivity.ANDROID_ID;

import android.content.Context;
import android.util.Log;

import com.test.sockettestclient.contentsdb.QueryContents;
import com.test.sockettestclient.worktime.WorkTime;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateCheckThread extends Thread {

    private WorkTime workTime = new WorkTime();

    private final String TAG = "AlarmTest";

    private QueryContents queryContents;

    private Context mContext;
    private List<ContentsResult> contentsResultList;

    public UpdateCheckThread(Context context) {
        this.mContext = context;
    }

    @Override
    public void run() {
        while (requestServerStop){
            try {
                Thread.sleep(5 * 6 * 10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                RetrofitService retrofitService = RetrofitFactory.create();
                retrofitService.getPosts(ANDROID_ID)
                        .enqueue(new Callback<ObjectResult>() {
                            @Override
                            public void onResponse(Call<ObjectResult> call, Response<ObjectResult> response) {
//                        if(!response.isSuccessful()){
//                            textViewResult.setText("code : " + response.code());
//                            return;
//                        }

                                ObjectResult objectResults = response.body();
                                contentsResultList = objectResults.contents;

                                Log.e(TAG, "서버 요청 성공 (UpdateCheckThread.class)");
                                //Log.d("msg", objectResults.toString());


                                queryContents = new QueryContents(mContext, contentsResultList);

                                if(queryContents.updateCheck() == true){
                                    Log.e(TAG, "업데이트 된 내용이 있음! (UpdateCheckThread.class)");
                                    queryContents.insertContentsDB();
                                    playImageStop = false;
                                } else {
                                    Log.e(TAG, "업데이트 된 내용이 없음! (UpdateCheckThread.class)");
                                }

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

    }


}
