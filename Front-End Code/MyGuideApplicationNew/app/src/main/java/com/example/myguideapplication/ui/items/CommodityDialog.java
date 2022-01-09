package com.example.myguideapplication.ui.items;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myguideapplication.R;
import com.example.myguideapplication.activity.GuideActivity;
import com.example.myguideapplication.sparkbutton.SparkButton;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;


public class CommodityDialog extends Dialog {
    private ImageView imageIv;
    private TextView titleTv;
    private TextView messageTv;
    private TextView numTv;
    private Button negtiveBn, positiveBn;
    private SparkButton button;
    public CommodityDialog(Context context) {
        super(context, R.style.Theme_AppCompat_Dialog);
    }
    private int a;
    private String message;
    private String title;
    private String positive, negtive;
    private byte[] img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_commodity);
        setCanceledOnTouchOutside(false);
        initView();
        refreshView();
        initEvent();
    }

    private void initEvent() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!button.isChecked()){
                    playHeartAnimation(v);
                    button.setChecked(true);
                    a+=1;
                    numTv.setText(String.valueOf(a));
                }else{
                    button.setChecked(false);
                    a-=1;
                    numTv.setText(String.valueOf(a));
                }
            }
        });
        positiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener != null) {
                    onClickBottomListener.onPositiveClick();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        negtiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickBottomListener != null) {
                    onClickBottomListener.onNegtiveClick();
                }
            }
        });
    }

    private void refreshView() {
        //如果用户自定了title和message
        if (!TextUtils.isEmpty(title)) {
            titleTv.setText(title);
            titleTv.setVisibility(View.VISIBLE);
        } else {
            titleTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(message)) {
            messageTv.setText(message);
            messageTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
        a = (int)(Math.random()*100);
        numTv.setText(String.valueOf(a));
        //如果设置按钮的文字
        if (!TextUtils.isEmpty(positive)) {
            positiveBn.setText(positive);
        } else {
            positiveBn.setText("开始导航");
        }
        if (!TextUtils.isEmpty(negtive)) {
            negtiveBn.setText(negtive);
        } else {
            negtiveBn.setText("取消");
        }
        if(img!=null)
        {
            imageIv.setImageBitmap(createScaledBitmap(ByteToBitmap(img)));
        }
        else{
            imageIv.setImageResource(R.drawable.logo);
        }


    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    private void initView() {
        negtiveBn = (Button) findViewById(R.id.button12);
        positiveBn = (Button) findViewById(R.id.button11);
        titleTv = (TextView) findViewById(R.id.ComName);
        messageTv = (TextView) findViewById(R.id.ComInform);
        imageIv = (ImageView) findViewById(R.id.ComImg);
        numTv=(TextView)findViewById(R.id.textView2) ;
        button = (SparkButton) findViewById(R.id.heart_button);
    }


    public OnClickBottomListener onClickBottomListener;

    public CommodityDialog setOnClickBottomListener(OnClickBottomListener onClickBottomListener) {
        this.onClickBottomListener = onClickBottomListener;
        return this;
    }

    public interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        public void onPositiveClick();

        /**
         * 点击取消按钮事件
         */
        public void onNegtiveClick();
    }

    public String getMessage() {
        return message;
    }

    public CommodityDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CommodityDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPositive() {
        return positive;
    }

    public CommodityDialog setPositive(String positive) {
        this.positive = positive;
        return this;
    }

    public String getNegtive() {
        return negtive;
    }

    public CommodityDialog setNegtive(String negtive) {
        this.negtive = negtive;
        return this;
    }




    public CommodityDialog setImage(byte[] img) {

        this.img=img;
        return this;
    }
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
        int height=374;
        int width=435;
        int t_width;
        int t_height;
        System.out.println(width+","+height);
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
        System.out.println(t_width+","+t_height);
        bitmap = Bitmap.createScaledBitmap(bitmap, t_width, t_height, true);

        return bitmap;
    }
    private void playHeartAnimation(final View heartLayout) {
        ((SparkButton) heartLayout.findViewById(R.id.heart_button)).setChecked(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((SparkButton) heartLayout.findViewById(R.id.heart_button)).setChecked(true);
                ((SparkButton) heartLayout.findViewById(R.id.heart_button)).playAnimation();
            }
        }, 50);
    }
}

