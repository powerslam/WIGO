package com.capstone.whereigo;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CardFragment extends Fragment {

    private static final String ARG_LABEL = "label";
    private static final String ARG_POSITION = "position";
    private static final String ARG_IMAGE_RES_ID = "image_res_id";

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
        View rootView = inflater.inflate(R.layout.card_node_labeling, container, false);

        // 데이터 받기
        assert getArguments() != null;
        String label = getArguments().getString(ARG_LABEL);
        String position = getArguments().getString(ARG_POSITION);
        int imageResId = getArguments().getInt(ARG_IMAGE_RES_ID);

        ImageView thumbnail = rootView.findViewById(R.id.thumbnail);
        TextView positionText = rootView.findViewById(R.id.pose_stamp);
        EditText editLabel = rootView.findViewById(R.id.edit_stamp);

        // 데이터 설정
        thumbnail.setImageResource(imageResId);
        positionText.setText("위치 : " + position);
        editLabel.setText(label);

        return rootView;
    }
}
