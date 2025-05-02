package com.capstone.whereigo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.capstone.whereigo.databinding.FragmentDownloadBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DownloadFragment extends Fragment {
    private FragmentDownloadBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDownloadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        androidx.appcompat.widget.Toolbar toolbar = binding.toolbar;
        androidx.recyclerview.widget.RecyclerView recyclerView = binding.recyclerView;

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<MapData> sampleData = new ArrayList<>();
        sampleData.add(new MapData("미래관", "15MB", "2024-03-02"));
        sampleData.add(new MapData("복지관", "20MB", "2024-03-01"));
        sampleData.add(new MapData("본부관", "12MB", "2024-02-28"));
        sampleData.add(new MapData("도서관", "18MB", "2024-02-27"));
        sampleData.add(new MapData("학생회관", "22MB", "2024-02-26"));
        sampleData.add(new MapData("체육관", "25MB", "2024-02-25"));
        sampleData.add(new MapData("연구동", "30MB", "2024-02-24"));
        sampleData.add(new MapData("강의동A", "10MB", "2024-02-23"));
        sampleData.add(new MapData("강의동B", "12MB", "2024-02-22"));
        sampleData.add(new MapData("행정관", "28MB", "2024-02-21"));
        sampleData.add(new MapData("기숙사1동", "35MB", "2024-02-20"));
        sampleData.add(new MapData("기숙사2동", "32MB", "2024-02-19"));
        sampleData.add(new MapData("학생식당", "40MB", "2024-02-18"));
        sampleData.add(new MapData("대운동장", "50MB", "2024-02-17"));
        sampleData.add(new MapData("연구센터", "45MB", "2024-02-16"));

        recyclerView.setAdapter(new MapDataAdapter(requireContext(), sampleData));

        int spacing = 16;
        recyclerView.addItemDecoration(new ItemSpacingDecoration(spacing));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        toolbar.setNavigationOnClickListener(v -> requireParentFragment().getParentFragmentManager().popBackStack());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void createAppFolder() {
        File folder = new File(requireContext().getFilesDir(), "Maps");
        if (!folder.exists()) {
            boolean success = folder.mkdir();
            if (success) {
                Log.d("Storage", "폴더 생성 성공: " + folder.getAbsolutePath());
            } else {
                Log.d("Storage", "폴더 생성 실패");
            }
        }
    }

    private void saveMapFile(String fileName, String data) {
        File folder = new File(requireContext().getFilesDir(), "Maps");
        if (!folder.exists()) folder.mkdir();
        File file = new File(folder, fileName);
        try {
            java.nio.file.Files.write(file.toPath(), data.getBytes());
        } catch (Exception e) {
            Log.e("Storage", "파일 쓰기 실패", e);
        }
    }

    private String readMapFile(String fileName) {
        File folder = new File(requireContext().getFilesDir(), "Maps");
        File file = new File(folder, fileName);
        if (file.exists()) {
            try {
                return new String(java.nio.file.Files.readAllBytes(file.toPath()));
            } catch (Exception e) {
                Log.e("Storage", "파일 읽기 실패", e);
                return "파일 읽기 중 오류 발생";
            }
        } else {
            return "파일이 존재하지 않습니다.";
        }
    }

    private List<String> listMapFiles() {
        File folder = new File(requireContext().getFilesDir(), "Maps");
        String[] files = folder.list();
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
