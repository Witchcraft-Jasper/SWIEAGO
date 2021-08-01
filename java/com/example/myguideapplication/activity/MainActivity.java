package com.example.myguideapplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import com.example.myguideapplication.R;
import com.example.myguideapplication.argument.NetworkSettings;
import com.example.myguideapplication.argument.ResponseCode;
import com.example.myguideapplication.construction.RestResponse;
import com.example.myguideapplication.construction.User;
import com.example.myguideapplication.sparkbutton.SparkButton;
import com.example.myguideapplication.util.Utils;
import com.example.myguideapplication.databinding.ActivityMainBinding;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ActivityMainBinding binding;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Message message = new Message();

    private String oldName;
    private String oldPassword;

    private AlphaAnimation alphaAniShow;
    private TranslateAnimation translateAniShow;
    private Animation bigAnimation;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        binding.buttonLogin.setOnClickListener(this);
        binding.buttonRegister.setOnClickListener(this);

        CheckAndroidPermission();
        getPrivacy();

        initAnimation();
        startAnimation();
    }
    private void CheckAndroidPermission() {
        List<String> permissionLists = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionLists.add(Manifest.permission.CAMERA);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionLists.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionLists.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionLists.add(Manifest.permission.READ_PHONE_STATE);
        }if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            permissionLists.add(Manifest.permission.RECEIVE_SMS);
        }if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            permissionLists.add(Manifest.permission.READ_CONTACTS);
        }if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionLists.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
    }

    private void getPrivacy() {
        if (Build.VERSION.SDK_INT >= 23) {
            int readPhone = checkSelfPermission("android.permission.READ_PHONE_STATE");
            int receiveSms = checkSelfPermission("android.permission.RECEIVE_SMS");
            int readContacts = checkSelfPermission("android.permission.READ_CONTACTS");
            int readSdcard = checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE");
            int readWIFI = checkSelfPermission("android.permission.ACCESS_WIFI_STATE");
            int rearCamera = checkSelfPermission("android.permission.CAMERA");

            int requestCode = 0;
            final ArrayList<String> permissions = new ArrayList<String>();
            if (readPhone != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 0;
                permissions.add("android.permission.READ_PHONE_STATE");
            }
            if (receiveSms != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 1;
                permissions.add("android.permission.RECEIVE_SMS");
            }
            if (readContacts != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 2;
                permissions.add("android.permission.READ_CONTACTS");
            }
            if (readSdcard != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 3;
                permissions.add("android.permission.READ_EXTERNAL_STORAGE");
            }
            if(readWIFI != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 4;
                permissions.add("android.permission.ACCESS_WIFI_STATE");
            }
            if(rearCamera != PackageManager.PERMISSION_GRANTED){
                requestCode |= 1 << 5;
                permissions.add("android.permission.CAMERA");
            }
            if (requestCode > 0) {
                String[] permission = new String[permissions.size()];
                this.requestPermissions(permissions.toArray(permission), requestCode);
                return;
            }
        }
    }

    private void initAnimation()
    {
        bigAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_big);

        alphaAniShow = new AlphaAnimation(0, 1);//百分比透明度，从0%到100%显示
        alphaAniShow.setDuration(500);//

        translateAniShow = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,//RELATIVE_TO_SELF表示操作自身
                0,//fromXValue表示开始的X轴位置
                Animation.RELATIVE_TO_SELF,
                0,//fromXValue表示结束的X轴位置
                Animation.RELATIVE_TO_SELF,
                1,//fromXValue表示开始的Y轴位置
                Animation.RELATIVE_TO_SELF,
                0);//fromXValue表示结束的Y轴位置
        translateAniShow.setRepeatMode(Animation.REVERSE);
        translateAniShow.setDuration(500);
    }

    private void startAnimation()
    {
        binding.editTextName.startAnimation(alphaAniShow);
        binding.editTextPassword.startAnimation(alphaAniShow);

        binding.buttonLogin.startAnimation(bigAnimation);
        binding.buttonRegister.startAnimation(bigAnimation);
        binding.imageViewAccountIcon.startAnimation(bigAnimation);
        binding.imageViewPasswordIcon.startAnimation(bigAnimation);

        binding.imageViewTitle.startAnimation(translateAniShow);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        startAnimation();
    }

    public void buttonLogin() {
//        Intent intent = new Intent();
//        intent.setClass(MainActivity.this, GuideActivity.class);
//        startActivity(intent);
//        finish();
        String name = binding.editTextName.getText().toString();
        String password = binding.editTextPassword.getText().toString();
        if (!"".equals(name) || !"".equals(password)) {
            try {
                Request request = new Request.Builder().url(NetworkSettings.SIGN_IN).post(
                        RequestBody.create(mapper.writeValueAsString(new User(name, password)), NetworkSettings.mediaType)
                ).build();
                String values = mapper.writeValueAsString(new User(name, password));
                System.out.println(values);
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        message.what = ResponseCode.REQUEST_FAILED;
                        handler.post(() -> Utils.showMessage(getApplicationContext(), message));
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        //Log.w("Ho", Objects.requireNonNull(response.body()).string());
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            if (body != null) {
                                RestResponse restResponse = mapper.readValue(body.string(), RestResponse.class);
                                message.what = restResponse.getCode();
                                if (message.what == ResponseCode.SIGN_IN_SUCCESS) {
                                    handler.post(() -> {
                                        User.setUser(mapper.convertValue(restResponse.getData(),User.class));
                                        Intent intent = new Intent();
                                        intent.setClass(MainActivity.this, GuideActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                } else {
                                    handler.post(() -> {
                                        binding.editTextName.setText("");
                                        binding.editTextPassword.setText("");
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
            } catch (JsonProcessingException e) {
                message.what = ResponseCode.JSON_SERIALIZATION;
                Utils.showMessage(getApplicationContext(), message);
                e.printStackTrace();
            }
        } else {
            message.what = ResponseCode.EMPTY_INFO;
            Utils.showMessage(getApplicationContext(), message);
        }
    }

    public void buttonSignUp() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, RegisterActivity.class);
        startActivity(intent);

//        String name = binding.editTextName.getText().toString();
//        String password = binding.editTextPassword.getText().toString();
//        if (!"".equals(name) || !"".equals(password)) {
//            try {
//                Request request = new Request.Builder().url(NetworkSettings.SIGN_UP).post(
//                        RequestBody.create(mapper.writeValueAsString(new User(name, password)), NetworkSettings.mediaType)
//                ).build();
//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        message.what = ResponseCode.REQUEST_FAILED;
//                        handler.post(() -> Utils.showMessage(getApplicationContext(), message));
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                        if (response.isSuccessful()) {
//                            ResponseBody body = response.body();
//                            if (body != null) {
//                                RestResponse restResponse = mapper.readValue(body.string(), RestResponse.class);
//                                message.what = restResponse.getCode();
//                                if (message.what == ResponseCode.SIGN_UP_SUCCESS) {
//                                    handler.post(() -> {
//                                        Intent a = new Intent();
//                                        a.setClass(MainActivity.this, GuideActivity.class);
//                                        startActivity(a);
//                                    });
//                                } else {
//                                    handler.post(() -> {
//                                        binding.editTextName.setText("");
//                                        binding.editTextPassword.setText("");
//                                    });
//                                }
//                            } else {
//                                message.what = ResponseCode.EMPTY_RESPONSE;
//                                Log.e("RESPONSE_BODY_EMPTY", response.message());
//                            }
//                        } else {
//                            message.what = ResponseCode.SERVER_ERROR;
//                            Log.e("SERVER_ERROR", response.message());
//
//                        }
//                        handler.post(() -> Utils.showMessage(getApplicationContext(), message));
//                    }
//                });
//            } catch (JsonProcessingException e) {
//                message.what = ResponseCode.JSON_SERIALIZATION;
//                Utils.showMessage(getApplicationContext(), message);
//                e.printStackTrace();
//            }
//        } else {
//            message.what = ResponseCode.EMPTY_INFO;
//            Utils.showMessage(getApplicationContext(), message);
//        }
    }

//    public void update(View view) {
//        try {
//            String name = binding.editTextName.getText().toString();
//            String password = binding.editTextPassword.getText().toString();
//            if (name.equals(oldName) && password.equals(oldPassword)) {
//                message.what = ResponseCode.UNCHANGED_INFORMATION;
//                handler.post(() -> Utils.showMessage(getApplicationContext(), message));
//                return;
//            }
//            password = Utils.encrypt(password);
//            Request request = new Request.Builder().url(NetworkSettings.UPDATE).put(
//                    RequestBody.create(
//                            mapper.writeValueAsString(new User(signInId, name, password)),
//                            NetworkSettings.mediaType
//                    )
//            ).build();
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
//                    if (response.isSuccessful()) {
//                        ResponseBody body = response.body();
//                        if (body != null) {
//                            RestResponse restResponse = mapper.readValue(body.string(), RestResponse.class);
//                            message.what = restResponse.getCode();
//                            if (message.what == ResponseCode.UPDATE_SUCCESS) {
//                                oldName = binding.editTextName.getText().toString();
//                                oldPassword = binding.editTextPassword.getText().toString();
//                            }
//                        } else {
//                            message.what = ResponseCode.EMPTY_RESPONSE;
//                            Log.e("RESPONSE_BODY_EMPTY", response.message());
//                        }
//                    } else {
//                        message.what = ResponseCode.SERVER_ERROR;
//                        Log.e("SERVER_ERROR", response.message());
//                    }
//                    handler.post(() -> Utils.showMessage(getApplicationContext(), message));
//                }
//            });
//        } catch (Exception e) {
//            message.what = ResponseCode.JSON_SERIALIZATION;
//            Utils.showMessage(getApplicationContext(), message);
//            e.printStackTrace();
//        }
//    }
//
//    public void delete(View view) {
//        try {
//            Request request = new Request.Builder().url(NetworkSettings.DELETE + "?id=" + signInId).delete().build();
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
//                    if (response.isSuccessful()) {
//                        ResponseBody body = response.body();
//                        if (body != null) {
//                            RestResponse restResponse = mapper.readValue(body.string(), RestResponse.class);
//                            message.what = restResponse.getCode();
//                            if (message.what == ResponseCode.DELETE_SUCCESS) {
//                                handler.post(() -> {
//                                });
//                            }
//                        } else {
//                            message.what = ResponseCode.EMPTY_RESPONSE;
//                            Log.e("RESPONSE_BODY_EMPTY", response.message());
//                        }
//                    } else {
//                        message.what = ResponseCode.SERVER_ERROR;
//                        Log.e("SERVER_ERROR", response.message());
//                    }
//                    handler.post(() -> Utils.showMessage(getApplicationContext(), message));
//                }
//            });
//        } catch (Exception e) {
//            message.what = ResponseCode.JSON_SERIALIZATION;
//            Utils.showMessage(getApplicationContext(), message);
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonLogin:
                buttonLogin();
                break;
            case R.id.buttonRegister:
                buttonSignUp();
                break;

            default:
                break;
        }
    }
}