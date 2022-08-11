package com.eiraj.intel.drone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImageUtils {

    public static MultipartBody.Part handleAndConvertImage(Context context, Bitmap bitmap, String fileName){
        return getMultipart_image(context, fileName, convertBitmapToJPEG(context, bitmap, fileName));
    }

    private static File convertBitmapToJPEG(Context context, Bitmap bitmap, String filename) {

        File f = null;

        try {
            //create a file to write bitmap data
            f = new File(context.getCacheDir(), filename);
            f.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            // Task failed for some reason
            e.printStackTrace();
        }

        Log.e("XXX", "convertBitmapToJPEG: " + f );
        return f;
    }

    private static MultipartBody.Part getMultipart_image(Context context, String fileNameInitial, File file) {

        RequestBody requestFile = null;

        try {
            requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fileName = fileNameInitial + ".jpeg";

        return MultipartBody.Part.createFormData(fileNameInitial, fileName, requestFile);
    }
}
