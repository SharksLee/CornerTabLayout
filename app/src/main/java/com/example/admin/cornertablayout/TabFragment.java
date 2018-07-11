package com.example.admin.cornertablayout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TabFragment extends Fragment {

    private static final String KEY_TITLE = "KEY_TITLE";

    public static TabFragment newInstance(String title) {

        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fr_tab, container, false);
        TextView textView = root.findViewById(R.id.title);
        textView.setText(getArguments().getString(KEY_TITLE));

        return root;
    }
}
