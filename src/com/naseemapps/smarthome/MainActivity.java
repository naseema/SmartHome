package com.naseemapps.smarthome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener{
	public static final int RES_OK = 200;
	public static final int RES_ERR = 999;
	public static final int RES_ERR_DEV_OFFLINE = 901;
	public static final String RESPONSE_DEVICE_OFFLINE_BODY = ":(";
	public static final String RESPONSE_DEVICE_ONLINE_BODY = ":)";
	public static final String APP_TAG = "SMART_HOME";
	
	public static final String URL_DEVICE_AGENT = "https://agent.electricimp.com/I8Kh0YqqYUy8";
	
	//
	public static final int STATE_ON = 0;
	public static final int STATE_OFF = 1;
	
	public int devState [] = {-1,-1,-1,-1,-1,-1};
	ToggleButton devTg1,devTg2,devTg3,devTg4,devTg5,devTg6;
	
	
	Context context;
	
	ProgressDialog connectingDialog ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//		StrictMode.setThreadPolicy(policy); 
		
//		tg1 = (ToggleButton) findViewById(R.id.ToggleButton1);
//		tg2 = (ToggleButton) findViewById(R.id.toggleButton2);
		
//		new AlertDialog.Builder(this)
//	    .setTitle("Refreshing Status...")
//	    .setMessage("Refreshing Status...")
//	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//	        public void onClick(DialogInterface dialog, int which) { 
//	            // continue with delete
//	        }
//	     })
//	    .setNegativeButton("No", new DialogInterface.OnClickListener() {
//	        public void onClick(DialogInterface dialog, int which) { 
//	            // do nothing
//	        }
//	     })
//	     .show();
		
		Button b = (Button) findViewById(R.id.refresh_b);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        new SwitchDevice().execute(STATE_ON, -1, null);
				connectingDialog.show();
			}
		});
		
		connectingDialog = new ProgressDialog(context);//.show(context, "Connecting", "Please wait...", true);
		connectingDialog.setTitle("Connecting...");
		connectingDialog.show();
		//		connectingDialog.show();
