package com.mytest.testdecode;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import dalvik.system.DexFile;

public class Decoder {
    final static String TAG = "TestDecode";
    private ArrayList<Codec> mCodecs = new ArrayList<Codec>();

    public Decoder(Context context) {
        init(context);
    }

    void addCodec(Codec codec) {
        mCodecs.add(codec);
        Log.i(TAG, "codec count " + mCodecs.size());
    }

    private ArrayList<Class<?>> getClassesForPackage(Context context) {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        try {
            DexFile df = new DexFile(context.getPackageCodePath());
            for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); ) {
                String s = iter.nextElement();
                Class<?> c;
                try {
                    c = Class.forName(s);
                    if (Codec.class.isAssignableFrom(c) && !c.isInterface()) {
                        Log.i(TAG, "find codec " + s);
                        classes.add(c);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    private void init(Context context) {
        ArrayList<Class<?>> classes = getClassesForPackage(context);
        Log.i(TAG, "find codec classes " + classes.size());
        for (Class<?> c : classes) {
            try {
                addCodec((Codec) c.newInstance());
            } catch (InstantiationException e) {
                Log.i(TAG, "InstantiationException");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.i(TAG, "IllegalAccessException");
                e.printStackTrace();
            }
        }
    }

    public Codec factory(InputStream inputStream) throws IOException {
        for (Codec codec : mCodecs) {
            inputStream.mark(255);
            if (codec.factory(inputStream)) {
                inputStream.reset();
                return codec;
            }
            inputStream.reset();
        }

        return null;
    }

}
