package com.aghakhani.khaterrebaz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class DecadeFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MEMORY = "memory";
    private static final String ARG_DESC = "desc";

    private String title;
    private String memory;
    private String desc;

    public DecadeFragment() {
        // Required empty public constructor
    }

    public static DecadeFragment newInstance(String title, String memory, String desc) {
        DecadeFragment fragment = new DecadeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MEMORY, memory);
        args.putString(ARG_DESC, desc);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            memory = getArguments().getString(ARG_MEMORY);
            desc = getArguments().getString(ARG_DESC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_decade, container, false);

        // Initialize views
        ImageView ivMemory = view.findViewById(R.id.iv_decade_memory);
        TextView tvMemoryText = view.findViewById(R.id.tv_decade_memory_text);
        TextView tvMemoryDesc = view.findViewById(R.id.tv_decade_memory_desc);

        // Set data
        tvMemoryText.setText(memory);
        tvMemoryDesc.setText(desc);
        // اینجا می‌تونی تصویر خاص هر دهه رو ست کنی
        ivMemory.setImageResource(android.R.drawable.ic_menu_gallery);

        return view;
    }
}