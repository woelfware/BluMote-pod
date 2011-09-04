
package com.woelfware.blumote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CreateActivity extends Activity {
	private EditText entered_activity;
	CharSequence activity_string;
	private Button closeActivityButton;

	static final String IMAGE_ID = "image_id";
	
	int imageId = R.drawable.tv; // set to default picture
	
	ImageAdapter imageAdapter;
	
	Gallery gallery;
	Intent i;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_activity);
		
		i = getIntent();
		
		entered_activity = (EditText) findViewById(R.id.activity_name);

		entered_activity.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// register the text when "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {

					return true;
				}
				return false;
			}
		});

		closeActivityButton = (Button)findViewById(R.id.activity_enter_button);
		closeActivityButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// grab the text for use in the activity
				activity_string = entered_activity.getText();
				i.putExtra("returnStr", activity_string.toString());
				i.putExtra(IMAGE_ID, imageId);
				setResult(RESULT_OK,i);
				finish();	
			}
		});
		
		imageAdapter = new ImageAdapter(this);
		gallery = (Gallery) findViewById(R.id.activity_gallery);
	    gallery.setAdapter(imageAdapter);

	    gallery.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView parent, View v, int position, long id) {
	            Toast.makeText(CreateActivity.this, "" + position + " RID: "+
	            		(Integer)imageAdapter.getItem(position), Toast.LENGTH_SHORT).show();
	            // save the image that was selected
	            imageId = (Integer)imageAdapter.getItem(position);
	        }
	    });

		setResult(RESULT_CANCELED,i);
	} // end of oncreate
	
	public class ImageAdapter extends BaseAdapter {
	    int mGalleryItemBackground;
	    private Context mContext;

	    private Integer[] mImageIds = {
	            R.drawable.tv,
	            R.drawable.oldgamecontroller,
	            R.drawable.dvd
	    };

	    public ImageAdapter(Context c) {
	        mContext = c;
	        TypedArray attr = mContext.obtainStyledAttributes(R.styleable.HelloGallery);
	        mGalleryItemBackground = attr.getResourceId(
	                R.styleable.HelloGallery_android_galleryItemBackground, 0);
	        attr.recycle();
	    }

	    public int getCount() {
	        return mImageIds.length;
	    }

	    public Object getItem(int position) {
	        return mImageIds[position];
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView = new ImageView(mContext);
	        imageView.setImageResource(mImageIds[position]);
	        imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
	        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
	        imageView.setBackgroundResource(mGalleryItemBackground);

	        return imageView;
	    }
	}
}
