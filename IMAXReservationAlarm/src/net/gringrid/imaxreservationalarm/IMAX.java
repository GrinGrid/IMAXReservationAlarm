package net.gringrid.imaxreservationalarm;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;


public class IMAX extends Activity implements OnClickListener{

	private static final boolean DEBUG = false;
	private String mCgvUrl;
	private int mInterval;
	private static final int NOTIFICATION_ID_MAIN = 7576;

	private final String CURRENT_MOVIE_LIST_URL = "http://m.cgv.co.kr/Movie/MovieList.aspx";
	private final String SCHEDULED_MOVIE_LIST_URL = "http://m.cgv.co.kr/Movie/MovieList.aspx?MovieType=next";
	
	private String[] mImaxNameList = {
			"왕십리"
			,"용산"
			,"상암"
			,"인천"
			,"일산"
			,"광주(터미널)"
			,"대구"
			,"대전"
			,"서면"
			,"수원"
			,"울산삼산"
	};

	private String[] mImaxUrlList = {
			//http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0074&PlayYMD=20130723

			"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0074"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0013"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0014"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0002"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0054"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0090"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0058"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0007"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0005"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0012"
			,"http://m.cgv.co.kr/Theater/Theater.aspx?TheaterCd=0128"

	};

	private String[] mTimeList = {
		 "30분"
		,"1시간"
		,"2시간"
		,"3시간"
		,"4시간"
		,"5시간"
	};

