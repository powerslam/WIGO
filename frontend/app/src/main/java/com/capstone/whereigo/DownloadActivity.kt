package com.capstone.whereigo

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.whereigo.databinding.ActivityDownloadBinding
import java.io.File

class DownloadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //default

        val toolbar = binding.toolbar

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

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

        val adapter = MapDataAdapter(this, sampleData)
        val spacing = 16

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(ItemSpacingDecoration(spacing))

        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // 뒤로 가기 기능 수행
        }


        //default
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }

    //지금은 안씀, 나중에 slam 파일이 어떻게 구성되는지 보고 해야 할 듯
    //파일 생성
    private fun createAppFolder() {
        val folder = File(filesDir, "Maps") // "Maps" 폴더 생성
        if (!folder.exists()) {
            val success = folder.mkdir()
            if (success) {
                Log.d("Storage", "폴더 생성 성공: ${folder.absolutePath}")
            } else {
                Log.d("Storage", "폴더 생성 실패")
            }
        }
    }

    //파일에 저장
    private fun saveMapFile(fileName: String, data: String) {
        val folder = File(filesDir, "Maps") // "Maps" 폴더 지정
        if (!folder.exists()) folder.mkdir() // 폴더 없으면 생성

        val file = File(folder, fileName) // 저장할 파일 생성
        file.writeText(data) // 파일에 데이터 저장
    }

    //파일에서 불러오기
    private fun readMapFile(fileName: String): String {
        val folder = File(filesDir, "Maps")
        val file = File(folder, fileName)

        return if (file.exists()) {
            file.readText()
        } else {
            "파일이 존재하지 않습니다."
        }
    }

    //폴더 내 모든 파일 불러오기
    private fun listMapFiles(): List<String> {
        val folder = File(filesDir, "Maps")
        return folder.list()?.toList() ?: emptyList()
    }

}









// toolbar 기본제공 쓰고 싶으면 가져다 넣기
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)