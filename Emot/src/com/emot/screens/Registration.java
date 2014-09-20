package com.emot.screens;

import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.json.JSONException;
import org.json.JSONObject;

import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.ApplicationConstants;
import com.emot.constants.WebServiceConstants;
import com.emot.api.EmotHTTPClient;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class Registration extends Activity {

	private static final String TAG = "Registration";
	private EditText mEnterMobile;
	private Button mSubmitNumber;
	private EditText mEnterVerificationCode;
	private Button mSendVerificationCode;
	private String mMobileNumber;
	private SecureRandom mRandom = new SecureRandom();
	private String mRN;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_register_screen);
		initializeUI();
		setOnClickListeners();
	}
	
	private boolean isNumberValid(final String pNumber){
		boolean isValid = false;
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		try {
			  PhoneNumber numberProto = phoneUtil.parse(pNumber, "IN");
			  isValid = phoneUtil.isValidNumber(numberProto); 
			} catch (NumberParseException e) {
			  System.err.println("NumberParseException was thrown: " + e.toString());
			}
		return isValid;
	}

	private void setOnClickListeners() {

		mSubmitNumber.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				mMobileNumber = mEnterMobile.getText().toString();
				if(isNumberValid(mMobileNumber)){
				String url = WebServiceConstants.HTTP + "://"+ 
						WebServiceConstants.SERVER_IP+":"+WebServiceConstants.SERVER_PORT
						+WebServiceConstants.PATH_API+WebServiceConstants.OP_SETCODE
						+WebServiceConstants.GET_QUERY+WebServiceConstants.DEVICE_TYPE+
						"="+mMobileNumber;
				URL wsURL = null;
				Log.d(TAG, "wsurl is  " +wsURL);
				try {
					wsURL = new URL(url);
				} catch (MalformedURLException e) {

					e.printStackTrace();
				}
				Log.d(TAG, "wsurl is  " +wsURL);
				TaskCompletedRunnable taskCompletedRunnable = new TaskCompletedRunnable() {

					@Override
					public void onTaskComplete(String result) {
						Log.i("Registration", "callback called");
						try {
							JSONObject resultJson = new JSONObject(result);

							Log.i("TAG", "callback called");
							String status = resultJson.getString("status");
							if(status.equals("true")){
								Log.i("Registration", "status us true");
								Toast.makeText(Registration.this, "You have been registered successfully", Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(Registration.this, "Error in Registration", Toast.LENGTH_LONG).show();
								Log.i(TAG, "registration status is " +status);
								Log.d(TAG, "message from server " + resultJson.getString("message"));

							}
						}
						catch (JSONException e) {

							e.printStackTrace();
						}

					}
				};

				EmotHTTPClient registrationHTTPClient = new EmotHTTPClient(wsURL, null, taskCompletedRunnable);
				registrationHTTPClient.execute(new Void[]{});
			}else{
				Toast.makeText(Registration.this, "Mobile Number is invalid", Toast.LENGTH_LONG).show();
			}
			}

		});

		mSendVerificationCode.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String vCode = mEnterVerificationCode.getText().toString();	
				String url = WebServiceConstants.HTTP + "://"+ 
						WebServiceConstants.SERVER_IP+":"+WebServiceConstants.SERVER_PORT
						+WebServiceConstants.PATH_API+WebServiceConstants.OP_REGISTER;

				URL wsURL = null;
				Log.d(TAG, "wsurl is  " +wsURL);
				try {
					wsURL = new URL(url);
				} catch (MalformedURLException e) {

					e.printStackTrace();
				}
				ArrayList<NameValuePair> reqContent = new ArrayList<NameValuePair>();
				mRN = RN();

				String s = "1|"+"android|" +vCode+ "|" + mMobileNumber+ "|" + "register|"+ mRN +"|"+ ApplicationConstants.VERIFICATION_SALT;
				String ht = hText(s);
				reqContent.add(new BasicNameValuePair(WebServiceConstants.WSRegisterParamConstants.REQUEST, "register"));
				reqContent.add(new BasicNameValuePair(WebServiceConstants.WSRegisterParamConstants.MOBILE, mMobileNumber));
				reqContent.add(new BasicNameValuePair(WebServiceConstants.WSRegisterParamConstants.APP_VERSION, "1"));
				reqContent.add(new BasicNameValuePair(WebServiceConstants.WSRegisterParamConstants.CLIENT_OS, "android"));
				reqContent.add(new BasicNameValuePair(WebServiceConstants.WSRegisterParamConstants.VERIFICATION_CODE, vCode));
				reqContent.add(new BasicNameValuePair(WebServiceConstants.WSRegisterParamConstants.S, mRN));
				reqContent.add(new BasicNameValuePair(WebServiceConstants.WSRegisterParamConstants.HASH, ht));

				TaskCompletedRunnable taskCompletedRunnable = new TaskCompletedRunnable() {

					@Override
					public void onTaskComplete(String result) {
							try {
								JSONObject resultJson = new JSONObject(result);
								String status = resultJson.getString("status");
								if(status.equals("success")){
									Thread login = new Thread(new Runnable() {

										@Override
										public void run() {
											XMPPConnection connection;

											int portInt = 5222;

											// Create a connection
											ConnectionConfiguration connConfig = new ConnectionConfiguration("ec2-54-85-148-36.compute-1.amazonaws.com", portInt,"emot-net");
											connConfig.setSASLAuthenticationEnabled(true);
											//connConfig.setCompressionEnabled(true);
											connConfig.setSecurityMode(SecurityMode.enabled);

											if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
												connConfig.setTruststoreType("AndroidCAStore");
												connConfig.setTruststorePassword(null);
												connConfig.setTruststorePath(null);
												Log.i(TAG, "[XmppConnectionTask] Build Icecream");

											} else {
												connConfig.setTruststoreType("BKS");
												String path = System.getProperty("javax.net.ssl.trustStore");
												if (path == null)
													path = System.getProperty("java.home") + File.separator + "etc"
															+ File.separator + "security" + File.separator
															+ "cacerts.bks";
												connConfig.setTruststorePath(path);
												Log.i(TAG, "[XmppConnectionTask] Build less than Icecream ");

											}
											connConfig.setDebuggerEnabled(true);
											XMPPConnection.DEBUG_ENABLED = true;
											connection = new XMPPConnection(connConfig);

											try {
												connection.connect();
												Log.i(TAG, "[SettingsDialog] Connected to " + connection.getHost());
												// publishProgress("Connected to host " + HOST);
											} catch (XMPPException ex) {
												Log.e(TAG, "[SettingsDialog] Failed to connect to " + connection.getHost());
												Log.e(TAG, ex.toString());
												//publishProgress("Failed to connect to " + HOST);
												//xmppClient.setConnection(null);
											}



											try {
												connection.login(mMobileNumber,mRN);
												if(connection.isAuthenticated()){
													Log.i(TAG, "Authenticated : "+connection.isAuthenticated());
													///In a UI thread launch contacts Activity
												}else{

												}


											} catch(Exception ex){

												Log.i(TAG, "loginfails ");
												ex.printStackTrace();
											}


										}
									});
									login.start();
								}
							} catch (JSONException e) {

								e.printStackTrace();
							}
						}

				};
				EmotHTTPClient registrationHTTPClient = new EmotHTTPClient(wsURL, reqContent, taskCompletedRunnable);
				registrationHTTPClient.execute(new Void[]{});



			}
		});
	}

	private String hText(final String input){

		MessageDigest md;
		String hashtext = "";
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			hashtext = number.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			//System.out.println("hastext is " +hashtext);
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}

		return hashtext;	
	}

	private String RN(){
		return new BigInteger(130, mRandom).toString(32);
	}

	private void initializeUI() {
		mEnterMobile = (EditText)findViewById(R.id.enterNumber);
		mSubmitNumber = (Button)findViewById(R.id.submitNumber);
		mEnterVerificationCode = (EditText)findViewById(R.id.verificationCode);
		mSendVerificationCode = (Button)findViewById(R.id.sendVerificationCode);

	}

	@Override
	protected void onResume() {

		super.onResume();
	}




}
