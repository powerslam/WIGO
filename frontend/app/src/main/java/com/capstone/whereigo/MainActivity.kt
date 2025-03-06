package com.capstone.whereigo

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.whereigo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 2000 // 2초 이내에 두 번 눌러야 종료됨

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
    override fun onBackPressed() {
        // 현재 시간
        val currentTime = System.currentTimeMillis()

        // 뒤로가기 버튼이 눌린 시간이 2초 이내이면 종료
        if (currentTime - backPressedTime < backPressInterval) {
            super.onBackPressed()  // 종료
            return
        } else {
            backPressedTime = currentTime
            // "한 번 더 누르면 종료됩니다." 메시지 출력
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
