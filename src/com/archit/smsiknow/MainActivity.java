package com.archit.smsiknow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.gitanshu.smsiknow.R;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
	ArrayList<Sms> lista;
    ArrayAdapter<Sms> adaptor;
    Sms sms1;
	Sms sms2;
	List<Sms> list;
	ProgressBar progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progress = (ProgressBar) findViewById(R.id.progress);
		
		loadSms();
	}
	
	public void reload(MenuItem m){
		loadSms();
	}
	
	
	public void loadSms(){
		AsyncTask<Void, Void, List<Sms>> task = new AsyncTask<Void, Void, List<Sms>>() {
			@Override
			protected void onPreExecute(){
				progress.setVisibility(View.VISIBLE);
				lista = new ArrayList<Sms>();
				adaptor = new MyArrayAdapter(getBaseContext(), lista);
			}
						
			@Override
	    	protected List<Sms> doInBackground(Void... params) {
	        	list = getSmsList();
	        	return list;
	    	}
	    			
	    	@Override
	    	protected void onPostExecute(List<Sms> list){
	    		if (list.size() != 0 ){
	    		for (int i=0; i<list.size();i++){
	    			lista.add(list.get(i));
	    		}
	    		adaptor.notifyDataSetChanged();
	    		setListAdapter(adaptor);
	    		progress.setVisibility(View.INVISIBLE);
	    		setContentView(R.layout.activity_main);}
	    		else {
	    			Toast.makeText(getBaseContext(), "No msgs", Toast.LENGTH_SHORT).show();
	    			progress.setVisibility(View.INVISIBLE);
	    		}
	    	}
	    };
	    task.execute();
	}
		

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public class MyArrayAdapter extends ArrayAdapter<Sms> {
		private final Context context;
		private final List<Sms> msgs;
		private LayoutInflater inflater;

		public MyArrayAdapter(Context context, List<Sms> msgs) {
			super(context, R.layout.list_item, msgs);
			this.context = context;
			this.msgs = msgs;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		  
		  
		  
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			Sms s = msgs.get(position);
			View vi = convertView;
			ViewHolder vh;
		    if (vi == null) {
		    	vi = getLayoutInflater().inflate(R.layout.list_item, parent, false);
		    	vh = new ViewHolder();
		    	vh.number = (TextView) vi.findViewById(R.id.mnumber);
		    	vh.msg = (TextView) vi.findViewById(R.id.msms);
		    	vh.time = (TextView) vi.findViewById(R.id.mtime);
		    	vi.setTag(vh);
		    } else {
		    	vh = (ViewHolder) vi.getTag(); 
		    }
		   
			if (vh.msg !=  null && vh.number != null){
				vh.time.setText(getTime(s));
				vh.number.setText(s.getContactName());
				vh.msg.setText(s.getMsg());
			}
			  
			return vi;
			  
		}
		  
		  
	}
	
	public String getContactName(Sms s, Context context){
		String contact = s.getSender();
    	Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contact));  
    	Cursor cs = null;
        try {
    	cs= context.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME},PhoneLookup.NUMBER+"='"+contact+"'",null,null);
        
        if(cs.getCount()>0)
        {
        	cs.moveToFirst();
        	contact=cs.getString(cs.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        } 
        } finally {
        	if (cs != null) { cs.close(); }
        }
		return contact;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	public String getTime(Sms s){
		DateFormat df = new SimpleDateFormat("dd/MM hh:mm a");
		Calendar calendar = Calendar.getInstance();
		long time = Long.parseLong(s.getTime());
		calendar.setTimeInMillis(time);
		return df.format(calendar.getTime());
	}
	
	public static class ViewHolder{
		TextView number;
		TextView msg;
		TextView time;
	}
	
	@SuppressWarnings("deprecation")
	public List<Sms> getSmsList() {
	    List<Sms> lstSms = new ArrayList<Sms>();
	    Sms sms = new Sms();
	    Uri message = Uri.parse("content://sms/inbox");
	    ContentResolver cr = this.getContentResolver();
	    Cursor c = null;
	    try {
	    c = cr.query(message, null, null, null, null);
	    int totalSMS = c.getCount();

	 
	    if (c.moveToFirst()) {
	        for (int i = 0; i < totalSMS; i++) {

	            sms = new Sms();
	            sms.setSender(c.getString(c.getColumnIndexOrThrow("address")));
	            sms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
	            sms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
	            String contact = getContactName(sms, getApplicationContext());
	            if(sms.getSender() != contact){
	            	sms.setCOntactName(contact);
	            	lstSms.add(sms);
	            }
	            c.moveToNext();
	        }
	    }
	    } finally {
	    	if (c != null) {
	    		
	    	c.close();
	    	}
	    }
	    return lstSms;
	}
	
	public class Sms{
		private String sSender;
		private String sMsg;
		private String sPerson;
		private String sTime;

		public String getSender(){
			return sSender;
		}
		public String getMsg(){
			return sMsg;
		}
		public String getContactName(){
			return sPerson;
		}
		public String getTime(){
			return sTime;
		}

		public void setSender(String sender){
			sSender = sender;
		}
		public void setMsg(String msg){
			sMsg = msg;
		}
		public void setCOntactName(String person){
			sPerson = person;
		}
		public void setTime(String time){
			sTime = time;
		}
	}
}

//sms reading borrowed from http://stackoverflow.com/questions/848728/how-can-i-read-sms-messages-from-the-inbox-programmatically-in-android/851668#851668
//contact matching copied from an old code. Don't remember online source but most probably stackoverflow.