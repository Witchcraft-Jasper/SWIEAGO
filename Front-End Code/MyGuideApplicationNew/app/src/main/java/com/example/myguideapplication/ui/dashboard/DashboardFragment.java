package com.example.myguideapplication.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.myguideapplication.R;
import com.example.myguideapplication.argument.NetworkSettings;
import com.example.myguideapplication.argument.ResponseCode;
import com.example.myguideapplication.construction.Point;
import com.example.myguideapplication.construction.RestResponse;
import com.example.myguideapplication.databinding.FragmentDashboardBinding;
import com.example.myguideapplication.ui.items.ItemListFragment;
import com.example.myguideapplication.util.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    public static FragmentDashboardBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Message message = new Message();
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(1);
            }
        });
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(2);
            }
        });
        binding.button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(3);
            }
        });
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void search(int i){
        int Type=i;
        try {
            Request request = new Request.Builder().url(NetworkSettings.FLOOR_QUERY + "?floor=" + Type).get().build();
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
                            System.out.println(restResponse.getData());
                            ArrayList<Point> al;
                            try{
                                al=(ArrayList)restResponse.getData();
                            } catch (ClassCastException castException) {
                                return;
                            }
                            if (message.what == ResponseCode.QUERY_SUCCESS) {
                                handler.post(() -> {
                                    Point.setPoints(al);
                                    Point.setJudge(2);
                                    if(!Point.getPoints().isEmpty()) {
                                        new ItemListFragment().newInstance(Point.getPoints().size()).show(getActivity().getSupportFragmentManager(), "item_count");
                                    }
                                });
                            }else {
                                handler.post(() -> {
                                    message.what = ResponseCode.QUERY_FAILED;
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
                    handler.post(() -> Utils.showMessage(getContext(), message));
                }
            });

        } catch (Exception e) {
            message.what = ResponseCode.JSON_SERIALIZATION;
            Utils.showMessage(getContext(), message);
            e.printStackTrace();
        }
    }
}