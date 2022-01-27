package com.example.meta.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.meta.Other.Rotation3DSample;
import com.example.meta.R;
import com.kiprotich.japheth.TextAnim;

import su.levenetc.android.textsurface.TextSurface;

public class MainActivity extends AppCompatActivity {
    AppCompatButton mLoginBtn,mRegisterBtn;
    private TextAnim textWriter;
    private TextSurface textSurface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoginBtn = findViewById(R.id.login_btn);
        mRegisterBtn = findViewById(R.id.register_btn);
        textWriter = findViewById(R.id.textWriter);
        textSurface = findViewById(R.id.textSurface);
        textWriter
                .setWidth(12)
                .setDelay(30)
                .setColor(Color.BLACK)
                .setConfig(TextAnim.Configuration.INTERMEDIATE)
                .setSizeFactor(35f)
                .setLetterSpacing(25f)
                .setText("ALO CHAT")
//                .setListener(() -> {
//                    //do stuff after animation is finished
//                })
                .startAnimation();
        mRegisterBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
            startActivity(intent);
        });
        mLoginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        });
        textSurface.postDelayed(() -> {
            show();
        }, 1500);
    }

    private void show() {
        Rotation3DSample.play(textSurface);
    }
}