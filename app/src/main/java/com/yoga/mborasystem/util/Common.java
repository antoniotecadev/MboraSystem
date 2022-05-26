package com.yoga.mborasystem.util;

import android.content.Context;

import com.yoga.mborasystem.R;

import java.io.File;

public class Common {
    public static String getAppPath(Context context, String pasta) {
        File dir = new File(android.os.Environment.getExternalStorageDirectory()
                + File.separator
                + context.getResources().getString(R.string.app_name) + "/" + pasta
                + File.separator
        );
        if (!dir.exists())
            dir.mkdirs();
        return dir.getPath() + File.separator;
    }
}