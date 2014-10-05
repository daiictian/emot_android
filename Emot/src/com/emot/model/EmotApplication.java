package com.emot.model;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.emot.constants.PreferenceKeys;
import com.emot.emotobjects.ConnectionQueue;

public class EmotApplication extends Application {

	private static final String TAG = EmotApplication.class.getSimpleName();
	private static Context context;
	private static SharedPreferences prefs;
	private static XMPPConnection connection;
	private static boolean connecting = false;

	public void onCreate() {
		super.onCreate();

		EmotApplication.context = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(context
				.getApplicationContext());

	}

	public static XMPPConnection getConnection(){
		if (connection==null || !connection.isAuthenticated()){
			if(!connecting){
				startConnection();
			}
		}

		return connection;
	}

	public static Context getAppContext() {
		return EmotApplication.context;
	}

	public static boolean setValue(String k, String v) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(k, v);
		editor.commit();
		return true;
	}

	public static String getValue(String k, String d) {
		return prefs.getString(k, d);
	}

	public static String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static void startConnection(){
		connecting = true;
		Thread connThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while(connection == null || connection.getHost() == null){
					Log.i(TAG, "----------STARTING LOGIN---------");

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
						Log.i("XMPPClient", "[XmppConnectionTask] Build Icecream");

					} else {
						connConfig.setTruststoreType("BKS");
						String path = System.getProperty("javax.net.ssl.trustStore");
						if (path == null)
							path = System.getProperty("java.home") + File.separator + "etc"
									+ File.separator + "security" + File.separator
									+ "cacerts.bks";
						connConfig.setTruststorePath(path);
						Log.i("XMPPClient", "[XmppConnectionTask] Build less than Icecream ");

					}
					connConfig.setDebuggerEnabled(true);
					XMPPConnection.DEBUG_ENABLED = true;
					connection = new XMPPConnection(connConfig);


					try {
						connection.connect();
						Log.i("XMPPClient", "[SettingsDialog] Connected to " + connection.getHost());
						// publishProgress("Connected to host " + HOST);
					} catch (XMPPException ex) {
						Log.e("XMPPClient", "[SettingsDialog] Failed to connect to " + connection.getHost());
						Log.e("XMPPClient", ex.toString());
						//publishProgress("Failed to connect to " + HOST);
						//xmppClient.setConnection(null);
					}
					Log.i("XMPPClient", "connection authenticated " +connection.isAuthenticated());

				}


				try {
					connection.login("test5","1234");
					Log.i("XMPPClient", "Logged in as " + connection.getUser() + ". Authenticated : "+connection.isAuthenticated());

					//					UserSearchManager search = new UserSearchManager(connection);
					//					Form searchForm = search.getSearchForm("emot-net");
					//					Form answerForm = searchForm.createAnswerForm();
					//					answerForm.setAnswer("test2", "DeMoro");
					//					ReportedData data = search.getSearchResults(answerForm, "emot-net");
					//					Log.i(TAG, "hmm : "+data);

					/*
					VCard vCard = new VCard();

					vCard.setFirstName("abhinav2");
					vCard.setLastName("singh2");
					vCard.setEmailHome("abhi@gmail.com");
					vCard.setJabberId("test2@emot-net");
					vCard.setOrganization("Some organization");
					vCard.setNickName("abhi");
					try {
						Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.asin);
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
						byte[] bytes = stream.toByteArray();
			            String encodedImage = StringUtils.encodeBase64(bytes);
			            vCard.setAvatar(bytes, encodedImage);
			            vCard.setEncodedImage(encodedImage);
			            vCard.setField("PHOTO", 
			            		"<TYPE>image/jpg</TYPE><BINVAL>"
			                    + encodedImage + 
			                    "</BINVAL>", 
			                    true);
			        } catch (NullPointerException e) {
			            e.printStackTrace();
			        }
					vCard.save(connection);
					Log.i(TAG, vCard.toString());



					// To load VCard:

					configure(ProviderManager.getInstance());
					Log.i(TAG, "-------------------");
					VCard vCard1 = new VCard();
					vCard1.load(connection, "test1@emot-net"); // load own VCard
					Log.i(TAG, "Nick name = " + vCard1.getNickName());
					Log.i(TAG, "avatar = " + vCard1.getAvatar());
					vCard1.load(connection, "test2@emot-net"); // load someone's VCard
					Log.i(TAG, "Nick name 1 = " + vCard1.getNickName());
					 */

					Log.i("XMPPClient", "----------------- LOGIN SUCCESSFULL --------------- ");

					try{
						boolean b = false;
						if(connection != null && connection.isConnected()){
							ConnectionQueue.add(connection);
							Log.i("XMPPClient", "putting into blocking queue successful? size=");

						}
					}catch(IllegalStateException e){
						e.printStackTrace();
					}





				} catch(Exception ex){

					Log.i("XMPPClient", "!!!!!!!!!!!  LOGINFAILS  !!!!!!!!!");
					ex.printStackTrace();
				}
				connecting = false;


			}

		});
		//connThread.start();
	}

	public static void configure(ProviderManager pm) {

		//  Private Data Storage
		pm.addIQProvider("query","jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());

		//  Time
		try {
			pm.addIQProvider("query","jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		//  Roster Exchange
		pm.addExtensionProvider("x","jabber:x:roster", new RosterExchangeProvider());

		//  Message Events
		pm.addExtensionProvider("x","jabber:x:event", new MessageEventProvider());

		//  Chat State
		pm.addExtensionProvider("active","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider()); 
		pm.addExtensionProvider("paused","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());

		//  XHTML
		pm.addExtensionProvider("html","http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

		//  Group Chat Invitations
		pm.addExtensionProvider("x","jabber:x:conference", new GroupChatInvitation.Provider());

		//  Service Discovery # Items    
		pm.addIQProvider("query","http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());

		//  Service Discovery # Info
		pm.addIQProvider("query","http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

		//  Data Forms
		pm.addExtensionProvider("x","jabber:x:data", new DataFormProvider());

		//  MUC User
		pm.addExtensionProvider("x","http://jabber.org/protocol/muc#user", new MUCUserProvider());

		//  MUC Admin    
		pm.addIQProvider("query","http://jabber.org/protocol/muc#admin", new MUCAdminProvider());

		//  MUC Owner    
		pm.addIQProvider("query","http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

		//  Delayed Delivery
		pm.addExtensionProvider("x","jabber:x:delay", new DelayInformationProvider());

		//  Version
		try {
			pm.addIQProvider("query","jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			//  Not sure what's happening here.
		}

		//  VCard
		pm.addIQProvider("vCard","vcard-temp", new VCardProvider());

		//  Offline Message Requests
		pm.addIQProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());

		//  Offline Message Indicator
		pm.addExtensionProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());

		//  Last Activity
		pm.addIQProvider("query","jabber:iq:last", new LastActivity.Provider());

		//  User Search
		pm.addIQProvider("query","jabber:iq:search", new UserSearch.Provider());

		//  SharedGroupsInfo
		pm.addIQProvider("sharedgroup","http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());

		//  JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses","http://jabber.org/protocol/address", new MultipleAddressesProvider());

		//   FileTransfer
		pm.addIQProvider("si","http://jabber.org/protocol/si", new StreamInitiationProvider());

		pm.addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());

		//  Privacy
		pm.addIQProvider("query","jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.SessionExpiredError());
	}

	public static String randomId() {
		return new BigInteger(130, new SecureRandom()).toString(32);
	}

	
}
