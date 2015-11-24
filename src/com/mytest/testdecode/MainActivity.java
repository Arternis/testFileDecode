package com.mytest.testdecode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {
    final static String TAG = "TestDecode";
    final static String IMAGES_ROOT = "/sdcard/test_images";
    ListView mListView;
    PopupWindow mPopup;
    Decoder mDecoder;
    MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        mListView = (ListView) findViewById(R.id.listView);
        mDecoder = new Decoder(this);
        String strPaths[];
        try {
            File folder = new File(IMAGES_ROOT);
            strPaths = folder.list();
        } catch (Exception e1) {
            Log.e(TAG, "List file failed");
            e1.printStackTrace();
            return;
        }
        if (strPaths != null) {
            Log.i(TAG, "Find files " + strPaths.length);
            mAdapter = new MyAdapter(this, strPaths);
            mListView.setOnItemClickListener(mAdapter);
            mListView.setAdapter(mAdapter);
        } else {
            Log.e(TAG, "List file failed");
        }
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

    private class MyAdapter extends BaseAdapter implements OnItemClickListener {
        private LayoutInflater mInflater;
        private String mListPaths[];
        private Codec mCodecs[];

        public MyAdapter(Context context, String listPaths[]) {
            mInflater = LayoutInflater.from(context);
            mListPaths = listPaths;
            mCodecs = new Codec[listPaths.length];
            initCodec();
        }

        private void initCodec() {
            for (int i = 0; i < mListPaths.length; i++) {
                String path = mListPaths[i];
                try {
                    InputStream stream = new BufferedInputStream(new FileInputStream(IMAGES_ROOT + "/" + path));
                    mCodecs[i] = mDecoder.factory(stream);
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getCount() {
            return mListPaths.length;
        }

        @Override
        public Object getItem(int position) {
            return mListPaths[position];
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

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(mListPaths[position]);

            TextView type = (TextView) convertView.findViewById(R.id.type);
            if (mCodecs[position] != null) {
                type.setText(mCodecs[position].getType());
            } else {
                type.setText("XXX");
            }

            return convertView;
        }

        private View decodeBitmap(String path, Codec codec) {
            if (path == null || codec == null) {
                Toast.makeText(MainActivity.this, "Not supported", Toast.LENGTH_SHORT).show();
                return null;
            }

            try {
                InputStream stream = new BufferedInputStream(new FileInputStream(IMAGES_ROOT + "/" + path));
                if (!codec.decode(stream)) {
                    Log.e(TAG, "Decode failed to path " + path);
                    return null;
                }
                return codec.getInformation(MainActivity.this);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String path = (String) parent.getItemAtPosition(position);
            View v = decodeBitmap(path, mCodecs[position]);

            if (mPopup == null) {
                mPopup = new PopupWindow(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                mPopup.setFocusable(true);
            }

            if (v != null) {
                mPopup.setContentView(v);
                mPopup.showAtLocation(getWindow().getDecorView(), android.view.Gravity.CENTER, 0, 0);
            }
        }
    }
}
