package com.example.myguideapplication.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myguideapplication.construction.FingerData;
import com.example.myguideapplication.argument.NetworkSettings;
import com.example.myguideapplication.R;
import com.example.myguideapplication.argument.ResponseCode;
import com.example.myguideapplication.construction.Point;
import com.example.myguideapplication.construction.RestResponse;
import com.example.myguideapplication.construction.User;
import com.example.myguideapplication.util.Utils;
import com.example.myguideapplication.util.WifiUtilThread;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myguideapplication.ui.home.HomeFragment;
import com.example.myguideapplication.databinding.ActivityGuideBinding;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GuideActivity extends AppCompatActivity {

    private ActivityGuideBinding binding;
    private NavController navController;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Message message = new Message();
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_guide);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    public void onStartGuideClick()
    {
        navController.navigate(R.id.navigation_home);
    }

    public void OnSearch(View view){
        String name = HomeFragment.binding.editTextSearch.getText().toString();
        try {
            Request request = new Request.Builder().url(NetworkSettings.POINT_QUERY + "?name=" + name).get().build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    message.what = ResponseCode.REQUEST_FAILED;
//                    handler.post(() -> Utils.showMessage(getApplicationContext(), message));
                    e.printStackTrace();
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            RestResponse restResponse = mapper.readValue(body.string(), RestResponse.class);
                            message.what = restResponse.getCode();
                            if (message.what == ResponseCode.QUERY_SUCCESS) {
                                handler.post(() -> { Point.setPoint(mapper.convertValue(restResponse.getData(),Point.class));
                                });
                            }else {
                                handler.post(() -> {
                                });
                            }
                        } else {
                            message.what = ResponseCode.EMPTY_RESPONSE;
                            Log.e("RESPONSE_BODY_EMPTY", response.message());
                        }
                    } else {
                        message.what = ResponseCode.SERVER_ERROR;
                        Log.e("SERVER_ERROR", response.message());

                    }
                    handler.post(() -> Utils.showMessage(getApplicationContext(), message));
                }
            });
        } catch (Exception e) {
            message.what = ResponseCode.JSON_SERIALIZATION;
            Utils.showMessage(getApplicationContext(), message);
            e.printStackTrace();
        }
    }

//    public void onSentToServer(View view)
//    {
//        HomeFragment.binding.textView.setText("");
//        System.out.println(wifiUtilThread.getFingerDataList().size());
//        if(!wifiUtilThread.getFingerDataList().isEmpty()) {
//            for(FingerData fingerData : wifiUtilThread.getFingerDataList()) {
//                HomeFragment.binding.textView.append("id:" + fingerData.getMAC() + " RSSI:" + fingerData.getRSSI() + "\n");
//                System.out.println(fingerData.getMAC() + " " + fingerData.getMAC() + "\n");
//            }
//            sentToServer();
//        }
//    }
//
//    private void sentToServer()
//    {
//        try {
//            Request request = new Request.Builder().url(NetworkSettings.WIFI_MES).post(
//                    RequestBody.create(mapper.writeValueAsString(wifiUtilThread.getFingerDataList()), NetworkSettings.mediaType)
//            ).build();
//            String values = mapper.writeValueAsString(wifiUtilThread.getFingerDataList());
//            System.out.println(values);
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                    message.what = ResponseCode.REQUEST_FAILED;
//                    handler.post(() -> Utils.showMessage(getApplicationContext(), message));
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                    //Log.w("Ho", Objects.requireNonNull(response.body()).string());
//                    if (response.isSuccessful()) {
//                        ResponseBody body = response.body();
//                        if (body != null) {
//                            RestResponse restResponse = mapper.readValue(body.string(), RestResponse.class);
//                            message.what = restResponse.getCode();
//                            if (message.what == ResponseCode.LOCATE_SUCCESS) {
//                                handler.post(() -> {
//                                    HomeFragment.binding.textView.append("信息发送");
//                                });
//                            }else {
//                                message.what = ResponseCode.LOCATE_FAILED;
//                                Log.e("RESPONSE_BODY_EMPTY", response.message());
//                            }
//                        } else {
//                            message.what = ResponseCode.LOCATE_FAILED;
//                            Log.e("RESPONSE_BODY_EMPTY", response.message());
//                        }
//                    } else {
//                        message.what = ResponseCode.SERVER_ERROR;
//                        Log.e("SERVER_ERROR", response.message());
//
//                    }
//                    handler.post(() -> Utils.showMessage(getApplicationContext(), message));
//                }
//            });
//        } catch (JsonProcessingException e) {
//            message.what = ResponseCode.JSON_SERIALIZATION;
//            Utils.showMessage(getApplicationContext(), message);
//            e.printStackTrace();
//        }
//    }
}