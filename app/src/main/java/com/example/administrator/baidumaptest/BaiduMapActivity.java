package com.example.administrator.baidumaptest;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class BaiduMapActivity extends AppCompatActivity {

    /**
     * 显示地图的控件
     */
    private MapView mapView;

    private static BaiduMap baiduMap;

    /**
     * 表示更新地图的操作,用于异步消息处理机制
     */
    public static final int UPDATE_MAP=1;

    /**
     * 表示活动没有启动,默认为false,在onCreate()中为true,onDestroy()中为false
     */
    public static boolean BaiduMapActivitySTATE=false;

    private static boolean isFirstRun=true;

    /**
     * 必须声明为static,否则必须实例化对象才能使用
     */
    public static Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_MAP:
                    // 更新地图
                    BDLocation bdLocation=(BDLocation) msg.obj;
                    double lat=bdLocation.getLatitude();
                    double lon=bdLocation.getLongitude();

                    navigateTo(lat,lon);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_baidu_map);

        BaiduMapActivitySTATE=true;
        mapView=(MapView)findViewById(R.id.map_view);
        baiduMap=mapView.getMap();  // 初始化BaiduMap
        baiduMap.setMyLocationEnabled(true);    // 打开显示当前位置的功能

        Intent intent=getIntent();  //  获取用于启动当前活动的Intent
        double lat=intent.getDoubleExtra("lat",4.9E-324D );
        double lon=intent.getDoubleExtra("lon",4.9E-324D);
        navigateTo(lat,lon);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baiduMap.setMyLocationEnabled(false);
        BaiduMapActivitySTATE=false;
        isFirstRun=true;
    }

    /**
     * 更新地图的位置信息
     * @param lat   纬度
     * @param lon   经度
     */
    private static void navigateTo(double lat,double lon){
        LatLng ll=new LatLng(lat,lon);
        MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(update);
        // 启动活动的首次加载地图
        if(isFirstRun) {
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstRun=false;
        }

        // 在地图上面显示我的当前位置
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(lat);
        locationBuilder.longitude(lon);
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }
}
