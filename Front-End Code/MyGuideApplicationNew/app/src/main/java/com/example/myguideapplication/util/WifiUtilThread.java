package com.example.myguideapplication.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.myguideapplication.argument.NetworkSettings;
import com.example.myguideapplication.argument.ResponseCode;
import com.example.myguideapplication.construction.Coordinate;
import com.example.myguideapplication.construction.FingerData;
import com.example.myguideapplication.construction.RestResponse;
import com.example.myguideapplication.ui.home.HomeFragment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WifiUtilThread extends Thread
{
    private WifiManager wifiManager;
    private List<ScanResult> scanResults;
    private StringBuilder scanBuilder;
    private List<FingerData> fingerDataList;
    private static Coordinate coordinate;

    private Context context;
    private OkHttpClient client;
    private ObjectMapper mapper;
    private Handler handler;
    private Message message;

    private boolean pause = false;

    public WifiUtilThread(Context context)
    {
        this.context = context;
        client = new OkHttpClient();
        mapper = new ObjectMapper();
        handler = new Handler(Looper.getMainLooper());
        message = new Message();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan(); //启动扫描
        scanResults = wifiManager.getScanResults();
        fingerDataList = new ArrayList();
        coordinate = new Coordinate();
    }

    public WifiUtilThread(Context context,Handler handler, Message message, OkHttpClient okHttpClient, ObjectMapper objectMapper)
    {
        this.context = context;
        this.client = okHttpClient;
        this.mapper = objectMapper;
        this.handler = handler;
        this.message = message;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan(); //启动扫描
        scanResults = wifiManager.getScanResults();
        fingerDataList = new ArrayList();
        coordinate = new Coordinate();
    }

    @Override
    public synchronized void run() {
        while(true)
        {
            if(!pause)
            {
                scanWifiInfo();
                sendWIFIMessage();
            }
            try{
                Thread.sleep(2000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void scanWifiInfo(){
        wifiManager.startScan(); //启动扫描
        scanBuilder = new StringBuilder();
        scanResults = wifiManager.getScanResults();

        if(!fingerDataList.isEmpty()) {
            fingerDataList.clear();
        }

        for (ScanResult scanResult : scanResults) {
            FingerData fingerData = new FingerData(scanResult.BSSID,scanResult.level * (-1));
            fingerDataList.add(fingerData);
//            System.out.println(fingerData.getMAC() + fingerData.getRSSI());
        }
    }

    private void sendWIFIMessage() {
        try {
            Request request = new Request.Builder().url(NetworkSettings.WIFI_MES).post(
                    RequestBody.create(mapper.writeValueAsString(fingerDataList), NetworkSettings.mediaType)
            ).build();
            String values = mapper.writeValueAsString(fingerDataList);
            System.out.println(values);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    message.what = ResponseCode.REQUEST_FAILED;
                    handler.post(() -> Utils.showMessage(context, message));
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            RestResponse restResponse = mapper.readValue(body.string(), RestResponse.class);
                            int code = restResponse.getCode();
                            if (code == ResponseCode.LOCATE_SUCCESS) {
                                handler.post(() -> {
                                    try {
                                        coordinate = mapper.convertValue(restResponse.getData(), Coordinate.class);
                                    } catch (Exception e)
                                    {
                                        return;
                                    }
                                });
                            }else {
                                message.what = ResponseCode.LOCATE_FAILED;
                                Log.e("RESPONSE_BODY_EMPTY", response.message());
                                handler.post(() -> Utils.showMessage(context, message));
                            }
                        } else {
                            message.what = ResponseCode.LOCATE_FAILED;
                            Log.e("RESPONSE_BODY_EMPTY", response.message());
                            handler.post(() -> Utils.showMessage(context, message));
                        }
                    } else {
                        message.what = ResponseCode.SERVER_ERROR;
                        Log.e("SERVER_ERROR", response.message());
                        handler.post(() -> Utils.showMessage(context, message));
                    }
                }
            });
        } catch (JsonProcessingException e) {
            message.what = ResponseCode.JSON_SERIALIZATION;
//            Utils.showMessage(context, message);
            e.printStackTrace();
        }
    }

    public void setPause(boolean value)
    {
        pause = value;
    }

    public Coordinate getCoordinate()
    {
        return coordinate;
    }

    public List<FingerData> getFingerDataList(){
        return fingerDataList;
    }
}
