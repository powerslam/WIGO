package com.capstone.whereigo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class FileDownloader {

    public interface OnUnzipCompleteListener {
        void onComplete();
        void onFailure(String errorMessage);
    }

    public static void downloadAndUnzipFile(
            Context context,
            String url,
            String zipFileName,
            String extractFolderName,
            OnUnzipCompleteListener listener
    ) {
        File zipFile = new File(context.getExternalFilesDir(null), zipFileName);
        File extractDir = new File(context.getExternalFilesDir(null), extractFolderName);
        Handler handler = new Handler(Looper.getMainLooper());

        if (extractDir.exists()) {
            handler.post(() -> {
                Toast.makeText(context, "이미 다운로드 및 압축 해제됨", Toast.LENGTH_SHORT).show();
                listener.onComplete();
            });
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("DownloadFailure", Objects.requireNonNull(e.getMessage()));
                handler.post(() -> {
                    Toast.makeText(context, "다운로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    listener.onFailure(e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    handler.post(() -> {
                        Toast.makeText(context, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                        listener.onFailure("서버 응답 실패");
                    });
                    return;
                }

                BufferedSink sink = Okio.buffer(Okio.sink(zipFile));
                sink.writeAll(response.body().source());
                sink.close();

                try {
                    unzip(zipFile, extractDir);
                    zipFile.delete();

                    listener.onComplete();
                } catch (Exception e) {
                    handler.post(() -> {
                        Toast.makeText(context, "압축 해제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        listener.onFailure(e.getMessage());
                    });
                }
            }
        });
    }

    private static void unzip(File zipFile, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                String[] parts = entryName.split("/", 2);
                if (parts.length < 2) continue;

                String relativePath = parts[1];
                File newFile = new File(targetDir, relativePath);

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

                zis.closeEntry();
            }
        }
    }
}
