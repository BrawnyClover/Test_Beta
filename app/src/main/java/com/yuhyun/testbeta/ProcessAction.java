package com.yuhyun.testbeta;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.telephony.SmsManager;

import com.yuhyun.testbeta.utils.ChatBotProcess;
import com.yuhyun.testbeta.utils.TeachChatBotProcess;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sonbill on 2017-08-13.
 */

    // 이 에플리케이션의 핵심인, 명령어 처리를 관장하는 class 입니다.
    // 몇 개의 class로 분리하고 싶지만 리펙토링은 나중에~

public class ProcessAction {
    List<String> orderList = new ArrayList(); // 전달 받은 문장을 어절 단위로 끊어 저장하는 list
    List<String> preOrdered = new ArrayList(); // 처음엔 간단한 명령만 하려고 해서 명령에 대한 정보를 모아놓은 list를 만들었지만, 기능이 추가되면서 왜 이렇게 했을까 후회가 된다.
    Context context;
    Activity activity;
    String order;
    public ProcessAction(String str, Context context, Activity activity){
        order = str;
        this.context = context;
        this.activity = activity;
    }
    public void setPreOrdered()
    {
        // 잠깐만 이거 심각한데
        // 명령을 처리할 때마다 이 함수를 호출하니까
        preOrdered.add(context.getString(R.string.call_kor));
        preOrdered.add(context.getString(R.string.camera_kor));
        preOrdered.add(context.getString(R.string.calender_kor));
        preOrdered.add(context.getString(R.string.sms_kor));
        preOrdered.add(context.getString(R.string.search_kor));
        preOrdered.add(context.getString(R.string.music_kor));
        // 이런거 반복한다는 얘기네
        // 맙소사;
        try{
            orderList = Arrays.asList(order.split(" ")); // 어절 단위로 분리하여 list에 저장
        }catch (NullPointerException e){
            // 음
        }
    }
    public String findWho(String name) // 전화번호부에서 사람 찾기
    {
        Uri people = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 전화번호부 uri 가져오기
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        }; // 가져올 정보에 대한 String 배열

