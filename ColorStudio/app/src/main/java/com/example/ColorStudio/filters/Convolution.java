package com.example.ColorStudio.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.example.ColorStudio.filters.Effects;

public class Convolution extends Effects {

    public Convolution(Context context) {
        super(context);
    }

    /**
     * Applies kernel passed as parameter with kernelWidth and kernelHeight parameter and applies in only one or 3 color channels of Bitmap bmp image passed as parameter
     * @param bmp image
     * @param kernel kernel to apply
     * @param kernelWidth kernel width
     * @param kernelHeight kernel height
     * @param color true if you want to apply in all 3 RGB channels
     * @return
     */
    public Bitmap convolve2D(Bitmap bmp, float[] kernel, int kernelWidth, int kernelHeight, boolean color) {

        Bitmap imax;
        if (!color)
            imax = toGrayRS(bmp); //Get gray
        else
            imax = bmp;
        //Variables declarations and initialization
        int kCenterX, kCenterY, rowMin, rowMax, colMin, colMax, i, j, m, n, inIndex1, inIndex2, outIndex, width, height, kIndex, gray, r, g ,b ;
        float minGray, maxGray, minR, minG, minB, maxR, maxG, maxB, sumGray = 0, sumR = 0, sumG = 0, sumB = 0;
        minGray = minR = minG = minB = Float.MAX_VALUE;
        maxGray = maxR = maxG = maxB = Float.MIN_VALUE;

        kCenterX = kernelWidth / 2;
        kCenterY = kernelHeight / 2;

        width = imax.getWidth();
        height = imax.getHeight();
        float[] pixelsOutGray = null; float[] pixelsOutR = null; float[] pixelsOutG = null; float[] pixelsOutB = null;
        int[] pixelsIn = new int[height * width];
        if(!color) {
            pixelsOutGray = new float[height * width];
        }else{
            pixelsOutR = new float[height * width];
            pixelsOutG = new float[height * width];
            pixelsOutB = new float[height * width];
        }

        imax.getPixels(pixelsIn, 0, width, 0, 0, width, height);

        inIndex1 = inIndex2 = width * kCenterY + kCenterX;
        outIndex = 0;

        kIndex = 0;

        for (i = 0; i < height; ++i) // Rows
        {

            //Convolution range
            rowMax = i + kCenterY;
            rowMin = i - height + kCenterY;


            for (j = 0; j < width; ++j) //Columns

            {

                //Convolution range
                colMax = j + kCenterX;
                colMin = j - width + kCenterX;
                if (!color) {
                    sumGray = 0;  //Set to 0
                }else{
                    sumR = sumG = sumB = 0;
                }

                for (m = 0; m < kernelHeight; ++m) { //kRows

                    if (m <= rowMax && m > rowMin) //Check bounds
                    {
                        for (n = 0; n < kernelWidth; ++n) //kColumns
                        {
                            if (n <= colMax && n > colMin)//Check bounds of input
                            {
                                if(!color) {
                                    gray = (pixelsIn[inIndex1 - n] >> 16) & 0xff;
                                    sumGray += gray * kernel[kIndex];
                                }else{
                                    r = (pixelsIn[inIndex1 - n] >> 16) & 0xff;
                                    g = (pixelsIn[inIndex1 - n] >> 8) & 0xff;
                                    b = (pixelsIn[inIndex1 - n]) & 0xff;
                                    sumR += r * kernel[kIndex];
                                    sumG += g * kernel[kIndex];
                                    sumB += b * kernel[kIndex];

                                }
                            }

                            kIndex++; //next kernel index case


                        }
                    } else
                        kIndex += kernelWidth; //Next row of kernel


                    inIndex1 -= width; //Next line in picture

                }

                //Calculate min and max to save time
                if (!color) {
                    if (sumGray <= minGray) {
                        minGray = sumGray;
                    } else if (sumGray >= maxGray) {
                        maxGray = sumGray;
                    }
                    pixelsOutGray[outIndex] = sumGray;
                }else{
                    if (sumR <= minR) {
                        minR = sumR;
                    } else if (sumR >= maxR) {
                        maxR = sumR;
                    }

                    if (sumG <= minG) {
                        minG = sumG;
                    } else if (sumG >= maxG) {
                        maxG = sumG;
                    }

                    if (sumB <= minB) {
                        minB = sumB;
                    } else if (sumB >= maxB) {
                        maxB = sumB;
                    }

                    pixelsOutR[outIndex] = sumR;
                    pixelsOutG[outIndex] = sumG;
                    pixelsOutB[outIndex] = sumB;
                }
                kIndex = 0;

                inIndex1 = ++inIndex2; //Move input index
                ++outIndex; //Move output index


            }
        }
        //Normalization
        float normalizedPixel, normalizedPixelR ,normalizedPixelG, normalizedPixelB;

        if (!color) {
            if (minGray == maxGray) {
                return bmp;
            }
        } else {
            if (minR == maxR || minG == maxG || minB == maxB) {
                return bmp;
            }
        }

        for (int index = 0; index < width * height; ++index) { // Normalize
            if (!color) {
                normalizedPixel = ((pixelsOutGray[index] - minGray) / (maxGray - minGray)) * 255;
                gray = (int) normalizedPixel;
                pixelsIn[index] = ((0xff) << 24) | ((gray & 0xff) << 16) | ((gray & 0xff) << 8) | (gray & 0xff);
            } else {
                normalizedPixelR = ((pixelsOutR[index] - minR) / (maxR - minR)) * 255;
                normalizedPixelG = ((pixelsOutG[index] - minG) / (maxG - minG)) * 255;
                normalizedPixelB = ((pixelsOutB[index] - minB) / (maxB - minB)) * 255;

                pixelsIn[index] = ((0xff) << 24) | (((int) normalizedPixelR & 0xff) << 16) | (((int) normalizedPixelG & 0xff) << 8) | ((int) normalizedPixelB & 0xff);
            }
        }
        imax.setPixels(pixelsIn, 0, width, 0, 0, width, height);



        return imax;

    }



}
