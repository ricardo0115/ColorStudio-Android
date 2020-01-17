package com.example.ColorStudio.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ColorStudio.bitmap.BitmapAsync;
import com.example.ColorStudio.bitmap.BitmapTransfer;
import com.example.ColorStudio.filters.Convolution;
import com.example.ColorStudio.filters.DynamicExtensionFilters;
import com.example.ColorStudio.filters.Effects;
import com.example.ColorStudio.filters.EqualizationFilters;
import com.example.ColorStudio.util.Task;
import com.example.colortogray.R;


/**
 * MenuHandler class, handles the big menu to choice filters
 */
public class MenuHandler {
    private MainActivity context = BitmapTransfer.getInstance().getContext();
    private ImageView mainImageView = context.getImgView();
    private Bitmap modBmp = context.getModBmp();
    private Bitmap bmp = context.getBmp();
    private final Effects filters = new Effects(context);
    private final DynamicExtensionFilters dynFilters = new DynamicExtensionFilters(context);
    private final Convolution convFilters = new Convolution(context);
    private final EqualizationFilters equalizeFilters = new EqualizationFilters(context);

    /**
     * Update bmps reference to prevent memory leaks between activities
     */
    public void updateBmps(){
        this.bmp = BitmapTransfer.getInstance().getContext().getBmp();
        this.modBmp = BitmapTransfer.getInstance().getContext().getModBmp();
    }

