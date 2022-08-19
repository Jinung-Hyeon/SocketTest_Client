package com.test.sockettestclient.contentsdb;

import android.content.Context;
import android.util.Log;

import com.test.sockettestclient.retrofit.ContentsResult;

import java.util.List;

public class QueryContents {

    private final String TAG = "AlarmTest";
    private Context mContext;
    private List<ContentsResult> contentsResultList;
    private AppDatabase db;

    public QueryContents(Context context, List<ContentsResult> list){
        this.mContext = context;
        this.contentsResultList = list; // 서버로 받아온 JSON정보
    }

    public boolean updateCheck(){
        db = AppDatabase.getDBInstance(mContext.getApplicationContext());
        List<Contents> contentsList = db.contentsDao().getAllContents();
        //Log.e(TAG, "contentsListSize : " + contentsList.size() + " contentsResultListSize : " + contentsResultList.size());
        if (contentsResultList.size() != contentsList.size()){ // 서버로 받아온 정보(최신)의 리스트 사이즈 != 기존 내부DB 리스트 사이즈 => 내용이 업데이트 되었다. 업데이트 된 내용으로 내부DB 교체
            db.contentsDao().deleteAllContents(); // 기존 내부DB 데이터 삭제하고
            insertContentsDB(); // 새로운 정보를 내부DB 에 저장
            return true;
        } else if (contentsResultList.size() == contentsList.size()){ // 최신 정보 리스트 사이즈 == 기존 내부DB 리스트 사이즈 => 내용은 바뀌었는데 사이즈가 같을수도있음. 이럴땐 최신정보 리스트와 기존 내부DB 리스트간 idx 검사
            for (int i = 0; i < contentsList.size(); i++){
                if (contentsList.get(i).idx != contentsResultList.get(i).getIdx()){ // 기존 내부DB i번째 idx값 != 최신 정보 i번째 idx값 => 내용이 바뀌었다. 업데이트 된 내용으로 내부DB 교체
                    db.contentsDao().deleteAllContents(); // 기존 내부DB 데이터 삭제하고
                    insertContentsDB(); // 새로운 정보를 내부DB 에 저장
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    // json 정보 내부 DB에 저장
    public void insertContentsDB(){
        Contents contents = new Contents();

        db = AppDatabase.getDBInstance(mContext.getApplicationContext());
        List<Contents> contentsList = db.contentsDao().getAllContents();

        if (contentsList.size() != 0){
            db.contentsDao().deleteAllContents();
        }

        for (int i = 0; i < contentsResultList.size(); i++){
            Log.d("ROOMMSG", contentsResultList.toString());
            contents.idx = contentsResultList.get(i).getIdx();
            contents.text = contentsResultList.get(i).getText();
            contents.imgUrl = contentsResultList.get(i).getContentsPath();
            contents.speechTime = contentsResultList.get(i).getSpeechTime();


            db.contentsDao().insertContents(contents);

        }
    }
}
