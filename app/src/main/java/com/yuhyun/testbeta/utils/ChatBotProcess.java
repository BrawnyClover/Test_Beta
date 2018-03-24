package com.yuhyun.testbeta.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.view.View;

import com.yuhyun.testbeta.MainActivity;
import com.yuhyun.testbeta.R;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * Created by sonbill on 2017-08-19.
 */

public class ChatBotProcess{ // 잡담과 관련된 process를 관장하는 class
    Context context = MainActivity.mContext;
    XPath xpath;
    Document xml = null;
    String rootSD = Environment.getExternalStorageDirectory().toString();

    public ChatBotProcess(){
        setInstance();
    }
    public void setInstance(){
        String xmlFilePath = rootSD+"/TestBeta/chatbotxml.xml"; // 파싱할 chatbotxml 문서 불러오기
        File xmlFile = new File(xmlFilePath);
        try{
            InputStream inputStream = new FileInputStream(xmlFile); // 입력 스트림 설정
            xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        }catch (Exception e){

        }
        xpath = XPathFactory.newInstance().newXPath();
    }
    public String parseProcess(String order)
    {
        String intentName = "";
        List<String> reActions = new ArrayList<>();
        ArrayList<String> temp = new ArrayList<>();
        try {
            NodeList node = (NodeList) xpath.evaluate("chatbot/intent/*[@class='order']", xml, XPathConstants.NODESET); // intent 노드의 자식 노드들을 모두 불러오자
            for(int i=0; i<node.getLength(); i++){
                temp.add(node.item(i).getTextContent()); // 이건 디버그용 코드인데 그낭 놔둡시다.
            }
            boolean flag = false; // 명령과 일치한 노드를 찾으면 탐색을 중지하기 위해 필요한 flag
            for(int i=0; i<node.getLength();i++){
                NodeList intent = node.item(i).getChildNodes(); // intent의 각 자식 노드에 대한 탐색을 진행
                for(int j=0; j<intent.getLength();j++){
                    if((intent.item(j).getTextContent()).equals(order)){ // 명령과 일치한 노드가 있다?
                        intentName = node.item(i).getNodeName(); // 노드의 이름 반환
                        flag = true;
                        break;
                    }
                }
                if(flag){break;}
            }
            if(intentName == ""){ // 명령과 같은 내용을 담은 intent 노드를 찾지 못했을 경우
                intentName = "exception"; // exception 이름을 부여
            }
            String eval = "chatbot/response/"+intentName;
            Node reactionNode = (Node) xpath.evaluate(eval,xml,XPathConstants.NODE);

//            reActions =  Arrays.asList((reactionNodes.item(1).getTextContent()).toString()).split("\n"));
//            Arrays.asList(order.split(" "));
            try {
                reActions = Arrays.asList(((reactionNode.getTextContent().toString())).split("\n            "));
                // 이거 어째서인지는 모르겠는데, intentName과 같은 이름을 가진 답변노드의, 자식 노드를 List에 집어넣기 위해선
                // 개행과 탭이 적절하게 섞인 문자로 슬라이싱할 필요가 있다.
            }catch(Exception e){
                return e.toString();
            }
            Random random = new Random(); // 해당 명령에 대한 여러가지 답변 중 하나를 무작위로 골라 반환한다.
            int index = random.nextInt(reActions.size()-2);
            String reStr = reActions.get(index+1);
//            reStr = reStr.replaceAll(" ", "");
            if(reStr.equals("greatTest")){ // 특히 선정된 답변이 greatTest일 경우
                reStr = "";
                reStr += "네? 수능이 "+ String.valueOf(countdday())+"일 남았다구요?"; // 수능까지의 d-day를 보내주자. 공부하라는 마음을 담아서
            }
            return reStr;
        }catch (XPathExpressionException e){
            return e.toString(); // 그럴 일은 없겠지만 혹시 몰라, 사용자가 임의로 xml 파일을 조작했다가 xpath 구문에 오류가 날 수 있다.
        }
    }

    public int countdday() { // d-day 반환 함수
        try {
            Calendar todayCal = Calendar.getInstance();
            Calendar ddayCal = Calendar.getInstance();

            int myear, mmonth, mday;
            myear = todayCal.get(Calendar.YEAR);
            mmonth = 10;
            mday = 16; // 맙소사
            ddayCal.set(myear,mmonth,mday);
            final int mili = 24 * 60 * 60 * 1000; // 있어보이게 final 속성을 부여해보았다.
            long today = todayCal.getTimeInMillis()/mili;
            long dday = ddayCal.getTimeInMillis()/mili;
            long count = dday - today;
            return (int) count;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }
}
