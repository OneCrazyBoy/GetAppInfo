/*******************************************************************************
 * Copyright (c) 2009 Ferenc Hechler - ferenc_hechler@users.sourceforge.net
 * 
 * This file is part of the Android Battery Dog
 *
 * The Android Battery Dog is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * 
 * The Android Battery Dog is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Android Battery Dog;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 *******************************************************************************/
package com.nubia.getappinfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

public class BatteryDogService extends Service {

	private final static String TAG = "BATDOG.service";
	
	public static final String LOGFILEPATH = "BatteryDog/battery.csv";
	public static final String BATTERYPATH = "AppLogFile/battery.txt";
	
	private final static String[] batteryExtraKeys = {"level", "scale", "voltage", "temperature", "plugged", "status", "health", "present", "technology", "icon-small"};

	private File mBatteryLogFile;
	private int mCount;
	private Intent mLastBatteryIntent;
    private boolean mQuitThread;
    private boolean mThreadRunning;


    @Override
    public void onCreate() {
    	super.onCreate();
    	File file = new File("/sdcard/AppLogFile");
    	if(!file.exists()){
    		file.mkdirs();
    	}
		if (!mThreadRunning) {
			mCount = 0;
			mLastBatteryIntent = null;
			mQuitThread = false;
	        Thread thr = new Thread(null, mTask, "BatteryDog_Service");
	        thr.start();
			registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	        Toast.makeText(this, "BatteryDog Service started", Toast.LENGTH_SHORT).show();
		}
    }
	
	@Override
	public void onDestroy() {
    	Log.i(TAG, "onDestroy");
        mQuitThread = true;
        notifyService();
        
    	super.onDestroy();
    	unregisterReceiver(mBatInfoReceiver);
        Toast.makeText(this, "BatteryDog Service stopped", Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
    public void method1(String file, String conent) {     
        BufferedWriter out = null;     
        try {     
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)));     
            out.write(conent);     
        } catch (Exception e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(out != null){  
                    out.close();     
                }  
            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        }     
    } 
	

	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			try {
            	mCount += 1;
				mLastBatteryIntent = (Intent) intent.clone();
				notifyService();
			}
			catch (Exception e) {
				Log.e(TAG,e.getMessage(), e);
			}
		}

	};

	private void logBattery(Intent batteryChangeIntent) {
		if (batteryChangeIntent == null)
			return;
		try {
//			FileWriter out = null;
//			if (mBatteryLogFile != null) {
//				try {
//					out = new FileWriter(mBatteryLogFile, true);
//				}
//				catch (Exception e) {}
//			}
//			if (out == null) {
//				File root = Environment.getExternalStorageDirectory();
//				if (root == null)
//					throw new Exception("external storage dir not found");
//				mBatteryLogFile = new File(root,BatteryDogService.LOGFILEPATH);
//				boolean fileExists = mBatteryLogFile.exists();
//				if (!fileExists) {
//					mBatteryLogFile.getParentFile().mkdirs();
//					mBatteryLogFile.createNewFile();
//				}
//				if (!mBatteryLogFile.exists()) 
//					throw new Exception("creation of file '"+mBatteryLogFile.toString()+"' failed");
//				if (!mBatteryLogFile.canWrite()) 
//					throw new Exception("file '"+mBatteryLogFile.toString()+"' is not writable");
//				out = new FileWriter(mBatteryLogFile, true);
//				if (!fileExists) {
//					String header = createHeadLine();
//					out.write(header);
//					out.write("\n");
//				}
//			}
			if (mLastBatteryIntent != null) {
				String extras = createBatteryInfoLine(mLastBatteryIntent);
//				out.write(extras);
//				out.write("\n");
			}
//			out.flush();
//			out.close();
		} catch (Exception e) {
			Log.e(TAG,e.getMessage(),e);
		}
	}

	
//    private String createHeadLine() {
//    	StringBuffer result = new StringBuffer();
//    	result.append("电池记录开始:");
//		return result.toString();
//	}

	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private DecimalFormat dfT = new DecimalFormat("###.#");
	private DecimalFormat dfV = new DecimalFormat("##.###");
	
	private String createBatteryInfoLine(Intent batteryIntent) {
    	StringBuffer extrasResult = new StringBuffer();
    	extrasResult.append(Integer.toString(mCount)).append(";").append(Long.toString(System.currentTimeMillis()));
    	Bundle extras = batteryIntent.getExtras();
    	for (String key : batteryExtraKeys)
    		extrasResult.append(";").append(extras.get(key));
    	String[] split = extrasResult.toString().split(";");
    	try {
			int count = Integer.parseInt(split[0]);
			long time = Long.parseLong(split[1]);
			int level = Integer.parseInt(split[2]);
			int scale = Integer.parseInt(split[3]);
			int percent = level*100/scale;
			int voltage = Integer.parseInt(split[4]);
			int temperature = Integer.parseInt(split[5]);
			double v = 0.001*voltage;
			double t = 0.1*temperature;
		
	    	File file = new File("/sdcard/AppLogFile");
	    	if(file.exists()){
	    		method1("/sdcard/AppLogFile/battery.txt",level+"");
	    	}
			String timestamp = sdf.format(new Date(time));
			StringBuffer result = new StringBuffer();
			result.append(Integer.toString(count)).append(")   ")
					.append(timestamp).append("   ")
					.append(percent).append("%   ")
					.append(dfV.format(v)).append("V   ")
					.append(dfT.format(t)).append("C")
					;
			return result.toString();
		}
		catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return null;
		}
		
	}

	/**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {

		public void run() {
            mThreadRunning = true;
            Log.i(TAG,"STARTING BATTERYDOG TASK");
            while (!mQuitThread) {
				logBattery(mLastBatteryIntent);
                synchronized (BatteryDogService.this) {
                	try {
                    	BatteryDogService.this.wait();
                	} catch (Exception ignore) {}
                }
            }
            mThreadRunning = false;
			logBattery(mLastBatteryIntent);
            Log.i(TAG,"LEAVING BATTERYDOG TASK");
        }

    };
	

	public void notifyService() {
		synchronized (BatteryDogService.this) {
			BatteryDogService.this.notifyAll();
		}
	}
}

