package com.example.module2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.annotation.Route;

/**
 * @author xushibin
 * @date 2019-11-14
 * description：
 */
@Route("/module2/fragment")
public class Module2Fragment extends Fragment {

    private String param;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        param = getArguments().getString("param");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_module2, container, false);
        TextView textView = view.findViewById(R.id.text_view);
        textView.setText(param);
        return view;
    }
}
