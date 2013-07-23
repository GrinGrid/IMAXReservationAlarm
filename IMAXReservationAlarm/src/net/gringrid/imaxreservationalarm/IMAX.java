package net.gringrid.imaxreservationalarm;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.widget.DatePicker;
import android.widget.Spinner;


public class IMAX extends Activity implements OnClickListener{

	private static final boolean DEBUG = false;
	private String mCgvUrl;
	private int mInterval;
	private static final int NOTIFICATION_ID_MAIN = 7576;

	private String[] mImaxNameList = {
			"신도림"
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

		View view = findViewById(R.id.id_bt_start);
		if ( view != null ){
			view.setOnClickListener(this);
		}

		view = findViewById(R.id.id_bt_stop);
		if ( view != null ){
			view.setOnClickListener(this);
		}

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
     

}