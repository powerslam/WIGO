package com.capstone.whereigo;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.capstone.whereigo.databinding.ActivityMappingBinding;

public class MappingActivity extends AppCompatActivity {
    private static final String TAG = "MappingActivity";

    private ActivityMappingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        binding = ActivityMappingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutMappingMain, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backButton.setOnClickListener(v -> {
            checkDialog();
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, BuildingInputFragment.newInstance(false))
                .commit();
    }

    public void checkDialog(){
        new AlertDialog.Builder(this)
            .setTitle("확인")
            .setMessage("정말 지도 작성을 취소하시겠습니까?")
            .setPositiveButton("예", (dialog, which) -> {
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss())
            .show();
    }
}
