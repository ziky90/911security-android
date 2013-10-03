package com.security.android.dialogs;


import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.security.android.R;
import com.security.android.util.ApiConnector;


public class InputTextDialog extends DialogFragment{
	
	private double latitude;
	private double longtitude;
	private String uid;
	
	public InputTextDialog(){
		
	}
	
	
	public static InputTextDialog newInstance() {
		return new InputTextDialog();
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Bundle args = this.getArguments();
		latitude = args.getDouble("lat");
		longtitude = args.getDouble("lon");
		uid = args.getString("uid");

	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    final View layout = inflater.inflate(R.layout.text_dialog, null);
	    builder.setView(layout)
	    // Add action buttons
	           .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   EditText et = (EditText) layout.findViewById(R.id.info_edit_text);
	            	   String text = et.getText().toString();   				   
	            	   if(text.length()>0){
	   					   StringEntity se = jsonifyData(et.getText().toString());
	   					   AsyncTask<StringEntity, Void, Boolean> task = new ReportApiAsyncTask();
	   					   task.execute(se);
	   					   InputTextDialog.this.getDialog().cancel();
	   				   }else{
	   					   Toast.makeText(getActivity(), getActivity().getApplicationContext().getResources().getString(R.string.enter_text), Toast.LENGTH_LONG).show();
	   				   }
	            	   // TODO call the API
	               }
	           })
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   InputTextDialog.this.getDialog().cancel();
	               }
	           });      
	    return builder.create();
	}
	

private StringEntity jsonifyData(String text){
	StringEntity params = null;
	try {
		params = new StringEntity("{\"lat\":"+latitude+",\"lon\":"+longtitude+",\"uid\":\""+ uid +"\",\"description\":\""+text+"\"}");
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
	return params;
}
	
	
private class ReportApiAsyncTask extends AsyncTask<StringEntity, Void, Boolean> {

		
		private ProgressDialog dialog;
		
		private StringEntity data;
		
		@Override
		protected void onPreExecute() {
			
			// show progress dialog
			dialog = ProgressDialog.show(getActivity(), "", getResources().getString(R.string.reporting), true);
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
			return ApiConnector.postReport(data);	
		}
		

		@Override
		protected void onPostExecute(Boolean result) {
			
			dialog.cancel();
			if(result){
				//TODO solve this
				//Toast.makeText(getActivity(), getActivity().getApplicationContext().getResources().getString(R.string.reported), Toast.LENGTH_LONG).show();
			}else{
				//TODO solve this
				//Toast.makeText(getActivity(), getActivity().getApplicationContext().getResources().getString(R.string.report_fail), Toast.LENGTH_LONG).show();
			}			
			super.onPostExecute(result);
		}

	}
}
