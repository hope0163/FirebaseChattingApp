package com.hope0163.chattingapp;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hope0163.chattingapp.model.Chatting;

public class FullScreenActivity extends AppCompatActivity {

    ImageView imageView;
    Chatting chatting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        imageView = findViewById(R.id.imageView);

        String photoUrl = getIntent().getStringExtra("photoUrl");

        Glide.with(FullScreenActivity.this).load(photoUrl).into(imageView);
    }
}