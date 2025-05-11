package com.capstone.whereigo;

public class NodeLabelData {
    private String label;
    private String position;
    private int imageResId;

    public NodeLabelData(String label, String position, int imageResId) {
        this.label = label;
        this.position = position;
        this.imageResId = imageResId;
    }

    public String getLabel() { return label; }
    public String getPosition() { return position; }
    public int getImageResId() { return imageResId; }
}