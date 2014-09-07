package com.emot.mainscreen;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TestActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		XMPPConnection xc = new XMPPConnection("abhinavsingh.local");
		try {
			xc.connect();
		} catch (XMPPException e) {
			Log.d("CONNECTION", "Conneciton error");
			e.printStackTrace();
		}
		
	}
}
