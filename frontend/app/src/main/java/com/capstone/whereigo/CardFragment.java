package com.capstone.whereigo;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.capstone.whereigo.databinding.CardNodeLabelingBinding;

public class CardFragment extends Fragment {

    private static final String ARG_LABEL = "label";
    private static final String ARG_POSITION = "position";
    private static final String ARG_IMAGE_RES_ID = "image_res_id";

    private CardNodeLabelingBinding binding;

    public static CardFragment newInstance(NodeLabelData nodeData) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LABEL, nodeData.getLabel());
        args.putString(ARG_POSITION, nodeData.getPosition());
        args.putInt(ARG_IMAGE_RES_ID, nodeData.getImageResId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = CardNodeLabelingBinding.inflate(inflater, container, false);

        // 데이터 받기
        assert getArguments() != null;
        String label = getArguments().getString(ARG_LABEL);
        String position = getArguments().getString(ARG_POSITION);
        int imageResId = getArguments().getInt(ARG_IMAGE_RES_ID);

        ImageView thumbnail = binding.thumbnail;
        TextView positionText = binding.poseStamp;
        EditText editLabel = binding.editStamp;

        // 데이터 설정
        thumbnail.setImageResource(imageResId);
        positionText.setText("위치 : " + position);
        editLabel.setText(label);

        return binding.getRoot();
    }
}
