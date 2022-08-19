package com.test.sockettestclient.socket;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.test.sockettestclient.worktime.WorkTime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class SocketThread extends Thread{
    private final String TAG = "AlarmTest";

    //로컬 IP사용시 127.0.0.1로 ip 변경하기
    private final String ip = "127.0.0.1";
    //private final String ip = "10.0.2.16";  // 에뮬레이터 ip
    private final String port = "5001";

    WorkTime workTime = new WorkTime();

    // WatchDog과 소켓통신할 소켓 변수
    public Socket socket;     //클라이언트의 소켓
    public DataInputStream is;
    public DataOutputStream os;
    public static int endSignal = 0;

    private Context mContext;

    public SocketThread(Context context){
        this.mContext = context;
    }

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

    public void getPackageList() {
        //SDK30이상은 Manifest권한 추가가 필요 출처:https://inpro.tistory.com/214
        PackageManager pkgMgr = mContext.getPackageManager();
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
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage("com.test.sokettestserver");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
