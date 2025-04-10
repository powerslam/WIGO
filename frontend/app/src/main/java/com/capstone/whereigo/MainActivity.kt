package com.capstone.whereigo

import SearchResultAdapter
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.whereigo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 설정 버튼 → SettingsFragment 전환
        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_setting, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        val searchBar = binding.searchBar
        val searchView = binding.searchView
        val recyclerView = searchView.findViewById<RecyclerView>(R.id.search_result)

        // SearchBar와 SearchView 연결
        searchView.setupWithSearchBar(searchBar)

        // 더미 데이터
        val allResults = listOf(
            "미래관 445호", "미래관 447호", "미래관 449호",
            "미래관 444호", "미래관 425호", "미래관 415호", "미래관 405호"
        )

        // 검색어 입력 후 엔터 시 필터링 & 표시
        searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchView.text.toString()
                if (query.isNotBlank()) {
                    val filtered = allResults.filter { it.contains(query, ignoreCase = true) }
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = SearchResultAdapter(filtered)
                    searchView.hide()
                }
                true
            } else {
                false
            }
        }

        // 뒤로가기 처리
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchView.isShowing) {
                    searchView.hide()
                } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime < backPressInterval) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    } else {
                        backPressedTime = currentTime
                    }
                }
            }
        })

        // AR 프래그먼트 초기 진입
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HelloArFragment())
                .commit()
        }

        // 카메라 Pose 표시 on/off
        searchView.addTransitionListener { _, _, newState ->
            HelloArFragment.setCameraPoseVisibility(
                newState != com.google.android.material.search.SearchView.TransitionState.SHOWN
            )
        }
    }
}
