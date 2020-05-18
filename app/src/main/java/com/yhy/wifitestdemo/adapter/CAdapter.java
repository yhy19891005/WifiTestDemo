package com.yhy.wifitestdemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yhy.wifitestdemo.bean.ResultBean;

import java.util.ArrayList;
import java.util.List;

public class CAdapter extends BaseAdapter {

    private List<ResultBean> mList = new ArrayList<>();
    private Context mContext;

    public CAdapter(Context context) {
        mContext = context;
    }

    public List<ResultBean> getDataList(){
        return mList;
    }

    public void addData(ResultBean bean){
        mList.add(bean);
        //Collections.reverse(mList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public ResultBean getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(mContext,android.R.layout.simple_list_item_1,null);
        }
        ResultBean bean = mList.get(position);
        String str = bean.isSuc() ? "请求成功，" : "请求失败，";
        ((TextView)convertView).setText("第" + (position + 1) + "次，"+ str +"共耗时" + bean.getRequestTime() + "秒");
        return convertView;
    }
}
