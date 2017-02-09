package com.passwordinputview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.textView);
        PasswordInputView inputWordView = (PasswordInputView) findViewById(R.id.inputView);
        inputWordView.setOnGetInputListener(new PasswordInputView.OnGetInputListener() {
            @Override
            public void getInput(String result) {
                textView.setText(result);
            }
        });
    }
}
