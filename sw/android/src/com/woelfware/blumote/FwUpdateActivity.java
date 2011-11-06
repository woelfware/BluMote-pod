package com.woelfware.blumote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This activity is launched from the main BluMote interface. 
 * This activity deals with updating the firmware on the pod hardware.
 * @author keusej
 *
 */
public class FwUpdateActivity extends Activity implements OnItemClickListener {	
	private FwArrayAdapter fwImagesArrayAdapter;
    ListView fwImagesListView;
	
    String[] fwImages;
    
    DownloadManager manager = new DownloadManager();

    public static final String FW_IMAGES = "FW_IMAGES";
    // constants for showDialog()
    private static final int FW_DOWNLOAD_DIALOG = 0;

    static final String FW_LOCATION = "FW_LOCATION";
    
    // name of file when stored in the sdcard temp directory
    private static final String FW_IMAGE_NAME = "fwImage.bin";

    // progress dialog used for flashing code to pod and downloading FW
    ProgressDialog progressDialog;
    ProgressDialog progressDialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	// Setup the window
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	setContentView(R.layout.fw_manage); 

    	Intent i = getIntent();
    	fwImages = i.getStringArrayExtra(FW_IMAGES);

    	// Initialize array adapters
    	fwImagesArrayAdapter = new FwArrayAdapter(this, R.layout.fw_images_item); 

    	fwImagesListView = (ListView) findViewById(R.id.fw_images_list); 
    	fwImagesListView.setAdapter(fwImagesArrayAdapter);
    	fwImagesListView.setOnItemClickListener(this);
    	populateDisplay();             

