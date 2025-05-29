package com.capstone.whereigo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchResultHandler {

    public static int goal_floor = -1;
//
//    public interface FragmentProvider {
//        HelloArFragment getFragment();
//    }

    public static void handle(Context context, String selected, /* FragmentProvider provider ,*/ int currentFloor) {
        String buildingName = selected.split(" ")[0];
        String fileName = buildingName + ".zip";

        Toast.makeText(context, buildingName + " " + fileName, Toast.LENGTH_SHORT).show();
        String url = "https://media-server-jubin.s3.amazonaws.com/" + buildingName + "/" + fileName;

        File labelFile = new File(context.getExternalFilesDir(null), buildingName + "/label.txt");

        if (labelFile.exists()) {
            Toast.makeText(context, "이미 파일이 있는 부분 ㅋㅋ", Toast.LENGTH_SHORT).show();
//            sendMultiGoals(context, selected, buildingName, provider, currentFloor);
        } else {
            FileDownloader.downloadAndUnzipFile(context, url, fileName, buildingName, new FileDownloader.OnUnzipCompleteListener() {
                @Override
                public void onComplete() {
                    waitForLabelFile(labelFile);
                    Toast.makeText(context, "오예! 다운로드가 완료되었어용가리치킨더조이", Toast.LENGTH_SHORT).show();
                    
//                    sendMultiGoals(context, selected, buildingName, provider, currentFloor);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(context, "다운로드에 실패하였습니다. Wifi 혹은 모바일 데이터 환경을 점검하세요.", Toast.LENGTH_SHORT).show();
                    Log.e("SearchResultHandler", "다운로드 또는 압축 해제 실패: " + errorMessage);
                }});
        }
    }

    private static void waitForLabelFile(File labelFile) {
        Handler handler = new Handler(Looper.getMainLooper());
        long startTime = System.currentTimeMillis();
        long timeout = 10000;

        Runnable checkTask = new Runnable() {
            @Override
            public void run() {
                if (labelFile.exists()) {
                    Log.d("SearchResultHandler", "✅ label.txt 발견됨: " + labelFile.getAbsolutePath());
                } else if (System.currentTimeMillis() - startTime < timeout) {
                    handler.postDelayed(this, 500);
                } else {
                    Log.e("SearchResultHandler", "❌ label.txt 생성 시간 초과: " + labelFile.getAbsolutePath());
                }
            }
        };

        handler.post(checkTask);
    }
//
//    private static void sendMultiGoals(Context context, String selected, String buildingName, FragmentProvider provider, int currentFloor) {
//        HelloArFragment fragment = provider.getFragment();
//        if (fragment == null) {
//            Log.e("SearchResultHandler", "❌ HelloArFragment is null");
//            return;
//        }
//
//        fragment.setCurrentFloor(currentFloor);
//
//        // pose_graph 전체 로드
//        PoseGraphLoader.loadAll(context, buildingName, fragment);
//
//        // 목적지 방번호 추출
//        String roomNumber = selected.replaceAll("[^0-9]", "");
//        int goalFloor = Character.getNumericValue(roomNumber.charAt(0));  // 예: 445 → 4
//        goal_floor = goalFloor;
//
//        Log.i("SearchResultHandler", "currentFloor: " + currentFloor + ", roomNumber: " + roomNumber + ", goalFloor: " + goalFloor);
//
//        List<Pair<Float, Float>> goalCoords = new ArrayList<>();
//
//        if (currentFloor != goalFloor) {
//            // 층 다르면 엘리베이터 경유 목표 설정
//            Pair<Float, Float> toElevator = LabelReader.getCoordinates(context, buildingName, "elevator" + currentFloor);
//            Pair<Float, Float> fromElevator = LabelReader.getCoordinates(context, buildingName, "elevator" + goalFloor);
//            Pair<Float, Float> destination = LabelReader.getCoordinates(context, buildingName, roomNumber);
//
//            if (toElevator != null) goalCoords.add(toElevator);
//            if (fromElevator != null) goalCoords.add(fromElevator);
//            if (destination != null) goalCoords.add(destination);
//        } else {
//            // 층 같으면 바로 목적지
//            Pair<Float, Float> destination = LabelReader.getCoordinates(context, buildingName, roomNumber);
//            if (destination != null) goalCoords.add(destination);
//        }
//
//        if (!goalCoords.isEmpty()) {
//            float[] goalArray = new float[goalCoords.size() * 2];
//            for (int i = 0; i < goalCoords.size(); i++) {
//                goalArray[2 * i] = goalCoords.get(i).first;
//                goalArray[2 * i + 1] = goalCoords.get(i).second;
//            }
//
//            Log.i("SearchResultHandler", "📍 다중 경로 전달: " + goalCoords.size() + "개 지점");
//            fragment.sendMultiGoalsToNative(goalArray);
//        } else {
//            Log.e("SearchResultHandler", "❌ 유효한 좌표가 없어 목표 설정 실패");
//        }
//    }
}
