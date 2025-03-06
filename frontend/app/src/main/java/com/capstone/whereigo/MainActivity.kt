package com.capstone.whereigo

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.whereigo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val searchBar = binding.searchBar
        val menuButton = binding.menuButton

        // 예제 데이터 (최근 검색어 + 연관 검색어)
        val searchHistory = listOf(
            "미래관", "북악관", "과학관", "예술관", "공학관",
            "성곡 도서관", "아무거나", "편의점 택배",
            "스타벅스 메뉴", "스타벅스 아메리카노"
        )

        var searchAdapter =
            ArrayAdapter(this, R.layout.simple_list_item_activated_1, searchHistory)
        searchBar.setAdapter(searchAdapter)

        // 리스트 중 하나 선택 시
        searchBar.setOnItemClickListener { _, _, position, _ ->
            val selectedText = searchAdapter.getItem(position)
            searchBar.setText(selectedText)
        }

        menuButton.setOnClickListener {
            val intent = Intent(this, DownloadActivity::class.java)
            startActivity(intent)
        }

    }
}
