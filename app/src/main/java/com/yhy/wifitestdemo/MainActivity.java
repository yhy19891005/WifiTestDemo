package com.yhy.wifitestdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yhy.wifitestdemo.adapter.CAdapter;
import com.yhy.wifitestdemo.adapter.WifiListAdapter;
import com.yhy.wifitestdemo.app.AppContants;
import com.yhy.wifitestdemo.bean.ResultBean;
import com.yhy.wifitestdemo.bean.WifiBean;
import com.yhy.wifitestdemo.dialog.WifiLinkDialog;
import com.yhy.wifitestdemo.utils.CollectionUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";

    private static final int DEFAULT_TIMEOUT = 60;
    private long startTime = 0,endTime = 0;
    //private RecyclerView mRvConnectTime;
    //private ConnectTimeAdapter mAdapter;

    private ListView mLvConnectTime;
    private CAdapter mAdapter;
    private static final int REQUEST_SUC = 100;
    private static final int REQUEST_FAIL = 101;
    private static final int REQUEST_START = 102;
    private static final int CONNECT_WIFI = 103;

    private boolean mHasPermission;
    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;

    private DecimalFormat mFormat = new DecimalFormat("0.0000");
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_SECURE_SETTINGS,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    private TextView mTvStatus;
    private WifiBroadcastReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initReceiver();

        initPermission();
    }

    private void initView() {
        ActionBar bar = getSupportActionBar();
        if(bar != null){
            bar.hide();
        }

        mTvStatus = findViewById(R.id.tv_status);
        mLvConnectTime = findViewById(R.id.lv_connect_time);
        mAdapter = new CAdapter(this);
        mLvConnectTime.setAdapter(mAdapter);

        //mRvConnectTime = findViewById(R.id.rv_connect_time);
        //LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //layoutManager.setStackFromEnd(true);//列表再底部开始展示，反转后由上面开始展示
        //layoutManager.setReverseLayout(true);//列表翻转
        //mRvConnectTime.setLayoutManager(layoutManager);
        //mAdapter = new ConnectTimeAdapter(this);
        //mRvConnectTime.setAdapter(mAdapter);

    }

    private void initReceiver() {
        //注册广播
        wifiReceiver = new WifiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播,是否连接了一个有效路由
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
        registerReceiver(wifiReceiver, filter);

    }

    private void initPermission() {
        mHasPermission = checkPermission();
        if(!mHasPermission){
            requestPermission();
        }else {
            if(WifiSupport.isOpenWifi(MainActivity.this)){
                if(WifiSupport.isNetworkConnected(MainActivity.this)){
                    startTime = System.currentTimeMillis();
                    sendRequest();
                }else {
                    connectWifi();
                }
            }else {
                Toast.makeText(MainActivity.this,"WIFI处于关闭状态", Toast.LENGTH_SHORT).show();
            }
        }
        //if (!mHasPermission && WifiSupport.isOpenWifi(MainActivity.this)) {  //未获取权限，申请权限
        //    requestPermission();
        //}else if(mHasPermission && WifiSupport.isOpenWifi(MainActivity.this)){  //已经获取权限
        //    connectWifi();
        //}else{
        //    Toast.makeText(MainActivity.this,"WIFI处于关闭状态", Toast.LENGTH_SHORT).show();
        //}
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

        mHandler.removeMessages(CONNECT_WIFI);
        mHandler.removeMessages(REQUEST_SUC);
        mHandler.removeMessages(REQUEST_FAIL);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        OkHttpClient okHttpClient = clientBuilder.build();

        String url = "http://wwww.baidu.com";

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(REQUEST_FAIL);
                Log.e(TAG, "onFailure: " + e.toString());
                connectWifi();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.sendEmptyMessage(REQUEST_SUC);
                Log.e(TAG, "onResponse: " + response.body().string());
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            List<ResultBean> dataList = mAdapter.getDataList();

            switch (msg.what){
                case REQUEST_SUC:
                    mTvStatus.setVisibility(View.GONE);
                    endTime = System.currentTimeMillis();
                    long time = endTime - startTime;
                    double timed = time / 1000.0;
                    ResultBean sucBean = new ResultBean();
                    sucBean.setSuc(true);
                    sucBean.setRequestTime(mFormat.format(timed));
                    mAdapter.addData(sucBean);

                    if(dataList.size() > 10){
                        mLvConnectTime.setSelection(dataList.size() - 1);
                    }
                    //mHandler.sendEmptyMessageDelayed(REQUEST_START,10);
                    mTvStatus.setVisibility(View.VISIBLE);
                    mTvStatus.setText("正在断开wifi......");
                    WifiSupport.disconnectWifi(MainActivity.this);
                    break;
                case REQUEST_FAIL:
                    endTime = System.currentTimeMillis();
                    long time1 = endTime - startTime;
                    double timed1 = time1 / 1000.0;
                    ResultBean failBean = new ResultBean();
                    failBean.setSuc(false);
                    failBean.setRequestTime(mFormat.format(timed1));
                    mAdapter.addData(failBean);
                    if(dataList.size() > 10){
                        mLvConnectTime.setSelection(dataList.size() - 1);
                    }

                    mTvStatus.setVisibility(View.GONE);

                    if(WifiSupport.isNetworkConnected(MainActivity.this)){
                        mHandler.sendEmptyMessage(REQUEST_START);
                    }else {
                        mHandler.sendEmptyMessage(CONNECT_WIFI);
                    }

                    Toast.makeText(MainActivity.this,"请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case REQUEST_START:
                    mTvStatus.setVisibility(View.VISIBLE);
                    mTvStatus.setText("正在发送请求......");
                    sendRequest();
                    break;
                case CONNECT_WIFI:
                    connectWifi();
                    break;
            }
        }
    };

    private void connectWifi() {
        startTime = System.currentTimeMillis();
        mHandler.removeMessages(REQUEST_START);
        mHandler.removeMessages(REQUEST_SUC);
        mHandler.removeMessages(REQUEST_FAIL);
        List<ScanResult> wifiScanResult = WifiSupport.getWifiScanResult(MainActivity.this);
        Log.e(TAG,"size = " + wifiScanResult.size());
        if(WifiSupport.containName(wifiScanResult,"TestSu")){
            WifiConfiguration tempConfig  = WifiSupport.isExsits("TestSu",MainActivity.this);
            if(tempConfig == null){
                Log.e(TAG,"bbbbbbbbbbbbbbbbbbbb");
                WifiConfiguration wifiConfiguration =  WifiSupport.createWifiConfig("TestSu","zxcvbnma",WifiSupport.WifiCipherType.WIFICIPHER_WPA);
                WifiSupport.addNetWork(wifiConfiguration,MainActivity.this);
            }else{
                Log.e(TAG,"ccccccccccccccccccccc");
                WifiSupport.addNetWork(tempConfig,MainActivity.this);
            }
        }else {
            Toast.makeText(MainActivity.this,"抱歉，没有扫描到指定热点", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllPermission = true;

        if (requestCode == PERMISSION_REQUEST_CODE) {
            //for (int i : grantResults) {
            //    if (i != PackageManager.PERMISSION_GRANTED) {
            //        hasAllPermission = false;   //判断用户是否同意获取权限
            //        break;
            //    }
            //}
               if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                   hasAllPermission = false;
               }
            Log.e("ffff","hasAllPermission = " + hasAllPermission);

            //如果同意权限
            if (hasAllPermission) {
                mHasPermission = true;
                if(WifiSupport.isOpenWifi(MainActivity.this) && !WifiSupport.isNetworkConnected(MainActivity.this)){  //如果wifi开关是开
                    connectWifi();
                }else{
                    Toast.makeText(MainActivity.this,"WIFI处于关闭状态或权限获取失败", Toast.LENGTH_SHORT).show();
                }

            } else {  //用户不同意权限
                mHasPermission = false;
                Toast.makeText(MainActivity.this,"获取权限失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(wifiReceiver);
    }

    class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (state){
                    /**
                     * WIFI_STATE_DISABLED    WLAN已经关闭
                     * WIFI_STATE_DISABLING   WLAN正在关闭
                     * WIFI_STATE_ENABLED     WLAN已经打开
                     * WIFI_STATE_ENABLING    WLAN正在打开
                     * WIFI_STATE_UNKNOWN     未知
                     */
                    case WifiManager.WIFI_STATE_DISABLED:{
                        Log.e(TAG,"已经关闭");
                        Toast.makeText(context,"WIFI处于关闭状态", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLING:{
                        Log.e(TAG,"正在关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED:{
                        Log.e(TAG,"已经打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING:{
                        Log.e(TAG,"正在打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_UNKNOWN:{
                        Log.e(TAG,"未知状态");
                        break;
                    }
                }
            }else if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())){
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.e(TAG, "--NetworkInfo--" + info.toString());
                if(NetworkInfo.State.DISCONNECTED == info.getState()){//wifi没连接上
                    Log.e(TAG,"wifi没连接上");
                    connectWifi();
                    //if(WifiSupport.isOpenWifi(MainActivity.this) && !WifiSupport.isNetworkConnected(MainActivity.this)){  //如果wifi开关是开
                    //
                    //}

                }else if(NetworkInfo.State.CONNECTED == info.getState()){//wifi连接上了
                    Log.e(TAG,"wifi连接上了");
                    Toast.makeText(context,"wifi连接上了", Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessage(REQUEST_START);
                }else if(NetworkInfo.State.CONNECTING == info.getState()){//正在连接
                    Log.e(TAG,"wifi正在连接");
                }
            }else if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())){
                Log.e(TAG,"网络列表变化了");
            }
        }
    }
}
