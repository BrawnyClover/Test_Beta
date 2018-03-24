package com.yuhyun.testbeta.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.Locale;

/**
 * Created by sonbill on 2017-08-18.
 */

// 구글 tts를 사용하여 좀 더 자비스 같은 느낌이 나게 하자! 라는 생각에 구글 tts 기능을 사용해보았습니다.

public class TextToSpeechProcess implements OnInitListener {
    String speechText;
    TextToSpeech textToSpeech;
    Context context;
    public TextToSpeechProcess(Context context){
        this.context = context;
        textToSpeech = new TextToSpeech(context,this);
        textToSpeech.setLanguage(Locale.KOREA);
        textToSpeech.setSpeechRate(1);
    }

    public void speechProcess(String str)
    {
        speechText = str;
        textToSpeech.speak(speechText, TextToSpeech.QUEUE_ADD, null); // 이거 deprecated라 하는데 된다. 혹시 모르니까 다른 방법이 있는지 알아보자.
    }
    public void onInit(int status) {
        textToSpeech.speak(speechText, TextToSpeech.QUEUE_ADD, null);
    } // ???
}
