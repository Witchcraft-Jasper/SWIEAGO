package com.example.myguideapplication.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.myguideapplication.R;
import com.example.myguideapplication.activity.MainActivity;
import com.example.myguideapplication.argument.NetworkSettings;
import com.example.myguideapplication.argument.ResponseCode;
import com.example.myguideapplication.construction.Point;
import com.example.myguideapplication.construction.RestResponse;
import com.example.myguideapplication.databinding.FragmentHomeBinding;
import com.example.myguideapplication.ui.items.ItemListFragment;
import com.example.myguideapplication.util.Utils;
import com.example.myguideapplication.util.WifiUtilThread;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chrisbanes.photoview.PhotoView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private HomeViewModel homeViewModel;
    public static FragmentHomeBinding binding;
    private static PhotoView mapImage;
    private static TextView location;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Message message = new Message();
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private float rectLeft;
    private float rectTop;

    private WifiUtilThread wifiUtilThread;
    private Thread upDateThread;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }

        });

        wifiUtilThread = new WifiUtilThread(getContext());
        wifiUtilThread.start();

        mapImage = binding.photoMapView;
        location = binding.locationView;
        binding.editTextSearch.bringToFront();
        binding.searchButton.bringToFront();

        upDateThread = new Thread() {
            @Override
            public synchronized void run() {
                while (true) {
                    System.out.println(((wifiUtilThread.getCoordinate().getX()) * mapImage.getScale() + rectLeft - 75) +"+"+ (location.getX()));
                    if (wifiUtilThread.getCoordinate().getX() != -1) {
                        location.setX((wifiUtilThread.getCoordinate().getX()) * mapImage.getScale() + rectLeft - 75);
                        location.setY((wifiUtilThread.getCoordinate().getY()) * mapImage.getScale() + rectTop - 125);
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        upDateThread.start();

        setListener();

        return root;
    }

    private void onSearch() {
        try {
            String name = HomeFragment.binding.editTextSearch.getText().toString();
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
                            System.out.println(restResponse.getData());
                            try {
                                ArrayList<Point> al = (ArrayList) restResponse.getData();
                                if (message.what == ResponseCode.QUERY_SUCCESS) {
                                    handler.post(() -> {
                                        Point.setPoints(al);
                                        Point.setJudge(1);
                                        if (!Point.getPoints().isEmpty()) {
                                            new ItemListFragment().newInstance(Point.getPoints().size()).show(getActivity().getSupportFragmentManager(), "item_count");
                                        }
                                    });
                                } else {
                                    handler.post(() -> {
                                    });
                                }
                            } catch (ClassCastException castException) {
                                return;
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


    private void showDestinationIcon() {
        binding.destinationView.setVisibility(View.VISIBLE);
        binding.destinationView.setX(((Point.getPoint().getX()) * mapImage.getScale() + rectLeft - 75));
        binding.destinationView.setY(((Point.getPoint().getY()) * mapImage.getScale() + rectTop - 125));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        wifiUtilThread.setPause(false);
        if (Point.getPoint().getX() != 0) {
            showDestinationIcon();
        }
    }

    private void destinationDissmiss() {
        binding.destinationView.setVisibility(View.INVISIBLE);
        Point.getPoint().setX(0);
        Point.getPoint().setY(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        wifiUtilThread.setPause(true);
    }

    private void setListener() {
        binding.searchButton.setOnClickListener(this);

        binding.destinationView.setLongClickable(true);
        binding.destinationView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                destinationDissmiss();
                return false;
            }
        });

        mapImage.setOnMatrixChangeListener(rect -> {
            rectLeft = rect.left;
            rectTop = rect.top;
            binding.destinationView.setX((float) ((Point.getPoint().getX()) * mapImage.getScale() + rectLeft - 75));
            binding.destinationView.setY((float) ((Point.getPoint().getY()) * mapImage.getScale() + rectTop - 125));
            location.setX((float) ((wifiUtilThread.getCoordinate().getX()) * mapImage.getScale() + rectLeft - 75));
            location.setY((float) ((wifiUtilThread.getCoordinate().getY()) * mapImage.getScale() + rectTop - 125));
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchButton:
                onSearch();
        }
    }
}