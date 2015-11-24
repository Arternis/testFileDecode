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

public class Codec_PNG implements Codec {
    final static byte header[] = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    ArrayList<Chunk> mChunks;
    Bitmap mBitmap;

    public String getType() {
        return "PNG";
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
        //skip header
        try {
            stream.mark(stream.available());
            stream.skip(header.length);
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }

        //read chunks
        mChunks = new ArrayList<Chunk>();
        byte lengthbuf[] = new byte[4];
        byte typebuf[] = new byte[4];

        while (true) {
            try {
                if (-1 == stream.read(lengthbuf)) break;
                if (-1 == stream.read(typebuf)) break;
                int length = 0;
                String type = "";
                //translate byte[4] to integer
                for (int i = 0; i < 4; i++) {
                    length = (length << 8) + (lengthbuf[i] & 0xFF);
                    type += (char) (typebuf[i] & 0xFF);
                }
                mChunks.add(new Chunk(length, type));
                try {
                    stream.skip(length + 4); //data field + CRC
                } catch (IOException e1) {
                    e1.printStackTrace();
                    android.util.Log.e("Codec", "chunk is not complete for length = " + length);
                }
            } catch (IOException e) {
                e.printStackTrace();
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

        //try to decode to a bitmap
        mBitmap = BitmapFactory.decodeStream(stream);
        android.util.Log.e("Codec", "Decode bitmap = " + mBitmap);
        return mBitmap != null;
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
        int size = mChunks.size();
        ArrayList<String> chunks = new ArrayList<String>(size);

        for (int i = 0; i < size; i++) {
            Chunk c = mChunks.get(i);
            String info = "Chunk type : " + c.mType + ", Length : " + c.mLength;
            chunks.add(info);
        }
        list.setAdapter(new ArrayAdapter(context, android.R.layout.simple_expandable_list_item_1, chunks));
    }

    private class Chunk {
        int mLength;
        String mType;

        public Chunk(int length, String type) {
            mLength = length;
            mType = type;
        }
    }

}
