package com.example.myguideapplication.ui.notifications;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.myguideapplication.R;
import com.example.myguideapplication.argument.NetworkSettings;
import com.example.myguideapplication.argument.ResponseCode;
import com.example.myguideapplication.construction.RestResponse;
import com.example.myguideapplication.construction.User;
import com.example.myguideapplication.databinding.FragmentNotificationsBinding;
import com.example.myguideapplication.util.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NotificationsFragment extends Fragment implements View.OnClickListener {
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private static boolean switchState;
    private NotificationsViewModel notificationsViewModel;
    public static FragmentNotificationsBinding binding;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Message message = new Message();
    private AlphaAnimation hideAni;
    private AlphaAnimation showAni;

    private Uri imageUri;

    @Override
    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });

        switchState = false;
        binding.imageFile.setAlpha((float) 0.4);

        binding.revisePortrait.setFocusable(true);
        binding.revisePortrait.setClickable(true);
        binding.revisePortrait.setOnClickListener(this);

        initUser();
        initAnimation();
        return root;
    }

    private void initAnimation(){
        showAni = new AlphaAnimation((float) 0.2,1);
        hideAni = new AlphaAnimation(1, (float) 0.2);
        showAni.setDuration(700);
        hideAni.setDuration(700);

        hideAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(switchState){
                    binding.imageCamera.setAlpha((float) 0.2);
                    binding.imageFile.setAlpha((float) 1);
                }else{
                    binding.imageFile.setAlpha((float) 0.2);
                    binding.imageCamera.setAlpha((float) 1);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        binding.switchGetImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (binding.switchGetImage.isChecked()) {
                    switchState = true;
                    binding.imageCamera.startAnimation(hideAni);
                    binding.imageFile.startAnimation(showAni);
                    Toast.makeText(getActivity(), "相册获取头像", Toast.LENGTH_LONG).show();
                } else {
                    switchState = false;
                    binding.imageFile.startAnimation(hideAni);
                    binding.imageCamera.startAnimation(showAni);
                    Toast.makeText(getActivity(), "拍照获取头像", Toast.LENGTH_LONG).show();
                }
            }
        });

        binding.buttonSave.setOnClickListener(this);
    }

    private void takePhoto() {
        File outputImage = new File(String.valueOf(getContext().getExternalCacheDir()), "output_image.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(NotificationsFragment.this.getContext(), "com.example.cameraalbumtest.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        //启动相机
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                        ByteArrayOutputStream baos=new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                        byte[] data1= baos.toByteArray();
//                        ByteBuffer buffer=ByteBuffer.allocate(bitmap.getByteCount());
//                        bitmap.copyPixelsToBuffer(buffer);
//                        byte[] data1= buffer.array();
                        User.getUser().setProfile(data1);
                        binding.riPortrait.setImageBitmap(bitmap);
                        //将图片解析成Bitmap对象，并把它显现出来
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    System.out.println(data.toString());
                    handleImageOnKitKat(data);
                }

            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(getContext(), uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        // 根据图片路径显示图片
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
//            ByteBuffer buffer=ByteBuffer.allocate(bitmap.getByteCount());
//            bitmap.copyPixelsToBuffer(buffer);
//            byte[] data= buffer.array();
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            byte[] data= baos.toByteArray();
            User.getUser().setProfile(data);
            binding.riPortrait.setImageBitmap(bitmap);
        } else {
            Toast.makeText(getActivity(), "获取相册图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initUser() {
        binding.igAccount.getContentEdt().setText(User.getUser().getId().toString());
        binding.igEmail.getContentEdt().setText(User.getUser().getEmail());
        binding.igName.getContentEdt().setText(User.getUser().getUsername());
        binding.igTel.getContentEdt().setText(User.getUser().getPhone());
        if (User.getUser().getProfile() != null) {
            binding.riPortrait.setImageBitmap(ByteToBitmap(User.getUser().getProfile()));
        }
        switch (User.getUser().getVIP()) {
            case 1:
                binding.igVip.getContentEdt().setText("黄金会员");
                break;
            case 2:
                binding.igVip.getContentEdt().setText("钻石会员");
                break;
            default:
                binding.igVip.getContentEdt().setText("非会员");
                break;
        }
    }

    public void update() {
        try {
            User user = User.getUser();
            String data = mapper.writeValueAsString(new User(user.getId(), binding.igName.getContentEdt().getText().toString(),
                    user.getPassword(),
                    binding.igEmail.getContentEdt().getText().toString(),
                    user.getVIP(), binding.igTel.getContentEdt().getText().toString(),
                    user.getProfile()));
            Request request = new Request.Builder().url(NetworkSettings.UPDATE).post(
                    RequestBody.create(
                            mapper.writeValueAsString(new User(user.getId(), binding.igName.getContentEdt().getText().toString(),
                                    user.getPassword(),
                                    binding.igEmail.getContentEdt().getText().toString(),
                                    user.getVIP(), binding.igTel.getContentEdt().getText().toString(),
                                    user.getProfile())),
                            NetworkSettings.mediaType
                    )
            ).build();
            Log.w("Ho", data);
            //System.out.println("Ho"+data);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    message.what = ResponseCode.REQUEST_FAILED;
                    handler.post(() -> Utils.showMessage(getContext(), message));
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            RestResponse restResponse = mapper.readValue(body.string(), RestResponse.class);
                            message.what = restResponse.getCode();
                            if (message.what == ResponseCode.UPDATE_SUCCESS) {
                                System.out.println("HO");
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

//    public static Bitmap getLocalBitmap(String url) {
//        try {
//            FileInputStream fis = new FileInputStream(url);
//            return BitmapFactory.decodeStream(fis);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public Bitmap ByteToBitmap(byte[] str)
    {
        Bitmap bitmap=null;
        try{
            bitmap= BitmapFactory.decodeByteArray(str,0,str.length);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return bitmap;
    }
//
    private Bitmap createScaledBitmap(Bitmap bitmap){

        int width = 165;
        int height =165;
        int t_width;
        int t_height;
        if (bitmap.getWidth()>width || bitmap.getHeight()>height){
            t_width = width;
            t_height = bitmap.getHeight()*width/bitmap.getWidth();
            if (t_height>height){
                t_width = t_width*height/t_height;
                t_height = height;
            }
        } else
        if (bitmap.getWidth()<width && bitmap.getHeight()<height){
            t_width = width;
            t_height = bitmap.getHeight()*width/bitmap.getWidth();
            if (t_height>height){
                t_width = t_width*height/t_height;
                t_height = height;
            }
        } else {
            t_width = bitmap.getWidth();
            t_height = bitmap.getHeight();
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, t_width, t_height, true);
        return bitmap;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                update();
                break;
            case R.id.revise_portrait:
                System.out.println(switchState);
                if (switchState) {
                    openAlbum();
                } else {
                    takePhoto();
                }
                break;
            default:
                break;
        }
    }
}