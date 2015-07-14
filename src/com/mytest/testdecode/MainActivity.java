package com.mytest.testdecode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

	final static String TAG = "MyTest";
	final static String ASSETROOT = "test_images";
	ListView mListView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        mListView = (ListView)findViewById(R.id.listView);
        
    	String strPaths[];
		try {
			strPaths = getAssets().list(ASSETROOT);
		} catch (IOException e1) {
			Log.e(TAG, "List file failed");
			e1.printStackTrace();
			return;
		}
		Log.i(TAG, "Find files " + strPaths.length);
        mListView.setAdapter(new MyAdapter(this, strPaths));
    }
    


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private String mListPaths[];
		
		public MyAdapter(Context context, String listPaths[]) {
			mInflater = LayoutInflater.from(context);
			mListPaths = listPaths;
		}
		
		@Override
		public int getCount() {
			return mListPaths.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
				return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item, parent, false);
			}
			
			Bitmap bm = decodeBitmap(mListPaths[position]);
			
			TextView title = (TextView) convertView.findViewById(R.id.title);
			ImageView image = (ImageView) convertView.findViewById(R.id.imageItem);
			if(bm != null) {
				title.setText(mListPaths[position] );
				image.setImageBitmap(bm);
			}
			else {
				title.setText("Path " + mListPaths[position] + " decode failed..."  );
				image.setImageBitmap(null);
			}

			return convertView;
		}
		
		private Bitmap decodeBitmap(String path) {
			Bitmap bm = null;
			Log.i(TAG, "Start decode stream from file " + path);
			try {
				InputStream is = getAssets().open(ASSETROOT + "/" + path);
				bm = BitmapFactory.decodeStream(is);
				if (bm == null) {
					Log.i(TAG, "decode failed...");
				} else {
					Log.i(TAG, "load bm = " + bm);
				}
				// image.setImageBitmap(bm);
			} catch (IOException e) {
				Log.e(TAG, "open file failed");
				e.printStackTrace();
			}
			Log.i(TAG, "End decode stream");
			return bm;
		}

	}

}