        Cursor cursor = context.getContentResolver().query(people, projection, null, null, null);
        int end = cursor.getCount();
        String phone = "";
        int count = 0;
        boolean flag = false;
        if(cursor.moveToFirst())
        {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while(cursor.moveToNext() || count > end) {
                if (name.equals(cursor.getString(nameIndex))) {
                    phone = cursor.getString(phoneIndex);
                    flag = true;
                }
            }
            if(flag == false){
                return null;
            }
        }
        // 탐색 끝
        return phone; // 전화번호 반환
    }
    public String reAction()
    {
        setPreOrdered();
        int endIndex = orderList.size(); // 명령의 마지막 어절
        endIndex--; // list는 0번부터 시작하니까 1을 빼줍니다.
        try {
            if (orderList.get(endIndex).equals(preOrdered.get(1))) {//카메라
                return "Operating Camera...";
            } else if (orderList.get(endIndex).equals(preOrdered.get(2))) {//일정
                return "일정을 확인합니다.";
            }  else if (orderList.get(endIndex).equals(preOrdered.get(4))) {//검색
                return "검색 중...";
            } else if (orderList.get(endIndex).equals(preOrdered.get(5))) {//음악
                return "음악 목록을 불러오는 중...";
            } else if (orderList.get(endIndex).equals(context.getString(R.string.time_kor)) || orderList.get(endIndex).equals(context.getString(R.string.time_kor_space))) {
                return "현재 시간은";
            } else if (order.equals("개발자가 누구야")){
                return "개발자는";
            } else if(orderList.get(endIndex).equals("help") || orderList.get(endIndex).equals("도움말")){
                return "예약어 목록";
            } else if(orderList.get(0).equals("가르치기")){
                TeachChatBotProcess tcb = new TeachChatBotProcess(context);
                tcb.teachingAction(orderList);
                return "학습중...";
            } else if (orderList.get(endIndex).equals(preOrdered.get(0)) || // 마지막 어절이 "전화" 이거나
                    (orderList.get(endIndex).substring(orderList.get(endIndex).length()-2,orderList.get(endIndex).length())) // 마지막 어절의 끝에서 2 글자가 "전화" 일 때
                            .equals(preOrdered.get(0))) {//전화
                return orderList.get(0) + " 전화합니다.";
            } else if (orderList.get(endIndex).equals(preOrdered.get(3)) || orderList.get(endIndex - 1).equals(preOrdered.get(3))
                    || (orderList.get(endIndex).substring(orderList.get(endIndex).length()-2,orderList.get(endIndex).length())).equals(preOrdered.get(3))) {//문자
                return orderList.get(0) + " 문자를 보냅니다.";
            }
        } catch (Exception e){
//            return e.toString();
            // 잡담 명령어를 입력했을 때 "전화"나 "문자"에서 ArrayBound...Exception이 발생한다. 그냥 넘어가도 큰 문제는 없다.
            // 굉장히 무책임한 코딩이 진행되는 것 같다.
        }
        ChatBotProcess chatBotProcess = new ChatBotProcess();
        // 잡담 관련 명령이 아니었을 경우 위의 if ~ else if 문에서의 검문에 걸려 return 되었을 것이기 때문에
        // 여기까지 온 명령어는 잡담과 관련된 명령임이 자명하다.
        String res = chatBotProcess.parseProcess(order);
        if(res!=null){
            return res; // 음 잡담이 맞군
        }
        return "Undefined Order!"; // 음 잡담도 아닌 잘못된 명령이군
    }

    public String processOrder() // 위의 메소드는 반응과 관련된 함수, 이 메소드는 실제 처리를 담당하는 함수
    {
        setPreOrdered(); // 아 이거 또 호출하죠~ 갈수록 코드가 막장이 되어가고 있다
        int endIndex = orderList.size();
        endIndex--; // 위와 마찬가지로 이러한 과정을 거친다. 우리말은 서술어가 문장의 끝에 있는 경우가 많기 때문에 endIndex는 중요하다.
        String ordStr = orderList.get(endIndex); // 하나의 변수에 담아버렸다.
        try {
            if (ordStr.equals(preOrdered.get(1))) {//카메라를 실행하자
                Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA); // 이거 참 간단하구나
                context.startActivity(intent);
                return null;
            } else if(orderList.get(0).equals("가르치기")){
                // xml에 넣는 과정은 위에서 처리했으니까 여기선 아무것도 하지 않아도 된다
                // 이 명령에 대해서는 여기가 반응 메소드네
                return "학습 완료!";
            }
            else if (order.equals("개발자가 누구야")){
                return "한국디지털미디어고등학교 3학년 5반 손명준 입니다.";
                // 크
            } else if(orderList.get(endIndex).equals("help") || orderList.get(endIndex).equals("도움말")){
                String resStr = context.getString(R.string.help_1)+"\n"
                        +context.getString(R.string.help_2)+"\n"
                        +context.getString(R.string.help_3)+"\n"
                        +context.getString(R.string.help_4)+"\n"
                        +context.getString(R.string.help_5)+"\n"
                        +context.getString(R.string.help_6)+"\n"
                        +context.getString(R.string.help_7)+"\n"
                        +context.getString(R.string.help_8)+"\n"
                        +context.getString(R.string.help_9);
                return resStr;
                // 이렇게 많은 내용을 String에 담아 본 적은 처음이다. 더 좋은 방법이 있을텐데
            }
            else if (ordStr.equals(preOrdered.get(2))) {//일정
                Uri calendars = null;
                Intent intent = new Intent();
                if (android.os.Build.VERSION.SDK_INT == 7) {
                    calendars = Uri.parse("content://calendar/time/");
                } else {
                    calendars = Uri.parse("content://com.android.calendar/time/");
                }
                intent.setData(calendars);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                return null;

                // 이거 되는 기기가 있고 그렇지 않은 기기도 있다.
            }
            else if (ordStr.equals(preOrdered.get(0)) || (ordStr.substring(0, ordStr.length() - 1)).equals(preOrdered.get(0))
                    || (ordStr.substring(ordStr.length()-2,ordStr.length())).equals(preOrdered.get(0))) {//전화
                boolean called = callTo(orderList.get(0)); // 전화 가능한가?
                if(called == false){
                    return "Cannot find "+orderList.get(0).substring(0,orderList.get(0).length()-2); // 대상을 찾을 수 없습니다.
                }
                return null;
            }
            else if (ordStr.equals(preOrdered.get(5))) {//음악
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                List songsList = getSongList(); // 음악 리스트를 불러와서
                File file = new File(songsList.get(0).toString());
                Uri uri = FileProvider.getUriForFile(context,"com.yuhyun.testbeta.provider",file);
                intent.setDataAndType(uri, "audio/*");
                activity.startActivity(intent); // 실행
            }
            else if (ordStr.equals(context.getString(R.string.time_kor)) || ordStr.equals(context.getString(R.string.time_kor_space))) {
                Calendar calendar = Calendar.getInstance();
                String str = calendar.getTime().toString();
                return str + " 입니다."; // 가장 간단하고 직관적인 명령이 아닐까 싶다
            }
            else if (ordStr.equals(preOrdered.get(3)) || orderList.get(endIndex - 1).equals(preOrdered.get(3)) ||
                    (ordStr.substring(ordStr.length()-2,ordStr.length())).equals(preOrdered.get(3))) {//문자
                boolean sent = messageTo(orderList.get(0));
                if(sent == false){
                    return "Cannot find "+orderList.get(0).substring(0,orderList.get(0).length()-2);
                } // 이것은 전화와 그 과정이 매우 유사하다
                return null;
            }
            else if (ordStr.equals(preOrdered.get(4))) {//검색
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH); // 무려 intent를 이용해 구글 검색을 할 수 있다. 와우
                String content = "";
                for (int i = 0; i <= orderList.size() - 2; ++i) { // 모든 어절을 다시 하나의 String으로 바꾼다
                    content += orderList.get(i) + " ";
                }
                intent.putExtra(SearchManager.QUERY, content);
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(intent);
                    return null;
                } else {
                    return "There is no proper web browser for task"; // 이런 경우는 없겠지만 혹시 브라우저가 없는 기기가 있을까봐
                }
            }

        }catch (Exception e){
//            return e.toString();
        }
        return null;
    }
    boolean messageTo(String toWhom){ // 문자를 보내는 함수
        String toWhomName = toWhom.substring(0,toWhom.length()-2); //~에게 제거
        String phoneNumber = findWho(toWhomName); // 사람에 대한 전화번호 획득
        if(phoneNumber == null){
            return false; // 전화번호를 못찾았으면 거짓된 반환
        }
        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), 0);
        SmsManager sms = SmsManager.getDefault();
        String content = "";
        String endStr="";
        for(int i=1; i<=orderList.size()-2;++i){
            if(i == orderList.size()-2){
                endStr = orderList.get(i);
                endStr = endStr.substring(0,endStr.length()-2); //'~고' 제거
                content += endStr+" ";
            }
            else{
                content+=orderList.get(i)+" ";
            }
        }
        sms.sendTextMessage(phoneNumber, null, content, sentIntent, deliveredIntent);
        return true;
    }
    boolean callTo(String toWhom) // 전화하는 메소드
    {
        String toWhomName = toWhom.substring(0,toWhom.length()-2); //~에게 제거
        String phoneNumber = findWho(toWhomName);
        if(phoneNumber == null){
            return false;
        }
        String tel = "tel:"+phoneNumber;
        activity.startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
        return true;
    }

    public List getSongList() // 음악 목록 가져오는 메소드
    {
        List myList = new ArrayList();
        String rootSD = Environment.getExternalStorageDirectory().toString();
        File file = new File( rootSD + "/Download" ) ;
        MusicFilter musicFilter = new MusicFilter();
        File list[] = file.listFiles(musicFilter); // 이런 것도 되는구나
        for(int i = 0; i<list.length; i++) {
            myList.add( rootSD+"/Download/"+list[i].getName() );
        }
        return myList;
    }
}
class MusicFilter implements FilenameFilter{ // 보조기억장치에 있는 파일이 음악 파일인지를 검색하기 위해 필요한 class

    @Override
    public boolean accept(File file, String s) {
        if(s.toLowerCase().endsWith(".mp3")||s.toLowerCase().endsWith(".wma")){
            return true;
        }
        return false;
    }
}

// 주석 끝