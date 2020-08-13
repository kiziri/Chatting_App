package com.example.chatting_app.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatting_app.R;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String uid = getIntent().getStringExtra("uid");
        String[] uids = getIntent().getStringArrayExtra("uids");

    }
}