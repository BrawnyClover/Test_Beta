package com.yuhyun.testbeta;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.yuhyun.testbeta.utils.AudioWriterPCM;
import com.naver.speech.clientapi.SpeechRecognitionResult;
import com.yuhyun.testbeta.utils.TeachChatBotProcess;
import com.yuhyun.testbeta.utils.TextToSpeechProcess;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String CLIENT_ID = "s3JZvlVVcSinh6kiS5QK";
    public static Context mContext;
    TextToSpeechProcess ttsProcess;
    ProcessAction processAction;
    CustomAdapter m_Adapter;
    ListView m_ListView;

    private RecognitionHandler handler;
    private NaverRecognizer naverRecognizer;

    private EditText txtResult; // 하단의 명령어 입력 에딧 텍스트 박스
    private Button btnStart; // 하단의 마이크 아이콘 버튼
    private String mResult; // api를 통해 얻은 입력 명령어
    private boolean isHearing = false;

    private AudioWriterPCM writer;
    // Handle speech recognition Messages.
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.clientReady: // 들을 준비가 되었다
                // Now an user can speak.
                txtResult.setText("듣는 중...");
                writer = new AudioWriterPCM(
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/NaverSpeechTest");
                writer.open("Test");
                break;

            case R.id.audioRecording: // 기록 중이다
                writer.write((short[]) msg.obj);
                break;

            case R.id.partialResult: // 기록된 결과 반환
                // Extract obj property typed with String.
                mResult = (String) (msg.obj);
                txtResult.setText(mResult);
                break;

            case R.id.finalResult: // 최종 결과 반환. api에선 5개의 결과를 제공한다.
                // Extract obj property typed with String array.
                // The first element is recognition result for speech.
            	SpeechRecognitionResult speechRecognitionResult = (SpeechRecognitionResult) msg.obj;
            	List<String> results = speechRecognitionResult.getResults();
            	StringBuilder strBuf = new StringBuilder();
            	for(String result : results) {
            		strBuf.append(result);
            		strBuf.append("\n");
            	}
                mResult = strBuf.toString();
                txtResult.setText(mResult);
                refresh(results.get(0),1); // 5개의 결과 중 첫 번째 것만 사용하자. 편의상

                processAction = new ProcessAction(results.get(0),this,this); // 명령어 처리 작업
                String reaction = processAction.reAction(); // 일단 반응을 얻고
                refresh(reaction,0); // 반응을 보여준 다음

                String res = processAction.processOrder(); // 실제 명령 처리
                if(res!=null){
                    refresh(res,0);
                }
                txtResult.setText("");
                break;

            case R.id.recognitionError: // 인식 오류
                if (writer != null) {
                    writer.close();
                }

                mResult = "Error code : " + msg.obj.toString();
                txtResult.setText(mResult);
                btnStart.setBackgroundResource(R.drawable.button_wh);
                btnStart.setEnabled(true);
                break;

            case R.id.clientInactive:
                if (writer != null) {
                    writer.close();
                }

                btnStart.setBackgroundResource(R.drawable.button_wh);
                btnStart.setEnabled(true);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한 부여를 쉽게 하기 위해 외부 라이브러리 사용. 마산이가 알려주었습니다.
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
            }
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            }
        };

        // 권한 부여 창을 나타냅니다.
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
//                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.INTERNET, // 인터넷 연결 여부를 확인하기 위해, 인터넷 검색을 위해
                        Manifest.permission.RECORD_AUDIO, // 음성 인식을 위해
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, // xml 파일 저장 등을 위해
                        Manifest.permission.READ_EXTERNAL_STORAGE, // xml 파일 불러오기 등을 위해
                        Manifest.permission.CAMERA, // 카메라 실행을 위해
                        Manifest.permission.SEND_SMS, // 문자를 보내기 위해
                        Manifest.permission.RECEIVE_SMS, // 문자를 받...기 위해
                        Manifest.permission.CALL_PHONE, // 전화를 걸기 위해
                        Manifest.permission.READ_PHONE_STATE, // 전화를 걸기 위해
                        Manifest.permission.READ_CONTACTS, // 전화를 걸거나 문자를 보내기 위해 전화번호부에서 정보 가져오기
                        Manifest.permission.WRITE_CONTACTS, // 딱히 전화번호부를 생성하지는 않지만 없으면 뭔가 허전하니까
                        Manifest.permission.READ_CALENDAR, // 일정을 위해 달력을 가져오기
                        Manifest.permission.ACCESS_NETWORK_STATE) // 인터넷 연결 여부를 확인하기 위해
                .check(); // 췤!
