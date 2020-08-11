package com.example.chatting_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText emailEdit, passEdit;
    View mProgressView;
    SignInButton googleLoginBtn;

    Intent intent;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEdit = findViewById(R.id.editEmail);
        passEdit = findViewById(R.id.editPassword);

        mProgressView = (ProgressBar) findViewById(R.id.login_progress);
        googleLoginBtn = findViewById(R.id.google_sign_in_btn);
    }

    public void userSystem(View view) {
        switch (view.getId()) {
            case R.id.loginBtn :
                break;
            case R.id.registerBtn :
                break;
            case R.id.cancelBtn :
                break;
            case R.id.google_sign_in_btn :
                break;
        }
    }
}