package com.example.ColorStudio.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class HistogramBarHelper {
    private ProgressBar progressBar;

    public HistogramBarHelper(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public HistoTask startHistoTask(Bitmap bmp, boolean rgb, int rgbhand) {
        HistoTask task = new HistoTask(bmp, rgb, rgbhand);
        task.execute();
        return task;
    }


    private class HistoTask extends AsyncTask<Void, Void, BarData> {
        private Bitmap bmp;
        private boolean rgb;
        private int rgbhand;

        public HistoTask(Bitmap bmp, boolean rgb, int rgbhand) {
            this.bmp = bmp;
            this.rgb = rgb;
            this.rgbhand = rgbhand;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getProgressBar().setVisibility(View.VISIBLE);

        }

        @Override
        protected BarData doInBackground(Void... voids) {
            if (this.rgb) {
                return updateHistogram(this.bmp,true, this.rgbhand);
            }else{
                return updateHistogram(this.bmp, false,0);
            }

        }

        @Override
        protected void onPostExecute(BarData res) {
            super.onPostExecute(res);
            getProgressBar().setVisibility(View.INVISIBLE);

        }

    }


    /**
     * Computes an histogram to show bins depending the int rgbHand : 0 for red; 1 for green; 2 for blue; 3 for rgb
     *
     * @param bmp
     * @param rgbHand rgbHand : 0 for red; 1 for green; 2 for blue; 3 for rgb
     */

    public BarData bmpRgbHistogram(Bitmap bmp, int rgbHand) {
        if (rgbHand > 3 || rgbHand < 0) {
            throw new InvalidParameterException();
        }
        int[] histr = new int[256];
        int[] histg = new int[256];
        int[] histb = new int[256];
        int width, height;
        width = bmp.getWidth();
        height = bmp.getHeight();
        int[] minMax = {0, 0};
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int r, g, b;
        for (int index = 0; index < width * height; index++) {
            r = (pixels[index] >> 16) & 0xff;
            g = (pixels[index] >> 8) & 0xff;
            b = (pixels[index]) & 0xff;
            histr[r]++;
            histg[g]++;
            histb[b]++;
        }


        List<BarEntry> red = new ArrayList<BarEntry>();
        List<BarEntry> green = new ArrayList<BarEntry>();
        List<BarEntry> blue = new ArrayList<BarEntry>();

        for (int index = 0; index < 256; ++index) {
            red.add(new BarEntry(index, histr[index]));
            green.add(new BarEntry(index, histg[index]));
            blue.add(new BarEntry(index, histb[index]));
        }


        BarDataSet redBins = new BarDataSet(red, "red");
        redBins.setColor(Color.RED);
        redBins.setValueTextColor(Color.RED);

        BarDataSet greenBins = new BarDataSet(green, "green");
        greenBins.setColor(Color.GREEN);
        greenBins.setValueTextColor(Color.GREEN);

        BarDataSet blueBins = new BarDataSet(blue, "blue");
        blueBins.setColor(Color.BLUE);
        blueBins.setValueTextColor(Color.BLUE);

        List<IBarDataSet> dataSets = new ArrayList<IBarDataSet>(); //I have to pass this ArrayList instead of image through json
        if (rgbHand == 0) {
            dataSets.add(redBins);
        } else if (rgbHand == 1) {
            dataSets.add(greenBins);
        } else if (rgbHand == 2) {
            dataSets.add(blueBins);
        } else {
            dataSets.add(redBins);
            dataSets.add(greenBins);
            dataSets.add(blueBins);
        }



        //BarData barData = new BarData(dataSets);
        //histogram.setData(barData);
        //setBarOptions(histogram);
        return new BarData(dataSets);

    }

    /**
     * Computes an histogram from a grayscale image
     *
     * @param bmp
     */
    public BarData bmpLuminanceHistogram(Bitmap bmp) {
        int[] hist = new int[256];
        int width, height;
        width = bmp.getWidth();
        height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int lum;
        for (int index = 0; index < width * height; index++) {
            lum = (((pixels[index] >> 16) & 0xff) + ((pixels[index] >> 8) & 0xff) + (pixels[index] & 0xff)) / 3;
            hist[lum]++;
        }

        List<BarEntry> entries = new ArrayList<BarEntry>();

        int cpt = 0;
        for (int data : hist) {
            entries.add(new BarEntry(cpt, data));
            ++cpt;
        }


        BarDataSet dataSet = new BarDataSet(entries, "bins");
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE);

        //BarData barData = new BarData(dataSet);
        //histogram.setData(barData);
        //setBarOptions(histogram);

        return new BarData(dataSet);


    }

    /**
     * Updates actual histogram
     *
     * @param color
     */
    public BarData updateHistogram(Bitmap bmp , boolean color, int rgbhand) {
        if (!color) {
           return bmpLuminanceHistogram(bmp);
        } else {
            return bmpRgbHistogram(bmp, rgbhand);
        }
    }

    public void setBarOptions(BarChart histogram){
        histogram.setTouchEnabled(true);
        histogram.setPinchZoom(true);
        histogram.setScaleEnabled(true);
        histogram.getDescription().setEnabled(false);
        histogram.getAxisLeft().setDrawGridLines(false);
        histogram.getXAxis().setDrawGridLines(false);
        histogram.getAxisRight().setDrawGridLines(false);
        histogram.setDrawBorders(false);
        histogram.setDrawGridBackground(false);


        // remove axis
        histogram.getAxisLeft().setEnabled(false);
        //histogram.getAxisRight().setEnabled(false);
        histogram.getAxisRight().setTextColor(Color.WHITE);

        //histogram.getXAxis().setEnabled(false);
        histogram.getXAxis().setTextColor(Color.WHITE);
        histogram.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // hide legend
        histogram.getLegend().setEnabled(false);


        histogram.invalidate();


    }

    public void setDataToBarChart(BarData data, BarChart histoChart){
        histoChart.setData(data);
        setBarOptions(histoChart);
    }




}
