package com.example.ColorStudio.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.internal.view.SupportMenu;

import com.example.ColorStudio.bitmap.BitmapTransfer;
import com.example.ColorStudio.filters.Effects;
import com.example.ColorStudio.util.Conversion;
import com.example.colortogray.R;

import java.util.Objects;

/**
 * Color tweaks activity, let user to set a hue to the image and keep one color in the image.
 * Distance : Tolerance value
 * Hue : hue to apply
 * Context : Reference to MainActivity class
 */

public class ColorizeActivity extends AppCompatActivity {
    private int distance;
    private float hue;
    private ImageView imageViewColorize;
    private Button colorizeButton;
    private SeekBar distanceSeekBar;
    private SeekBar hueSeekbar;
    private Button keepColorButton;
    private MainActivity context;

    /** Getters and setters **/
    public MainActivity getContext() {
        return context;
    }

    public void setContext(MainActivity context) {
        this.context = context;
    }

    public SeekBar getHueSeekbar() {
        return this.hueSeekbar;
    }

    public void setHueSeekbar(SeekBar hueSeekbar2) {
        this.hueSeekbar = hueSeekbar2;
    }

    public ImageView getImageViewColorize() {
        return this.imageViewColorize;
    }

    public void setImageViewColorize(ImageView image2) {
        this.imageViewColorize = image2;
    }


    public float getHue() {
        return this.hue;
    }

    public void setHue(float hue2) {
        this.hue = hue2;
    }

    public SeekBar getDistanceSeekBar() {
        return this.distanceSeekBar;
    }

    public void setDistanceSeekBar(SeekBar distanceSeekBar2) {
        this.distanceSeekBar = distanceSeekBar2;
    }

    public Button getKeepColorButton() {
        return this.keepColorButton;
    }

    public void setKeepColorButton(Button keepColorButton2) {
        this.keepColorButton = keepColorButton2;
    }

    public Button getColorizeButton() {
        return this.colorizeButton;
    }

    public void setColorizeButton(Button colorizeButton2) {
        this.colorizeButton = colorizeButton2;
    }

    public int getDistance() {
        return this.distance;
    }

    public void setDistance(int distance2) {
        this.distance = distance2;
    }

    @Override
    /**Menu inflater **/
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.colorize_menu, menu);
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get MainActivity Referece
        setContext(BitmapTransfer.getInstance().getContext());

        //Initialize filters object
        final Effects filters = new Effects(this);
        /***Charging layouts ***/


        //Loading activity layout
        setContentView(R.layout.activity_colorize);




        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setDistanceSeekBar((SeekBar) findViewById(R.id.seekBarDistance));
        setKeepColorButton((Button) findViewById(R.id.keepColorButton));
        setHueSeekbar((SeekBar) findViewById(R.id.hueSeekBar));
        setImageViewColorize((ImageView) findViewById(R.id.colorizeView));
        setColorizeButton((Button) findViewById(R.id.colorizeButton));
        final ImageView rgbSquare = findViewById(R.id.hueSquare);

        /*** Set distance bar values ***/
        getDistanceSeekBar().setMax(180);
        getDistanceSeekBar().setProgress(90);
        getHueSeekbar().setMax(359);



        //Setting image from MainActivity to ImageView
        getImageViewColorize().setImageBitmap(context.getModBmp());
        getImageViewColorize().setRotation(BitmapTransfer.getInstance().getViewRotation());
        rgbSquare.setColorFilter(SupportMenu.CATEGORY_MASK);

        //DistanceSeekBar listener
        this.distanceSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ColorizeActivity.this.setDistance(progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.hueSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ColorizeActivity.this.setHue((float) progress);
                rgbSquare.setColorFilter(Conversion.hsvToRGB(new float[]{getHue(), 1.0f, 1.0f}));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.keepColorButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                    Bitmap bmp;
                    if (getContext().isRenderScript()) {
                        bmp = filters.keepOneColorRS(context.getModBmp(), (int) getHue(), getDistance());
                        getImageViewColorize().setImageBitmap(bmp);
                        BitmapTransfer.getInstance().getContext().setIsmod(true);
                        context.setModBmp(bmp);
                    } else {
                        bmp = filters.keepOneColor(context.getModBmp(), (int) hue, distance);
                        getImageViewColorize().setImageBitmap(bmp);
                        BitmapTransfer.getInstance().getContext().setIsmod(true);
                        context.setModBmp(bmp);

                }
            }
        });
        this.colorizeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                    Bitmap bmp;
                    if (getContext().isRenderScript()) {
                        bmp = filters.colorizeRS(context.getModBmp(), hue);
                        getImageViewColorize().setImageBitmap(bmp);
                        BitmapTransfer.getInstance().getContext().setIsmod(true);
                        context.setModBmp(bmp);
                    } else {
                        bmp = filters.colorize(context.getModBmp(), hue);
                        getImageViewColorize().setImageBitmap(bmp);
                        BitmapTransfer.getInstance().getContext().setIsmod(true);
                        context.setModBmp(bmp);
                    }

            }
        });
    }


    /***  Item menu handler from colorize activity***/
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                //BitmapTransfer.getInstance().getContext().setModBmp(getModBmp());
                finish();
                return true;

            case (R.id.UndoChangesItem):
                BitmapTransfer.getInstance().getContext().onOptionsItemSelected(item);
                getImageViewColorize().setImageBitmap(context.getBmp());
                return true;

            default:
                return super.onOptionsItemSelected(item);


        }
    }
}
