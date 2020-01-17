package com.example.ColorStudio.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.example.ColorStudio.util.Conversion;
import com.example.ColorStudio.ScriptC_computeLut;
import com.example.ColorStudio.ScriptC_grey;
import com.example.ColorStudio.ScriptC_histogram;

import static com.example.ColorStudio.util.Conversion.hsvToRGB;
import static com.example.ColorStudio.util.Conversion.rgbToHSV;

public class EqualizationFilters extends Effects {


    public EqualizationFilters(Context context) {
        super(context);
    }

    /**
     * Increase contrast of an image using histogram equalization method and returns the @param bmp at grayscale with contrast increased
     * Source https://en.wikipedia.org/wiki/Histogram_equalization
     *
     * @param bmp Image
     * @return bmp with grayscale contrast increased
     */

    public Bitmap grayEqualizationContrastAugmentation(Bitmap bmp) {


        Bitmap imax = toGray(bmp);
        int gray, width, height;
        long acc = 0;
        width = imax.getWidth();
        height = imax.getHeight();
        //Allocating memory
        int[] pixels = new int[height * width];
        int[] histogram = new int[256];
        int[] LUT = new int[256];
        int[] minMax = {0, 0}; //Case 0 == Min // Case 1 == Max
        imax.getPixels(pixels, 0, width, 0, 0, width, height);
        Conversion.grayHistogram(pixels, histogram, minMax, width * height); //Compute
        if(minMax[0] == minMax[1]){
            return bmp;
        }
        for (int ng = 0; ng < 256; ng++) {
            acc += histogram[ng]; //Cumulative function

            LUT[ng] = (int) ((acc * 255) / ((width * height) - minMax[0])); //Histogram Equalization formula

        }

        for (int index = 0; index < width * height; index++) {
            gray = LUT[(pixels[index] >> 16) & 0xff];
            pixels[index] = ((0xff) << 24) | ((gray & 0xff) << 16) | ((gray & 0xff) << 8) | (gray & 0xff);
        }
        imax.setPixels(pixels, 0, width, 0, 0, width, height);




        return imax;

    }


    /**
     * Increase contrast of an image using histogram equalization method and returns the @param bmp at grayscale with contrast increased
     * Source https://en.wikipedia.org/wiki/Histogram_equalization
     *
     * @param bmp Image
     * @return bmp with grayscale contrast increased
     */


    public Bitmap grayEqualizationContrastAugmentationRS(Bitmap bmp) {



        RenderScript rs = RenderScript.create(getContext()); //Create base renderscript

        Allocation input = Allocation.createFromBitmap(rs, bmp); //Bitmap input
        Allocation output = Allocation.createTyped(rs, input.getType(), Allocation.USAGE_SCRIPT ); //Bitmap output

        ScriptC_grey grey = new ScriptC_grey(rs); //Script for switching to gray



        grey.forEach_grey(input, output); //Switch to gray

        grey.destroy();

        ScriptC_histogram histoScript = new ScriptC_histogram(rs);

        histoScript.set_GRAY(true);

        histoScript.set_size(bmp.getWidth() * bmp.getHeight());

        short[] lutSingle;

        lutSingle = histoScript.reduce_LUTCumulatedHistogram(output).get(); //Get result

        ScriptC_computeLut lut = new ScriptC_computeLut(rs);

        lut.set_lutSingle(lutSingle);

        lut.forEach_assignLutSingle(output,input);


        //Keep only one chann
        input.copyTo(bmp);
        input.destroy();
        output.destroy();
        lut.destroy();
        histoScript.destroy();
        rs.destroy();




        return bmp;
    }

    /**
     * Increase contrast of @param bmp by histogram equalization method, passing by HSV color space. This functions works better in darker images
     * rather than rgbAverageContrastAugmentation function.     Source https://en.wikipedia.org/wiki/Histogram_equalization
     *
     * @param bmp Image to modify
     * @return bmp with contrast increased.
     */
    public Bitmap hsvEqualizationContrastAugmentation(Bitmap bmp) {



        int width, height, r, g, b;
        int value;
        long acc = 0;
        width = bmp.getWidth();
        height = bmp.getHeight();
        //Allocating memory
        float[] hsv = new float[3];
        int[] pixels = new int[height * width];
        int[] hist = new int[256];
        int[] minMax = {0, 0};
        int[] LUT = new int[256];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int index = 0; index < width * height; index++) { //First turn to HSV
            //Calculate value
            r = (pixels[index] >> 16) & 0xff;
            g = (pixels[index] >> 8) & 0xff;
            b = (pixels[index]) & 0xff;
            value = r > g ? r : g;
            value = value > b ? value : b;
            //Compute histogram
            if (value >= minMax[1]) {
                minMax[1] = value;
            } else if (value <= minMax[0]) {
                minMax[0] = value;
            }
            hist[value]++;

        }

