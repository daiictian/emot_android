package com.emot.screens;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.WebServiceConstants;
import com.emot.registration.RegistrationHTTPClient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class Registration extends Activity {
	
	private static final String TAG = "Registration";
	private EditText mEnterMobile;
	private Button mSubmitNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_register_screen);
		initializeUI();
		setOnClickListeners();
	}

	private void setOnClickListeners() {
		
		mSubmitNumber.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String mobileNumber = mEnterMobile.getText().toString();
				String url = WebServiceConstants.HTTP + "://"+ 
							 WebServiceConstants.SERVER_IP+":"+WebServiceConstants.SERVER_PORT
							+WebServiceConstants.PATH_API+WebServiceConstants.OP_SETCODE
							+WebServiceConstants.GET_QUERY+WebServiceConstants.DEVICE_TYPE+
							"="+mobileNumber;
				URL wsURL = null;
				Log.d(TAG, "wsurl is  " +wsURL);
				try {
					wsURL = new URL(url);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.d(TAG, "wsurl is  " +wsURL);
				TaskCompletedRunnable taskCompletedRunnable = new TaskCompletedRunnable() {
					
					@Override
					public void onTaskComplete(Object result) {
						Log.i("Registration", "callback called");
						if(result  instanceof JSONObject){
							
							try {
								Log.i("TAG", "callback called");
								String status = ((JSONObject) result).getString("status");
								if(status.equals("true")){
									Log.i("Registration", "status us true");
								Toast.makeText(Registration.this, "You have been registered successfully", Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(Registration.this, "Error in Registration", Toast.LENGTH_LONG).show();
								Log.i(TAG, "registration status is " +status);
								Log.d(TAG, "message from server " + ((JSONObject) result).getString("message"));
								
							}
								}
								catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						
					}
				};

				RegistrationHTTPClient registrationHTTPClient = new RegistrationHTTPClient(wsURL, null, taskCompletedRunnable);
				registrationHTTPClient.execute(new Void[]{});
			}
			
		});
	}

	private void initializeUI() {
		mEnterMobile = (EditText)findViewById(R.id.enterNumber);
		mSubmitNumber = (Button)findViewById(R.id.submitNumber);
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	
	

}