//        Intent intent = getIntent();
//        Boolean bool = intent.getExtras().getBoolean("isCalled");
//        if(bool!=true) {
//            NameReceiver nameReceiver = new NameReceiver(this, this);
//            nameReceiver.setIntent();
//        }

        txtResult = (EditText) findViewById(R.id.txt_result);
        btnStart = (Button) findViewById(R.id.btn_start);
        ttsProcess = new TextToSpeechProcess(this);

        mContext = this;

        handler = new RecognitionHandler(this);
        naverRecognizer = new NaverRecognizer(this, handler, CLIENT_ID);

        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean taskOk = true;
                if(!naverRecognizer.getSpeechRecognizer().isRunning()) { // 인식 작업이 진행되고 있지 아니한 경우
                    // Start button is pushed when SpeechRecognizer's state is inactive.
                    // Run SpeechRecongizer by calling recognize().
                    mResult = "";
                    txtResult.setText("연결 중...");

                    if(getNetworkInfo() == null){ // 네트워크에 연결되어있지 않을 경우
                        txtResult.setText("");
                        refresh("연결 실패! 연결 상태를 확인해주세요...",0);
                        taskOk = false;
                    }
                    else {
                        btnStart.setBackgroundResource(R.drawable.button_on);
                        naverRecognizer.recognize();
                        taskOk = true;
                    }
                } else { // 인식 작업이 진행되고 있을 경우
                    Log.d(TAG, "stop and wait Final Result");
                    btnStart.setEnabled(false);
                    naverRecognizer.getSpeechRecognizer().stop();
                }
                if(taskOk == true) { // 버튼을 눌렀을 때, 버튼 입력에 대한 반응을 내보냄. 편의를 위해 별도의 if문으로 분리
                    if (isHearing == false) {
                        ttsProcess.speechProcess("Hearing");
                        isHearing = true;
                    } else if (isHearing == true) {
                        ttsProcess.speechProcess("Processing");
                        isHearing = false;
                    }
                }
            }
        });
        m_Adapter = new CustomAdapter(); // listview 어댑터
        m_ListView = (ListView) findViewById(R.id.listView1);
        m_ListView.setAdapter(m_Adapter);
        final Activity activity = this;

        // 텍스트로 명령을 줄 경우에 대한 처리
        findViewById(R.id.textOrderBtn).setOnClickListener(new Button.OnClickListener() {
              @Override
              public void onClick(View v) {
                  String order = txtResult.getText().toString();
                  ProcessAction processAction = new ProcessAction(order, getApplicationContext(), activity);
                  refresh(order,1);
                  txtResult.setText("");
                  String reaction = processAction.reAction();
                  refresh(reaction,0);
                  String res = processAction.processOrder();
                  if(res!=null){
                      refresh(res,0);
                  }
              }
          }
        );

        refresh("What can I do for you?",0);// 초기 메세지 출력, 한국어로 말하는 건 컨셉이다. 이게 바로 한국형
        TeachChatBotProcess teachChatBotProcess = new TeachChatBotProcess(this);
        teachChatBotProcess.initXml(); // 잡담 기능을 위한 xml 파일 설정
    }
    public void refresh (String inputValue, int _str) { // listview에 요소 추가, tts 기능 실행
        m_Adapter.add(inputValue,_str) ;
        m_Adapter.notifyDataSetChanged();
        if(_str == 0) {
            ttsProcess.speechProcess(inputValue);
        }
    }
    @Override
    protected void onStart() {
    	super.onStart();
    	// NOTE : initialize() must be called on start time.
    	naverRecognizer.getSpeechRecognizer().initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mResult = "";
        txtResult.setText("");
        btnStart.setBackgroundResource(R.drawable.button_wh);
        btnStart.setEnabled(true);
    }

    @Override
    protected void onStop() {
    	super.onStop();
    	// NOTE : release() must be called on stop time.
    	naverRecognizer.getSpeechRecognizer().release();
    }

    // Declare handler for handling SpeechRecognizer thread's Messages.
    static class RecognitionHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        RecognitionHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }
    public void onImageClick(View view){
        refresh("안녕하세요, TestBeta입니다.",0);
    } // 상단의 앱 아이콘 이미지를 터치했을 때의 반응
    private NetworkInfo getNetworkInfo() // 네트워크에 연결되어 있는지 확인하는 함수
    {
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        return netInfo;
    }
}
