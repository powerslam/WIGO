package com.capstone.whereigo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PoseStampViewModel extends ViewModel {
    private final MutableLiveData<List<PoseStamp>> poseStampList = new MutableLiveData<>();
    private final MutableLiveData<List<String>> poseStampLabelList = new MutableLiveData<>();

    public PoseStampViewModel() {
        List<PoseStamp> initPoseStampList = new ArrayList<>();
        poseStampList.setValue(initPoseStampList);

        List<String> initLabelList = new ArrayList<>();
        poseStampLabelList.setValue(initLabelList);
    }

    public LiveData<List<PoseStamp>> getPoseStampList() {
        return poseStampList;
    }

    public LiveData<List<String>> getPoseStampLabelList() {
        return poseStampLabelList;
    }

    public int getPoseStampListSize() {
        return Objects.requireNonNull(poseStampList.getValue()).size();
    }

    public PoseStamp getPoseStampAt(int index) {
        List<PoseStamp> list = poseStampList.getValue();
        if (list != null && index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    public String getLabelAt(int index) {
        List<String> list = poseStampLabelList.getValue();
        if (list != null && index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    public void updateLabel(int index, String newLabel) {
        List<String> list = poseStampLabelList.getValue();
        if (list != null && index < list.size()) {
            list.set(index, newLabel);
            poseStampLabelList.setValue(list);
        }
    }

    public void addPoseStampData(PoseStamp newItem) {
        List<PoseStamp> currentPoseStampList = poseStampList.getValue();
        List<String> currentPoseStampLabelList = poseStampLabelList.getValue();
        
        if (currentPoseStampList != null && currentPoseStampLabelList != null) {
            currentPoseStampList.add(newItem);
            currentPoseStampLabelList.add(
                    String.format(Locale.ROOT, "노드 : %d", currentPoseStampList.size())
            );

            poseStampList.setValue(currentPoseStampList);
            poseStampLabelList.setValue(currentPoseStampLabelList);
        }
    }
}