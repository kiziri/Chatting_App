package com.example.chatting_app.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatting_app.R;
import com.example.chatting_app.models.RemoteService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEdit, passEdit, nameEdit;
    private FirebaseAuth mAuth;
    private FirebaseUser fUser;
    private DatabaseReference mUserRef;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String getUserEmail, getUserPass, getUserName;

    private Retrofit retrofit;
    private RemoteService remoteService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("신규 회원 가입");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailEdit = findViewById(R.id.editEmail);
        passEdit = findViewById(R.id.editPassword);
        nameEdit = findViewById(R.id.editName);


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

    public void userRegister(String userEmail, String userPassword, String userName) {
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    fUser = mAuth.getCurrentUser();
                    Toast.makeText(RegisterActivity.this, "User Register Successful", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(RegisterActivity.this, "User register Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
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