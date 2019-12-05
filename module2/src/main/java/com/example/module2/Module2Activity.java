package com.example.module2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.annotation.Autowired;
import com.example.annotation.Route;
import com.example.router.Router;


@Route("/module2/activity")
public class Module2Activity extends AppCompatActivity {

    @Autowired
    String param;

//    @Autowired("param")  //注解不传参数，默认取field名称
//    String param;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module2);
        //如果要使用 Autowired 注解，必须在初始化方法中注入
        Router.getInstance().inject(this);
        TextView textView = findViewById(R.id.text_view);
//        param = getIntent().getStringExtra("param");
        textView.setText(param);
    }
}
