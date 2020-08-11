package com.example.chatting_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static int GOOGLE_LOGIN = 10;

    EditText emailEdit, passEdit;
    View mProgressView;
    SignInButton googleLoginBtn;

    Intent intent;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseUser fUser;

    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleApiClient mGoogleAPIClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEdit = findViewById(R.id.editEmail);
        passEdit = findViewById(R.id.editPassword);

        mProgressView = (ProgressBar) findViewById(R.id.login_progress);
        googleLoginBtn = findViewById(R.id.google_sign_in_btn);

        mAuth = FirebaseAuth.getInstance();
        fUser = mAuth.getCurrentUser();
        if (fUser != null) {
            startActivity(new Intent(LoginActivity.this, ChatHomeActivity.class));
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance();
        mUserRef = mDatabase.getReference("Users");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();  // 구글로 로그인 설정부

        mGoogleAPIClient = new GoogleApiClient.Builder(this).enableAutoManage(this /*FragmentActivity*/, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                // 실패 시 처리 하는 부분.
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions).build();

        googleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
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

    // 구글 로그인 화면 실행 단계용 메소드
    private void signInWithGoogle() {
        System.out.println("-------------------");
        intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleAPIClient);
        startActivityForResult(intent, GOOGLE_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isComplete()) {
                    if (task.isSuccessful()) {
                        fUser = task.getResult().getUser();
                        final User user = new User();
                        user.setEmail(fUser.getEmail());
                        user.setName(fUser.getDisplayName());
                        user.setUid(fUser.getUid());
                        if (fUser.getPhotoUrl() != null) { user.setProfileUrl(fUser.getPhotoUrl().toString()); }

                        mUserRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    mUserRef.child(user.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                startActivity(new Intent(LoginActivity.this, ChatHomeActivity.class));
                                                finish();
                                            }
                                        }
                                    });
                                }
                                else {
                                    startActivity(new Intent(LoginActivity.this, ChatHomeActivity.class));
                                    finish();
                                }

                                Bundle eventBundle = new Bundle();
                                eventBundle.putString("email", user.getEmail());
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, eventBundle);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    } else {
                        Snackbar.make(mProgressView, "로그인 실패", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(mProgressView, "로그인 실패", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}