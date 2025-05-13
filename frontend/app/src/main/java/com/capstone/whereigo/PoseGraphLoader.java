package com.capstone.whereigo;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class PoseGraphLoader {

    public static void loadAll(Context context, String buildingName, HelloArFragment fragment) {
        File buildingDir = new File(context.getFilesDir(), buildingName);
        File[] floorDirs = buildingDir.listFiles();

        if (floorDirs == null) {
            Log.e("PoseGraphLoader", "건물 디렉토리가 비었거나 없음: " + buildingDir.getAbsolutePath());
            return;
        }

        for (File floorDir : floorDirs) {
            if (floorDir.isDirectory() && floorDir.getName().matches("\\d+층")) {
                File poseFile = new File(floorDir, "pose_graph.txt");
                if (poseFile.exists()) {
                    int floor = Integer.parseInt(floorDir.getName().replaceAll("[^0-9]", ""));
                    String poseFilePath = poseFile.getAbsolutePath();
                    Log.d("PoseGraphLoader", floor + "층 pose_graph.txt: " + poseFilePath);

                    // JNI 호출
                    fragment.loadPoseGraphFromFile(poseFilePath, floor);
                }
            }
        }
    }
}
