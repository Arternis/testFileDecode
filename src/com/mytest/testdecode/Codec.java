package com.mytest.testdecode;

import android.content.Context;
import android.view.View;

import java.io.InputStream;

public interface Codec {
    public boolean factory(InputStream stream);

    public boolean decode(InputStream stream);

    public String getType();

    public View getInformation(Context context);
}
