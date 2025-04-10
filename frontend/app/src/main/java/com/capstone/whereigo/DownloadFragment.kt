package com.capstone.whereigo

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.whereigo.databinding.FragmentDownloadBinding
import java.io.File

class DownloadFragment : Fragment() {
    private var _binding: FragmentDownloadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar = binding.toolbar
        val recyclerView = binding.recyclerView

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sampleData = mutableListOf(
            MapData("미래관", "15MB", "2024-03-02"),
            MapData("복지관", "20MB", "2024-03-01"),
            MapData("본부관", "12MB", "2024-02-28"),
            MapData("도서관", "18MB", "2024-02-27"),
            MapData("학생회관", "22MB", "2024-02-26"),
            MapData("체육관", "25MB", "2024-02-25"),
            MapData("연구동", "30MB", "2024-02-24"),
            MapData("강의동A", "10MB", "2024-02-23"),
            MapData("강의동B", "12MB", "2024-02-22"),
            MapData("행정관", "28MB", "2024-02-21"),
            MapData("기숙사1동", "35MB", "2024-02-20"),
            MapData("기숙사2동", "32MB", "2024-02-19"),
            MapData("학생식당", "40MB", "2024-02-18"),
            MapData("대운동장", "50MB", "2024-02-17"),
            MapData("연구센터", "45MB", "2024-02-16")
        )

        recyclerView.adapter = MapDataAdapter(requireContext(), sampleData)

        val spacing = 16
        recyclerView.addItemDecoration(ItemSpacingDecoration(spacing))
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // 뒤로가기
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun createAppFolder() {
        val folder = File(requireContext().filesDir, "Maps")
        if (!folder.exists()) {
            val success = folder.mkdir()
            if (success) Log.d("Storage", "폴더 생성 성공: ${folder.absolutePath}")
            else Log.d("Storage", "폴더 생성 실패")
        }
    }

    private fun saveMapFile(fileName: String, data: String) {
        val folder = File(requireContext().filesDir, "Maps")
        if (!folder.exists()) folder.mkdir()
        val file = File(folder, fileName)
        file.writeText(data)
    }

    private fun readMapFile(fileName: String): String {
        val folder = File(requireContext().filesDir, "Maps")
        val file = File(folder, fileName)
        return if (file.exists()) file.readText() else "파일이 존재하지 않습니다."
    }

    private fun listMapFiles(): List<String> {
        val folder = File(requireContext().filesDir, "Maps")
        return folder.list()?.toList() ?: emptyList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
