package com.example.ColorStudio.filters;


import android.content.Context;
import android.graphics.Bitmap;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.example.ColorStudio.util.Conversion;
import com.example.ColorStudio.ScriptC_grey;
import com.example.ColorStudio.ScriptC_keep_one_color_and_colorize;

import static com.example.ColorStudio.util.Conversion.*;


/**
 * Principal module which contains all the image processing functions
 */
public class Effects {
    private Context context;

    public Effects() {
    }

    public Effects(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    /**
     * Applies a random hue to an image using HSV space color.
     *
     * @param bmp Image to modify
     * @return copy of bmp modified
     */
    public Bitmap colorize(Bitmap bmp, float hue) {



        //hue = (float) (Math.random() * 360);
        int width, height, index;
        width = bmp.getWidth();
        height = bmp.getHeight();
        int[] pixels = new int[height * width];
        float[] hsv = new float[3];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (index = 0; index < width * height; index++) {
            rgbToHSV(pixels[index], hsv); //Modifies hsv array and stores pixel in hsv array
            hsv[0] = hue;
            pixels[index] = hsvToRGB(hsv);

        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);

        return bmp;

    }

    public Bitmap colorizeRS(Bitmap bmp, float hue) {



        // 1) Creer un contexte RenderScript
        RenderScript rs = RenderScript.create(getContext());
        // 2) Creer des Allocations pour passer les donnees
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());
        // 3) Creer le script
        ScriptC_keep_one_color_and_colorize colorize = new ScriptC_keep_one_color_and_colorize(rs);
        // 4) Copier les donnees dans les Allocations
        // ...// 5) Initialiser les variables globales potentielles

        colorize.set_rnd_hue(hue);
        // ...
        // 6) Lancer le noyau
        colorize.forEach_colorizerand(input, output);
        // 7) Recuperer les donnees des Allocation (s)
        output.copyTo(bmp);
        // 😎 Detruire le context , les Allocation (s) et le script
        input.destroy();
        output.destroy();
        colorize.destroy();
        rs.destroy();




        return bmp;

    }


    /**
     * Takes an bmp image as parameter and turn image to gray except the pixels which are equals to the int color parameter,
     * function passes images to HSV to identify more easily the colors and keeps in gray the pixels which a different color than passed in parameter
     *
     * @param bmp   Image to modify
     * @param color Color to keep in the image
     * @return Copy of bmp modified
     */
    public Bitmap keepOneColor(Bitmap bmp, int color, int range) {




        float[] hsvRange = new float[3];
        int r, g, b, gray;
        //int range = 60;
        rgbToHSV(color, hsvRange); // Convert color passed as parameter
        //hsvRange[0] = hsvRange[0] - (hsvRange[0] % 60); //Getting %60 to place ourselves in the right HSV angle
        int width, height, index;
        width = bmp.getWidth();
        height = bmp.getHeight();
        int[] pixels = new int[height * width];
        float[] hsv = new float[3];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (index = 0; index < width * height; index++) {
            rgbToHSV(pixels[index], hsv); //Modifies hsv array and stores pixel in hsv array
            if ((hsv[0] <= hsvRange[0] + range && hsv[0] >= hsvRange[0] - range)) { // hsv[2] = value, tolerance.
                pixels[index] = hsvToRGB(hsv); //Keep color
            } else {


                pixels[index] = Conversion.toGray(pixels[index]); //To gray
            }

        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);




        return bmp;
    }

    public Bitmap keepOneColorRS(Bitmap bmp, int color, int range) {



        // 1) Creer un contexte RenderScript
        RenderScript rs = RenderScript.create(getContext());
        // 2) Creer des Allocations pour passer les donnees
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());
        // 3) Creer le script
        ScriptC_keep_one_color_and_colorize keeponecolor = new ScriptC_keep_one_color_and_colorize(rs);
        // 4) Copier les donnees dans les Allocations
        // ...// 5) Initialiser les variables globales potentielles
        float[] hsv = new float[3];
        rgbToHSV(color, hsv);
        keeponecolor.set_H_COLOR(hsv[0]);
        keeponecolor.set_S_COLOR(hsv[1]);
        keeponecolor.set_V_COLOR(hsv[2]);
        keeponecolor.set_RANGE(range);
        // ...
        // 6) Lancer le noyau
        keeponecolor.forEach_keeponecolor(input, output);
        // 7) Recuperer les donnees des Allocation (s)
        output.copyTo(bmp);
        // 😎 Detruire le context , les Allocation (s) et le script
        input.destroy();
        output.destroy();
        keeponecolor.destroy();
        rs.destroy();



        return bmp;

    }

    /**
     * Convert a colored image into a grayscaled one using a int array, much more faster than previous version that modifies pixel by pixel
     *
     * @param bmp Image to modify
     * @return A new grayscale image
     */

    public Bitmap toGray(Bitmap bmp) { //Much must faster because we are treating the image as an int array



        int r, g, b, width, height, index;
        width = bmp.getWidth();
        height = bmp.getHeight();
        int[] pixels = new int[height * width];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (index = 0; index < width * height; index++) {

            pixels[index] = Conversion.toGray(pixels[index]);
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);



        return bmp;

    }

    /**
     * Convert a colored image into a grayscaled one using render script kernel, much more faster than previous version
     *
     * @param bmp Image to modify
     * @return bmp at grayscale
     */


    public Bitmap toGrayRS(Bitmap bmp) {



        // 1) Creer un contexte RenderScript
        RenderScript rs = RenderScript.create(getContext());
        // 2) Creer des Allocations pour passer les donnees
        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());
        // 3) Creer le script
        ScriptC_grey grey = new ScriptC_grey(rs);
        // 4) Copier les donnees dans les Allocations
        // ...// 5) Initialiser les variables globales potentielles
        // ...
        // 6) Lancer le noyau
        grey.forEach_grey(input, output);
        // 7) Recuperer les donnees des Allocation (s)
        output.copyTo(bmp);
        // 😎 Detruire le context , les Allocation (s) et le script
        input.destroy();
        output.destroy();
        grey.destroy();
        rs.destroy();



        return bmp;
    }












}