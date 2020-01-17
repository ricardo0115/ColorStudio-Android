package com.example.ColorStudio.bitmap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/**
 * Helps to scale an image to prevent memory crashes in phone
 */
public class BitmapHelper {
    private Activity activity;
    private int scaleLimit;

    public BitmapHelper() {
    }

    public BitmapHelper(Activity activity2, int scaleLimit2) {
        this.activity = activity2;
        this.scaleLimit = scaleLimit2;
    }

    /**
     * Return sample size of a Bitmap which Options has passed in parameter
     * @param options options of Bitmap
     * @param reqWidth Required width for downscale
     * @param reqHeight Required height for downscaling
     * @return desired approximately size
     */
    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /***Getters and setters ***/
    public int getScaleLimit() {
        return this.scaleLimit;
    }

    public void setScaleLimit(int scaleLimit2) {
        this.scaleLimit = scaleLimit2;
    }

    public Activity getActivity() {
        return this.activity;
    }

    public void setActivity(Activity activity2) {
        this.activity = activity2;
    }

    /**
     * Scale options if width is to big
     * @param options Bitmap options
     * @param width Desired width
     */

    public void scaleOptionsToFitWidth(Options options, int width) {
        if (options.outWidth > width) {
            options.inSampleSize = calculateInSampleSize(options, width, ((int) (((float) width) / ((float) options.outWidth))) * options.outHeight);
        }
    }

    /**
     * Decode and scale Bitmap which has been loaded from resources
     * @param id id from bitmap
     * @return return downscaled bitmap
     */
    public Bitmap decodeAndScaleBitmapFromResId(int id) {
        Options opt = new Options();
        opt.inScaled = false;
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(this.activity.getResources(), id, opt);
        scaleOptionsToFitWidth(opt, getScaleLimit());
        opt.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(this.activity.getResources(), id, opt);
    }
    /**
     * Decode and scale Bitmap which has been loaded from resources
     * @param imageUri uri to bitmap
     * @return return downscaled bitmap
     */
    public Bitmap decodeAndScaleBitmapFromUri(Uri imageUri) {
        Bitmap selectedImage = null;
        try {
            InputStream imageStream = this.activity.getContentResolver().openInputStream(imageUri);
            Options options = new Options();
            options.inScaled = false;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            Objects.requireNonNull(imageStream).close();
            scaleOptionsToFitWidth(options, getScaleLimit());
            options.inJustDecodeBounds = false;
            InputStream imageStream2 = this.activity.getContentResolver().openInputStream(imageUri);
            selectedImage = BitmapFactory.decodeStream(imageStream2, null, options);
            Objects.requireNonNull(imageStream2).close();
            return selectedImage;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return selectedImage;
        } catch (IOException e2) {
            e2.printStackTrace();
            return selectedImage;
        }
    }
}
