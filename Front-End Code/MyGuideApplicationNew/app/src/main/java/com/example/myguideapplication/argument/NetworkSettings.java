package com.example.myguideapplication.argument;

import okhttp3.MediaType;

public class NetworkSettings {
//    private static final String HOST = "192.168.137.1";
//
//    private static final String PORT = "8080";

    //private static final String HOST = "4162w1944k.wicp.vip";
    private static final String HOST = "414641hi56.qicp.vip";
    private static final String PORT = "80";
    public static final String SIGN_IN = "http://"+ HOST +":"+PORT + "/User/Sign/in";
    public static final String WIFI_MES = "http://"+ HOST +":"+PORT + "/Wifi/get";
    public static final String SIGN_UP = "http://"+ HOST +":"+PORT + "/User/Sign/up";
    public static final String UPDATE = "http://"+ HOST +":"+PORT + "/User/update";
    public static final String DELETE = "http://"+ HOST +":"+PORT + "/delete";
    public static final String POINT_QUERY = "http://"+ HOST +":"+PORT + "/Point/get";
    public static final String FLOOR_QUERY = "http://"+ HOST +":"+PORT + "/Point/get1";
    public static final MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
}
