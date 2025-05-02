package com.capstone.whereigo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.whereigo.databinding.ActivityMainBinding;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private final int RECORD_AUDIO_REQUEST_CODE = 100;
    private long backPressedTime = 0;
    private final long backPressInterval = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SearchBar searchBar = binding.searchBar;
        SearchView searchView = binding.searchView;
        searchView.setupWithSearchBar(searchBar);

        int searchMenu = R.menu.search_menu;
        searchBar.inflateMenu(searchMenu);

        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> getSupportFragmentManager().beginTransaction()
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

        searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchView.getText().toString();
                if (!query.isEmpty()) {
                    List<String> filtered = allResults.stream()
                            .filter(item -> item.toLowerCase().contains(query.toLowerCase()))
                            .collect(Collectors.toList());
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(new SearchResultAdapter(filtered));
                    searchView.hide();
                }
                return true;
            }
            return false;
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

        searchView.addTransitionListener((searchView1, previousState, newState) -> 
            HelloArFragment.setCameraPoseVisibility(
                newState != com.google.android.material.search.SearchView.TransitionState.SHOWN
            )
        );

        setupWakeWordListener();
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
}
