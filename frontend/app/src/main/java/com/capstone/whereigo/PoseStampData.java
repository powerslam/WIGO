package com.capstone.whereigo;

public class PoseStampData {
    private float x, y;
    private String label;

    public PoseStampData(float x, float y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    public float x() {
        return this.x;
    }
    public float y() {
        return this.y;
    }

    public String label() {
        return this.label;
    }
    public void setLabel(float y) {
        this.label = label;
    }
}
