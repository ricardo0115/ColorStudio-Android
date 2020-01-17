package com.example.ColorStudio.util;

public class Conversion {


    public static float max(float a, float b) {
        return (a >= b) ? a : b;
    }

    public static float min(float a, float b) {
        return (a <= b) ? a : b;
    }


    /**
     * Computes a HSV version of RGB color passed in parameter and stores it in float [] hsvArray passed as parameter
     * Source for conversion: https://en.wikipedia.org/wiki/HSL_and_HSV
     *
     * @param color RGB int color to convert.
     * @param hsv   HSV array to store the result.
     */
    public static void rgbToHSV(int color, float[] hsv) {
        if (hsv.length != 3) {
            throw new IllegalArgumentException
                    ("Hsv array must be of size 3");
        }
        float red, green, blue;
        red = (((color >> 16) & 0xff));
        green = (((color >> 8) & 0xff));
        blue = (((color) & 0xff));
        float cmax, cmin, delta, saturation, value, hue = 0;
        //Normalize colors in 0 to 255 range

        red = (float) (red / 255.0);
        green = (float) (green / 255.0);
        blue = (float) (blue / 255.0);


        cmin = red < green ? red : green;
        cmin = cmin < blue ? cmin : blue;

        cmax = red > green ? red : green;
        cmax = cmax > blue ? cmax : blue;

        delta = cmax - cmin;
        if (cmax == 0) { //0 case we just put 0 in all cases of the array
            for (int i = 0; i < 3; i++) {
                hsv[i] = 0;
            }
            return;

            //Hue calculation by formula
        } else if (cmax == red) {
            hue = (float) (60.0 * (((green - blue) / delta)));

        } else if (cmax == green) {

            hue = (float) (60.0 * (((blue - red) / delta) + 2));

        } else if (cmax == blue) {
            hue = (float) (60.0 * (((red - green) / delta) + 4));
        }
        hsv[0] = hue;

        saturation = delta / cmax;

        value = (cmax);

        hsv[1] = saturation;

        hsv[2] = value;

    }

    /**
     * Convert param color into a grayscale pixel.
     * @param color
     * @return
     */

    public static int toGray (int color){
        int r,g,b, gray;
        r = (color >> 16) & 0xff;
        g = (color >> 8) & 0xff;
        b = (color) & 0xff;
        gray = (r*30 + g*59 + b*11) / 100;
        return ((0xff) << 24) | ((gray & 0xff) << 16) | (gray & 0xff) << 8 | (gray & 0xff); //To gray

    }

    /**
     * Computes a conversion of a HSV float array hsv[3] = {float hue [0..1], float saturation [0..1], float value [0..1] } to
     * an RGB int. Using alternative formula conversion. Source: https://en.wikipedia.org/wiki/HSL_and_HSV
     *
     * @param hsv HSV Array as float hsv[] = {float hue [0..1], float saturation [0..1], float value [0..1] }
     * @return RGB int color
     */
    public static int hsvToRGB(float[] hsv) {
        if (hsv.length != 3) {
            throw new IllegalArgumentException
                    ("Hsv/l array must be of size 3");
        }
        float r, g, b, k;
        k = (float) (5 + (hsv[0] / 60.0)) % 6;
        r = (int) ((hsv[2] - hsv[2] * hsv[1] * max(min(min(k, 4 - k), 1), 0)) * 255.0);

        k = (float) (3 + (hsv[0] / 60.0)) % 6;
        g = (int) ((hsv[2] - hsv[2] * hsv[1] * max(min(min(k, 4 - k), 1), 0)) * 255.0);

        k = (float) (1 + (hsv[0] / 60.0)) % 6;

        b = (int) ((hsv[2] - hsv[2] * hsv[1] * max(min(min(k, 4 - k), 1), 0)) * 255.0);
        return (0xff) << 24 | ((int) r & 0xff) << 16 | ((int) g & 0xff) << 8 | ((int) b & 0xff);


    }


    /**
     * Computes min and max values of each rgb channel of a @param image, and stores it in array minMax.
     *
     * @param image  Image to analyse
     * @param minMax minMax[0] = minRed value | minMax [1] = maxRed value | minMax[2] = minGreen value | minMax[3] = maxGreen value | minMax[4] = minBlue value| minMax[5] = maxBlue value
     * @param imsize size of image.
     */
    public static void rgbMinMax(int[] image, int[] minMax, int imsize) { //Case 0 min, case 1 max
        int r, g, b;
        minMax[0] = 255;
        minMax[1] = 0;
        minMax[2] = 255;
        minMax[3] = 0;
        minMax[4] = 255;
        minMax[5] = 0;

        for (int index = 0; index < imsize; index++) {
            r = (image[index] >> 16) & 0xff;
            g = (image[index] >> 8) & 0xff;
            b = (image[index]) & 0xff;
            if ((r >= minMax[1])) {
                minMax[1] = r;
            } else if (r <= minMax[0]) {
                minMax[0] = r;
            }
            if ((g >= minMax[3])) {
                minMax[3] = g;
            } else if (g <= minMax[2]) {
                minMax[2] = g;
            }
            if ((b >= minMax[5])) {
                minMax[5] = b;
            } else if (b <= minMax[4]) {
                minMax[4] = b;
            }
        }

    }

    /**
     * Computes histogram of a grayscale image.
     *
     * @param image  Input image encoded as RGB 8 bit.
     * @param hist   Histogram pointer to store the computed histogram
     * @param minMax array to indicate minimum and maximum values minMax[0] = Min and minMax[1] = 1
     * @param imsize size of image
     */
    public static void grayHistogram(int[] image, int[] hist, int[] minMax, int imsize) { //Case 0 min, case 1 max
        int gray;
        minMax[0] = 255;
        minMax[1] = 0;
        for (int index = 0; index < imsize; index++) {
            gray = (image[index] >> 16) & 0xff;
            if ((gray >= minMax[1])) {
                minMax[1] = gray;
            } else if (gray <= minMax[0]) {
                minMax[0] = gray;
            }
            hist[gray]++;
        }

    }



}
