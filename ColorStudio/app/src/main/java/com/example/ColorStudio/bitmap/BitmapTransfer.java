package com.example.ColorStudio.bitmap;

import com.example.ColorStudio.activity.MainActivity;

/**
 * Singleton class to transfer MainActivty context and modified it from another activities.
 */

public class BitmapTransfer {
    private static final BitmapTransfer instance = new BitmapTransfer();
    private MainActivity context = null;
    private float viewRotation = 0.0f;

    public static BitmapTransfer getInstance() {
        return instance;
    }

    public MainActivity getContext() {
        return context;
    }

    public void setContext(MainActivity context) {
        this.context = context;
    }

    public float getViewRotation() {
        return this.viewRotation;
    }

    public void setViewRotation(float viewRotation2) {
        this.viewRotation = viewRotation2;
    }

}