	private int [] mTimeMinuteList = {
			30,60,120,180,240,300
	};



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_imax);

		registEvent();
		
		Spinner id_spinner = (Spinner)findViewById(R.id.id_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mImaxNameList);
		id_spinner.setAdapter(adapter);
		id_spinner.setPrompt("IMAX 극장선택");
		id_spinner.setOnItemSelectedListener( new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mCgvUrl = mImaxUrlList[position];


			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

		Spinner id_spinner_interval = (Spinner)findViewById(R.id.id_spinner_interval);
		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTimeList);
		id_spinner_interval.setAdapter(timeAdapter);
		id_spinner_interval.setOnItemSelectedListener( new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mInterval = mTimeMinuteList[position];

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		id_spinner_interval.setPrompt("조회간격 선택");



	}


	private void registEvent(){
		View view = findViewById(R.id.id_bt_start);
		if ( view != null ){
			view.setOnClickListener(this);
		}

		view = findViewById(R.id.id_bt_stop);
		if ( view != null ){
			view.setOnClickListener(this);
		}
		view = findViewById(R.id.id_bt_current_movie);
		if ( view != null ){
			view.setOnClickListener(this);
		}
		view = findViewById(R.id.id_bt_scheduled_movie);
		if ( view != null ){
			view.setOnClickListener(this);
		}

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.imax, menu);
		return true;
	}



	@Override
	public void onClick(View v) {

		switch ( v.getId() ) {
		case R.id.id_bt_start:
			startAlarm();
			break;
		case R.id.id_bt_stop:
			stopAlarm();
			break;

		case R.id.id_bt_current_movie:
			new LoadMovieList().execute(CURRENT_MOVIE_LIST_URL);
			//loadMovieList( CURRENT_MOVIE_LIST_URL );
			break;

		case R.id.id_bt_scheduled_movie:
			new LoadMovieList().execute(SCHEDULED_MOVIE_LIST_URL);
			//loadMovieList( SCHEDULED_MOVIE_LIST_URL );
			break;

		default:
			break;
		}
	}
	
	
	

	private void startAlarm(){
		NotificationManager notificationManager = null; 
		DatePicker id_datepicker = (DatePicker)findViewById(R.id.datePicker1);
		String day = id_datepicker.getDayOfMonth()<10?"0"+id_datepicker.getDayOfMonth():Integer.toString(id_datepicker.getDayOfMonth());
		String month = id_datepicker.getMonth() + 1<10?"0"+(id_datepicker.getMonth() + 1):Integer.toString(id_datepicker.getMonth() + 1);
		String year = Integer.toString(id_datepicker.getYear());				
		String targetDay = year+month+day;
		String cgvUrl = mCgvUrl+"&PlayYMD="+targetDay; 


		if ( DEBUG ){
			Log.d("jiho", year+"-"+month+"-"+day);
			Log.d("jiho", "URL : "+mCgvUrl);		
			Log.d("jiho", "targetDay : "+targetDay);
			Log.d("jiho", "mInterval : "+mInterval);
		}

		// 알람등록을 위한 데이타 세팅
		AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, SearchAlarmReceiver.class);
		intent.putExtra(SearchAlarmReceiver.ACTION_ALARM, SearchAlarmReceiver.ACTION_ALARM);
		intent.putExtra("targetDay", targetDay);
		intent.putExtra("cgvUrl", cgvUrl);



		PendingIntent pIntent = PendingIntent.getBroadcast(this, 1234567, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// 설정한 시간 간격으로 알람 호출
		int interval = mInterval * 60 * 1000;
		interval = 5000;
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pIntent);	



		Intent resultIntent = new Intent(this, IMAX.class);
		resultIntent.putExtra("IS_FROM_NOTIFICATION", true);
		// 앱 실행하고 다른 메뉴로 이동후 noti 클릭하면 새로 앱을 띄움
		//resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent notifyIntent =
		        PendingIntent.getActivity(
		        this,
		        0,
		        resultIntent,
		        PendingIntent.FLAG_UPDATE_CURRENT
		);


		NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this);
		notiBuilder.setSmallIcon(R.drawable.ic_launcher);
		notiBuilder.setContentTitle("CGV Alarm");
		notiBuilder.setContentText("Application is running ~");
		notiBuilder.setOngoing(true);
		notiBuilder.setContentIntent(notifyIntent);

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID_MAIN, notiBuilder.build());
	}



	private void stopAlarm(){
		NotificationManager notificationManager = null; 
		Log.d("jiho", "stopAlarm");
		Intent intent = new Intent(getApplicationContext(), SearchAlarmReceiver.class);
		intent.putExtra(SearchAlarmReceiver.ACTION_ALARM, SearchAlarmReceiver.ACTION_ALARM);		 
		PendingIntent  pIntent = PendingIntent.getBroadcast(this, 1234567,intent, PendingIntent.FLAG_UPDATE_CURRENT);		 
		AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		alarms.cancel(pIntent);

		// Notification dismiss
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID_MAIN);


	}
     
	private class LoadMovieList extends AsyncTask<String, Void, String>{
		private ProgressDialog progressDialog;
		HttpPost httpPost;
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        String url;
        
        ArrayList<String> mTotalList = new ArrayList<String>();
        HashMap<String, String> moviewTag = new HashMap<String, String>();
        HashMap<String, String> moview = new HashMap<String, String>();
        
		@Override
		protected String doInBackground(String... params) {
			        
	        try {        	
	        	url = params[0];
	        	httpPost = new HttpPost(url);
	            response = client.execute(httpPost);
	            
	            HttpEntity entity = response.getEntity();            
	            InputStream stream = entity.getContent();
	            
	            //BufferedReader br = new BufferedReader(new InputStreamReader(stream, "EUC-KR"));
	            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	            
	            String line = null;
	            
	            //Reader reader=new InputStreamReader(stream);
	            while ( (line = br.readLine()) != null ){            	
	            	mTotalList.add(line);            	
	            }
	            
	            int totalSize = mTotalList.size();
	            for ( int i=0; i<totalSize; i++ ){
	            	if ( mTotalList.get(i).trim().indexOf( "<p class=\"subject\">" ) != -1 ){
	            		moviewTag.put(mTotalList.get(i+1), mTotalList.get(i+2));
	            	}
	            }
	            
	            String idRegex = "([0-9]*)";
	            Pattern idPattern = Pattern.compile(idRegex);
	    		Matcher idMatcher = null;
	    		
	    		String nameRegex = "[^>]*>?([^<]*)";
	            Pattern namePattern = Pattern.compile(nameRegex);
	    		Matcher nameMatcher = null;
	    		
	    		String moviewID = null;
	    		String moviewName = null;
	    		
	    		for (Map.Entry<String,String> entry : moviewTag.entrySet()) {
	    			
	    			idMatcher = idPattern.matcher(entry.getKey());	    			    			
	    			
	    			while( idMatcher.find() ){
	    				if ( idMatcher.group(1).trim().length() > 0 ){
	    					moviewID = idMatcher.group(1).trim();
	    				}
	    			}    			
	    			
	    			nameMatcher = namePattern.matcher(entry.getValue());
	    			    			
	    			while( nameMatcher.find() ){
	    				if ( nameMatcher.group(1).trim().length() > 0 ){
	    					moviewName = nameMatcher.group(1).trim();
	    				}
	    			}	    			
	    			moview.put(moviewID, moviewName);
	            }
	        }catch (Exception e){
	        	
	        }	
			
			return null;
		}
		
		
		@Override
		protected void onPreExecute() {
			progressDialog= ProgressDialog.show(IMAX.this, null,"Loading movie list...", true);

			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(String result) {
			int spinnerId = 0;
    		String spinnerTitle = null;
    		
    		if ( url.equals( CURRENT_MOVIE_LIST_URL )){
    			spinnerId = R.id.id_spinner_current_movie;
    			spinnerTitle = "현재상영작";
    		}else if ( url.equals( SCHEDULED_MOVIE_LIST_URL )){
    			spinnerId = R.id.id_spinner_scheduled_movie;
    			spinnerTitle = "상영예정작";
    		}
    		
    		ArrayList<String> movieName = new ArrayList<String>();
    		
    		for (Map.Entry<String,String> entry : moview.entrySet()) {
    			
    			Log.d("jiho", "[ "+entry.getKey()+" ] : "+entry.getValue());
    			movieName.add(entry.getValue());	
    		}
    		
    		Spinner id_spinner = (Spinner)findViewById( spinnerId );
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(IMAX.this, android.R.layout.simple_spinner_item, movieName);
    		id_spinner.setAdapter(adapter);
    		id_spinner.setPrompt( spinnerTitle );

    		super.onPostExecute(result);
			progressDialog.dismiss();
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
	}

}