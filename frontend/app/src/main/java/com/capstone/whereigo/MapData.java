package com.capstone.whereigo;

public class MapData {
    private String fileName;
    private String fileSize;
    private String saveDate;

    public MapData(String fileName, String fileSize, String saveDate) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.saveDate = saveDate;
    }

    public MapData(String fileName, String fileSize, String saveDate, boolean isFavorite) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.saveDate = saveDate;
    }

    // Getter & Setter
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(String saveDate) {
        this.saveDate = saveDate;
    }

}
