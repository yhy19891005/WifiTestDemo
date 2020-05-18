package com.yhy.wifitestdemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yhy.wifitestdemo.bean.ResultBean;

import java.util.ArrayList;
import java.util.List;

public class ConnectTimeAdapter extends RecyclerView.Adapter<ConnectTimeAdapter.ConnectTimeViewHolder> {

    private List<ResultBean> mList = new ArrayList<>();
    private Context mContext;

    public ConnectTimeAdapter(Context context) {
        mContext = context;
    }

    public void addData(ResultBean bean){
        mList.add(bean);
        //Collections.reverse(mList);
        notifyDataSetChanged();
    }

    public List<ResultBean> getDataList(){
        return mList;
    }

    @NonNull
    @Override
    public ConnectTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(mContext,android.R.layout.simple_list_item_1,null);
        return new ConnectTimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectTimeViewHolder holder, int position) {
        ResultBean bean = mList.get(position);
        holder.setTimeStr(bean,position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ConnectTimeViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ConnectTimeViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTimeStr(ResultBean bean,int position){
            String str = bean.isSuc() ? "请求成功，" : "请求失败，";
            ((TextView)mView).setText("第" + (position + 1) + "次，"+ str +"共耗时" + bean.getRequestTime() + "秒");
        }
    }
}
