package com.capstone.whereigo

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.whereigo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 2000 // 2초 이내에 두 번 눌러야 종료됨

    private val searchSuggestions = listOf("서울", "부산", "대구", "전주", "경기도")
    private val searchResults = listOf("서울 랜드마크", "부산 해운대", "대구 수목원", "전주 한옥마을", "경기도 관광지")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var searchBar = binding.searchBar
        var searchView = binding.searchView
        searchView.setupWithSearchBar(searchBar)

        searchBar.inflateMenu(R.menu.search_menu)
        searchView.editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchView.text.toString()
                Toast.makeText(this, query, Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }
        // 예측 결과 RecyclerView 설정
        val suggestionsAdapter = SearchSuggestionsAdapter(searchSuggestions)
        binding.searchResult.layoutManager = LinearLayoutManager(this)
        binding.searchResult.adapter = suggestionsAdapter

        // 검색어 예측 기능
        searchView.editText.addTextChangedListener { text ->
            if (text != null && text.isNotEmpty()) {
                val filteredSuggestions = searchSuggestions.filter { it.contains(text, ignoreCase = true) }
                suggestionsAdapter.updateSuggestions(filteredSuggestions)
            } else {
                suggestionsAdapter.updateSuggestions(searchSuggestions) // 초기 상태로 되돌리기
            }
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
