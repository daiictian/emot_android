package com.emot.screens;

import java.io.ByteArrayOutputStream;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.emot.androidclient.XMPPRosterServiceAdapter;
import com.emot.androidclient.service.IXMPPRosterService;
import com.emot.androidclient.service.XMPPService;
import com.emot.androidclient.util.PreferenceConstants;
import com.emot.common.TaskCompletedRunnable;
import com.emot.model.EmotApplication;
import com.emot.model.EmotUser;

public class UpdateProfileScreen extends ActionBarActivity {
	private EditText editStatus;
	private Button saveButton;
	private ImageView imageAvatar;
	private static final int CAMERA_REQUEST = 1;
	private static final int PICK_FROM_GALLERY = 2;
	protected static final String TAG = UpdateProfileScreen.class.getSimpleName();
	private Intent mServiceIntent;
	private ServiceConnection mServiceConnection;
	private XMPPRosterServiceAdapter mServiceAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		editStatus = (EditText)findViewById(R.id.editTextStatus);
		editStatus.setText(EmotUser.getStatus());
		saveButton = (Button)findViewById(R.id.buttonProfileSave);
		imageAvatar = (ImageView)findViewById(R.id.imageAvatar);
		imageAvatar.setImageBitmap(EmotUser.getAvatar());
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//pd.show();
				EmotApplication.setValue(PreferenceConstants.STATUS_MESSAGE, editStatus.getText().toString());
				mServiceAdapter.setStatusFromConfig();
			}
		});

		final String[] option = new String[] { "Take from Camera", "Select from Gallery" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, option);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Select Option");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.e("Selected Item", String.valueOf(which));
				if (which == 0) {
					callCamera();
				}
				if (which == 1) {
					callGallery();
				}

			}
		});
		final AlertDialog dialog = builder.create();

		imageAvatar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.show();
			}
		});
		Log.i(TAG, "on create of update profile screen");
		registerXMPPService();
	}


	@Override
	protected void onResume() {
		super.onResume();
		bindXMPPService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindXMPPService();
	}

	private void registerXMPPService() {
		Log.i(TAG, "called startXMPPService()");
		mServiceIntent = new Intent(this, XMPPService.class);
		mServiceIntent.setAction("org.emot.androidclient.XMPPSERVICE");

		mServiceConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.i(TAG, "called onServiceConnected()");
				mServiceAdapter = new XMPPRosterServiceAdapter(IXMPPRosterService.Stub.asInterface(service));
			}

			public void onServiceDisconnected(ComponentName name) {
				Log.i(TAG, "called onServiceDisconnected()");
			}

		};
	}

	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Service wasn't bound!");
		}
	}

	private void bindXMPPService() {
		bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}



	public void callCamera() {
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra("crop", "true");
		cameraIntent.putExtra("aspectX", 0);
		cameraIntent.putExtra("aspectY", 0);
		cameraIntent.putExtra("outputX", 200);
		cameraIntent.putExtra("outputY", 150);
		startActivityForResult(cameraIntent, CAMERA_REQUEST);

	}

	public void callGallery() {
//		TaskCompletedRunnable avatarHandler = new TaskCompletedRunnable() {
//
//			@Override
//			public void onTaskComplete(String result) {
//				pd.cancel();
//				if(result.equals("success")){
//					Log.i(TAG, "Status being set is ");
//				}else{
//					Toast.makeText(EmotApplication.getAppContext(), "Oops, we encountered some error while updating your pic. Please try again later.", Toast.LENGTH_LONG).show();
//				}
//				imageAvatar.setImageBitmap(EmotUser.getAvatar());
//			}
//		};
//		Bitmap yourImage = BitmapFactory.decodeResource(getResources(), R.drawable.friends);
//		chatService.updateAvatar(yourImage, avatarHandler);

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 0);
		intent.putExtra("aspectY", 0);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_GALLERY);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		TaskCompletedRunnable avatarHandler = new TaskCompletedRunnable() {

			@Override
			public void onTaskComplete(String result) {
				if(result.equals("success")){
					Log.i(TAG, "Status being set is ");
				}else{
					Toast.makeText(EmotApplication.getAppContext(), "Oops, we encountered some error while updating your pic. Please try again later.", Toast.LENGTH_LONG).show();
				}
				imageAvatar.setImageBitmap(EmotUser.getAvatar());
			}
		};

		switch (requestCode) {
		case CAMERA_REQUEST:

			Bundle extras = data.getExtras();

			if (extras != null) {
				Bitmap yourImage = extras.getParcelable("data");
				Uri selectedImageUri = data.getData();
                String selectedImagePath = getPath(selectedImageUri);
				// convert bitmap to byte
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte imageInByte[] = stream.toByteArray();
				Log.e("output before conversion", imageInByte.toString());
			}
			break;
		case PICK_FROM_GALLERY:
			Bundle extras2 = data.getExtras();

			if (extras2 != null) {
//				Uri selectedImageUri = data.getData();
//              String selectedImagePath = getPath(selectedImageUri);
//              chatService.updateAvatar(selectedImagePath, avatarHandler);
				
				Bitmap yourImage = extras2.getParcelable("data");
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte imageInByte[] = stream.toByteArray();
				Log.e("output before conversion", imageInByte.toString());
				mServiceAdapter.setAvatar(yourImage);
				imageAvatar.setImageBitmap(yourImage);
			}

			break;
		}
	}
	
	public String getPath(Uri uri) {
		// just some safety built in 
		if( uri == null ) {
			// TODO perform some logging or show user feedback
			return null;
		}
		// try to retrieve the image from the media store first
		// this will only work for images selected from gallery
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if( cursor != null ){
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		// this is our fallback here
		return uri.getPath();
	}
	
}
