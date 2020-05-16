package com.yhy.wifitestdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yhy.wifitestdemo.adapter.ConnectTimeAdapter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TestActivity extends AppCompatActivity {

    private static final int DEFAULT_TIMEOUT = 60;
    private long startTime = 0,endTime = 0;
    private RecyclerView mRvConnectTime;
    private ConnectTimeAdapter mAdapter;

    private static final int REQUEST_SUC = 100;
    private static final int REQUEST_FAIL = 101;
    private static final int REQUEST_START = 102;
    private static final int CONNECT_WIFI = 103;

    private boolean mHasPermission;
    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;
    //两个危险权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private TextView mTvStatus;
    private OkHttpClient mOkHttpClient;

    private String url = "http://wwww.baidu.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ActionBar bar = getSupportActionBar();
        if(bar != null){
            bar.hide();
        }

        initView();

        mHasPermission = checkPermission();
        if (!mHasPermission && WifiSupport.isOpenWifi(TestActivity.this)) {  //未获取权限，申请权限
            requestPermission();
        }else if(mHasPermission && WifiSupport.isOpenWifi(TestActivity.this)){  //已经获取权限
            mHandler.sendEmptyMessage(CONNECT_WIFI);
        }else{
            Toast.makeText(TestActivity.this,"WIFI处于关闭状态", Toast.LENGTH_SHORT).show();
        }
        //sendRequest();

    }

    private void initView() {
        mTvStatus = findViewById(R.id.tv_status);
        mRvConnectTime = findViewById(R.id.rv_connect_time);
        mRvConnectTime.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ConnectTimeAdapter(this);
        mRvConnectTime.setAdapter(mAdapter);
    }

    /**
     * 检查是否已经授予权限
     * @return
     */
    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    private void sendRequest() {
        //mHandler.sendEmptyMessage(REQUEST_START);

        if(mOkHttpClient == null){
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            clientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                         .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                         .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                         .retryOnConnectionFailure(true);
            mOkHttpClient = clientBuilder.build();
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(REQUEST_FAIL);
                Log.e("ddddddddddddd", "onFailure: " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.sendEmptyMessage(REQUEST_SUC);
                Log.e("ddddddddddddd", "onResponse: " + response.body().string());
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REQUEST_SUC:
                    mTvStatus.setVisibility(View.GONE);
                    endTime = System.currentTimeMillis();
                    long time = endTime - startTime;
                    double timed = time / 1000.0;
                    mAdapter.addData(timed + "");

                    mTvStatus.setVisibility(View.VISIBLE);
                    mTvStatus.setText("正在断开wifi......");
                    boolean disconnect = WifiSupport.disconnectWifi(TestActivity.this);
                    if(disconnect){
                        Toast.makeText(TestActivity.this,"wifi已断开", Toast.LENGTH_SHORT).show();
                        mTvStatus.setVisibility(View.GONE);
                        mHandler.sendEmptyMessage(CONNECT_WIFI);
                    }else {
                        Toast.makeText(TestActivity.this,"wifi断开失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_FAIL:
                    mTvStatus.setVisibility(View.GONE);
                    Toast.makeText(TestActivity.this,"请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_START:
                    mTvStatus.setVisibility(View.VISIBLE);
                    mTvStatus.setText("正在发送请求......");
                    sendRequest();
                    break;
                case CONNECT_WIFI:
                    startTime = System.currentTimeMillis();
                    List<ScanResult> wifiScanResult = WifiSupport.getWifiScanResult(TestActivity.this);
                    Log.e("ddddddddddd","size = " + wifiScanResult.size());
                    if(WifiSupport.containName(wifiScanResult,"TestSu")){
                        WifiConfiguration tempConfig  = WifiSupport.isExsits("TestSu",TestActivity.this);
                        if(tempConfig == null){
                            WifiConfiguration wifiConfiguration =  WifiSupport.createWifiConfig("TestSu","zxcvbnma",WifiSupport.WifiCipherType.WIFICIPHER_WPA);
                            boolean b = WifiSupport.addNetWork(wifiConfiguration,TestActivity.this);
                            if (b){
                                Toast.makeText(TestActivity.this,"连接热点成功", Toast.LENGTH_SHORT).show();
                                mHandler.sendEmptyMessage(REQUEST_START);
                            }else {
                                Toast.makeText(TestActivity.this,"抱歉，连接热点失败", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            boolean b = WifiSupport.addNetWork(tempConfig,TestActivity.this);
                            if (b){
                                Toast.makeText(TestActivity.this,"连接热点成功", Toast.LENGTH_SHORT).show();
                                mHandler.sendEmptyMessage(REQUEST_START);
                            }else {
                                Toast.makeText(TestActivity.this,"抱歉，连接热点失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else {
                        Toast.makeText(TestActivity.this,"抱歉，没有扫描到指定热点", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllPermission = true;
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermission = false;   //判断用户是否同意获取权限
                    break;
                }
            }

            //如果同意权限
            if (hasAllPermission) {
                mHasPermission = true;
                if(WifiSupport.isOpenWifi(TestActivity.this) && mHasPermission){  //如果wifi开关是开 并且 已经获取权限
                    mHandler.sendEmptyMessage(CONNECT_WIFI);
                }else{
                    Toast.makeText(TestActivity.this,"WIFI处于关闭状态或权限获取失败1111", Toast.LENGTH_SHORT).show();
                }

            } else {  //用户不同意权限
                mHasPermission = false;
                Toast.makeText(TestActivity.this,"获取权限失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
    }
}