    	// since we know that GET_VERSION was called prior to this activity, we can
    	// construct the version data from the Pod class
    	StringBuilder podVersion = new StringBuilder();
    	String podV;
    	try {
	    	podVersion.append(Pod.pod_data[1] + ".");
	    	podVersion.append(Pod.pod_data[2] + ".");
	    	podVersion.append(Pod.pod_data[3]);
	    	podV = podVersion.toString();
    	} catch (Exception e) {
    		podV = "";
    	}
    	// indicate the version in the list that matches podVersion
    	for (int j=0 ; j < fwImagesArrayAdapter.getCount(); j++) {
    		FwItem test = fwImagesArrayAdapter.getItem(j);
    		if (test.title.equalsIgnoreCase(podV)) {
    			test.notes = "Currently installed version";
    			break;
    		}
    	}    	
    	// set to OK only after data all downloaded and ready to flash
    	setResult(RESULT_CANCELED,i);        
	}

	/**
	 * Populate the display with all the fw images
	 */
	private void populateDisplay() {
        fwImagesArrayAdapter.clear(); // always clear before adding items
        
        if (fwImages != null && fwImages.length > 0) {
        	// iterate through these values
        	for (String item : fwImages) {	
        		if (item.equals("")) {
        			continue; // skip if its empty
        		}
        		// otherwise split based on csv format
        		String[] items = item.split(",");
        		try {
        			if (items.length >= 3) {
        				fwImagesArrayAdapter.add(
        						new FwItem(items[0], items[1], new URL(items[2]), items[3]));
        			}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.d("URL", "Failed to create URL, malformed");
				}
        	}
        }
	}
	@Override
	protected void onPrepareDialog(int id, Dialog d) {
		ProgressDialog dialog = (ProgressDialog)d;	
		switch(id) {         
		case FW_DOWNLOAD_DIALOG:				
			dialog.setProgress((int)0);			
            return;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		
		switch(id) {         
			case FW_DOWNLOAD_DIALOG:				
				progressDialog = new ProgressDialog(FwUpdateActivity.this);
	            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	            progressDialog.setCancelable(true); // allow back button to cancel it
	            progressDialog.setMessage("Downloading firmware image...");
	            return progressDialog;
	            	            
			default:
				return null;
		}
	}
	   
	/**
	 * The on-click listener for all devices in the ListViews
	 * @param av
	 * @param v The View object of the listview
	 * @param position the position that was clicked in the listview
	 * @param id the resource-id of the listview
	 */
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {		
		final FwItem item = fwImagesArrayAdapter.getItem(position);
		// prompt user if they want to download and install this version
		new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_dialog_alert)
	        .setMessage(R.string.confirm_fw)
	        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {	               
	            	// run the download manager for the image
	        		manager.start(item);
	            }

	        })
	        .setNegativeButton(R.string.cancel, null)
	        .show();
		
	}
	
	private class DownloadManager {
		FwItem item;
		
		DownloadManager() {
			// unused constructor
		}
		
		void start(FwItem item) {
			// download the binary file
			this.item = item;
						
			showDialog(FW_DOWNLOAD_DIALOG);
			DownloadBinaryFileTask downloader = new DownloadBinaryFileTask();
			downloader.execute(item.url); 
		}
		
		void loadFwImage(String imageName, File fileDir) {
			dismissDialog(FW_DOWNLOAD_DIALOG);
			
			// compare md5sum of downloaded file with expected value
			if (Util.FileUtils.checkMD5(item.md5sum, fileDir, FW_IMAGE_NAME)) {
				// if it passes then begin loading process......
				// send BluMote the name of the downloaded file
				// and it will then start loading it to the pod
				Intent i = getIntent();
				
				try {
					i.putExtra(FwUpdateActivity.FW_LOCATION, 
							new File(fileDir, FW_IMAGE_NAME).getCanonicalPath());

					setResult(RESULT_OK,i);
					finish();	
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// if md5sum check doesn't pass then alert user
				new AlertDialog.Builder(FwUpdateActivity.this)
		        	.setIcon(android.R.drawable.ic_dialog_alert)
			        .setTitle(R.string.error)
			        .setMessage(R.string.error_fw_download)
			        .setPositiveButton(R.string.OK, null)
			        .show();
			}
		}
	}

	/**
	 * Custom array adapter to allow for 2 types of text to be 
	 * on a list entry.  
	 * @author keusej
	 *
	 */
	public class FwArrayAdapter extends ArrayAdapter<FwItem> {
		Context context; 
		int layoutResourceId;    

		public FwArrayAdapter(Context context, int layoutResourceId) {
			super(context, layoutResourceId);
			this.layoutResourceId = layoutResourceId;
			this.context = context;     
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			FwTextHolder holder = null;

			if(row == null)
			{
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new FwTextHolder();
				holder.txtNotes = (TextView)row.findViewById(R.id.fw_notes_label);
				holder.txtTitle = (TextView)row.findViewById(R.id.fw_version_label);

				row.setTag(holder);
			}
			else
			{
				holder = (FwTextHolder)row.getTag();
			}

			FwItem item = getItem(position);
			holder.txtTitle.setText(item.title);
			holder.txtNotes.setText(item.notes);

			return row;
		}

		class FwTextHolder
		{
			TextView txtNotes;
			TextView txtTitle;
		}
	}

	/**
	 * Holder class for data items in each arrayadapter entry
	 * @author keusej
	 *
	 */
	static class FwItem {
		public String title;
		public String notes;
		public URL url;
		public String md5sum;

		public FwItem(){
			super();
		}

		public FwItem(String title, String notes, URL url, String md5sum) {
			super();
			this.notes = notes;
			this.title = title;
			this.url = url;
			this.md5sum = md5sum;
		}
	}

	private class DownloadBinaryFileTask extends AsyncTask<URL, Integer, Integer> {
		int byteCounter = 0;   
		File fileDir = FwUpdateActivity.this.getExternalFilesDir(null);

		@Override 
		protected Integer doInBackground(URL... urls) {
			try {
				publishProgress(0); // start at 0
				URL url = urls[0];
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();
				int count = c.getContentLength(); // get file size
				FileOutputStream f = new FileOutputStream(new File(fileDir,FW_IMAGE_NAME));

				InputStream in = c.getInputStream();
				byte[] buffer = new byte[1024];
				int len1 = 0;
				while ( (len1 = in.read(buffer)) > 0 ) {
					f.write(buffer, 0, len1);
					byteCounter += len1;
					publishProgress((int) ((byteCounter / (float) count) * 100));
				}
				c.disconnect();
				f.close();

			} catch (Exception e) {}
			return new Integer(byteCounter);
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// update progress bar
			progressDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(Integer bytes) {
			manager.loadFwImage(FW_IMAGE_NAME, fileDir);
			return;
		}
	}
}
