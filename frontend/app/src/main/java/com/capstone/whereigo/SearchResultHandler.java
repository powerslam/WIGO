package com.capstone.whereigo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import java.io.File;

public class SearchResultHandler {

    public interface FragmentProvider {
        HelloArFragment getFragment();
    }

    public static void handle(Context context, String selected, FragmentProvider provider) {
        String buildingName = selected.split(" ")[0];
        String fileName = buildingName + ".zip";
        String url = "https://media-server-jubin.s3.amazonaws.com/" + buildingName + "/" + fileName;

        File labelFile = new File(context.getFilesDir(), buildingName + "/label.txt");

        if (labelFile.exists()) {
            // 이미 있음 → 바로 처리
            sendGoal(context, selected, buildingName, provider);
        } else {
            // 다운로드 및 대기
            FileDownloader.downloadAndUnzipFile(context, url, fileName, buildingName);
            waitForLabelFile(context, labelFile, selected, buildingName, provider);
        }
    }

    private static void waitForLabelFile(Context context, File labelFile, String selected, String buildingName, FragmentProvider provider) {
        Handler handler = new Handler(Looper.getMainLooper());
        long startTime = System.currentTimeMillis();
        long timeout = 10000; // 10초

        Runnable checkTask = new Runnable() {
            @Override
            public void run() {
                if (labelFile.exists()) {
                    Log.d("SearchResultHandler", "label.txt 발견됨: " + labelFile.getAbsolutePath());
                    sendGoal(context, selected, buildingName, provider);
                } else if (System.currentTimeMillis() - startTime < timeout) {
                    handler.postDelayed(this, 500);
                } else {
                    Log.e("SearchResultHandler", "label.txt 생성 시간 초과: " + labelFile.getAbsolutePath());
                }
            }
        };

        handler.post(checkTask);
    }

    private static void sendGoal(Context context, String selected, String buildingName, FragmentProvider provider) {
        String roomNumber = selected.replaceAll("[^0-9]", "");
        Pair<Float, Float> coords = LabelReader.getCoordinates(context, buildingName, roomNumber);

        if (coords != null) {
            HelloArFragment fragment = provider.getFragment();
            if (fragment != null) {
                Log.i("SearchResultHandler", "경로 전달: x=" + coords.first + ", y=" + coords.second);
                fragment.sendGoalToNative(coords.first, coords.second);
            } else {
                Log.e("SearchResultHandler", "HelloArFragment not found");
            }
        } else {
            Log.e("SearchResultHandler", "좌표를 찾을 수 없습니다: " + selected);
        }
    }
}
