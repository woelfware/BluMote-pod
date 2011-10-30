package com.woelfware.blumote;

import java.io.File;
import java.io.FileOutputStream;
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
import android.os.Handler;
import android.os.Message;
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
 * TODO - 	add checking for currently installed version, indicate that to user somehow
 *    		make sure the extra fields can be extracted from listview and used
 *    		add downloading of real .BIN file logic
 *    		add flashing code to pod
 */
public class FwUpdateActivity extends Activity implements OnItemClickListener {	
	private FwArrayAdapter fwImagesArrayAdapter;
    ListView fwImagesListView;
	
    String[] fwImages;
    
    DownloadManager manager = new DownloadManager();
    
    public static final String FW_IMAGES = "FW_IMAGES";
    // constants for showDialog()
    private static final int FW_DOWNLOAD_DIALOG = 0;
    private static final int FLASH_PROGRESS_DIALOG = 1;
    
    // progress dialog used for flashing code to pod
    ProgressThread progressThread;
	ProgressDialog progressDialog;

	
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
                      
        populateDisplay();             
        
        setResult(RESULT_OK,i);       
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
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case FW_DOWNLOAD_DIALOG:
            progressDialog.setProgress(0);
            progressThread = new ProgressThread(handler);
            progressThread.start();
            return;
            
        case FLASH_PROGRESS_DIALOG:
            progressDialog.setProgress(0);
            progressThread = new ProgressThread(handler);
            progressThread.start();
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
	            
			case FLASH_PROGRESS_DIALOG:	          
				progressDialog = new ProgressDialog(FwUpdateActivity.this);
	            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	            progressDialog.setCancelable(false); // don't allow back button to cancel it
	            progressDialog.setMessage("Loading...");
	            return progressDialog;
	            
			default:
				return null;
		}
	}
	
	// Define the Handler that receives messages from the thread and update the progress
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.arg1;
            progressDialog.setProgress(total);
            if (total >= 100){
                dismissDialog(FLASH_PROGRESS_DIALOG);
                progressThread.setState(ProgressThread.STATE_DONE);
            }
        }
    };
    
	/**
	 * The on-click listener for all devices in the ListViews
	 * @param av
	 * @param v The View object of the listview
	 * @param position the position that was clicked in the listview
	 * @param id the resource-id of the listview
	 */
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {		
		final FwItem item = fwImagesArrayAdapter.getItem(position);
		// setConfirmation() gets set to false, overridden by 'ok' button
		manager.setConfirmation(false);
		// prompt user if they want to download and install this version
		new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.confirmation)
	        .setMessage(R.string.confirm_fw)
	        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {	               
	            	manager.setConfirmation(true);
	            }

	        })
	        .setNegativeButton(R.string.cancel, null)
	        .show();
		
		// run the download manager for the image
		manager.start(item);
	}
	
	private class DownloadManager {
		boolean confirmDownload = false;
		FwItem item;
		
		DownloadManager() {
			// unused constructor
		}
		
		void setConfirmation(boolean confirm) {
			this.confirmDownload = confirm;
		}
		
		void start(FwItem item) {
			if (confirmDownload == false) {
				return;
			}
			// else download the binary file
			this.item = item;
			showDialog(FW_DOWNLOAD_DIALOG);
			DownloadBinaryFileTask downloader = new DownloadBinaryFileTask();
			downloader.doInBackground(item.url); // TODO work with handler to post progress
		}
		
		void loadFwImage(String imageName, File imageLocation) {
			// compare md5sum of downloaded file with expected value
			if (Util.FileUtils.checkMD5(item.md5sum, imageLocation)) {
				// TODO
				// if it passes then begin loading process......
				// close spinning wait dialog, turn into a progress dialog for FW image flash
				dismissDialog(FW_DOWNLOAD_DIALOG);
				showDialog(FLASH_PROGRESS_DIALOG); // kick off flashing of pod ( onPrepareDialog() )				
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
		private static final String FW_IMAGE_NAME = "fwImage.bin";
	    File temp = FwUpdateActivity.this.getExternalFilesDir(null);
		
	    protected Integer doInBackground(URL... urls) {
	    	 try {
				URL url = urls[0];
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
    		    c.setRequestMethod("GET");
    		    c.setDoOutput(true);
    		    c.connect();
    		    FileOutputStream f = new FileOutputStream(new File(temp,FW_IMAGE_NAME));

    		    InputStream in = c.getInputStream();
    		        		  		   
    		    byte[] buffer = new byte[1024];
    		    int len1 = 0;
    		    while ( (len1 = in.read(buffer)) > 0 ) {
    		         f.write(buffer, 0, len1);
    		         byteCounter += len1;
    		    }
    		    f.close();
    		    
	         } catch (Exception e) {}
	         return new Integer(byteCounter);
	    }

	    protected void onPostExecute(Integer bytes) {
	    	 manager.loadFwImage(FW_IMAGE_NAME, temp);
	    	 return;
	    }
	}
	
	/** Nested class that performs progress calculations (counting)
	 *  TODO - will be used to flash code to the pod....TBD
	 */
    private class ProgressThread extends Thread {
        Handler mHandler;
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        int mState;
        int total;
       
        ProgressThread(Handler h) {
            mHandler = h;
        }
       
        public void run() {
            mState = STATE_RUNNING;   
            total = 0;
            while (mState == STATE_RUNNING) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e("ERROR", "Thread Interrupted");
                }
                Message msg = mHandler.obtainMessage();
                msg.arg1 = total;
                mHandler.sendMessage(msg);
                total++;
            }
        }
        
        /* sets the current state for the thread,
         * used to stop the thread */
        public void setState(int state) {
            mState = state;
        }
    }
}
