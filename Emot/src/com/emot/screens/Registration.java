package com.emot.screens;

import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.emot.androidclient.util.PreferenceConstants;
import com.emot.api.EmotHTTPClient;
import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.ApplicationConstants;
import com.emot.constants.PreferenceKeys;
import com.emot.constants.WebServiceConstants;
import com.emot.model.EmotApplication;
import com.emot.persistence.ContactUpdater;
import com.emot.persistence.EmoticonDBHelper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;



public class Registration extends ActionBarActivity {

	private static final String TAG = Registration.class.getSimpleName();
	private EditText mEnterMobile;
	private Spinner mCountryList;
	private Button mSubmitNumber;
	private EditText mEnterVerificationCode;
	private Button mSendVerificationCode;
	private String mMobileNumber;
	private SecureRandom mRandom = new SecureRandom();
	private String mRN;
	private ProgressDialog pd;
	private View viewMobileBlock;
	private View viewVerificationBlock;
	private AutoCompleteTextView mCountrySelector;
	private static Map<String, String> mCountryCode = new HashMap<String, String>();
	private static Map<String, Integer> mCountryCallingCodeMap = new HashMap<String, Integer>();
	private static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	static{
		String[] locales = Locale.getISOCountries();
		for (String countryCode : locales) {

			Locale obj = new Locale("", countryCode);
			mCountryCode.put(obj.getDisplayCountry(), obj.getCountry());
			mCountryCallingCodeMap.put(obj.getCountry(), phoneUtil.getCountryCodeForRegion(obj.getCountry()));
			


		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if(EmotApplication.getValue(PreferenceKeys.USER_APPID, null)!=null){
			startActivity(new Intent(this, ContactScreen.class));
			finish();
		}
		setContentView(R.layout.layout_register_screen);
		initializeUI();
		new EmoticonDBHelper(EmotApplication.getAppContext()).createDatabase();
		suggestCountryOnEntry();
		setOnClickListeners();
//		new EmoticonDBHelper(EmotApplication.getAppContext()).createDatabase();
//		EmoticonDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase().execSQL(EmoticonDBHelper.SQL_CREATE_TABLE_EMOT);
//		EmoticonDBHelper.getInstance(EmotApplication.getAppContext()).getWritableDatabase().execSQL("insert into emots select * from emoticons");
	}



	private boolean isNumberValid(final String pNumber){
		boolean isValid = false;
		//PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
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
				pd.show();
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
							pd.hide();
							viewMobileBlock.setVisibility(View.GONE);
							viewVerificationBlock.setVisibility(View.VISIBLE);
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
							}catch(Exception e){
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
				pd.show();
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
							Log.i(TAG, result);
							JSONObject resultJson = new JSONObject(result);
							String status = resultJson.getString("status");
							if(status.equals("success")){
								EmotApplication.setValue(PreferenceKeys.USER_APPID, resultJson.getString("appid"));
								EmotApplication.setValue(PreferenceKeys.USER_MOBILE, mMobileNumber);
								EmotApplication.setValue(PreferenceKeys.USER_PWD, mRN);
								EmotApplication.setValue(PreferenceConstants.JID, mMobileNumber+"@"+WebServiceConstants.CHAT_DOMAIN);
								EmotApplication.setValue(PreferenceConstants.PASSWORD, mRN);
								EmotApplication.setValue(PreferenceConstants.CUSTOM_SERVER, WebServiceConstants.CHAT_SERVER);
								EmotApplication.setValue(PreferenceConstants.RESSOURCE, WebServiceConstants.CHAT_DOMAIN);
								Editor e = EmotApplication.getPrefs().edit();
								e.putBoolean(PreferenceConstants.REQUIRE_SSL, false);
								e.commit();
								//EmotApplication.setValue(PreferenceConstants.RESSOURCE, WebServiceConstants.CHAT_DOMAIN);
								Thread login = new Thread(new Runnable() {

									@Override
									public void run() {
										XMPPConnection connection;

										// Create a connection
										ConnectionConfiguration connConfig = new ConnectionConfiguration(WebServiceConstants.CHAT_SERVER, WebServiceConstants.CHAT_PORT, WebServiceConstants.CHAT_DOMAIN);
										//connConfig.setSASLAuthenticationEnabled(true);
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
											connection.login(EmotApplication.getValue(PreferenceKeys.USER_MOBILE, ""),EmotApplication.getValue(PreferenceKeys.USER_PWD, ""));
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
								//login.start();
								ContactUpdater.updateContacts(new TaskCompletedRunnable() {

									@Override
									public void onTaskComplete(String result) {
										//Contacts updated in SQLite. You might want to update UI
										pd.cancel();
										startActivity(new Intent(EmotApplication.getAppContext(), ContactScreen.class));
										finish();
									}
								});
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
		
		
		
		
		
	
		
		mCountrySelector.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Log.i(TAG,"ssssssssssssss" + mCountrySelector.getText().toString() );
				Log.i(TAG,"ssssssssssssss" +mCountryCallingCodeMap.get(mCountryCode.get(mCountrySelector.getText().toString())) );
				Log.i(TAG, "ssss " +String.valueOf(mCountryCallingCodeMap.get(mCountryCode.get(mCountrySelector.getText().toString())) ));
				mEnterMobile.setText("+"+String.valueOf(mCountryCallingCodeMap.get(mCountryCode.get(mCountrySelector.getText().toString()))) +"-");
				
				
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

//	private void addItemsOnCountrySpinner() {
//
//
//		List<String> list = new ArrayList<String>();
//		for(String key : mCountryCode.keySet()){
//			list.add(key);
//		}
//		Collections.sort(list);
//
//		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
//				android.R.layout.simple_spinner_item, list);
//		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		mCountryList.setAdapter(dataAdapter);
//	}
	
	private void suggestCountryOnEntry(){
		
		List<String> list = new ArrayList<String>();
		for(String key : mCountryCode.keySet()){
			list.add(key);
		}
		Collections.sort(list);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>  
        (this,android.R.layout.select_dialog_item,list);
		mCountrySelector.setThreshold(1);//will start working from first character  
		mCountrySelector.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView  
	}

	private void initializeUI() {
		mEnterMobile = (EditText)findViewById(R.id.enterNumber);
		mCountrySelector = (AutoCompleteTextView)findViewById(R.id.countryselector);
		mSubmitNumber = (Button)findViewById(R.id.submitNumber);
		mEnterVerificationCode = (EditText)findViewById(R.id.verificationCode);
		mSendVerificationCode = (Button)findViewById(R.id.sendVerificationCode);
		
		pd = new ProgressDialog(Registration.this);
		pd.setMessage("Loading");
		viewMobileBlock = findViewById(R.id.viewRegisterMobileBlock);
		viewVerificationBlock = findViewById(R.id.viewRegisterVerificationBlock);
	}

	@Override
	protected void onResume() {

		super.onResume();
	}




}
