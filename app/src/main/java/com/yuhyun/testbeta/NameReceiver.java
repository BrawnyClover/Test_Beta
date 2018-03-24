package com.yuhyun.testbeta;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

/**
 * Created by sonbill on 2017-08-15.
 */

    // 이 class는 없는 것과 다를 바 없는 유령 클래스입니다.
    // Siri의 "Siri야~" 나 구글 어시스턴트의 "Ok Google"과 같은 기능을 구현하고 싶었는데
    // 생각보다 너무 번거로워서 이건 나중에 다시 도전해보기로 하죠

public class NameReceiver {
    Context context;
    Activity activity;
    public NameReceiver(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }
    public void setIntent() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        SpeechRecognizer mRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        ReceiveNameService recSer = new ReceiveNameService(mRecognizer,listener,i);
        recSer.startService(i);
    }
    private RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {
        }
        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            Intent intent = new Intent(context,ReceiveNameService.class);
            activity.stopService(intent);
            activity.startService(intent);
        }

        @Override
        public void onResults(Bundle results) {
            ///
            String key= "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            if(mResult.get(0).equals("김승환")){
                Intent dialogIntent = new Intent(context, MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(dialogIntent);
            }
            else{
                Intent intent = new Intent(context,ReceiveNameService.class);
                activity.stopService(intent);
                activity.startService(intent);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };
}
