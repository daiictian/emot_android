package com.emot.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.emot.common.TaskCompletedRunnable;
import com.emot.constants.ApplicationConstants;

import android.os.AsyncTask;
import android.util.Log;

public class EmotHTTPClient extends AsyncTask<Void, Void, Object>{

	private static final String TAG = "RegistrationHTTPClient";
	private URL url;
	private ArrayList<NameValuePair> reqContent;
	private TaskCompletedRunnable taskCompletedRunnable;

	public EmotHTTPClient(final URL pUrl){

		this.url = pUrl;
	}

	public EmotHTTPClient(final URL pUrl, final ArrayList<NameValuePair> pRequestContents, final TaskCompletedRunnable pTaskCompletedRunnable){

		this.url = pUrl;
		this.reqContent = pRequestContents;
		this.taskCompletedRunnable = pTaskCompletedRunnable;
	}

	public EmotHTTPClient(final URL pUrl, final ArrayList<NameValuePair> pRequestContents, final String pVerificationSalt,final TaskCompletedRunnable pTaskCompletedRunnable){

		this.url = pUrl;
		this.reqContent = pRequestContents;
		this.taskCompletedRunnable = pTaskCompletedRunnable;
	}

	private String getQuery(final List<NameValuePair> params)
			throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			Log.d(TAG, pair.getName() + " = "+pair.getValue());
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}
		//Log.d(TAG, "Post data: " + result.toString());
		return result.toString();
	}
	
	public URL createGETURL(final String pUrl){
		URL url = null;
		try {
			url = new URL(pUrl);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return url;
	}

	@Override
	protected Object doInBackground(Void... params) {



		InputStream in = null;
		HttpURLConnection urlConnection;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		//reqContent = new ArrayList<NameValuePair>();

		String VERIFICATION_SALT = ApplicationConstants.VERIFICATION_SALT;
		/*reqContent.add(new BasicNameValuePair("request", "register"));
		reqContent.add(new BasicNameValuePair("mobile", "9379475511"));
		reqContent.add(new BasicNameValuePair("app_version", "1"));
		reqContent.add(new BasicNameValuePair("client_os", "android"));
		reqContent.add(new BasicNameValuePair("code", "1234"));
		reqContent.add(new BasicNameValuePair("s", "5678"));
		reqContent.add(new BasicNameValuePair("hash", "20b75e4507a50fddd647945784bcbd96"));*/
		JSONObject result = null;
		String line;
		try {
			//url = new URL("http://192.168.0.104:8000/api/register/");
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.addRequestProperty("Content-type","application/x-www-form-urlencoded");
			System.setProperty("http.keepAlive", "false");
			MessageDigest md = MessageDigest.getInstance("MD5");
			String s = "1|"+"android|" + "1234|" + "9379475511|" + "register|"+"5678|"+VERIFICATION_SALT;
			byte[] messageDigest = md.digest(s.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			System.out.println("hastext is " +hashtext);

			if (reqContent != null) {
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod(ApplicationConstants.HTTP_POST);

				urlConnection.setRequestProperty("Content-Length",
						Integer.toString(getQuery(reqContent).getBytes().length));

				PrintWriter out = new PrintWriter(
						urlConnection.getOutputStream());
				out.print(getQuery(reqContent));
				out.close();
			}
			Log.d(TAG, "connection : " + urlConnection.toString());
			int status = urlConnection.getResponseCode();
			Log.d(TAG, "status =" + status);
			System.out.println("status= " +status);
			if (urlConnection.getErrorStream() != null) {
				in = urlConnection.getErrorStream();
				Log.d(TAG, "Error : " );
			}

			if (status == HttpsURLConnection.HTTP_OK) {
				in = urlConnection.getInputStream();
				String contentEncoding = urlConnection
						.getHeaderField("Content-Encoding");
				Log.d(TAG, "Content encoding : " + contentEncoding);
				System.out.println("Content encoding : " + contentEncoding);
				br = new BufferedReader(new InputStreamReader(in));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				 result = new JSONObject(sb.toString());
				 

				System.out.println("Response String is " +sb);
				if (contentEncoding != null
						&& contentEncoding.equalsIgnoreCase("gzip")) {
					in = new GZIPInputStream(in);
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		} catch(JSONException e){
			e.printStackTrace();
		} catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		}

		return result;
	}

	@Override
	protected void onPostExecute(Object result) {
		
		if(result instanceof JSONObject){
			Log.i("Registration", "in postExecute");
			Log.i("task Val", taskCompletedRunnable.toString());
			taskCompletedRunnable.onTaskComplete(result);
		}
	}

}
