package com.security.android;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.entity.StringEntity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.security.android.dialogs.InputTextDialog;
import com.security.android.model.District;
import com.security.android.util.ApiConnector;
import com.security.android.util.SingletonDataExchange;

/**
 * main and only activity of the app
 * 
 * @author zikesjan
 * 
 */
public class MainActivity extends FragmentActivity {

	private Location location;
	private static final int CAMERA_REQUEST = 1888;
	private String uid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		List<String> providers = locationManager.getProviders(true);

		for (int i = providers.size() - 1; i >= 0; i--) {
			location = locationManager.getLastKnownLocation(providers.get(i));
			if (location != null)
				break;
		}

		TelephonyManager tManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		uid = tManager.getDeviceId();

		if (location != null) {

			AsyncTask<Double, Void, District> task = new GetInfoApiAsyncTask();
			task.execute(location.getLatitude(), location.getLongitude());

		} else {
			Toast.makeText(
					MainActivity.this,
					getApplicationContext().getResources().getString(
							R.string.geolocation_problem), Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * method that is calling help
	 * 
	 * @param view
	 */
	public void help(View view) {
		if (location != null) {
			AsyncTask<StringEntity, Void, Boolean> task = new HelpApiAsyncTask();
			task.execute(jsonifyDataHelp());
		} else {
			Toast.makeText(
					this,
					getApplicationContext().getResources().getString(
							R.string.geolocation_problem), Toast.LENGTH_LONG)
					.show();
		}
	}

	public void report(View view) {
		// Log.v("location = "+location.getLatitude()+", "+location.getLongitude(),
		// "location = "+location.getLatitude()+", "+location.getLongitude());
		if (location != null) {
			Bundle args = new Bundle();
			args.putDouble("lat", location.getLatitude());
			args.putDouble("lon", location.getLongitude());
			args.putString("uid", uid);

			InputTextDialog itd = InputTextDialog.newInstance();
			itd.setArguments(args);
			itd.show(getSupportFragmentManager(), "message");
		} else {
			Toast.makeText(
					this,
					getApplicationContext().getResources().getString(
							R.string.geolocation_problem), Toast.LENGTH_LONG)
					.show();
		}
	}

	public void reportPhoto(View view) {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAMERA_REQUEST);

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");

			ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
			photo.compress(Bitmap.CompressFormat.JPEG, 100,
					byteArrayBitmapStream);
			byte[] b = byteArrayBitmapStream.toByteArray();

			String encodedImage = Base64.encodeToString(b, Base64.NO_WRAP);

			if (location != null) {
				StringEntity se = jsonifyDataPhoto(encodedImage);
				AsyncTask<StringEntity, Void, Boolean> task = new PhotoApiAsyncTask();
				task.execute(se);

			} else {
				Toast.makeText(
						this,
						getApplicationContext().getResources().getString(
								R.string.geolocation_problem),
						Toast.LENGTH_LONG).show();
			}

		}
	}

	private StringEntity jsonifyDataPhoto(String data) {
		StringEntity params = null;
		try {
			params = new StringEntity("{\"lat\":" + location.getLatitude()
					+ ",\"lon\":" + location.getLongitude() + ",\"uid\":\""
					+ uid + "\",\"photo\":\"" + data + "\"}");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return params;
	}

	private StringEntity jsonifyDataHelp() {
		StringEntity params = null;
		try {
			params = new StringEntity("{\"lat\":" + location.getLatitude()
					+ ",\"lon\":" + location.getLongitude() + ",\"uid\":\""
					+ uid + "\"}");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return params;
	}

	/**
	 * Async task that is calling API that gets initial informations about the
	 * district
	 * 
	 * @author zikesjan
	 * 
	 */
	private class GetInfoApiAsyncTask extends AsyncTask<Double, Void, District> {

		// private GeoPoint origin;

		// private GeoPoint destination;

		private ProgressDialog dialog;

		private double latitude;

		private double longtitude;

		@Override
		protected void onPreExecute() {

			// show progress dialog
			dialog = ProgressDialog.show(MainActivity.this, "", getResources()
					.getString(R.string.info_dialog_text), true);
			dialog.setCancelable(true);

			super.onPreExecute();
		}

		@Override
		protected District doInBackground(Double... coordinates) {
			// check for invalid number of arguments
			if (coordinates.length != 2) {
				return null;
			}

			latitude = coordinates[0];
			longtitude = coordinates[1];

			// return result of appropriate instance
			return ApiConnector.getInfo(latitude, longtitude); // FIXME
		}

		@Override
		protected void onPostExecute(District result) {
			// TODO set textfields in UI
			if (result != null) {
				TextView tv = (TextView) findViewById(R.id.text);
				tv.setText(result.getName());
				tv = (TextView) findViewById(R.id.text2);
				tv.setText(result.getDescription());
			} else {
				TextView tv = (TextView) findViewById(R.id.text2);
				tv.setText("server conection problem!!!");
			}
			dialog.cancel();
			super.onPostExecute(result);
		}

	}

	/**
	 * Async task that is calling API that calls help
	 * 
	 * @author zikesjan
	 * 
	 */
	private class HelpApiAsyncTask extends
			AsyncTask<StringEntity, Void, Boolean> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {

			// show progress dialog
			dialog = ProgressDialog.show(MainActivity.this, "", getResources()
					.getString(R.string.calling_help), true);
			dialog.setCancelable(true);

			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(StringEntity... data) {
			// check for invalid number of arguments
			if (data.length != 1) {
				return null;
			}

			// return result of appropriate instance
			return ApiConnector.postHelp(data[0]); // FIXME
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO set textfields in UI
			dialog.cancel();
			if (result) {
				Toast.makeText(
						MainActivity.this,
						getApplicationContext().getResources().getString(
								R.string.help_is_coming), Toast.LENGTH_LONG)
						.show();
			} else {
				if (SingletonDataExchange.getInstance().banned) {
					Toast.makeText(
							MainActivity.this,
							getApplicationContext().getResources().getString(
									R.string.banned), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
							MainActivity.this,
							getApplicationContext().getResources().getString(
									R.string.help_call_problem),
							Toast.LENGTH_LONG).show();
				}
			}
			super.onPostExecute(result);
		}

	}

	/**
	 * Async task that is dealing with sending photo
	 * 
	 * @author zikesjan
	 * 
	 */
	private class PhotoApiAsyncTask extends
			AsyncTask<StringEntity, Void, Boolean> {

		private ProgressDialog dialog;

		private StringEntity data;

		@Override
		protected void onPreExecute() {

			// show progress dialog
			dialog = ProgressDialog.show(MainActivity.this, "", getResources()
					.getString(R.string.uploading), true);
			dialog.setCancelable(true);

			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(StringEntity... se) {
			// check for invalid number of arguments
			if (se.length != 1) {
				return null;
			}

			data = se[0];

			// return result of appropriate instance
			return ApiConnector.postPhoto(data);
		}

		@Override
		protected void onPostExecute(Boolean result) {

			dialog.cancel();
			if (result) {
				Toast.makeText(MainActivity.this,
						getResources().getString(R.string.data_sent),
						Toast.LENGTH_LONG).show();
			} else {
				if (SingletonDataExchange.getInstance().banned) {
					Toast.makeText(
							MainActivity.this,
							getApplicationContext().getResources().getString(
									R.string.banned), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(
							MainActivity.this,
							getResources().getString(
									R.string.sending_data_failed),
							Toast.LENGTH_LONG).show();
				}
			}
			super.onPostExecute(result);
		}

	}
}
