package com.example.module1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.annotation.Autowired;
import com.example.annotation.Route;
import com.example.module1.databinding.ActivityModule1Binding;
import com.example.router.Router;
import com.zdww.baselib.ITestProvider;

@Route("/module1/activity")
public class Module1Activity extends AppCompatActivity {

    @Autowired("/module2/provider")
    ITestProvider iTestProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityModule1Binding binding = DataBindingUtil.setContentView(this, R.layout.activity_module1);

        Router.getInstance().inject(this);

        //activity
        binding.btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("param", "from module1");
                Router.getInstance().build("/module2/activity").withBundle(bundle).navigation();
            }
        });

        //fragment
        binding.btnView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("param", "from module1");
                Fragment fragment = (Fragment) Router.getInstance().build("/module2/fragment").withBundle(bundle).navigation();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        //接口通信
        binding.btnView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //导航方式
//                ITestProvider iTestProvider = (ITestProvider) Router.getInstance().build("/module2/provider").navigation();
                if (iTestProvider != null) {
                    String module1 = iTestProvider.test("module1");
                    Toast.makeText(Module1Activity.this, module1, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
