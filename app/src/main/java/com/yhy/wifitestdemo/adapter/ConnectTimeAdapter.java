package com.yhy.wifitestdemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ConnectTimeAdapter extends RecyclerView.Adapter<ConnectTimeAdapter.ConnectTimeViewHolder> {

    private List<String> mList = new ArrayList<>();
    private Context mContext;

    public ConnectTimeAdapter(Context context) {
        mContext = context;
    }

    public void addData(String time){
        mList.add(time);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConnectTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(mContext,android.R.layout.simple_list_item_1,null);
        return new ConnectTimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectTimeViewHolder holder, int position) {
        holder.setTimeStr(mList.get(position),position);
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

        public void setTimeStr(String time,int position){
            ((TextView)mView).setText("第" + (position + 1) + "次，共耗时" + time + "秒");
        }
    }
}
