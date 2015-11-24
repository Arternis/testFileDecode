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

public class Codec_WBMP implements Codec {
    Bitmap mBitmap;

    static int read_mbf(InputStream stream) {
        int n = 0;
        byte data[] = new byte[1];
        do {
            try {
                if (stream.read(data) == -1) {
                    return -1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            n = (n << 7) | (data[0] & 0x7F);
        } while ((data[0] & 0x80) != 0);
        return n;
    }

    public String getType() {
        return "WBMP";
    }

    @Override
    public boolean factory(InputStream stream) {
        byte data[] = new byte[1];
        int width = 0;
        int height = 0;
        try {
            if (stream.read(data) == -1 || data[0] != 0) return false;
            if (stream.read(data) == -1 || (data[0] & 0x9F) != 0) return false;
            if ((width = read_mbf(stream)) == -1 || width > 0xFFFF) return false;
            if ((height = read_mbf(stream)) == -1 || height > 0xFFFF) return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return width != 0 && height != 0;
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
