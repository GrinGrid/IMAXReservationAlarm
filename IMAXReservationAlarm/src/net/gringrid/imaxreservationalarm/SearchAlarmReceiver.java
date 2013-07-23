package net.gringrid.imaxreservationalarm;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SearchAlarmReceiver extends BroadcastReceiver {


	private static final boolean DEBUG = true;
	public static String ACTION_ALARM = "com.alarammanager.alaram";
	private Context mContext;
	private ArrayList<String> mTotalList;
	private ArrayList<String> mList;
	private boolean mStart;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;

		Bundle bundle = intent.getExtras();
		String cgvUrl = bundle.getString("cgvUrl");
		String targetDay = bundle.getString("targetDay");

		Log.d("jiho", "AlarmReceiver cgvUrl : "+cgvUrl);
		Log.d("jiho", "AlarmReceiver targetDay : "+targetDay);


		//String url = "http://cgv.co.kr/Reservation/timeTable/Default.aspx?theaterCode=0074";
		String url = cgvUrl;
		HttpPost httpPost = new HttpPost(url);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        mTotalList = new ArrayList<String>();
        
        try {        	
        	
            response = client.execute(httpPost);
 
            
            HttpEntity entity = response.getEntity();            
            InputStream stream = entity.getContent();
            
            // 한글을 위해
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "EUC-KR"));
            
            String line = null;
            mList = new ArrayList<String>();
            //Reader reader=new InputStreamReader(stream);
            while ( (line = br.readLine()) != null ){            	
            	mTotalList.add(line);            	
            }
            
            String findStr = "class=\"tlt\"";
    		String imaxStr = "IMAX";
    		
    		for ( String totalLine : mTotalList ){
    			if ( totalLine.indexOf( findStr ) != -1 ){
    				if ( totalLine.indexOf( imaxStr) != -1 ){
    					//mList.add(totalLine);
    					
    					long time = System.currentTimeMillis(); 
                		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                		String currentTime = sdf.format(new Date(time));
                		
                		NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(mContext);
            			notiBuilder.setSmallIcon(R.drawable.ic_launcher);
            			notiBuilder.setContentTitle("CGV Alarm");
            			notiBuilder.setContentText("TIME : "+currentTime);
            			
            			Notification notification = notiBuilder.build();

            			notification.flags = Notification.FLAG_AUTO_CANCEL;
            			
            			notification.vibrate = new long[] {0,1000,1100};
            			
            			int notificationID = Integer.parseInt(currentTime.substring(0,10).replace("-", ""));
            			Log.d("jiho", "notificationID : "+notificationID);
            			
            			NotificationManager notificationManager = null;
            			notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            			notificationManager.notify(notificationID, notification);
                		
                		Log.d("jiho", "!!!!!!!!!!!!!!! NOTIFICATION !!!!!!!!!!!!!!");

    					
    					
    				}
    			}
    		}
    		
            
            /*
            for ( String tmp : mTotalList ){
    			if ( tmp.indexOf("liDate\" class=\"today\"") != -1 ){
            		mStart = true;            		
            	}
            	
            	if ( mStart ){
            		if ( tmp.indexOf("</ul>") != -1 ){
            			mStart = false;
            			break;
            		}
            		if ( tmp.trim().length() > 0 ){
            			mList.add(tmp);
            		}
            	} 
    		}
            
    		
            for ( String tmp : mList ){
            	//Log.d("jiho", tmp);
            	if ( tmp.indexOf(targetDay) != -1 && tmp.indexOf("die") == -1 ){
            		
            		long time = System.currentTimeMillis(); 
            		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            		String currentTime = sdf.format(new Date(time));
            		
            		NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(mContext);
        			notiBuilder.setSmallIcon(R.drawable.ic_launcher);
        			notiBuilder.setContentTitle("CGV Alarm");
        			notiBuilder.setContentText("TIME : "+currentTime);
        			
        			Notification notification = notiBuilder.build();

        			notification.flags = Notification.FLAG_AUTO_CANCEL;
        			
        			notification.vibrate = new long[] {0,1000,1100};
        			
        			int notificationID = Integer.parseInt(currentTime.substring(0,10).replace("-", ""));
        			Log.d("jiho", "notificationID : "+notificationID);
        			
        			NotificationManager notificationManager = null;
        			notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        			notificationManager.notify(notificationID, notification);
            		
            		Log.d("jiho", tmp);
            		
            		Log.d("jiho", "!!!!!!!!!!!!!!! NOTIFICATION !!!!!!!!!!!!!!");
            	}
            }
            */
            //loadTargetString();

        } catch (ClientProtocolException e) {
        	Log.d("jiho", "ClientProtocolException");
        	
        } catch (IOException e) {
        	Log.d("jiho", "IOException");
        	
        }
        
	}
}
