package com.mytest.testdecode.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.mytest.testdecode.Codec;
import com.mytest.testdecode.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class Codec_JPEG implements Codec {
    final static byte header[] = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    Bitmap mBitmap;
    private ArrayList<Marker> mMarkers;

    public String getType() {
        return "JPEG";
    }

    @Override
    public View getInformation(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View infoView = layoutInflater.inflate(R.layout.image_info, null);
        ImageView imageView = (ImageView) infoView.findViewById(R.id.content);
        imageView.setImageBitmap(mBitmap);

        ListView list = (ListView) infoView.findViewById(R.id.listView);
        setupAdapter(context, list);

        return infoView;
    }

    private void setupAdapter(Context context, ListView list) {
        int size = mMarkers.size();
        ArrayList<String> chunks = new ArrayList<String>(size);

        for (int i = 0; i < size; i++) {
            Marker m = mMarkers.get(i);
            String info = "Marker type : " + String.format("0x%x", m.mType) + ", Length : " + m.mLength;
            chunks.add(info);
        }
        list.setAdapter(new ArrayAdapter(context, android.R.layout.simple_expandable_list_item_1, chunks));
    }

    @Override
    public boolean factory(InputStream stream) {
        byte buffer[] = new byte[header.length];
        try {
            stream.read(buffer, 0, header.length);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return Arrays.equals(buffer, header);
    }

    @Override
    public boolean decode(InputStream stream) {
        //skip header SOI
        try {
            stream.mark(stream.available());
            stream.skip(2);
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }
        //read markers
        mMarkers = new ArrayList<Marker>();
        int buf, seg;
        byte lengthbuf[] = new byte[2];
        while (true) {
            try {
                buf = stream.read();
                if (buf != 0xFF || buf == -1) break;
                seg = stream.read();
                if (seg == -1) break;
                int length = 0;
                //parameterless segments that DON'T have a size
                if (seg != 0x01 && (seg < 0xd0 || seg > 0xd7)) {
                    if (-1 == stream.read(lengthbuf)) break;
                    for (int i = 0; i < 2; i++) {
                        length = (length << 8) + (lengthbuf[i] & 0xFF);
                    }
                }
                mMarkers.add(new Marker(length, seg));
                try {
                    if (length > 2) {
                        stream.skip(length - 2); //segment size
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                    android.util.Log.e("Codec", "marker is not complete for length = " + length);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            if (seg == 0xda) { //The image data (scans) is immediately following the SOS segment
                break;
            }
        }

        //reset steam to begin
        try {
            stream.reset();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mBitmap = BitmapFactory.decodeStream(stream);
        return mBitmap != null;
    }

    private class Marker {
        int mLength;
        int mType;

        public Marker(int length, int type) {
            mLength = length;
            mType = type;
        }
    }

}
