package com.example.ColorStudio.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ColorStudio.bitmap.BitmapHelper;
import com.example.ColorStudio.bitmap.BitmapTransfer;
import com.example.ColorStudio.util.HistogramBarHelper;
import com.example.ColorStudio.util.Task;
import com.example.colortogray.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Main class contains
 * boolean SCALE = True for toggle image scaling
 * Bitmap bmp = Bitmap to modify
 * boolean isMod = indicates if the image has been modified
 * ImageView imgView = ImageView structure
 * BarChart barHistogram = Histogram for actual shown image
 * int defaultKeepColor = Color used by ambilwarna color selector
 * int REQUEST_GET_SINGLE_FILE = code to request a file from storage
 * BitMapHelper bmpHelper class for downscaling image.
 * int scaleLimit = safest value which prevents apps crashes.
 *
 */

public class MainActivity extends AppCompatActivity {
    public static final int COLORIZE_PIC = 104;
    public static final int REQUEST_GET_SINGLE_FILE = 202;
    private ImageView imgView;
    private Bitmap bmp;

    private Task currentTask;
    public MenuHandler menuHandler;
    private ProgressBar progressBar;
    private boolean isMod;
    private BarChart barHistogram;
    private BitmapHelper bmpHelper;
    private Bitmap modBmp;
    private boolean scale = true;
    private int scaleLimit = 3000; //safest value
    private int scaleSize = 2000; //
    private boolean renderScript = true;

    //Default kernels
    public final float[] kernelLaplace = {-1.0f, -1.0f, -1.0f, -1.0f, 8.0f, -1.0f, -1.0f, -1.0f, -1.0f};
    public final float[] kernelBlurry = {1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f, 1/9f};


    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public boolean isRenderScript() {
        return renderScript;
    }

    public void setRenderScript(boolean renderScript) {
        this.renderScript = renderScript;
    }

    public static Bitmap scaleToFitWidth(Bitmap b, int width) {
        return Bitmap.createScaledBitmap(b, width, (int) (((float) b.getHeight()) * (((float) width) / ((float) b.getWidth()))), true);
    }

    /**
     * Rewrites bmp modified with the original, mostly used by undoChanges function
     */
    public void modBmpInit() {
        setModBmp(this.bmp.copy(Config.ARGB_8888, true));
    }

    public Bitmap getModBmp() {
        return this.modBmp;
    }

    public void setModBmp(Bitmap modBmp2) {
        this.modBmp = modBmp2;
    }

    public BitmapHelper getBmpHelper() {
        return this.bmpHelper;
    }

    public void setBmpHelper(BitmapHelper bmpHelper2) {
        this.bmpHelper = bmpHelper2;
    }

    public ProgressBar getProgressBar() {
        return this.progressBar;
    }

    public static int getRequestGetSingleFile() {
        return REQUEST_GET_SINGLE_FILE;
    }

    public void setProgressBar(ProgressBar progressBar2) {
        this.progressBar = progressBar2;
    }

    public boolean isMod() {
        return this.isMod;
    }

    public void setMod(boolean mod) {
        this.isMod = mod;
    }

    public ImageView getImgView() {
        return this.imgView;
    }

    public void setImgView(ImageView imgView2) {
        this.imgView = imgView2;
    }

    public BarChart getBarHistogram() {
        return this.barHistogram;
    }

    public void setBarHistogram(BarChart barHistogram2) {
        this.barHistogram = barHistogram2;
    }

    public int getScaleLimit() {
        return this.scaleLimit;
    }

    public void setScaleLimit(int scaleLimit2) {
        this.scaleLimit = scaleLimit2;
    }

    public boolean isScale() {
        return this.scale;
    }

    public void setScale(boolean scale2) {
        this.scale = scale2;
    }

    public int getScaleSize() {
        return this.scaleSize;
    }

    public void setScaleSize(int scaleSize2) {
        this.scaleSize = scaleSize2;
    }

    public Bitmap getBmp() {
        return this.bmp;
    }

