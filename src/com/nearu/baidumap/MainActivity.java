package com.nearu.baidumap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	TextView tv1;
	Location location;
	LocationManager lm;
	LocationListener locationListener;
	//通过network获取location
	private String networkProvider = LocationManager.NETWORK_PROVIDER;
	//通过gps获取location
	private String GpsProvider = LocationManager.GPS_PROVIDER;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 定义UI组件
		Button b1 = (Button) findViewById(R.id.getLocation);
		Button b2 = (Button) findViewById(R.id.openMap);
		tv1 = (TextView) findViewById(R.id.textView);
		//开始执行获取location对象
		initLocation(MainActivity.this);
		//这个不解释
		b1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//实际这段代码再次更新了tv中的信息
				initLocation(MainActivity.this);
			}
		});
		b2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this, MapDemo.class);
				startActivity(intent);
			}
		});
	}
	
	//获取location对象
	private void initLocation(Context mContext){
		//获得系统及服务的  LocationManager 对象  这个代码就这么写 不用考虑
		lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		
		//首先检测 通过network 能否获得location对象
		//如果获得了location对象 则更新tv
		if (startLocation(networkProvider,mContext)) {
			updateLocation(location,mContext);
		}else 
			//通过gps 能否获得location对象
			//如果获得了location对象 则更新tv
			if(startLocation(GpsProvider,mContext)){
			updateLocation(location,mContext);
		}else{
			//如果上面两种方法都不能获得location对象 则显示下列信息
			Toast.makeText(this, "没有打开GPS设备", 5000).show();
		}
	}
	/**
	 * 通过参数 获取Location对象
	 * 如果Location对象为空 则返回 true 并且赋值给全局变量 location
	 *   如果为空 返回false 不赋值给全局变量location
	 * 
	 * @param provider
	 * @param mContext
	 * @return
	 */
	private boolean startLocation(String provider,final Context mContext){
		Location location = lm.getLastKnownLocation(provider);
		
		// 位置监听器
		locationListener = new LocationListener() {
			// 当位置改变时触发
			@Override
			public void onLocationChanged(Location location) {
				System.out.println(location.toString());
				updateLocation(location,mContext);
			}

			// Provider失效时触发
			@Override
			public void onProviderDisabled(String arg0) {
				System.out.println(arg0);
			}

			// Provider可用时触发
			@Override
			public void onProviderEnabled(String arg0) {
				System.out.println(arg0);
			}

			// Provider状态改变时触发
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				System.out.println("onStatusChanged");
			}
		};

		// 500毫秒更新一次，忽略位置变化
		lm.requestLocationUpdates(provider, 500, 0, locationListener);
		
//		如果Location对象为空 则返回 true 并且赋值给全局变量 location
//		如果为空 返回false 不赋值给全局变量location
		if (location!= null) {
			this.location=location;
			return true;
		}
		return false;
		
	}
	// 更新位置信息 展示到tv中
	private void updateLocation(Location location,Context mContext) {
		if (location != null) {
			tv1.setText("定位对象信息如下：" + location.toString() + "/n/t其中经度："
					+ location.getLongitude() + "/n/t其中纬度："
					+ location.getLatitude());
			//如果已经获取到location信息 则在这里注销location的监听
			//gps会在一定时间内自动关闭
			lm.removeUpdates(locationListener);
		} else {
			System.out.println("没有获取到定位对象Location");
		}
	}

	protected void onDestroy() {
		//当这个activity销毁时  在这里注销location的监听
		//gps会在一定时间内自动关闭
		lm.removeUpdates(locationListener);
		
		super.onDestroy();
	}

}
