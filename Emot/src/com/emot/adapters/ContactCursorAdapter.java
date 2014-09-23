package com.emot.adapters;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.emot.api.ImageDownloader;
import com.emot.common.BitmapHandler;
import com.emot.persistence.DBContract;
import com.emot.screens.R;

public class ContactCursorAdapter extends SimpleCursorAdapter {

	private Cursor c;
	private Context context;

	@SuppressWarnings("deprecation")
	public ContactCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		this.c = c;
		this.context = context;
	}

	public View getView(int pos, View inView, ViewGroup parent) {
		View v = inView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.contact_row, null);
		}
		this.c.moveToPosition(pos);      
		String name = this.c.getString(this.c.getColumnIndex(DBContract.ContactsDBEntry.CONTACT_NAME));
		String mobile = this.c.getString(this.c.getColumnIndex(DBContract.ContactsDBEntry.MOBILE_NUMBER));
		String status = this.c.getString(this.c.getColumnIndex(DBContract.ContactsDBEntry.CURRENT_STATUS));
		String imageUrl = this.c.getString(this.c.getColumnIndex(DBContract.ContactsDBEntry.PROFILE_THUMB));
		final ImageView iv = (ImageView) v.findViewById(R.id.image_contact_profile);
		if (imageUrl != null && imageUrl != "") {

		}else{
		}
		TextView fname = (TextView)v.findViewById(R.id.text_contact_name);
		fname.setText(name);

		TextView fmobile = (TextView) v.findViewById(R.id.text_contact_number);
		fmobile.setText(mobile);

		TextView fstatus = (TextView) v.findViewById(R.id.text_contact_status);
		fmobile.setText(status);
		ImageDownloader imgCall = new ImageDownloader(imageUrl, new BitmapHandler() {
			
			@Override
			public void processImage(Bitmap img) {
				iv.setImageBitmap(img);
			}
		});
		imgCall.execute();
		return(v);
	}
}