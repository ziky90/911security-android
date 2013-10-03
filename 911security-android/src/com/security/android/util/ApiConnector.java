package com.security.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.security.android.model.District;

public class ApiConnector {

	private static final String infoURL = "http://911Backend-911Backend.rhcloud.com/client/info/";

	private static final String helpURL = "http://911Backend-911Backend.rhcloud.com/client/report/help";

	private static final String reportURL = "http://911Backend-911Backend.rhcloud.com/client/report/details";

	private static final String reportPhotoURL = "http://911Backend-911Backend.rhcloud.com/client/report/photo";

	
	
	
	
	
	
	
	/**
	 * method for getting the informations
	 * 
	 * @param latitude
	 * @param longtitude
	 * @return
	 */
	public static District getInfo(double latitude, double longtitude) {
		String url = infoURL + latitude + "/" + longtitude;
		HttpClient httpclient = new DefaultHttpClient();
		try {
			

			HttpGet method = new HttpGet(new URI(url));
			HttpResponse response = httpclient.execute(method);
			StatusLine statusLine = response.getStatusLine();

			//Log.v(statusLine.toString(), "");
			
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();

				// a simple JSON response read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);

				// a simple JSONObject creation
				JSONObject json = null;
				try {
					json = new JSONObject(result);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// closing the input stream will trigger connection release
				instream.close();

				try {
					return new District(json.getString("name"),
							json.getString("description"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (statusLine.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
				return new District(
						"District does not support the app where you are",
						"ask them why?");
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		return null;
	}

	/**
	 * method for posting help request
	 * 
	 * @param latitude
	 * @param longtitude
	 * @return
	 */
	public static boolean postHelp(StringEntity params) {
		boolean result = false;
		HttpClient httpclient = new DefaultHttpClient();
		try {

			HttpPost post = new HttpPost(new URI(helpURL));
			post.setHeader("Content-type", "application/json");
			post.setEntity(params);

			HttpResponse response = httpclient.execute(post);

			StatusLine statusLine = response.getStatusLine();

			//Log.v(statusLine.toString(), statusLine.toString());
			
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				result = true;
			}else if(statusLine.getStatusCode() == HttpStatus.SC_FORBIDDEN){
				SingletonDataExchange.getInstance().banned = true;
			}

		} catch (ClientProtocolException e) {
			result = false;
			e.printStackTrace();
		} catch (IOException e) {
			result = false;
			e.printStackTrace();
		} catch (URISyntaxException e1) {
			result = false;
			e1.printStackTrace();
		}
		return result;
	}

	/**
	 * method that is posting report
	 * 
	 * @param data
	 * @return
	 */
	public static boolean postReport(StringEntity data) {
		boolean result = false;
		HttpClient httpclient = new DefaultHttpClient();
		try {

			HttpPost post = new HttpPost(new URI(reportURL));
			post.setHeader("Content-type", "application/json");
			post.setEntity(data);

			HttpResponse response = httpclient.execute(post);

			StatusLine statusLine = response.getStatusLine();

			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				result = true;
			}else if(statusLine.getStatusCode() == HttpStatus.SC_FORBIDDEN){
				SingletonDataExchange.getInstance().banned = true;
			}

		} catch (ClientProtocolException e) {
			result = false;
			e.printStackTrace();
		} catch (IOException e) {
			result = false;
			e.printStackTrace();
		} catch (URISyntaxException e1) {
			result = false;
			e1.printStackTrace();
		}
		return result;
	}

	/**
	 * method that is posting report
	 * 
	 * @param data
	 * @return
	 */
	public static boolean postPhoto(StringEntity data) {
		boolean result = false;
		HttpClient httpclient = new DefaultHttpClient();
		try {

			HttpPost post = new HttpPost(new URI(reportPhotoURL));
			post.setHeader("Content-type", "application/json");
			post.setEntity(data);

			HttpResponse response = httpclient.execute(post);

			StatusLine statusLine = response.getStatusLine();

			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				result = true;
			}else if(statusLine.getStatusCode() == HttpStatus.SC_FORBIDDEN){
				SingletonDataExchange.getInstance().banned = true;
			}

		} catch (ClientProtocolException e) {
			result = false;
			e.printStackTrace();
		} catch (IOException e) {
			result = false;
			e.printStackTrace();
		} catch (URISyntaxException e1) {
			result = false;
			e1.printStackTrace();
		}
		return result;
	}

	/**
	 * method for converting stream to string
	 * 
	 * @param is
	 * @return
	 */
	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
