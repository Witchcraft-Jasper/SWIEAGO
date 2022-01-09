package com.example.myguideapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.myguideapplication.R;
import com.example.myguideapplication.argument.NetworkSettings;
import com.example.myguideapplication.argument.ResponseCode;
import com.example.myguideapplication.construction.RestResponse;
import com.example.myguideapplication.construction.User;
import com.example.myguideapplication.databinding.ActivityMainBinding;
import com.example.myguideapplication.databinding.ActivityRegisterBinding;
import com.example.myguideapplication.util.ToastHelper;
import com.example.myguideapplication.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mob.MobSDK;
import com.mob.tools.utils.SharePrefrenceHelper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.UserInterruptException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private @NonNull
    ActivityRegisterBinding binding;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final int COUNTDOWN = 60;//设置每一分钟可以发送一次验证码
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Handler codeHandler;
    private final Message message = new Message();
    private EventHandler eventHandler;
    private SharePrefrenceHelper helper;
    private int currentSecond;//当前时间离1分钟差几秒
    private Animation bigAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobSDK.submitPolicyGrantResult(true, null);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        helper = new SharePrefrenceHelper(this);
        helper.open("sms_sp");

        initTool();
        initHandler();

        startAnimation();

        binding.buttonGetCode.setOnClickListener(this);
        binding.buttonRegister1.setOnClickListener(this);
        binding.buttonReturn.setOnClickListener(this);
    }

    private void startAnimation()
    {
        bigAnimation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.scale_big);

        binding.buttonGetCode.startAnimation(bigAnimation);
        binding.buttonRegister1.startAnimation(bigAnimation);
        binding.buttonReturn.startAnimation(bigAnimation);
        binding.editTextCode.startAnimation(bigAnimation);
        binding.editTextEmail.startAnimation(bigAnimation);
        binding.editTextName.startAnimation(bigAnimation);
        binding.editTextTel.startAnimation(bigAnimation);
        binding.editTextTextPassword.startAnimation(bigAnimation);
        binding.editTextTextPassword2.startAnimation(bigAnimation);
        binding.imageViewAccountIcon.startAnimation(bigAnimation);
        binding.imageViewCode.startAnimation(bigAnimation);
        binding.imageViewEmail.startAnimation(bigAnimation);
        binding.imageViewPassword.startAnimation(bigAnimation);
        binding.imageViewPassword2.startAnimation(bigAnimation);
        binding.imageViewTel.startAnimation(bigAnimation);
    }

    void initTool() {
        binding.editTextTel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //手机号输入大于5位，获取验证码按钮可点击
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.buttonGetCode.setEnabled(binding.editTextTel.getText().length() == 11);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.editTextCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.buttonRegister1.setEnabled(binding.editTextTel.getText() != null &&
                        binding.editTextCode.getText().length() > 3);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void initHandler() {
        codeHandler = new Handler() {
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                if (currentSecond > 0) {
                    binding.buttonGetCode.setText("已发送" + "(" + currentSecond + "s)");
                    binding.buttonGetCode.setEnabled(false);
                    currentSecond--;
                    codeHandler.sendEmptyMessageDelayed(0, 1000);
                } else {
                    binding.buttonGetCode.setText("获取验证码");
                    binding.buttonGetCode.setEnabled(true);
                }
            }
        };

        eventHandler = new EventHandler() {
            @Override
            public void afterEvent(final int event, final int result, final Object data) {
                String name = binding.editTextName.getText().toString();
                String password = binding.editTextTextPassword.getText().toString();
                String password2 = binding.editTextTextPassword2.getText().toString();
                String phone = binding.editTextTel.getText().toString();
                String email = binding.editTextEmail.getText().toString();
                if (!isNetworkConnected()) {
                    return;
                } else if (!password.equals(password2)) {
                    message.what = ResponseCode.WRONG_PASS;
                    Utils.showMessage(getApplicationContext(), message);
                    return;
                } else if ("".equals(name) || "".equals(password) || "".equals(email)) {
                    message.what = ResponseCode.EMPTY_INFO;
                    Utils.showMessage(getApplicationContext(), message);
                    return;
                }
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    runOnUiThread(() -> {
                        try {
                            //提交验证成功，跳转成功页面，否则toast提示
                            if (result == SMSSDK.RESULT_COMPLETE) {
                                Request request = null;
                                request = new Request.Builder().url(NetworkSettings.SIGN_UP).post(
                                        RequestBody.create(mapper.writeValueAsString(new User(name, password, email, 0, phone)),
                                                NetworkSettings.mediaType)).build();
                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                        message.what = ResponseCode.REQUEST_FAILED;
                                        handler.post(() -> Utils.showMessage(getApplicationContext(), message));
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                        if (response.isSuccessful()) {
                                            ResponseBody body = response.body();
                                            if (body != null) {
                                                RestResponse restResponse = mapper.readValue(body.string(), RestResponse.class);
                                                message.what = restResponse.getCode();
                                                if (message.what == ResponseCode.SIGN_UP_SUCCESS) {
                                                    handler.post(() -> {

                                                    });
                                                } else {
                                                    handler.post(() -> {
                                                        binding.editTextName.setText("");
                                                        binding.editTextTextPassword.setText("");
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
                            }
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    });
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE || event == SMSSDK.EVENT_GET_VOICE_VERIFICATION_CODE) {
                    runOnUiThread(() -> {
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            currentSecond = COUNTDOWN;
                            codeHandler.sendEmptyMessage(0);
                            helper.putLong("start_time", System.currentTimeMillis());
                        } else {
                            if (data instanceof UserInterruptException) {
                            }
                        }
                    });
                }else {
                    ToastHelper.showToast(RegisterActivity.this,"验证码错误",0);
                }
            }
        };
        SMSSDK.registerEventHandler(eventHandler);
        binding.buttonGetCode.setEnabled(false);
        binding.buttonRegister1.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            binding.buttonGetCode.setEnabled(true);
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (codeHandler != null) {
            codeHandler.removeCallbacksAndMessages(null);
        }
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonRegister1:
                if (!isNetworkConnected()) {
                    break;
                }
                SMSSDK.submitVerificationCode("86",
                        binding.editTextTel.getText().toString().trim(), binding.editTextCode.getText().toString());
                break;
            case R.id.buttonGetCode:
                long startTime = helper.getLong("start_time");
                if (System.currentTimeMillis() - startTime < COUNTDOWN * 1000) {
                    break;
                }
                if (!isNetworkConnected()) {
                    break;
                }
                SMSSDK.getVerificationCode("86", binding.editTextTel.getText().toString().trim());
                currentSecond = COUNTDOWN;
                codeHandler.sendEmptyMessage(0);
                helper.putLong("start_time", System.currentTimeMillis());
                break;
            case R.id.buttonReturn:
                finish();
            default:
                break;
        }
    }
}