//		connectingDialog.hide();
		
		devTg1 = (ToggleButton) findViewById(R.id.dev1_tb);
		devTg2 = (ToggleButton) findViewById(R.id.dev2_tb);
		devTg3 = (ToggleButton) findViewById(R.id.dev3_tb);
		devTg4 = (ToggleButton) findViewById(R.id.dev4_tb);
		devTg5 = (ToggleButton) findViewById(R.id.dev5_tb);
		devTg6 = (ToggleButton) findViewById(R.id.dev6_tb);
		
		devTg1.setOnClickListener(this);
		devTg2.setOnClickListener(this);
		devTg3.setOnClickListener(this);
		devTg4.setOnClickListener(this);
		devTg5.setOnClickListener(this);
		devTg6.setOnClickListener(this);
		
        new SwitchDevice().execute(STATE_ON, -1, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public int postData(int value, int index) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost ;
	    if (index == -1)
	    	httppost = new HttpPost(URL_DEVICE_AGENT + "?status=0");
	    else 
	    	httppost = new HttpPost(URL_DEVICE_AGENT + "?dev" + index +"=" + value);

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("dev"+index, value +""));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        HttpEntity en = response.getEntity();
	        String resBody = "";
	        if (en != null) {
	        	resBody = EntityUtils.toString(en);
	        	Log.i(APP_TAG,"index="+ index + " - " + resBody);
	        	if (index == -2 && (resBody == null || resBody.equals(RESPONSE_DEVICE_OFFLINE_BODY))) {
//	        		Log.e(APP_TAG,"OFFLINE->" + )
	        		return RES_ERR_DEV_OFFLINE;
	        	}
	        }
		    if (index == -1) {
		    	String [] resArr = resBody.split(",");
		    	if (resArr.length != 6) {
		    		Log.e(APP_TAG, "Bad lenght...!!!");
		    		throw new Exception("Bad lenght");
		    	}
		    	for (int i = 0; i < resArr.length; i++){
		    		devState[i] = Integer.parseInt(resArr[i]);
		    	}
		    	return RES_OK;
		    }


		    if (resBody != null && resBody.equals(RESPONSE_DEVICE_ONLINE_BODY)) 
		    	return RES_OK;
		    else
		    	return RES_ERR;
	        
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
		  ToggleButton mTg = null;
		    @Override
		protected void onPreExecute() {
			super.onPreExecute();
//	    	connectingDialog.show();

		}

			@Override
		    protected Integer doInBackground(Object... param) {
				mIndex = (Integer) param[1];
				mTg = (ToggleButton) param[2];
				return postData((Integer)param[0], mIndex);
		    }

		@Override
		protected void onPostExecute(Integer result) {
			Log.i(APP_TAG,"switchDevice::Result->index=" + mIndex + ", result=" + result);
			connectingDialog.hide();
			
			if (mIndex != -2) {
		        new SwitchDevice().execute(STATE_ON, -2, null);
			}
			
		    switch (mIndex) {
		    case -2:
		    	if (result == RES_ERR_DEV_OFFLINE) {
		    		Toast.makeText(context, "Check Your Imp connectivity!!", Toast.LENGTH_SHORT).show();
//		    		if (mTg != null) {
//		    			mTg.setEnabled(false);
//		    		} else {
		    			devTg2.setEnabled(false);
		    			devTg3.setEnabled(false);
//		    		}
		    	} else if (result == RES_ERR) {
		    		devTg2.setEnabled(false);
	    			devTg3.setEnabled(false);
		    		Toast.makeText(context, "ERROR!!", Toast.LENGTH_SHORT).show();
		    		if (mTg != null) {
		    			mTg.setChecked(!devTg3.getText().equals("ON"));
		    		}
		    	} 
//		    	else {
					devTg2.setEnabled(true);
					devTg3.setEnabled(true);
//		    	}
		    	break;
		    case -1:
		    	break;
		    case 1: 
		    case 2:
		    case 3:
		    default:
		    }

//			devTg2.setChecked(devState[1] == STATE_ON);
//			devTg3.setChecked(devState[2] == STATE_ON);


		}
		  }


	@Override
	public void onClick(View v) {
		if (!isNetworkConnected()) {
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
			return;
		}
		if (v.equals(devTg1)) {
			Toast.makeText(context, "light1 " + "not connected yet!", Toast.LENGTH_SHORT).show();
		} else if (v.equals(devTg2)) {
			setEnable(2, false);
            new SwitchDevice().execute(devTg2.getText().equals("ON") ? STATE_ON : STATE_OFF, 2, devTg2);
//			Toast.makeText(context, "light2 " + "not connected yet!", Toast.LENGTH_SHORT).show();
		} else if (v.equals(devTg3)) {
			setEnable(3, false);
            new SwitchDevice().execute(devTg3.getText().equals("ON") ? STATE_ON : STATE_OFF, 3, devTg3);
        } else if (v.equals(devTg4)) {
			Toast.makeText(context, "light4 " + "not connected yet!", Toast.LENGTH_SHORT).show();
		} else if (v.equals(devTg5)) {
			Toast.makeText(context, "light5 " + "not connected yet!", Toast.LENGTH_SHORT).show();
		} else if (v.equals(devTg6)) {
			Toast.makeText(context, "light6 " + "not connected yet!", Toast.LENGTH_SHORT).show();
		} 
	}

	private void setEnable(int index, boolean state) {
		switch (index) {
		case 1:
//			onBtn1.setEnabled(state);
//			offBtn1.setEnabled(state);
			break;
		case 2:
			devTg2.setEnabled(state);
//			onBtn2.setEnabled(state);
//			offBtn2.setEnabled(state);
			break;
		case 3:
			devTg3.setEnabled(state);
//			onBtn3.setEnabled(state);
//			offBtn3.setEnabled(state);
			break;
		case 4:
//			onBtn4.setEnabled(state);
//			offBtn4.setEnabled(state);
			break;
		case 5:
//			onBtn5.setEnabled(state);
//			offBtn5.setEnabled(state);
			break;
		case 6:
//			onBtn6.setEnabled(state);
//			offBtn6.setEnabled(state);
			break;

		default:
			break;
		}
	}
	
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if (ni == null) {
		   // There are no active networks.
		   return false;
		  } else
		   return true;
		 }
	
	
	
	
	public static String _getResponseBody(final HttpEntity entity) throws IOException, ParseException {
		 
		if (entity == null) { throw new IllegalArgumentException("HTTP entity may not be null"); }
		 
		InputStream instream = entity.getContent();
		 
		if (instream == null) { return ""; }
		 
		if (entity.getContentLength() > Integer.MAX_VALUE) { throw new IllegalArgumentException(
		 
		"HTTP entity too large to be buffered in memory"); }
		 
		String charset = getContentCharSet(entity);
		 
		if (charset == null) {
		 
		charset = HTTP.DEFAULT_CONTENT_CHARSET;
		 
		}
		 
		Reader reader = new InputStreamReader(instream, charset);
		 
		StringBuilder buffer = new StringBuilder();
		 
		try {
		 
		char[] tmp = new char[1024];
		 
		int l;
		 
		while ((l = reader.read(tmp)) != -1) {
		 
		buffer.append(tmp, 0, l);
		 
		}
		 
		} finally {
		 
		reader.close();
		 
		}
		 
		return buffer.toString();
		 
		}
		
	public static String getResponseBody(HttpResponse response) {
		 
		String response_text = null;
		 
		HttpEntity entity = null;
		 
		try {
		 
		entity = response.getEntity();
		 
		response_text = _getResponseBody(entity);
		 
		} catch (ParseException e) {
		 
		e.printStackTrace();
		 
		} catch (IOException e) {
		 
		if (entity != null) {
		 
		try {
		 
		entity.consumeContent();
		 
		} catch (IOException e1) {
		 
		}
		 
		}
		 
		}
		 
		return response_text;
		 
		}
	
		public static String getContentCharSet(final HttpEntity entity) throws ParseException {
		 
		if (entity == null) { throw new IllegalArgumentException("HTTP entity may not be null"); }
		 
		String charset = null;
		 
		if (entity.getContentType() != null) {
		 
		HeaderElement values[] = entity.getContentType().getElements();
		 
		if (values.length > 0) {
		 
		NameValuePair param = values[0].getParameterByName("charset");
		 
		if (param != null) {
		 
		charset = param.getValue();
		 
		}
		 
		}
		 
		}
		 
		return charset;
		 
		}
		
		
		public String getHTML(String urlToRead) {
		      URL url;
		      HttpURLConnection conn;
		      BufferedReader rd;
		      String line;
		      String result = "";
		      try {
		         url = new URL(urlToRead);
		         conn = (HttpURLConnection) url.openConnection();
		         conn.setRequestMethod("GET");
		         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		         while ((line = rd.readLine()) != null) {
		            result += line;
		         }
		         rd.close();
		      } catch (Exception e) {
		         e.printStackTrace();
		      }
		      return result;
		   }
	
}
