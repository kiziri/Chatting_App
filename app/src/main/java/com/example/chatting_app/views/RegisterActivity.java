package com.example.chatting_app.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.chatting_app.R;
import com.example.chatting_app.models.RemoteService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    EditText emailEdit, passEdit, nameEdit;
    FirebaseAuth mAuth;
    FirebaseUser user;

    Retrofit retrofit;
    RemoteService remoteService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("신규 사용자 등록");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void userSystem(View view) {
        switch (view.getId()) {
            case R.id.loginBtn :
                break;
            case R.id.registerBtn :
                break;
            case R.id.cancelBtn :
                break;
        }
    }

    public void userRegister() {
        
    }

    // 액션바 뒤로가기 버튼 이벤트 구현부
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}