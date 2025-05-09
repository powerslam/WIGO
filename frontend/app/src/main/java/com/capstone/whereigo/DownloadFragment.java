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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // ✅ Maps 폴더에서 하위 폴더(맵들) 가져오기
        List<MapData> mapDataList = listMapFolders();

        recyclerView.setAdapter(new MapDataAdapter(requireContext(), mapDataList));

        int spacing = 16;
        recyclerView.addItemDecoration(new ItemSpacingDecoration(spacing));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        toolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private List<MapData> listMapFolders() {
        List<MapData> result = new ArrayList<>();

        File mapsFolder = new File(requireContext().getFilesDir(), "Maps");
        File[] folders = mapsFolder.listFiles();

        if (folders != null) {
            for (File folder : folders) {
                if (folder.isDirectory()) {
                    long folderSize = calculateFolderSize(folder);
                    String sizeString = (folderSize / (1024 * 1024)) + "MB";

                    String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(new Date(folder.lastModified()));

                    result.add(new MapData(folder.getName(), sizeString, dateString));
                }
            }
        }

        return result;
    }

    // 폴더 크기 계산 (재귀)
    private long calculateFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    length += calculateFolderSize(file);
                }
            }
        }
        return length;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
