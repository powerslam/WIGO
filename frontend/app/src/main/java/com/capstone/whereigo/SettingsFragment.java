package com.capstone.whereigo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.core.graphics.Insets;


public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.setting_list, new SetDownloadPreference());
        transaction.commit();

        return view;
    }


    public static class SetDownloadPreference extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.download_preference, rootKey);

            Preference mapItemPreference = findPreference("download_map");
            if (mapItemPreference != null) {
                mapItemPreference.setOnPreferenceClickListener(preference -> {
                    Fragment parent = getParentFragment();
                    if (parent != null) {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new DownloadFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                    return true;
                });
            }

            Preference makeMapPreference = findPreference("make_map");
            if (makeMapPreference != null) {
                makeMapPreference.setOnPreferenceClickListener(preference -> {
                    Fragment parent = getParentFragment();
                    if (parent != null) {
                        parent.getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new MappingFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                    return true;
                });
            }
        }
    }
}