    /**
     * Set bmp and scale it with scale.size
     * @param bmp2 bmp to set
     */
    public void setBmp(Bitmap bmp2) {
        int width = bmp2.getWidth();
        int i = this.scaleSize;
        if (width > i && this.scale) {
            bmp2 = scaleToFitWidth(bmp2, i);
        }
        this.bmp = bmp2;
        modBmpInit();
    }

    public boolean isIsmod() {
        return this.isMod;
    }

    public void setIsmod(boolean ismod) {
        this.isMod = ismod;
    }

    public ImageView getImgview() {
        return this.imgView;
    }

    public void setImgview(ImageView imgview) {
        this.imgView = imgview;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /*********************  BEGIN OF MENU HANDLER **********************/

    public boolean onOptionsItemSelected(MenuItem item) {
        /*** Filters objects initialization ***/


        boolean response = menuHandler.menuHandlerSwitch(item);
        if (!response){
            return super.onOptionsItemSelected(item);
        }

        return response;



    }


    /*********************  END OF MENU HANDLER **********************/

    /**
     * Initializes async task to display a popup with histogram chosen by user
     * @param bmp2 Image to show histogram
     * @param rgb Boolean to show a RGB histogram
     * @param rgbhand rgbHand 0 = red; 1 = green; 2 = blue; 3 = rgb
     */
    public void histoDialog(Bitmap bmp2, boolean rgb, int rgbhand) {
        HistogramBarHelper histoBuilder = new HistogramBarHelper(getProgressBar());
        AsyncTask task = histoBuilder.startHistoTask(getModBmp(), rgb, rgbhand);
        AlertDialog dialog = new Builder(this).setView(R.layout.histowindow).create();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        BarData data = null;
        dialog.show();
        BarChart histogramBarChart = (BarChart) dialog.findViewById(R.id.histoGraph);
        try {
            data = (BarData) task.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        histoBuilder.setDataToBarChart(data, histogramBarChart);
        Objects.requireNonNull(dialog.getWindow()).setLayout(width, (int) (((double) height) * 0.5d));
    }

    /**
     * Load image from internal storage and used to change from MainActivity to ColorizeActivity
     * @param reqCode request code to identify user's choice
     * @param resultCode result to load image
     * @param data Event given by user to display something
     */
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == REQUEST_GET_SINGLE_FILE) {
            if (resultCode == RESULT_OK) {
                try {
                    LoadImageTask(data.getData(), this);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        }
        if (reqCode == COLORIZE_PIC) {
            getImgview().setImageBitmap(getModBmp());
        }
    }



    public void onCreate(Bundle savedInstanceState) {

        /** Init mainactivity **/
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        setBmpHelper(new BitmapHelper(this, getScaleLimit()));

        ImageView image = (ImageView) findViewById(R.id.imageView);
        this.progressBar = (ProgressBar) findViewById(R.id.progress_circular);

        /********************** LOAD IMAGE FROM GALLERY HERE ***************************/
        setBmp(this.bmpHelper.decodeAndScaleBitmapFromResId(R.drawable.lenna));
        /********************** LOAD IMAGE FROM GALLERY HERE ***************************/


        setImgview(image);
        setIsmod(false);
        image.setImageBitmap(getModBmp());
        //modBmpInit();

        BitmapTransfer.getInstance().setContext(this);
        this.menuHandler = new MenuHandler();
    }

    /**Async Task LoadImage**/

    public void LoadImageTask(Uri uri, MainActivity activity) {
        LoadImageTask task = new LoadImageTask(activity);
        task.execute(uri);
    }

    private static class LoadImageTask extends AsyncTask<Uri, Void, Bitmap> {
        private WeakReference<MainActivity> activityWeakReference;

        public LoadImageTask(MainActivity activity) {
            this.activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(Uri... uris) {
            MainActivity activity = activityWeakReference.get();


            return activity.getBmpHelper().decodeAndScaleBitmapFromUri(uris[0]);


        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            super.onPostExecute(bmp);
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.progressBar.setVisibility(View.INVISIBLE);
            activity.setBmp(bmp);
            activity.imgView.setImageBitmap(activity.getModBmp());
            //activity.getHistogramBarHelper().updateHistogram(true);

        }

    }
    /**End of Async Task declaration **/
}