    /**
     * Main fonction to handle menus
     * @param item item chosen by user
     * @return true or false if task was successful.
     */
    public boolean menuHandlerSwitch(MenuItem item) {
        /*** Filters objects initialization ***/
        updateBmps(); //Update bmps references


        switch (item.getItemId()) {
            //Rotates image
            case R.id.RotateImLeft:
                context.getImgview().setRotation(mainImageView.getRotation() - 90.0f);
                return true;
            case R.id.RotateImRight:
                mainImageView.setRotation(mainImageView.getRotation() + 90.0f);
                return true;


            case R.id.ColorTweaks:

                /*** Set useful data to singletop BitmapTransfer ***/
                BitmapTransfer.getInstance().setViewRotation(mainImageView.getRotation());
                BitmapTransfer.getInstance().setContext(context);
                context.startActivityForResult(new Intent(context, ColorizeActivity.class), 104);
                context.setIsmod(true);
                return true;


            case R.id.LoadImage:

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                context.startActivityForResult(photoPickerIntent, context.REQUEST_GET_SINGLE_FILE);
                //updateHistogram(true);
                return true;


            case R.id.UndoChangesItem:

                if (context.isIsmod()) {
                    mainImageView.setImageBitmap(this.bmp);
                    context.modBmpInit(); //Reset picture
                    context.setIsmod(false);
                    //starthistoTask(true,3);
                    return true;
                } else {
                    mainImageView.setImageBitmap(this.bmp);

                    ////starthistoTask(true, 3);
                    return true;
                }

            case R.id.ScaleSwitch:
                if (context.isScale()) {
                    context.setScale(false);
                    Toast.makeText(context, "Scaling mode OFF", Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    context.setScale(true);
                    Toast.makeText(context, "Scaling mode ON", Toast.LENGTH_LONG).show();
                    return true;
                }

            case R.id.RenderScriptMode:
                if (context.isRenderScript()) {
                    context.setRenderScript(false);
                    Toast.makeText(context, "Renderscript mode OFF", Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    context.setRenderScript(true);
                    Toast.makeText(context, "Renderscript mode ON", Toast.LENGTH_LONG).show();
                    return true;
                }

                //Histogram buttons
            case R.id.ImDims:
                Toast.makeText(context, "Width = " + this.bmp.getWidth() + " Height = " + this.bmp.getHeight(), Toast.LENGTH_LONG).show();
                return true;

            case R.id.KeepLumBins:
                context.histoDialog(modBmp, false, 3);
                return true;

            case R.id.KeepRedBins:
                context.histoDialog(modBmp, true, 0);
                return true;

            case R.id.KeepGreenBins:
                context.histoDialog(modBmp, true, 1);
                //histoIntent(true, 1);

                return true;


            case R.id.KeepBlueBins:
                context.histoDialog(modBmp, true, 2);
                return true;


            case R.id.KeepAllBins:
                //histoIntent(true, 3);
                context.histoDialog(modBmp, true, 3);
                return true;


            case R.id.ToGrayItem:

                    try {
                        BitmapAsync callback = new BitmapAsync() {
                            @Override
                            public Bitmap process(Bitmap bmp) {
                                if (context.isRenderScript())
                                    return filters.toGrayRS(bmp);
                                else
                                    return filters.toGray(bmp);

                            }
                        };
                        new Task(callback, context).execute(this.modBmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                context.setIsmod(true);


                //valueHistogram();
                return true;


            case R.id.ColorizeItem:
                try {
                    BitmapAsync callback = new BitmapAsync() {
                        @Override
                        public Bitmap process(Bitmap bmp) {
                            if (context.isRenderScript())
                                return filters.colorizeRS(bmp, (float) Math.random()*360);
                            else
                                return filters.colorize(bmp, (float) Math.random()*360);
                        }
                    };
                    new Task(callback, context).execute(this.modBmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                context.setIsmod(true);
                //valueHistogram();
                return true;



            case R.id.GrayContrastAug:

                    try {
                        BitmapAsync callback = new BitmapAsync() {
                            @Override
                            public Bitmap process(Bitmap bmp) {
                                if (context.isRenderScript())
                                    return dynFilters.grayContrastModifierRS(bmp, false);
                                else
                                    return dynFilters.grayContrastModifier(bmp, false);

                            }
                        };
                        new Task(callback, context).execute(this.modBmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    context.setIsmod(true);

                //valueHistogram();
                return true;



            case R.id.GrayContrastLower:
                try {
                    BitmapAsync callback = new BitmapAsync() {
                        @Override
                        public Bitmap process(Bitmap bmp) {
                            if (context.isRenderScript())
                                return dynFilters.grayContrastModifierRS(bmp, true);
                            else
                                return dynFilters.grayContrastModifier(bmp, true);
                        }
                    };
                    new Task(callback, context).execute(this.modBmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                context.setIsmod(true);
                //valueHistogram();
                return true;


            case R.id.RGBContrastAug:

                    try {
                        BitmapAsync callback = new BitmapAsync() {
                            @Override
                            public Bitmap process(Bitmap bmp) {
                                if (context.isRenderScript())
                                    return dynFilters.rgbContrastModifierRS(bmp, false);
                                else
                                    return dynFilters.rgbContrastModifier(bmp, false);
                            }
                        };
                        new Task(callback, context).execute(this.modBmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    context.setIsmod(true);
                    //valueHistogram();

                return true;

            case R.id.RGBContrastLower:
                try {
                    BitmapAsync callback = new BitmapAsync() {
                        @Override
                        public Bitmap process(Bitmap bmp) {
                            if (context.isRenderScript())
                                return dynFilters.rgbContrastModifierRS(bmp, true);
                            else
                                return dynFilters.rgbContrastModifier (bmp, true);
                        }
                    };
                    new Task(callback, context).execute(this.modBmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                context.setIsmod(true);
                //valueHistogram();
                return true;

            case R.id.HSVContrastAug:

                try {
                    BitmapAsync callback = new BitmapAsync() {
                        @Override
                        public Bitmap process(Bitmap bmp) {
                            if (context.isRenderScript())
                                return dynFilters.hsvContrastModifierRS(bmp, false);
                            else
                                return dynFilters.hsvContrastModifier(bmp, false);
                        }
                    };
                    new Task(callback, context).execute(this.modBmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                context.setIsmod(true);

                //valueHistogram();
                return true;

            case R.id.HSVContrastLower:
                try {
                    BitmapAsync callback = new BitmapAsync() {
                        @Override
                        public Bitmap process(Bitmap bmp) {
                            if (context.isRenderScript())
                                return dynFilters.hsvContrastModifierRS(bmp, true);
                            else
                                return dynFilters.hsvContrastModifier(bmp, true);
                        }
                    };
                    new Task(callback, context).execute(this.modBmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                context.setIsmod(true);
                //valueHistogram();
                return true;


            case R.id.EGrayContrastAug:
                    try {
                        BitmapAsync callback = new BitmapAsync() {
                            @Override
                            public Bitmap process(Bitmap bmp) {
                                if (context.isRenderScript())
                                    return equalizeFilters.grayEqualizationContrastAugmentationRS(bmp);
                                else
                                    return equalizeFilters.grayEqualizationContrastAugmentation(bmp);
                            }
                        };
                        new Task(callback, context).execute(this.modBmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    context.setIsmod(true);

                return true;

            case R.id.EQHSVContrastAug:

                    try {
                        BitmapAsync callback = new BitmapAsync() {
                            @Override
                            public Bitmap process(Bitmap bmp) {
                                if (context.isRenderScript())
                                    return equalizeFilters.hsvEqualizationContrastAugmentationRS(bmp);
                                else
                                    return equalizeFilters.hsvEqualizationContrastAugmentation(bmp);


                            }
                        };
                        new Task(callback, context).execute(this.modBmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    context.setIsmod(true);

                //valueHistogram();
                return true;

            case R.id.RGBAvgContrastAug:

                    try {
                        BitmapAsync callback = new BitmapAsync() {
                            @Override
                            public Bitmap process(Bitmap bmp) {
                                if (context.isRenderScript())
                                    return equalizeFilters.rgbAverageContrastAugmentationRS(bmp);
                                else
                                    return equalizeFilters.rgbAverageContrastAugmentation(bmp);
                            }
                        };
                        new Task(callback, context).execute(this.modBmp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    context.setIsmod(true);

                return true;

            case R.id.convolveEdges:


                try {
                    BitmapAsync callback = new BitmapAsync() {
                        @Override
                        public Bitmap process(Bitmap bmp) {
                            if (context.isRenderScript()) {
                                return bmp;
                            }
                            else
                                return convFilters.convolve2D(bmp, context.kernelLaplace, 3, 3, false);
                        }
                    };
                    context.setCurrentTask(new Task(callback, context));
                    context.getCurrentTask().execute(this.modBmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (context.isRenderScript())
                    Toast.makeText(context, "Not yet implemented in RS", Toast.LENGTH_LONG).show();
                context.setIsmod(true);

                return true;

            case R.id.convolveBlurry:
                try {
                    BitmapAsync callback = new BitmapAsync() {
                        @Override
                        public Bitmap process(Bitmap bmp) {
                            if (context.isRenderScript()) {
                                return bmp;
                            }
                            else
                                return convFilters.convolve2D(bmp, context.kernelBlurry, 3, 3, true);
                        }
                    };
                    new Task(callback, context).execute(this.modBmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (context.isRenderScript())
                    Toast.makeText(context, "Not yet implemented in RS", Toast.LENGTH_LONG).show();

                context.setIsmod(true);



            return true;



            default:
                return false;
        }
    }
}
