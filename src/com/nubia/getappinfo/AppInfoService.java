package com.nubia.getappinfo;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.Toast;

public class AppInfoService extends Service {
	
	private File mAppLogFile;
	private BroadcastReceiver mReceiver = new AppPakReceiver();
	public static final String LOGFILEPATH = "AppLogFile/applog.txt";
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//注册广播接收
		IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
		intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
		intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
		intentFilter.addDataScheme("package");
	    registerReceiver(this.mReceiver, intentFilter);
	    
        try {Toast.makeText(AppInfoService.this, "已启动", Toast.LENGTH_SHORT).show();
            startService(new Intent(AppInfoService.this, BatteryDogService.class));
		} catch (Exception e) {
            Toast.makeText(AppInfoService.this, "Start Service failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
		} 	    		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(this.mReceiver);
    	try {
            stopService(new Intent(AppInfoService.this, BatteryDogService.class));
		} catch (Exception e) {
            Toast.makeText(AppInfoService.this, "Stop Service failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	private void getAppList(){
		Intent intent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.LAUNCHER");
	    List appList = getPackageManager().queryIntentActivities(intent, 0);
	    JSONObject appJSONObject = new JSONObject();
	    JSONArray appJSONArray2 = new JSONArray();   	
        Iterator appIterator = appList.iterator();
        int i = 0;
        while(true){
        	if(!appIterator.hasNext()){
        		try {
        			appJSONObject.put("root", appJSONArray2);
				} catch (JSONException e) {
					e.printStackTrace();
				}
        		logAppInfo(appJSONObject.toString());
        		Toast.makeText(this, "导入文件啦 " + appList.size(), Toast.LENGTH_SHORT).show();
        	    return;	
        	}
        	ResolveInfo appResolveInfo = (ResolveInfo)appIterator.next();
        	JSONObject appJSONObject1 = new JSONObject();     	
        	JSONObject appJSONObject2 = new JSONObject();
        	JSONArray appJSONArray1 = new JSONArray();
        	
        	String pakStr = appResolveInfo.activityInfo.packageName;
        	try{
        		appJSONObject1.put("title", appResolveInfo.loadLabel(getPackageManager()).toString());
        		appJSONObject1.put("activity", appResolveInfo.activityInfo.name);
        		appJSONArray1.put(appJSONObject1);
        		appJSONObject2.put("info", appJSONArray1);
        		appJSONObject2.put("package", pakStr);
        		appJSONArray2.put(appJSONObject2);
        		
        	}catch(JSONException appJSONException){
        		appJSONException.printStackTrace();
        	}

        }
	}
	
	private void logAppInfo(String appInfo) {
		try {
			FileWriter out = null;
			if (mAppLogFile != null) {
				try {
					out = new FileWriter(mAppLogFile, false);
				}
				catch (Exception e) {}
			}
			if (out == null) {
				File root = Environment.getExternalStorageDirectory();
				if (root == null)
					throw new Exception("external storage dir not found");
				mAppLogFile = new File(root,AppInfoService.LOGFILEPATH);
				boolean fileExists = mAppLogFile.exists();
				if (!fileExists) {
					mAppLogFile.getParentFile().mkdirs();
					mAppLogFile.createNewFile();
					Toast.makeText(this, "创建目录和文件 ", Toast.LENGTH_SHORT).show();
				}
				if (!mAppLogFile.exists()) 
					throw new Exception("creation of file '"+mAppLogFile.toString()+"' failed");
				if (!mAppLogFile.canWrite()) 
					throw new Exception("file '"+mAppLogFile.toString()+"' is not writable");
				out = new FileWriter(mAppLogFile, false);
				if (!fileExists) {
//					String header = "";
//					out.write(header);
//					out.write("\n");
				}
			}
			
			out.write(appInfo);
			out.write("\n");

			out.flush();
			out.close();
		} catch (Exception e) {
		}
	}
	
	private void getDisplay(){
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		getAppList();
		getDisplay();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
