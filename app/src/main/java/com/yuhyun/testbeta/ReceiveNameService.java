package com.yuhyun.testbeta;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by sonbill on 2017-08-15.
 */

// 이 class는 없는 것과 다를 바 없는 유령 클래스입니다.
// Siri의 "Siri야~" 나 구글 어시스턴트의 "Ok Google"과 같은 기능을 구현하고 싶었는데
// 생각보다 너무 번거로워서... 이건 나중에 다시 도전해보기로 하죠

public class ReceiveNameService extends Service {
    SpeechRecognizer mRecognizer;
    RecognitionListener listener;
    Intent i;
    public ReceiveNameService(SpeechRecognizer mRecognizer,  RecognitionListener listener, Intent i){
        this.mRecognizer = mRecognizer;
        this.listener = listener;
        this.i = i;
    }
    public ReceiveNameService()
    {

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mRecognizer.startListening(i);
        System.out.println("start");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mRecognizer.stopListening();
        super.onDestroy();
    }
}
