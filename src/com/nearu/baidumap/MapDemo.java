package com.nearu.baidumap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKOLUpdateElement;
import com.baidu.mapapi.map.MKOfflineMap;
import com.baidu.mapapi.map.MKOfflineMapListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MapDemo extends Activity {
	BMapManager      mBMapManager = null;
	MapView              mMapView = null;
	MapController  mMapController = null;
	MKOfflineMap 		 mOffline = null;
	GeoPoint			   mPoint = null;
	BDLocation          mLocation = null;
	TextView			  popText = null;
	View				viewCache = null;
	PopupOverlay              pop = null;
	MyLocationOverlay 	 mLocationOverlay = null;
	public LocationClient mLocationClient = null;
	public BDLocationListener  myListener = new MyLocationListener();
	private final String              key = "D05eaed8231caf901d57441e1994747f";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//隐去标题（应用的名字)
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//此设定必须要写在setContentView之前，否则会有异常）
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		mLocation    = new BDLocation();
		mLocation.setLatitude(0.0);
		mLocation.setLongitude(0.0);
		mBMapManager = new BMapManager(getApplication());
		mBMapManager.init(key, null);
		setContentView(R.layout.mapdemo);		
		//set map view
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mMapView.setBuiltInZoomControls(true);
		mMapView.getOverlays().clear();
		mMapController = mMapView.getController();
		mMapController.setZoom(19);

		mLocationOverlay     = new MyLocationOverlay(mMapView);
		mMapView.getOverlays().add(mLocationOverlay);
		mLocationOverlay.enableCompass();
		
		Button button = (Button) findViewById(R.id.request);
		//set request button 		
		button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mMapController != null && mLocation.getLatitude() != 0.0){
					LocationData locData = new LocationData();
					locData.latitude  = mLocation.getLatitude();
					locData.longitude = mLocation.getLongitude();
					mLocationOverlay.setData(locData);
					mMapView.refresh();
					mMapController.animateTo(new GeoPoint((int)(locData.latitude * 1E6), (int)(locData.longitude * 1E6)));
				}	
			}
		});
		createPopup();
		//set start service for get user location-----------------------------
		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.setAK(key);
		mLocationClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("all");
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);
		option.disableCache(false);
		option.setPoiExtraInfo(false);
		mLocationClient.setLocOption(option);
		// location service set end--------------------------------------------
		
		// set offline map 
		mOffline = new MKOfflineMap();
		mOffline.init(mMapController, new MKOfflineMapListener(){
			@Override
			public void onGetOfflineMapState(int type, int state) {
				switch(type){
					case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
						MKOLUpdateElement update = mOffline.getUpdateInfo(state);
						break;
					case MKOfflineMap.TYPE_NEW_OFFLINE:
						 Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
						 break;
					case MKOfflineMap.TYPE_VER_UPDATE:
						Log.d("OfflineDemo", String.format("new offlinemap ver"));  
						break;
				}
			}
		});
		
		// set offline map end
		mLocationClient.start();
		if(mLocationClient != null){
			mLocationClient.requestLocation();
		}else {
			Log.v("nearu", "locClient is null or not started");
		}
		
	}
	private void createPopup(){
		viewCache = getLayoutInflater().inflate(R.layout.custom_text_view,null);
		popText = (TextView) findViewById(R.id.textcache);
		pop = new PopupOverlay(mMapView, new PopupClickListener(){
			@Override
			public void onClickedPopup(int arg0) {
					
			}
		});
	}
	protected void onDestory(){
		mMapView.destroy();
		if(mBMapManager != null){
			mBMapManager.destroy();
			mBMapManager = null;
		}
		super.onDestroy();
	}
	
	protected void onPause(){
		mMapView.onPause();
		if(mBMapManager != null){
			mBMapManager.stop();
		}
		super.onPause();
	}
	
	protected void onResume(){
		mMapView.onResume();
		if(mBMapManager != null){
			mBMapManager.start();
		}
		super.onResume();
	}
	public class locationOverlay extends MyLocationOverlay{

		public locationOverlay(MapView mapView) {
			super(mapView);
			// TODO Auto-generated constructor stub
		}
		@Override
		protected boolean dispatchTap(){
			popText.setBackgroundResource(R.drawable.popup);
			popText.setText("我的位置");
			pop.showPopup(getBitmapFromView(popText),
					      new GeoPoint((int) (mLocation.getLatitude() * 1e6), (int)(mLocation.getLongitude()) ),
					      8);
			
			return false;
			// process the click event on mylocationoverlay
			
		}
		private Bitmap getBitmapFromView(View view) {
			Bitmap bitmap = null;
			try {
				int width = view.getWidth();
				int height = view.getHeight();
				if(width != 0 && height != 0){
					bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(bitmap);
					view.layout(0, 0, width, height);
					view.draw(canvas);
				}
			} catch (Exception e) {
				bitmap = null;
				e.getStackTrace();
			}
			return bitmap;
		}

		
	}
	public class MyLocationListener implements BDLocationListener {
		
		@Override
		public void onReceiveLocation(BDLocation location) {
			if(location == null){
				Log.v("nearu", "location is null");
				return;
			}
			Log.v("nearu", "经度 = " + location.getLatitude() + " 纬度  =  " + location.getLongitude());
			mLocation.setLatitude(location.getLatitude());
			mLocation.setLongitude(location.getLongitude());
			mMapController.animateTo(new GeoPoint((int) (location.getLatitude()* 1E6),(int)(location.getLongitude()* 1E6)));
		}

		@Override
		public void onReceivePoi(BDLocation location) {
			
		}
	}
}


