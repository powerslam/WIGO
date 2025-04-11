package com.capstone.whereigo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.whereigo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val RECORD_AUDIO_REQUEST_CODE = 100
    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 1000 // 1초 이내에 두 번 눌러야 종료됨

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

        // 설정 메뉴 클릭 시
        searchBar.menu.findItem(R.id.action_menu).setOnMenuItemClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_setting, SettingsFragment())
                .addToBackStack(null)
                .commit()
            true
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

        searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchView.text.toString()
                Toast.makeText(this, query, Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }

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
                        Toast.makeText(this@MainActivity, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        // Fragment 추가
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HelloArFragment())
                .commit()
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