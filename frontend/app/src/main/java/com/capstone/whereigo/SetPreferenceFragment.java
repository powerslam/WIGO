package com.capstone.whereigo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import androidx.activity.OnBackPressedCallback;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Objects;

public class SetPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.download_preference, rootKey);


        SwitchPreferenceCompat vibratePref = findPreference("vibrate");
        if (vibratePref != null) {
            boolean isVibrateOn = Objects.requireNonNull(getPreferenceManager().getSharedPreferences())
                    .getBoolean("vibrate", true);
        }

        SwitchPreferenceCompat sdcardPref = findPreference("sdcard");
        //여기에 마저 sdcard 설정 하기

        Preference mapItemPreference = findPreference("download_map");
        if (mapItemPreference != null) {
            mapItemPreference.setOnPreferenceClickListener(preference -> {
                TextView tv = requireActivity().findViewById(R.id.toolbar_title);
                tv.setText(R.string.download_title);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.setting_list, new DownloadFragment())
                        .commit();
                return true;
            });
        }

        Preference makeMapPreference = findPreference("make_map");
        if (makeMapPreference != null) {
            makeMapPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), MappingActivity.class);
                startActivity(intent);
                requireActivity().finish();
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(requireContext(), SearchActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }
}
