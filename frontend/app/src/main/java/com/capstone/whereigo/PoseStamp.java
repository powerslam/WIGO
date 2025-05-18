package com.capstone.whereigo;

import android.graphics.Bitmap;

public class PoseStamp {
    private final float x, z;
    private final Bitmap image;

    public PoseStamp(float x, float z, Bitmap image) {
        this.x = x;
        this.z = z;
        this.image = image;
    }

    public float x() { return this.x; }
    public float z() {
        return this.z;
    }
    public Bitmap image() {
        return this.image;
    }
}
