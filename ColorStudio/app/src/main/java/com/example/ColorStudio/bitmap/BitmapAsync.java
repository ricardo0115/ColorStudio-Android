package com.example.ColorStudio.bitmap;

import android.graphics.Bitmap;

/**
 * Interface to pass a method into a general AsyncTaskClass
 */
public interface BitmapAsync {
    Bitmap process(Bitmap bmp);

}
