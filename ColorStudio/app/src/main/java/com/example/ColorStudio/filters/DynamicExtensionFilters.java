package com.example.ColorStudio.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Short2;

import com.example.ColorStudio.util.Conversion;
import com.example.ColorStudio.ScriptC_computeLut;
import com.example.ColorStudio.ScriptC_findMinMax;
import com.example.ColorStudio.ScriptC_grey;

import static com.example.ColorStudio.util.Conversion.*;

public class DynamicExtensionFilters extends Effects {

    public DynamicExtensionFilters(Context context) {
        super(context);
    }


    /**
     * Modifies contrast on a grayscale image by dynamic linear extension.
     * Increase with the dynamic linear extension formula Source: http://dept-info.labri.fr/~vialard/ANDROID/TraitementImage.pdf
     * Decrease with the inverse formula.
     *
     * @param bmp      Input image to modify contrast
     * @param decrease Boolean to indicate if you want to decrease or increase contrast.
     *                 True = Decrease contrast
     *                 False = Increase Contrast
     * @return @param bmp with modifications
     */

    public Bitmap grayContrastModifier(Bitmap bmp, boolean decrease) { //If decrease true then decrease histogram

        double startTime = System.currentTimeMillis();


        Bitmap imax = toGray(bmp);
        int gray, width, height;
        width = imax.getWidth();
        height = imax.getHeight();
        int[] pixels = new int[height * width];
        int[] histogram = new int[256];
        int[] minMax = {0, 0}; //Case 0 == Min // Case 1 == Max
        imax.getPixels(pixels, 0, width, 0, 0, width, height);
        Conversion.grayHistogram(pixels, histogram, minMax, width * height);
        if(minMax[0] == minMax[1]){
            return bmp;
        }
        int[] LUT = new int[256]; //Look Up Table initializer
        for (int ng = 0; ng < 256; ng++) {
            if (!decrease) {
                LUT[ng] = ((255 * (ng - minMax[0])) / (minMax[1] - minMax[0]));
            } else {
                LUT[ng] = ((ng * (minMax[1] - minMax[0])) / 255) + minMax[0];
            }
        }

        for (int index = 0; index < width * height; index++) {
            gray = LUT[(pixels[index] >> 16) & 0xff];
            pixels[index] = ((0xff) << 24) | ((gray & 0xff) << 16) | ((gray & 0xff) << 8) | (gray & 0xff);
        }
        imax.setPixels(pixels, 0, width, 0, 0, width, height);
        double difference = System.currentTimeMillis() - startTime;
        String nameOfMethod = new Throwable()
                .getStackTrace()[0]
                .getMethodName();

        Log.e("TIME TOOK " + nameOfMethod, String.valueOf(difference) + "Ms");


        return imax;


    }

    /**
     * Modifies contrast on a grayscale image by dynamic linear extension.
     * Increase with the dynamic linear extension formula Source: http://dept-info.labri.fr/~vialard/ANDROID/TraitementImage.pdf
     * Decrease with the inverse formula. Using RS reduction kernel to calculate min and max values
     *
     *
     *
     * @param bmp      Input image to modify contrast
     * @param decrease Boolean to indicate if you want to decrease or increase contrast.
     *                 True = Decrease contrast
     *                 False = Increase Contrast
     * @return bmp Copy with modifications
     */
    public Bitmap grayContrastModifierRS(Bitmap bmp, boolean decrease) {


        RenderScript rs = RenderScript.create(getContext()); //Create base renderscript

        Allocation input = Allocation.createFromBitmap(rs, bmp); //Bitmap input
        Allocation output = Allocation.createTyped(rs, input.getType(), Allocation.USAGE_SCRIPT ); //Bitmap output

        ScriptC_grey grey = new ScriptC_grey(rs); //Script for switching to gray
        ScriptC_findMinMax findMM = new ScriptC_findMinMax(rs); //Script for find min and max using reduction kernel

        grey.forEach_grey(input, output); //Switch to gray

        grey.destroy();

        Short2[] minMax; //Allocating memory for calculate min and max
        findMM.set_GRAYONLY(true);
        minMax = findMM.reduce_findMinMax(output).get(); //Get result

        findMM.destroy();

        if (minMax[0].x == minMax[0].y) //Exit if only one color
            return bmp;

        ScriptC_computeLut lut = new ScriptC_computeLut(rs);    //Create lut script

        lut.set_minMaxGray(minMax[0]); //Set min and max

        lut.set_DECREASE(decrease);
        lut.invoke_processGray(output,input);


        //Keep only one chann
        input.copyTo(bmp);
        // 😎 Detruire le context , les Allocation (s) et le script
        input.destroy();
        output.destroy();

        rs.destroy();
        lut.destroy();




        return bmp;
    }


