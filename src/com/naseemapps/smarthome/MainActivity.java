package com.naseemapps.smarthome;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener {
	public static final int RES_OK = 200;
	public static final int RES_ERR = 999;
	public static final int RES_ERR_DEV_OFFLINE = 901;
	public static final String RESPONSE_DEVICE_OFFLINE_BODY = ":(";
	public static final String RESPONSE_DEVICE_ONLINE_BODY = ":)";
	public static final String APP_TAG = "SMART_HOME";
	
	public static String USER_ID = "nas!K2310Gf";
	public static  final String PREF_USER_ID = "pref_user_id";

	public static final String URL_DEVICE_AGENT = "https://agent.electricimp.com/I8Kh0YqqYUy8";

	//
	public static final int STATE_ON = 0;
	public static final int STATE_OFF = 1;

	private int devState[] = { -1, -1, -1, -1, -1, -1 };
	private ToggleButton[] devStateTB = new ToggleButton[6];
	private boolean tryToRefresh = false;
	private int waitingRequest = 0;
	private Button refreshB;
	// ToggleButton devTg1,devTg2,devTg3,devTg4,devTg5,devTg6;

	Context context;

	ProgressDialog connectingDialog;

	ProgressBar mProgressBar;
	SharedPreferences mSharedPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		context = this;
  	  	
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		USER_ID = mSharedPrefs.getString(PREF_USER_ID, USER_ID);

		mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);

		refreshB = (Button) findViewById(R.id.refresh_b);
		refreshB.setOnClickListener(this);

		connectingDialog = new ProgressDialog(context);
		connectingDialog.setTitle("Connecting...");

		devStateTB[0] = (ToggleButton) findViewById(R.id.dev1_tb);
		devStateTB[1] = (ToggleButton) findViewById(R.id.dev2_tb);
		devStateTB[2] = (ToggleButton) findViewById(R.id.dev3_tb);
		devStateTB[3] = (ToggleButton) findViewById(R.id.dev4_tb);
		devStateTB[4] = (ToggleButton) findViewById(R.id.dev5_tb);
		devStateTB[5] = (ToggleButton) findViewById(R.id.dev6_tb);

		devStateTB[0].setOnClickListener(this);
		devStateTB[1].setOnClickListener(this);
		devStateTB[2].setOnClickListener(this);
		devStateTB[3].setOnClickListener(this);
		devStateTB[4].setOnClickListener(this);
		devStateTB[5].setOnClickListener(this);
		

	}

	@Override
	protected void onResume() {
		super.onResume();

		syncStatus();
	}



	public int postData(int value, int index) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost;
		if (index == -1)
			httppost = new HttpPost(URL_DEVICE_AGENT + "?status=0" + "&uid=" + USER_ID);
		else
			httppost = new HttpPost(URL_DEVICE_AGENT + "?dev" + index + "="
					+ value + "&uid=" + USER_ID);

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs
					.add(new BasicNameValuePair("dev" + index, value + ""));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity en = response.getEntity();
			String resBody = "";
			if (en != null) {
				resBody = EntityUtils.toString(en);
				Log.i(APP_TAG, "index=" + index + " - " + resBody);
				if (index == -2
						&& (resBody == null || resBody
								.equals(RESPONSE_DEVICE_OFFLINE_BODY))) {
					// Log.e(APP_TAG,"OFFLINE->" + )
					return RES_ERR_DEV_OFFLINE;
				}
			}

			String[] resArr = resBody.split(",");
			if (resArr.length != 6) {
				Log.e(APP_TAG, "Bad lenght...!!!");
				throw new Exception("Bad lenght");
			}
			for (int i = 0; i < resArr.length; i++) {
				devState[i] = Integer.parseInt(resArr[i]);
			}

			return RES_OK;

		} catch (ClientProtocolException e) {
			Log.e(APP_TAG, "error postData: " + e);
			return RES_ERR;
		} catch (IOException e) {
			Log.e(APP_TAG, "error postData: " + e);
			return RES_ERR;
		} catch (Exception e) {
			Log.e(APP_TAG, "Exception: " + e);
			return RES_ERR;
		}
	}

	private class SwitchDevice extends AsyncTask<Object, Void, Integer> {
		int mIndex;

		public SwitchDevice(int index) {
			waitingRequest++;
			mProgressBar.setMax(waitingRequest);
//			mProgressBar.setProgress(progress);
			mProgressBar.setVisibility(View.VISIBLE);
			refreshB.setEnabled(false);
			setProgressBarIndeterminateVisibility(true);
			refreshB.setText("Connecting...");
			if (index != -1)
				devStateTB[index].setEnabled(false);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// connectingDialog.show();

		}

		@Override
		protected Integer doInBackground(Object... param) {
			mIndex = (Integer) param[1];
			return postData((Integer) param[0], mIndex);
		}

		@Override
		protected void onPostExecute(Integer result) {
			Log.i(APP_TAG, "switchDevice::Result->index=" + mIndex+ ", result=" + result);

			
			
			waitingRequest--;
			mProgressBar.setProgress(mProgressBar.getMax() - waitingRequest);
//			ObjectAnimator animation = ObjectAnimator.ofInt(mProgressBar, "progress", mProgressBar.getProgress(),mProgressBar.getMax() - waitingRequest*10); 
//		    animation.setDuration(500); // 0.5 second
//		    animation.setInterpolator(new DecelerateInterpolator());
//		    animation.start();
			
			
			if (waitingRequest == 0) {

				if (mIndex != -1 || tryToRefresh == true) {
					tryToRefresh = false;
					new SwitchDevice(-1).execute(STATE_ON, -1, null);
					return;
				}
//				connectingDialog.hide();
				setProgressBarIndeterminateVisibility(false);
				mProgressBar.setProgress(0);
				mProgressBar.setVisibility(View.GONE);

				refreshButtonStatus();
				refreshB.setEnabled(true);
				refreshB.setText("Manual refresh");

				if (result == RES_ERR) {
					Toast.makeText(context, "Connection err, see logs!!",
							Toast.LENGTH_LONG).show();
					setEnableAvaliableButtons(false);
					return;
				}
				
				if (devState[0] == -1) {
					Toast.makeText(context, "Your Imp offline!!",
							Toast.LENGTH_LONG).show();
					setEnableAvaliableButtons(false);
				} else {
					setEnableAvaliableButtons(true);
				}
			}

//			if (mIndex != -1)
//				devStateTB[mIndex - 1].setEnabled(true);

		}
	}

	@Override
	public void onClick(View v) {
		if (!isNetworkConnected()) {
			Toast.makeText(context, "No internet connection",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (v.equals(devStateTB[0])) {
			Toast.makeText(context, "light1 " + "not connected yet!",
					Toast.LENGTH_SHORT).show();
		} else if (v.equals(devStateTB[1])) {
			devState[1] = -1;
			new SwitchDevice(1)
					.execute(devStateTB[1].getText().equals("ON") ? STATE_ON
							: STATE_OFF, 2);
		} else if (v.equals(devStateTB[2])) {
			devState[2] = -1;
			new SwitchDevice(2)
					.execute(devStateTB[2].getText().equals("ON") ? STATE_ON
							: STATE_OFF, 3);
		} else if (v.equals(devStateTB[3])) {
			Toast.makeText(context, "light4 " + "not connected yet!",
					Toast.LENGTH_SHORT).show();
		} else if (v.equals(devStateTB[4])) {
			new SwitchDevice(4)
			.execute(devStateTB[4].getText().equals("ON") ? STATE_ON
					: STATE_OFF, 5);
//			Toast.makeText(context, "light5 " + "not connected yet!",
//					Toast.LENGTH_SHORT).show();
		} else if (v.equals(devStateTB[5])) {
			new SwitchDevice(5)
					.execute(devStateTB[5].getText().equals("ON") ? STATE_ON
							: STATE_OFF, 6);
//			Toast.makeText(context, "light6 " + "not connected yet!",
//					Toast.LENGTH_SHORT).show();
		} else if (v.equals(refreshB)) {
			syncStatus();
		}
	}

	// private void setEnable(int index, boolean state) {
	// switch (index) {
	// case 1:
	// // onBtn1.setEnabled(state);
	// // offBtn1.setEnabled(state);
	// break;
	// case 2:
	// devTg2.setEnabled(state);
	// // onBtn2.setEnabled(state);
	// // offBtn2.setEnabled(state);
	// break;
	// case 3:
	// devTg3.setEnabled(state);
	// // onBtn3.setEnabled(state);
	// // offBtn3.setEnabled(state);
	// break;
	// case 4:
	// // onBtn4.setEnabled(state);
	// // offBtn4.setEnabled(state);
	// break;
	// case 5:
	// // onBtn5.setEnabled(state);
	// // offBtn5.setEnabled(state);
	// break;
	// case 6:
	// // onBtn6.setEnabled(state);
	// // offBtn6.setEnabled(state);
	// break;
	//
	// default:
	// break;
	// }
	// }
	//

	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	private void refreshButtonStatus() {
		for (int i = 0; i < devState.length; i++) {
			if (devState[i] == -1) {
				devStateTB[i].setEnabled(false);
			} else {
				devStateTB[i].setChecked(devState[i] == 0);

			}
		}
	}

	private void setEnableAvaliableButtons(boolean stat) {
		for (int i = 0; i < devState.length; i++) {
			devStateTB[i].setEnabled(stat);
		}
		if (stat) {
			refreshB.setVisibility(View.GONE);
		} else {
			refreshB.setVisibility(View.VISIBLE);
			refreshB.setText("Manual refresh");
		}
	}

	private void syncStatus() {
//		connectingDialog.show();
		setProgressBarIndeterminateVisibility(true);
		tryToRefresh = true;
		new SwitchDevice(-1).execute(STATE_ON, -1);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		 return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId())
	    {
	    case R.id.edit_your_id:
	    	LayoutInflater li = LayoutInflater.from(context);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);

			final EditText userInput = new EditText(context);
	    	  
	    	  ;
	    	  
			userInput.setText(mSharedPrefs.getString(PREF_USER_ID, USER_ID));
			
			// set prompts.xml to alertdialog builder
			alertDialogBuilder.setView(userInput);


			
	    	alertDialogBuilder.setTitle("Changing user id")
//	    	.setMessage("your current user id: " + USER_ID)
			.setCancelable(false)
			.setPositiveButton("Save",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
			    	Editor editor = mSharedPrefs.edit();
			    	USER_ID = userInput.getText() + "";
			    	editor.putString(PREF_USER_ID, USER_ID);
			    	editor.commit();
			    }
			  })
			.setNegativeButton("Cancel",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			    }
			  });

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	        break;
	    }
	    return true;
	}

}
