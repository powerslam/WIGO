package com.capstone.whereigo;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.capstone.whereigo.databinding.FragmentNodeLabelingBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class NodeLabelingDialog extends DialogFragment implements DialogInterface.OnDismissListener {
    private static final String ARG_NODE_IDX_LIST = "arg_node_idx_list";
    private static final String ARG_NODE_POSE_STAMP_DATA_X_LIST = "arg_node_pose_stamp_data_x_list";
    private static final String ARG_NODE_POSE_STAMP_DATA_Y_LIST = "arg_node_pose_stamp_data_y_list";
    private static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9\\- _]+$");

    private FragmentNodeLabelingBinding binding;
    List<NodeLabelData> nodeList;

    @FunctionalInterface
    public interface SendDataInterface {
        void sendData(String[] data);
    }

    public SendDataInterface sendData;

    public static NodeLabelingDialog newInstance(ArrayList<Integer> indexList, float[] xList, float[] yList) {
        NodeLabelingDialog fragment = new NodeLabelingDialog();
        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_NODE_IDX_LIST, indexList);
        args.putFloatArray(ARG_NODE_POSE_STAMP_DATA_X_LIST, xList);
        args.putFloatArray(ARG_NODE_POSE_STAMP_DATA_Y_LIST, yList);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentNodeLabelingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = binding.dialogViewPager;

        assert getArguments() != null;
        ArrayList<Integer> indexList = getArguments().getIntegerArrayList(ARG_NODE_IDX_LIST);
        float[] xArray = getArguments().getFloatArray(ARG_NODE_POSE_STAMP_DATA_X_LIST);
        float[] yArray = getArguments().getFloatArray(ARG_NODE_POSE_STAMP_DATA_Y_LIST);
        assert indexList != null;
        assert xArray != null;
        assert yArray != null;

        nodeList = new ArrayList<>();
        for(int i = 0; i < indexList.size(); i++){
            nodeList.add(new NodeLabelData(
                    String.format(Locale.ROOT, "노드 %d", indexList.get(i)),
                    String.format(Locale.ROOT, "(%f, %f)", xArray[i], yArray[i]),
                    R.drawable.test)
            );
        }

        nodeList.add(new NodeLabelData(
                "",
                "-",
                R.drawable.test)
        );

        CardPagerAdapter adapter = new CardPagerAdapter(requireActivity(), nodeList);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new MarginPageTransformer(30));
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2, false); // 무한 슬라이딩처럼

        Button btn = binding.buttonLabelingDone;
        btn.setOnClickListener(v -> {
            int backStackCount = requireActivity().getSupportFragmentManager().getBackStackEntryCount();
            Toast.makeText(requireContext(), "저장이완료됐어용가리치킨더조이 : " + backStackCount, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HelloArFragment())
                    .commit();
            dismiss();
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (sendData != null) {
            int size = nodeList.size();
            String[] labels = new String[size];
            for(int i = 0; i < size; i++){
                labels[i] = nodeList.get(i).getLabel();
            }

            if(!VALID_FILENAME_PATTERN.matcher(labels[size - 1]).matches()){
                labels[size - 1] = "새로운 지도";
            }

            sendData.sendData(labels);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
