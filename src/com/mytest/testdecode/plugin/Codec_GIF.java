package com.mytest.testdecode.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.mytest.testdecode.Codec;
import com.mytest.testdecode.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Codec_GIF implements Codec {
    final static byte header1[] = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61};
    final static byte header2[] = {0x47, 0x49, 0x46, 0x38, 0x37, 0x61};
    Bitmap mBitmap;

    public String getType() {
        return "GIF";
    }

    @Override
    public boolean factory(InputStream stream) {
        byte buffer[] = new byte[header1.length];
        try {
            stream.read(buffer, 0, header1.length);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return Arrays.equals(buffer, header1) || Arrays.equals(buffer, header2);
    }

    @Override
    public boolean decode(InputStream stream) {
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
        return infoView;
    }
}
