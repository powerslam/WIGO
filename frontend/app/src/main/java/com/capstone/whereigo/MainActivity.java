package com.capstone.whereigo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.whereigo.databinding.ActivityMainBinding;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import android.content.Context;
import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final int RECORD_AUDIO_REQUEST_CODE = 100;
    private long backPressedTime = 0;
    private final long backPressInterval = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SearchBar searchBar = binding.searchBar;
        SearchView searchView = binding.searchView;
        searchView.setupWithSearchBar(searchBar);

        int searchMenu = R.menu.search_menu;
        searchBar.inflateMenu(searchMenu);

        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_setting, new SettingsFragment())
                .addToBackStack(null)
                .commit());

        searchBar.getMenu().findItem(R.id.action_voice_search).setOnMenuItemClickListener(item -> {
            if (checkAudioPermission()) {
                if (SpeechRecognizer.isRecognitionAvailable(this)) {
                    VoiceRecordDialog dialog = new VoiceRecordDialog();
                    dialog.show(getSupportFragmentManager(), "VoiceRecordDialog");
                } else {
                    Toast.makeText(this, "음성 인식을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestAudioPermission();
            }
            return true;
        });

        RecyclerView recyclerView = searchView.findViewById(R.id.search_result);

        List<String> allResults = new ArrayList<>();
        allResults.add("미래관 445호");
        allResults.add("미래관 447호");
        allResults.add("미래관 449호");
        allResults.add("미래관 444호");
        allResults.add("미래관 425호");
        allResults.add("미래관 415호");
        allResults.add("미래관 405호");

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 필요 없다면 비워둠
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                List<String> filtered = new ArrayList<>();
                for (String item : allResults) {
                    if (item.toLowerCase().contains(query.toLowerCase())) {
                        filtered.add(item);
                    }
                }

                if (!query.isEmpty()) {
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    recyclerView.setAdapter(new SearchResultAdapter(filtered, selected -> {
                        // 예: "미래관 445호" → "미래관"
                        String buildingName = selected.split(" ")[0];
                        String fileName = buildingName + ".zip";
                        String unzipFolder = buildingName;
                        String url = "https://media-server-jubin.s3.amazonaws.com/" + buildingName + "/" + fileName;

                        downloadAndUnzipFile(MainActivity.this, url, fileName, unzipFolder);
                    }));
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 필요 없다면 비워둠
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchView.isShowing()) {
                    searchView.hide();
                } else {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - backPressedTime < backPressInterval) {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    } else {
                        backPressedTime = currentTime;
                    }
                }
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HelloArFragment())
                    .commit();
        }

        searchView.addTransitionListener((view, previousState, newState) ->
                HelloArFragment.setCameraPoseVisibility(
                        newState != com.google.android.material.search.SearchView.TransitionState.SHOWN)
        );

        setupWakeWordListener();
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_AUDIO_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "음성 권한이 허용되었습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "음성 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupWakeWordListener() {
        if (!checkAudioPermission()) {
            requestAudioPermission();
        }
    }

    private void downloadAndUnzipFile(Context context, String url, String zipFileName, String extractFolderName) {
        File zipFile = new File(context.getFilesDir(), zipFileName);
        File extractDir = new File(context.getFilesDir(), extractFolderName);

        if (extractDir.exists()) {
            String msg = "이미 압축 풀린 폴더: " + extractDir.getAbsolutePath();
            Log.d("ZIP", msg);
            runOnUiThread(() -> Toast.makeText(context, "이미 다운로드 및 압축 해제됨", Toast.LENGTH_SHORT).show());
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(context, "다운로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(context, "서버 응답 실패", Toast.LENGTH_SHORT).show());
                    return;
                }

                BufferedSink sink = Okio.buffer(Okio.sink(zipFile));
                sink.writeAll(response.body().source());
                sink.close();

                Log.d("ZIP", "ZIP 저장 경로: " + zipFile.getAbsolutePath());
                runOnUiThread(() -> Toast.makeText(context, "ZIP 저장: " + zipFile.getAbsolutePath(), Toast.LENGTH_SHORT).show());

                try {
                    unzip(zipFile, extractDir);
                    runOnUiThread(() -> Toast.makeText(context, "압축 해제 완료: " + extractDir.getAbsolutePath(), Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(context, "압축 해제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void unzip(File zipFile, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                // ✅ 최상위 폴더명 제거 (예: "미래관/4층/...")
                String[] parts = entryName.split("/", 2);
                if (parts.length < 2) continue; // 파일이 미래관만 있거나 이상할 때 무시

                String relativePath = parts[1]; // "4층/..."만 사용

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
