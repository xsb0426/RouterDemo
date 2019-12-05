package com.example.module1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.annotation.Route;

@Route("/module1/test")
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }
}
