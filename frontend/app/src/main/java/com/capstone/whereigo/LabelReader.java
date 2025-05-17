package com.capstone.whereigo;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class LabelReader {

    public static Pair<Float, Float> getCoordinates(Context context, String buildingName, String roomName) {
        File labelFile = new File(context.getExternalFilesDir(null), buildingName + "/label.txt");

        if (!labelFile.exists()) {
            Log.e("LabelReader", "label.txt 파일이 존재하지 않음");
            return null;
        }

        try (FileInputStream fis = new FileInputStream(labelFile)) {
            byte[] data = new byte[(int) labelFile.length()];
            fis.read(data);
            String jsonString = new String(data, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(jsonString);

            if (jsonObject.has(roomName)) {
                float x = (float) jsonObject.getJSONArray(roomName).getDouble(0);
                float y = (float) jsonObject.getJSONArray(roomName).getDouble(1);
                Log.i("LabelReader", "✅ " + roomName + " 좌표 불러옴: x=" + x + ", y=" + y);
                return new Pair<Float, Float>(x, y);
            } else {
                Log.e("LabelReader", roomName + " 좌표 없음");
            }

        } catch (Exception e) {
            Log.e("LabelReader", "파일 읽기 실패: " + e.getMessage());
        }

        return null;
    }
}