package com.emot.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.emot.common.ImageHelper;
import com.emot.emotobjects.Contact;
import com.emot.screens.R;

public class ContactArrayAdapter extends ArrayAdapter<Contact> {

	private ArrayList<Contact> contacts;
	public ContactArrayAdapter(Context context, int resource, ArrayList<Contact> contacts) {
		super(context, resource, contacts);
		this.contacts = contacts;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
	    if(view==null) {
	        LayoutInflater vi = LayoutInflater.from(this.getContext());
	        view=vi.inflate(R.layout.contact_row, null);
	    }

	    TextView name = (TextView)view.findViewById(R.id.text_contact_name);
	    TextView mobile = (TextView)view.findViewById(R.id.text_contact_number);
	    ImageView profile = (ImageView)view.findViewById(R.id.image_contact_profile);
	    name.setText(contacts.get(position).getName());
	    mobile.setText(contacts.get(position).getMobile());
	    Bitmap sampleIcon = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.asin);
	    profile.setImageBitmap(ImageHelper.getRoundedCornerBitmap(sampleIcon, 10));
	    
	    return view;
	}
}