    /**
     * Modifies contrast by dynamic linear extension and passing through RGB space color separately.
     * Increase with the dynamic linear extension formula Source: http://dept-info.labri.fr/~vialard/ANDROID/TraitementImage.pdf
     * Decrease with the inverse formula.
     *
     * @param bmp      Input image to modify contrast
     * @param decrease Boolean to indicate if you want to decrease or increase contrast.
     *                 True = Decrease contrast
     *                 False = Increase Contrast
     * @return bmp modified
     * */


    public Bitmap rgbContrastModifier(Bitmap bmp, boolean decrease) {


        int width, height, r, g, b;
        width = bmp.getWidth();
        height = bmp.getHeight();
        //Allocate Memory
        int[] pixels = new int[height * width];
        int[] minMax = {0, 0, 0, 0, 0, 0}; //Case 0 == Min // Case 1 == Max
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        Conversion.rgbMinMax(pixels, minMax, width * height); //Compute rgb histogram
        if(minMax[0] == minMax[1] || minMax[2] == minMax[3] || minMax[4] == minMax[5]){
            return bmp;
        }
        //LUT for every color space
        int[] LUTr = new int[256]; //Look Up Table initializer
        int[] LUTg = new int[256];
        int[] LUTb = new int[256];
        for (int ng = 0; ng < 256; ng++) {
            if (!decrease) {
                LUTr[ng] = ((255 * (ng - minMax[0])) / (minMax[1] - minMax[0]));
                LUTg[ng] = ((255 * (ng - minMax[2])) / (minMax[3] - minMax[2]));
                LUTb[ng] = ((255 * (ng - minMax[4])) / (minMax[5] - minMax[4]));
            } else {
                LUTr[ng] = ((ng * (minMax[1] - minMax[0])) / 255) + minMax[0];
                LUTg[ng] = ((ng * (minMax[3] - minMax[2])) / 255) + minMax[2];
                LUTb[ng] = ((ng * (minMax[5] - minMax[4])) / 255) + minMax[4];
            }
        }

        for (int index = 0; index < width * height; index++) {
            r = LUTr[(pixels[index] >> 16) & 0xff];
            g = LUTg[(pixels[index] >> 8) & 0xff];
            b = LUTb[(pixels[index]) & 0xff];
            pixels[index] = ((0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);



        return bmp;
    }


    /**
     * Modifies contrast by dynamic linear extension and passing through RGB space color separately.
     * Increase with the dynamic linear extension formula Source: http://dept-info.labri.fr/~vialard/ANDROID/TraitementImage.pdf
     * Decrease with the inverse formula. RS implementation using reduction kernel to compute min and max values of the image
     *
     * @param bmp      Input image to modify contrast
     * @param decrease Boolean to indicate if you want to decrease or increase contrast.
     *                 True = Decrease contrast
     *                 False = Increase Contrast
     * @return bmp modified
     * */

    public Bitmap rgbContrastModifierRS(Bitmap bmp, boolean decrease) {



        RenderScript rs = RenderScript.create(getContext()); //Create base renderscript

        ScriptC_findMinMax findMM = new ScriptC_findMinMax(rs); //Script for find min and max using reduction kernel
        ScriptC_computeLut lut = new ScriptC_computeLut(rs);  //Create lut script

        Allocation input = Allocation.createFromBitmap(rs, bmp); //Bitmap input
        Allocation output = Allocation.createTyped(rs, input.getType(), Allocation.USAGE_SCRIPT ); //Bitmap output


        Short2[] minMax; //Allocating memory for calculate min and max
        findMM.set_GRAYONLY(false);

        minMax = findMM.reduce_findMinMax(input).get(); //Get result


        if (minMax[0].x == minMax[0].y || minMax[1].x == minMax[1].y ||
                minMax[2].x == minMax[2].y) //Exit if only one color
            return bmp;



        lut.set_minMaxRGB(minMax); //Set min and max

        lut.set_DECREASE(decrease);

        lut.invoke_processRGB(input,output);


        output.copyTo(bmp);
        // 😎 Detruire le context , les Allocation (s) et le script
        input.destroy();
        output.destroy();
        rs.destroy();
        lut.destroy();
        findMM.destroy();





        return bmp;

    }



    /**
     * Modifies contrast by dynamic linear extension and passing through HSV space color.
     * Increase with the dynamic linear extension formula Source: http://dept-info.labri.fr/~vialard/ANDROID/TraitementImage.pdf
     * Decrease with the inverse formula.
     *
     * @param bmp      Input image to modify contrast
     * @param decrease Boolean to indicate if you want to decrease or increase contrast.
     *                 True = Decrease contrast
     *                 False = Increase Contrast
     * @return bmp Copy with modifications
     */
    public Bitmap hsvContrastModifier(Bitmap bmp, boolean decrease) {

        int width, height, r, g, b, value;

        //Getting image size dimensions
        width = bmp.getWidth();
        height = bmp.getHeight();

        //Allocate memory
        float[] hsv = new float[3];
        int[] pixels = new int[height * width];
        int[] minMax = {255, 0};
        int[] LUT = new int[256];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height); // Getting pixels
        for (int index = 0; index < width * height; index++) { //First turn to HSV
            //Calculate value
            r = (pixels[index] >> 16) & 0xff;
            g = (pixels[index] >> 8) & 0xff;
            b = (pixels[index]) & 0xff;
            value = r > g ? r : g;
            value = value > b ? value : b;
            //Compute Histogram
            if (value >= minMax[1]) {
                minMax[1] = value;
            } else if (value <= minMax[0]) {
                minMax[0] = value;
            }
        }
        if(minMax[0] == minMax[1]){
            return bmp;
        }

        for (int ng = 0; ng < 256; ng++) {
            if (!decrease) {
                LUT[ng] = ((255 * (ng - minMax[0])) / (minMax[1] - minMax[0])); //Dynamic linear extension formula
            } else {
                LUT[ng] = ((ng * (minMax[1] - minMax[0])) / 255) + minMax[0]; //Inverse of above formula
            }
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


  public Bitmap hsvContrastModifierRS(Bitmap bmp, boolean decrease) {


      RenderScript rs = RenderScript.create(getContext()); //Create base renderscript

        ScriptC_findMinMax findMM = new ScriptC_findMinMax(rs); //Script for find min and max using reduction kernel
        ScriptC_computeLut lut = new ScriptC_computeLut(rs);  //Create lut script

        Allocation input = Allocation.createFromBitmap(rs, bmp); //Bitmap input
        Allocation output = Allocation.createTyped(rs, input.getType(), Allocation.USAGE_SCRIPT ); //Bitmap output


      Short2[] minMax; //Allocating memory for calculate min and max
        findMM.set_GRAYONLY(false);
        findMM.set_HSV_MODE(true);

        minMax = findMM.reduce_findMinMax(input).get(); //Get result


        if (minMax[0].x == minMax[0].y)
            return bmp;



        lut.set_minMaxGray(minMax[0]); //Set min and max

        lut.set_DECREASE(decrease);

        lut.invoke_processHSV(input,output);


        output.copyTo(bmp);
        // 😎 Detruire le context , les Allocation (s) et le script
        input.destroy();
        output.destroy();
        rs.destroy();
        lut.destroy();
        findMM.destroy();




        return bmp;

    }


}
