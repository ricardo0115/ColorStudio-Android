package com.example.ColorStudio.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import com.example.ColorStudio.activity.MainActivity;
import com.example.ColorStudio.bitmap.BitmapAsync;

import java.lang.ref.WeakReference;

/**
 * General Async task any filter function from this project as an async Task
 */
public class Task extends AsyncTask<Bitmap, Void, Bitmap> {
    private WeakReference<MainActivity> activityWeakReference;
    private BitmapAsync callback;

    public Task(BitmapAsync callback, MainActivity activity) {
        this.callback = callback;
        this.activityWeakReference = new WeakReference<MainActivity>(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return;
        }
        activity.getProgressBar().setVisibility(View.VISIBLE);
    }

    @Override
    protected Bitmap doInBackground(Bitmap ... bitmaps) {
        MainActivity activity = activityWeakReference.get();
        Bitmap bmp;
        while (!isCancelled()) {
            if (activity == null || activity.isFinishing()) {
                return bitmaps[0];
            }

            bmp = callback.process(bitmaps[0]);
            return bmp;
        }
        return bitmaps[0];

    }



    @Override
    protected void onPostExecute(Bitmap img) {
        super.onPostExecute(img);
        MainActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()){
            return;
        }
        activity.getProgressBar().setVisibility(View.INVISIBLE);
        activity.setModBmp(img);
        activity.getImgview().setImageBitmap(img);
        //ctivity.getHistogramBarHelper().updateHistogram(true);
    }
}