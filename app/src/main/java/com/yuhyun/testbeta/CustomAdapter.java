package com.yuhyun.testbeta;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    public class ListContents{
        String msg;
        int type;
        ListContents(String _msg,int _type)
        {
            this.msg = _msg; // 출력할 메세지
            this.type = _type; // 0 -> 상대방, 1 -> 사용자
        }
    }

    private ArrayList<ListContents> m_List;
    public CustomAdapter() {
        m_List = new ArrayList<ListContents>();
    }
    public void add(String _msg,int _type) {

        m_List.add(new ListContents(_msg,_type)); // 리스트에 메세지에 대한 정보를 저장
    }
    public void remove(int _position) {
        m_List.remove(_position);
    }
    @Override
    public int getCount() {
        return m_List.size();
    }

    @Override
    public Object getItem(int position) {
        return m_List.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        TextView text = null;
        CustomHolder holder = null;
        LinearLayout layout = null;
        View viewRight = null;
        View viewLeft = null;

        if ( convertView == null ) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chatitem, parent, false);

            layout = (LinearLayout) convertView.findViewById(R.id.layout);
            text = (TextView) convertView.findViewById(R.id.text);
            viewRight = (View) convertView.findViewById(R.id.imageViewright);
            viewLeft = (View) convertView.findViewById(R.id.imageViewleft);

            holder = new CustomHolder();
            holder.m_TextView = text;
            holder.layout = layout;
            holder.viewRight = viewRight;
            holder.viewLeft = viewLeft;
            convertView.setTag(holder);
        }
        else {
            holder = (CustomHolder) convertView.getTag();
            text = holder.m_TextView;
            layout = holder.layout;
            viewRight = holder.viewRight;
            viewLeft = holder.viewLeft;
        }

        text.setText(m_List.get(position).msg);
        if( m_List.get(position).type == 0 ) { // 0번일 경우 왼쪽에서 메세지 버블 생성
            text.setBackgroundResource(R.drawable.inyou);
            layout.setGravity(Gravity.LEFT);
            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);
        }else if(m_List.get(position).type == 1){ // 1번일 경우 오른쪽에서 메세지 버블 생성
            text.setBackgroundResource(R.drawable.whiteme);
            layout.setGravity(Gravity.RIGHT);
            viewRight.setVisibility(View.GONE);
            viewLeft.setVisibility(View.GONE);
        }
        return convertView;
    }

    private class CustomHolder { // 좌, 우측 view 객체에 대한 연산을 처리하기 위해 custom class 선언
        TextView m_TextView;
        LinearLayout layout;
        View viewRight;
        View viewLeft;
    }
}
