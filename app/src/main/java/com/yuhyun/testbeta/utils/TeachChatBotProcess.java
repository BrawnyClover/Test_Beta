package com.yuhyun.testbeta.utils;

import android.content.Context;
import android.os.Environment;

import com.yuhyun.testbeta.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by sonbill on 2017-08-20.
 */

public class TeachChatBotProcess { // 빅스비의 가르치기 비슷한 기능을 구현하고자 했습니다.
    File chatbotxmlFolder, chatbotxml; // xml 파일이 위치할 폴더와 xml 파일에 대한 정보를 담을 File 객체
    Context context; // 현재 에플리케이션에 대한 context
    String rootSD = Environment.getExternalStorageDirectory().toString(); // 저장소 root 디렉토리
    Document xml = null; // xml 문서에 대한 내용
    Document document = null; // 이건 필요가 없네...

    public TeachChatBotProcess(Context context){
        this.context = context;
    } // 생성자

    public void initXml() { // xml 파일을 생성
        String fileDir = rootSD+"/TestBeta";
        chatbotxmlFolder = new File(fileDir);
        if(chatbotxmlFolder.isDirectory() == false){ // /root/TestBeta 디렉토리가 없을 경우 디렉토리 생성
            chatbotxmlFolder.mkdir();
        }
        fileDir = fileDir + "/chatbotxml.xml";
        chatbotxml = new File(fileDir);
        if(!chatbotxml.isFile()){ // chatbotxml 파일이 없을 경우 파일 생성
            writeDefaultXml();
        }
    }
    public void writeDefaultXml() {
        InputStream inputStream = context.getResources().openRawResource(R.raw.chatbotxml); // raw 폴더에 있는 기본 xml 파일에 대한 스트림 설정
        try {
            FileOutputStream fos = new FileOutputStream(chatbotxml); // 내보낼 파일에 대한 스트림 설정
            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream,"UTF-8")); // 기본 xml 파일을 읽어옵시다
            Writer out = new OutputStreamWriter(fos, "UTF-8");
            while(true){
                String string= bufferedReader.readLine(); // 한 줄씩 읽어오기
                if(string != null){
                    out.write(string+"\n");
                }else{
                    break;
                }
            }
            out.close(); // 닫아주기
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void teachingAction(List<String> order){ // 가르치기 명령에 대한 처리
        chatbotxml = new File(rootSD+"/TestBeta/chatbotxml.xml");
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(chatbotxml); // xml 파일을 불러오자
        } catch (Exception e) {
            e.printStackTrace();
        }
//        switch(order.get(1)){
//            case "명령":
                addXmlChild("intent",order.get(1),order.get(2),"orderStr"); // xml 파일의 intent의 자식 노드에 추가
//                break;
//            case "응답":
//                addXmlChild("response",order.get(2),order.get(3),"resStr");
//                break;
//            case "명령모음":
//                addXmlChild("intent",order.get(2),"order");
//                break;
//            case "응답모음":
//                addXmlChild("response",order.get(2),"res");
//                break;
//        }
        DOMSource xmlDOM = new DOMSource(xml);
        StreamResult xmlFile = new StreamResult(chatbotxml);
        try {
            TransformerFactory.newInstance().newTransformer().transform(xmlDOM, xmlFile); // xml DOM을 문서로 변환
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
    public void addXmlChild(String tagName, String intentName, String content, String classAttr)
    {
        XPath xpath = XPathFactory.newInstance().newXPath(); // 직관적이고 편한 xPath를 씁시다
        try {
            String evaluate = "chatbot/"+tagName+"/"+intentName; // 자식 노드를 추가할 부모 노드에 대한 xPath 설정
            Node parentNode = (Node) xpath.evaluate(evaluate, xml, XPathConstants.NODE); // 부모 노드 불러오기
            Document doc = parentNode.getOwnerDocument(); // Document 객체 생성
            Element child = doc.createElement("string"); // 자식 노드 선언
            child.setAttribute("class",classAttr); // 자식 노드에 속성 부여
            child.appendChild(doc.createTextNode(content)); // 자식 노드에 텍스트 자식 노드 부여

//            if(classAttr.equals("resStr")) {
//                Node nullChild = parentNode.getLastChild();
//                parentNode.removeChild(parentNode.getLastChild());
//                parentNode.appendChild(child);
//                parentNode.appendChild(nullChild);
//            }
            parentNode.appendChild(child); // 부모 노드에 자식 노드 부여
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }
//    public void addXmlChild(String tagName, String intentName, String classAttr)
//    {
//        XPath xpath = XPathFactory.newInstance().newXPath();
//        try {
//            String evaluate = "chatbot/"+tagName;
//            Node parentNode = (Node) xpath.evaluate(evaluate, xml, XPathConstants.NODE);
//            Element child = document.createElement(intentName);
//            child.setAttribute("class",classAttr);
//            parentNode.appendChild(child);
//        } catch (XPathExpressionException e) {
//            e.printStackTrace();
//        }
//    }
}
