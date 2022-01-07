package com.mobile.eznote.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mobile.eznote.R;

public class LoginActivity extends AppCompatActivity {
    private EditText mloginemail, mloginpassword;
    private RelativeLayout mlogin, mgotosignup;
    private TextView mgotoforgotpassword;
    public FirebaseAuth mAuth;

    private String EMAIL_KEY = "EMAIL_KEY";
    private String PASSWORD_KEY = "EMAIL_KEY";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = getSharedPreferences("loginPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mAuth = FirebaseAuth.getInstance();
        String email = sharedPreferences.getString(EMAIL_KEY, "null");
        String password = sharedPreferences.getString(PASSWORD_KEY, "null");

        if (!email.equals("null")) {
            mAuth.signInWithEmailAndPassword(email, password);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        } else {
            getSupportActionBar().hide();
            mloginemail = findViewById(R.id.Loginemail);
            mloginpassword = findViewById(R.id.Loginpassword);
            mlogin = findViewById(R.id.dangnhap);
            mgotoforgotpassword = findViewById(R.id.gotoforgotpassword);
            mgotosignup = findViewById(R.id.gotosignup);
            mgotosignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                }
            });
            mgotoforgotpassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(LoginActivity.this, ForgetPasswodActivity.class));
                }
            });
            mlogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String mail = mloginemail.getText().toString().trim();
                    String password = mloginpassword.getText().toString().trim();
                    if (mail.isEmpty() || password.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Bạn phải điền tất cả!", Toast.LENGTH_SHORT).show();
                    } else {
                        mAuth.signInWithEmailAndPassword(mail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();

                                    //sau khi dang nhap thanh cong thi tat man hinh login de test upload
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    //luu lai tai khoan
                                    savelogin(mail,password);
                                    finish();
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });

        }
    }

    void savelogin(String email, String password) {
        sharedPreferences = getSharedPreferences("loginPref", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(EMAIL_KEY, email);
        editor.putString(PASSWORD_KEY, password);
        editor.commit();
    }

}