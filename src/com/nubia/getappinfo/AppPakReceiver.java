package com.nubia.getappinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AppPakReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		String pakStr = null;
		 if (Intent.ACTION_PACKAGE_ADDED.equals(action)){ 
			 pakStr = intent.getData().getSchemeSpecificPart();
			 if(pakStr == null || pakStr.length() == 0){
				 return;
			 }		 
	        Toast.makeText(context, "有应用被添加", Toast.LENGTH_LONG).show();  
	     }else if(Intent.ACTION_PACKAGE_REMOVED.equals(action)){
	    	 Toast.makeText(context, "有应用被删除", Toast.LENGTH_LONG).show();   
	     }else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)){  
	         Toast.makeText(context, "有应用被替换", Toast.LENGTH_LONG).show();  
	     } 
		
	}

}
