package com.capstone.whereigo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Fragment 내부에 또 Fragment 넣기
        childFragmentManager.beginTransaction()
            .replace(R.id.settings1, SetSoundPreference())
            .replace(R.id.settings2, SetDownloadPreference())
            .commit()

        return view
    }

    class SetSoundPreference : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.sound_preference, rootKey)
        }
    }

    class SetDownloadPreference : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.download_preference, rootKey)

            val mapItemPreference: Preference? = findPreference("map_download")

            mapItemPreference?.setOnPreferenceClickListener {
                // SettingsFragment의 parentFragment를 통해 DownloadFragment를 띄움
                parentFragment?.parentFragmentManager?.beginTransaction()
                    ?.replace(R.id.fragment_setting, DownloadFragment()) // ⚠️ 여기에 실제 container ID 써야 함
                    ?.addToBackStack(null)
                    ?.commit()
                true
            }
        }
    }

}
