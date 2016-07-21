package com.example.administrator.baidumaptest;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView locationInfoTextView = null;
    private Button startButton = null;
    private Button showMapButton;
    private LocationClient locationClient = null;
    private static final int UPDATE_TIME = 5000;
    private static int LOCATION_COUTNS = 0;
    private BDLocation bdLocation;
    private boolean isStart=false;  // 表示没有启动定位功能

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationInfoTextView = (TextView) this.findViewById(R.id.tv_loc_info);
        startButton = (Button) this.findViewById(R.id.btn_start);
        showMapButton=(Button)findViewById(R.id.btn_show_map);
        showMapButton.setOnClickListener(this);

        locationClient = new LocationClient(this);
        //设置定位条件
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);		//是否打开GPS
        option.setCoorType("bd09ll");		//设置返回值的坐标类型。
        option.setPriority(LocationClientOption.NetWorkFirst);	//设置定位优先级
        option.setProdName("LocationDemo");	//设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        option.setScanSpan(UPDATE_TIME);    //设置定时定位的时间间隔。单位毫秒
        locationClient.setLocOption(option);

        //注册位置监听器
        locationClient.registerLocationListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation location) {
                // TODO Auto-generated method stub
                if (location == null) {
                    return;
                }
                bdLocation=location;    // 存在当前定位提供器 对象
                // 判断显示地图的活动是否启动
                if(BaiduMapActivity.BaiduMapActivitySTATE) {
                    Message message = new Message();
                    message.what = BaiduMapActivity.UPDATE_MAP;
                    message.obj=bdLocation;
                    BaiduMapActivity.handler.sendMessage(message);  // 将Message对象发送出去
                }

                StringBuffer sb = new StringBuffer(256);
                sb.append("Time : ");
                sb.append(location.getTime());
                sb.append("\nError code : ");
                sb.append(location.getLocType());
                sb.append("\nLatitude : ");
                sb.append(location.getLatitude());
                sb.append("\nLontitude : ");
                sb.append(location.getLongitude());
                sb.append("\nRadius : ");
                sb.append(location.getRadius());
                if (location.getLocType() == BDLocation.TypeGpsLocation){
                    sb.append("\nSpeed : ");
                    sb.append(location.getSpeed());
                    sb.append("\nSatellite : ");
                    sb.append(location.getSatelliteNumber());
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                    sb.append("\nAddress : ");
                    sb.append(location.getAddrStr());
                }
                LOCATION_COUTNS ++;
                sb.append("\n检查位置更新次数：");
                sb.append(String.valueOf(LOCATION_COUTNS));
                locationInfoTextView.setText(sb.toString());
            }
        });

        /**
         * 启动按钮
         */
        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /**
                 * 没有可用定位的位置提供器
                 */
                if (locationClient == null) {
                    return;
                }
                // 暂停定位
                if (locationClient.isStarted()) {
                    isStart=false;
                    startButton.setText("Start");
                    locationClient.stop();
                }else {
                    // 启动定位
                    isStart=true;
                    startButton.setText("Stop");
                    locationClient.start();
					/*
					 *当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。
					 *调用requestLocation( )后，每隔设定的时间，定位SDK就会进行一次定位。
					 *如果定位SDK根据定位依据发现位置没有发生变化，就不会发起网络请求，
					 *返回上一次定位的结果；如果发现位置改变，就进行网络请求进行定位，得到新的定位结果。
					 *定时定位时，调用一次requestLocation，会定时监听到定位结果。
					 */
                    locationClient.requestLocation();
                }
            }
        });
    }

    /**
     * 启动地图的功能
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_show_map:
                // 匿名的内部类不能识别Toast
                if(bdLocation==null){
                    Toast.makeText(this,"请先启动定位,如果已经启动,请检查是否应用程序获取位置信息的权限,以及是否开启了GPS和网络以及WiFi",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isStart){
                    Toast.makeText(this,"请先点击启动按钮",Toast.LENGTH_SHORT).show();
                    return;
                }
                // 启动另一个活动显示当前位置信息的地图
                Intent intent= new Intent(MainActivity.this,BaiduMapActivity.class);
                intent.putExtra("lon",bdLocation.getLongitude());   // 经度
                intent.putExtra("lat",bdLocation.getLatitude());    // 维度
                Log.d("MainActivity","位置描述: "+ bdLocation.getLocationDescribe());
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
            locationClient = null;
        }
    }
}
