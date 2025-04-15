package com.capstone.whereigo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import SearchResultAdapter
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.whereigo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val RECORD_AUDIO_REQUEST_CODE = 100
    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val searchBar = binding.searchBar
        val searchView = binding.searchView
        searchView.setupWithSearchBar(searchBar)

        val searchMenu = R.menu.search_menu
        searchBar.inflateMenu(searchMenu)

        // 설정 버튼 → SettingsFragment 전환
        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_setting, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        // 음성 검색 클릭
        searchBar.menu.findItem(R.id.action_voice_search).setOnMenuItemClickListener {
            if (checkAudioPermission()) {
                if (SpeechRecognizer.isRecognitionAvailable(this)) {
                    val dialog = VoiceRecordDialog()
                    dialog.show(supportFragmentManager, "VoiceRecordDialog")
                } else {
                    Toast.makeText(this, "음성 인식을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestAudioPermission()
            }
            true
        }

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

        setupWakeWordListener()
    }
    // 오디오 권한 확인
    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 오디오 권한 요청
    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "음성 권한이 허용되었습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "음성 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupWakeWordListener() {
        if (!checkAudioPermission()) {
            requestAudioPermission()
            return
        }
    }
}
