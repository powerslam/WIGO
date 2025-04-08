package com.capstone.whereigo

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.whereigo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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
        searchBar.menu.findItem(R.id.action_menu).setOnMenuItemClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_setting, SettingsFragment())
                .addToBackStack(null)
                .commit()
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
    }
}