        if(minMax[0] == minMax[1]){
            return bmp;
        }
        for (int ng = 0; ng < 256; ng++) {
            acc += hist[ng]; //Cumulative histogram
            LUT[ng] = (int) ((acc * 255) / ((width * height) - minMax[0])); //Histogram Equalization formula
        }
        for (int index = 0; index < width * height; index++) {
            rgbToHSV(pixels[index], hsv);
            value = LUT[(int) (hsv[2] * 255)];
            hsv[2] = value / 255.0f; // Back to normal
            pixels[index] = hsvToRGB(hsv);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);




        return bmp;

    }
    /**
     * Increase contrast of @param bmp by histogram equalization method, passing by HSV color space. This functions works better in darker images
     * rather than rgbAverageContrastAugmentation function.     Source https://en.wikipedia.org/wiki/Histogram_equalization by renderscript implementation.
     *
     * @param bmp Image to modify
     * @return bmp with contrast increased.
     */


    public Bitmap hsvEqualizationContrastAugmentationRS(Bitmap bmp) {



        RenderScript rs = RenderScript.create(getContext()); //Create base renderscript

        Allocation input = Allocation.createFromBitmap(rs, bmp); //Bitmap input
        Allocation output = Allocation.createTyped(rs, input.getType(), Allocation.USAGE_SCRIPT ); //Bitmap output

        ScriptC_histogram histoScript = new ScriptC_histogram(rs);

        histoScript.set_HSV(true);

        histoScript.set_size(bmp.getWidth() * bmp.getHeight());

        short[] LUThsv;

        LUThsv = histoScript.reduce_LUTCumulatedHistogram(input).get(); //Get result

        histoScript.destroy();

        ScriptC_computeLut lut = new ScriptC_computeLut(rs);

        lut.set_lutSingle(LUThsv);

        lut.forEach_assignLutHSV(input,output);


        //Keep only one chann
        output.copyTo(bmp);
        input.destroy();
        output.destroy();
        lut.destroy();
        rs.destroy();




        return bmp;
    }

    /**
     * Increase contrast of param bmp image using equalization constrast method, calculating an histogram of the 3 RGB color channels and
     * calculating an average of three RGB channels. Source https://hypjudy.github.io/2017/03/19/dip-histogram-equalization/
     * Source https://en.wikipedia.org/wiki/Histogram_equalization
     *
     * @param bmp Image to modify
     * @return bmp  Copy of bmp with higher contrast by histogram equalization method.
     */

    public Bitmap rgbAverageContrastAugmentation(Bitmap bmp) {


        int width, height, r, g, b;
        long acc = 0;
        width = bmp.getWidth();
        height = bmp.getHeight();
        //Alocate memory
        int[] hist = new int[256];
        int[] LUT = new int[256];
        int[] pixels = new int[height * width];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int index = 0; index < width * height; ++index) {
            //Do general Histogram
            r = (pixels[index] >> 16) & 0xff;
            g = (pixels[index] >> 8) & 0xff;
            b = (pixels[index]) & 0xff;
            hist[r]++;
            hist[g]++;
            hist[b]++;
        }
        for (int ng = 0; ng < 256; ng++) {
            hist[ng] /= 3; //Getting average of three RGB channels
            acc += hist[ng]; //Cumulative function
            LUT[ng] = (int) ((acc * 255) / ((width * height))); //Histogram Equalization formula
        }
        //Setting new pixels to image.
        for (int index = 0; index < width * height; index++) {
            r = LUT[(pixels[index] >> 16) & 0xff];
            g = LUT[(pixels[index] >> 8) & 0xff];
            b = LUT[(pixels[index]) & 0xff];
            pixels[index] = ((0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);




        return bmp;
    }

    /**
     * Increase contrast of param bmp image using equalization constrast method, calculating an histogram of the 3 RGB color channels and
     * calculating an average of three RGB channels. Source https://hypjudy.github.io/2017/03/19/dip-histogram-equalization/
     * Source https://en.wikipedia.org/wiki/Histogram_equalization
     *
     * @param bmp Image to modify
     * @return bmp  Copy of bmp with higher contrast by histogram equalization method.
     */



    public Bitmap rgbAverageContrastAugmentationRS(Bitmap bmp) {



        RenderScript rs = RenderScript.create(getContext()); //Create base renderscript

        Allocation input = Allocation.createFromBitmap(rs, bmp); //Bitmap input
        Allocation output = Allocation.createTyped(rs, input.getType(), Allocation.USAGE_SCRIPT ); //Bitmap output

        ScriptC_histogram histoScript = new ScriptC_histogram(rs);

        histoScript.set_RGB(true);

        histoScript.set_size(bmp.getWidth() * bmp.getHeight());

        short[] LUTrgb;

        LUTrgb = histoScript.reduce_LUTCumulatedHistogram(input).get(); //Get result

        histoScript.destroy();

        ScriptC_computeLut lut = new ScriptC_computeLut(rs);

        lut.set_lutSingle(LUTrgb);

        lut.forEach_assignLutRGBAverage(input,output);


        //Keep only one chann
        output.copyTo(bmp);
        input.destroy();
        output.destroy();
        lut.destroy();
        rs.destroy();






        return bmp;
    }

}
