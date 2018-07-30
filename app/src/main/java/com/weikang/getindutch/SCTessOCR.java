package com.weikang.getindutch;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SCTessOCR {
    public static final String PACKAGE_NAME = "com.weikang.getindutch";
    public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/AndroidOCR/";

    private static final String TAG = "TESSERACT";
    private AssetManager assetManager;

    private TessBaseAPI mTess;

    public SCTessOCR(AssetManager assetManager, String lang) {

        //Log.i(TAG, DATA_PATH);

        this.assetManager = assetManager;

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };


        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }
        }

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                OutputStream out = new FileOutputStream(new File(DATA_PATH + "tessdata/", lang + ".traineddata"));

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        mTess = new TessBaseAPI();
        mTess.setDebug(true);
        mTess.init(DATA_PATH, lang);
        mTess.setVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$.!?/,+=-*\"'<:&#");
    }

    public String getOCRResult(Bitmap bitmap) {
        if (mTess == null){
            return "wtf";
        } else {
            mTess.setImage(bitmap);
            return mTess.getUTF8Text();
        }
    }

    public TessBaseAPI getmTess() {
        return mTess;
    }

    public void onDestroy() {
        if (mTess != null) mTess.end();
    }
}
